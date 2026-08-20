package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScientificMethodologyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethodology(methodology: ScientificMethodologyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethodologies(methodologies: List<ScientificMethodologyEntity>)

    @Query("SELECT * FROM scientific_methodologies WHERE methodologyId = :id")
    suspend fun getMethodologyById(id: String): ScientificMethodologyEntity?

    @Query("SELECT * FROM scientific_methodologies WHERE metricId = :metricId")
    suspend fun getMethodologiesForMetric(metricId: String): List<ScientificMethodologyEntity>

    @Query("SELECT * FROM scientific_methodologies")
    suspend fun getAllMethodologies(): List<ScientificMethodologyEntity>
}

@Dao
interface ScientificProtocolDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocol(protocol: ScientificProtocolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocols(protocols: List<ScientificProtocolEntity>)

    @Query("SELECT * FROM scientific_protocols WHERE protocolId = :id")
    suspend fun getProtocolById(id: String): ScientificProtocolEntity?

    @Query("SELECT * FROM scientific_protocols WHERE metricId = :metricId")
    suspend fun getProtocolsForMetric(metricId: String): List<ScientificProtocolEntity>

    @Query("SELECT * FROM scientific_protocols")
    suspend fun getAllProtocols(): List<ScientificProtocolEntity>
}

@Dao
interface MeasurementInstrumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstrument(instrument: MeasurementInstrumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstruments(instruments: List<MeasurementInstrumentEntity>)

    @Query("SELECT * FROM measurement_instruments WHERE instrumentId = :id")
    suspend fun getInstrumentById(id: String): MeasurementInstrumentEntity?

    @Query("SELECT * FROM measurement_instruments")
    suspend fun getAllInstruments(): List<MeasurementInstrumentEntity>
}

@Dao
interface QualityGateEvaluationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: QualityGateEvaluationEntity)

    @Query("SELECT * FROM quality_gate_evaluations WHERE measurementId = :measurementId")
    suspend fun getEvaluationsForMeasurement(measurementId: String): List<QualityGateEvaluationEntity>

    @Query("SELECT * FROM quality_gate_evaluations ORDER BY evaluatedAt DESC")
    fun getAllEvaluationsFlow(): Flow<List<QualityGateEvaluationEntity>>
}

@Dao
interface ProtocolDeviationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviation(deviation: ProtocolDeviationEntity)

    @Query("SELECT * FROM protocol_deviations WHERE measurementId = :measurementId")
    suspend fun getDeviationsForMeasurement(measurementId: String): List<ProtocolDeviationEntity>
}

@Dao
interface PopulationReferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReference(reference: PopulationReferenceEntity)

    @Query("SELECT * FROM population_references WHERE referenceId = :id")
    suspend fun getReferenceById(id: String): PopulationReferenceEntity?

    @Query("SELECT * FROM population_references WHERE metricId = :metricId")
    suspend fun getReferencesForMetric(metricId: String): List<PopulationReferenceEntity>
}

@Dao
interface ScientificAuditLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: ScientificAuditLogEntity)

    @Query("SELECT * FROM scientific_audit_logs ORDER BY timestamp DESC")
    suspend fun getAllAudits(): List<ScientificAuditLogEntity>

    @Query("SELECT * FROM scientific_audit_logs WHERE securityViolation = 1 ORDER BY timestamp DESC")
    suspend fun getSecurityViolations(): List<ScientificAuditLogEntity>
}
