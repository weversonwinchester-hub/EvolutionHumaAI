package com.example.core.evidenceconsistency.maturity

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.ValidationStatus
import com.example.core.evidenceconsistency.model.EvidenceMaturity
import com.example.core.evidenceconsistency.model.MaturityStatus

class EvidenceMaturityEvaluator {

    fun evaluateMaturity(
        userId: String,
        measurements: List<DataCoreMeasurement>,
        evidences: List<DataCoreEvidence>,
        methodologyVersion: String = "1.0.0"
    ): EvidenceMaturity {
        val validMsrs = measurements.filter { it.validationStatus == ValidationStatus.VALID }
        val validEvs = evidences.filter { ev -> ev.measurementIds.any { msrId -> validMsrs.any { it.id == msrId } } }

        if (validMsrs.isEmpty()) {
            return EvidenceMaturity(
                userId = userId,
                metricCoverage = 0,
                temporalCoverageDays = 0.0,
                protocolConsistency = 0.0,
                evidenceCount = 0,
                sourceQuality = "NONE",
                repeatability = "NONE",
                longitudinalCoverage = "ZERO_DAYS",
                maturityStatus = MaturityStatus.INITIAL,
                methodologyVersion = methodologyVersion,
                limitations = "Nenhuma evidência ou medição válida registrada para o usuário."
            )
        }

        val metricCoverage = validMsrs.map { it.metricId }.distinct().size
        val sortedTimestamps = validMsrs.map { it.timestamp }.sorted()
        val firstTimestamp = sortedTimestamps.first()
        val lastTimestamp = sortedTimestamps.last()
        val spanDays = (lastTimestamp - firstTimestamp).toDouble() / 86_400_000.0

        // Consistência de protocolo (fração de medições no protocolo primário de cada métrica)
        val metricProtocols = validMsrs.groupBy { it.metricId }
        val protocolConsistencyScores = metricProtocols.map { (_, msrs) ->
            val dominantProtocolCount = msrs.groupBy { it.protocolId }.values.maxOf { it.size }
            dominantProtocolCount.toDouble() / msrs.size.toDouble()
        }
        val avgProtocolConsistency = if (protocolConsistencyScores.isNotEmpty()) protocolConsistencyScores.average() else 1.0

        // Avaliação de fonte de qualidade
        val sources = validEvs.map { it.source }.distinct()
        val sourceQuality = if (sources.size == 1) sources.first() else "MIXED_SOURCES (${sources.size})"

        val maturityStatus = when {
            validEvs.size < 3 || spanDays < 7.0 -> MaturityStatus.INITIAL
            validEvs.size >= 10 && spanDays >= 60.0 && avgProtocolConsistency >= 0.9 -> MaturityStatus.MATURE
            validEvs.size >= 5 && spanDays >= 21.0 && avgProtocolConsistency >= 0.75 -> MaturityStatus.ESTABLISHED
            else -> MaturityStatus.DEVELOPING
        }

        return EvidenceMaturity(
            userId = userId,
            metricCoverage = metricCoverage,
            temporalCoverageDays = spanDays,
            protocolConsistency = avgProtocolConsistency,
            evidenceCount = validEvs.size,
            sourceQuality = sourceQuality,
            repeatability = if (avgProtocolConsistency >= 0.8) "CONSISTENT" else "VARIABLE",
            longitudinalCoverage = "%.1f days span (%d observations)".format(spanDays, validMsrs.size),
            maturityStatus = maturityStatus,
            methodologyVersion = methodologyVersion,
            limitations = "Maturidade avaliada por cobertura dimensional (%d métricas) e janela temporal (%.1fd).".format(metricCoverage, spanDays)
        )
    }
}
