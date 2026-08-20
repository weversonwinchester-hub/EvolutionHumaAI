package com.example.core.biomechanical.tracking

import com.example.core.biomechanical.pose.Landmark
import com.example.core.biomechanical.pose.LandmarkType
import com.example.core.biomechanical.pose.PoseFrame

/**
 * PERFORMAI LANDMARK TRACKER & TEMPORAL FILTER
 *
 * Aplica suavização temporal explícita e rastreamento de trajetórias.
 * Nunca aplica filtros de forma oculta; todo processamento é parametrizado e registrado.
 */
data class TemporalFilterConfig(
    val filterType: String = "EXPONENTIAL_MOVING_AVERAGE",
    val alpha: Double = 0.70, // Peso do novo dado [0.0, 1.0]
    val maxInterpolationGapMs: Long = 100L,
    val version: String = "EMA-V1.0"
)

object LandmarkTracker {

    fun trackAndSmooth(
        poseSequence: List<PoseFrame>,
        config: TemporalFilterConfig = TemporalFilterConfig()
    ): List<PoseFrame> {
        if (poseSequence.size <= 1) return poseSequence

        val smoothedSequence = mutableListOf<PoseFrame>()
        val lastSmoothedLandmarks = mutableMapOf<LandmarkType, Landmark>()

        poseSequence.forEach { pose ->
            val smoothedMap = mutableMapOf<LandmarkType, Landmark>()

            pose.landmarks.forEach { (type, current) ->
                val prev = lastSmoothedLandmarks[type]
                if (prev == null || current.confidence < 0.20) {
                    // Sem dado prévio ou baixa confiança -> usa atual
                    smoothedMap[type] = current
                    lastSmoothedLandmarks[type] = current
                } else {
                    // Aplica Exponential Moving Average
                    val smoothedX = config.alpha * current.x + (1.0 - config.alpha) * prev.x
                    val smoothedY = config.alpha * current.y + (1.0 - config.alpha) * prev.y
                    val smoothedZ = if (current.z != null && prev.z != null) {
                        config.alpha * current.z + (1.0 - config.alpha) * prev.z
                    } else current.z

                    val smoothedLandmark = current.copy(
                        x = smoothedX,
                        y = smoothedY,
                        z = smoothedZ
                    )
                    smoothedMap[type] = smoothedLandmark
                    lastSmoothedLandmarks[type] = smoothedLandmark
                }
            }

            val smoothedPose = PoseFrame.createWithHash(
                frameId = pose.frameId,
                sessionId = pose.sessionId,
                timestamp = pose.timestamp,
                landmarks = smoothedMap,
                poseConfidence = pose.poseConfidence,
                bodyVisibility = pose.bodyVisibility,
                detectedBodyParts = pose.detectedBodyParts,
                occludedBodyParts = pose.occludedBodyParts,
                coordinateSystem = pose.coordinateSystem,
                estimatorVersion = "${pose.estimatorVersion}+${config.version}"
            )
            smoothedSequence.add(smoothedPose)
        }

        return smoothedSequence
    }
}
