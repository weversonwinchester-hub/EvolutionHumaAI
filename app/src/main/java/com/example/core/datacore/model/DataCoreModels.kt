package com.example.core.datacore.model

import java.util.UUID

/**
 * PERFORMAI DATA CORE V1 - Domain Entities
 *
 * Implements the core pipeline:
 * INPUT -> RAW DATA -> VALIDATION -> PROTOCOL -> MEASUREMENT -> METRIC -> EVIDENCE -> PROVENANCE -> RELIABILITY -> AUDIT
 */

// ==========================================
// 1. USER & PROFILE
// ==========================================
enum class UserStatus {
    ACTIVE,
    PENDING,
    SUSPENDED,
    ARCHIVED
}

data class DataCoreUser(
    val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: UserStatus = UserStatus.ACTIVE
)

data class DataCoreProfile(
    val id: String,
    val userId: String,
    val displayName: String,
    val birthYear: Int? = null, // Only collected when required by protocol (e.g. age-adjusted VO2)
    val biologicalContext: Map<String, String>? = null, // Lean context: only necessary biological variables
    val preferences: Map<String, String>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 2. GOAL
// ==========================================
enum class DataCoreGoalType {
    VO2_AEROBIC_CAPACITY,
    RELATIVE_STRENGTH,
    RATE_OF_FORCE_DEVELOPMENT,
    AUTONOMIC_RECOVERY,
    CRITICAL_POWER_THRESHOLD,
    JOINT_STABILITY_SYMMETRY,
    GENERAL_PERFORMANCE
}

enum class DataCoreGoalStatus {
    DRAFT,
    ACTIVE,
    ACHIEVED,
    PAUSED,
    ARCHIVED
}

data class DataCoreGoal(
    val id: String,
    val userId: String,
    val type: DataCoreGoalType,
    val description: String,
    val priority: Int = 1, // 1 (High) to 5 (Low)
    val status: DataCoreGoalStatus = DataCoreGoalStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 3. PROTOCOL & METHODOLOGY
// ==========================================
data class ProtocolValidityRange(
    val minAllowed: Double,
    val maxAllowed: Double,
    val expectedUnit: String,
    val description: String = ""
)

data class ProtocolValidityRules(
    val allowedUnits: List<String>,
    val valueRange: ProtocolValidityRange,
    val minSamplingDurationSeconds: Int? = null,
    val minSamplingRateHz: Int? = null,
    val maxClockSkewToleranceMs: Long = 60_000L, // 60s max clock skew
    val maxDataAgeMs: Long = 90L * 24 * 60 * 60 * 1000 // 90 days max past age
)

data class DataCoreProtocol(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val category: String,
    val requiredInputs: List<String>,
    val methodology: String,
    val validityRules: ProtocolValidityRules,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 4. RAW DATA INGESTION
// ==========================================
data class RawDataInput(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val assessmentId: String? = null,
    val metricId: String,
    val rawPayload: String, // Exact payload as received from sensor/input
    val unit: String,
    val source: String,
    val sourceType: String, // e.g. SENSOR_BLUETOOTH, WEARABLE_OPTICAL, VIDEO_TELEMETRY, MANUAL_INPUT
    val sourceIdentifier: String,
    val deviceId: String? = null,
    val protocolId: String,
    val clientTimestamp: Long,
    val serverTimestamp: Long = System.currentTimeMillis(),
    val isMock: Boolean = false
)

// ==========================================
// 5. MEASUREMENT
// ==========================================
enum class ValidationStatus {
    VALID,
    INVALID,
    REJECTED,
    PENDING
}

data class DataCoreMeasurement(
    val id: String,
    val assessmentId: String?,
    val userId: String,
    val metricId: String,
    val rawValue: Double,
    val normalizedValue: Double? = null, // Scaled or adjusted if calibrated
    val unit: String,
    val timestamp: Long,
    val source: String,
    val deviceId: String? = null,
    val protocolId: String,
    val validationStatus: ValidationStatus,
    val rejectionReason: String? = null,
    val rawDataInputId: String? = null,
    val isMock: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// 6. METRIC & EXPLAINABILITY
// ==========================================
data class MetricExplainability(
    val whatIsIt: String, // O que é?
    val howIsMeasured: String, // Como é medida?
    val unit: String, // Qual unidade?
    val protocol: String, // Qual protocolo?
    val instrumentOrSensor: String, // Qual instrumento ou sensor?
    val source: String, // Qual fonte?
    val howIsCalculated: String, // Como foi calculada?
    val methodVersion: String, // Qual versão do método?
    val evidenceRequirement: String, // Qual evidência sustenta o valor?
    val physiologicalBasis: String = "",
    val performanceImpact: String = ""
)

data class DataCoreMetric(
    val id: String,
    val name: String,
    val definition: String,
    val unit: String,
    val category: String,
    val calculationMethod: String,
    val protocolIds: List<String>,
    val evidenceRequirements: String,
    val explainability: MetricExplainability,
    val version: String = "1.0.0",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 7. PROVENANCE
// ==========================================
data class DataCoreProvenance(
    val id: String,
    val sourceType: String,
    val sourceIdentifier: String,
    val deviceIdentifier: String? = null,
    val captureTimestamp: Long,
    val processingTimestamp: Long,
    val processingVersion: String,
    val protocolId: String,
    val integrityHash: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// 8. RELIABILITY FRAMEWORK
// ==========================================
enum class SourceTier {
    TIER_1_DIRECT_SENSOR,      // e.g. Linear Position Transducer 1000Hz, Dual Force Plate
    TIER_2_CLINICAL_WEARABLE,   // e.g. Chest strap ECG Polar H10, VO2 metabolic cart
    TIER_3_CONSUMER_OPTICAL,   // e.g. PPG optical watch
    TIER_4_VIDEO_CV_ESTIMATE,  // e.g. Computer vision kinematics
    TIER_5_MANUAL_INPUT        // e.g. Self-reported manual log
}

data class ReliabilityAssessment(
    val sourceTier: SourceTier,
    val sourceQualityScore: Double, // 0.0 - 1.0
    val measurementConsistency: Double, // 0.0 - 1.0
    val integrityValid: Boolean,
    val repeatabilityFactor: Double, // 0.0 - 1.0
    val compositeConfidenceScore: Double, // 0.0 - 1.0 (Framework indicator, not definitive science formula)
    val isPeerAudited: Boolean = false,
    val notes: String = ""
)

// ==========================================
// 9. EVIDENCE
// ==========================================
enum class IntegrityStatus {
    VALID,
    INVALID,
    UNKNOWN,
    TAMPERED
}

data class DataCoreEvidence(
    val id: String, // e.g. "EV-2026-000001"
    val userId: String,
    val assessmentId: String?,
    val measurementIds: List<String>,
    val source: String,
    val capturedAt: Long,
    val submittedAt: Long,
    val integrityStatus: IntegrityStatus,
    val reliabilityScore: Double?,
    val confidenceScore: Double?,
    val provenanceId: String,
    val coreVersion: String = "1.0.0-datacore",
    val isMock: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// 10. ASSESSMENT
// ==========================================
enum class DataCoreAssessmentStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PENDING_VALIDATION,
    COMPLETED,
    REJECTED
}

data class DataCoreAssessment(
    val id: String,
    val userId: String,
    val protocolId: String,
    val status: DataCoreAssessmentStatus = DataCoreAssessmentStatus.NOT_STARTED,
    val startedAt: Long,
    val completedAt: Long? = null,
    val coreVersion: String = "1.0.0-datacore",
    val isMock: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// 11. AUDIT LOG (IMMUTABLE)
// ==========================================
enum class ActorType {
    USER,
    CLIENT,
    SYSTEM,
    CORE_ENGINE,
    ADMIN,
    AI_GATEWAY,
    SENSOR_BRIDGE
}

data class DataCoreAuditLog(
    val id: String,
    val actorType: ActorType,
    val actorId: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val previousState: String? = null,
    val newState: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val requestId: String = UUID.randomUUID().toString(),
    val systemVersion: String = "1.0.0-datacore"
)
