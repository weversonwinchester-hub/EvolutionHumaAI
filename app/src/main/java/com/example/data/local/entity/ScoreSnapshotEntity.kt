package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.scoreengine.model.CalculationStatus

@Entity(
    tableName = "score_snapshots",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["assessmentId"]),
        Index(value = ["calculatedAt"]),
        Index(value = ["isMock"])
    ]
)
data class ScoreSnapshotEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val assessmentId: String?,
    val scoreVersion: String,
    val coreVersion: String,
    val calculatedAt: Long,
    val performanceIndexValue: Double?,
    val performanceIndexStatus: CalculationStatus,
    val dimensionScoresJson: String,
    val evidenceIdsJson: String,
    val metricIdsJson: String,
    val calculationStatus: CalculationStatus,
    val confidenceMetadataJson: String,
    val isMock: Boolean,
    val provenanceId: String?,
    val overallExplanationJson: String
)
