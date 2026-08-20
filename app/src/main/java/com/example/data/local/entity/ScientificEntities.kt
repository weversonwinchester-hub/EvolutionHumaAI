package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.scientific.model.*

/**
 * PERFORMAI SCIENTIFIC METHODOLOGY & PROTOCOL REGISTRY - ROOM ENTITIES
 */

@Entity(tableName = "scientific_methodologies")
data class ScientificMethodologyEntity(
    @PrimaryKey val methodologyId: String,
    val name: String,
    val description: String,
    val version: String,
    val metricId: String,
    val category: String,
    val measurementPrinciple: String,
    val calculationMethod: String,
    val acceptedUnits: List<String>,
    val requiredConditions: List<String>,
    val requiredEquipment: List<String>,
    val acceptableSources: List<String>,
    val validationStatus: MethodologyValidationStatus,
    val evidenceLevel: EvidenceLevel,
    val limitations: List<String>,
    val effectiveFrom: Long,
    val supersedesVersion: String? = null
)

@Entity(tableName = "scientific_protocols")
data class ScientificProtocolEntity(
    @PrimaryKey val protocolId: String,
    val name: String,
    val version: String,
    val methodologyId: String,
    val metricId: String,
    val purpose: String,
    val preparationRequirements: List<String>,
    val equipmentRequirements: List<String>,
    val executionSteps: List<String>,
    val samplingRate: Double?,
    val duration: Long?,
    val repetitions: Int?,
    val restInterval: Long?,
    val environmentalRequirements: List<String>,
    val exclusionCriteria: List<String>,
    val qualityRequirements: List<String>,
    val acceptedDevices: List<String>,
    val acceptedSources: List<String>,
    val validationStatus: ProtocolValidationStatus
)

@Entity(tableName = "measurement_instruments")
data class MeasurementInstrumentEntity(
    @PrimaryKey val instrumentId: String,
    val manufacturer: String?,
    val model: String?,
    val instrumentType: String,
    val sensorType: String?,
    val supportedMetrics: List<String>,
    val samplingRate: Double?,
    val calibrationRequirement: Boolean,
    val calibrationIntervalDays: Int?,
    val accuracySpecification: String?,
    val firmware: String?,
    val validationStatus: InstrumentValidationStatus
)

@Entity(tableName = "quality_gate_evaluations")
data class QualityGateEvaluationEntity(
    @PrimaryKey val evaluationId: String,
    val measurementId: String,
    val metricId: String,
    val protocolId: String,
    val instrumentId: String,
    val status: QualityGateStatus,
    val passedChecks: List<String>,
    val failedChecks: List<String>,
    val samplingRateObserved: Double?,
    val samplingRateRequired: Double?,
    val unitObserved: String,
    val unitExpected: String,
    val timestampValidity: Boolean,
    val calibrationValidity: Boolean,
    val signalIntegrity: Boolean,
    val isMock: Boolean,
    val simulationMode: Boolean,
    val evaluatedAt: Long,
    val auditReference: String
)

@Entity(tableName = "protocol_deviations")
data class ProtocolDeviationEntity(
    @PrimaryKey val deviationId: String,
    val measurementId: String,
    val protocolId: String,
    val type: String,
    val severity: DeviationSeverity,
    val description: String,
    val impact: String,
    val status: String
)

@Entity(tableName = "population_references")
data class PopulationReferenceEntity(
    @PrimaryKey val referenceId: String,
    val metricId: String,
    val populationDefinition: String,
    val ageRange: String?,
    val sexCategory: String?,
    val trainingStatus: String?,
    val sportContext: String?,
    val sampleSize: Int?,
    val percentileDataJson: String,
    val source: String,
    val methodologyVersion: String,
    val validationStatus: MethodologyValidationStatus
)

@Entity(tableName = "scientific_audit_logs")
data class ScientificAuditLogEntity(
    @PrimaryKey val auditId: String,
    val action: String,
    val targetEntity: String,
    val targetId: String,
    val callerTier: ScientificCallerTier,
    val callerId: String,
    val success: Boolean,
    val reason: String,
    val securityViolation: Boolean,
    val simulationMode: Boolean,
    val timestamp: Long,
    val checksum: String
)
