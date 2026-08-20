package com.example.ui.motionavatar.model

import kotlin.math.roundToInt

/**
 * EVOLUTION HUMAN AI — MOTION AVATAR V1
 *
 * Represents an instantaneous articulated skeletal pose with joint flexion/extension angles
 * and pelvic vertical displacement.
 */
data class MotionAvatarPose(
    val phase: MotionAvatarPhase = MotionAvatarPhase.START,
    val trunkAngleDeg: Float = 0f,       // Forward lean in degrees (0 = vertical)
    val hipFlexionDeg: Float = 0f,      // Hip flexion from anatomical extension (0 = 180° straight, 90 = 90° flexed)
    val kneeFlexionDeg: Float = 0f,     // Knee flexion from anatomical extension (0 = 180° straight, 90 = 90° flexed)
    val ankleDorsiDeg: Float = 0f,      // Ankle dorsiflexion (0 = 90° neutral ankle)
    val shoulderFlexionDeg: Float = 0f, // Shoulder flexion (0 = arms down, 90 = arms horizontal)
    val elbowFlexionDeg: Float = 0f,    // Elbow flexion (0 = straight arm, 90 = 90° bent)
    val neckAngleDeg: Float = 0f,       // Neck angle (0 = neutral spine)
    val pelvisOffsetY: Float = 0f,      // Vertical pelvic drop in normalized units (0.0 = standing, 0.35 = bottom squat)
    val pelvisOffsetX: Float = 0f       // Horizontal pelvic shift (0.0 = centered)
) {
    /**
     * Display-only calculated anatomical angles (degrees)
     */
    val displayKneeAngle: Int
        get() = (180f - kneeFlexionDeg).roundToInt().coerceIn(40, 180)

    val displayHipAngle: Int
        get() = (180f - hipFlexionDeg).roundToInt().coerceIn(40, 180)

    val displayAnkleAngle: Int
        get() = (90f - ankleDorsiDeg).roundToInt().coerceIn(45, 110)

    /**
     * Smoothly interpolates between this pose and another pose given a normalized fraction [0.0, 1.0].
     */
    fun interpolate(target: MotionAvatarPose, fraction: Float): MotionAvatarPose {
        val t = fraction.coerceIn(0f, 1f)
        return MotionAvatarPose(
            phase = if (t < 0.5f) this.phase else target.phase,
            trunkAngleDeg = lerp(this.trunkAngleDeg, target.trunkAngleDeg, t),
            hipFlexionDeg = lerp(this.hipFlexionDeg, target.hipFlexionDeg, t),
            kneeFlexionDeg = lerp(this.kneeFlexionDeg, target.kneeFlexionDeg, t),
            ankleDorsiDeg = lerp(this.ankleDorsiDeg, target.ankleDorsiDeg, t),
            shoulderFlexionDeg = lerp(this.shoulderFlexionDeg, target.shoulderFlexionDeg, t),
            elbowFlexionDeg = lerp(this.elbowFlexionDeg, target.elbowFlexionDeg, t),
            neckAngleDeg = lerp(this.neckAngleDeg, target.neckAngleDeg, t),
            pelvisOffsetY = lerp(this.pelvisOffsetY, target.pelvisOffsetY, t),
            pelvisOffsetX = lerp(this.pelvisOffsetX, target.pelvisOffsetX, t)
        )
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
