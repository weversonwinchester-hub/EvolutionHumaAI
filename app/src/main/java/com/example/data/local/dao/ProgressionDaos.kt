package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * PERFORMAI PROGRESSION ENGINE V1 - DAOS
 */

@Dao
interface ProgressionStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertState(state: EvolutionProgressionStateEntity)

    @Query("SELECT * FROM evolution_progression_states WHERE userId = :userId AND isMock = 0 ORDER BY lastAssessmentAt DESC LIMIT 1")
    suspend fun getLatestState(userId: String): EvolutionProgressionStateEntity?

    @Query("SELECT * FROM evolution_progression_states WHERE userId = :userId AND isMock = 0 ORDER BY lastAssessmentAt DESC LIMIT 1")
    fun observeLatestState(userId: String): Flow<EvolutionProgressionStateEntity?>
}

@Dao
interface PromotionCandidateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: PromotionCandidateEntity)

    @Query("SELECT * FROM promotion_candidates WHERE userId = :userId AND isMock = 0 ORDER BY createdAt DESC")
    suspend fun getCandidatesForUser(userId: String): List<PromotionCandidateEntity>

    @Query("SELECT * FROM promotion_candidates WHERE userId = :userId AND isMock = 0 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestCandidate(userId: String): PromotionCandidateEntity?
}

@Dao
interface ProgressionSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: ProgressionAssessmentSnapshotEntity)

    @Query("SELECT * FROM progression_assessment_snapshots WHERE userId = :userId AND isMock = 0 ORDER BY calculatedAt DESC")
    suspend fun getSnapshotsForUser(userId: String): List<ProgressionAssessmentSnapshotEntity>

    @Query("SELECT * FROM progression_assessment_snapshots WHERE id = :id")
    suspend fun getSnapshotById(id: String): ProgressionAssessmentSnapshotEntity?
}

@Dao
interface EvolutionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertHistoryEntry(entry: EvolutionHistoryEntity)

    @Query("SELECT * FROM evolution_history_entries WHERE userId = :userId AND isMock = 0 ORDER BY timestamp ASC")
    suspend fun getHistoryForUser(userId: String): List<EvolutionHistoryEntity>

    @Query("SELECT * FROM evolution_history_entries WHERE userId = :userId AND isMock = 0 ORDER BY timestamp ASC")
    fun observeHistoryForUser(userId: String): Flow<List<EvolutionHistoryEntity>>
}

@Dao
interface ProgressionAnomalyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnomaly(anomaly: ProgressionAnomalyEntity)

    @Query("SELECT * FROM progression_anomalies WHERE userId = :userId ORDER BY detectedAt DESC")
    suspend fun getAnomaliesForUser(userId: String): List<ProgressionAnomalyEntity>
}
