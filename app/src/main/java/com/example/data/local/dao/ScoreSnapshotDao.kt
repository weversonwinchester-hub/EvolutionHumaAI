package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ScoreSnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * ScoreSnapshotDao: Acesso a dados imutáveis de Score Snapshot.
 *
 * REGRA DE AUDITORIA E SEGURANÇA:
 * Snapshots históricos NUNCA são modificados ou deletados.
 * Somente inserções e consultas são suportadas.
 */
@Dao
interface ScoreSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertScoreSnapshot(snapshot: ScoreSnapshotEntity)

    @Query("SELECT * FROM score_snapshots WHERE userId = :userId ORDER BY calculatedAt DESC")
    fun getScoreSnapshotsFlow(userId: String): Flow<List<ScoreSnapshotEntity>>

    @Query("SELECT * FROM score_snapshots WHERE userId = :userId AND isMock = 0 ORDER BY calculatedAt DESC LIMIT 1")
    fun getLatestOfficialSnapshotFlow(userId: String): Flow<ScoreSnapshotEntity?>

    @Query("SELECT * FROM score_snapshots WHERE userId = :userId AND isMock = 0 ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getLatestOfficialSnapshot(userId: String): ScoreSnapshotEntity?

    @Query("SELECT * FROM score_snapshots WHERE userId = :userId ORDER BY calculatedAt DESC LIMIT 1")
    fun getLatestSnapshotFlow(userId: String): Flow<ScoreSnapshotEntity?>

    @Query("SELECT * FROM score_snapshots WHERE userId = :userId ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(userId: String): ScoreSnapshotEntity?

    @Query("SELECT * FROM score_snapshots WHERE id = :id LIMIT 1")
    suspend fun getSnapshotById(id: String): ScoreSnapshotEntity?
}
