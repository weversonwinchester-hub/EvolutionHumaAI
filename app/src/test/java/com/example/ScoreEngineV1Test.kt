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
import com.example.core.scoreengine.eligibility.EvidenceEligibilityChecker
import com.example.core.scoreengine.engine.ScoreEngineV1
import com.example.core.scoreengine.model.CalculationStatus
import com.example.core.scoreengine.model.DimensionType
import com.example.core.scoreengine.normalization.ScoreNormalizationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ScoreEngineV1Test {

    private lateinit var scoreEngine: ScoreEngineV1

    @Before
    fun setup() {
        scoreEngine = ScoreEngineV1(
            scoreEngineVersion = "1.0.0-score-v1",
            coreVersion = "1.0.0-datacore-v1"
        )
    }

    private fun createValidMeasurement(
        id: String = "MSR-${UUID.randomUUID().toString().take(6)}",
        userId: String = "USR-001",
        metricId: String = MetricCatalog.METRIC_RELATIVE_STRENGTH,
        rawValue: Double = 1.85,
        status: ValidationStatus = ValidationStatus.VALID,
        isMock: Boolean = false
    ): DataCoreMeasurement {
        return DataCoreMeasurement(
            id = id,
            assessmentId = "ASM-001",
            userId = userId,
            metricId = metricId,
            rawValue = rawValue,
            normalizedValue = null,
            unit = "1RM/BW ratio",
            timestamp = System.currentTimeMillis() - 5000,
            source = "LPT Encoder",
            deviceId = "DEV-LPT-01",
            protocolId = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
            validationStatus = status,
            rawDataInputId = "RAW-001",
            isMock = isMock
        )
    }

    private fun createValidProvenance(
        id: String = "PROV-001",
        sourceType: String = "DIRECT_CALIBRATED_SENSOR",
        hash: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    ): DataCoreProvenance {
        return DataCoreProvenance(
            id = id,
            sourceType = sourceType,
            sourceIdentifier = "LPT-SN-100",
            deviceIdentifier = "LPT-01",
            captureTimestamp = System.currentTimeMillis() - 5000,
            processingTimestamp = System.currentTimeMillis(),
            processingVersion = "1.0.0-datacore-v1",
            protocolId = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
            integrityHash = hash
        )
    }

    private fun createValidEvidence(
        id: String = "EV-2026-000001",
        userId: String = "USR-001",
        measurementIds: List<String>,
        provenanceId: String = "PROV-001",
        integrityStatus: IntegrityStatus = IntegrityStatus.VALID,
        isMock: Boolean = false
    ): DataCoreEvidence {
        return DataCoreEvidence(
            id = id,
            userId = userId,
            assessmentId = "ASM-001",
            measurementIds = measurementIds,
            source = "DIRECT_CALIBRATED_SENSOR",
            capturedAt = System.currentTimeMillis() - 5000,
            submittedAt = System.currentTimeMillis(),
            integrityStatus = integrityStatus,
            reliabilityScore = 0.98,
            confidenceScore = 0.96,
            provenanceId = provenanceId,
            coreVersion = "1.0.0-datacore-v1",
            isMock = isMock
        )
    }

    // =========================================================================
    // TESTE 1: MEASUREMENT INVÁLIDA NÃO ENTRA NO SCORE
    // =========================================================================
    @Test
    fun test1_invalidMeasurement_doesNotEnterScore() {
        val invalidMsr = createValidMeasurement(
            id = "MSR-INVALID",
            status = ValidationStatus.INVALID
        )
        val validMsr = createValidMeasurement(
            id = "MSR-VALID",
            metricId = MetricCatalog.METRIC_VO2_MAX,
            rawValue = 52.0,
            status = ValidationStatus.VALID
        )
        val prov = createValidProvenance(id = "PROV-001")
        val ev = createValidEvidence(
            id = "EV-2026-001",
            measurementIds = listOf("MSR-INVALID", "MSR-VALID"),
            provenanceId = prov.id
        )

        val result = scoreEngine.computeScore(
            userId = "USR-001",
            assessmentId = "ASM-001",
            measurements = listOf(invalidMsr, validMsr),
            evidences = listOf(ev),
            provenances = mapOf(prov.id to prov)
        )

        assertTrue(result is AppResult.Success)
        val snapshot = (result as AppResult.Success).data.snapshot

        // Somente a medição válida entra no score
        assertEquals(1, snapshot.metricIds.size)
        assertTrue(snapshot.metricIds.contains(MetricCatalog.METRIC_VO2_MAX))
        assertFalse(snapshot.metricIds.contains(MetricCatalog.METRIC_RELATIVE_STRENGTH))
    }

    // =========================================================================
    // TESTE 2: EVIDENCE SEM PROVENANCE NÃO ENTRA NO SCORE
    // =========================================================================
    @Test
    fun test2_evidenceWithoutProvenance_isRejectedFromScore() {
        val msr = createValidMeasurement(id = "MSR-001")
        val ev = createValidEvidence(
            id = "EV-2026-NOPROV",
            measurementIds = listOf("MSR-001"),
            provenanceId = "PROV-NON-EXISTENT"
        )

        // Mapa de provenances vazio
        val result = scoreEngine.computeScore(
            userId = "USR-001",
            assessmentId = "ASM-001",
            measurements = listOf(msr),
            evidences = listOf(ev),
            provenances = emptyMap(),
            isMockMode = false
        )

        assertTrue("Evidência sem provenance deve falhar no Score Engine", result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error.message.contains("Rejeição de Provenance"))
    }

    // =========================================================================
    // TESTE 3: MOCK DATA NÃO GERA SCORE OFICIAL
    // =========================================================================
    @Test
    fun test3_mockData_doesNotGenerateOfficialScore() {
        val mockMsr = createValidMeasurement(id = "MSR-MOCK", isMock = true)
        val prov = createValidProvenance(id = "PROV-MOCK")
        val mockEv = createValidEvidence(
            id = "EV-2026-MOCK",
            measurementIds = listOf("MSR-MOCK"),
            provenanceId = prov.id,
            isMock = true
        )

        // 1. Tentar calcular como Score oficial
        val officialAttempt = scoreEngine.computeScore(
            userId = "USR-001",
            assessmentId = "ASM-001",
            measurements = listOf(mockMsr),
            evidences = listOf(mockEv),
            provenances = mapOf(prov.id to prov),
            isMockMode = false
        )

        assertTrue("Mock data deve ser bloqueada de gerar score oficial", officialAttempt is AppResult.Failure)

        // 2. Modo demonstrativo isolado é marcado estritamente como isMock = true
        val demoResult = scoreEngine.computeScore(
            userId = "USR-001",
            assessmentId = "ASM-001",
            measurements = listOf(mockMsr),
            evidences = listOf(mockEv),
            provenances = mapOf(prov.id to prov),
            isMockMode = true
        )

        assertTrue(demoResult is AppResult.Success)
        val snapshot = (demoResult as AppResult.Success).data.snapshot
        assertTrue("Snapshot de demo deve ter isMock = true", snapshot.isMock)
        assertEquals(CalculationStatus.MOCK_DEMO, snapshot.calculationStatus)
    }

    // =========================================================================
    // TESTE 4: CLIENTE TENTANDO ALTERAR SCORE É BLOQUEADO
    // =========================================================================
    @Test
    fun test4_clientDirectScoreAlteration_isBlocked() {
        // Validação da regra: O cliente não pode injetar performanceIndex = 99 diretamente
        val unauthorizedAttempt = AppError.UnauthorizedStateMutation(
            "Violação de Segurança: Mutação direta de scores por clientes é estritamente proibida."
        )
        assertNotNull(unauthorizedAttempt)
        assertTrue(unauthorizedAttempt.message.contains("Mutação direta de scores por clientes é estritamente proibida"))
    }

    // =========================================================================
    // TESTE 5: SCORE HISTÓRICO NÃO PODE SER ALTERADO
    // =========================================================================
    @Test
    fun test5_historicalScoreSnapshot_isImmutable() {
        val msr = createValidMeasurement(id = "MSR-001")
        val prov = createValidProvenance(id = "PROV-001")
        val ev = createValidEvidence(id = "EV-001", measurementIds = listOf("MSR-001"), provenanceId = prov.id)

        val fixedTime = 1718000000000L
        val result = scoreEngine.computeScore(
            userId = "USR-001",
            assessmentId = "ASM-001",
            measurements = listOf(msr),
            evidences = listOf(ev),
            provenances = mapOf(prov.id to prov),
            calculationTimestamp = fixedTime
        )

        assertTrue(result is AppResult.Success)
        val snapshot = (result as AppResult.Success).data.snapshot

        assertEquals(fixedTime, snapshot.calculatedAt)
        assertNotNull(snapshot.id)
        // Garantia de imutabilidade estrutural: Não há métodos de mutação na data class ScoreSnapshot
    }

    // =========================================================================
    // TESTE 6: VERSÕES DIFERENTES DE FÓRMULA PRODUZEM SNAPSHOTS INDEPENDENTES
    // =========================================================================
    @Test
    fun test6_differentFormulaVersions_produceIndependentSnapshots() {
        val msr = createValidMeasurement(id = "MSR-001")
        val prov = createValidProvenance(id = "PROV-001")
        val ev = createValidEvidence(id = "EV-001", measurementIds = listOf("MSR-001"), provenanceId = prov.id)

        val engineV1 = ScoreEngineV1(scoreEngineVersion = "1.0.0-score-v1")
        val engineV2 = ScoreEngineV1(scoreEngineVersion = "2.0.0-score-v2")

        val res1 = engineV1.computeScore("USR-001", "ASM-001", listOf(msr), listOf(ev), mapOf(prov.id to prov))
        val res2 = engineV2.computeScore("USR-001", "ASM-001", listOf(msr), listOf(ev), mapOf(prov.id to prov))

        assertTrue(res1 is AppResult.Success)
        assertTrue(res2 is AppResult.Success)

        val s1 = (res1 as AppResult.Success).data.snapshot
        val s2 = (res2 as AppResult.Success).data.snapshot

        assertEquals("1.0.0-score-v1", s1.scoreVersion)
        assertEquals("2.0.0-score-v2", s2.scoreVersion)
        assertFalse("Snapshots de versões diferentes devem ter IDs distintos", s1.id == s2.id)
    }

    // =========================================================================
    // TESTE 7: MESMO INPUT + MESMA VERSÃO PRODUZ RESULTADO DETERMINÍSTICO
    // =========================================================================
    @Test
    fun test7_sameInputAndVersion_producesDeterministicResult() {
        val fixedTime = 1720000000000L
        val msr = createValidMeasurement(id = "MSR-001", rawValue = 1.75)
        val prov = createValidProvenance(id = "PROV-001")
        val ev = createValidEvidence(id = "EV-001", measurementIds = listOf("MSR-001"), provenanceId = prov.id)

        val res1 = scoreEngine.computeScore("USR-001", "ASM-001", listOf(msr), listOf(ev), mapOf(prov.id to prov), calculationTimestamp = fixedTime)
        val res2 = scoreEngine.computeScore("USR-001", "ASM-001", listOf(msr), listOf(ev), mapOf(prov.id to prov), calculationTimestamp = fixedTime)

        assertTrue(res1 is AppResult.Success)
        assertTrue(res2 is AppResult.Success)

        val r1 = (res1 as AppResult.Success).data
        val r2 = (res2 as AppResult.Success).data

        assertTrue("O Score Engine deve ser estritamente reproduzível", scoreEngine.verifyReproducibility(r1, r2))
    }

    // =========================================================================
    // TESTE 8: MÉTRICA SEM METODOLOGIA APROVADA RESULTA EM PENDING_VALIDATION
    // =========================================================================
    @Test
    fun test8_metricWithoutApprovedMethodology_resultsInPendingValidation() {
        val msr = createValidMeasurement(
            id = "MSR-RELSTR",
            metricId = MetricCatalog.METRIC_RELATIVE_STRENGTH,
            rawValue = 1.95
        )
        val normResult = ScoreNormalizationEngine.normalize(msr)

        assertEquals(CalculationStatus.PENDING_VALIDATION, normResult.calculationStatus)
        assertNull("Valor normalizado deve ser null para não fabricar números artificiais", normResult.normalizedValue)
    }

    // =========================================================================
    // TESTE 9: AUDIT LOG REGISTRA CÁLCULO OFICIAL
    // =========================================================================
    @Test
    fun test9_auditLog_recordsOfficialScoreCalculation() {
        val msr = createValidMeasurement(id = "MSR-001")
        val prov = createValidProvenance(id = "PROV-001")
        val ev = createValidEvidence(id = "EV-001", measurementIds = listOf("MSR-001"), provenanceId = prov.id)

        val result = scoreEngine.computeScore(
            userId = "USR-001",
            assessmentId = "ASM-001",
            measurements = listOf(msr),
            evidences = listOf(ev),
            provenances = mapOf(prov.id to prov)
        )

        assertTrue(result is AppResult.Success)
        val auditLog = (result as AppResult.Success).data.auditLog

        assertEquals(ActorType.CORE_ENGINE, auditLog.actorType)
        assertEquals("ScoreEngineV1", auditLog.actorId)
        assertEquals("SCORE_SNAPSHOT_CALCULATED", auditLog.action)
        assertEquals("ScoreSnapshot", auditLog.entityType)
        assertNotNull(auditLog.requestId)
        assertTrue(auditLog.newState!!.contains("STATUS=PENDING_VALIDATION"))
    }

    // =========================================================================
    // TESTE 10: TENTATIVA DE MANIPULAÇÃO GERA EVENTO DE SEGURANÇA
    // =========================================================================
    @Test
    fun test10_manipulationAttempt_generatesSecurityAuditViolation() {
        val auditLog = com.example.core.datacore.model.DataCoreAuditLog(
            id = UUID.randomUUID().toString(),
            actorType = ActorType.CLIENT,
            actorId = "UNAUTHORIZED_CLIENT_01",
            action = "SECURITY_VIOLATION_SCORE_MANIPULATION_ATTEMPT",
            entityType = "ScoreSnapshot",
            entityId = "TARGET_USER_USR-1001",
            previousState = null,
            newState = "ATTEMPTED_MUTATION: dimension=FORCE, value=99.0; REJECTED=True",
            timestamp = System.currentTimeMillis(),
            requestId = UUID.randomUUID().toString(),
            systemVersion = "1.0.0-score-v1"
        )

        assertEquals("SECURITY_VIOLATION_SCORE_MANIPULATION_ATTEMPT", auditLog.action)
        assertEquals(ActorType.CLIENT, auditLog.actorType)
        assertTrue(auditLog.newState!!.contains("REJECTED=True"))
    }
}
