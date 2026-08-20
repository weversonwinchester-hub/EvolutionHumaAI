package com.example.core.trainingengine.model

import com.example.core.exerciseengine.model.ExerciseCategory
import com.example.core.exerciseengine.model.ExerciseDifficulty
import com.example.core.exerciseengine.model.MovementPattern
import java.util.UUID

/**
 * EVOLUTION HUMAN AI — TRAINING DOMAIN MODELS V1
 *
 * Formal separation of:
 * - Exercise (Movement definition)
 * - Workout (Planned session structure)
 * - Training Session (Real-world execution log)
 * - Training Program (Longitudinal periodization plan)
 * - Athlete Evolution (Scientific progression state - read only)
 */

enum class ExecutionPrescriptionType {
    REPETITIONS,
    DURATION,
    DISTANCE,
    LOAD,
    INTERVAL,
    ISOMETRIC_HOLD
}

enum class SetType {
    WARMUP,
    WORKING,
    DROPSET,
    MYOREP,
    COOLDOWN,
    FAILURE_TEST,
    REST_PAUSE
}

enum class SessionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    ABANDONED
}

enum class ExerciseExecutionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}

enum class SyncStatus {
    LOCAL_ONLY,
    PENDING_SYNC,
    SYNCED,
    CONFLICT
}

/**
 * Prescribed targets for a workout item.
 */
data class TrainingPrescription(
    val executionType: ExecutionPrescriptionType = ExecutionPrescriptionType.REPETITIONS,
    val targetReps: ValueState<Int> = ValueState.NotSpecified,
    val targetLoadKg: ValueState<Double> = ValueState.NotSpecified,
    val targetDurationSeconds: ValueState<Int> = ValueState.NotSpecified,
    val targetDistanceMeters: ValueState<Double> = ValueState.NotSpecified,
    val targetRpe: ValueState<Double> = ValueState.NotSpecified,
    val tempo: String? = null
)

/**
 * An exercise item inside a workout template/plan.
 */
data class WorkoutItem(
    val itemId: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val exerciseName: String,
    val order: Int,
    val targetSets: Int = 3,
    val prescription: TrainingPrescription = TrainingPrescription(),
    val restBetweenSetsSeconds: Int = 90,
    val exerciseAlternatives: List<String> = emptyList(),
    val notes: String = ""
)

/**
 * Planned workout definition.
 */
data class Workout(
    val workoutId: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val category: ExerciseCategory = ExerciseCategory.STRENGTH,
    val difficulty: ExerciseDifficulty = ExerciseDifficulty.INTERMEDIATE,
    val targetMuscles: List<String> = emptyList(),
    val items: List<WorkoutItem> = emptyList(),
    val estimatedDurationMinutes: Int = 45,
    val isTemplate: Boolean = true,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: String = "1.0.0"
)

/**
 * Log of a single executed set within a session.
 */
data class SessionSetLog(
    val setNumber: Int,
    val setType: SetType = SetType.WORKING,
    val reps: ValueState<Int> = ValueState.NotSpecified,
    val loadKg: ValueState<Double> = ValueState.NotSpecified,
    val durationSeconds: ValueState<Int> = ValueState.NotSpecified,
    val distanceMeters: ValueState<Double> = ValueState.NotSpecified,
    val rpe: ValueState<Double> = ValueState.NotSpecified,
    val restTakenSeconds: ValueState<Int> = ValueState.NotSpecified,
    val completed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val formQualityScore: ValueState<Double> = ValueState.NotSpecified
)

/**
 * Log of an exercise executed within a session.
 */
data class SessionExerciseLog(
    val logId: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val exerciseName: String,
    val order: Int,
    val sets: List<SessionSetLog> = emptyList(),
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val totalRestSeconds: Int = 0,
    val status: ExerciseExecutionStatus = ExerciseExecutionStatus.PENDING,
    val notes: String = ""
)

/**
 * Real-time or historical execution record of a training session.
 */
data class TrainingSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val userId: String,
    val workoutId: String? = null,
    val sessionName: String,
    val status: SessionStatus = SessionStatus.NOT_STARTED,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val totalDurationSeconds: Int = 0,
    val activeDurationSeconds: Int = 0,
    val pausedDurationSeconds: Int = 0,
    val exerciseLogs: List<SessionExerciseLog> = emptyList(),
    val perceivedExertion: ValueState<Double> = ValueState.NotSpecified,
    val notes: String = "",
    val completionRate: Float = 0.0f,
    val totalVolumeKg: Double = 0.0,
    val totalReps: Int = 0,
    val evidencePackageId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val version: String = "1.0.0"
)

/**
 * Longitudinal Training Program.
 */
data class ProgramSessionSlot(
    val dayOfWeek: Int, // 1 to 7
    val workoutId: String,
    val workoutName: String,
    val targetVolumeModifier: Float = 1.0f
)

data class ProgramWeek(
    val weekNumber: Int,
    val theme: String = "",
    val sessionSlots: List<ProgramSessionSlot> = emptyList()
)

data class TrainingProgram(
    val programId: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val difficulty: ExerciseDifficulty = ExerciseDifficulty.INTERMEDIATE,
    val durationWeeks: Int = 4,
    val weeks: List<ProgramWeek> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val version: String = "1.0.0"
)
