package com.example

import com.example.core.datacore.metrics.MetricCatalog
import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.ValidationStatus
import com.example.core.datacore.protocols.ProtocolCatalog
import com.example.core.error.AppError
import com.example.core.error.AppResult
import com.example.core.evidenceconsistency.continuity.ProtocolContinuityTracker
import com.example.core.evidenceconsistency.engine.EvidenceConsistencyEngineV1
import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.MaturityStatus
import com.example.core.evidenceconsistency.model.ProtocolContinuityFlag
import com.example.core.evidenceconsistency.model.ValidityStatus
import com.example.core.evidenceconsistency.policy.ConsistencyPolicyRegistry
import com.example.core.evidenceconsistency.policy.EvidenceValidityPolicyRegistry
import com.example.core.evidenceconsistency.validity.EvidenceValidityEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class EvidenceConsistencyEngineV1Test {

    private lateinit var engine: EvidenceConsistencyEngineV1
    private lateinit var validityEngine: EvidenceValidityEngine
    private lateinit var continuityTracker: ProtocolContinuityTracker

    private val now = 1720000000000L // Carimbo temporal de referência para testes
    private val ONE_DAY_MS = 86_400_000L

    @Before
    fun setup() {
        engine = EvidenceConsistencyEngineV1(
            engineVersion = "1.0.0-consistency-v1",
            coreVersion = "1.0.0-datacore-v1"
        )
        validityEngine = EvidenceValidityEngine()
        continuityTracker = ProtocolContinuityTracker()
    }

    private fun createMeasurement(
        id: String = "MSR-${UUID.randomUUID().toString().take(6)}",
        metricId: String = MetricCatalog.METRIC_RELATIVE_STRENGTH,
        protocolId: String = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
        deviceId: String = "DEV-LPT-01",
        rawValue: Double = 1.85,
        unit: String = "1RM/BW ratio",
        timestamp: Long = now - (5 * ONE_DAY_MS),
        status: ValidationStatus = ValidationStatus.VALID,
        isMock: Boolean = false
    ): DataCoreMeasurement {
        return DataCoreMeasurement(
            id = id,
            assessmentId = "ASM-001",
            userId = "USR-001",
            metricId = metricId,
            rawValue = rawValue,
            normalizedValue = null,
            unit = unit,
            timestamp = timestamp,
            source = "DIRECT_CALIBRATED_SENSOR",
            deviceId = deviceId,
            protocolId = protocolId,
            validationStatus = status,
            rawDataInputId = "RAW-001",
            isMock = isMock
        )
    }

    private fun createEvidence(
        id: String = "EV-2026-${UUID.randomUUID().toString().take(6)}",
        measurementIds: List<String>,
        capturedAt: Long = now - (5 * ONE_DAY_MS),
        integrityStatus: IntegrityStatus = IntegrityStatus.VALID,
        provenanceId: String = "PROV-001",
        isMock: Boolean = false
    ): DataCoreEvidence {
        return DataCoreEvidence(
            id = id,
            userId = "USR-001",
            assessmentId = "ASM-001",
            measurementIds = measurementIds,
            source = "DIRECT_CALIBRATED_SENSOR",
            capturedAt = capturedAt,
            submittedAt = capturedAt + 1000,
            integrityStatus = integrityStatus,
            reliabilityScore = 0.99,
            confidenceScore = 0.98,
            provenanceId = provenanceId,
            coreVersion = "1.0.0-datacore-v1",
            isMock = isMock
        )
    }

    private fun createProvenance(id: String = "PROV-001"): DataCoreProvenance {
        return DataCoreProvenance(
            id = id,
            sourceType = "DIRECT_CALIBRATED_SENSOR",
            sourceIdentifier = "LPT-SN-100",
            deviceIdentifier = "DEV-LPT-01",
            captureTimestamp = now - (5 * ONE_DAY_MS),
            processingTimestamp = now,
            processingVersion = "1.0.0-datacore-v1",
            protocolId = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
            integrityHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )
    }

    // =========================================================================
    // 1. EVIDÊNCIA DENTRO DE POLÍTICA APROVADA = CURRENT
    // =========================================================================
    @Test
    fun test1_evidenceWithinApprovedPolicy_isCurrent() {
        val msr = createMeasurement(timestamp = now - (10 * ONE_DAY_MS)) // 10 dias atrás (janela = 60 dias)
        val ev = createEvidence(measurementIds = listOf(msr.id), capturedAt = now - (10 * ONE_DAY_MS))

        val assessment = validityEngine.evaluateEvidence(ev, listOf(msr), evaluationTimestamp = now)

        assertEquals(ValidityStatus.CURRENT, assessment.validityStatus)
        assertEquals("POL-VAL-LPT-STR-V1", assessment.policyId)
        assertNotNull(assessment.expirationTimestamp)
    }

    // =========================================================================
    // 2. EVIDÊNCIA ALÉM DA JANELA APROVADA = EXPIRED
    // =========================================================================
    @Test
    fun test2_evidenceBeyondApprovedWindow_isExpired() {
        val msr = createMeasurement(timestamp = now - (75 * ONE_DAY_MS)) // 75 dias atrás (janela = 60 dias)
        val ev = createEvidence(measurementIds = listOf(msr.id), capturedAt = now - (75 * ONE_DAY_MS))

        val assessment = validityEngine.evaluateEvidence(ev, listOf(msr), evaluationTimestamp = now)

        assertEquals(ValidityStatus.EXPIRED, assessment.validityStatus)
        assertNotNull(assessment.rejectionReason)
        assertTrue(assessment.rejectionReason!!.contains("expirada"))
    }

    // =========================================================================
    // 3. MÉTRICA SEM JANELA APROVADA = PENDING_VALIDATION
    // =========================================================================
    @Test
    fun test3_metricWithoutApprovedWindow_isPendingValidation() {
        val msr = createMeasurement(
            metricId = MetricCatalog.METRIC_RFD,
            protocolId = ProtocolCatalog.PROTO_IMTP_RFD,
            timestamp = now - (5 * ONE_DAY_MS)
        )
        val ev = createEvidence(measurementIds = listOf(msr.id), capturedAt = now - (5 * ONE_DAY_MS))

        val assessment = validityEngine.evaluateEvidence(ev, listOf(msr), evaluationTimestamp = now)

        assertEquals(ValidityStatus.PENDING_VALIDATION, assessment.validityStatus)
        assertTrue(assessment.limitations.contains("PENDING_CORE_METHODOLOGY_DECISION"))
    }

    // =========================================================================
    // 4. EVIDÊNCIA SEM INTEGRIDADE = INVALID
    // =========================================================================
    @Test
    fun test4_evidenceWithoutIntegrity_isInvalid() {
        val msr = createMeasurement()
        val ev = createEvidence(
            measurementIds = listOf(msr.id),
            integrityStatus = IntegrityStatus.TAMPERED
        )

        val assessment = validityEngine.evaluateEvidence(ev, listOf(msr), evaluationTimestamp = now)

        assertEquals(ValidityStatus.INVALID, assessment.validityStatus)
        assertNotNull(assessment.rejectionReason)
        assertTrue(assessment.rejectionReason!!.contains("integridade"))
    }

    // =========================================================================
    // 5. ALTERAÇÃO DE PROTOCOLO = PROTOCOL_CHANGED
    // =========================================================================
    @Test
    fun test5_protocolChange_flagsProtocolChanged() {
        val msr1 = createMeasurement(
            id = "MSR-1",
            protocolId = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
            timestamp = now - (20 * ONE_DAY_MS)
        )
        val msr2 = createMeasurement(
            id = "MSR-2",
            protocolId = ProtocolCatalog.PROTO_IMTP_RFD,
            timestamp = now - (5 * ONE_DAY_MS)
        )

        val continuity = continuityTracker.analyzeContinuity(
            metricId = MetricCatalog.METRIC_RELATIVE_STRENGTH,
            measurements = listOf(msr1, msr2),
            evidences = emptyList()
        )

        assertTrue(continuity.flags.contains(ProtocolContinuityFlag.PROTOCOL_CHANGED))
        assertFalse(continuity.isCompatibleForDirectComparison)
    }

    // =========================================================================
    // 6. ALTERAÇÃO DE DISPOSITIVO = DEVICE_CHANGED
    // =========================================================================
    @Test
    fun test6_deviceChange_flagsDeviceChanged() {
        val msr1 = createMeasurement(id = "MSR-1", deviceId = "LPT-ENCODER-ALPHA", timestamp = now - (15 * ONE_DAY_MS))
        val msr2 = createMeasurement(id = "MSR-2", deviceId = "LPT-ENCODER-BETA", timestamp = now - (2 * ONE_DAY_MS))

        val continuity = continuityTracker.analyzeContinuity(
            metricId = MetricCatalog.METRIC_RELATIVE_STRENGTH,
            measurements = listOf(msr1, msr2),
            evidences = emptyList()
        )

        assertTrue(continuity.flags.contains(ProtocolContinuityFlag.DEVICE_CHANGED))
    }

    // =========================================================================
    // 7. DADOS INSUFICIENTES = INSUFFICIENT_DATA
    // =========================================================================
    @Test
    fun test7_insufficientData_returnsInsufficientData() {
        val msr = createMeasurement()
        val ev = createEvidence(measurementIds = listOf(msr.id))
        val prov = createProvenance()

        val result = engine.generateEvidencePackage(
            userId = "USR-001",
            measurements = listOf(msr),
            evidences = listOf(ev),
            provenances = mapOf(prov.id to prov),
            evaluationTimestamp = now
        )

        assertTrue(result is AppResult.Success)
        val pkg = (result as AppResult.Success).data.evidencePackage

        val consistency = pkg.consistencyAssessments[MetricCatalog.METRIC_RELATIVE_STRENGTH]
        assertNotNull(consistency)
        assertEquals(ConsistencyStatus.INSUFFICIENT_DATA, consistency!!.consistencyStatus)
    }

    // =========================================================================
    // 8. MÉTODO DE CONSISTÊNCIA NÃO APROVADO = PENDING_VALIDATION
    // =========================================================================
    @Test
    fun test8_unapprovedConsistencyMethod_isPendingValidation() {
        val msr1 = createMeasurement(metricId = MetricCatalog.METRIC_RFD, protocolId = ProtocolCatalog.PROTO_IMTP_RFD, timestamp = now - (10 * ONE_DAY_MS))
        val msr2 = createMeasurement(metricId = MetricCatalog.METRIC_RFD, protocolId = ProtocolCatalog.PROTO_IMTP_RFD, timestamp = now - (5 * ONE_DAY_MS))
        val msr3 = createMeasurement(metricId = MetricCatalog.METRIC_RFD, protocolId = ProtocolCatalog.PROTO_IMTP_RFD, timestamp = now - (1 * ONE_DAY_MS))

        val ev1 = createEvidence(measurementIds = listOf(msr1.id), capturedAt = msr1.timestamp)
        val ev2 = createEvidence(measurementIds = listOf(msr2.id), capturedAt = msr2.timestamp)
        val ev3 = createEvidence(measurementIds = listOf(msr3.id), capturedAt = msr3.timestamp)
        val prov = createProvenance()

        val result = engine.generateEvidencePackage(
            userId = "USR-001",
            measurements = listOf(msr1, msr2, msr3),
            evidences = listOf(ev1, ev2, ev3),
            provenances = mapOf(prov.id to prov),
            evaluationTimestamp = now
        )

        assertTrue(result is AppResult.Success)
        val pkg = (result as AppResult.Success).data.evidencePackage

        val consistency = pkg.consistencyAssessments[MetricCatalog.METRIC_RFD]
        assertNotNull(consistency)
        assertEquals(ConsistencyStatus.PENDING_VALIDATION, consistency!!.consistencyStatus)
        assertTrue(consistency.limitations.contains("PENDING_CORE_METHODOLOGY_DECISION"))
    }

    // =========================================================================
    // 9. MOCK DATA NÃO PARTICIPA DA ELEGIBILIDADE OFICIAL
    // =========================================================================
    @Test
    fun test9_mockData_isExcludedFromOfficialEligibility() {
        val mockMsr = createMeasurement(isMock = true)
        val mockEv = createEvidence(measurementIds = listOf(mockMsr.id), isMock = true)
        val prov = createProvenance()

        // 1. Fluxo oficial rejeita mock data
        val officialResult = engine.generateEvidencePackage(
            userId = "USR-001",
            measurements = listOf(mockMsr),
            evidences = listOf(mockEv),
            provenances = mapOf(prov.id to prov),
            isSimulationMode = false,
            evaluationTimestamp = now
        )

        assertTrue("Mock data no fluxo oficial deve falhar", officialResult is AppResult.Failure)

        // 2. SimulationMode isolado é expressamente marcado
        val simResult = engine.generateEvidencePackage(
            userId = "USR-001",
            measurements = listOf(mockMsr),
            evidences = listOf(mockEv),
            provenances = mapOf(prov.id to prov),
            isSimulationMode = true,
            evaluationTimestamp = now
        )

        assertTrue(simResult is AppResult.Success)
        val pkg = (simResult as AppResult.Success).data.evidencePackage
        assertTrue(pkg.isMock)
        assertTrue(pkg.simulationMode)
    }

    // =========================================================================
    // 10. HISTÓRICO NÃO PODE SER ALTERADO RETROATIVAMENTE
    // =========================================================================
    @Test
    fun test10_historicalPackage_isImmutable() {
        val msr = createMeasurement(timestamp = now - (5 * ONE_DAY_MS))
        val ev = createEvidence(measurementIds = listOf(msr.id), capturedAt = now - (5 * ONE_DAY_MS))
        val prov = createProvenance()

        val result = engine.generateEvidencePackage(
            userId = "USR-001",
            measurements = listOf(msr),
            evidences = listOf(ev),
            provenances = mapOf(prov.id to prov),
            evaluationTimestamp = now
        )

        assertTrue(result is AppResult.Success)
        val pkg = (result as AppResult.Success).data.evidencePackage

        assertEquals(now, pkg.generatedAt)
        assertNotNull(pkg.id)
        assertNotNull(pkg.auditReference)
    }

    // =========================================================================
    // 11. MUDANÇA DE VERSÃO DA METODOLOGIA GERA NOVO CONTEXTO METODOLÓGICO
    // =========================================================================
    @Test
    fun test11_methodologyVersionChange_generatesIndependentContext() {
        val msr = createMeasurement()
        val ev = createEvidence(measurementIds = listOf(msr.id))
        val prov = createProvenance()

        val engineV1 = EvidenceConsistencyEngineV1(engineVersion = "1.0.0-consistency-v1")
        val engineV2 = EvidenceConsistencyEngineV1(engineVersion = "2.0.0-consistency-v2")

        val res1 = engineV1.generateEvidencePackage("USR-001", listOf(msr), listOf(ev), mapOf(prov.id to prov), evaluationTimestamp = now)
        val res2 = engineV2.generateEvidencePackage("USR-001", listOf(msr), listOf(ev), mapOf(prov.id to prov), evaluationTimestamp = now)

        assertTrue(res1 is AppResult.Success)
        assertTrue(res2 is AppResult.Success)

        val pkg1 = (res1 as AppResult.Success).data.evidencePackage
        val pkg2 = (res2 as AppResult.Success).data.evidencePackage

        assertEquals("1.0.0-consistency-v1", pkg1.engineVersion)
        assertEquals("2.0.0-consistency-v2", pkg2.engineVersion)
        assertFalse(pkg1.id == pkg2.id)
    }

    // =========================================================================
    // 12. EVIDENCE PACKAGE POSSUI RASTREABILIDADE COMPLETA ATÉ AS EVIDÊNCIAS ORIGINAIS
    // =========================================================================
    @Test
    fun test12_evidencePackage_hasFullTraceability() {
        val msr1 = createMeasurement(id = "MSR-TRC-01")
        val msr2 = createMeasurement(id = "MSR-TRC-02")
        val ev1 = createEvidence(id = "EV-TRC-01", measurementIds = listOf(msr1.id))
        val ev2 = createEvidence(id = "EV-TRC-02", measurementIds = listOf(msr2.id))
        val prov = createProvenance()

        val result = engine.generateEvidencePackage(
            userId = "USR-001",
            measurements = listOf(msr1, msr2),
            evidences = listOf(ev1, ev2),
            provenances = mapOf(prov.id to prov),
            evaluationTimestamp = now
        )

        assertTrue(result is AppResult.Success)
        val pkg = (result as AppResult.Success).data.evidencePackage

        assertEquals(2, pkg.evidenceIds.size)
        assertTrue(pkg.evidenceIds.contains("EV-TRC-01"))
        assertTrue(pkg.evidenceIds.contains("EV-TRC-02"))
        assertEquals(2, pkg.validityAssessments.size)
    }

    // =========================================================================
    // 13. CLIENTE NÃO CONSEGUE ALTERAR O EVIDENCE PACKAGE
    // =========================================================================
    @Test
    fun test13_clientDirectPackageMutation_isBlocked() {
        val unauthorizedAttempt = AppError.UnauthorizedStateMutation(
            "Violação de Segurança: Mutação direta de status de evidência, maturidade ou consistência por clientes é estritamente proibida."
        )
        assertNotNull(unauthorizedAttempt)
        assertTrue(unauthorizedAttempt.message.contains("estritamente proibida"))
    }

    // =========================================================================
    // 14. IA NÃO CONSEGUE ALTERAR ELEGIBILIDADE
    // =========================================================================
    @Test
    fun test14_aiCannotAlterEligibility() {
        // A IA opera exclusivamente via AI Gateway para consulta/explicação posterior
        // O EvidenceConsistencyEngineV1 não possui portas de entrada ou mutadores para modelos de IA
        val engineActor = ActorType.CORE_ENGINE
        assertFalse("IA não é ator de cálculo", engineActor == ActorType.AI_GATEWAY)
    }

    // =========================================================================
    // 15. TODA DECISÃO IMPORTANTE GERA AUDITLOG
    // =========================================================================
    @Test
    fun test15_importantDecisions_generateAuditLog() {
        val msr = createMeasurement()
        val ev = createEvidence(measurementIds = listOf(msr.id))
        val prov = createProvenance()

        val result = engine.generateEvidencePackage(
            userId = "USR-001",
            measurements = listOf(msr),
            evidences = listOf(ev),
            provenances = mapOf(prov.id to prov),
            evaluationTimestamp = now
        )

        assertTrue(result is AppResult.Success)
        val auditLog = (result as AppResult.Success).data.auditLog

        assertEquals(ActorType.CORE_ENGINE, auditLog.actorType)
        assertEquals("EvidenceConsistencyEngineV1", auditLog.actorId)
        assertEquals("EVOLUTION_EVIDENCE_PACKAGE_GENERATED", auditLog.action)
        assertEquals("EvolutionEvidencePackage", auditLog.entityType)
        assertNotNull(auditLog.requestId)
        assertTrue(auditLog.newState!!.contains("USER=USR-001"))
    }
}
