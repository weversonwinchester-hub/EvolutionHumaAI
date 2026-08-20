package com.example.ui.training.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.exerciseengine.catalog.ExerciseCatalogV1
import com.example.core.exerciseengine.model.ExerciseDefinition
import com.example.core.trainingengine.engine.TrainingEngineV1
import com.example.core.trainingengine.model.*
import com.example.data.local.entity.SessionExerciseLogEntity
import com.example.data.local.entity.TrainingSessionEntity
import com.example.data.repository.PerformAIRepository
import com.example.ui.motionavatar.engine.MotionAvatarEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExerciseExecutionUiState(
    val exerciseId: String = "EX-SQ-BW-001-V1",
    val exerciseName: String = "Bodyweight Squat",
    val exerciseCategory: String = "STRENGTH",
    val currentSetNumber: Int = 1,
    val targetSets: Int = 3,
    val targetReps: Int = 12,
    val loggedReps: Int = 12,
    val loggedLoadKg: Double = 0.0,
    val selectedRpe: Double = 7.5,
    val isResting: Boolean = false,
    val restRemainingSeconds: Int = 60,
    val isSessionActive: Boolean = true,
    val isPaused: Boolean = false,
    val totalVolumeKg: Double = 0.0,
    val completedSetsCount: Int = 0,
    val isFinished: Boolean = false
)

class ExerciseExecutionViewModel(
    val motionAvatarEngine: MotionAvatarEngine = MotionAvatarEngine(),
    private val trainingEngine: TrainingEngineV1 = TrainingEngineV1(),
    private val repository: PerformAIRepository? = null,
    private val athleteId: String = com.example.core.security.SecurityContext.getAuthenticatedUserId() ?: "ATHLETE-ANONYMOUS"
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseExecutionUiState())
    val uiState: StateFlow<ExerciseExecutionUiState> = _uiState.asStateFlow()

    private var activeSession: TrainingSession = trainingEngine.startFreeformSession(athleteId, "Sessão de Treino")
    private var restTimerJob: Job? = null

    fun initializeExercise(exerciseId: String) {
        val exercise = ExerciseCatalogV1.getExerciseById(exerciseId)
        val name = exercise?.canonicalName ?: "Bodyweight Squat"
        val category = exercise?.category?.name ?: "STRENGTH"

        motionAvatarEngine.loadAnimation(exerciseId)

        _uiState.value = _uiState.value.copy(
            exerciseId = exerciseId,
            exerciseName = name,
            exerciseCategory = category,
            currentSetNumber = 1,
            completedSetsCount = 0,
            isFinished = false
        )
    }

    fun completeCurrentSet() {
        val currentState = _uiState.value
        val setLog = SessionSetLog(
            setNumber = currentState.currentSetNumber,
            reps = ValueState.Recorded(currentState.loggedReps),
            loadKg = ValueState.Recorded(currentState.loggedLoadKg),
            rpe = ValueState.Recorded(currentState.selectedRpe),
            completed = true
        )

        activeSession = trainingEngine.logSet(
            session = activeSession,
            exerciseId = currentState.exerciseId,
            setLog = setLog,
            exerciseName = currentState.exerciseName
        )

        val nextSet = currentState.currentSetNumber + 1
        val newCompleted = currentState.completedSetsCount + 1

        _uiState.value = currentState.copy(
            currentSetNumber = nextSet,
            completedSetsCount = newCompleted,
            totalVolumeKg = activeSession.totalVolumeKg,
            isResting = true,
            restRemainingSeconds = 60
        )

        startRestTimer()
    }

    private fun startRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restRemainingSeconds > 0 && _uiState.value.isResting) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    restRemainingSeconds = _uiState.value.restRemainingSeconds - 1
                )
            }
            _uiState.value = _uiState.value.copy(isResting = false)
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(isResting = false, restRemainingSeconds = 0)
    }

    fun updateReps(reps: Int) {
        _uiState.value = _uiState.value.copy(loggedReps = reps.coerceAtLeast(1))
    }

    fun updateLoad(load: Double) {
        _uiState.value = _uiState.value.copy(loggedLoadKg = load.coerceAtLeast(0.0))
    }

    fun updateRpe(rpe: Double) {
        _uiState.value = _uiState.value.copy(selectedRpe = rpe)
    }

    fun togglePauseSession() {
        val paused = !_uiState.value.isPaused
        activeSession = if (paused) {
            motionAvatarEngine.pause()
            trainingEngine.pauseSession(activeSession)
        } else {
            motionAvatarEngine.play()
            trainingEngine.resumeSession(activeSession)
        }
        _uiState.value = _uiState.value.copy(isPaused = paused)
    }

    fun finishSession() {
        restTimerJob?.cancel()
        activeSession = trainingEngine.finishSession(
            session = activeSession,
            perceivedExertion = ValueState.Recorded(_uiState.value.selectedRpe)
        )
        _uiState.value = _uiState.value.copy(isFinished = true, isSessionActive = false)
        persistActiveSession()
    }

    fun abandonSession() {
        restTimerJob?.cancel()
        activeSession = trainingEngine.abandonSession(activeSession)
        _uiState.value = _uiState.value.copy(isFinished = true, isSessionActive = false)
        persistActiveSession()
    }

    private fun persistActiveSession() {
        val repo = repository ?: return
        viewModelScope.launch {
            try {
                val entity = TrainingSessionEntity(
                    sessionId = activeSession.sessionId,
                    userId = activeSession.userId,
                    workoutId = activeSession.workoutId,
                    sessionName = activeSession.sessionName,
                    status = activeSession.status,
                    startedAt = activeSession.startedAt,
                    endedAt = activeSession.endedAt,
                    totalDurationSeconds = activeSession.totalDurationSeconds,
                    activeDurationSeconds = activeSession.activeDurationSeconds,
                    pausedDurationSeconds = activeSession.pausedDurationSeconds,
                    perceivedExertionValue = (activeSession.perceivedExertion as? ValueState.Recorded)?.value,
                    notes = activeSession.notes,
                    completionRate = activeSession.completionRate,
                    totalVolumeKg = activeSession.totalVolumeKg,
                    totalReps = activeSession.totalReps,
                    evidencePackageId = activeSession.evidencePackageId,
                    syncStatus = activeSession.syncStatus,
                    version = activeSession.version
                )
                repo.insertTrainingSession(entity)

                val logEntities = activeSession.exerciseLogs.map { log ->
                    SessionExerciseLogEntity(
                        logId = log.logId,
                        sessionId = activeSession.sessionId,
                        exerciseId = log.exerciseId,
                        exerciseName = log.exerciseName,
                        order = log.order,
                        setsJson = log.sets.joinToString(separator = ";") { set ->
                            "${set.setNumber}:${set.reps.getOrNull() ?: 0}:${set.loadKg.getOrNull() ?: 0.0}:${set.rpe.getOrNull() ?: 0.0}:${set.completed}"
                        },
                        startedAt = log.startedAt,
                        endedAt = log.endedAt,
                        totalRestSeconds = log.totalRestSeconds,
                        status = log.status,
                        notes = log.notes
                    )
                }
                if (logEntities.isNotEmpty()) {
                    repo.insertSessionExerciseLogs(logEntities)
                }
            } catch (e: Exception) {
                // Keep session memory state intact even if SQLite write encounters non-fatal issue
            }
        }
    }
}
