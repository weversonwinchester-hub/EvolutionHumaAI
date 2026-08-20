package com.example.ui.motionavatar.engine

import com.example.core.exerciseengine.media.model.AvatarCharacterId
import com.example.ui.motionavatar.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * EVOLUTION HUMAN AI — MOTION AVATAR V1
 *
 * Isolated presentation-layer state machine and runtime controller for 2D/2.5D articulated avatar animation.
 *
 * ARCHITECTURAL CONSTRAINTS:
 * - Has ZERO authority to alter Scientific Score, Evolution, Progression, or Classes.
 * - Operates purely on read-only visual presentation data.
 */
data class MotionAvatarState(
    val exerciseId: String? = null,
    val animation: MotionAvatarAnimation? = null,
    val selectedAvatar: AvatarCharacterId = AvatarCharacterId.MALE_AVATAR_V1,
    val isPlaying: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val currentProgress: Float = 0.0f,
    val currentPose: MotionAvatarPose = MotionAvatarPose(),
    val repetitionCount: Int = 1,
    val isMirrored: Boolean = false,
    val activeCue: String = "",
    val availableSpeeds: List<Float> = listOf(0.5f, 1.0f, 1.5f, 2.0f)
)

class MotionAvatarEngine {

    private val _state = MutableStateFlow(MotionAvatarState())
    val state: StateFlow<MotionAvatarState> = _state.asStateFlow()

    private var accumulatedTimeMs: Float = 0f

    init {
        // Default to initial exercise Bodyweight Squat
        loadAnimation("EX-SQ-BW-001-V1")
    }

    /**
     * Checks if a valid 2D motion avatar animation exists for the given exerciseId.
     */
    fun hasAnimationFor(exerciseId: String): Boolean {
        return ExerciseMotionRegistry.hasDefinitionFor(exerciseId) || exerciseId == MotionAvatarAnimation.BODYWEIGHT_SQUAT_V1.exerciseId
    }

    /**
     * Loads the motion avatar animation for the specified exercise ID.
     */
    fun loadAnimation(exerciseId: String): Boolean {
        val anim = ExerciseMotionRegistry.getAnimationFor(exerciseId)
            ?: when (exerciseId) {
                MotionAvatarAnimation.BODYWEIGHT_SQUAT_V1.exerciseId -> MotionAvatarAnimation.BODYWEIGHT_SQUAT_V1
                else -> null
            }

        if (anim != null) {
            accumulatedTimeMs = 0f
            val initialPose = anim.samplePose(0f)
            _state.value = _state.value.copy(
                exerciseId = exerciseId,
                animation = anim,
                currentProgress = 0f,
                currentPose = initialPose,
                repetitionCount = 1,
                activeCue = anim.keyframes.firstOrNull()?.technicalCue ?: ""
            )
            return true
        } else {
            _state.value = _state.value.copy(
                exerciseId = exerciseId,
                animation = null,
                currentProgress = 0f,
                activeCue = ""
            )
            return false
        }
    }

    /**
     * Advances the animation timeline by deltaMs (scaled by playbackSpeed).
     */
    fun advanceTime(deltaMs: Long) {
        val currentState = _state.value
        val anim = currentState.animation ?: return
        if (!currentState.isPlaying || deltaMs <= 0) return

        val scaledDelta = deltaMs * currentState.playbackSpeed
        accumulatedTimeMs += scaledDelta

        val totalDuration = anim.totalDurationMs.toFloat()
        if (totalDuration <= 0f) return

        var newProgress = accumulatedTimeMs / totalDuration
        var newRep = currentState.repetitionCount

        if (newProgress >= 1.0f) {
            if (anim.isLooping) {
                val completedReps = (newProgress).toInt()
                newRep += completedReps
                accumulatedTimeMs %= totalDuration
                newProgress = accumulatedTimeMs / totalDuration
            } else {
                newProgress = 1.0f
                pause()
            }
        }

        val interpolatedPose = anim.samplePose(newProgress)
        val activeCue = resolveActiveCue(anim, newProgress)

        _state.value = currentState.copy(
            currentProgress = newProgress,
            currentPose = interpolatedPose,
            repetitionCount = newRep,
            activeCue = activeCue
        )
    }

    private fun resolveActiveCue(anim: MotionAvatarAnimation, progress: Float): String {
        var cue = anim.keyframes.first().technicalCue
        for (kf in anim.keyframes) {
            if (progress >= kf.normalizedTime) {
                cue = kf.technicalCue
            }
        }
        return cue
    }

    fun play() {
        _state.value = _state.value.copy(isPlaying = true)
    }

    fun pause() {
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun togglePlay() {
        _state.value = _state.value.copy(isPlaying = !_state.value.isPlaying)
    }

    fun restart() {
        accumulatedTimeMs = 0f
        val anim = _state.value.animation
        val pose = anim?.samplePose(0f) ?: MotionAvatarPose()
        _state.value = _state.value.copy(
            isPlaying = true,
            currentProgress = 0f,
            currentPose = pose,
            repetitionCount = 1
        )
    }

    fun setSpeed(speed: Float) {
        if (speed in listOf(0.5f, 1.0f, 1.5f, 2.0f)) {
            _state.value = _state.value.copy(playbackSpeed = speed)
        }
    }

    fun seek(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        val anim = _state.value.animation ?: return
        accumulatedTimeMs = clamped * anim.totalDurationMs
        val pose = anim.samplePose(clamped)
        val cue = resolveActiveCue(anim, clamped)
        _state.value = _state.value.copy(
            currentProgress = clamped,
            currentPose = pose,
            activeCue = cue
        )
    }

    fun setAvatar(avatarId: AvatarCharacterId) {
        _state.value = _state.value.copy(selectedAvatar = avatarId)
    }

    fun setMirrored(mirrored: Boolean) {
        _state.value = _state.value.copy(isMirrored = mirrored)
    }
}
