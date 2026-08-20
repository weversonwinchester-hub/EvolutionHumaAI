package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.evolutionengine.model.ClassEligibilityStatus

@Entity(
    tableName = "evolution_snapshots",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["evaluatedClass"]),
        Index(value = ["evaluatedAt"]),
        Index(value = ["isMock"])
    ]
)
data class EvolutionSnapshotEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val currentClass: String,
    val evaluatedClass: String,
    val eligibilityStatus: ClassEligibilityStatus,
    val eligibilityResultJson: String,
    val requirementResultsJson: String,
    val evidencePackageId: String?,
    val scoreSnapshotId: String?,
    val policyVersion: String,
    val coreVersion: String,
    val evaluatedAt: Long,
    val auditReference: String,
    val isMock: Boolean,
    val simulationMode: Boolean
)
