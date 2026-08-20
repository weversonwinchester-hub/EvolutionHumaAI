package com.example.ui.motionavatar.model

/**
 * EVOLUTION HUMAN AI — MOTION AVATAR V1
 *
 * Articulated skeletal joints and components for lightweight 2D/2.5D animation.
 */
enum class MotionAvatarJointType {
    HEAD,
    NECK,
    TORSO,
    PELVIS,
    LEFT_SHOULDER,
    LEFT_ELBOW,
    LEFT_WRIST,
    LEFT_HAND,
    RIGHT_SHOULDER,
    RIGHT_ELBOW,
    RIGHT_WRIST,
    RIGHT_HAND,
    LEFT_HIP,
    LEFT_KNEE,
    LEFT_ANKLE,
    LEFT_FOOT,
    RIGHT_HIP,
    RIGHT_KNEE,
    RIGHT_ANKLE,
    RIGHT_FOOT
}

enum class MotionAvatarPhase {
    START,
    DESCENT,
    BOTTOM,
    ASCENT,
    END
}
