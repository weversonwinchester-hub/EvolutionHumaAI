package com.example.core.scoreengine.normalization

import com.example.core.datacore.metrics.MetricCatalog
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.scoreengine.model.CalculationStatus

enum class MetricDirection {
    HIGHER_IS_BETTER,
    LOWER_IS_BETTER,
    TARGET_OPTIMAL_RANGE
}

data class NormalizationSpec(
    val metricId: String,
    val expectedUnit: String,
    val direction: MetricDirection,
    val referenceMin: Double,
    val referenceMax: Double,
    val normalizationMethodName: String,
    val populationContext: String,
    val protocolId: String,
    val isMethodologyApproved: Boolean, // Se falso, retorna PENDING_VALIDATION
    val version: String
)

data class NormalizationResult(
    val rawValue: Double,
    val normalizedValue: Double?,
    val calculationStatus: CalculationStatus,
    val normalizationMethod: String,
    val populationContext: String,
    val formulaVersion: String,
    val validityRange: String,
    val notes: String
)

/**
 * ScoreNormalizationEngine: Camada oficial de normalização do Score Engine V1.
 *
 * REGRA MANDATÓRIA:
 * Não cria artificialmente percentuais ou scores de 0–100 apenas para preencher a interface.
 * Quando uma metodologia de normalização ainda não estiver formalmente aprovada:
 * calculationStatus = PENDING_VALIDATION e normalizedValue = null.
 */
object ScoreNormalizationEngine {

    private val SPECS: Map<String, NormalizationSpec> = listOf(
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_RELATIVE_STRENGTH,
            expectedUnit = "1RM/BW ratio",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 0.5,
            referenceMax = 3.0,
            normalizationMethodName = "Linear Scale to Athletic Reference Population (1RM/BW)",
            populationContext = "Adult Athletic Population (Male/Female Adjusted)",
            protocolId = "PROTO_LPT_RELATIVE_STRENGTH",
            isMethodologyApproved = false, // Em validação científica (Core V1)
            version = "NORM-RELSTR-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_RFD,
            expectedUnit = "N/s",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 500.0,
            referenceMax = 8000.0,
            normalizationMethodName = "IMTP 100ms Force Gradient Scaling",
            populationContext = "Competitive Athletes (Tier 1-2)",
            protocolId = "PROTO_IMTP_RFD",
            isMethodologyApproved = false,
            version = "NORM-RFD-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_VO2_MAX,
            expectedUnit = "ml/kg/min",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 20.0,
            referenceMax = 85.0,
            normalizationMethodName = "Astrand Submaximal Workload Extrapolation",
            populationContext = "General to Elite Endurance Population",
            protocolId = "PROTO_BASELINE_SUBMAX_VO2",
            isMethodologyApproved = false,
            version = "NORM-VO2-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_CRITICAL_POWER,
            expectedUnit = "W",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 100.0,
            referenceMax = 500.0,
            normalizationMethodName = "3-Minute All-Out Asymptotic Power Scaling",
            populationContext = "Cyclists / High-Output Endurance",
            protocolId = "PROTO_CRITICAL_POWER_3MIN_ALL_OUT",
            isMethodologyApproved = false,
            version = "NORM-CP-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_W_PRIME,
            expectedUnit = "kJ",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 5.0,
            referenceMax = 35.0,
            normalizationMethodName = "Supramaximal Work Capacity Integral",
            populationContext = "Anaerobic Energy Reserve Reference",
            protocolId = "PROTO_CRITICAL_POWER_3MIN_ALL_OUT",
            isMethodologyApproved = false,
            version = "NORM-WPRIME-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_ROM,
            expectedUnit = "°",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 30.0,
            referenceMax = 180.0,
            normalizationMethodName = "Kinematic Joint Arc Displacement",
            populationContext = "Anatomical Mobility Reference",
            protocolId = "PROTO_BIOMECHANICAL_ROM_STABILITY",
            isMethodologyApproved = false,
            version = "NORM-ROM-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_JOINT_STABILITY,
            expectedUnit = "%",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 50.0,
            referenceMax = 100.0,
            normalizationMethodName = "Dynamic Trajectory Variance & Bilateral Symmetry",
            populationContext = "Biomechanical Stability Reference",
            protocolId = "PROTO_BIOMECHANICAL_ROM_STABILITY",
            isMethodologyApproved = false,
            version = "NORM-STAB-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_VELOCITY,
            expectedUnit = "m/s",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 0.3,
            referenceMax = 2.5,
            normalizationMethodName = "Mean Propulsive Velocity (MPV) VBT",
            populationContext = "Velocity-Based Training Reference",
            protocolId = "PROTO_BIOMECHANICAL_ROM_STABILITY",
            isMethodologyApproved = false,
            version = "NORM-VEL-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_ACCELERATION,
            expectedUnit = "m/s²",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 2.0,
            referenceMax = 30.0,
            normalizationMethodName = "Peak Explosive Acceleration dv/dt",
            populationContext = "Kinematic Acceleration Reference",
            protocolId = "PROTO_BIOMECHANICAL_ROM_STABILITY",
            isMethodologyApproved = false,
            version = "NORM-ACC-1.0"
        ),
        NormalizationSpec(
            metricId = MetricCatalog.METRIC_HRV_RMSSD,
            expectedUnit = "ms",
            direction = MetricDirection.HIGHER_IS_BETTER,
            referenceMin = 15.0,
            referenceMax = 150.0,
            normalizationMethodName = "Log-Transformed Resting rMSSD Ln(rMSSD*10)",
            populationContext = "Resting Autonomic Tone Reference",
            protocolId = "PROTO_RESTING_HRV_RMSSD",
            isMethodologyApproved = false,
            version = "NORM-HRV-1.0"
        )
    ).associateBy { it.metricId }

    fun normalize(measurement: DataCoreMeasurement): NormalizationResult {
        val spec = SPECS[measurement.metricId]
            ?: return NormalizationResult(
                rawValue = measurement.rawValue,
                normalizedValue = null,
                calculationStatus = CalculationStatus.PENDING_VALIDATION,
                normalizationMethod = "Sem especificação de normalização para a métrica ${measurement.metricId}",
                populationContext = "Desconhecido",
                formulaVersion = "NORM-UNKNOWN-1.0",
                validityRange = "N/A",
                notes = "Métrica não possui especificação cadastrada no ScoreNormalizationEngine."
            )

        // Se a metodologia ainda não estiver aprovada formalmente pela ciência
        if (!spec.isMethodologyApproved) {
            return NormalizationResult(
                rawValue = measurement.rawValue,
                normalizedValue = null, // Não inventa score artificial 0-100
                calculationStatus = CalculationStatus.PENDING_VALIDATION,
                normalizationMethod = spec.normalizationMethodName,
                populationContext = spec.populationContext,
                formulaVersion = spec.version,
                validityRange = "${spec.referenceMin} a ${spec.referenceMax} ${spec.expectedUnit}",
                notes = "Metodologia de normalização aprovada pendente de validação pelo comitê científico (Core V1)."
            )
        }

        // Caso futuro onde metodologia foi aprovada
        val raw = measurement.rawValue
        val normalized = when (spec.direction) {
            MetricDirection.HIGHER_IS_BETTER -> ((raw - spec.referenceMin) / (spec.referenceMax - spec.referenceMin)) * 100.0
            MetricDirection.LOWER_IS_BETTER -> ((spec.referenceMax - raw) / (spec.referenceMax - spec.referenceMin)) * 100.0
            MetricDirection.TARGET_OPTIMAL_RANGE -> 100.0 - (kotlin.math.abs(raw - ((spec.referenceMin + spec.referenceMax) / 2.0)))
        }.coerceIn(0.0, 100.0)

        return NormalizationResult(
            rawValue = raw,
            normalizedValue = normalized,
            calculationStatus = CalculationStatus.CALCULATED,
            normalizationMethod = spec.normalizationMethodName,
            populationContext = spec.populationContext,
            formulaVersion = spec.version,
            validityRange = "${spec.referenceMin} a ${spec.referenceMax} ${spec.expectedUnit}",
            notes = "Normalização calculada determinística com versão ${spec.version}."
        )
    }
}
