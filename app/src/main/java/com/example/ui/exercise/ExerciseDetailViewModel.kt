package com.example.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.exerciseengine.engine.ExerciseEngineV1
import com.example.core.exerciseengine.media.model.*
import com.example.core.exerciseengine.media.resolver.ExerciseMediaResolver
import com.example.core.exerciseengine.media.resolver.MediaResolutionRequest
import com.example.core.exerciseengine.media.resolver.MediaResolutionResult
import com.example.core.exerciseengine.model.ExerciseDefinition
import com.example.core.exerciseengine.progression.ExerciseProgressionPath
import com.example.ui.motionavatar.engine.MotionAvatarEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExerciseDetailUiState(
    val isLoading: Boolean = true,
    val exercise: ExerciseDefinition? = null,
    val resolvedMedia: MediaResolutionResult? = null,
    val progressionPath: ExerciseProgressionPath? = null,
    val selectedAvatar: AvatarCharacterId = AvatarCharacterId.MALE_AVATAR_V1,
    val selectedAngle: CameraPreset = CameraPreset.FRONT,
    val isPlaying: Boolean = false,
    val isFullscreen: Boolean = false,
    val networkStatus: NetworkStatus = NetworkStatus.ONLINE,
    val availableAngles: List<CameraPreset> = listOf(CameraPreset.FRONT, CameraPreset.SIDE, CameraPreset.THREE_QUARTER),
    val errorMessage: String? = null
)

class ExerciseDetailViewModel(
    private val initialExerciseId: String = "EX-SQ-BW-001-V1"
) : ViewModel() {

    val motionAvatarEngine: MotionAvatarEngine = MotionAvatarEngine()

    private val _uiState = MutableStateFlow(ExerciseDetailUiState())
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()

    init {
        loadExercise(initialExerciseId)
    }

    fun loadExercise(exerciseId: String) {
        motionAvatarEngine.loadAnimation(exerciseId)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val exercise = ExerciseEngineV1.getExercise(exerciseId)
            if (exercise == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Exercício não encontrado no catálogo oficial."
                )
                return@launch
            }

            val progression = ExerciseEngineV1.getProgressionPath(exerciseId)
            val mediaResolution = resolveCurrentMedia(exercise.exerciseId)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                exercise = exercise,
                resolvedMedia = mediaResolution,
                progressionPath = progression,
                availableAngles = exercise.biomechanicalProfile.cameraRequirements.mapNotNull {
                    runCatching { CameraPreset.valueOf(it) }.getOrNull()
                }.ifEmpty { listOf(CameraPreset.FRONT, CameraPreset.SIDE, CameraPreset.THREE_QUARTER) }
            )
        }
    }

    fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    fun setAvatar(avatar: AvatarCharacterId) {
        motionAvatarEngine.setAvatar(avatar)
        _uiState.value = _uiState.value.copy(selectedAvatar = avatar)
        _uiState.value.exercise?.let { ex ->
            _uiState.value = _uiState.value.copy(resolvedMedia = resolveCurrentMedia(ex.exerciseId))
        }
    }

    fun setCameraAngle(angle: CameraPreset) {
        _uiState.value = _uiState.value.copy(selectedAngle = angle)
        _uiState.value.exercise?.let { ex ->
            _uiState.value = _uiState.value.copy(resolvedMedia = resolveCurrentMedia(ex.exerciseId))
        }
    }

    fun toggleFullscreen() {
        _uiState.value = _uiState.value.copy(isFullscreen = !_uiState.value.isFullscreen)
    }

    fun setNetworkStatus(status: NetworkStatus) {
        _uiState.value = _uiState.value.copy(networkStatus = status)
        _uiState.value.exercise?.let { ex ->
            _uiState.value = _uiState.value.copy(resolvedMedia = resolveCurrentMedia(ex.exerciseId))
        }
    }

    private fun resolveCurrentMedia(exerciseId: String): MediaResolutionResult {
        val state = _uiState.value
        val request = MediaResolutionRequest(
            exerciseId = exerciseId,
            preferredAvatar = state.selectedAvatar,
            preferredAngle = state.selectedAngle,
            networkStatus = state.networkStatus
        )
        return ExerciseMediaResolver.resolve(request)
    }
}
