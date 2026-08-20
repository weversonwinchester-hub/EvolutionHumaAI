package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.exerciseengine.model.ExerciseCategory
import com.example.core.exerciseengine.model.ExerciseDifficulty
import com.example.core.trainingengine.baseline.AthleteGoalType
import com.example.core.trainingengine.baseline.BaselineVerificationStatus
import com.example.core.trainingengine.baseline.ExperienceLevel
import com.example.core.trainingengine.model.*
import com.example.core.sync.SyncEntityType

/**
 * EVOLUTION HUMAN AI — TRAINING DOMAIN ROOM ENTITIES
 */

@Entity(
    tableName = "workouts",
    indices = [Index(value = ["isTemplate"])]
)
data class WorkoutEntity(
    @PrimaryKey
    val workoutId: String,
    val name: String,
    val description: String,
    val category: ExerciseCategory,
    val difficulty: ExerciseDifficulty,
    val targetMusclesJson: String,
    val estimatedDurationMinutes: Int,
    val isTemplate: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val version: String
)

@Entity(
    tableName = "workout_items",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["workoutId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workoutId"])]
)
data class WorkoutItemEntity(
    @PrimaryKey
    val itemId: String,
    val workoutId: String,
    val exerciseId: String,
    val exerciseName: String,
    val order: Int,
    val targetSets: Int,
    val prescriptionJson: String,
    val restBetweenSetsSeconds: Int,
    val notes: String
)

@Entity(
    tableName = "training_sessions",
    indices = [Index(value = ["userId"]), Index(value = ["startedAt"])]
)
data class TrainingSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val userId: String,
    val workoutId: String?,
    val sessionName: String,
    val status: SessionStatus,
    val startedAt: Long,
    val endedAt: Long?,
    val totalDurationSeconds: Int,
    val activeDurationSeconds: Int,
    val pausedDurationSeconds: Int,
    val perceivedExertionValue: Double?,
    val notes: String,
    val completionRate: Float,
    val totalVolumeKg: Double,
    val totalReps: Int,
    val evidencePackageId: String?,
    val syncStatus: SyncStatus,
    val version: String
)

@Entity(
    tableName = "session_exercise_logs",
    foreignKeys = [
        ForeignKey(
            entity = TrainingSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SessionExerciseLogEntity(
    @PrimaryKey
    val logId: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val order: Int,
    val setsJson: String,
    val startedAt: Long,
    val endedAt: Long?,
    val totalRestSeconds: Int,
    val status: ExerciseExecutionStatus,
    val notes: String
)

@Entity(
    tableName = "athlete_baselines",
    indices = [Index(value = ["userId"]), Index(value = ["version"])]
)
data class AthleteBaselineEntity(
    @PrimaryKey
    val baselineId: String,
    val userId: String,
    val version: Int,
    val experienceLevel: ExperienceLevel,
    val modalitiesJson: String,
    val availableDaysPerWeek: Int,
    val sessionDurationLimitMinutes: Int,
    val equipmentJson: String,
    val benchmarkResultsJson: String,
    val verificationStatus: BaselineVerificationStatus,
    val createdAt: Long,
    val auditHash: String
)

@Entity(
    tableName = "athlete_goals",
    indices = [Index(value = ["userId"])]
)
data class AthleteGoalEntity(
    @PrimaryKey
    val goalId: String,
    val userId: String,
    val goalType: AthleteGoalType,
    val title: String,
    val description: String,
    val targetMetric: String?,
    val targetValue: Double?,
    val currentValue: Double?,
    val targetDateEpoch: Long?,
    val isAchieved: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "training_sync_queue",
    indices = [Index(value = ["syncStatus"])]
)
data class TrainingSyncQueueEntity(
    @PrimaryKey
    val queueId: String,
    val entityType: SyncEntityType,
    val entityId: String,
    val payloadJson: String,
    val attempts: Int,
    val lastAttemptAt: Long?,
    val syncStatus: SyncStatus,
    val errorReason: String?,
    val createdAt: Long
)
