package com.example

import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.DataCoreAuditLog
import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.SourceTier
import com.example.core.datacore.model.ValidationStatus
import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.trialengine.engine.TrialEngineV1
import com.example.core.trialengine.evaluator.TrialResultEvaluator
import com.example.core.trialengine.model.TrialAbortReason
import com.example.core.trialengine.model.TrialAttemptValidationStatus
import com.example.core.trialengine.model.TrialCategory
import com.example.core.trialengine.model.TrialPolicy
import com.example.core.trialengine.model.TrialResultStatus
import com.example.core.trialengine.model.TrialScoringMethod
import com.example.core.trialengine.model.TrialSession
import com.example.core.trialengine.model.TrialSessionStatus
import com.example.core.trialengine.policy.TrialPolicyRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * PERFORMAI TRIAL ENGINE V1 - TESTES AUTOMATIZADOS OBRIGATÓRIOS (20 CRITÉRIOS)
 */
class TrialEngineV1Test {

    private lateinit var auditLogs: MutableList<DataCoreAuditLog>
    private lateinit var evaluator: TrialResultEvaluator
    private lateinit var engine: TrialEngineV1

    @Before
    fun setUp() {
        auditLogs = mutableListOf()
        evaluator = TrialResultEvaluator()
        engine = TrialEngineV1(
            policyRegistry = TrialPolicyRegistry,
            resultEvaluator = evaluator,
            auditLogger = { auditLogs.add(it) }
        )
    }

    // =========================================================================
    // TESTE 1: Trial Policy inexistente bloqueia execução
    // =========================================================================
    @Test
    fun testNonExistentPolicyBlocksExecution() {
        try {
            engine.createSession(
                userId = "USR-01",
                classId = ClassCatalog.CLASS_08,
                trialPolicyId = "POLICY-INEXISTENTE-999",
                deviceId = "LAB_FORCE_PLATE_V1",
                protocolId = "PROT-TRIAL-LOAD-01"
            )
            fail("Deveria lançar exceção para política inexistente")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Trial Policy inexistente"))
        }
    }

    // =========================================================================
    // TESTE 2: Trial Policy inativa bloqueia execução
    // =========================================================================
    @Test
    fun testInactivePolicyBlocksExecution() {
        val inactivePolicy = TrialPolicy(
            trialPolicyId = "TRIAL-POL-SUSPENDED",
            classId = ClassCatalog.CLASS_08,
            version = "1.0.0",
            name = "Suspended Trial",
            description = "Desc",
            category = TrialCategory.NEUROMUSCULAR,
            requiredEvidenceTypes = listOf("DIRECT_SENSOR"),
            protocolId = "PROT-TEST",
            allowedDevices = listOf("LAB_FORCE_PLATE_V1"),
            status = "INACTIVE"
        )

        val precondition = com.example.core.trialengine.validator.TrialStateValidator.validatePreconditions(
            userId = "USR-02",
            policy = inactivePolicy,
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TEST",
            hasActiveSession = false
        )

        assertTrue(precondition is com.example.core.trialengine.validator.TrialStateValidator.PreconditionResult.Failure)
        assertTrue((precondition as com.example.core.trialengine.validator.TrialStateValidator.PreconditionResult.Failure).reason.contains("inativa ou suspensa"))
    }

    // =========================================================================
    // TESTE 3: Usuário sem autenticação/vazio não pode iniciar Trial oficial
    // =========================================================================
    @Test
    fun testUnauthenticatedUserCannotStartTrial() {
        try {
            engine.createSession(
                userId = "",
                classId = ClassCatalog.CLASS_08,
                trialPolicyId = "TRIAL-POL-CLASS-08-V1",
                deviceId = "LAB_FORCE_PLATE_V1",
                protocolId = "PROT-TRIAL-LOAD-01"
            )
            fail("Deveria bloquear criação de sessão sem userId")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("não autenticado"))
        }
    }

    // =========================================================================
    // TESTE 4: Mock Trial não gera resultado oficial
    // =========================================================================
    @Test
    fun testMockTrialDoesNotGenerateOfficialResult() {
        val session = engine.createSession(
            userId = "USR-MOCK-04",
            classId = ClassCatalog.CLASS_08,
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            isMock = true,
            simulationMode = true
        )

        assertTrue(session.isMock)
        assertTrue(session.simulationMode)
        assertEquals(TrialSessionStatus.SIMULATION, session.status)
    }

    // =========================================================================
    // TESTE 5: Estado inválido de sessão é rejeitado
    // =========================================================================
    @Test
    fun testInvalidSessionStateTransitionRejected() {
        val session = engine.createSession(
            userId = "USR-05",
            classId = ClassCatalog.CLASS_08,
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01"
        )

        assertEquals(TrialSessionStatus.CREATED, session.status)

        try {
            // Transição ilegal: CREATED direto para COMPLETED
            engine.transitionSession(session, TrialSessionStatus.COMPLETED)
            fail("Deveria rejeitar transição ilegal de CREATED para COMPLETED")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Transição inválida"))
        }
    }

    // =========================================================================
    // TESTE 6: Tentativa duplicada é detectada
    // =========================================================================
    @Test
    fun testDuplicateAttemptIsDetectedAndRejected() {
        var session = engine.createSession(
            userId = "USR-06",
            classId = ClassCatalog.CLASS_08,
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01"
        )
        session = engine.transitionSession(session, TrialSessionStatus.READY)
        session = engine.transitionSession(session, TrialSessionStatus.RUNNING)

        val (updatedSession, attempt1) = engine.recordAttempt(
            session = session,
            attemptNumber = 1,
            rawEvidenceIds = listOf("EV-01"),
            measurementIds = listOf("M-01"),
            resultValue = 25.0,
            unit = "N/kg",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            startedAt = 1000L,
            completedAt = 2000L,
            existingAttempts = emptyList()
        )

        try {
            // Tenta gravar novamente attemptNumber = 1
            engine.recordAttempt(
                session = updatedSession,
                attemptNumber = 1,
                rawEvidenceIds = listOf("EV-02"),
                measurementIds = listOf("M-02"),
                resultValue = 26.0,
                unit = "N/kg",
                deviceId = "LAB_FORCE_PLATE_V1",
                protocolId = "PROT-TRIAL-LOAD-01",
                startedAt = 3000L,
                completedAt = 4000L,
                existingAttempts = listOf(attempt1)
            )
            fail("Deveria rejeitar tentativa com número duplicado")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("já foi registrada"))
        }
    }

    // =========================================================================
    // TESTE 7: Mudança de dispositivo é registrada
    // =========================================================================
    @Test
    fun testDeviceChangeIsRecorded() {
        var session = engine.createSession(
            userId = "USR-07",
            classId = ClassCatalog.CLASS_08,
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01"
        )
        session = engine.transitionSession(session, TrialSessionStatus.READY)
        session = engine.transitionSession(session, TrialSessionStatus.RUNNING)

        val (_, attempt) = engine.recordAttempt(
            session = session,
            attemptNumber = 1,
            rawEvidenceIds = listOf("EV-07"),
            measurementIds = listOf("M-07"),
            resultValue = 30.0,
            unit = "N/kg",
            deviceId = "CALIBRATED_LINEAR_TRANSDUCER_V1", // Dispositivo diferente do da sessão
            protocolId = "PROT-TRIAL-LOAD-01",
            startedAt = 1000L,
            completedAt = 2000L
        )

        assertEquals(TrialAttemptValidationStatus.DEVICE_VIOLATION, attempt.validationStatus)
        assertTrue(attempt.invalidationReason!!.contains("Dispositivo alterado"))
        assertTrue(auditLogs.any { it.action == "DEVICE_CHANGED_DURING_SESSION" })
    }

    // =========================================================================
    // TESTE 8: Violação de protocolo invalida tentativa quando aplicável
    // =========================================================================
    @Test
    fun testProtocolViolationInvalidatesAttempt() {
        var session = engine.createSession(
            userId = "USR-08",
            classId = ClassCatalog.CLASS_08,
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01"
        )
        session = engine.transitionSession(session, TrialSessionStatus.READY)
        session = engine.transitionSession(session, TrialSessionStatus.RUNNING)

        val (_, attempt) = engine.recordAttempt(
            session = session,
            attemptNumber = 1,
            rawEvidenceIds = listOf("EV-08"),
            measurementIds = listOf("M-08"),
            resultValue = 35.0,
            unit = "N/kg",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-WRONG-PROTOCOL-99", // Protocolo divergente
            startedAt = 1000L,
            completedAt = 2000L
        )

        assertEquals(TrialAttemptValidationStatus.PROTOCOL_VIOLATION, attempt.validationStatus)
        assertTrue(attempt.invalidationReason!!.contains("Violação de protocolo"))
    }

    // =========================================================================
    // TESTE 9: Evidência sem provenance é rejeitada
    // =========================================================================
    @Test
    fun testEvidenceWithoutProvenanceIsRejected() {
        val session = createTestSession("USR-09", "TRIAL-POL-CLASS-08-V1")
        val policy = TrialPolicyRegistry.getPolicyById("TRIAL-POL-CLASS-08-V1")!!

        val attempt = createTestAttempt(session.id, 1, listOf("EV-NO-PROV"), listOf("M-09"), 40.0)
        val evidence = createTestEvidence("EV-NO-PROV", "PROV-MISSING")

        val result = evaluator.evaluateSessionResult(
            session = session,
            policy = policy,
            attempts = listOf(attempt),
            evidences = listOf(evidence),
            measurements = listOf(createTestMeasurement("M-09")),
            provenances = emptyMap() // Sem mapa de proveniência
        )

        assertTrue(result.limitations.any { it.contains("MISSING_PROVENANCE") })
        assertEquals(TrialResultStatus.INSUFFICIENT_EVIDENCE, result.resultStatus)
    }

    // =========================================================================
    // TESTE 10: Integridade inválida (TAMPERED) impede qualificação
    // =========================================================================
    @Test
    fun testTamperedIntegrityPreventsQualification() {
        val session = createTestSession("USR-10", "TRIAL-POL-CLASS-08-V1").copy(
            sessionIntegrity = IntegrityStatus.TAMPERED
        )
        val policy = TrialPolicyRegistry.getPolicyById("TRIAL-POL-CLASS-08-V1")!!

        val attempt = createTestAttempt(session.id, 1, listOf("EV-10"), listOf("M-10"), 50.0)
        val evidence = createTestEvidence("EV-10", "PROV-10")
        val prov = createTestProvenance("PROV-10")

        val result = evaluator.evaluateSessionResult(
            session = session,
            policy = policy,
            attempts = listOf(attempt),
            evidences = listOf(evidence),
            measurements = listOf(createTestMeasurement("M-10")),
            provenances = mapOf("PROV-10" to prov)
        )

        assertEquals(TrialResultStatus.INVALID, result.resultStatus)
        assertTrue(result.limitations.contains("SESSION_INTEGRITY_TAMPERED"))
    }

    // =========================================================================
    // TESTE 11: Resultado não pode ser alterado pelo cliente
    // =========================================================================
    @Test
    fun testClientCannotModifyTrialResult() {
        try {
            engine.attemptDirectResultModification(
                sessionId = "TS-11",
                attemptedStatus = TrialResultStatus.QUALIFIED,
                actor = ActorType.CLIENT
            )
            fail("Deveria lançar SecurityException para tentativa de alteração pelo CLIENT")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("Segurança PERFORMAI"))
        }

        assertTrue(auditLogs.any {
            it.action == "UNAUTHORIZED_TRIAL_RESULT_MUTATION_BLOCKED" &&
                    it.actorType == ActorType.CLIENT
        })
    }

    // =========================================================================
    // TESTE 12: Resultado não pode ser alterado pela IA
    // =========================================================================
    @Test
    fun testAICannotModifyTrialResult() {
        try {
            engine.attemptDirectResultModification(
                sessionId = "TS-12",
                attemptedStatus = TrialResultStatus.QUALIFIED,
                actor = ActorType.AI_GATEWAY
            )
            fail("Deveria lançar SecurityException para tentativa de alteração por AI_GATEWAY")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("Segurança PERFORMAI"))
        }

        assertTrue(auditLogs.any {
            it.action == "UNAUTHORIZED_TRIAL_RESULT_MUTATION_BLOCKED" &&
                    it.actorType == ActorType.AI_GATEWAY
        })
    }

    // =========================================================================
    // TESTE 13: Histórico de tentativa é imutável
    // =========================================================================
    @Test
    fun testTrialAttemptHistoryIsImmutable() {
        var session = engine.createSession(
            userId = "USR-13",
            classId = ClassCatalog.CLASS_08,
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01"
        )
        session = engine.transitionSession(session, TrialSessionStatus.READY)
        session = engine.transitionSession(session, TrialSessionStatus.RUNNING)

        val (_, attempt) = engine.recordAttempt(
            session = session,
            attemptNumber = 1,
            rawEvidenceIds = listOf("EV-13"),
            measurementIds = listOf("M-13"),
            resultValue = 42.0,
            unit = "N/kg",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            startedAt = 1000L,
            completedAt = 2000L
        )

        assertNotNull(attempt.integrityHash)
        val copy = attempt.copy()
        assertEquals(attempt.integrityHash, copy.integrityHash)
    }

    // =========================================================================
    // TESTE 14: Mesma evidência + mesma política produz resultado determinístico
    // =========================================================================
    @Test
    fun testDeterministicTrialOutcome() {
        val session = createTestSession("USR-14", "TRIAL-POL-CLASS-08-V1")
        val policy = TrialPolicyRegistry.getPolicyById("TRIAL-POL-CLASS-08-V1")!!

        val attempt1 = createTestAttempt(session.id, 1, listOf("EV-14-1"), listOf("M-14-1"), 30.0)
        val attempt2 = createTestAttempt(session.id, 2, listOf("EV-14-2"), listOf("M-14-2"), 35.0)

        val ev1 = createTestEvidence("EV-14-1", "PROV-14")
        val ev2 = createTestEvidence("EV-14-2", "PROV-14")
        val m1 = createTestMeasurement("M-14-1")
        val m2 = createTestMeasurement("M-14-2")
        val prov = createTestProvenance("PROV-14")

        val fixedTime = 1720000000000L

        val res1 = evaluator.evaluateSessionResult(
            session = session,
            policy = policy,
            attempts = listOf(attempt1, attempt2),
            evidences = listOf(ev1, ev2),
            measurements = listOf(m1, m2),
            provenances = mapOf("PROV-14" to prov),
            evaluationTimestamp = fixedTime
        )

        val res2 = evaluator.evaluateSessionResult(
            session = session,
            policy = policy,
            attempts = listOf(attempt1, attempt2),
            evidences = listOf(ev1, ev2),
            measurements = listOf(m1, m2),
            provenances = mapOf("PROV-14" to prov),
            evaluationTimestamp = fixedTime
        )

        assertEquals(res1.resultStatus, res2.resultStatus)
        assertEquals(res1.bestAttemptId, res2.bestAttemptId)
        assertEquals(res1.limitations, res2.limitations)
    }

    // =========================================================================
    // TESTE 15: Toda conclusão gera AuditLog
    // =========================================================================
    @Test
    fun testTrialCompletionGeneratesAuditLog() {
        auditLogs.clear()

        var session = engine.createSession(
            userId = "USR-15",
            classId = ClassCatalog.CLASS_08,
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01"
        )
        session = engine.transitionSession(session, TrialSessionStatus.READY)
        session = engine.transitionSession(session, TrialSessionStatus.RUNNING)

        val (s2, att1) = engine.recordAttempt(
            session = session,
            attemptNumber = 1,
            rawEvidenceIds = listOf("EV-15-1"),
            measurementIds = listOf("M-15-1"),
            resultValue = 28.0,
            unit = "N/kg",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            startedAt = 1000L,
            completedAt = 2000L
        )

        val (s3, att2) = engine.recordAttempt(
            session = s2,
            attemptNumber = 2,
            rawEvidenceIds = listOf("EV-15-2"),
            measurementIds = listOf("M-15-2"),
            resultValue = 31.0,
            unit = "N/kg",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            startedAt = 200000L,
            completedAt = 201000L,
            restSecondsBeforeAttempt = 190L, // > 180s exigidos
            existingAttempts = listOf(att1)
        )

        val ev1 = createTestEvidence("EV-15-1", "PROV-15")
        val ev2 = createTestEvidence("EV-15-2", "PROV-15")
        val m1 = createTestMeasurement("M-15-1")
        val m2 = createTestMeasurement("M-15-2")
        val prov = createTestProvenance("PROV-15")

        val snapshot = engine.completeSession(
            session = s3,
            attempts = listOf(att1, att2),
            evidences = listOf(ev1, ev2),
            measurements = listOf(m1, m2),
            provenances = mapOf("PROV-15" to prov)
        )

        assertNotNull(snapshot)
        assertTrue(auditLogs.any { it.action == "TRIAL_COMPLETED_OFFICIAL" })
    }

    // =========================================================================
    // TESTE 16: Sessão expirada não pode continuar
    // =========================================================================
    @Test
    fun testExpiredSessionCannotContinue() {
        val policy = TrialPolicyRegistry.getPolicyById("TRIAL-POL-CLASS-08-V1")!!
        val expiredSession = createTestSession("USR-16", "TRIAL-POL-CLASS-08-V1").copy(
            startedAt = System.currentTimeMillis() - ((policy.executionWindowSeconds + 600) * 1000L) // Expirada há 10 min
        )

        val snapshot = engine.completeSession(
            session = expiredSession,
            attempts = emptyList(),
            evidences = emptyList(),
            measurements = emptyList(),
            provenances = emptyMap()
        )

        assertEquals(TrialResultStatus.INVALID, snapshot.result.resultStatus)
        assertTrue(snapshot.result.limitations.contains("SESSION_EXPIRED"))
    }

    // =========================================================================
    // TESTE 17: Tentativa antes do descanso mínimo é bloqueada
    // =========================================================================
    @Test
    fun testAttemptBeforeMinimumRestPeriodIsBlocked() {
        var session = engine.createSession(
            userId = "USR-17",
            classId = ClassCatalog.CLASS_08,
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01"
        )
        session = engine.transitionSession(session, TrialSessionStatus.READY)
        session = engine.transitionSession(session, TrialSessionStatus.RUNNING)

        val (s2, att1) = engine.recordAttempt(
            session = session,
            attemptNumber = 1,
            rawEvidenceIds = listOf("EV-17-1"),
            measurementIds = listOf("M-17-1"),
            resultValue = 30.0,
            unit = "N/kg",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            startedAt = 1000L,
            completedAt = 2000L
        )

        try {
            // Política exige 180s de descanso; tentamos com apenas 30s
            engine.recordAttempt(
                session = s2,
                attemptNumber = 2,
                rawEvidenceIds = listOf("EV-17-2"),
                measurementIds = listOf("M-17-2"),
                resultValue = 32.0,
                unit = "N/kg",
                deviceId = "LAB_FORCE_PLATE_V1",
                protocolId = "PROT-TRIAL-LOAD-01",
                startedAt = 32000L,
                completedAt = 34000L,
                restSecondsBeforeAttempt = 30L, // 30s < 180s
                existingAttempts = listOf(att1)
            )
            fail("Deveria bloquear tentativa antes do descanso mínimo")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Período mínimo de descanso não atingido"))
        }
    }

    // =========================================================================
    // TESTE 18: SimulationMode não contamina estado oficial
    // =========================================================================
    @Test
    fun testSimulationModeDoesNotContaminateOfficialState() {
        val simSession = engine.createSession(
            userId = "USR-18",
            classId = ClassCatalog.CLASS_10,
            trialPolicyId = "TRIAL-POL-CLASS-10-V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-POWER-01",
            isMock = true,
            simulationMode = true
        )

        assertTrue(simSession.simulationMode)
        assertTrue(simSession.isMock)
        assertEquals(TrialSessionStatus.SIMULATION, simSession.status)

        val (_, att) = engine.recordAttempt(
            session = simSession,
            attemptNumber = 1,
            rawEvidenceIds = listOf("EV-SIM-1"),
            measurementIds = listOf("M-SIM-1"),
            resultValue = 55.0,
            unit = "W/kg",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-POWER-01",
            startedAt = 1000L,
            completedAt = 2000L
        )

        val ev = createTestEvidence("EV-SIM-1", "PROV-SIM", isMock = true)
        val m = createTestMeasurement("M-SIM-1", isMock = true)
        val prov = createTestProvenance("PROV-SIM")

        val snapshot = engine.completeSession(
            session = simSession,
            attempts = listOf(att),
            evidences = listOf(ev),
            measurements = listOf(m),
            provenances = mapOf("PROV-SIM" to prov)
        )

        assertTrue(snapshot.isMock)
        assertTrue(snapshot.simulationMode)
        assertTrue(snapshot.result.simulationMode)
        assertFalse(snapshot.result.resultStatus == TrialResultStatus.QUALIFIED)
    }

    // =========================================================================
    // TESTE 19: Mudança de versão da Trial Policy cria novo contexto
    // =========================================================================
    @Test
    fun testPolicyVersionChangeCreatesNewContext() {
        val policyV1 = TrialPolicy(
            trialPolicyId = "TRIAL-POL-CUSTOM",
            classId = ClassCatalog.CLASS_12,
            version = "1.0.0",
            name = "Prova V1",
            description = "Desc V1",
            category = TrialCategory.NEUROMUSCULAR,
            requiredEvidenceTypes = listOf("HIGH_FREQUENCY_FORCE_TIME"),
            protocolId = "PROT-RFD-01",
            allowedDevices = listOf("LAB_FORCE_PLATE_V1"),
            minimumAttempts = 1,
            scoringMethod = TrialScoringMethod.BEST_ATTEMPT,
            thresholdValue = 50.0,
            methodologyStatus = "ACTIVE"
        )

        val policyV2 = policyV1.copy(
            version = "2.0.0-CONSORCIO-ATUALIZADO",
            thresholdValue = 100.0 // Threshold mais exigente
        )

        val session = createTestSession("USR-19", "TRIAL-POL-CUSTOM")
        val attempt = createTestAttempt(session.id, 1, listOf("EV-19"), listOf("M-19"), 75.0)
        val ev = createTestEvidence("EV-19", "PROV-19")
        val m = createTestMeasurement("M-19")
        val prov = createTestProvenance("PROV-19")

        val resV1 = evaluator.evaluateSessionResult(
            session = session,
            policy = policyV1,
            attempts = listOf(attempt),
            evidences = listOf(ev),
            measurements = listOf(m),
            provenances = mapOf("PROV-19" to prov)
        )

        val resV2 = evaluator.evaluateSessionResult(
            session = session,
            policy = policyV2,
            attempts = listOf(attempt),
            evidences = listOf(ev),
            measurements = listOf(m),
            provenances = mapOf("PROV-19" to prov)
        )

        assertEquals("1.0.0", resV1.trialPolicyVersion)
        assertEquals(TrialResultStatus.QUALIFIED, resV1.resultStatus) // 75 >= 50

        assertEquals("2.0.0-CONSORCIO-ATUALIZADO", resV2.trialPolicyVersion)
        assertEquals(TrialResultStatus.NOT_QUALIFIED, resV2.resultStatus) // 75 < 100
    }

    // =========================================================================
    // TESTE 20: Trial Qualified não altera automaticamente a classe
    // =========================================================================
    @Test
    fun testTrialQualifiedDoesNotAutomaticallyPromoteClass() {
        val policy = TrialPolicy(
            trialPolicyId = "TRIAL-POL-CLASS-08-PROMO-CHECK",
            classId = ClassCatalog.CLASS_08,
            version = "1.0.0",
            name = "Prova Check",
            description = "Desc",
            category = TrialCategory.NEUROMUSCULAR,
            requiredEvidenceTypes = listOf("DIRECT_SENSOR"),
            protocolId = "PROT-TRIAL-LOAD-01",
            allowedDevices = listOf("LAB_FORCE_PLATE_V1"),
            minimumAttempts = 1,
            scoringMethod = TrialScoringMethod.BEST_ATTEMPT,
            thresholdValue = 20.0,
            methodologyStatus = "ACTIVE"
        )

        val session = createTestSession("USR-20", policy.trialPolicyId, classId = ClassCatalog.CLASS_08)
        val attempt = createTestAttempt(session.id, 1, listOf("EV-20"), listOf("M-20"), 35.0) // 35 >= 20
        val ev = createTestEvidence("EV-20", "PROV-20")
        val m = createTestMeasurement("M-20")
        val prov = createTestProvenance("PROV-20")

        val result = evaluator.evaluateSessionResult(
            session = session,
            policy = policy,
            attempts = listOf(attempt),
            evidences = listOf(ev),
            measurements = listOf(m),
            provenances = mapOf("PROV-20" to prov)
        )

        assertEquals(TrialResultStatus.QUALIFIED, result.resultStatus)

        // O resultado isolado do Trial não altera a classe do usuário.
        // A classe permanece vinculada à sessão avaliada sem promoção automática executada.
        assertEquals(ClassCatalog.CLASS_08, session.classId)
        assertEquals(ClassCatalog.CLASS_08, result.classId)
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private fun createTestSession(
        userId: String,
        trialPolicyId: String,
        classId: String = ClassCatalog.CLASS_08
    ): TrialSession {
        return TrialSession(
            id = "TS-TEST-${java.util.UUID.randomUUID()}",
            userId = userId,
            classId = classId,
            trialPolicyId = trialPolicyId,
            policyVersion = "1.0.0",
            startedAt = System.currentTimeMillis(),
            completedAt = null,
            status = TrialSessionStatus.RUNNING,
            attemptCount = 1,
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            sessionIntegrity = IntegrityStatus.VALID,
            isMock = false,
            simulationMode = false,
            auditReference = "AUDIT-TS-TEST"
        )
    }

    private fun createTestAttempt(
        sessionId: String,
        attemptNumber: Int,
        evidenceIds: List<String>,
        measurementIds: List<String>,
        resultValue: Double
    ): com.example.core.trialengine.model.TrialAttempt {
        return com.example.core.trialengine.model.TrialAttempt(
            id = "ATT-TEST-$attemptNumber",
            sessionId = sessionId,
            attemptNumber = attemptNumber,
            startedAt = System.currentTimeMillis() - 10000,
            completedAt = System.currentTimeMillis(),
            rawEvidenceIds = evidenceIds,
            measurementIds = measurementIds,
            resultValue = resultValue,
            unit = "N/kg",
            validationStatus = TrialAttemptValidationStatus.VALID,
            invalidationReason = null,
            integrityHash = "HASH-TEST-$attemptNumber",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createTestEvidence(
        id: String,
        provenanceId: String,
        isMock: Boolean = false
    ): DataCoreEvidence {
        return DataCoreEvidence(
            id = id,
            userId = "USR-TEST",
            assessmentId = "ASSESS-TEST",
            measurementIds = listOf("M-$id"),
            source = "LAB_FORCE_PLATE_V1",
            capturedAt = System.currentTimeMillis(),
            submittedAt = System.currentTimeMillis(),
            integrityStatus = IntegrityStatus.VALID,
            reliabilityScore = 1.0,
            confidenceScore = 1.0,
            provenanceId = provenanceId,
            coreVersion = "1.0.0",
            isMock = isMock,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createTestMeasurement(
        id: String,
        isMock: Boolean = false
    ): DataCoreMeasurement {
        return DataCoreMeasurement(
            id = id,
            assessmentId = "ASSESS-TEST",
            userId = "USR-TEST",
            metricId = "METRIC-FORCE",
            rawValue = 100.0,
            normalizedValue = 1.0,
            unit = "N",
            timestamp = System.currentTimeMillis(),
            source = "LAB_FORCE_PLATE_V1",
            deviceId = "LAB_FORCE_PLATE_V1",
            protocolId = "PROT-TRIAL-LOAD-01",
            validationStatus = ValidationStatus.VALID,
            rejectionReason = null,
            rawDataInputId = "RAW-01",
            isMock = isMock,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createTestProvenance(id: String): DataCoreProvenance {
        return DataCoreProvenance(
            id = id,
            sourceType = "DIRECT_SENSOR",
            sourceIdentifier = "LAB_FORCE_PLATE_V1",
            deviceIdentifier = "LAB_FORCE_PLATE_V1",
            captureTimestamp = System.currentTimeMillis() - 1000,
            processingTimestamp = System.currentTimeMillis(),
            processingVersion = "1.0.0",
            protocolId = "PROT-TRIAL-LOAD-01",
            integrityHash = "HASH-PROV-01",
            createdAt = System.currentTimeMillis()
        )
    }
}
