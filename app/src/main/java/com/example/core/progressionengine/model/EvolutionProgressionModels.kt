package com.example.core.progressionengine.model

import com.example.core.datacore.model.IntegrityStatus

/**
 * PERFORMAI EVOLUTION PROGRESSION SYSTEM V1 - DOMAIN MODELS
 *
 * Modela a progressão longitudinal real do atleta.
 * Princípio fundamental: RESULTADO ISOLADO != EVOLUÇÃO
 * A progressão exige Performance + Evidence + Consistency + Maturity + Adaptation + Balance + Time + Trial (quando exigido).
 */

enum class EvolutionProgressionStatus {
    STABLE,
    IMPROVING,
    READY_FOR_EVALUATION,
    TRIAL_REQUIRED,
    TRIAL_PENDING,
    ELIGIBLE_FOR_PROMOTION,
    INSUFFICIENT_EVIDENCE,
    PENDING_VALIDATION,
    AT_RISK,
    REGRESSION_REVIEW
}

enum class DimensionTrajectoryTrend {
    IMPROVING,
    STABLE,
    VARIABLE,
    DECLINING,
    INSUFFICIENT_DATA,
    PENDING_VALIDATION
}

enum class AdaptationStatus {
    ADAPTING,
    ADAPTED,
    INSUFFICIENT_DATA,
    VARIABLE,
    DECLINING,
    PENDING_VALIDATION
}

enum class ClassMaintenanceStatus {
    MAINTAINED,
    AT_RISK,
    INSUFFICIENT_EVIDENCE,
    PENDING_VALIDATION,
    REVIEW_REQUIRED
}

enum class RegressionReviewStatus {
    NO_CONCERN,
    MONITOR,
    REVIEW_REQUIRED,
    PENDING_VALIDATION
}

enum class PromotionCandidateStatus {
    NOT_READY,
    UNDER_REVIEW,
    TRIAL_REQUIRED,
    READY,
    ELIGIBLE,
    BLOCKED,
    PENDING_VALIDATION
}

enum class CallerTier {
    CLIENT,
    AI_GATEWAY,
    SYSTEM,
    CORE_ENGINE,
    ADMIN
}

enum class ProgressionAnomalyType {
    RAPID_PROGRESSION,
    DATA_GAP,
    PROTOCOL_CHANGE,
    DEVICE_CHANGE,
    PERFORMANCE_SPIKE,
    INCONSISTENT_SEQUENCE,
    INSUFFICIENT_LONGITUDINAL_DATA
}

enum class AnomalySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class EvolutionProgressionState(
    val id: String,
    val userId: String,
    val currentClassId: String,
    val currentClassSince: Long,
    val highestEligibleClassId: String,
    val nextTargetClassId: String,
    val progressionStatus: EvolutionProgressionStatus,
    val progressionPhase: String,
    val lastAssessmentAt: Long,
    val methodologyVersion: String,
    val coreVersion: String,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false
)

data class DimensionTrajectory(
    val dimensionId: String,
    val initialValue: Double?,
    val currentValue: Double?,
    val historicalBest: Double?,
    val recentValue: Double?,
    val trend: DimensionTrajectoryTrend,
    val volatility: Double?,
    val consistencyScore: Double?,
    val observationCount: Int,
    val observationSpanDays: Long,
    val methodologyVersion: String,
    val explanation: String
)

data class ProgressionTimePolicy(
    val policyId: String,
    val classId: String,
    val minimumTimeInClassDays: Long,
    val minimumEvidenceSpanDays: Long,
    val minimumObservationCount: Int,
    val methodologyVersion: String = "1.0.0",
    val status: String = "ACTIVE"
)

data class EvolutionMomentum(
    val rateOfImprovement: Double? = null,
    val stabilityScore: Double? = null,
    val consistencyScore: Double? = null,
    val recoveryIndex: Double? = null,
    val adaptationIndex: Double? = null,
    val sustainabilityScore: Double? = null,
    val explanation: String,
    val methodologyStatus: String = "PENDING_VALIDATION"
)

data class AdaptationAssessment(
    val dimensionId: String,
    val baseline: Double?,
    val currentPerformance: Double?,
    val observationSpanDays: Long,
    val trainingExposure: String? = null,
    val responsePattern: String,
    val recoveryPattern: String? = null,
    val status: AdaptationStatus,
    val evidenceIds: List<String>,
    val methodologyVersion: String
)

data class PerformanceSustainabilityAssessment(
    val peakPerformance: Double?,
    val recentPerformance: Double?,
    val performanceVariance: Double?,
    val observationSpanDays: Long,
    val validObservationCount: Int,
    val isSustained: Boolean,
    val consistencyStatus: String,
    val explanation: String,
    val methodologyVersion: String
)

data class ClassMaintenanceAssessment(
    val currentClassId: String,
    val status: ClassMaintenanceStatus,
    val daysInClass: Long,
    val recentActivityCount: Int,
    val affectedDimensions: List<String>,
    val explanation: String,
    val calculatedAt: Long,
    val methodologyVersion: String
)

data class RegressionReview(
    val id: String,
    val userId: String,
    val currentClass: String,
    val affectedDimensions: List<String>,
    val evidenceIds: List<String>,
    val declineDurationDays: Long,
    val consistencyStatus: String,
    val methodologyVersion: String,
    val reviewStatus: RegressionReviewStatus,
    val notes: String = "Downgrade automático desativado por design no V1",
    val reviewedAt: Long
)

data class ProgressionAnomaly(
    val anomalyId: String,
    val userId: String,
    val type: ProgressionAnomalyType,
    val severity: AnomalySeverity,
    val evidenceIds: List<String>,
    val affectedSnapshots: List<String>,
    val detectedAt: Long,
    val status: String = "DETECTED",
    val explanation: String
)

data class ProgressionExplanation(
    val performanceSummary: String,
    val consistencySummary: String,
    val evidenceSummary: String,
    val maturitySummary: String,
    val adaptationSummary: String,
    val timeRequirementSummary: String,
    val trialSummary: String,
    val balanceSummary: String,
    val overallOutcome: String,
    val detailedBlockers: List<String>
)

data class PromotionCandidate(
    val id: String,
    val userId: String,
    val currentClassId: String,
    val targetClassId: String,
    val satisfiedRequirements: List<String>,
    val blockingRequirements: List<String>,
    val evidencePackageId: String?,
    val scoreSnapshotId: String?,
    val trialSnapshotId: String?,
    val progressionAssessmentId: String,
    val timePolicyResult: String,
    val consistencyResult: String,
    val maturityResult: String,
    val adaptationResult: String,
    val balanceResult: String,
    val status: PromotionCandidateStatus,
    val explanation: ProgressionExplanation,
    val methodologyVersion: String,
    val createdAt: Long,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false
)

data class EvolutionHistoryEntry(
    val id: String,
    val userId: String,
    val timestamp: Long,
    val previousClass: String,
    val newClass: String,
    val reason: String,
    val evidencePackageId: String?,
    val scoreSnapshotId: String?,
    val trialSnapshotId: String?,
    val progressionAssessmentId: String,
    val policyVersion: String,
    val methodologyVersion: String,
    val auditReference: String,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false
)

data class ProgressionAssessmentSnapshot(
    val id: String,
    val userId: String,
    val currentClassId: String,
    val targetClassId: String,
    val progressionState: EvolutionProgressionState,
    val candidate: PromotionCandidate,
    val trajectories: Map<String, DimensionTrajectory>,
    val sustainability: PerformanceSustainabilityAssessment,
    val maintenance: ClassMaintenanceAssessment,
    val regressionReview: RegressionReview?,
    val anomalies: List<ProgressionAnomaly>,
    val calculatedAt: Long,
    val coreVersion: String,
    val auditReference: String,
    val isMock: Boolean,
    val simulationMode: Boolean
)
