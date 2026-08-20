package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.ValidationStatus

@Entity(
    tableName = "raw_data_inputs",
    indices = [Index(value = ["userId"]), Index(value = ["metricId"]), Index(value = ["clientTimestamp"])]
)
data class RawDataInputEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val assessmentId: String?,
    val metricId: String,
    val rawPayload: String,
    val unit: String,
    val source: String,
    val sourceType: String,
    val sourceIdentifier: String,
    val deviceId: String?,
    val protocolId: String,
    val clientTimestamp: Long,
    val serverTimestamp: Long,
    val isMock: Boolean
)

@Entity(
    tableName = "provenance_records",
    indices = [Index(value = ["sourceIdentifier"]), Index(value = ["protocolId"])]
)
data class ProvenanceEntity(
    @PrimaryKey val id: String,
    val sourceType: String,
    val sourceIdentifier: String,
    val deviceIdentifier: String?,
    val captureTimestamp: Long,
    val processingTimestamp: Long,
    val processingVersion: String,
    val protocolId: String,
    val integrityHash: String?,
    val createdAt: Long
)

@Entity(
    tableName = "core_protocols",
    indices = [Index(value = ["category"])]
)
data class ProtocolEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val description: String,
    val category: String,
    val requiredInputsJson: String,
    val methodology: String,
    val minAllowed: Double,
    val maxAllowed: Double,
    val expectedUnit: String,
    val allowedUnitsJson: String,
    val minSamplingDurationSec: Int?,
    val minSamplingRateHz: Int?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "core_metrics",
    indices = [Index(value = ["category"])]
)
data class MetricEntity(
    @PrimaryKey val id: String,
    val name: String,
    val definition: String,
    val unit: String,
    val category: String,
    val calculationMethod: String,
    val protocolIdsJson: String,
    val evidenceRequirements: String,
    val explainabilityJson: String,
    val version: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "core_measurements",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["assessmentId"]),
        Index(value = ["metricId"]),
        Index(value = ["timestamp"])
    ]
)
data class CoreMeasurementEntity(
    @PrimaryKey val id: String,
    val assessmentId: String?,
    val userId: String,
    val metricId: String,
    val rawValue: Double,
    val normalizedValue: Double?,
    val unit: String,
    val timestamp: Long,
    val source: String,
    val deviceId: String?,
    val protocolId: String,
    val validationStatus: ValidationStatus,
    val rejectionReason: String?,
    val rawDataInputId: String?,
    val isMock: Boolean,
    val createdAt: Long
)

@Entity(
    tableName = "core_evidences",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["assessmentId"]),
        Index(value = ["provenanceId"])
    ]
)
data class CoreEvidenceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val assessmentId: String?,
    val measurementIdsJson: String,
    val source: String,
    val capturedAt: Long,
    val submittedAt: Long,
    val integrityStatus: IntegrityStatus,
    val reliabilityScore: Double?,
    val confidenceScore: Double?,
    val provenanceId: String,
    val coreVersion: String,
    val isMock: Boolean,
    val createdAt: Long
)

@Entity(
    tableName = "core_audit_trail",
    indices = [
        Index(value = ["actorId"]),
        Index(value = ["entityType"]),
        Index(value = ["entityId"]),
        Index(value = ["timestamp"])
    ]
)
data class CoreAuditLogEntity(
    @PrimaryKey val id: String,
    val actorType: ActorType,
    val actorId: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val previousState: String?,
    val newState: String?,
    val timestamp: Long,
    val requestId: String,
    val systemVersion: String
)
