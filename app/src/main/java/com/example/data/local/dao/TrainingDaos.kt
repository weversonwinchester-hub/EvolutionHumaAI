package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * EVOLUTION HUMAN AI — TRAINING DOMAIN DAOS
 */

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutItems(items: List<WorkoutItemEntity>)

    @Query("SELECT * FROM workouts WHERE workoutId = :workoutId")
    suspend fun getWorkoutById(workoutId: String): WorkoutEntity?

    @Query("SELECT * FROM workout_items WHERE workoutId = :workoutId ORDER BY `order` ASC")
    suspend fun getItemsForWorkout(workoutId: String): List<WorkoutItemEntity>

    @Query("SELECT * FROM workouts WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isTemplate = 1 AND isArchived = 0")
    suspend fun getTemplates(): List<WorkoutEntity>

    @Query("DELETE FROM workouts WHERE workoutId = :workoutId")
    suspend fun deleteWorkout(workoutId: String)
}

@Dao
interface TrainingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLogs(logs: List<SessionExerciseLogEntity>)

    @Query("SELECT * FROM training_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): TrainingSessionEntity?

    @Query("SELECT * FROM session_exercise_logs WHERE sessionId = :sessionId ORDER BY `order` ASC")
    suspend fun getExerciseLogsForSession(sessionId: String): List<SessionExerciseLogEntity>

    @Query("SELECT * FROM training_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getSessionsForUserFlow(userId: String): Flow<List<TrainingSessionEntity>>

    @Query("SELECT * FROM training_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    suspend fun getSessionsForUser(userId: String): List<TrainingSessionEntity>

    @Query("SELECT COUNT(*) FROM training_sessions WHERE userId = :userId")
    suspend fun getSessionCountForUser(userId: String): Int
}

@Dao
interface AthleteBaselineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaseline(baseline: AthleteBaselineEntity)

    @Query("SELECT * FROM athlete_baselines WHERE userId = :userId ORDER BY version DESC LIMIT 1")
    suspend fun getLatestBaseline(userId: String): AthleteBaselineEntity?

    @Query("SELECT * FROM athlete_baselines WHERE userId = :userId ORDER BY version DESC")
    suspend fun getAllBaselineVersions(userId: String): List<AthleteBaselineEntity>
}

@Dao
interface AthleteGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: AthleteGoalEntity)

    @Query("SELECT * FROM athlete_goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsForUserFlow(userId: String): Flow<List<AthleteGoalEntity>>

    @Query("SELECT * FROM athlete_goals WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getGoalsForUser(userId: String): List<AthleteGoalEntity>

    @Query("UPDATE athlete_goals SET isAchieved = :isAchieved, updatedAt = :timestamp WHERE goalId = :goalId")
    suspend fun updateGoalStatus(goalId: String, isAchieved: Boolean, timestamp: Long)
}

@Dao
interface TrainingSyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: TrainingSyncQueueEntity)

    @Query("SELECT * FROM training_sync_queue WHERE syncStatus = 'PENDING_SYNC' ORDER BY createdAt ASC")
    suspend fun getPendingItems(): List<TrainingSyncQueueEntity>

    @Query("DELETE FROM training_sync_queue WHERE queueId = :queueId")
    suspend fun deleteQueueItem(queueId: String)
}
