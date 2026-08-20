package com.example.core.trainingengine.taxonomy

import com.example.core.exerciseengine.model.EquipmentType
import com.example.core.trainingengine.model.ValueState

/**
 * EVOLUTION HUMAN AI — TRAINING TAXONOMY & PERFORMANCE DOMAINS V1
 */

enum class TrainingModality {
    STRENGTH,
    CALISTHENICS,
    MOBILITY,
    CARDIORESPIRATORY,
    POWER,
    PLYOMETRIC,
    SKILL,
    RECOVERY,
    SPORT_SPECIFIC
}

enum class PerformanceDomain {
    STRENGTH,
    SPEED,
    ENDURANCE,
    MOBILITY,
    POWER,
    CONTROL,
    COORDINATION
}

enum class DemandLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

/**
 * Non-clinical educational safety & requirements profile for an exercise.
 */
data class ExerciseEducationalRequirements(
    val exerciseId: String,
    val technicalDifficulty: DemandLevel = DemandLevel.MODERATE,
    val physicalDemand: DemandLevel = DemandLevel.MODERATE,
    val coordinationDemand: DemandLevel = DemandLevel.MODERATE,
    val mobilityRequirement: DemandLevel = DemandLevel.MODERATE,
    val stabilityRequirement: DemandLevel = DemandLevel.MODERATE,
    val requiredEquipment: List<EquipmentType> = listOf(EquipmentType.BODYWEIGHT),
    val optionalEquipment: List<EquipmentType> = emptyList(),
    val equipmentAlternatives: Map<EquipmentType, List<EquipmentType>> = emptyMap(),
    val prerequisiteExercises: List<String> = emptyList(),
    val progressionExercises: List<String> = emptyList(),
    val regressionExercises: List<String> = emptyList(),
    val commonExecutionErrors: List<String> = emptyList(),
    val executionCautions: List<String> = emptyList(),
    val safetyNotes: List<String> = emptyList()
)

/**
 * Cardiorespiratory structured parameters.
 * Heart rate is explicitly UNKNOWN / NOT_RECORDED if no biometric sensor is paired.
 */
data class CardiorespiratoryParameters(
    val durationSeconds: Int,
    val distanceMeters: ValueState<Double> = ValueState.NotSpecified,
    val speedKmh: ValueState<Double> = ValueState.NotSpecified,
    val paceMinPerKm: ValueState<Double> = ValueState.NotSpecified,
    val intervalsCount: ValueState<Int> = ValueState.NotSpecified,
    val heartRateBpm: ValueState<Int> = ValueState.Unknown,
    val perceivedExertionRpe: ValueState<Double> = ValueState.NotSpecified
)

/**
 * Mobility & range of motion structured parameters.
 */
data class MobilityParameters(
    val rangeOfMotionDegrees: ValueState<Double> = ValueState.NotSpecified,
    val holdDurationSeconds: ValueState<Int> = ValueState.NotSpecified,
    val isBilateral: Boolean = true,
    val targetSide: String = "BILATERAL",
    val repetitionCount: Int = 1,
    val movementControlScore: ValueState<Double> = ValueState.NotSpecified
)
