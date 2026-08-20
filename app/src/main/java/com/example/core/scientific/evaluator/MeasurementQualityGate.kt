package com.example.core.scientific.evaluator

import com.example.core.scientific.model.*
import com.example.core.scientific.registry.DeviceCapabilityRegistry
import com.example.core.scientific.registry.InstrumentRegistry
import com.example.core.scientific.registry.ScientificMethodologyRegistry
import com.example.core.scientific.registry.ScientificProtocolRegistry
import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI MEASUREMENT QUALITY GATE
 *
 * Autoridade metodológica para verificação estrita de integridade e validade científica.
 * Verifica:
 * - Instrumento válido e homologado
 * - Protocolo correto e compatível
 * - Unidade compatível
 * - Taxa de amostragem (sampling rate) suficiente
 * - Timestamp válido
 * - Calibração de instrumento quando exigida
 * - Sinal íntegro (ausência de corrupção/adulteração)
 * - Ausência de desvios invalidantes de protocolo
 * - Condições ambientais mínimas
 */
object MeasurementQualityGate {

    private const val MAX_CLOCK_SKEW_MS = 60_000L // 60 segundos de tolerância de relógio no futuro
    private const val MAX_DATA_AGE_MS = 90L * 24 * 60 * 60 * 1000 // 90 dias de idade máxima

    fun evaluate(
        measurementId: String,
        metricId: String,
        protocolId: String,
        instrumentId: String,
        unit: String,
        rawValue: Double,
        samplingRateObserved: Double?,
        clientTimestamp: Long,
        serverTimestamp: Long = System.currentTimeMillis(),
        calibrationDate: Long? = null,
        environmentalCondition: EnvironmentalCondition? = null,
        explicitDeviations: List<ProtocolDeviation> = emptyList(),
        isMock: Boolean = false,
        simulationMode: Boolean = false
    ): QualityGateEvaluation {
        val passedChecks = mutableListOf<String>()
        val failedChecks = mutableListOf<String>()
        val accumulatedDeviations = explicitDeviations.toMutableList()

        // 1. Verificação de Valor Físico (Sanity / Não corrupção)
        var signalIntegrity = true
        if (rawValue.isNaN() || rawValue.isInfinite()) {
            failedChecks.add("VALUE_IS_NOT_A_VALID_FINITE_NUMBER")
            signalIntegrity = false
        } else {
            passedChecks.add("PHYSICAL_VALUE_FINITE")
        }

        // 2. Verificação de Timestamp
        var timestampValidity = true
        if (clientTimestamp > serverTimestamp + MAX_CLOCK_SKEW_MS) {
            failedChecks.add("TIMESTAMP_IN_FUTURE_CLOCK_SKEW_EXCEEDED")
            timestampValidity = false
        } else if (serverTimestamp - clientTimestamp > MAX_DATA_AGE_MS) {
            failedChecks.add("TIMESTAMP_EXCESSIVELY_OLD")
            timestampValidity = false
        } else {
            passedChecks.add("TIMESTAMP_VALID")
        }

        // 3. Verificação de Metodologia e Definição da Métrica
        val methodology = ScientificMethodologyRegistry.getMethodologiesForMetric(metricId)
            .firstOrNull { it.validationStatus == MethodologyValidationStatus.ACTIVE || it.validationStatus == MethodologyValidationStatus.VALIDATED }

        if (methodology == null) {
            failedChecks.add("METRIC_METHODOLOGY_NOT_ACTIVE_OR_REGISTERED")
        } else {
            passedChecks.add("METHODOLOGY_ACTIVE")
        }

        // 4. Verificação de Protocolo
        val protocol = ScientificProtocolRegistry.getProtocol(protocolId)
        var expectedUnit = methodology?.acceptedUnits?.firstOrNull() ?: ""
        if (protocol == null) {
            failedChecks.add("PROTOCOL_NOT_FOUND_OR_UNREGISTERED")
        } else {
            if (protocol.metricId != metricId) {
                failedChecks.add("PROTOCOL_METRIC_MISMATCH_${protocol.metricId}_VS_$metricId")
            } else {
                passedChecks.add("PROTOCOL_METRIC_MATCH")
            }

            if (protocol.validationStatus != ProtocolValidationStatus.ACTIVE) {
                failedChecks.add("PROTOCOL_NOT_ACTIVE_STATUS_${protocol.validationStatus}")
            } else {
                passedChecks.add("PROTOCOL_ACTIVE")
            }
        }

        // 5. Verificação de Unidade
        val isUnitAccepted = methodology?.acceptedUnits?.any { it.equals(unit, ignoreCase = true) } == true
        if (!isUnitAccepted) {
            failedChecks.add("INCOMPATIBLE_UNIT_OBSERVED_${unit}_ACCEPTED_${methodology?.acceptedUnits}")
        } else {
            passedChecks.add("UNIT_COMPATIBLE")
            expectedUnit = unit
        }

        // 6. Verificação de Instrumento
        val instrument = InstrumentRegistry.getInstrument(instrumentId)
        var calibrationValidity = true
        if (instrument == null) {
            failedChecks.add("INSTRUMENT_NOT_HOMOLOGATED_OR_UNKNOWN")
            calibrationValidity = false
        } else {
            if (!instrument.supportedMetrics.contains(metricId)) {
                failedChecks.add("INSTRUMENT_DOES_NOT_SUPPORT_METRIC_$metricId")
            } else {
                passedChecks.add("INSTRUMENT_METRIC_SUPPORTED")
            }

            if (instrument.validationStatus != InstrumentValidationStatus.VALIDATED) {
                failedChecks.add("INSTRUMENT_STATUS_NOT_VALIDATED_${instrument.validationStatus}")
            } else {
                passedChecks.add("INSTRUMENT_HOMOLOGATED")
            }

            // Calibração do instrumento
            if (instrument.calibrationRequirement) {
                val intervalDays = instrument.calibrationIntervalDays ?: 365
                val intervalMs = intervalDays.toLong() * 24 * 60 * 60 * 1000
                if (calibrationDate == null) {
                    failedChecks.add("CALIBRATION_REQUIRED_BUT_NO_RECORD")
                    calibrationValidity = false
                    accumulatedDeviations.add(
                        ProtocolDeviation(
                            deviationId = UUID.randomUUID().toString(),
                            measurementId = measurementId,
                            protocolId = protocolId,
                            type = "CALIBRATION_EXPIRED_OR_MISSING",
                            severity = DeviationSeverity.MAJOR,
                            description = "Instrumento requer calibração periódica mas data não foi informada.",
                            impact = "Possível desvio sistemático de medição."
                        )
                    )
                } else if (serverTimestamp - calibrationDate > intervalMs) {
                    failedChecks.add("CALIBRATION_EXPIRED_MAX_INTERVAL_${intervalDays}_DAYS")
                    calibrationValidity = false
                    accumulatedDeviations.add(
                        ProtocolDeviation(
                            deviationId = UUID.randomUUID().toString(),
                            measurementId = measurementId,
                            protocolId = protocolId,
                            type = "CALIBRATION_EXPIRED",
                            severity = DeviationSeverity.MAJOR,
                            description = "Calibração expirou há mais de $intervalDays dias.",
                            impact = "Precisão reduzida."
                        )
                    )
                } else {
                    passedChecks.add("CALIBRATION_UP_TO_DATE")
                }
            } else {
                passedChecks.add("CALIBRATION_NOT_REQUIRED")
            }
        }

        // 7. Verificação de Taxa de Amostragem (Sampling Rate)
        val requiredSamplingRate = protocol?.samplingRate ?: instrument?.samplingRate
        if (requiredSamplingRate != null) {
            if (samplingRateObserved == null || samplingRateObserved < requiredSamplingRate * 0.95) { // 5% de tolerância
                failedChecks.add("SAMPLING_RATE_INSUFFICIENT_OBSERVED_${samplingRateObserved}HZ_REQUIRED_${requiredSamplingRate}HZ")
                accumulatedDeviations.add(
                    ProtocolDeviation(
                        deviationId = UUID.randomUUID().toString(),
                        measurementId = measurementId,
                        protocolId = protocolId,
                        type = "SAMPLING_RATE_DEFICIT",
                        severity = DeviationSeverity.MAJOR,
                        description = "Amostragem observada ($samplingRateObserved Hz) inferior ao mínimo do protocolo ($requiredSamplingRate Hz)",
                        impact = "Subamostragem de picos transitórios e eventos rápidos."
                    )
                )
            } else {
                passedChecks.add("SAMPLING_RATE_SUFFICIENT")
            }
        }

        // 8. Verificação de Desvios Invalidantes de Protocolo
        val hasInvalidatingDeviation = accumulatedDeviations.any { it.severity == DeviationSeverity.INVALIDATING }
        if (hasInvalidatingDeviation) {
            failedChecks.add("HAS_INVALIDATING_PROTOCOL_DEVIATION")
        }

        // Determinação do Status do Quality Gate
        val status: QualityGateStatus = when {
            hasInvalidatingDeviation || !timestampValidity || !signalIntegrity || failedChecks.contains("INCOMPATIBLE_UNIT_OBSERVED_${unit}_ACCEPTED_${methodology?.acceptedUnits}") || failedChecks.contains("PROTOCOL_METRIC_MISMATCH_${protocol?.metricId}_VS_$metricId") -> {
                QualityGateStatus.REJECTED
            }
            failedChecks.any { it.contains("NOT_ACTIVE") || it.contains("NOT_HOMOLOGATED") || it.contains("NOT_FOUND") } -> {
                QualityGateStatus.PENDING_VALIDATION
            }
            failedChecks.isNotEmpty() -> {
                QualityGateStatus.INSUFFICIENT_QUALITY
            }
            else -> {
                QualityGateStatus.ACCEPTED
            }
        }

        // Gerar referência de auditoria criptográfica
        val auditPayload = "$measurementId|$metricId|$protocolId|$instrumentId|$status|${passedChecks.size}|${failedChecks.size}|$serverTimestamp"
        val auditReference = MessageDigest.getInstance("SHA-256")
            .digest(auditPayload.toByteArray())
            .joinToString("") { "%02x".format(it) }

        return QualityGateEvaluation(
            evaluationId = UUID.randomUUID().toString(),
            measurementId = measurementId,
            metricId = metricId,
            protocolId = protocolId,
            instrumentId = instrumentId,
            status = status,
            passedChecks = passedChecks,
            failedChecks = failedChecks,
            deviations = accumulatedDeviations,
            samplingRateObserved = samplingRateObserved,
            samplingRateRequired = requiredSamplingRate,
            unitObserved = unit,
            unitExpected = expectedUnit,
            timestampValidity = timestampValidity,
            calibrationValidity = calibrationValidity,
            signalIntegrity = signalIntegrity,
            isMock = isMock,
            simulationMode = simulationMode,
            evaluatedAt = serverTimestamp,
            auditReference = auditReference
        )
    }
}
