package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.progressionengine.model.EvolutionProgressionStatus
import com.example.core.progressionengine.model.PromotionCandidateStatus
import com.example.core.progressionengine.model.ProgressionAnomalyType
import com.example.core.progressionengine.model.AnomalySeverity

/**
 * PERFORMAI PROGRESSION ENGINE V1 - ROOM ENTITIES
 */

@Entity(tableName = "evolution_progression_states")
data class EvolutionProgressionStateEntity(
    @PrimaryKey
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

@Entity(tableName = "promotion_candidates")
data class PromotionCandidateEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val currentClassId: String,
    val targetClassId: String,
    val satisfiedRequirementsJson: String,
    val blockingRequirementsJson: String,
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
    val overallOutcome: String,
    val methodologyVersion: String,
    val createdAt: Long,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false
)

@Entity(tableName = "progression_assessment_snapshots")
data class ProgressionAssessmentSnapshotEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val currentClassId: String,
    val targetClassId: String,
    val progressionStatus: EvolutionProgressionStatus,
    val candidateStatus: PromotionCandidateStatus,
    val trajectoriesJson: String,
    val sustainabilityJson: String,
    val maintenanceJson: String,
    val regressionReviewJson: String?,
    val anomaliesJson: String,
    val calculatedAt: Long,
    val coreVersion: String,
    val auditReference: String,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false
)

@Entity(tableName = "evolution_history_entries")
data class EvolutionHistoryEntity(
    @PrimaryKey
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

@Entity(tableName = "progression_anomalies")
data class ProgressionAnomalyEntity(
    @PrimaryKey
    val anomalyId: String,
    val userId: String,
    val type: ProgressionAnomalyType,
    val severity: AnomalySeverity,
    val evidenceIdsJson: String,
    val affectedSnapshotsJson: String,
    val detectedAt: Long,
    val status: String,
    val explanation: String
)
