package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.trialengine.model.TrialAttemptValidationStatus
import com.example.core.trialengine.model.TrialResultStatus
import com.example.core.trialengine.model.TrialSessionStatus

@Entity(
    tableName = "trial_sessions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["classId"]),
        Index(value = ["trialPolicyId"]),
        Index(value = ["status"]),
        Index(value = ["startedAt"])
    ]
)
data class TrialSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val classId: String,
    val trialPolicyId: String,
    val policyVersion: String,
    val startedAt: Long,
    val completedAt: Long?,
    val status: TrialSessionStatus,
    val attemptCount: Int,
    val deviceId: String,
    val protocolId: String,
    val sessionIntegrity: IntegrityStatus,
    val isMock: Boolean,
    val simulationMode: Boolean,
    val auditReference: String
)

@Entity(
    tableName = "trial_attempts",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["attemptNumber"]),
        Index(value = ["validationStatus"])
    ]
)
data class TrialAttemptEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val attemptNumber: Int,
    val startedAt: Long,
    val completedAt: Long?,
    val rawEvidenceIdsJson: String,
    val measurementIdsJson: String,
    val resultValue: Double?,
    val unit: String?,
    val validationStatus: TrialAttemptValidationStatus,
    val invalidationReason: String?,
    val integrityHash: String,
    val deviceId: String,
    val protocolId: String,
    val restPeriodSeconds: Long,
    val createdAt: Long
)

@Entity(
    tableName = "trial_snapshots",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["userId"]),
        Index(value = ["classId"]),
        Index(value = ["resultStatus"]),
        Index(value = ["calculatedAt"])
    ]
)
data class TrialSnapshotEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val userId: String,
    val classId: String,
    val trialPolicyId: String,
    val trialPolicyVersion: String,
    val resultStatus: TrialResultStatus,
    val bestAttemptId: String?,
    val qualifyingAttemptsJson: String,
    val failedAttemptsJson: String,
    val metricResultsJson: String,
    val evidenceIdsJson: String,
    val explanation: String,
    val limitationsJson: String,
    val sessionIntegrity: IntegrityStatus,
    val calculatedAt: Long,
    val coreVersion: String,
    val auditReference: String,
    val isMock: Boolean,
    val simulationMode: Boolean
)
