package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TrialAttemptEntity
import com.example.data.local.entity.TrialSessionEntity
import com.example.data.local.entity.TrialSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrialSessionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTrialSession(session: TrialSessionEntity)

    @Update
    suspend fun updateTrialSession(session: TrialSessionEntity)

    @Query("SELECT * FROM trial_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): TrialSessionEntity?

    @Query("SELECT * FROM trial_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getSessionsByUserFlow(userId: String): Flow<List<TrialSessionEntity>>

    @Query("SELECT * FROM trial_sessions WHERE userId = :userId AND status IN ('CREATED', 'READY', 'RUNNING', 'PAUSED') LIMIT 1")
    suspend fun getActiveSession(userId: String): TrialSessionEntity?
}

@Dao
interface TrialAttemptDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTrialAttempt(attempt: TrialAttemptEntity)

    @Query("SELECT * FROM trial_attempts WHERE sessionId = :sessionId ORDER BY attemptNumber ASC")
    suspend fun getAttemptsBySessionId(sessionId: String): List<TrialAttemptEntity>

    @Query("SELECT * FROM trial_attempts WHERE sessionId = :sessionId ORDER BY attemptNumber ASC")
    fun getAttemptsBySessionIdFlow(sessionId: String): Flow<List<TrialAttemptEntity>>
}

@Dao
interface TrialSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTrialSnapshot(snapshot: TrialSnapshotEntity)

    @Query("SELECT * FROM trial_snapshots WHERE userId = :userId ORDER BY calculatedAt DESC")
    fun getSnapshotsByUserFlow(userId: String): Flow<List<TrialSnapshotEntity>>

    @Query("SELECT * FROM trial_snapshots WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSnapshotBySessionId(sessionId: String): TrialSnapshotEntity?

    @Query("SELECT * FROM trial_snapshots WHERE userId = :userId AND isMock = 0 AND resultStatus = 'QUALIFIED' ORDER BY calculatedAt DESC")
    suspend fun getQualifiedOfficialSnapshots(userId: String): List<TrialSnapshotEntity>
}
