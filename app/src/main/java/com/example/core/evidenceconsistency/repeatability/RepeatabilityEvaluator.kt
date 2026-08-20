package com.example.core.evidenceconsistency.repeatability

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.ValidationStatus
import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.RepeatabilityAssessment
import kotlin.math.abs

class RepeatabilityEvaluator {

    fun evaluateRepeatability(
        metricId: String,
        measurements: List<DataCoreMeasurement>,
        evidences: List<DataCoreEvidence>
    ): RepeatabilityAssessment {
        val validMsrs = measurements
            .filter { it.metricId == metricId && it.validationStatus == ValidationStatus.VALID }
            .sortedBy { it.timestamp }

        val evidenceIds = evidences
            .filter { ev -> ev.measurementIds.any { msrId -> validMsrs.any { it.id == msrId } } }
            .map { it.id }

        if (validMsrs.size < 2) {
            return RepeatabilityAssessment(
                metricId = metricId,
                protocolId = validMsrs.firstOrNull()?.protocolId ?: "N/A",
                evidenceIds = evidenceIds,
                result = "INSUFFICIENT_OBSERVATIONS",
                repeatabilityScore = null,
                methodologyVersion = "1.0.0",
                status = ConsistencyStatus.INSUFFICIENT_DATA,
                limitations = "Requer no mínimo 2 medições válidas sob mesmo protocolo para avaliar repetibilidade."
            )
        }

        // Verifica se todas as medições foram feitas sob o mesmo protocolo
        val protocols = validMsrs.map { it.protocolId }.distinct()
        if (protocols.size > 1) {
            return RepeatabilityAssessment(
                metricId = metricId,
                protocolId = "MULTIPLE_PROTOCOLS",
                evidenceIds = evidenceIds,
                result = "PROTOCOL_MISMATCH",
                repeatabilityScore = null,
                methodologyVersion = "1.0.0",
                status = ConsistencyStatus.UNDETERMINED,
                limitations = "Repetibilidade não pode ser calculada entre protocolos distintos ($protocols)."
            )
        }

        val primaryProtocol = protocols.first()

        // Cálculo de repetibilidade teste-reteste (Intraclass Agreement relativo)
        // Para pares de testes consecutivos sob mesmo protocolo
        var totalPairDiffPercent = 0.0
        var pairCount = 0

        for (i in 0 until validMsrs.size - 1) {
            val v1 = validMsrs[i].rawValue
            val v2 = validMsrs[i + 1].rawValue
            val mean = (v1 + v2) / 2.0
            if (mean > 0.0) {
                val diffPercent = (abs(v2 - v1) / mean) * 100.0
                totalPairDiffPercent += diffPercent
                pairCount++
            }
        }

        if (pairCount == 0) {
            return RepeatabilityAssessment(
                metricId = metricId,
                protocolId = primaryProtocol,
                evidenceIds = evidenceIds,
                result = "UNDETERMINED",
                repeatabilityScore = null,
                methodologyVersion = "1.0.0",
                status = ConsistencyStatus.UNDETERMINED,
                limitations = "Dados numéricos insuficientes para cálculo de erro típico percentual."
            )
        }

        val meanTypicalErrorPercent = totalPairDiffPercent / pairCount
        val repeatabilityScore = (100.0 - meanTypicalErrorPercent).coerceIn(0.0, 100.0) / 100.0

        val resultCategory = when {
            meanTypicalErrorPercent <= 3.0 -> "HIGH"
            meanTypicalErrorPercent <= 8.0 -> "MODERATE"
            else -> "LOW"
        }

        return RepeatabilityAssessment(
            metricId = metricId,
            protocolId = primaryProtocol,
            evidenceIds = evidenceIds,
            result = resultCategory,
            repeatabilityScore = repeatabilityScore,
            methodologyVersion = "1.0.0",
            status = ConsistencyStatus.STABLE,
            limitations = "Baseado em erro típico percentual médio (%.2f%%) entre %d pares de observações.".format(meanTypicalErrorPercent, pairCount)
        )
    }
}
