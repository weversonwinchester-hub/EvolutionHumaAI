package com.example.core.model

data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

enum class ProfileStatus {
    INCOMPLETE,
    PENDING_INITIAL_ASSESSMENT,
    ACTIVE,
    SUSPENDED
}

data class Profile(
    val id: String,
    val userId: String,
    val fullName: String,
    val nickname: String,
    val dateOfBirth: String,
    val gender: String,
    val heightCm: Double,
    val weightKg: Double,
    val status: ProfileStatus = ProfileStatus.INCOMPLETE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class GoalCategory {
    BIOMECHANICAL,
    PHYSIOLOGICAL,
    NEURO_COGNITIVE,
    DISCIPLINE
}

enum class GoalStatus {
    DRAFT,
    ACTIVE,
    COMPLETED,
    ARCHIVED
}

data class Goal(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: GoalCategory,
    val targetValue: String,
    val targetDate: Long?,
    val status: GoalStatus = GoalStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AssessmentType {
    INITIAL_FOUNDATION,
    PERIODIC_CHECKPOINT,
    TRIAL_QUALIFICATION
}

enum class AssessmentStatus {
    NOT_STARTED,
    PENDING_EVALUATION,
    IN_PROGRESS,
    COMPLETED,
    REJECTED
}

data class Assessment(
    val id: String,
    val userId: String,
    val assessmentType: AssessmentType = AssessmentType.INITIAL_FOUNDATION,
    val status: AssessmentStatus = AssessmentStatus.NOT_STARTED,
    val summary: String? = null,
    val requestedAt: Long = System.currentTimeMillis(),
    val conductedAt: Long? = null,
    val engineVersion: String = "1.0.0-foundation"
)

data class Measurement(
    val id: String,
    val userId: String,
    val assessmentId: String?,
    val metricType: String,
    val rawValue: Double,
    val unit: String,
    val source: String,
    val recordedAt: Long = System.currentTimeMillis(),
    val signature: String? = null
)

enum class EvidenceType {
    BIOMETRIC_DATA,
    VIDEO_RECORDING,
    SENSOR_LOG,
    DOC_VERIFICATION
}

data class Evidence(
    val id: String,
    val userId: String,
    val referenceType: String,
    val referenceId: String,
    val evidenceType: EvidenceType,
    val dataHash: String,
    val uriOrLocation: String,
    val verified: Boolean = false,
    val submittedAt: Long = System.currentTimeMillis()
)

data class PerformanceState(
    val id: String,
    val userId: String,
    val readinessScore: Double? = null,
    val staminaIndex: Double? = null,
    val cognitiveLoad: Double? = null,
    val recoveryIndex: Double? = null,
    val calculatedAt: Long = System.currentTimeMillis(),
    val engineVersion: String = "1.0.0-foundation"
)

// MANDATE: The initial evolutionary class MUST be "Corpo Adormecido"
const val INITIAL_EVOLUTION_CLASS = "Corpo Adormecido"

data class EvolutionState(
    val id: String,
    val userId: String,
    val currentClass: String = INITIAL_EVOLUTION_CLASS,
    val currentLevel: Int = 1,
    val currentXp: Long = 0L,
    val requiredXpForNextLevel: Long = 1000L,
    val rankStatus: String = "Iniciante Não Avaliado",
    val updatedAt: Long = System.currentTimeMillis(),
    val engineVersion: String = "1.0.0-foundation"
)

enum class MissionStatus {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    SUBMITTED_FOR_REVIEW,
    COMPLETED,
    FAILED
}

data class Mission(
    val id: String,
    val userId: String,
    val code: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val status: MissionStatus = MissionStatus.LOCKED,
    val tentativeXpReward: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TrialStatus {
    LOCKED,
    AVAILABLE,
    ACTIVE,
    PASSED,
    FAILED
}

data class Trial(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val requirements: String,
    val status: TrialStatus = TrialStatus.LOCKED,
    val evaluatedAt: Long? = null
)

enum class AuditSeverity {
    INFO,
    WARNING,
    SECURITY_VIOLATION,
    CRITICAL
}

data class AuditLog(
    val id: String,
    val userId: String?,
    val action: String,
    val resource: String,
    val detailsJson: String,
    val severity: AuditSeverity = AuditSeverity.INFO,
    val source: String = "SystemCore",
    val timestamp: Long = System.currentTimeMillis()
)

data class AIInteraction(
    val id: String,
    val userId: String,
    val promptContext: String,
    val suggestedAction: String,
    val confidence: Double,
    val processedByCore: Boolean = false,
    val appliedStateChange: Boolean = false, // AI CANNOT directly mutate official state
    val timestamp: Long = System.currentTimeMillis()
)
