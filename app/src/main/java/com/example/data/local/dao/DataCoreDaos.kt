package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CoreAuditLogEntity
import com.example.data.local.entity.CoreEvidenceEntity
import com.example.data.local.entity.CoreMeasurementEntity
import com.example.data.local.entity.MetricEntity
import com.example.data.local.entity.ProtocolEntity
import com.example.data.local.entity.ProvenanceEntity
import com.example.data.local.entity.RawDataInputEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RawDataDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRawData(rawInput: RawDataInputEntity)

    @Query("SELECT * FROM raw_data_inputs WHERE userId = :userId ORDER BY clientTimestamp DESC")
    fun getRawDataInputsFlow(userId: String): Flow<List<RawDataInputEntity>>

    @Query("SELECT * FROM raw_data_inputs WHERE id = :id LIMIT 1")
    suspend fun getRawDataInputById(id: String): RawDataInputEntity?
}

@Dao
interface ProvenanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvenance(provenance: ProvenanceEntity)

    @Query("SELECT * FROM provenance_records WHERE id = :id LIMIT 1")
    suspend fun getProvenanceById(id: String): ProvenanceEntity?
}

@Dao
interface DataCoreMeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoreMeasurement(measurement: CoreMeasurementEntity)

    @Query("SELECT * FROM core_measurements WHERE userId = :userId ORDER BY timestamp DESC")
    fun getMeasurementsFlow(userId: String): Flow<List<CoreMeasurementEntity>>

    @Query("SELECT * FROM core_measurements WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getMeasurementsByUserId(userId: String): List<CoreMeasurementEntity>

    @Query("SELECT * FROM core_measurements WHERE assessmentId = :assessmentId ORDER BY timestamp ASC")
    suspend fun getMeasurementsByAssessmentId(assessmentId: String): List<CoreMeasurementEntity>

    @Query("SELECT * FROM core_measurements WHERE id = :id LIMIT 1")
    suspend fun getMeasurementById(id: String): CoreMeasurementEntity?
}

@Dao
interface DataCoreEvidenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoreEvidence(evidence: CoreEvidenceEntity)

    @Query("SELECT * FROM core_evidences WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getEvidencesFlow(userId: String): Flow<List<CoreEvidenceEntity>>

    @Query("SELECT * FROM core_evidences WHERE userId = :userId ORDER BY submittedAt DESC")
    suspend fun getEvidencesByUserId(userId: String): List<CoreEvidenceEntity>

    @Query("SELECT * FROM core_evidences WHERE id = :id LIMIT 1")
    suspend fun getEvidenceById(id: String): CoreEvidenceEntity?
}

/**
 * DataCoreAuditDao: REGRA DE SEGURANÇA MÁXIMA
 * O histórico de auditoria é rigorosamente imutável.
 * Somente inserções e consultas são suportadas.
 * Não existem métodos de UPDATE ou DELETE.
 */
@Dao
interface DataCoreAuditDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditLog(log: CoreAuditLogEntity)

    @Query("SELECT * FROM core_audit_trail ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAuditLogsFlow(limit: Int = 100): Flow<List<CoreAuditLogEntity>>

    @Query("SELECT * FROM core_audit_trail WHERE actorId = :actorId ORDER BY timestamp DESC")
    fun getAuditLogsByActorFlow(actorId: String): Flow<List<CoreAuditLogEntity>>
}

@Dao
interface ProtocolDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocol(protocol: ProtocolEntity)

    @Query("SELECT * FROM core_protocols")
    suspend fun getAllProtocols(): List<ProtocolEntity>

    @Query("SELECT * FROM core_protocols WHERE id = :id LIMIT 1")
    suspend fun getProtocolById(id: String): ProtocolEntity?
}

@Dao
interface MetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: MetricEntity)

    @Query("SELECT * FROM core_metrics")
    suspend fun getAllMetrics(): List<MetricEntity>

    @Query("SELECT * FROM core_metrics WHERE id = :id LIMIT 1")
    suspend fun getMetricById(id: String): MetricEntity?
}
