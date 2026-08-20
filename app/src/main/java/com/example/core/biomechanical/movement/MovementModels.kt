package com.example.core.biomechanical.movement

import com.example.core.biomechanical.kinematics.BodySide
import com.example.core.biomechanical.kinematics.JointType
import java.util.UUID

/**
 * PERFORMAI MOVEMENT PHASES, REPETITIONS AND PATTERNS
 */

enum class MovementPhaseType {
    PREPARATION,
    ECCENTRIC,
    TRANSITION,
    CONCENTRIC,
    DECELERATION,
    STABILIZATION,
    UNKNOWN
}

enum class RepetitionQualityStatus {
    ACCEPTED,
    INCOMPLETE,
    DEGRADED,
    REJECTED
}

data class MovementPhase(
    val phaseType: MovementPhaseType,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val confidence: Double = 1.0,
    val startAngle: Double? = null,
    val endAngle: Double? = null,
    val peakVelocity: Double? = null
)

data class RangeOfMotion(
    val joint: JointType,
    val side: BodySide,
    val minimumAngle: Double,
    val maximumAngle: Double,
    val totalROM: Double,
    val plane: String = "SAGITTAL_2D",
    val confidence: Double = 1.0,
    val uncertainty: Double = 2.0
)

data class MovementRepetition(
    val repetitionId: String = UUID.randomUUID().toString(),
    val sequenceIndex: Int,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val phaseSequence: List<MovementPhase>,
    val rangeOfMotion: Double,
    val peakVelocity: Double,
    val peakAcceleration: Double,
    val qualityStatus: RepetitionQualityStatus = RepetitionQualityStatus.ACCEPTED
)

data class MovementPattern(
    val patternId: String = UUID.randomUUID().toString(),
    val movementType: String,
    val repetitions: List<MovementRepetition>,
    val temporalCharacteristics: Map<String, Double> = emptyMap(),
    val spatialCharacteristics: Map<String, Double> = emptyMap(),
    val stabilityIndicators: Map<String, Double> = emptyMap(),
    val symmetryIndicators: Map<String, Double> = emptyMap(),
    val quality: String = "DESCRIPTIVE_ANALYSIS_COMPLETED",
    val confidence: Double = 1.0
)

data class FatigueObservation(
    val fatigueId: String = UUID.randomUUID().toString(),
    val observedChanges: List<String>,
    val velocityLossPercent: Double? = null,
    val romLossPercent: Double? = null,
    val symmetryDriftPercent: Double? = null,
    val observationType: String = "OBSERVED_CHANGE",
    val physiologicalFatigueConfirmed: Boolean = false // SEMPRE FALSE (requere metodologia e biomarker fisiológico validado)
)
