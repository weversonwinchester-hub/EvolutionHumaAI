package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EvolutionSnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * EvolutionSnapshotDao: Acesso a dados imutáveis de Snapshots de Evolução.
 *
 * REGRA DE AUDITORIA E GOVERNANÇA:
 * Snapshots de evolução são rigorosamente append-only.
 * Uma vez gravado, o snapshot é imutável para garantir integridade probatória.
 */
@Dao
interface EvolutionSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvolutionSnapshot(snapshot: EvolutionSnapshotEntity)

    @Query("SELECT * FROM evolution_snapshots WHERE userId = :userId ORDER BY evaluatedAt DESC")
    fun getEvolutionSnapshotsFlow(userId: String): Flow<List<EvolutionSnapshotEntity>>

    @Query("SELECT * FROM evolution_snapshots WHERE userId = :userId AND isMock = 0 ORDER BY evaluatedAt DESC LIMIT 1")
    fun getLatestOfficialEvolutionSnapshotFlow(userId: String): Flow<EvolutionSnapshotEntity?>

    @Query("SELECT * FROM evolution_snapshots WHERE userId = :userId AND isMock = 0 ORDER BY evaluatedAt DESC LIMIT 1")
    suspend fun getLatestOfficialEvolutionSnapshot(userId: String): EvolutionSnapshotEntity?

    @Query("SELECT * FROM evolution_snapshots WHERE userId = :userId ORDER BY evaluatedAt DESC LIMIT 1")
    suspend fun getLatestEvolutionSnapshot(userId: String): EvolutionSnapshotEntity?

    @Query("SELECT * FROM evolution_snapshots WHERE userId = :userId AND evaluatedClass = :classId ORDER BY evaluatedAt DESC LIMIT 1")
    suspend fun getLatestSnapshotForClass(userId: String, classId: String): EvolutionSnapshotEntity?

    @Query("SELECT * FROM evolution_snapshots WHERE id = :id LIMIT 1")
    suspend fun getEvolutionSnapshotById(id: String): EvolutionSnapshotEntity?
}
