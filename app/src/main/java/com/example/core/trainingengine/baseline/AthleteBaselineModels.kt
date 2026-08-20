package com.example.core.trainingengine.baseline

import com.example.core.exerciseengine.model.EquipmentType
import com.example.core.trainingengine.taxonomy.TrainingModality
import java.util.UUID

/**
 * EVOLUTION HUMAN AI — ATHLETE BASELINE & GOAL SYSTEM V1
 */

enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    COMPETITIVE
}

enum class AthleteGoalType {
    STRENGTH,
    MUSCLE_DEVELOPMENT,
    ENDURANCE,
    MOBILITY,
    PERFORMANCE,
    CONSISTENCY,
    SKILL,
    GENERAL_FITNESS
}

enum class BaselineVerificationStatus {
    SELF_REPORTED,
    VERIFIED_WITH_EVIDENCE,
    HISTORICAL_CONSISTENCY,
    PENDING_ASSESSMENT
}

/**
 * Goal definition. Kept strictly isolated from scientific score and evolution.
 */
data class AthleteGoal(
    val goalId: String = UUID.randomUUID().toString(),
    val userId: String,
    val goalType: AthleteGoalType,
    val title: String,
    val description: String = "",
    val targetMetric: String? = null,
    val targetValue: Double? = null,
    val currentValue: Double? = null,
    val targetDateEpoch: Long? = null,
    val isAchieved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Versioned, immutable snapshot of athlete starting baseline.
 */
data class AthleteBaseline(
    val baselineId: String = UUID.randomUUID().toString(),
    val userId: String,
    val version: Int = 1,
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val primaryModalities: List<TrainingModality> = listOf(TrainingModality.STRENGTH),
    val availableDaysPerWeek: Int = 3,
    val sessionDurationLimitMinutes: Int = 60,
    val availableEquipment: List<EquipmentType> = listOf(EquipmentType.BODYWEIGHT),
    val trainingPreferences: List<String> = emptyList(),
    val benchmarkResults: Map<String, Double> = emptyMap(),
    val verificationStatus: BaselineVerificationStatus = BaselineVerificationStatus.SELF_REPORTED,
    val createdAt: Long = System.currentTimeMillis(),
    val auditHash: String = ""
)

/**
 * Engine for managing athlete baseline snapshots and goals.
 */
class AthleteBaselineEngineV1 {

    /**
     * Creates a new baseline version, preserving previous baselines for audit.
     */
    fun createBaseline(
        userId: String,
        experienceLevel: ExperienceLevel,
        primaryModalities: List<TrainingModality>,
        availableDaysPerWeek: Int,
        availableEquipment: List<EquipmentType>,
        currentVersion: Int = 1,
        benchmarkResults: Map<String, Double> = emptyMap()
    ): AthleteBaseline {
        require(userId.isNotBlank()) { "userId não pode ser vazio." }
        require(availableDaysPerWeek in 1..7) { "Disponibilidade semanal deve estar entre 1 e 7 dias." }

        return AthleteBaseline(
            baselineId = UUID.randomUUID().toString(),
            userId = userId,
            version = currentVersion + 1,
            experienceLevel = experienceLevel,
            primaryModalities = primaryModalities,
            availableDaysPerWeek = availableDaysPerWeek,
            availableEquipment = availableEquipment,
            benchmarkResults = benchmarkResults,
            verificationStatus = BaselineVerificationStatus.SELF_REPORTED,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Creates a structured goal for the athlete.
     */
    fun createGoal(
        userId: String,
        goalType: AthleteGoalType,
        title: String,
        description: String = "",
        targetMetric: String? = null,
        targetValue: Double? = null,
        targetDateEpoch: Long? = null
    ): AthleteGoal {
        require(title.isNotBlank()) { "Título da meta não pode ser vazio." }

        return AthleteGoal(
            goalId = UUID.randomUUID().toString(),
            userId = userId,
            goalType = goalType,
            title = title.trim(),
            description = description.trim(),
            targetMetric = targetMetric,
            targetValue = targetValue,
            currentValue = 0.0,
            targetDateEpoch = targetDateEpoch,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
