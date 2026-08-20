package com.example.core.trainingengine.engine

import com.example.core.trainingengine.model.*
import java.util.UUID

/**
 * EVOLUTION HUMAN AI — TRAINING ENGINE V1
 *
 * Domain engine for real-time and asynchronous training session execution,
 * lifecycle management, set/rep logging, and volume metrics derivation.
 *
 * ARCHITECTURAL CONSTRAINTS:
 * - Has ZERO authority to alter Scientific Score, Evolution, Progression, or Classes.
 * - Training Session data is strictly observational and empirical.
 */
class TrainingEngineV1 {

    /**
     * Initializes a new active training session from a planned workout.
     */
    fun startSession(
        userId: String,
        workout: Workout,
        startTimeMs: Long = System.currentTimeMillis()
    ): TrainingSession {
        require(userId.isNotBlank()) { "userId não pode ser vazio." }

        val exerciseLogs = workout.items.map { item ->
            SessionExerciseLog(
                logId = UUID.randomUUID().toString(),
                exerciseId = item.exerciseId,
                exerciseName = item.exerciseName,
                order = item.order,
                sets = emptyList(),
                startedAt = startTimeMs,
                status = ExerciseExecutionStatus.PENDING
            )
        }

        return TrainingSession(
            sessionId = UUID.randomUUID().toString(),
            userId = userId,
            workoutId = workout.workoutId,
            sessionName = workout.name,
            status = SessionStatus.IN_PROGRESS,
            startedAt = startTimeMs,
            exerciseLogs = exerciseLogs,
            syncStatus = SyncStatus.LOCAL_ONLY
        )
    }

    /**
     * Starts a freeform training session without a pre-existing workout template.
     */
    fun startFreeformSession(
        userId: String,
        sessionName: String = "Treino Livre",
        startTimeMs: Long = System.currentTimeMillis()
    ): TrainingSession {
        require(userId.isNotBlank()) { "userId não pode ser vazio." }

        return TrainingSession(
            sessionId = UUID.randomUUID().toString(),
            userId = userId,
            workoutId = null,
            sessionName = sessionName.trim(),
            status = SessionStatus.IN_PROGRESS,
            startedAt = startTimeMs,
            exerciseLogs = emptyList(),
            syncStatus = SyncStatus.LOCAL_ONLY
        )
    }

    /**
     * Pauses an in-progress training session.
     */
    fun pauseSession(session: TrainingSession, pauseTimeMs: Long = System.currentTimeMillis()): TrainingSession {
        if (session.status != SessionStatus.IN_PROGRESS) return session
        return session.copy(
            status = SessionStatus.PAUSED
        )
    }

    /**
     * Resumes a paused training session.
     */
    fun resumeSession(session: TrainingSession, resumeTimeMs: Long = System.currentTimeMillis()): TrainingSession {
        if (session.status != SessionStatus.PAUSED) return session
        return session.copy(
            status = SessionStatus.IN_PROGRESS
        )
    }

    /**
     * Logs or appends a completed set for an exercise in the session.
     */
    fun logSet(
        session: TrainingSession,
        exerciseId: String,
        setLog: SessionSetLog,
        exerciseName: String = exerciseId
    ): TrainingSession {
        val existingIndex = session.exerciseLogs.indexOfFirst { it.exerciseId == exerciseId }

        val updatedExerciseLogs = if (existingIndex >= 0) {
            val currentExLog = session.exerciseLogs[existingIndex]
            val updatedSets = currentExLog.sets + setLog
            val updatedExLog = currentExLog.copy(
                sets = updatedSets,
                status = ExerciseExecutionStatus.IN_PROGRESS
            )
            session.exerciseLogs.toMutableList().apply { set(existingIndex, updatedExLog) }
        } else {
            val newExLog = SessionExerciseLog(
                logId = UUID.randomUUID().toString(),
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                order = session.exerciseLogs.size + 1,
                sets = listOf(setLog),
                status = ExerciseExecutionStatus.IN_PROGRESS
            )
            session.exerciseLogs + newExLog
        }

        return calculateMetrics(session.copy(exerciseLogs = updatedExerciseLogs))
    }

    /**
     * Marks an exercise as skipped in the current session.
     */
    fun skipExercise(session: TrainingSession, exerciseId: String): TrainingSession {
        val updatedLogs = session.exerciseLogs.map { log ->
            if (log.exerciseId == exerciseId) {
                log.copy(status = ExerciseExecutionStatus.SKIPPED)
            } else {
                log
            }
        }
        return calculateMetrics(session.copy(exerciseLogs = updatedLogs))
    }

    /**
     * Finishes and finalizes the training session.
     */
    fun finishSession(
        session: TrainingSession,
        endTimeMs: Long = System.currentTimeMillis(),
        perceivedExertion: ValueState<Double> = ValueState.NotSpecified,
        notes: String = session.notes
    ): TrainingSession {
        val totalDurationSeconds = ((endTimeMs - session.startedAt) / 1000).toInt().coerceAtLeast(0)

        val updatedLogs = session.exerciseLogs.map { log ->
            if (log.sets.isNotEmpty() && log.status != ExerciseExecutionStatus.SKIPPED) {
                log.copy(status = ExerciseExecutionStatus.COMPLETED, endedAt = endTimeMs)
            } else {
                log
            }
        }

        val finalizedSession = session.copy(
            status = SessionStatus.COMPLETED,
            endedAt = endTimeMs,
            totalDurationSeconds = totalDurationSeconds,
            activeDurationSeconds = totalDurationSeconds, // simplified for non-paused duration
            exerciseLogs = updatedLogs,
            perceivedExertion = perceivedExertion,
            notes = notes.trim()
        )

        return calculateMetrics(finalizedSession)
    }

    /**
     * Abandons the training session.
     */
    fun abandonSession(
        session: TrainingSession,
        endTimeMs: Long = System.currentTimeMillis(),
        reason: String = "Abandonado pelo usuário"
    ): TrainingSession {
        val totalDurationSeconds = ((endTimeMs - session.startedAt) / 1000).toInt().coerceAtLeast(0)

        return session.copy(
            status = SessionStatus.ABANDONED,
            endedAt = endTimeMs,
            totalDurationSeconds = totalDurationSeconds,
            notes = if (session.notes.isBlank()) reason else "${session.notes} | $reason"
        )
    }

    /**
     * Computes completion rate, total volume (kg), and total reps across all logged sets.
     */
    fun calculateMetrics(session: TrainingSession): TrainingSession {
        var totalVolume = 0.0
        var totalRepsCount = 0
        var completedExercises = 0

        for (exLog in session.exerciseLogs) {
            if (exLog.status == ExerciseExecutionStatus.COMPLETED || (exLog.sets.isNotEmpty() && exLog.status != ExerciseExecutionStatus.SKIPPED)) {
                completedExercises++
            }
            for (setLog in exLog.sets) {
                if (setLog.completed) {
                    val reps = setLog.reps.getOrNull() ?: 0
                    val load = setLog.loadKg.getOrNull() ?: 0.0
                    totalRepsCount += reps
                    totalVolume += (reps * load)
                }
            }
        }

        val totalExercises = session.exerciseLogs.size
        val completionRate = if (totalExercises > 0) {
            completedExercises.toFloat() / totalExercises.toFloat()
        } else if (totalRepsCount > 0) {
            1.0f
        } else {
            0.0f
        }

        return session.copy(
            totalVolumeKg = totalVolume,
            totalReps = totalRepsCount,
            completionRate = completionRate.coerceIn(0.0f, 1.0f)
        )
    }
}
