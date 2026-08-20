package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercise_definitions WHERE exerciseId = :exerciseId")
    suspend fun getExerciseById(exerciseId: String): ExerciseEntity?

    @Query("SELECT * FROM exercise_definitions WHERE status = 'ACTIVE' ORDER BY canonicalName ASC")
    fun getAllActiveExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ExerciseVersionEntity)

    @Query("SELECT * FROM exercise_versions WHERE exerciseId = :exerciseId ORDER BY version ASC")
    suspend fun getVersionsByExerciseId(exerciseId: String): List<ExerciseVersionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaReference(media: ExerciseMediaEntity)

    @Query("SELECT * FROM exercise_media_references WHERE exerciseId = :exerciseId")
    suspend fun getMediaForExercise(exerciseId: String): List<ExerciseMediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: ExercisePrescriptionEntity)

    @Query("SELECT * FROM exercise_prescriptions WHERE exerciseId = :exerciseId")
    suspend fun getPrescriptionsForExercise(exerciseId: String): List<ExercisePrescriptionEntity>
}

@Dao
interface ExerciseAuditDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuditLog(log: ExerciseAuditLogEntity)

    @Query("SELECT * FROM exercise_audit_logs ORDER BY timestamp DESC")
    suspend fun getAllAuditLogs(): List<ExerciseAuditLogEntity>

    @Query("SELECT * FROM exercise_audit_logs WHERE targetId = :exerciseId ORDER BY timestamp DESC")
    suspend fun getAuditLogsForExercise(exerciseId: String): List<ExerciseAuditLogEntity>
}
