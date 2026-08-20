package com.example.core.biomechanical.movement

import com.example.core.biomechanical.kinematics.AngularVelocityMeasurement
import com.example.core.biomechanical.kinematics.BodySide
import com.example.core.biomechanical.kinematics.JointAngleMeasurement
import com.example.core.biomechanical.kinematics.JointType

/**
 * PERFORMAI MOVEMENT PHASE AND REPETITION DETECTOR
 *
 * Identifica fases mecânicas (excêntrica, concêntrica, transição, etc.) e agrupa repetições.
 * Quando dados são ambíguos ou insuficientes, fases permanecem estritamente UNKNOWN.
 */
object MovementPhaseDetector {

    fun detectPhases(
        angleMeasurements: List<JointAngleMeasurement>,
        velocityMeasurements: List<AngularVelocityMeasurement>
    ): List<MovementPhase> {
        val phases = mutableListOf<MovementPhase>()
        if (angleMeasurements.size < 3 || velocityMeasurements.isEmpty()) {
            if (angleMeasurements.isNotEmpty()) {
                phases.add(
                    MovementPhase(
                        phaseType = MovementPhaseType.UNKNOWN,
                        startTimestamp = angleMeasurements.first().timestamp,
                        endTimestamp = angleMeasurements.last().timestamp,
                        confidence = 0.30
                    )
                )
            }
            return phases
        }

        val sortedAngles = angleMeasurements.sortedBy { it.timestamp }
        val sortedVelocities = velocityMeasurements.sortedBy { it.timestamp }

        // Identifica transições baseadas no sinal da velocidade angular
        var currentPhaseType = MovementPhaseType.UNKNOWN
        var phaseStartIdx = 0

        for (i in 0 until sortedVelocities.size) {
            val v = sortedVelocities[i]
            val detectedType = when {
                Math.abs(v.value) < 10.0 -> MovementPhaseType.TRANSITION
                v.value < -10.0 -> MovementPhaseType.ECCENTRIC // Flexão / descida
                v.value > 10.0 -> MovementPhaseType.CONCENTRIC  // Extensão / subida
                else -> MovementPhaseType.UNKNOWN
            }

            if (i == 0) {
                currentPhaseType = detectedType
                phaseStartIdx = 0
            } else if (detectedType != currentPhaseType && (i - phaseStartIdx) >= 2) {
                // Fechar fase anterior
                val startTs = sortedVelocities[phaseStartIdx].timestamp
                val endTs = sortedVelocities[i].timestamp
                val subAngles = sortedAngles.filter { it.timestamp in startTs..endTs }
                val subVels = sortedVelocities.subList(phaseStartIdx, i)

                phases.add(
                    MovementPhase(
                        phaseType = currentPhaseType,
                        startTimestamp = startTs,
                        endTimestamp = endTs,
                        confidence = 0.85,
                        startAngle = subAngles.firstOrNull()?.angle,
                        endAngle = subAngles.lastOrNull()?.angle,
                        peakVelocity = subVels.maxOfOrNull { Math.abs(it.value) }
                    )
                )

                currentPhaseType = detectedType
                phaseStartIdx = i
            }
        }

        // Adiciona última fase
        if (phaseStartIdx < sortedVelocities.size) {
            val startTs = sortedVelocities[phaseStartIdx].timestamp
            val endTs = sortedVelocities.last().timestamp
            val subAngles = sortedAngles.filter { it.timestamp in startTs..endTs }
            val subVels = sortedVelocities.subList(phaseStartIdx, sortedVelocities.size)

            phases.add(
                MovementPhase(
                    phaseType = currentPhaseType,
                    startTimestamp = startTs,
                    endTimestamp = endTs,
                    confidence = 0.85,
                    startAngle = subAngles.firstOrNull()?.angle,
                    endAngle = subAngles.lastOrNull()?.angle,
                    peakVelocity = subVels.maxOfOrNull { Math.abs(it.value) }
                )
            )
        }

        return phases
    }
}

object RepetitionDetector {

    fun detectRepetitions(
        phases: List<MovementPhase>,
        minRomDegrees: Double = 30.0
    ): List<MovementRepetition> {
        val repetitions = mutableListOf<MovementRepetition>()
        if (phases.size < 2) return repetitions

        var currentRepPhases = mutableListOf<MovementPhase>()
        var repIndex = 1

        for (i in 0 until phases.size) {
            val phase = phases[i]
            currentRepPhases.add(phase)

            val hasEccentric = currentRepPhases.any { it.phaseType == MovementPhaseType.ECCENTRIC }
            val hasConcentric = currentRepPhases.any { it.phaseType == MovementPhaseType.CONCENTRIC }

            if (hasEccentric && hasConcentric && (phase.phaseType == MovementPhaseType.TRANSITION || phase.phaseType == MovementPhaseType.STABILIZATION || i == phases.size - 1)) {
                val startTs = currentRepPhases.first().startTimestamp
                val endTs = currentRepPhases.last().endTimestamp

                val minAng = currentRepPhases.mapNotNull { it.startAngle ?: it.endAngle }.minOrNull() ?: 0.0
                val maxAng = currentRepPhases.mapNotNull { it.startAngle ?: it.endAngle }.maxOrNull() ?: 0.0
                val rom = maxAng - minAng

                val peakVel = currentRepPhases.mapNotNull { it.peakVelocity }.maxOrNull() ?: 0.0

                val quality = if (rom >= minRomDegrees) RepetitionQualityStatus.ACCEPTED else RepetitionQualityStatus.INCOMPLETE

                repetitions.add(
                    MovementRepetition(
                        sequenceIndex = repIndex++,
                        startTimestamp = startTs,
                        endTimestamp = endTs,
                        phaseSequence = currentRepPhases.toList(),
                        rangeOfMotion = rom,
                        peakVelocity = peakVel,
                        peakAcceleration = 0.0,
                        qualityStatus = quality
                    )
                )
                currentRepPhases = mutableListOf()
            }
        }

        return repetitions
    }
}

object MovementPatternAnalyzer {

    fun calculateRangeOfMotion(
        angleMeasurements: List<JointAngleMeasurement>,
        targetJoint: JointType = JointType.LEFT_KNEE,
        targetSide: BodySide = BodySide.LEFT
    ): RangeOfMotion {
        val targetList = angleMeasurements.filter { it.joint == targetJoint && it.side == targetSide }
        if (targetList.isEmpty()) {
            return RangeOfMotion(
                joint = targetJoint,
                side = targetSide,
                minimumAngle = 0.0,
                maximumAngle = 0.0,
                totalROM = 0.0,
                confidence = 0.0,
                uncertainty = 10.0
            )
        }

        val minAngle = targetList.minOf { it.angle }
        val maxAngle = targetList.maxOf { it.angle }
        val rom = maxAngle - minAngle
        val avgConfidence = targetList.map { it.confidence }.average()
        val avgUncertainty = targetList.map { it.uncertainty }.average()

        return RangeOfMotion(
            joint = targetJoint,
            side = targetSide,
            minimumAngle = minAngle,
            maximumAngle = maxAngle,
            totalROM = rom,
            confidence = avgConfidence,
            uncertainty = avgUncertainty
        )
    }

    fun observeFatigue(
        repetitions: List<MovementRepetition>
    ): FatigueObservation {
        val observed = mutableListOf<String>()
        if (repetitions.size < 3) {
            return FatigueObservation(
                observedChanges = listOf("INSUFFICIENT_REPETITIONS_FOR_FATIGUE_OBSERVATION"),
                physiologicalFatigueConfirmed = false
            )
        }

        val validReps = repetitions.filter { it.qualityStatus == RepetitionQualityStatus.ACCEPTED }
        if (validReps.size < 2) {
            return FatigueObservation(
                observedChanges = listOf("INSUFFICIENT_ACCEPTED_REPETITIONS"),
                physiologicalFatigueConfirmed = false
            )
        }

        val firstRep = validReps.first()
        val lastRep = validReps.last()

        val velocityLoss = if (firstRep.peakVelocity > 0.0) {
            ((firstRep.peakVelocity - lastRep.peakVelocity) / firstRep.peakVelocity) * 100.0
        } else null

        val romLoss = if (firstRep.rangeOfMotion > 0.0) {
            ((firstRep.rangeOfMotion - lastRep.rangeOfMotion) / firstRep.rangeOfMotion) * 100.0
        } else null

        velocityLoss?.let {
            if (it > 10.0) observed.add("VELOCITY_LOSS_OBSERVED_${String.format("%.1f", it)}%")
        }

        romLoss?.let {
            if (it > 10.0) observed.add("ROM_REDUCTION_OBSERVED_${String.format("%.1f", it)}%")
        }

        if (observed.isEmpty()) {
            observed.add("NO_SIGNIFICANT_KINEMATIC_DRIFT_OBSERVED")
        }

        return FatigueObservation(
            observedChanges = observed,
            velocityLossPercent = velocityLoss,
            romLossPercent = romLoss,
            symmetryDriftPercent = null,
            observationType = "OBSERVED_CHANGE",
            physiologicalFatigueConfirmed = false
        )
    }
}
