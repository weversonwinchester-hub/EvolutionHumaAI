package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EvolutionEvidencePackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvolutionEvidencePackageDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPackage(pkg: EvolutionEvidencePackageEntity)

    @Query("SELECT * FROM evolution_evidence_packages WHERE userId = :userId ORDER BY generatedAt DESC")
    fun getPackagesByUserIdFlow(userId: String): Flow<List<EvolutionEvidencePackageEntity>>

    @Query("SELECT * FROM evolution_evidence_packages WHERE userId = :userId ORDER BY generatedAt DESC")
    suspend fun getPackagesByUserId(userId: String): List<EvolutionEvidencePackageEntity>

    @Query("SELECT * FROM evolution_evidence_packages WHERE userId = :userId AND isMock = 0 ORDER BY generatedAt DESC LIMIT 1")
    fun getLatestOfficialPackageFlow(userId: String): Flow<EvolutionEvidencePackageEntity?>

    @Query("SELECT * FROM evolution_evidence_packages WHERE userId = :userId AND isMock = 0 ORDER BY generatedAt DESC LIMIT 1")
    suspend fun getLatestOfficialPackage(userId: String): EvolutionEvidencePackageEntity?

    @Query("SELECT * FROM evolution_evidence_packages WHERE id = :id LIMIT 1")
    suspend fun getPackageById(id: String): EvolutionEvidencePackageEntity?
}
