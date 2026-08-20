package com.example.core.evidenceconsistency.consistency

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.ValidationStatus
import com.example.core.evidenceconsistency.continuity.ProtocolContinuityTracker
import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.MetricConsistencyAssessment
import com.example.core.evidenceconsistency.model.ProtocolContinuityFlag
import com.example.core.evidenceconsistency.policy.ConsistencyPolicyRegistry
import kotlin.math.pow
import kotlin.math.sqrt

class EvidenceConsistencyEngine(
    private val policyRegistry: ConsistencyPolicyRegistry = ConsistencyPolicyRegistry,
    private val continuityTracker: ProtocolContinuityTracker = ProtocolContinuityTracker()
) {

    fun evaluateMetricConsistency(
        metricId: String,
        measurements: List<DataCoreMeasurement>,
        evidences: List<DataCoreEvidence>,
        evaluationTimestamp: Long = System.currentTimeMillis()
    ): MetricConsistencyAssessment {
        val validMsrs = measurements
            .filter { it.metricId == metricId && it.validationStatus == ValidationStatus.VALID }
            .sortedBy { it.timestamp }

        val evidenceIds = evidences
            .filter { ev -> ev.measurementIds.any { msrId -> validMsrs.any { it.id == msrId } } }
            .map { it.id }

        if (validMsrs.isEmpty()) {
            return MetricConsistencyAssessment(
                metricId = metricId,
                protocolId = "N/A",
                evidenceIds = emptyList(),
                measurementCount = 0,
                consistencyStatus = ConsistencyStatus.INSUFFICIENT_DATA,
                policyId = "NO_DATA",
                policyVersion = "1.0.0",
                variationCoefficient = null,
                trendDirection = null,
                methodologyVersion = "1.0.0",
                limitations = "Nenhuma medição válida disponível para a métrica."
            )
        }

        val primaryProtocol = validMsrs.last().protocolId
        val policy = policyRegistry.getPolicy(metricId, primaryProtocol)

        val minObs = policy.minimumObservations ?: 2
        if (validMsrs.size < minObs) {
            return MetricConsistencyAssessment(
                metricId = metricId,
                protocolId = primaryProtocol,
                evidenceIds = evidenceIds,
                measurementCount = validMsrs.size,
                consistencyStatus = ConsistencyStatus.INSUFFICIENT_DATA,
                policyId = policy.policyId,
                policyVersion = policy.version,
                variationCoefficient = null,
                trendDirection = null,
                methodologyVersion = policy.version,
                limitations = "Observações insuficientes: ${validMsrs.size} medições encontradas, mínimo exigido pela política=${minObs}."
            )
        }

        // Se a política de consistência for PENDING_VALIDATION, não inventamos regras arbitrárias
        if (policy.status != "APPROVED" || policy.aggregationMethod == "PENDING_CORE_METHODOLOGY_DECISION") {
            return MetricConsistencyAssessment(
                metricId = metricId,
                protocolId = primaryProtocol,
                evidenceIds = evidenceIds,
                measurementCount = validMsrs.size,
                consistencyStatus = ConsistencyStatus.PENDING_VALIDATION,
                policyId = policy.policyId,
                policyVersion = policy.version,
                variationCoefficient = null,
                trendDirection = null,
                methodologyVersion = policy.version,
                limitations = "PENDING_CORE_METHODOLOGY_DECISION: ${policy.limitations}"
            )
        }

        // Análise de Continuidade
        val continuity = continuityTracker.analyzeContinuity(metricId, validMsrs, evidences)
        if (!continuity.isCompatibleForDirectComparison) {
            return MetricConsistencyAssessment(
                metricId = metricId,
                protocolId = primaryProtocol,
                evidenceIds = evidenceIds,
                measurementCount = validMsrs.size,
                consistencyStatus = ConsistencyStatus.UNDETERMINED,
                policyId = policy.policyId,
                policyVersion = policy.version,
                variationCoefficient = null,
                trendDirection = "INCOMPATIBLE_SERIES",
                methodologyVersion = policy.version,
                limitations = "Descontinuidade crítica de protocolo ou unidade detectada na série temporal: ${continuity.flags}."
            )
        }

        // Cálculo estatístico formal sob política aprovada
        val rawValues = validMsrs.map { it.rawValue }
        val mean = rawValues.average()
        val variance = rawValues.map { (it - mean).pow(2.0) }.average()
        val stdDev = sqrt(variance)
        val cv = if (mean != 0.0) (stdDev / mean) * 100.0 else 0.0

        // Avaliação de tendência (Slope simples dos valores ao longo do tempo)
        val firstVal = rawValues.first()
        val lastVal = rawValues.last()
        val deltaPercent = if (firstVal != 0.0) ((lastVal - firstVal) / firstVal) * 100.0 else 0.0

        val consistencyStatus = when {
            cv <= 5.0 -> ConsistencyStatus.STABLE
            deltaPercent >= 5.0 -> ConsistencyStatus.IMPROVING
            deltaPercent <= -5.0 -> ConsistencyStatus.DECLINING
            else -> ConsistencyStatus.VARIABLE
        }

        return MetricConsistencyAssessment(
            metricId = metricId,
            protocolId = primaryProtocol,
            evidenceIds = evidenceIds,
            measurementCount = validMsrs.size,
            consistencyStatus = consistencyStatus,
            policyId = policy.policyId,
            policyVersion = policy.version,
            variationCoefficient = cv,
            trendDirection = if (deltaPercent > 0) "+%.1f%%".format(deltaPercent) else "%.1f%%".format(deltaPercent),
            methodologyVersion = policy.version,
            limitations = policy.limitations
        )
    }
}
