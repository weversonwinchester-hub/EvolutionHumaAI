package com.example.core.exerciseengine.model

import java.security.MessageDigest

/**
 * EVOLUTION HUMAN AI — EXERCISE ENGINE V1 MODELS
 *
 * Entidades estruturadas do catálogo oficial de exercícios.
 * Preserva imutabilidade, versionamento e desacoplamento de mídia/biomecânica.
 */

enum class ExerciseCategory {
    STRENGTH,
    HYPERTROPHY,
    CALISTHENICS,
    MOBILITY,
    FLEXIBILITY,
    CARDIORESPIRATORY,
    POWER,
    SPEED,
    AGILITY,
    BALANCE,
    STABILITY,
    REHABILITATION_SUPPORT,
    FUNCTIONAL
}

enum class MovementPattern {
    SQUAT,
    HINGE,
    LUNGE,
    PUSH,
    PULL,
    CARRY,
    ROTATION,
    ANTI_ROTATION,
    JUMP,
    LANDING,
    LOCOMOTION,
    GAIT,
    CRAWL,
    CLIMB,
    PRESS,
    OLYMPIC_LIFT,
    ISOMETRIC,
    OTHER
}

enum class EquipmentType {
    BODYWEIGHT,
    BARBELL,
    DUMBBELL,
    KETTLEBELL,
    CABLE,
    MACHINE,
    RESISTANCE_BAND,
    PULL_UP_BAR,
    BENCH,
    BOX,
    MEDICINE_BALL,
    CARDIO_MACHINE,
    RINGS,
    PARALLETTES,
    OTHER
}

enum class ExerciseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ELITE
}

enum class ExecutionType {
    REPETITION,
    TIME_BASED,
    DISTANCE_BASED,
    ISOMETRIC,
    MAX_ATTEMPT,
    INTERVAL,
    CONTINUOUS
}

enum class Laterality {
    BILATERAL,
    UNILATERAL,
    ALTERNATING,
    NOT_APPLICABLE
}

enum class MovementPhase {
    SETUP,
    ECCENTRIC,
    TRANSITION,
    CONCENTRIC,
    ISOMETRIC,
    TERMINATION
}

enum class ExerciseStatus {
    DRAFT,
    ACTIVE,
    DEPRECATED,
    ARCHIVED,
    PENDING_VALIDATION
}

enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class MediaType {
    ANIMATION_3D,
    VIDEO,
    GIF,
    IMAGE_SEQUENCE,
    ILLUSTRATION,
    MOTION_CAPTURE,
    NONE
}

enum class MediaStatus {
    AVAILABLE,
    PROCESSING,
    UNAVAILABLE,
    NOT_APPLICABLE
}

enum class MuscleGroup {
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    CHEST,
    UPPER_BACK,
    LATS,
    DELTOIDS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    CORE,
    ABDOMINALS,
    OBLIQUES,
    LOWER_BACK,
    HIP_FLEXORS,
    ADDUCTORS,
    FULL_BODY
}

enum class TrainingGoal {
    MAX_STRENGTH,
    HYPERTROPHY,
    EXPLOSIVE_POWER,
    SPEED_ENDURANCE,
    CARDIO_ENDURANCE,
    JOINT_MOBILITY,
    MOTOR_CONTROL,
    POSTURAL_STABILITY,
    BALANCE,
    REHABILITATION
}

data class MovementPhaseDefinition(
    val phase: MovementPhase,
    val description: String,
    val jointFocus: List<String> = emptyList(),
    val durationRatio: Double? = null
)

data class CommonError(
    val errorId: String,
    val description: String,
    val severity: ErrorSeverity,
    val correction: String
)

data class MediaReference(
    val mediaId: String,
    val type: MediaType,
    val uri: String? = null,
    val thumbnailUri: String? = null,
    val durationMs: Long? = null,
    val version: String = "V1",
    val source: String = "OFFICIAL",
    val status: MediaStatus = MediaStatus.AVAILABLE
)

data class BiomechanicalProfile(
    val motionPattern: String? = null,
    val jointTargets: List<String> = emptyList(),
    val expectedPhases: List<MovementPhase> = emptyList(),
    val expectedROM: Double? = null,
    val expectedVelocity: Double? = null,
    val cameraRequirements: List<String> = emptyList(),
    val sensorRequirements: List<String> = emptyList()
)

data class ScientificReferenceRef(
    val scientificMethodologyIds: List<String> = emptyList(),
    val scientificProtocolIds: List<String> = emptyList()
)

data class ExerciseInstructions(
    val setup: List<String> = emptyList(),
    val execution: List<String> = emptyList(),
    val breathing: String? = null,
    val cuePoints: List<String> = emptyList()
)

data class ExerciseDefinition(
    val exerciseId: String,
    val version: String = "V1",
    val canonicalName: String,
    val displayName: String,
    val description: String,
    val category: ExerciseCategory,
    val movementPattern: MovementPattern,
    val primaryMuscles: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: List<EquipmentType>,
    val difficulty: ExerciseDifficulty,
    val trainingGoals: List<TrainingGoal> = emptyList(),
    val executionType: ExecutionType,
    val laterality: Laterality = Laterality.BILATERAL,
    val movementPhases: List<MovementPhaseDefinition> = emptyList(),
    val instructions: ExerciseInstructions = ExerciseInstructions(),
    val commonErrors: List<CommonError> = emptyList(),
    val safetyNotes: List<String> = emptyList(),
    val progressionIds: List<String> = emptyList(),
    val regressionIds: List<String> = emptyList(),
    val variationIds: List<String> = emptyList(),
    val mediaReferences: List<MediaReference> = emptyList(),
    val scientificReferences: ScientificReferenceRef = ScientificReferenceRef(),
    val biomechanicalProfile: BiomechanicalProfile = BiomechanicalProfile(),
    val status: ExerciseStatus = ExerciseStatus.ACTIVE,
    val checksum: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val publishedAtTimestamp: Long? = null
) {
    fun calculateChecksum(): String {
        val payload = "$exerciseId|$version|$canonicalName|$category|$movementPattern|$difficulty|$executionType|$status"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
