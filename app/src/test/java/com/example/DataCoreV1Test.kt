package com.example

import com.example.core.datacore.metrics.MetricCatalog
import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.RawDataInput
import com.example.core.datacore.model.SourceTier
import com.example.core.datacore.model.ValidationStatus
import com.example.core.datacore.pipeline.DataCorePipeline
import com.example.core.datacore.protocols.ProtocolCatalog
import com.example.core.datacore.reliability.ReliabilityFramework
import com.example.core.datacore.validation.ValidationEngineResult
import com.example.core.datacore.validation.ValidationEngineV1
import com.example.core.error.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class DataCoreV1Test {

    private lateinit var pipeline: DataCorePipeline
    private lateinit var validator: ValidationEngineV1

    @Before
    fun setup() {
        validator = ValidationEngineV1()
        pipeline = DataCorePipeline(validator)
    }

    // =========================================================================
    // 1. PIPELINE TEST: INPUT -> RAW DATA -> VALIDATION -> MEASUREMENT -> EVIDENCE
    // =========================================================================
    @Test
    fun pipeline_validMeasurement_generatesEvidenceAndProvenanceWithAudit() {
        val currentTime = System.currentTimeMillis()
        val input = RawDataInput(
            userId = "USR-1001",
            assessmentId = "ASM-2001",
            metricId = MetricCatalog.METRIC_VO2_MAX,
            rawPayload = "54.5",
            unit = "ml/kg/min",
            source = "Polar H10 BLE ECG",
            sourceType = "CLINICAL_WEARABLE",
            sourceIdentifier = "POLAR-H10-SN982341",
            deviceId = "POLAR_H10_01",
            protocolId = ProtocolCatalog.PROTO_BASELINE_SUBMAX_VO2,
            clientTimestamp = currentTime - 5000,
            serverTimestamp = currentTime,
            isMock = false
        )

        val result = pipeline.ingestRawData(input)

        assertTrue("Pipeline deve aceitar medição válida", result is AppResult.Success)
        val data = (result as AppResult.Success).data

        // 1. Measurement verificado
        assertEquals("USR-1001", data.measurement.userId)
        assertEquals(54.5, data.measurement.rawValue, 0.001)
        assertEquals(ValidationStatus.VALID, data.measurement.validationStatus)
        assertNull(data.measurement.rejectionReason)

        // 2. Provenance verificada com Hash Criptográfico
        assertEquals(ProtocolCatalog.PROTO_BASELINE_SUBMAX_VO2, data.provenance.protocolId)
        assertNotNull(data.provenance.integrityHash)
        assertTrue(data.provenance.integrityHash!!.length == 64) // SHA-256

        // 3. Evidence gerada
        assertTrue("ID da evidência deve seguir padrão EV-2026-XXXXXX", data.evidence.id.startsWith("EV-2026-"))
        assertEquals(IntegrityStatus.VALID, data.evidence.integrityStatus)
        assertFalse(data.evidence.isMock)

        // 4. Confiabilidade estruturada
        assertEquals(SourceTier.TIER_2_CLINICAL_WEARABLE, data.reliability.sourceTier)
        assertTrue(data.reliability.integrityValid)
        assertTrue(data.reliability.compositeConfidenceScore > 0.0)

        // 5. Audit Log imutável
        assertEquals("Evidence", data.auditLog.entityType)
        assertEquals("EVIDENCE_CREATED", data.auditLog.action)
    }

    // =========================================================================
    // 2. VALIDATION ENGINE: OUT OF RANGE REJECTION
    // =========================================================================
    @Test
    fun validator_outOfRangeMeasurement_isRejectedWithExplicitReason() {
        val input = RawDataInput(
            userId = "USR-1001",
            metricId = MetricCatalog.METRIC_VO2_MAX,
            rawPayload = "140.0", // Fisiologicamente impossível (max permitido: 95.0)
            unit = "ml/kg/min",
            source = "Sensor",
            sourceType = "DIRECT_CALIBRATED_SENSOR",
            sourceIdentifier = "SN-001",
            protocolId = ProtocolCatalog.PROTO_BASELINE_SUBMAX_VO2,
            clientTimestamp = System.currentTimeMillis()
        )

        val validation = validator.validateRawData(input)

        assertTrue(validation is ValidationEngineResult.Invalid)
        val reason = (validation as ValidationEngineResult.Invalid).reason
        assertTrue("Motivo deve explicar intervalo ultrapassado", reason.contains("fora do intervalo permitido"))
    }

    // =========================================================================
    // 3. VALIDATION ENGINE: INCOMPATIBLE UNIT REJECTION
    // =========================================================================
    @Test
    fun validator_incompatibleUnit_isRejected() {
        val input = RawDataInput(
            userId = "USR-1001",
            metricId = MetricCatalog.METRIC_RFD,
            rawPayload = "2500.0",
            unit = "kg", // Incompatível com RFD (esperado: N/s)
            source = "Force Plate",
            sourceType = "DIRECT_CALIBRATED_SENSOR",
            sourceIdentifier = "FP-01",
            protocolId = ProtocolCatalog.PROTO_IMTP_RFD,
            clientTimestamp = System.currentTimeMillis()
        )

        val validation = validator.validateRawData(input)

        assertTrue(validation is ValidationEngineResult.Rejected)
        val reason = (validation as ValidationEngineResult.Rejected).reason
        assertTrue("Deve rejeitar unidade incompatível", reason.contains("Rejeição de Unidade"))
    }

    // =========================================================================
    // 4. VALIDATION ENGINE: FUTURE TIMESTAMP REJECTION
    // =========================================================================
    @Test
    fun validator_futureTimestamp_isRejected() {
        val futureTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 min no futuro
        val input = RawDataInput(
            userId = "USR-1001",
            metricId = MetricCatalog.METRIC_HRV_RMSSD,
            rawPayload = "65.0",
            unit = "ms",
            source = "ECG",
            sourceType = "CLINICAL_WEARABLE",
            sourceIdentifier = "ECG-01",
            protocolId = ProtocolCatalog.PROTO_RESTING_HRV_RMSSD,
            clientTimestamp = futureTime
        )

        val validation = validator.validateRawData(input)

        assertTrue(validation is ValidationEngineResult.Rejected)
        val reason = (validation as ValidationEngineResult.Rejected).reason
        assertTrue("Deve rejeitar carimbo temporal futuro", reason.contains("Timestamp do cliente no futuro"))
    }

    // =========================================================================
    // 5. VALIDATION ENGINE: DUPLICATION DETECTION
    // =========================================================================
    @Test
    fun validator_duplicateInput_isDetectedAndRejected() {
        val fixedTime = System.currentTimeMillis() - 1000
        val input1 = RawDataInput(
            userId = "USR-1001",
            metricId = MetricCatalog.METRIC_RELATIVE_STRENGTH,
            rawPayload = "1.65",
            unit = "1RM/BW ratio",
            source = "LPT Encoder",
            sourceType = "DIRECT_CALIBRATED_SENSOR",
            sourceIdentifier = "LPT-1000HZ-01",
            protocolId = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
            clientTimestamp = fixedTime
        )

        val val1 = validator.validateRawData(input1)
        assertTrue(val1 is ValidationEngineResult.Valid)

        // Submissão duplicada idêntica
        val inputDuplicate = input1.copy(id = UUID.randomUUID().toString())
        val val2 = validator.validateRawData(inputDuplicate)

        assertTrue(val2 is ValidationEngineResult.Rejected)
        val reason = (val2 as ValidationEngineResult.Rejected).reason
        assertTrue("Deve acusar duplicação", reason.contains("Rejeição de Duplicação"))
    }

    // =========================================================================
    // 6. MOCK DATA ISOLATION
    // =========================================================================
    @Test
    fun pipeline_mockData_isExplicitlyFlaggedAndSeparatedFromOfficialData() {
        val input = RawDataInput(
            userId = "USR-1001",
            metricId = MetricCatalog.METRIC_CRITICAL_POWER,
            rawPayload = "280.0",
            unit = "W",
            source = "Mock Simulator",
            sourceType = "MANUAL_INPUT",
            sourceIdentifier = "SIMULATOR-00",
            protocolId = ProtocolCatalog.PROTO_CRITICAL_POWER_3MIN_ALL_OUT,
            clientTimestamp = System.currentTimeMillis() - 1000,
            isMock = true
        )

        val result = pipeline.ingestRawData(input)
        assertTrue(result is AppResult.Success)

        val data = (result as AppResult.Success).data
        assertTrue("Medição deve ser marcada como isMock", data.measurement.isMock)
        assertTrue("Evidência deve ser marcada como isMock", data.evidence.isMock)
        assertTrue("Audit deve registrar que se trata de mock", data.auditLog.newState!!.contains("IS_MOCK=true"))
    }

    // =========================================================================
    // 7. EXPLAINABILITY SHEETS RETRIEVAL FOR ALL CATALOG METRICS
    // =========================================================================
    @Test
    fun metricCatalog_allRequiredMetricsContainCompleteExplainabilitySheets() {
        val metrics = MetricCatalog.getAllMetrics()
        assertEquals("Devem existir 10 métricas centrais cadastradas", 10, metrics.size)

        for (metric in metrics) {
            val exp = metric.explainability
            assertNotNull("Explainability sheet não pode ser nula para ${metric.id}", exp)
            assertTrue("whatIsIt deve estar preenchido para ${metric.id}", exp.whatIsIt.isNotBlank())
            assertTrue("howIsMeasured deve estar preenchido para ${metric.id}", exp.howIsMeasured.isNotBlank())
            assertTrue("protocol deve estar preenchido para ${metric.id}", exp.protocol.isNotBlank())
            assertTrue("instrumentOrSensor deve estar preenchido para ${metric.id}", exp.instrumentOrSensor.isNotBlank())
            assertTrue("howIsCalculated deve estar preenchido para ${metric.id}", exp.howIsCalculated.isNotBlank())
            assertTrue("methodVersion deve estar preenchido para ${metric.id}", exp.methodVersion.isNotBlank())
            assertTrue("evidenceRequirement deve estar preenchido para ${metric.id}", exp.evidenceRequirement.isNotBlank())
        }
    }
}
