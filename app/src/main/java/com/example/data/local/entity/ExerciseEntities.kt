package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.exerciseengine.model.*
import com.example.core.exerciseengine.prescription.LoadUnit

/**
 * EVOLUTION HUMAN AI — EXERCISE ROOM ENTITIES
 *
 * Persistência local auditada do módulo Exercise Engine V1.
 */

@Entity(tableName = "exercise_definitions")
data class ExerciseEntity(
    @PrimaryKey val exerciseId: String,
    val canonicalName: String,
    val displayName: String,
    val description: String,
    val category: ExerciseCategory,
    val movementPattern: MovementPattern,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val equipment: List<String>,
    val difficulty: ExerciseDifficulty,
    val executionType: ExecutionType,
    val laterality: Laterality,
    val currentVersion: String,
    val status: ExerciseStatus,
    val createdAtTimestamp: Long,
    val publishedAtTimestamp: Long?,
    val checksum: String
)

@Entity(tableName = "exercise_versions", primaryKeys = ["exerciseId", "version"])
data class ExerciseVersionEntity(
    val exerciseId: String,
    val version: String,
    val canonicalName: String,
    val displayName: String,
    val description: String,
    val category: ExerciseCategory,
    val movementPattern: MovementPattern,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val equipment: List<String>,
    val difficulty: ExerciseDifficulty,
    val executionType: ExecutionType,
    val laterality: Laterality,
    val instructionsJson: String,
    val commonErrorsJson: String,
    val progressionIds: List<String>,
    val regressionIds: List<String>,
    val variationIds: List<String>,
    val status: ExerciseStatus,
    val checksum: String,
    val registeredAtTimestamp: Long
)

@Entity(tableName = "exercise_audit_logs")
data class ExerciseAuditLogEntity(
    @PrimaryKey val auditId: String,
    val action: String,
    val targetEntity: String,
    val targetId: String,
    val callerTier: String,
    val callerId: String,
    val success: Boolean,
    val reason: String,
    val securityViolation: Boolean,
    val simulationMode: Boolean,
    val timestamp: Long,
    val checksum: String
)

@Entity(tableName = "exercise_media_references")
data class ExerciseMediaEntity(
    @PrimaryKey val mediaId: String,
    val exerciseId: String,
    val type: MediaType,
    val uri: String?,
    val thumbnailUri: String?,
    val durationMs: Long?,
    val version: String,
    val source: String,
    val status: MediaStatus
)

@Entity(tableName = "exercise_prescriptions")
data class ExercisePrescriptionEntity(
    @PrimaryKey val prescriptionId: String,
    val exerciseId: String,
    val sets: Int?,
    val repetitions: Int?,
    val durationSeconds: Int?,
    val distanceMeters: Double?,
    val load: Double?,
    val loadUnit: LoadUnit,
    val restSeconds: Int?,
    val tempo: String?,
    val targetIntensity: Double?,
    val targetRPE: Double?,
    val targetVelocity: Double?,
    val targetROM: Double?,
    val notes: String?,
    val createdAtTimestamp: Long
)
