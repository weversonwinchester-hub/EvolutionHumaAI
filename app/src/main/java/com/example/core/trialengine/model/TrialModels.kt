package com.example.core.trialengine.model

import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.SourceTier

/**
 * PERFORMAI TRIAL ENGINE V1 - MODELOS DE DOMÍNIO
 *
 * O Trial é uma Prova Controlada de Desempenho.
 * O motor é genérico, reproduzível, versionado, auditável e imutável.
 */

enum class TrialSessionStatus {
    CREATED,
    READY,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    INVALIDATED,
    EXPIRED,
    CANCELLED,
    SIMULATION
}

enum class TrialAttemptValidationStatus {
    PENDING,
    VALID,
    INVALID,
    DISQUALIFIED,
    PROTOCOL_VIOLATION,
    DEVICE_VIOLATION
}

enum class TrialResultStatus {
    QUALIFIED,
    NOT_QUALIFIED,
    INSUFFICIENT_EVIDENCE,
    INVALID,
    PENDING_VALIDATION
}

enum class TrialScoringMethod {
    BEST_ATTEMPT,
    MEAN_ATTEMPTS,
    MEDIAN_ATTEMPTS,
    CONSISTENCY_ATTEMPTS,
    MINIMUM_ATTEMPT,
    MAXIMUM_ATTEMPT,
    ALL_QUALIFIED
}

enum class TrialCategory {
    NEUROMUSCULAR,
    CARDIORESPIRATORY,
    BIOMECHANICAL,
    POWER_ENDURANCE,
    MAXIMUM_STRENGTH,
    REACTION_AGILITY
}

enum class TrialAbortReason {
    SENSOR_FAILURE,
    INTEGRITY_VIOLATION,
    PROTOCOL_VIOLATION,
    SESSION_EXPIRED,
    SAFETY_HAZARD,
    DATA_LOSS,
    DEVICE_DISCONNECTED,
    DEVICE_CHANGED,
    USER_ABORTED
}

enum class DeviceIntegrityStatus {
    VERIFIED,
    UNVERIFIED,
    DEVICE_CHANGED,
    DEVICE_UNVERIFIED,
    DISCONNECTED,
    CALIBRATION_REQUIRED
}

data class RestPeriodRecord(
    val restStartedAt: Long,
    val restCompletedAt: Long?,
    val requiredRestSeconds: Int,
    val actualRestSeconds: Long,
    val isValid: Boolean
)

data class FatigueDegradationAnalysis(
    val velocityDropPercentage: Double? = null,
    val powerDropPercentage: Double? = null,
    val techniqueAlterationScore: Double? = null,
    val asymmetryIncreasePercentage: Double? = null,
    val stabilityLossScore: Double? = null,
    val motorPatternChangeDetected: Boolean = false,
    val methodologyStatus: String = "PENDING_VALIDATION",
    val notes: String = "Modelo de fadiga estruturado para validação científica futura"
)

data class TrialPolicy(
    val trialPolicyId: String,
    val classId: String,
    val version: String,
    val name: String,
    val description: String,
    val category: TrialCategory,
    val requiredEvidenceTypes: List<String>,
    val protocolId: String,
    val allowedDevices: List<String>,
    val minimumAttempts: Int = 1,
    val maximumAttempts: Int = 3,
    val restPeriodSeconds: Int = 180,
    val executionWindowSeconds: Int = 3600,
    val validityWindowDays: Int = 90,
    val safetyRequirements: List<String> = emptyList(),
    val scoringMethod: TrialScoringMethod = TrialScoringMethod.BEST_ATTEMPT,
    val thresholdValue: Double? = null, // null quando não homologado
    val thresholdUnit: String? = null,
    val methodologyStatus: String = "PENDING_VALIDATION",
    val status: String = "ACTIVE"
)

data class TrialSession(
    val id: String,
    val userId: String,
    val classId: String,
    val trialPolicyId: String,
    val policyVersion: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val status: TrialSessionStatus,
    val attemptCount: Int = 0,
    val deviceId: String,
    val protocolId: String,
    val sessionIntegrity: IntegrityStatus = IntegrityStatus.VALID,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false,
    val auditReference: String
)

data class TrialAttempt(
    val id: String,
    val sessionId: String,
    val attemptNumber: Int,
    val startedAt: Long,
    val completedAt: Long?,
    val rawEvidenceIds: List<String>,
    val measurementIds: List<String>,
    val resultValue: Double?,
    val unit: String?,
    val validationStatus: TrialAttemptValidationStatus,
    val invalidationReason: String? = null,
    val integrityHash: String,
    val deviceId: String,
    val protocolId: String,
    val restPeriodBeforeAttempt: RestPeriodRecord? = null,
    val createdAt: Long
)

data class TrialResult(
    val id: String,
    val sessionId: String,
    val userId: String,
    val classId: String,
    val bestAttemptId: String?,
    val qualifyingAttempts: List<String>,
    val failedAttempts: List<String>,
    val metricResults: Map<String, Double>,
    val evidenceIds: List<String>,
    val protocolVersion: String,
    val trialPolicyVersion: String,
    val methodologyVersion: String,
    val resultStatus: TrialResultStatus,
    val explanation: String,
    val limitations: List<String>,
    val fatigueAnalysis: FatigueDegradationAnalysis? = null,
    val calculatedAt: Long,
    val auditReference: String,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false
)

data class TrialSnapshot(
    val id: String,
    val sessionId: String,
    val userId: String,
    val classId: String,
    val trialPolicyId: String,
    val trialPolicyVersion: String,
    val result: TrialResult,
    val attempts: List<TrialAttempt>,
    val sessionIntegrity: IntegrityStatus,
    val calculatedAt: Long,
    val coreVersion: String,
    val auditReference: String,
    val isMock: Boolean,
    val simulationMode: Boolean
)
