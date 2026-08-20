package com.example.core.trainingengine

import com.example.core.aigateway.AIGateway
import com.example.core.aigateway.FoundationAIGateway
import com.example.core.aigateway.roles.AIAdvisoryRole
import com.example.core.aigateway.roles.AIAdvisoryService
import com.example.core.exerciseengine.model.EquipmentType
import com.example.core.privacy.PrivacyConsent
import com.example.core.privacy.PrivacyPolicyManager
import com.example.core.security.PasswordHasher
import com.example.core.sync.ConflictResolutionStrategy
import com.example.core.sync.OfflineSyncManager
import com.example.core.sync.SyncEntityType
import com.example.core.trainingengine.baseline.AthleteBaselineEngineV1
import com.example.core.trainingengine.baseline.AthleteGoalType
import com.example.core.trainingengine.baseline.ExperienceLevel
import com.example.core.trainingengine.engine.TrainingEngineV1
import com.example.core.trainingengine.history.TrainingHistoryEngineV1
import com.example.core.trainingengine.model.SessionSetLog
import com.example.core.trainingengine.model.SyncStatus
import com.example.core.trainingengine.model.ValueState
import com.example.core.trainingengine.taxonomy.TrainingModality
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * EVOLUTION HUMAN AI — TRAINING HISTORY, ADVISORY & OFFLINE-FIRST TEST SUITE
 */
class TrainingHistoryAndAdvisoryTest {

    private lateinit var historyEngine: TrainingHistoryEngineV1
    private lateinit var trainingEngine: TrainingEngineV1
    private lateinit var baselineEngine: AthleteBaselineEngineV1
    private lateinit var syncManager: OfflineSyncManager
    private lateinit var advisoryService: AIAdvisoryService

    @Before
    fun setUp() {
        historyEngine = TrainingHistoryEngineV1()
        trainingEngine = TrainingEngineV1()
        baselineEngine = AthleteBaselineEngineV1()
        syncManager = OfflineSyncManager(ConflictResolutionStrategy.LAST_WRITE_WINS)
        advisoryService = AIAdvisoryService(FoundationAIGateway())
    }

    // ------------------------------------------------------------------------
    // 1. TRAINING HISTORY & LONGITUDINAL ANALYTICS
    // ------------------------------------------------------------------------
    @Test
    fun testHistoryAnalyticsAndPersonalRecords() {
        var s1 = trainingEngine.startFreeformSession("ATHLETE-001", "Sessão 1", 1000L)
        s1 = trainingEngine.logSet(
            session = s1,
            exerciseId = "EX-SQ-BW-001-V1",
            setLog = SessionSetLog(
                setNumber = 1,
                reps = ValueState.Recorded(10),
                loadKg = ValueState.Recorded(80.0),
                completed = true
            ),
            exerciseName = "Bodyweight Squat"
        )
        val completedS1 = trainingEngine.finishSession(s1, 1000L + (45 * 60 * 1000L))

        var s2 = trainingEngine.startFreeformSession("ATHLETE-001", "Sessão 2", 1000L + (86400000L * 2))
        s2 = trainingEngine.logSet(
            session = s2,
            exerciseId = "EX-SQ-BW-001-V1",
            setLog = SessionSetLog(
                setNumber = 1,
                reps = ValueState.Recorded(8),
                loadKg = ValueState.Recorded(100.0),
                completed = true
            ),
            exerciseName = "Bodyweight Squat"
        )
        val completedS2 = trainingEngine.finishSession(s2, 1000L + (86400000L * 2) + (50 * 60 * 1000L))

        val summary = historyEngine.summarizeHistory(listOf(completedS1, completedS2))

        assertEquals(2, summary.totalSessions)
        assertEquals(2, summary.completedSessions)
        assertEquals(18, summary.totalReps)
        assertEquals(1600.0, summary.totalVolumeKg, 0.001)

        // Verify Personal Record for Squat (Max weight should be 100.0 kg)
        val squatPr = summary.personalRecords["EX-SQ-BW-001-V1"]
        assertNotNull(squatPr)
        assertEquals(100.0, squatPr?.maxWeightKg ?: 0.0, 0.001)
        assertTrue((squatPr?.estimated1RM ?: 0.0) > 100.0)
    }

    // ------------------------------------------------------------------------
    // 2. BASELINE & GOAL IMMUTABILITY & VERSIONING
    // ------------------------------------------------------------------------
    @Test
    fun testAthleteBaselineVersioning() {
        val b1 = baselineEngine.createBaseline(
            userId = "USER-123",
            experienceLevel = ExperienceLevel.BEGINNER,
            primaryModalities = listOf(TrainingModality.STRENGTH),
            availableDaysPerWeek = 3,
            availableEquipment = listOf(EquipmentType.BODYWEIGHT),
            currentVersion = 0
        )

        assertEquals(1, b1.version)
        assertEquals(3, b1.availableDaysPerWeek)

        val b2 = baselineEngine.createBaseline(
            userId = "USER-123",
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            primaryModalities = listOf(TrainingModality.STRENGTH, TrainingModality.CALISTHENICS),
            availableDaysPerWeek = 4,
            availableEquipment = listOf(EquipmentType.BODYWEIGHT, EquipmentType.DUMBBELL),
            currentVersion = b1.version
        )

        assertEquals(2, b2.version)
        assertNotEquals(b1.baselineId, b2.baselineId)
    }

    @Test
    fun testAthleteGoalCreation() {
        val goal = baselineEngine.createGoal(
            userId = "USER-123",
            goalType = AthleteGoalType.STRENGTH,
            title = "Aumentar Agachamento para 120kg",
            targetMetric = "1RM_SQUAT_KG",
            targetValue = 120.0
        )

        assertEquals("Aumentar Agachamento para 120kg", goal.title)
        assertEquals(120.0, goal.targetValue ?: 0.0, 0.001)
        assertFalse(goal.isAchieved)
    }

    // ------------------------------------------------------------------------
    // 3. AI ADVISORY ROLES (EXPLAINER, ANALYST, COACH)
    // ------------------------------------------------------------------------
    @Test
    fun testAIAdvisoryRoles() = runBlocking {
        // AI Explainer
        val explanation = advisoryService.explainClass(com.example.core.evolutionengine.catalog.ClassCatalog.CLASS_01, "Alex")
        assertEquals(AIAdvisoryRole.AI_EXPLAINER, explanation.role)
        assertTrue(explanation.content.contains("Alex"))
        assertTrue(explanation.content.contains("Corpo Adormecido"))

        // AI Analyst
        val summary = historyEngine.summarizeHistory(emptyList())
        val analysis = advisoryService.analyzeTrainingHistory(summary)
        assertEquals(AIAdvisoryRole.AI_ANALYST, analysis.role)
        assertTrue(analysis.content.contains("Volume total acumulado"))

        // AI Coach
        val session = trainingEngine.startFreeformSession("ATHLETE-001", "Manhã de Força")
        val coachFeedback = advisoryService.generatePostSessionFeedback(session)
        assertEquals(AIAdvisoryRole.AI_COACH, coachFeedback.role)
        assertTrue(coachFeedback.content.contains("Manhã de Força"))
    }

    // ------------------------------------------------------------------------
    // 4. OFFLINE SYNC & PRIVACY CONSENT
    // ------------------------------------------------------------------------
    @Test
    fun testOfflineSyncQueue() {
        val item = syncManager.enqueue(
            entityType = SyncEntityType.TRAINING_SESSION,
            entityId = "SESS-001",
            payloadJson = "{\"sessionId\":\"SESS-001\"}"
        )

        assertEquals(SyncStatus.PENDING_SYNC, item.syncStatus)
        assertEquals(1, syncManager.getPendingQueue().size)

        syncManager.markSynced(item.queueId)
        assertTrue(syncManager.getPendingQueue().isEmpty())
    }

    @Test
    fun testPrivacyConsentLocalEnforcement() {
        val consent = PrivacyPolicyManager.createDefaultLocalConsent("USER-LOCAL")
        assertTrue(PrivacyPolicyManager.canProcessCameraLocally(consent))
        assertFalse(PrivacyPolicyManager.canSendTelemetryRemotely(consent))
        assertFalse(consent.allowRawImageRetention)
    }

    @Test
    fun testPasswordHasherPbkdf2AndLazyRehash() {
        val rawPassword = "SecureAthletePassword#2026"
        val v2Hash = PasswordHasher.hashPassword(rawPassword)

        // Verificação do formato PBKDF2 v2
        assertTrue(v2Hash.startsWith("\$pbkdf2-sha512\$i=120000\$"))
        assertFalse(PasswordHasher.needsRehash(v2Hash))
        assertTrue(PasswordHasher.verifyPassword(rawPassword, v2Hash))
        assertFalse(PasswordHasher.verifyPassword("WrongPassword", v2Hash))

        // Verificação de retrocompatibilidade com hash legado v1 (SHA-256)
        val legacyInput = "PERFORMAI_EVOLUTION_FOUNDATION_SALT_v1:$rawPassword"
        val legacyDigest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(legacyInput.toByteArray(Charsets.UTF_8))
        val legacyHash = legacyDigest.joinToString("") { "%02x".format(it) }

        assertTrue(PasswordHasher.needsRehash(legacyHash))
        assertTrue(PasswordHasher.verifyPassword(rawPassword, legacyHash))
        assertFalse(PasswordHasher.verifyPassword("WrongPassword", legacyHash))
    }
}
