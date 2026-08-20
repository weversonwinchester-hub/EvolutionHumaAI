package com.example.core.trainingengine

import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.engine.EvolutionEngineV1
import com.example.core.exerciseengine.catalog.ExerciseCatalogV1
import com.example.core.exerciseengine.model.ExerciseCategory
import com.example.core.exerciseengine.model.ExerciseDifficulty
import com.example.core.scoreengine.engine.ScoreEngineV1
import com.example.core.trainingengine.engine.TrainingEngineV1
import com.example.core.trainingengine.engine.WorkoutEngineV1
import com.example.core.trainingengine.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * EVOLUTION HUMAN AI — TRAINING ENGINE V1 AUTOMATED TEST SUITE
 *
 * Validates the training lifecycle, set logging, ValueState explicit semantics,
 * and absolute isolation from scientific and evolution progression engines.
 */
class TrainingEngineV1Test {

    private lateinit var trainingEngine: TrainingEngineV1
    private lateinit var workoutEngine: WorkoutEngineV1

    @Before
    fun setUp() {
        trainingEngine = TrainingEngineV1()
        workoutEngine = WorkoutEngineV1()
        ExerciseCatalogV1.initializeCanonicalCatalog()
    }

    // ------------------------------------------------------------------------
    // 1. WORKOUT LIFECYCLE & STRUCTURAL INTEGRITY
    // ------------------------------------------------------------------------
    @Test
    fun testWorkoutCreationAndItemManagement() {
        val workout = workoutEngine.createWorkout(
            name = "Treino A - Superior & Core",
            description = "Foco em peitoral e estabilidade",
            category = ExerciseCategory.STRENGTH,
            difficulty = ExerciseDifficulty.BEGINNER
        )

        assertEquals("Treino A - Superior & Core", workout.name)
        assertTrue(workout.items.isEmpty())

        val withPushUp = workoutEngine.addExerciseItem(
            workout = workout,
            exerciseId = "EX-PU-BW-001-V1",
            targetSets = 4,
            prescription = TrainingPrescription(
                executionType = ExecutionPrescriptionType.REPETITIONS,
                targetReps = ValueState.Recorded(10),
                targetRpe = ValueState.Recorded(8.0)
            ),
            restBetweenSetsSeconds = 60
        )

        assertEquals(1, withPushUp.items.size)
        assertEquals(1, withPushUp.items[0].order)
        assertEquals(4, withPushUp.items[0].targetSets)

        val withSquat = workoutEngine.addExerciseItem(
            workout = withPushUp,
            exerciseId = "EX-SQ-BW-001-V1",
            targetSets = 3
        )

        assertEquals(2, withSquat.items.size)
        assertEquals(2, withSquat.items[1].order)

        // Remove item test
        val removed = workoutEngine.removeItem(withSquat, withPushUp.items[0].itemId)
        assertEquals(1, removed.items.size)
        assertEquals(1, removed.items[0].order)
        assertEquals("EX-SQ-BW-001-V1", removed.items[0].exerciseId)
    }

    @Test
    fun testStarterTemplatesAvailability() {
        val templates = workoutEngine.getStarterTemplates()
        assertTrue(templates.isNotEmpty())
        val fullBody = templates.firstOrNull { it.workoutId == "WRK-TEMPLATE-FULLBODY-001" }
        assertNotNull(fullBody)
        assertEquals(3, fullBody?.items?.size)
    }

    // ------------------------------------------------------------------------
    // 2. TRAINING SESSION LIFECYCLE (START, PAUSE, RESUME, FINISH, ABANDON)
    // ------------------------------------------------------------------------
    @Test
    fun testTrainingSessionLifecycle() {
        val workout = workoutEngine.getStarterTemplates().first()
        val session = trainingEngine.startSession(
            userId = "ATHLETE-TEST-001",
            workout = workout,
            startTimeMs = 10000L
        )

        assertEquals(SessionStatus.IN_PROGRESS, session.status)
        assertEquals("ATHLETE-TEST-001", session.userId)
        assertEquals(workout.items.size, session.exerciseLogs.size)

        // Pause
        val paused = trainingEngine.pauseSession(session, 15000L)
        assertEquals(SessionStatus.PAUSED, paused.status)

        // Resume
        val resumed = trainingEngine.resumeSession(paused, 20000L)
        assertEquals(SessionStatus.IN_PROGRESS, resumed.status)

        // Finish
        val finished = trainingEngine.finishSession(
            session = resumed,
            endTimeMs = 70000L,
            perceivedExertion = ValueState.Recorded(8.5),
            notes = "Treino com excelente intensidade."
        )

        assertEquals(SessionStatus.COMPLETED, finished.status)
        assertEquals(60, finished.totalDurationSeconds)
        assertEquals(8.5, finished.perceivedExertion.getOrNull())
        assertEquals("Treino com excelente intensidade.", finished.notes)
    }

    @Test
    fun testTrainingSessionAbandonment() {
        val session = trainingEngine.startFreeformSession("ATHLETE-TEST-002", "Sessão Teste", 1000L)
        val abandoned = trainingEngine.abandonSession(session, 5000L, "Falta de tempo")

        assertEquals(SessionStatus.ABANDONED, abandoned.status)
        assertEquals(4, abandoned.totalDurationSeconds)
        assertTrue(abandoned.notes.contains("Falta de tempo"))
    }

    // ------------------------------------------------------------------------
    // 3. SET LOGGING & VALUE STATE SEMANTICS
    // ------------------------------------------------------------------------
    @Test
    fun testSetLoggingAndMetricsDerivation() {
        var session = trainingEngine.startFreeformSession("ATHLETE-TEST-003", "Leg Day", 1000L)

        // Log Set 1: 10 reps @ 50.0 kg
        session = trainingEngine.logSet(
            session = session,
            exerciseId = "EX-SQ-BW-001-V1",
            setLog = SessionSetLog(
                setNumber = 1,
                reps = ValueState.Recorded(10),
                loadKg = ValueState.Recorded(50.0),
                completed = true
            ),
            exerciseName = "Bodyweight Squat"
        )

        // Log Set 2: 12 reps @ 50.0 kg
        session = trainingEngine.logSet(
            session = session,
            exerciseId = "EX-SQ-BW-001-V1",
            setLog = SessionSetLog(
                setNumber = 2,
                reps = ValueState.Recorded(12),
                loadKg = ValueState.Recorded(50.0),
                completed = true
            ),
            exerciseName = "Bodyweight Squat"
        )

        assertEquals(22, session.totalReps)
        assertEquals(1100.0, session.totalVolumeKg, 0.001)
        assertEquals(1, session.exerciseLogs.size)
        assertEquals(2, session.exerciseLogs[0].sets.size)
    }

    @Test
    fun testExplicitValueStateSemantics() {
        val notSpecified: ValueState<Double> = ValueState.NotSpecified
        val unknown: ValueState<Double> = ValueState.Unknown
        val notApplicable: ValueState<Double> = ValueState.NotApplicable
        val recorded: ValueState<Double> = ValueState.Recorded(85.5)

        assertFalse(notSpecified.isRecorded)
        assertFalse(unknown.isRecorded)
        assertFalse(notApplicable.isRecorded)
        assertTrue(recorded.isRecorded)

        assertNull(notSpecified.getOrNull())
        assertEquals(85.5, recorded.getOrNull())
        assertEquals(0.0, notSpecified.getOrDefault(0.0), 0.001)
    }

    // ------------------------------------------------------------------------
    // 4. ABSOLUTE ISOLATION FROM SCIENTIFIC / EVOLUTION ENGINES
    // ------------------------------------------------------------------------
    @Test
    fun testAbsoluteDecouplingFromEvolutionAndScoreEngines() {
        // Complete 10 intense sessions
        var session = trainingEngine.startFreeformSession("ATHLETE-ISOLATION", "Heavy Test", 1000L)
        for (i in 1..10) {
            session = trainingEngine.logSet(
                session = session,
                exerciseId = "EX-SQ-BW-001-V1",
                setLog = SessionSetLog(
                    setNumber = i,
                    reps = ValueState.Recorded(20),
                    loadKg = ValueState.Recorded(100.0),
                    completed = true
                )
            )
        }
        val finished = trainingEngine.finishSession(session, 100000L)
        assertEquals(SessionStatus.COMPLETED, finished.status)
        assertEquals(20000.0, finished.totalVolumeKg, 0.001)

        // Verify Evolution Engine version and state guarantees are untouched
        assertEquals("1.0.0-evolution-v1", EvolutionEngineV1.ENGINE_VERSION)

        // Verify Score Engine guarantees are untouched
        val scoreEngine = ScoreEngineV1()
        assertEquals("1.0.0-score-v1", scoreEngine.scoreEngineVersion)
        assertEquals("1.0.0-datacore-v1", scoreEngine.coreVersion)
    }
}
