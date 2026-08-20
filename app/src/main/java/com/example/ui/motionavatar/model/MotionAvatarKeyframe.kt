package com.example.ui.motionavatar.model

/**
 * EVOLUTION HUMAN AI — MOTION AVATAR V1
 *
 * A single timestamped keyframe in an exercise animation sequence.
 */
data class MotionAvatarKeyframe(
    val phase: MotionAvatarPhase,
    val normalizedTime: Float, // 0.0f to 1.0f across the cycle
    val pose: MotionAvatarPose,
    val technicalCue: String
)
