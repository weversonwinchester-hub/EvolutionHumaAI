package com.example.ui.motionavatar.model

import java.util.concurrent.ConcurrentHashMap

/**
 * EVOLUTION HUMAN AI — EXERCISE MOTION DEFINITION
 *
 * Generic, reusable data structure for 2D/2.5D articulated avatar movement.
 * Allows declarative expansion for any canonical exercise without creating
 * separate bespoke rendering systems.
 */
data class ExerciseMotionDefinition(
    val exerciseId: String,
    val canonicalName: String,
    val targetMuscleGroup: String = "",
    val totalDurationMs: Long = 3000L,
    val keyframes: List<MotionAvatarKeyframe>,
    val involvedJoints: List<MotionAvatarJointType> = listOf(
        MotionAvatarJointType.LEFT_HIP,
        MotionAvatarJointType.LEFT_KNEE,
        MotionAvatarJointType.LEFT_ANKLE,
        MotionAvatarJointType.TORSO
    ),
    val isLooping: Boolean = true,
    val version: String = "1.0.0"
) {
    /**
     * Converts to runtime MotionAvatarAnimation structure.
     */
    fun toAnimation(): MotionAvatarAnimation {
        return MotionAvatarAnimation(
            exerciseId = exerciseId,
            canonicalName = canonicalName,
            totalDurationMs = totalDurationMs,
            keyframes = keyframes,
            isLooping = isLooping
        )
    }
}

/**
 * Registry of declarative exercise motion definitions.
 */
object ExerciseMotionRegistry {

    private val definitions = ConcurrentHashMap<String, ExerciseMotionDefinition>()

    init {
        // Register canonical validation exercise: EX-SQ-BW-001-V1 (Bodyweight Squat)
        val squatAnim = MotionAvatarAnimation.BODYWEIGHT_SQUAT_V1
        val squatDef = ExerciseMotionDefinition(
            exerciseId = squatAnim.exerciseId,
            canonicalName = squatAnim.canonicalName,
            targetMuscleGroup = "Quadríceps & Glúteos",
            totalDurationMs = squatAnim.totalDurationMs,
            keyframes = squatAnim.keyframes,
            involvedJoints = listOf(
                MotionAvatarJointType.LEFT_HIP,
                MotionAvatarJointType.LEFT_KNEE,
                MotionAvatarJointType.LEFT_ANKLE,
                MotionAvatarJointType.TORSO,
                MotionAvatarJointType.PELVIS
            ),
            isLooping = squatAnim.isLooping
        )
        registerDefinition(squatDef)
    }

    fun registerDefinition(def: ExerciseMotionDefinition) {
        definitions[def.exerciseId] = def
    }

    fun getDefinition(exerciseId: String): ExerciseMotionDefinition? {
        return definitions[exerciseId]
    }

    fun hasDefinitionFor(exerciseId: String): Boolean {
        return definitions.containsKey(exerciseId)
    }

    fun getAnimationFor(exerciseId: String): MotionAvatarAnimation? {
        return definitions[exerciseId]?.toAnimation()
    }

    fun getAllRegisteredExerciseIds(): Set<String> {
        return definitions.keys.toSet()
    }
}
