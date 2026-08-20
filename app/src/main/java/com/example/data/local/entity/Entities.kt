package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.AssessmentStatus
import com.example.core.model.AssessmentType
import com.example.core.model.AuditSeverity
import com.example.core.model.EvidenceType
import com.example.core.model.GoalCategory
import com.example.core.model.GoalStatus
import com.example.core.model.MissionStatus
import com.example.core.model.ProfileStatus
import com.example.core.model.TrialStatus
import com.example.core.model.UserRole

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean
)

@Entity(tableName = "profiles", indices = [Index(value = ["userId"], unique = true)])
data class ProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val fullName: String,
    val nickname: String,
    val dateOfBirth: String,
    val gender: String,
    val heightCm: Double,
    val weightKg: Double,
    val status: ProfileStatus,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "goals", indices = [Index(value = ["userId"])])
data class GoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: GoalCategory,
    val targetValue: String,
    val targetDate: Long?,
    val status: GoalStatus,
    val createdAt: Long
)

@Entity(tableName = "assessments", indices = [Index(value = ["userId"])])
data class AssessmentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val assessmentType: AssessmentType,
    val status: AssessmentStatus,
    val summary: String?,
    val requestedAt: Long,
    val conductedAt: Long?,
    val engineVersion: String
)

@Entity(tableName = "measurements", indices = [Index(value = ["userId"]), Index(value = ["assessmentId"])])
data class MeasurementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val assessmentId: String?,
    val metricType: String,
    val rawValue: Double,
    val unit: String,
    val source: String,
    val recordedAt: Long,
    val signature: String?
)

@Entity(tableName = "evidences", indices = [Index(value = ["userId"]), Index(value = ["referenceId"])])
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val referenceType: String,
    val referenceId: String,
    val evidenceType: EvidenceType,
    val dataHash: String,
    val uriOrLocation: String,
    val verified: Boolean,
    val submittedAt: Long
)

@Entity(tableName = "performance_states", indices = [Index(value = ["userId"], unique = true)])
data class PerformanceStateEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val readinessScore: Double?,
    val staminaIndex: Double?,
    val cognitiveLoad: Double?,
    val recoveryIndex: Double?,
    val calculatedAt: Long,
    val engineVersion: String
)

@Entity(tableName = "evolution_states", indices = [Index(value = ["userId"], unique = true)])
data class EvolutionStateEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val currentClass: String,
    val currentLevel: Int,
    val currentXp: Long,
    val requiredXpForNextLevel: Long,
    val rankStatus: String,
    val updatedAt: Long,
    val engineVersion: String
)

@Entity(tableName = "missions", indices = [Index(value = ["userId"])])
data class MissionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val code: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val status: MissionStatus,
    val tentativeXpReward: Long,
    val createdAt: Long
)

@Entity(tableName = "trials", indices = [Index(value = ["userId"])])
data class TrialEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val requirements: String,
    val status: TrialStatus,
    val evaluatedAt: Long?
)

// REGRA CRÍTICA: Histórico de auditoria imutável
@Entity(tableName = "audit_logs", indices = [Index(value = ["userId"]), Index(value = ["timestamp"])])
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val action: String,
    val resource: String,
    val detailsJson: String,
    val severity: AuditSeverity,
    val source: String,
    val timestamp: Long
)

@Entity(tableName = "ai_interactions", indices = [Index(value = ["userId"])])
data class AIInteractionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val promptContext: String,
    val suggestedAction: String,
    val confidence: Double,
    val processedByCore: Boolean,
    val appliedStateChange: Boolean,
    val timestamp: Long
)
