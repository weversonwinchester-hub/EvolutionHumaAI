package com.example.core.biomechanical.tracking

import com.example.core.biomechanical.pose.LandmarkType
import com.example.core.biomechanical.pose.PoseFrame

/**
 * PERFORMAI OCCLUSION DETECTOR
 *
 * Detecta perda temporária ou contínua de landmarks anatômicos e quantifica a severidade.
 */
enum class OcclusionSeverity {
    NONE,
    MINOR,        // Perda pontual (< 100ms / 3 frames) em pontos não críticos
    MODERATE,     // Perda moderada (100 - 300ms) com impacto parcial
    SEVERE,       // Perda severa (> 300ms) em articulações principais
    INVALIDATING  // Perda crítica contínua que impossibilita a derivação biomecânica
}

data class OcclusionEvent(
    val frameId: String,
    val timestamp: Long,
    val bodyPart: LandmarkType,
    val durationMs: Long,
    val severity: OcclusionSeverity,
    val confidenceImpact: Double // Fator de redução de confiança [0.0, 1.0]
)

object OcclusionDetector {

    fun detectOcclusions(
        poseSequence: List<PoseFrame>,
        criticalJoints: List<LandmarkType> = listOf(
            LandmarkType.LEFT_KNEE, LandmarkType.RIGHT_KNEE,
            LandmarkType.LEFT_HIP, LandmarkType.RIGHT_HIP,
            LandmarkType.LEFT_ANKLE, LandmarkType.RIGHT_ANKLE,
            LandmarkType.LEFT_ELBOW, LandmarkType.RIGHT_ELBOW
        )
    ): List<OcclusionEvent> {
        val events = mutableListOf<OcclusionEvent>()
        if (poseSequence.isEmpty()) return events

        // Rastreador de duração de oclusão por articulação
        val occlusionDurations = mutableMapOf<LandmarkType, Long>()
        val lastSeenTimestamp = mutableMapOf<LandmarkType, Long>()

        poseSequence.forEach { pose ->
            criticalJoints.forEach { joint ->
                val landmark = pose.landmarks[joint]
                val isVisible = landmark != null && landmark.visibility >= 0.40 && landmark.confidence >= 0.35

                if (!isVisible) {
                    val prevLastSeen = lastSeenTimestamp[joint] ?: pose.timestamp
                    val currentDuration = (pose.timestamp - prevLastSeen).coerceAtLeast(33L)
                    occlusionDurations[joint] = currentDuration

                    val severity = when {
                        currentDuration < 100L -> OcclusionSeverity.MINOR
                        currentDuration < 300L -> OcclusionSeverity.MODERATE
                        currentDuration < 800L -> OcclusionSeverity.SEVERE
                        else -> OcclusionSeverity.INVALIDATING
                    }

                    val impact = when (severity) {
                        OcclusionSeverity.NONE -> 0.0
                        OcclusionSeverity.MINOR -> 0.10
                        OcclusionSeverity.MODERATE -> 0.30
                        OcclusionSeverity.SEVERE -> 0.60
                        OcclusionSeverity.INVALIDATING -> 0.95
                    }

                    events.add(
                        OcclusionEvent(
                            frameId = pose.frameId,
                            timestamp = pose.timestamp,
                            bodyPart = joint,
                            durationMs = currentDuration,
                            severity = severity,
                            confidenceImpact = impact
                        )
                    )
                } else {
                    lastSeenTimestamp[joint] = pose.timestamp
                    occlusionDurations.remove(joint)
                }
            }
        }

        return events
    }
}
