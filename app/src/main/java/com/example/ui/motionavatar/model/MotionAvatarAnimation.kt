package com.example.ui.motionavatar.model

/**
 * EVOLUTION HUMAN AI — MOTION AVATAR V1
 *
 * Sequence of keyframes representing an articulated exercise animation.
 */
data class MotionAvatarAnimation(
    val exerciseId: String,
    val canonicalName: String,
    val totalDurationMs: Long = 3000L,
    val keyframes: List<MotionAvatarKeyframe>,
    val isLooping: Boolean = true
) {
    init {
        require(keyframes.isNotEmpty()) { "Animation must have at least one keyframe" }
    }

    /**
     * Samples the continuous interpolated pose at any normalized progress [0.0f, 1.0f].
     */
    fun samplePose(normalizedProgress: Float): MotionAvatarPose {
        val clampedProgress = normalizedProgress.coerceIn(0f, 1f)

        if (keyframes.size == 1) return keyframes[0].pose

        // Find the bounding keyframes
        var prev = keyframes.first()
        var next = keyframes.last()

        for (i in 0 until keyframes.size - 1) {
            val k1 = keyframes[i]
            val k2 = keyframes[i + 1]
            if (clampedProgress >= k1.normalizedTime && clampedProgress <= k2.normalizedTime) {
                prev = k1
                next = k2
                break
            }
        }

        val range = next.normalizedTime - prev.normalizedTime
        val fraction = if (range > 0f) {
            ((clampedProgress - prev.normalizedTime) / range).coerceIn(0f, 1f)
        } else {
            0f
        }

        // Apply smooth cosine easing for organic biological movement
        val smoothedFraction = (1f - kotlin.math.cos(fraction * Math.PI.toFloat())) / 2f

        return prev.pose.interpolate(next.pose, smoothedFraction)
    }

    /**
     * Samples the pose at a given time in milliseconds.
     */
    fun samplePoseAtTime(timeMs: Long): MotionAvatarPose {
        val progress = if (totalDurationMs > 0) {
            val elapsed = if (isLooping) timeMs % totalDurationMs else timeMs.coerceAtMost(totalDurationMs)
            elapsed.toFloat() / totalDurationMs.toFloat()
        } else {
            0f
        }
        return samplePose(progress)
    }

    companion object {
        /**
         * The ONLY exercise defined in Motion Avatar V1:
         * EX-SQ-BW-001-V1 — Bodyweight Squat
         *
         * 5 Keyframes:
         * 1. START: Standing upright (0.00)
         * 2. DESCENT: Hip & knee flexion begins (0.25)
         * 3. BOTTOM: Controlled squat bottom position (0.50)
         * 4. ASCENT: Return toward standing (0.75)
         * 5. END: Standing upright (1.00)
         */
        val BODYWEIGHT_SQUAT_V1 = MotionAvatarAnimation(
            exerciseId = "EX-SQ-BW-001-V1",
            canonicalName = "Bodyweight Squat",
            totalDurationMs = 3200L,
            keyframes = listOf(
                MotionAvatarKeyframe(
                    phase = MotionAvatarPhase.START,
                    normalizedTime = 0.0f,
                    pose = MotionAvatarPose(
                        phase = MotionAvatarPhase.START,
                        trunkAngleDeg = 0f,
                        hipFlexionDeg = 0f,
                        kneeFlexionDeg = 0f,
                        ankleDorsiDeg = 0f,
                        shoulderFlexionDeg = 15f,
                        elbowFlexionDeg = 20f,
                        neckAngleDeg = 0f,
                        pelvisOffsetY = 0f,
                        pelvisOffsetX = 0f
                    ),
                    technicalCue = "Posição inicial ereta. Pés alinhados, tronco neutro e olhar à frente."
                ),
                MotionAvatarKeyframe(
                    phase = MotionAvatarPhase.DESCENT,
                    normalizedTime = 0.25f,
                    pose = MotionAvatarPose(
                        phase = MotionAvatarPhase.DESCENT,
                        trunkAngleDeg = 14f,
                        hipFlexionDeg = 45f,
                        kneeFlexionDeg = 45f,
                        ankleDorsiDeg = 10f,
                        shoulderFlexionDeg = 45f,
                        elbowFlexionDeg = 30f,
                        neckAngleDeg = 0f,
                        pelvisOffsetY = 0.18f,
                        pelvisOffsetX = 0f
                    ),
                    technicalCue = "Início da descida: flexão simultânea e controlada de quadril e joelhos."
                ),
                MotionAvatarKeyframe(
                    phase = MotionAvatarPhase.BOTTOM,
                    normalizedTime = 0.50f,
                    pose = MotionAvatarPose(
                        phase = MotionAvatarPhase.BOTTOM,
                        trunkAngleDeg = 28f,
                        hipFlexionDeg = 92f,   // Hip flexion to ~88°
                        kneeFlexionDeg = 96f,  // Knee flexion to ~84° (parallel depth)
                        ankleDorsiDeg = 20f,
                        shoulderFlexionDeg = 75f,
                        elbowFlexionDeg = 35f,
                        neckAngleDeg = 0f,
                        pelvisOffsetY = 0.38f,
                        pelvisOffsetX = 0f
                    ),
                    technicalCue = "Ponto de reversão: profundidade paralela com alinhamento articular estável."
                ),
                MotionAvatarKeyframe(
                    phase = MotionAvatarPhase.ASCENT,
                    normalizedTime = 0.75f,
                    pose = MotionAvatarPose(
                        phase = MotionAvatarPhase.ASCENT,
                        trunkAngleDeg = 15f,
                        hipFlexionDeg = 45f,
                        kneeFlexionDeg = 45f,
                        ankleDorsiDeg = 10f,
                        shoulderFlexionDeg = 45f,
                        elbowFlexionDeg = 30f,
                        neckAngleDeg = 0f,
                        pelvisOffsetY = 0.18f,
                        pelvisOffsetX = 0f
                    ),
                    technicalCue = "Subida concêntrica: extensão coordenada de joelhos e quadris mantendo o tronco firme."
                ),
                MotionAvatarKeyframe(
                    phase = MotionAvatarPhase.END,
                    normalizedTime = 1.0f,
                    pose = MotionAvatarPose(
                        phase = MotionAvatarPhase.END,
                        trunkAngleDeg = 0f,
                        hipFlexionDeg = 0f,
                        kneeFlexionDeg = 0f,
                        ankleDorsiDeg = 0f,
                        shoulderFlexionDeg = 15f,
                        elbowFlexionDeg = 20f,
                        neckAngleDeg = 0f,
                        pelvisOffsetY = 0f,
                        pelvisOffsetX = 0f
                    ),
                    technicalCue = "Finalização da repetição com bloqueio articular e postura ereta."
                )
            ),
            isLooping = true
        )
    }
}
