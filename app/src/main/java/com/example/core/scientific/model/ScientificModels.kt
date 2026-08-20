package com.example.core.scientific.model

/**
 * PERFORMAI SCIENTIFIC METHODOLOGY & PROTOCOL REGISTRY V1 - DOMAIN MODELS
 *
 * Autoridade metodológica e científica do PERFORMAI.
 * Princípio central: O PERFORMAI nunca transforma um número em "científico"
 * apenas porque possui casas decimais.
 * Cada medição deve possuir: Métrica + Método + Protocolo + Instrumento +
 * Unidade + Condições + Qualidade + Proveniência + Versão Metodológica.
 */

enum class MethodologyValidationStatus {
    DRAFT,
    PENDING_REVIEW,
    VALIDATED,
    ACTIVE,
    DEPRECATED,
    REJECTED
}

enum class ProtocolValidationStatus {
    DRAFT,
    PENDING_VALIDATION,
    ACTIVE,
    DEPRECATED,
    INVALID
}

enum class InstrumentValidationStatus {
    VALIDATED,
    PENDING_VALIDATION,
    DEPRECATED,
    UNAPPROVED
}

enum class UncertaintyValidationStatus {
    VALIDATED,
    PENDING_VALIDATION
}

enum class EvidenceLevel {
    EVIDENCE_LEVEL_UNSPECIFIED,
    EVIDENCE_LEVEL_LOW,
    EVIDENCE_LEVEL_MODERATE,
    EVIDENCE_LEVEL_HIGH,
    EVIDENCE_LEVEL_VERY_HIGH
}

enum class MeasurementClassification {
    DIRECT,
    DERIVED,
    ESTIMATED,
    PENDING_VALIDATION
}

enum class DeviationSeverity {
    NONE,
    MINOR,
    MAJOR,
    INVALIDATING,
    PENDING_VALIDATION
}

enum class QualityGateStatus {
    ACCEPTED,
    REJECTED,
    INSUFFICIENT_QUALITY,
    PENDING_VALIDATION
}

enum class NormalizationType {
    ABSOLUTE,
    RELATIVE,
    PERCENTILE,
    Z_SCORE,
    TARGET_RANGE,
    SPORT_SPECIFIC,
    POPULATION_SPECIFIC
}

enum class ScientificCallerTier {
    CLIENT,
    AI_GATEWAY,
    SYSTEM,
    CORE_ENGINE,
    ADMIN
}

data class MethodologySource(
    val sourceId: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int?,
    val publicationType: String, // JOURNAL, BOOK, CONSENSUS_STATEMENT, CLINICAL_GUIDELINE, THESIS, TECHNICAL_REPORT
    val identifier: String?, // DOI, PMID, ISBN
    val url: String? = null,
    val sourceAuthority: String,
    val sourceStatus: String = "VALIDATED", // VALIDATED, PENDING_VALIDATION
    val accessedAt: Long? = null,
    val notes: String? = null
)

data class ScientificMethodology(
    val methodologyId: String,
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
    val sourceReferences: List<MethodologySource>,
    val limitations: List<String>,
    val effectiveFrom: Long,
    val supersedesVersion: String? = null
)

data class ScientificProtocol(
    val protocolId: String,
    val name: String,
    val version: String,
    val methodologyId: String,
    val metricId: String,
    val purpose: String,
    val preparationRequirements: List<String>,
    val equipmentRequirements: List<String>,
    val executionSteps: List<String>,
    val samplingRate: Double?, // Hz
    val duration: Long?, // seconds
    val repetitions: Int?,
    val restInterval: Long?, // seconds
    val environmentalRequirements: List<String>,
    val exclusionCriteria: List<String>,
    val qualityRequirements: List<String>,
    val acceptedDevices: List<String>,
    val acceptedSources: List<String>,
    val validationStatus: ProtocolValidationStatus
)

data class MeasurementUncertainty(
    val metricId: String,
    val methodId: String,
    val uncertaintyType: String, // COMBINED_STANDARD, EXPANDED, INSTRUMENT_ERROR, BIOLOGICAL_VARIATION
    val value: Double?,
    val unit: String,
    val confidenceLevel: Double?, // e.g. 0.95 (95%)
    val source: String,
    val methodologyVersion: String,
    val validationStatus: UncertaintyValidationStatus = UncertaintyValidationStatus.PENDING_VALIDATION
)

data class MeasurementMethod(
    val methodId: String,
    val metricId: String,
    val methodologyId: String,
    val directOrDerived: MeasurementClassification,
    val formula: String?,
    val requiredInputs: List<String>,
    val units: String,
    val samplingRequirements: String?,
    val processingSteps: List<String>,
    val uncertainty: MeasurementUncertainty?,
    val validationStatus: MethodologyValidationStatus
)

data class MeasurementInstrument(
    val instrumentId: String,
    val manufacturer: String?,
    val model: String?,
    val instrumentType: String, // FORCE_PLATE, LINEAR_TRANSDUCER, HEART_RATE_SENSOR, METABOLIC_CART, CAMERA, IMU, OPTICAL_TIMING_GATE, CYCLE_ERGOMETER, GENERIC
    val sensorType: String?,
    val supportedMetrics: List<String>,
    val samplingRate: Double?, // Hz
    val calibrationRequirement: Boolean,
    val calibrationIntervalDays: Int?,
    val accuracySpecification: String?,
    val firmware: String?,
    val validationStatus: InstrumentValidationStatus
)

data class DeviceCapability(
    val deviceType: String, // SMARTPHONE, SMARTWATCH, IMU_SENSOR, CAMERA_OPTICAL, EXTERNAL_SENSOR, LAB_EQUIPMENT
    val supportedMetrics: List<String>,
    val captureMethods: List<String>,
    val minimumSamplingRate: Double,
    val qualityTier: String, // TIER_1_DIRECT_LAB, TIER_2_DEDICATED_SENSOR, TIER_3_WEARABLE, TIER_4_OPTICAL_MOBILE, TIER_5_MANUAL_ESTIMATION
    val limitations: List<String>,
    val validationStatus: MethodologyValidationStatus
)

data class VideoMeasurementMethod(
    val metricId: String,
    val cameraRequirements: List<String>,
    val minimumFrameRate: Int, // fps
    val resolutionRequirement: String, // e.g. 1080p@60fps
    val cameraPosition: String, // e.g. SAGITTAL_PLANE_ORTHOGONAL_2M
    val fieldOfView: String,
    val calibrationRequirement: Boolean,
    val bodyVisibilityRequirements: List<String>,
    val occlusionTolerance: String,
    val lightingRequirements: String,
    val confidenceRequirements: Double, // e.g. 0.85
    val validationStatus: MethodologyValidationStatus
)

data class SensorMeasurementMethod(
    val metricId: String,
    val sensorType: String,
    val placement: String,
    val calibration: String,
    val samplingRate: Double, // Hz
    val signalQuality: String,
    val preprocessing: List<String>,
    val artifactDetection: String,
    val validationStatus: MethodologyValidationStatus
)

data class MetricDefinition(
    val metricId: String,
    val name: String,
    val category: String,
    val whatItIs: String,
    val howItIsMeasured: String,
    val standardUnit: String,
    val acceptableUnits: List<String>,
    val primaryInstruments: List<String>,
    val referenceProtocols: List<String>,
    val calculationMethod: String,
    val captureFrequency: String,
    val requiredConditions: List<String>,
    val limitations: List<String>,
    val sources: List<MethodologySource>,
    val methodologyVersion: String,
    val evidenceLevel: EvidenceLevel,
    val validationStatus: MethodologyValidationStatus
)

data class EnvironmentalCondition(
    val temperatureCelsius: Double? = null,
    val humidityPercent: Double? = null,
    val altitudeMeters: Double? = null,
    val surface: String? = null,
    val equipmentCondition: String? = null,
    val lightingLux: Double? = null,
    val otherRelevantFactors: Map<String, String> = emptyMap()
)

data class ProtocolDeviation(
    val deviationId: String,
    val measurementId: String,
    val protocolId: String,
    val type: String, // SAMPLING_RATE_DEFICIT, REST_INTERVAL_SHORTENED, CALIBRATION_EXPIRED, ENVIRONMENT_OUT_OF_BOUNDS, POSITION_OFFSET, OTHER
    val severity: DeviationSeverity,
    val description: String,
    val impact: String,
    val status: String = "ACTIVE"
)

data class PopulationReference(
    val referenceId: String,
    val metricId: String,
    val populationDefinition: String,
    val ageRange: String?,
    val sexCategory: String?, // MALE, FEMALE, ALL, METRIC_INDEPENDENT
    val trainingStatus: String?, // SEDENTARY, RECREATIONAL, TRAINED, WELL_TRAINED, ELITE
    val sportContext: String?,
    val sampleSize: Int?,
    val percentileData: Map<String, Double>, // e.g. "p10" -> 35.0, "p50" -> 45.0, "p90" -> 58.0
    val source: String,
    val methodologyVersion: String,
    val validationStatus: MethodologyValidationStatus
)

data class NormalizationMethod(
    val normalizationId: String,
    val metricId: String,
    val type: NormalizationType,
    val formula: String?,
    val referenceId: String?,
    val validationStatus: MethodologyValidationStatus
)

data class MeasurementRepeatability(
    val metricId: String,
    val methodId: String,
    val testRetestReliability: Double?, // r or ICC
    val typicalError: Double?,
    val coefficientOfVariation: Double?, // % CV
    val intraclassCorrelation: Double?, // ICC
    val minimumDetectableChange: Double?, // MDC95
    val sampleSize: Int?,
    val validationStatus: MethodologyValidationStatus
)

data class MeasurementContext(
    val athleteAge: Int? = null,
    val athleteSex: String? = null,
    val bodyMassKg: Double? = null,
    val heightCm: Double? = null,
    val sportModality: String? = null,
    val trainingLevel: String? = null,
    val equipment: String? = null,
    val protocolId: String? = null,
    val environmentalCondition: EnvironmentalCondition? = null
)

data class QualityGateEvaluation(
    val evaluationId: String,
    val measurementId: String,
    val metricId: String,
    val protocolId: String,
    val instrumentId: String,
    val status: QualityGateStatus,
    val passedChecks: List<String>,
    val failedChecks: List<String>,
    val deviations: List<ProtocolDeviation>,
    val samplingRateObserved: Double?,
    val samplingRateRequired: Double?,
    val unitObserved: String,
    val unitExpected: String,
    val timestampValidity: Boolean,
    val calibrationValidity: Boolean,
    val signalIntegrity: Boolean,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false,
    val evaluatedAt: Long = System.currentTimeMillis(),
    val auditReference: String
)
