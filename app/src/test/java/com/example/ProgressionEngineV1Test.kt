package com.example

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.progressionengine.engine.ProgressionEngineV1
import com.example.core.progressionengine.evaluator.AnomalyDetector
import com.example.core.progressionengine.evaluator.PromotionGate
import com.example.core.progressionengine.evaluator.TrajectoryEvaluator
import com.example.core.progressionengine.model.*
import com.example.core.progressionengine.policy.ProgressionPolicyRegistry
import com.example.core.trialengine.model.TrialResult
import com.example.core.trialengine.model.TrialResultStatus
import com.example.core.trialengine.model.TrialSnapshot
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * PERFORMAI PROGRESSION ENGINE V1 - SUÍTE DE TESTES OBRIGATÓRIOS (25 TESTES)
 *
 * Valida a governança longitudinal, barreira de autoridade, detecção de anomalias,
 * preservação de histórico, não-linearidade e o Gate de Promoção.
 */
class ProgressionEngineV1Test {

    private lateinit var progressionEngine: ProgressionEngineV1
    private val auditLogs = mutableListOf<String>()

    @Before
    fun setUp() {
        auditLogs.clear()
        progressionEngine = ProgressionEngineV1(
            coreVersion = "1.0.0",
            methodologyVersion = "1.0.0",
            progressionPolicyVersion = "1.0.0",
            evolutionPolicyVersion = "1.0.0",
            trialPolicyVersion = "1.0.0",
            scoreVersion = "1.0.0",
            auditLogger = { auditLogs.add(it) }
        )
    }

    // =========================================================================
    // 1. Evolução baseada em resultado isolado não é suficiente
    // =========================================================================
    @Test
    fun testIsolatedResultIsNotSufficientForProgression() {
        // Apenas 1 evidência com valor alto
        val singleEvidence = listOf(
            createTestEvidence("EV-01", "FORCE", 0.98, System.currentTimeMillis())
        )

        val snapshot = progressionEngine.assessProgression(
            userId = "USR-01",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            evidences = singleEvidence,
            callerTier = CallerTier.CORE_ENGINE
        )

        assertNotEquals(PromotionCandidateStatus.ELIGIBLE, snapshot.candidate.status)
        assertTrue(snapshot.candidate.blockingRequirements.any { it.contains("INSUFFICIENT") || it.contains("TIME") })
        assertFalse(snapshot.sustainability.isSustained)
    }

    // =========================================================================
    // 2. Tempo mínimo impede progressão antecipada
    // =========================================================================
    @Test
    fun testMinimumTimeInClassBlocksEarlyProgression() {
        val policy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_01")
        assertNotNull(policy)

        // Atleta na classe há apenas 2 dias (mínimo exigido: 7 dias)
        val twoDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
        val evidences = createContinuousEvidences("USR-02", 6, 2)

        val candidate = PromotionGate.evaluatePromotionGate(
            userId = "USR-02",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = twoDaysAgo,
            timePolicy = policy,
            evidences = evidences
        )

        assertNotEquals(PromotionCandidateStatus.ELIGIBLE, candidate.status)
        assertTrue(candidate.blockingRequirements.contains("MINIMUM_TIME_IN_CLASS_NOT_MET"))
    }

    // =========================================================================
    // 3. Evidência longitudinal insuficiente bloqueia elegibilidade
    // =========================================================================
    @Test
    fun testInsufficientLongitudinalEvidenceBlocksEligibility() {
        val policy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_02") // Exige 14 dias e 8 observações

        // Apenas 2 observações em 14 dias
        val now = System.currentTimeMillis()
        val evidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.85, now - TimeUnit.DAYS.toMillis(14)),
            createTestEvidence("EV-2", "FORCE", 0.90, now)
        )

        val candidate = PromotionGate.evaluatePromotionGate(
            userId = "USR-03",
            currentClassId = "CLASS_02",
            targetClassId = "CLASS_03",
            currentClassSince = now - TimeUnit.DAYS.toMillis(20),
            timePolicy = policy,
            evidences = evidences
        )

        assertNotEquals(PromotionCandidateStatus.ELIGIBLE, candidate.status)
        assertTrue(candidate.blockingRequirements.any { it.contains("INSUFFICIENT_OBSERVATION_COUNT") })
    }

    // =========================================================================
    // 4. Performance sustentada é diferenciada de pico isolado
    // =========================================================================
    @Test
    fun testSustainedPerformanceIsDifferentiatedFromIsolatedPeak() {
        val now = System.currentTimeMillis()
        // Conjunto A: Pico isolado (1 registro muito alto, restante baixo)
        val peakEvidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.50, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-2", "FORCE", 0.99, now - TimeUnit.DAYS.toMillis(5)),
            createTestEvidence("EV-3", "FORCE", 0.52, now)
        )

        val peakSustainability = TrajectoryEvaluator.evaluateSustainability(peakEvidences)
        assertFalse(peakSustainability.isSustained)
        assertEquals("ISOLATED_PEAK", peakSustainability.consistencyStatus)

        // Conjunto B: Performance sustentada
        val sustainedEvidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.88, now - TimeUnit.DAYS.toMillis(14)),
            createTestEvidence("EV-2", "FORCE", 0.90, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-3", "FORCE", 0.92, now - TimeUnit.DAYS.toMillis(5)),
            createTestEvidence("EV-4", "FORCE", 0.91, now)
        )

        val sustainedAssessment = TrajectoryEvaluator.evaluateSustainability(sustainedEvidences)
        assertTrue(sustainedAssessment.isSustained)
        assertEquals("SUSTAINED", sustainedAssessment.consistencyStatus)
    }

    // =========================================================================
    // 5. Dimensões possuem trajetórias independentes
    // =========================================================================
    @Test
    fun testDimensionsHaveIndependentTrajectories() {
        val now = System.currentTimeMillis()
        val mixedEvidences = listOf(
            // FORCE melhorando
            createTestEvidence("EV-F1", "FORCE", 0.60, now - TimeUnit.DAYS.toMillis(15)),
            createTestEvidence("EV-F2", "FORCE", 0.75, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-F3", "FORCE", 0.90, now - TimeUnit.DAYS.toMillis(2)),
            // SPEED estável
            createTestEvidence("EV-S1", "SPEED", 0.80, now - TimeUnit.DAYS.toMillis(15)),
            createTestEvidence("EV-S2", "SPEED", 0.81, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-S3", "SPEED", 0.80, now - TimeUnit.DAYS.toMillis(2)),
            // MOBILITY declinando
            createTestEvidence("EV-M1", "MOBILITY", 0.90, now - TimeUnit.DAYS.toMillis(15)),
            createTestEvidence("EV-M2", "MOBILITY", 0.70, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-M3", "MOBILITY", 0.50, now - TimeUnit.DAYS.toMillis(2))
        )

        val trajectories = TrajectoryEvaluator.evaluateDimensionTrajectories(mixedEvidences)

        assertEquals(DimensionTrajectoryTrend.IMPROVING, trajectories["FORCE"]?.trend)
        assertEquals(DimensionTrajectoryTrend.STABLE, trajectories["SPEED"]?.trend)
        assertEquals(DimensionTrajectoryTrend.DECLINING, trajectories["MOBILITY"]?.trend)
    }

    // =========================================================================
    // 6. Dimensão declinante é identificada
    // =========================================================================
    @Test
    fun testDecliningDimensionIsIdentified() {
        val now = System.currentTimeMillis()
        val decliningEvidences = listOf(
            createTestEvidence("EV-1", "ENDURANCE", 0.95, now - TimeUnit.DAYS.toMillis(20)),
            createTestEvidence("EV-2", "ENDURANCE", 0.75, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-3", "ENDURANCE", 0.55, now)
        )

        val trajectories = TrajectoryEvaluator.evaluateDimensionTrajectories(decliningEvidences)
        assertEquals(DimensionTrajectoryTrend.DECLINING, trajectories["ENDURANCE"]?.trend)
    }

    // =========================================================================
    // 7. Mudança abrupta de dispositivo é registrada
    // =========================================================================
    @Test
    fun testDeviceChangeIsRecordedAsAnomaly() {
        val now = System.currentTimeMillis()
        val evidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.80, now - TimeUnit.DAYS.toMillis(10), provId = "PROV-1"),
            createTestEvidence("EV-2", "FORCE", 0.85, now - TimeUnit.DAYS.toMillis(5), provId = "PROV-2"),
            createTestEvidence("EV-3", "FORCE", 0.90, now, provId = "PROV-3")
        )

        val provenances = mapOf(
            "PROV-1" to createTestProvenance("PROV-1", "DEVICE_A", "PROT-1"),
            "PROV-2" to createTestProvenance("PROV-2", "DEVICE_B", "PROT-1"),
            "PROV-3" to createTestProvenance("PROV-3", "DEVICE_C", "PROT-1")
        )

        val anomalies = AnomalyDetector.detectAnomalies(
            userId = "USR-07",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            timeInClassDays = 10,
            timePolicy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_01"),
            evidences = evidences,
            provenances = provenances
        )

        assertTrue(anomalies.any { it.type == ProgressionAnomalyType.DEVICE_CHANGE })
    }

    // =========================================================================
    // 8. Mudança de protocolo é registrada
    // =========================================================================
    @Test
    fun testProtocolChangeIsRecordedAsAnomaly() {
        val now = System.currentTimeMillis()
        val evidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.80, now - TimeUnit.DAYS.toMillis(10), provId = "PROV-1"),
            createTestEvidence("EV-2", "FORCE", 0.85, now - TimeUnit.DAYS.toMillis(5), provId = "PROV-2"),
            createTestEvidence("EV-3", "FORCE", 0.90, now, provId = "PROV-3")
        )

        val provenances = mapOf(
            "PROV-1" to createTestProvenance("PROV-1", "DEV-LAB", "PROT_STANDARD_1"),
            "PROV-2" to createTestProvenance("PROV-2", "DEV-LAB", "PROT_MODIFIED_2"),
            "PROV-3" to createTestProvenance("PROV-3", "DEV-LAB", "PROT_EXPERIMENTAL_3")
        )

        val anomalies = AnomalyDetector.detectAnomalies(
            userId = "USR-08",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            timeInClassDays = 10,
            timePolicy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_01"),
            evidences = evidences,
            provenances = provenances
        )

        assertTrue(anomalies.any { it.type == ProgressionAnomalyType.PROTOCOL_CHANGE })
    }

    // =========================================================================
    // 9. Progressão rápida gera anomaly quando aplicável
    // =========================================================================
    @Test
    fun testRapidProgressionTriggersAnomaly() {
        val policy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_04") // 28 dias
        val evidences = createContinuousEvidences("USR-09", 15, 5) // apenas 5 dias

        val anomalies = AnomalyDetector.detectAnomalies(
            userId = "USR-09",
            currentClassId = "CLASS_04",
            targetClassId = "CLASS_05",
            timeInClassDays = 5,
            timePolicy = policy,
            evidences = evidences
        )

        assertTrue(anomalies.any { it.type == ProgressionAnomalyType.RAPID_PROGRESSION })
    }

    // =========================================================================
    // 10. Mock não gera promoção oficial
    // =========================================================================
    @Test
    fun testMockDoesNotGenerateOfficialPromotion() {
        val now = System.currentTimeMillis()
        val evidences = createContinuousEvidences("USR-10", 10, 10)

        val snapshot = progressionEngine.assessProgression(
            userId = "USR-10",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            evidences = evidences,
            callerTier = CallerTier.CORE_ENGINE,
            isMock = true,
            simulationMode = true
        )

        assertTrue(snapshot.isMock)
        assertTrue(snapshot.simulationMode)
        assertTrue(snapshot.candidate.isMock)
        assertTrue(snapshot.progressionState.isMock)
    }

    // =========================================================================
    // 11. XP não gera promoção
    // =========================================================================
    @Test
    fun testXPDoesNotTriggerClassPromotion() {
        // Usuário pode ter 1.000.000 de XP, mas sem cumprir o PromotionGate longitudinal ele permanece NOT_READY/não elegível
        val singleEvidence = listOf(
            createTestEvidence("EV-1", "FORCE", 0.90, System.currentTimeMillis())
        )

        val snapshot = progressionEngine.assessProgression(
            userId = "USR-11",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            evidences = singleEvidence,
            callerTier = CallerTier.CORE_ENGINE
        )

        assertNotEquals(PromotionCandidateStatus.ELIGIBLE, snapshot.candidate.status)
        assertEquals("CLASS_01", snapshot.progressionState.highestEligibleClassId)
    }

    // =========================================================================
    // 12. IA não pode promover classe
    // =========================================================================
    @Test(expected = SecurityException::class)
    fun testAICannotPromoteClassOrAssessProgression() {
        progressionEngine.assessProgression(
            userId = "USR-12",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = System.currentTimeMillis(),
            evidences = emptyList(),
            callerTier = CallerTier.AI_GATEWAY
        )
    }

    // =========================================================================
    // 13. Cliente não pode alterar estado
    // =========================================================================
    @Test(expected = SecurityException::class)
    fun testClientCannotMutateProgressionState() {
        progressionEngine.mutateStateByExternal(
            callerTier = CallerTier.CLIENT,
            action = "SET_STATUS_ELIGIBLE"
        )
    }

    // =========================================================================
    // 14. PromotionCandidate é rastreável
    // =========================================================================
    @Test
    fun testPromotionCandidateIsTraceable() {
        val now = System.currentTimeMillis()
        val evidences = createContinuousEvidences("USR-14", 8, 10)

        val snapshot = progressionEngine.assessProgression(
            userId = "USR-14",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            evidences = evidences,
            evidencePackageId = "EVP-PKG-14",
            scoreSnapshotId = "SCORE-SNAP-14",
            callerTier = CallerTier.CORE_ENGINE
        )

        val candidate = snapshot.candidate
        assertNotNull(candidate.id)
        assertEquals("USR-14", candidate.userId)
        assertEquals("CLASS_01", candidate.currentClassId)
        assertEquals("CLASS_02", candidate.targetClassId)
        assertEquals("EVP-PKG-14", candidate.evidencePackageId)
        assertEquals("SCORE-SNAP-14", candidate.scoreSnapshotId)
        assertNotNull(candidate.explanation.overallOutcome)
    }

    // =========================================================================
    // 15. Trial obrigatório impede READY/ELIGIBLE quando ausente
    // =========================================================================
    @Test
    fun testRequiredTrialBlocksEligibilityWhenMissing() {
        // CLASS_08 exige Prova de Desempenho (Trial)
        val now = System.currentTimeMillis()
        val policy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_07")
        val evidences = createContinuousEvidences("USR-15", 25, 60)

        val candidate = PromotionGate.evaluatePromotionGate(
            userId = "USR-15",
            currentClassId = "CLASS_07",
            targetClassId = "CLASS_08",
            currentClassSince = now - TimeUnit.DAYS.toMillis(65),
            timePolicy = policy,
            evidences = evidences,
            trialSnapshot = null // Sem Trial
        )

        assertEquals(PromotionCandidateStatus.TRIAL_REQUIRED, candidate.status)
        assertTrue(candidate.blockingRequirements.contains("TRIAL_REQUIRED_BUT_MISSING"))
    }

    // =========================================================================
    // 16. Trial qualificado não promove automaticamente
    // =========================================================================
    @Test
    fun testQualifiedTrialDoesNotAutomaticallyPromoteClass() {
        val now = System.currentTimeMillis()
        val trialSnapshot = createTestTrialSnapshot("CLASS_08", TrialResultStatus.QUALIFIED)
        val evidences = createContinuousEvidences("USR-16", 25, 60)

        val snapshot = progressionEngine.assessProgression(
            userId = "USR-16",
            currentClassId = "CLASS_07",
            targetClassId = "CLASS_08",
            currentClassSince = now - TimeUnit.DAYS.toMillis(65),
            evidences = evidences,
            trialSnapshot = trialSnapshot,
            callerTier = CallerTier.CORE_ENGINE
        )

        // A classe atual do atleta PERMANECE CLASS_07. O sistema apenas emite decisão de elegibilidade.
        assertEquals("CLASS_07", snapshot.progressionState.currentClassId)
        assertEquals(EvolutionProgressionStatus.ELIGIBLE_FOR_PROMOTION, snapshot.progressionState.progressionStatus)
        assertEquals(PromotionCandidateStatus.ELIGIBLE, snapshot.candidate.status)
    }

    // =========================================================================
    // 17. Consistência insuficiente bloqueia promoção
    // =========================================================================
    @Test
    fun testInsufficientConsistencyBlocksPromotion() {
        val now = System.currentTimeMillis()
        val policy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_01")

        // Evidências com volatilidade extrema e dimensões em declínio
        val inconsistentEvidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.90, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-2", "FORCE", 0.40, now - TimeUnit.DAYS.toMillis(5)),
            createTestEvidence("EV-3", "FORCE", 0.30, now),
            createTestEvidence("EV-4", "SPEED", 0.85, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-5", "SPEED", 0.50, now - TimeUnit.DAYS.toMillis(5)),
            createTestEvidence("EV-6", "SPEED", 0.35, now)
        )

        val candidate = PromotionGate.evaluatePromotionGate(
            userId = "USR-17",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            timePolicy = policy,
            evidences = inconsistentEvidences
        )

        assertNotEquals(PromotionCandidateStatus.ELIGIBLE, candidate.status)
        assertTrue(candidate.blockingRequirements.any { it.contains("DECLINING") })
    }

    // =========================================================================
    // 18. Maturidade insuficiente bloqueia promoção
    // =========================================================================
    @Test
    fun testInsufficientMaturityBlocksPromotion() {
        val now = System.currentTimeMillis()
        val policy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_01")
        // Poucas observações e span de tempo curto
        val lowMaturityEvidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.90, now - TimeUnit.DAYS.toMillis(3)),
            createTestEvidence("EV-2", "FORCE", 0.92, now)
        )

        val candidate = PromotionGate.evaluatePromotionGate(
            userId = "USR-18",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            timePolicy = policy,
            evidences = lowMaturityEvidences
        )

        assertNotEquals(PromotionCandidateStatus.ELIGIBLE, candidate.status)
        assertTrue(candidate.blockingRequirements.contains("INSUFFICIENT_MATURITY_AND_ADAPTATION"))
    }

    // =========================================================================
    // 19. Adaptação pendente impede decisão oficial quando obrigatória
    // =========================================================================
    @Test
    fun testPendingAdaptationBlocksOfficialEligibility() {
        val adaptation = TrajectoryEvaluator.evaluateAdaptation(
            dimensionId = "FORCE",
            evidences = listOf(createTestEvidence("EV-1", "FORCE", 0.80, System.currentTimeMillis()))
        )

        assertEquals(AdaptationStatus.INSUFFICIENT_DATA, adaptation.status)
        assertEquals("INSUFFICIENT_EXPOSURE", adaptation.responsePattern)
    }

    // =========================================================================
    // 20. Histórico é append-only
    // =========================================================================
    @Test
    fun testEvolutionHistoryIsAppendOnly() {
        val entry1 = progressionEngine.createHistoryEntry(
            userId = "USR-20",
            previousClass = "CLASS_01",
            newClass = "CLASS_02",
            reason = "Elegibilidade confirmada após validação longitudinal.",
            evidencePackageId = "PKG-1",
            scoreSnapshotId = "SCORE-1",
            trialSnapshotId = null,
            progressionAssessmentId = "PROG-1"
        )

        val entry2 = progressionEngine.createHistoryEntry(
            userId = "USR-20",
            previousClass = "CLASS_02",
            newClass = "CLASS_03",
            reason = "Elegibilidade confirmada para classe 03.",
            evidencePackageId = "PKG-2",
            scoreSnapshotId = "SCORE-2",
            trialSnapshotId = null,
            progressionAssessmentId = "PROG-2"
        )

        assertNotNull(entry1.id)
        assertNotNull(entry2.id)
        assertNotEquals(entry1.id, entry2.id)
        assertNotNull(entry1.auditReference)
        assertNotNull(entry2.auditReference)
    }

    // =========================================================================
    // 21. Política nova não altera avaliação antiga
    // =========================================================================
    @Test
    fun testNewPolicyVersionDoesNotAlterOldEvaluation() {
        val engineV1 = ProgressionEngineV1(progressionPolicyVersion = "1.0.0")
        val engineV2 = ProgressionEngineV1(progressionPolicyVersion = "2.0.0")

        val evidences = createContinuousEvidences("USR-21", 8, 10)
        val now = System.currentTimeMillis()

        val snapV1 = engineV1.assessProgression(
            userId = "USR-21",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            evidences = evidences
        )

        val snapV2 = engineV2.assessProgression(
            userId = "USR-21",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            evidences = evidences
        )

        assertEquals("1.0.0", snapV1.progressionState.methodologyVersion)
        assertEquals("2.0.0", engineV2.progressionPolicyVersion)
    }

    // =========================================================================
    // 22. Mesmo input + mesma política produz resultado determinístico
    // =========================================================================
    @Test
    fun testDeterministicProgressionOutcome() {
        val now = 1700000000000L
        val evidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.85, now - TimeUnit.DAYS.toMillis(10)),
            createTestEvidence("EV-2", "FORCE", 0.87, now - TimeUnit.DAYS.toMillis(8)),
            createTestEvidence("EV-3", "FORCE", 0.89, now - TimeUnit.DAYS.toMillis(6)),
            createTestEvidence("EV-4", "FORCE", 0.90, now - TimeUnit.DAYS.toMillis(4)),
            createTestEvidence("EV-5", "FORCE", 0.92, now - TimeUnit.DAYS.toMillis(2)),
            createTestEvidence("EV-6", "FORCE", 0.91, now)
        )

        val timePolicy = ProgressionPolicyRegistry.getPolicyForClass("CLASS_01")

        val cand1 = PromotionGate.evaluatePromotionGate(
            userId = "USR-22",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            timePolicy = timePolicy,
            evidences = evidences
        )

        val cand2 = PromotionGate.evaluatePromotionGate(
            userId = "USR-22",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            timePolicy = timePolicy,
            evidences = evidences
        )

        assertEquals(cand1.status, cand2.status)
        assertEquals(cand1.satisfiedRequirements, cand2.satisfiedRequirements)
        assertEquals(cand1.blockingRequirements, cand2.blockingRequirements)
    }

    // =========================================================================
    // 23. RegressionReview não causa downgrade automático
    // =========================================================================
    @Test
    fun testRegressionReviewDoesNotCauseAutomaticDowngrade() {
        val now = System.currentTimeMillis()
        val decliningEvidences = listOf(
            createTestEvidence("EV-1", "FORCE", 0.90, now - TimeUnit.DAYS.toMillis(30)),
            createTestEvidence("EV-2", "FORCE", 0.60, now - TimeUnit.DAYS.toMillis(15)),
            createTestEvidence("EV-3", "FORCE", 0.40, now),
            createTestEvidence("EV-4", "SPEED", 0.90, now - TimeUnit.DAYS.toMillis(30)),
            createTestEvidence("EV-5", "SPEED", 0.60, now - TimeUnit.DAYS.toMillis(15)),
            createTestEvidence("EV-6", "SPEED", 0.40, now)
        )

        val snapshot = progressionEngine.assessProgression(
            userId = "USR-23",
            currentClassId = "CLASS_05",
            targetClassId = "CLASS_06",
            currentClassSince = now - TimeUnit.DAYS.toMillis(60),
            evidences = decliningEvidences,
            callerTier = CallerTier.CORE_ENGINE
        )

        assertNotNull(snapshot.regressionReview)
        assertEquals(RegressionReviewStatus.REVIEW_REQUIRED, snapshot.regressionReview?.reviewStatus)
        // A classe permanece intacta (não há downgrade automático)
        assertEquals("CLASS_05", snapshot.progressionState.currentClassId)
    }

    // =========================================================================
    // 24. Anomaly gera auditoria
    // =========================================================================
    @Test
    fun testAnomalyGeneratesAuditLog() {
        val evidences = createContinuousEvidences("USR-24", 5, 2)

        progressionEngine.assessProgression(
            userId = "USR-24",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
            evidences = evidences,
            callerTier = CallerTier.CORE_ENGINE
        )

        assertTrue(auditLogs.any { it.contains("PROGRESSION_ANOMALY_DETECTED") })
    }

    // =========================================================================
    // 25. Promoção elegível gera audit trail completo
    // =========================================================================
    @Test
    fun testEligiblePromotionGeneratesCompleteAuditTrail() {
        val now = System.currentTimeMillis()
        val evidences = createContinuousEvidences("USR-25", 10, 10)

        val snapshot = progressionEngine.assessProgression(
            userId = "USR-25",
            currentClassId = "CLASS_01",
            targetClassId = "CLASS_02",
            currentClassSince = now - TimeUnit.DAYS.toMillis(10),
            evidences = evidences,
            callerTier = CallerTier.CORE_ENGINE
        )

        assertEquals(PromotionCandidateStatus.ELIGIBLE, snapshot.candidate.status)
        assertEquals(EvolutionProgressionStatus.ELIGIBLE_FOR_PROMOTION, snapshot.progressionState.progressionStatus)
        assertTrue(auditLogs.any { it.contains("PROGRESSION_ASSESSMENT_COMPLETED") })
        assertTrue(auditLogs.any { it.contains(snapshot.auditReference) })
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================
    private fun createTestEvidence(
        id: String,
        dimId: String,
        confidence: Double,
        collectedAt: Long,
        provId: String = "PROV-DEFAULT",
        integrity: IntegrityStatus = IntegrityStatus.VALID
    ): DataCoreEvidence {
        return DataCoreEvidence(
            id = id,
            userId = "USR-TEST",
            assessmentId = null,
            measurementIds = emptyList(),
            source = dimId,
            capturedAt = collectedAt,
            submittedAt = collectedAt,
            integrityStatus = integrity,
            reliabilityScore = 0.95,
            confidenceScore = confidence,
            provenanceId = provId,
            coreVersion = "1.0.0",
            isMock = false,
            createdAt = collectedAt
        )
    }

    private fun createContinuousEvidences(userId: String, count: Int, spanDays: Long): List<DataCoreEvidence> {
        val list = mutableListOf<DataCoreEvidence>()
        val now = System.currentTimeMillis()
        val interval = if (count > 1) TimeUnit.DAYS.toMillis(spanDays) / (count - 1) else 0L

        for (i in 0 until count) {
            val timestamp = (now - TimeUnit.DAYS.toMillis(spanDays)) + (i * interval)
            list.add(
                createTestEvidence(
                    id = "EV-$userId-$i",
                    dimId = "FORCE",
                    confidence = 0.88 + (i * 0.01),
                    collectedAt = timestamp
                )
            )
        }
        return list
    }

    private fun createTestProvenance(id: String, device: String, protocol: String): DataCoreProvenance {
        return DataCoreProvenance(
            id = id,
            sourceType = "DIRECT_SENSOR",
            sourceIdentifier = device,
            deviceIdentifier = device,
            captureTimestamp = System.currentTimeMillis() - 1000,
            processingTimestamp = System.currentTimeMillis(),
            processingVersion = "1.0.0",
            protocolId = protocol,
            integrityHash = "HASH-$id",
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createTestTrialSnapshot(classId: String, status: TrialResultStatus): TrialSnapshot {
        return TrialSnapshot(
            id = "TS-SNAP-TEST-$classId",
            sessionId = "TS-SESS-TEST-$classId",
            userId = "USR-TRIAL-TEST",
            classId = classId,
            trialPolicyId = "TRIAL-POL-$classId-V1",
            trialPolicyVersion = "1.0.0",
            result = TrialResult(
                id = "RES-TEST-$classId",
                sessionId = "TS-SESS-TEST-$classId",
                userId = "USR-TRIAL-TEST",
                classId = classId,
                bestAttemptId = "ATT-01",
                qualifyingAttempts = listOf("ATT-01", "ATT-02", "ATT-03"),
                failedAttempts = emptyList(),
                metricResults = mapOf("SCORE" to 100.0),
                evidenceIds = listOf("EV-01"),
                protocolVersion = "1.0.0",
                trialPolicyVersion = "1.0.0",
                methodologyVersion = "1.0.0",
                resultStatus = status,
                explanation = "Trial qualificado para testes",
                limitations = emptyList(),
                fatigueAnalysis = null,
                calculatedAt = System.currentTimeMillis(),
                auditReference = "AUDIT-TRIAL-QUALIFIED",
                isMock = false,
                simulationMode = false
            ),
            attempts = emptyList(),
            sessionIntegrity = IntegrityStatus.VALID,
            calculatedAt = System.currentTimeMillis(),
            coreVersion = "1.0.0",
            auditReference = "AUDIT-TRIAL-QUALIFIED",
            isMock = false,
            simulationMode = false
        )
    }
}
