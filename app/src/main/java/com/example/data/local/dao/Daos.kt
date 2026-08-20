package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AIInteractionEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.EvidenceEntity
import com.example.data.local.entity.EvolutionStateEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MeasurementEntity
import com.example.data.local.entity.MissionEntity
import com.example.data.local.entity.PerformanceStateEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.TrialEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    fun getProfileFlow(userId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfileByUserId(userId: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ProfileEntity)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsFlow(userId: String): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)
}

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getAssessmentsFlow(userId: String): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE userId = :userId AND assessmentType = 'INITIAL_FOUNDATION' LIMIT 1")
    fun getInitialAssessmentFlow(userId: String): Flow<AssessmentEntity?>

    @Query("SELECT * FROM assessments WHERE userId = :userId AND assessmentType = 'INITIAL_FOUNDATION' LIMIT 1")
    suspend fun getInitialAssessment(userId: String): AssessmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity)

    @Update
    suspend fun updateAssessment(assessment: AssessmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity)

    @Query("SELECT * FROM measurements WHERE userId = :userId ORDER BY recordedAt DESC")
    fun getMeasurementsFlow(userId: String): Flow<List<MeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceEntity)

    @Query("SELECT * FROM evidences WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getEvidencesFlow(userId: String): Flow<List<EvidenceEntity>>
}

@Dao
interface EvolutionDao {
    @Query("SELECT * FROM evolution_states WHERE userId = :userId LIMIT 1")
    fun getEvolutionStateFlow(userId: String): Flow<EvolutionStateEntity?>

    @Query("SELECT * FROM evolution_states WHERE userId = :userId LIMIT 1")
    suspend fun getEvolutionState(userId: String): EvolutionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEvolutionState(state: EvolutionStateEntity)

    @Query("SELECT * FROM performance_states WHERE userId = :userId LIMIT 1")
    fun getPerformanceStateFlow(userId: String): Flow<PerformanceStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePerformanceState(state: PerformanceStateEntity)
}

@Dao
interface MissionTrialDao {
    @Query("SELECT * FROM missions WHERE userId = :userId ORDER BY createdAt ASC")
    fun getMissionsFlow(userId: String): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity)

    @Query("SELECT * FROM trials WHERE userId = :userId")
    fun getTrialsFlow(userId: String): Flow<List<TrialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrial(trial: TrialEntity)
}

/**
 * AuditDao: REGRA CRÍTICA DE SEGURANÇA
 * Apenas inserções e leituras de logs são permitidas.
 * NUNCA fornecer métodos de UPDATE ou DELETE para a tabela de auditoria.
 */
@Dao
interface AuditDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAuditLogsFlow(limit: Int = 100): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserAuditLogsFlow(userId: String): Flow<List<AuditLogEntity>>
}

@Dao
interface AIGatewayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIInteraction(interaction: AIInteractionEntity)

    @Query("SELECT * FROM ai_interactions WHERE userId = :userId ORDER BY timestamp DESC LIMIT 20")
    fun getAIInteractionsFlow(userId: String): Flow<List<AIInteractionEntity>>
}
