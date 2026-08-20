package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.MaturityStatus

@Entity(
    tableName = "evolution_evidence_packages",
    indices = [
        Index("userId"),
        Index("generatedAt"),
        Index("isMock")
    ]
)
data class EvolutionEvidencePackageEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val generatedAt: Long,
    val coreVersion: String,
    val engineVersion: String,
    val evidenceIdsJson: String,
    val validMetricsJson: String,
    val invalidMetricsJson: String,
    val expiredEvidenceIdsJson: String,
    val pendingValidationItemsJson: String,
    val overallConsistencyStatus: ConsistencyStatus,
    val overallRepeatabilityStatus: String,
    val maturityStatus: MaturityStatus,
    val qualityMatrixJson: String,
    val limitationsJson: String,
    val auditReference: String,
    val isMock: Boolean,
    val simulationMode: Boolean
)
