package com.example.core.trainingengine.history

import com.example.core.trainingengine.model.SessionStatus
import com.example.core.trainingengine.model.TrainingSession
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Longitudinal Training History and Performance Analytics Models.
 */
data class ExercisePersonalRecord(
    val exerciseId: String,
    val exerciseName: String,
    val maxWeightKg: Double,
    val maxReps: Int,
    val estimated1RM: Double,
    val achievedAt: Long
)

data class WeeklyVolumeSummary(
    val weekEpoch: Long,
    val sessionCount: Int,
    val totalVolumeKg: Double,
    val totalReps: Int,
    val activeDurationMinutes: Int
)

data class TrainingHistorySummary(
    val totalSessions: Int,
    val completedSessions: Int,
    val totalVolumeKg: Double,
    val totalReps: Int,
    val totalDurationHours: Double,
    val weeklyConsistencyPercent: Float,
    val mostFrequentExercises: List<Pair<String, Int>>,
    val personalRecords: Map<String, ExercisePersonalRecord>,
    val weeklyVolumeHistory: List<WeeklyVolumeSummary>
)

/**
 * EVOLUTION HUMAN AI — TRAINING HISTORY ENGINE V1
 *
 * Provides analytical processing of historical training sessions.
 * Preserves strict read-only nature without scientific score contamination.
 */
class TrainingHistoryEngineV1 {

    /**
     * Analyzes a list of historical training sessions to produce a longitudinal summary.
     */
    fun summarizeHistory(sessions: List<TrainingSession>): TrainingHistorySummary {
        val completed = sessions.filter { it.status == SessionStatus.COMPLETED }
        val totalVolume = completed.sumOf { it.totalVolumeKg }
        val totalReps = completed.sumOf { it.totalReps }
        val totalDurationHours = completed.sumOf { it.totalDurationSeconds.toDouble() } / 3600.0

        val exerciseCounts = mutableMapOf<String, Int>()
        val prMap = mutableMapOf<String, ExercisePersonalRecord>()

        for (session in completed) {
            for (exLog in session.exerciseLogs) {
                exerciseCounts[exLog.exerciseName] = (exerciseCounts[exLog.exerciseName] ?: 0) + 1

                for (set in exLog.sets) {
                    if (!set.completed) continue
                    val reps = set.reps.getOrNull() ?: 0
                    val weight = set.loadKg.getOrNull() ?: 0.0

                    if (reps > 0) {
                        // Epley formula for estimated 1RM: 1RM = Weight * (1 + Reps/30)
                        val e1rm = if (weight > 0) weight * (1.0 + (reps / 30.0)) else 0.0
                        val existingPr = prMap[exLog.exerciseId]

                        if (existingPr == null || weight > existingPr.maxWeightKg || e1rm > existingPr.estimated1RM) {
                            prMap[exLog.exerciseId] = ExercisePersonalRecord(
                                exerciseId = exLog.exerciseId,
                                exerciseName = exLog.exerciseName,
                                maxWeightKg = maxOf(weight, existingPr?.maxWeightKg ?: 0.0),
                                maxReps = maxOf(reps, existingPr?.maxReps ?: 0),
                                estimated1RM = maxOf(e1rm, existingPr?.estimated1RM ?: 0.0),
                                achievedAt = set.timestamp
                            )
                        }
                    }
                }
            }
        }

        // Top 5 frequent exercises
        val topExercises = exerciseCounts.toList().sortedByDescending { it.second }.take(5)

        // Weekly Volume Grouping
        val weeklyVolume = groupSessionsByWeek(completed)

        // Consistency calculation (e.g. active weeks / total tracked weeks)
        val consistency = if (weeklyVolume.isNotEmpty()) {
            val activeWeeks = weeklyVolume.count { it.sessionCount >= 2 }
            (activeWeeks.toFloat() / weeklyVolume.size.toFloat()) * 100f
        } else {
            0f
        }

        return TrainingHistorySummary(
            totalSessions = sessions.size,
            completedSessions = completed.size,
            totalVolumeKg = (totalVolume * 10.0).roundToInt() / 10.0,
            totalReps = totalReps,
            totalDurationHours = (totalDurationHours * 10.0).roundToInt() / 10.0,
            weeklyConsistencyPercent = consistency,
            mostFrequentExercises = topExercises,
            personalRecords = prMap,
            weeklyVolumeHistory = weeklyVolume
        )
    }

    private fun groupSessionsByWeek(sessions: List<TrainingSession>): List<WeeklyVolumeSummary> {
        if (sessions.isEmpty()) return emptyList()

        val weekMillis = TimeUnit.DAYS.toMillis(7)
        val sorted = sessions.sortedBy { it.startedAt }
        val earliest = sorted.first().startedAt

        val grouped = sorted.groupBy { session ->
            ((session.startedAt - earliest) / weekMillis)
        }

        return grouped.map { (weekIndex, weekSessions) ->
            val weekEpoch = earliest + (weekIndex * weekMillis)
            WeeklyVolumeSummary(
                weekEpoch = weekEpoch,
                sessionCount = weekSessions.size,
                totalVolumeKg = weekSessions.sumOf { it.totalVolumeKg },
                totalReps = weekSessions.sumOf { it.totalReps },
                activeDurationMinutes = (weekSessions.sumOf { it.totalDurationSeconds } / 60)
            )
        }.sortedBy { it.weekEpoch }
    }
}
