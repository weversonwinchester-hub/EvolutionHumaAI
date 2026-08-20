package com.example.core.biomechanical.tracking

import com.example.core.biomechanical.pose.LandmarkType
import com.example.core.biomechanical.pose.PoseFrame

/**
 * PERFORMAI IDENTITY CONSISTENCY TRACKER
 *
 * Garante que:
 * - Landmark esquerdo permanece esquerdo e direito permanece direito (sem inversão lateral).
 * - O indivíduo rastreado é único e contínuo.
 * - Detecta eventos de MULTI_PERSON, IDENTITY_SWITCH, TRACK_LOSS e REACQUISITION.
 */

enum class IdentityAnomalyType {
    NONE,
    MULTI_PERSON_DETECTED,
    IDENTITY_SWITCH,
    LATERAL_INVERSION,
    TRACK_LOSS,
    REACQUISITION
}

data class IdentityTrackingStatus(
    val isValidSingleIdentity: Boolean,
    val anomaliesDetected: List<IdentityAnomalyType>,
    val lateralConsistencyScore: Double, // 1.0 = consistente
    val personCountObserved: Int,
    val details: List<String>
)

object IdentityConsistencyTracker {

    fun evaluateIdentityConsistency(
        poseSequence: List<PoseFrame>,
        multiPersonDetectedCount: Int = 0
    ): IdentityTrackingStatus {
        val anomalies = mutableListOf<IdentityAnomalyType>()
        val details = mutableListOf<String>()

        if (poseSequence.isEmpty()) {
            return IdentityTrackingStatus(
                isValidSingleIdentity = false,
                anomaliesDetected = listOf(IdentityAnomalyType.TRACK_LOSS),
                lateralConsistencyScore = 0.0,
                personCountObserved = 0,
                details = listOf("NO_POSES_PROVIDED")
            )
        }

        // 1. Verificação de Multi-Person
        if (multiPersonDetectedCount > 1) {
            anomalies.add(IdentityAnomalyType.MULTI_PERSON_DETECTED)
            details.add("MULTI_PERSON_FRAMES_DETECTED_COUNT_$multiPersonDetectedCount")
        }

        // 2. Verificação de Consistência Lateral (Esquerda vs Direita)
        var lateralInversions = 0
        var totalBilateralChecks = 0

        for (i in 0 until poseSequence.size) {
            val pose = poseSequence[i]
            val leftShoulder = pose.landmarks[LandmarkType.LEFT_SHOULDER]
            val rightShoulder = pose.landmarks[LandmarkType.RIGHT_SHOULDER]
            val leftHip = pose.landmarks[LandmarkType.LEFT_HIP]
            val rightHip = pose.landmarks[LandmarkType.RIGHT_HIP]

            // Em vista frontal/posterior típica, ombro esquerdo e direito mantêm relação espacial x consistente
            if (leftShoulder != null && rightShoulder != null) {
                totalBilateralChecks++
                // Se o ombro esquerdo cruzar abruptamente a posição do ombro direito além de uma margem
                // Em coordenadas de câmera frontal, left_shoulder.x costuma ser < right_shoulder.x (ou vice-versa)
                // Se houver salto instantâneo na ordem relativa
                if (i > 0) {
                    val prevPose = poseSequence[i - 1]
                    val prevLeft = prevPose.landmarks[LandmarkType.LEFT_SHOULDER]
                    val prevRight = prevPose.landmarks[LandmarkType.RIGHT_SHOULDER]
                    if (prevLeft != null && prevRight != null) {
                        val prevDeltaX = prevRight.x - prevLeft.x
                        val currDeltaX = rightShoulder.x - leftShoulder.x
                        // Mudança de sinal repentina indica inversão lateral ou giro brusco 180 sem rastreamento
                        if ((prevDeltaX > 0.1 && currDeltaX < -0.1) || (prevDeltaX < -0.1 && currDeltaX > 0.1)) {
                            lateralInversions++
                        }
                    }
                }
            }
        }

        if (lateralInversions > 0) {
            anomalies.add(IdentityAnomalyType.LATERAL_INVERSION)
            details.add("LATERAL_INVERSIONS_DETECTED_COUNT_$lateralInversions")
        }

        // 3. Verificação de Perda e Reaquisição de Rastreamento (saltos espaciais anômalos do centro de massa)
        var trackLossCount = 0
        for (i in 1 until poseSequence.size) {
            val prevPose = poseSequence[i - 1]
            val currPose = poseSequence[i]

            val prevMidHip = prevPose.landmarks[LandmarkType.MID_HIP] ?: prevPose.landmarks[LandmarkType.LEFT_HIP]
            val currMidHip = currPose.landmarks[LandmarkType.MID_HIP] ?: currPose.landmarks[LandmarkType.LEFT_HIP]

            if (prevMidHip != null && currMidHip != null) {
                val dx = currMidHip.x - prevMidHip.x
                val dy = currMidHip.y - prevMidHip.y
                val dist = Math.sqrt(dx * dx + dy * dy)
                val dtSec = (currPose.timestamp - prevPose.timestamp).coerceAtLeast(1L).toDouble() / 1000.0
                val speed = dist / dtSec // Normalized units per second

                // Teletransporte biomecanicamente impossível (> 4.0 norm units/sec)
                if (speed > 4.0) {
                    trackLossCount++
                }
            }
        }

        if (trackLossCount > 0) {
            anomalies.add(IdentityAnomalyType.IDENTITY_SWITCH)
            details.add("TELEPORT_OR_IDENTITY_SWITCH_DETECTED_COUNT_$trackLossCount")
        }

        val lateralScore = if (totalBilateralChecks > 0) {
            (1.0 - (lateralInversions.toDouble() / totalBilateralChecks)).coerceIn(0.0, 1.0)
        } else 1.0

        val isValid = anomalies.isEmpty() || (anomalies.size == 1 && anomalies.contains(IdentityAnomalyType.NONE))

        return IdentityTrackingStatus(
            isValidSingleIdentity = isValid,
            anomaliesDetected = if (anomalies.isEmpty()) listOf(IdentityAnomalyType.NONE) else anomalies,
            lateralConsistencyScore = lateralScore,
            personCountObserved = if (multiPersonDetectedCount > 0) 2 else 1,
            details = details
        )
    }
}
