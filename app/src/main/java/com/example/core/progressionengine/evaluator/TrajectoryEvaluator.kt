package com.example.core.progressionengine.evaluator

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.progressionengine.model.*
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

/**
 * PERFORMAI TRAJECTORY EVALUATOR
 *
 * Avalia trajetórias longitudinais de dimensões individuais de forma desacoplada,
 * sem médias simplistas, sustentabilidade vs pico isolado, e manutenção de classe.
 */
object TrajectoryEvaluator {

    /**
     * Avalia trajetórias independentes para cada dimensão física registrada.
     */
    fun evaluateDimensionTrajectories(
        evidences: List<DataCoreEvidence>,
        methodologyVersion: String = "1.0.0"
    ): Map<String, DimensionTrajectory> {
        val groupedByDimension = evidences.groupBy { it.source.ifEmpty { "GENERAL" } }

        val trajectories = mutableMapOf<String, DimensionTrajectory>()

        for ((dimensionId, dimEvidences) in groupedByDimension) {
            val sorted = dimEvidences.sortedBy { it.capturedAt }
            val count = sorted.size
            if (count == 0) continue

            val initial = sorted.first().confidenceScore ?: 0.0
            val current = sorted.last().confidenceScore ?: 0.0
            val historicalBest = sorted.mapNotNull { it.confidenceScore }.maxOrNull() ?: current
            val recent = if (count >= 3) {
                sorted.takeLast(3).mapNotNull { it.confidenceScore }.let { if (it.isNotEmpty()) it.average() else current }
            } else {
                current
            }

            val spanMillis = sorted.last().capturedAt - sorted.first().capturedAt
            val spanDays = TimeUnit.MILLISECONDS.toDays(spanMillis).coerceAtLeast(0)

            val trend: DimensionTrajectoryTrend
            val volatility: Double?
            val consistencyScore: Double?
            val explanation: String

            if (count < 3) {
                trend = DimensionTrajectoryTrend.INSUFFICIENT_DATA
                volatility = null
                consistencyScore = null
                explanation = "Dados insuficientes para traçar trajetória longitudinal (mínimo 3 observações)."
            } else {
                // Cálculo de volatilidade e desvio padrão
                val values = sorted.map { it.confidenceScore ?: 0.0 }
                val mean = values.average()
                val variance = values.map { (it - mean) * (it - mean) }.average()
                val stdDev = sqrt(variance)
                volatility = stdDev

                // Consistência baseada no inverso da volatilidade relativa
                consistencyScore = (1.0 - (stdDev / (mean.coerceAtLeast(0.01)))).coerceIn(0.0, 1.0)

                // Tendência comparando primeira metade vs segunda metade
                val half = (count / 2).coerceAtLeast(1)
                val firstHalfMean = values.take(half).average()
                val secondHalfMean = values.drop(half).average()
                val delta = secondHalfMean - firstHalfMean

                if (delta > 0.05) {
                    trend = DimensionTrajectoryTrend.IMPROVING
                    explanation = "Trajetória ascendente consistente ao longo de $spanDays dias."
                } else if (delta < -0.05) {
                    trend = DimensionTrajectoryTrend.DECLINING
                    explanation = "Trajetória em declínio identificada ao longo de $spanDays dias."
                } else if (stdDev > 0.20) {
                    trend = DimensionTrajectoryTrend.VARIABLE
                    explanation = "Elevada variabilidade nos registros da dimensão sem padrão estável."
                } else {
                    trend = DimensionTrajectoryTrend.STABLE
                    explanation = "Trajetória consolidada e estável ao longo de $spanDays dias."
                }
            }

            trajectories[dimensionId] = DimensionTrajectory(
                dimensionId = dimensionId,
                initialValue = initial,
                currentValue = current,
                historicalBest = historicalBest,
                recentValue = recent,
                trend = trend,
                volatility = volatility,
                consistencyScore = consistencyScore,
                observationCount = count,
                observationSpanDays = spanDays,
                methodologyVersion = methodologyVersion,
                explanation = explanation
            )
        }

        return trajectories
    }

    /**
     * Avaliação de sustentabilidade de performance para diferenciar pico isolado de performance sustentada.
     */
    fun evaluateSustainability(
        evidences: List<DataCoreEvidence>,
        methodologyVersion: String = "1.0.0"
    ): PerformanceSustainabilityAssessment {
        if (evidences.isEmpty()) {
            return PerformanceSustainabilityAssessment(
                peakPerformance = null,
                recentPerformance = null,
                performanceVariance = null,
                observationSpanDays = 0,
                validObservationCount = 0,
                isSustained = false,
                consistencyStatus = "INSUFFICIENT_DATA",
                explanation = "Nenhuma evidência disponível para avaliar sustentabilidade.",
                methodologyVersion = methodologyVersion
            )
        }

        val sorted = evidences.sortedBy { it.capturedAt }
        val values = sorted.map { it.confidenceScore ?: 0.0 }
        val count = sorted.size
        val spanDays = TimeUnit.MILLISECONDS.toDays(sorted.last().capturedAt - sorted.first().capturedAt)

        val peak = values.maxOrNull() ?: 0.0
        val recentCount = (count / 3).coerceAtLeast(1)
        val recentAvg = values.takeLast(recentCount).average()

        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()

        // Um pico isolado ocorre quando o pico é muito superior à média recente ou quando count < 3
        val isPeakIsolated = count < 3 || (peak - recentAvg > 0.25) || (spanDays < 7)
        val isSustained = !isPeakIsolated && (recentAvg >= peak * 0.85) && count >= 3 && spanDays >= 7

        val consistencyStatus = if (isSustained) "SUSTAINED" else if (isPeakIsolated) "ISOLATED_PEAK" else "VARIABLE"
        val explanation = if (isSustained) {
            "Performance consolidada e sustentada ao longo de $spanDays dias com $count observações válidas."
        } else if (isPeakIsolated) {
            "Pico isolado detectado ($peak) não sustentado pela média recente ($recentAvg) ou span insuficiente ($spanDays dias)."
        } else {
            "Performance em desenvolvimento com variabilidade moderada."
        }

        return PerformanceSustainabilityAssessment(
            peakPerformance = peak,
            recentPerformance = recentAvg,
            performanceVariance = variance,
            observationSpanDays = spanDays,
            validObservationCount = count,
            isSustained = isSustained,
            consistencyStatus = consistencyStatus,
            explanation = explanation,
            methodologyVersion = methodologyVersion
        )
    }

    /**
     * Avaliação de adaptação neuromuscular e fisiológica.
     */
    fun evaluateAdaptation(
        dimensionId: String,
        evidences: List<DataCoreEvidence>,
        methodologyVersion: String = "1.0.0"
    ): AdaptationAssessment {
        val dimEvidences = evidences.filter { it.source.ifEmpty { "GENERAL" } == dimensionId }.sortedBy { it.capturedAt }
        if (dimEvidences.size < 3) {
            return AdaptationAssessment(
                dimensionId = dimensionId,
                baseline = dimEvidences.firstOrNull()?.confidenceScore,
                currentPerformance = dimEvidences.lastOrNull()?.confidenceScore,
                observationSpanDays = 0,
                trainingExposure = null,
                responsePattern = "INSUFFICIENT_EXPOSURE",
                recoveryPattern = null,
                status = AdaptationStatus.INSUFFICIENT_DATA,
                evidenceIds = dimEvidences.map { it.id },
                methodologyVersion = methodologyVersion
            )
        }

        val baseline = dimEvidences.take(2).mapNotNull { it.confidenceScore }.let { if (it.isNotEmpty()) it.average() else 0.0 }
        val current = dimEvidences.takeLast(2).mapNotNull { it.confidenceScore }.let { if (it.isNotEmpty()) it.average() else 0.0 }
        val spanDays = TimeUnit.MILLISECONDS.toDays(dimEvidences.last().capturedAt - dimEvidences.first().capturedAt)

        val status: AdaptationStatus
        val responsePattern: String

        if (current > baseline * 1.10 && spanDays >= 14) {
            status = AdaptationStatus.ADAPTED
            responsePattern = "ADAPTIVE_SUPERCOMPENSATION_DEMONSTRATED"
        } else if (current >= baseline && spanDays >= 7) {
            status = AdaptationStatus.ADAPTING
            responsePattern = "POSITIVE_RESPONSE_IN_PROGRESS"
        } else if (current < baseline * 0.85) {
            status = AdaptationStatus.DECLINING
            responsePattern = "MALADAPTATION_OR_FATIGUE_ACCUMULATION"
        } else {
            status = AdaptationStatus.VARIABLE
            responsePattern = "OSCILLATING_ADAPTIVE_RESPONSE"
        }

        return AdaptationAssessment(
            dimensionId = dimensionId,
            baseline = baseline,
            currentPerformance = current,
            observationSpanDays = spanDays,
            trainingExposure = "RECORDED_SESSIONS_${dimEvidences.size}",
            responsePattern = responsePattern,
            recoveryPattern = "OBSERVED",
            status = status,
            evidenceIds = dimEvidences.map { it.id },
            methodologyVersion = methodologyVersion
        )
    }

    /**
     * Avaliação contínua de manutenção de classe.
     */
    fun evaluateClassMaintenance(
        currentClassId: String,
        classSinceTimestamp: Long,
        evidences: List<DataCoreEvidence>,
        methodologyVersion: String = "1.0.0"
    ): ClassMaintenanceAssessment {
        val daysInClass = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - classSinceTimestamp).coerceAtLeast(0)
        val recentEvidences = evidences.filter { it.capturedAt >= classSinceTimestamp }

        val status: ClassMaintenanceStatus
        val explanation: String
        val affectedDimensions = mutableListOf<String>()

        if (recentEvidences.isEmpty() && daysInClass > 30) {
            status = ClassMaintenanceStatus.AT_RISK
            explanation = "Ausência de evidências nos últimos $daysInClass dias. Manutenção em risco."
        } else if (recentEvidences.size < 3 && daysInClass > 14) {
            status = ClassMaintenanceStatus.INSUFFICIENT_EVIDENCE
            explanation = "Volume insuficiente de observações recentes para certificar manutenção da classe."
        } else {
            val trajectories = evaluateDimensionTrajectories(recentEvidences, methodologyVersion)
            val decliningDims = trajectories.filter { it.value.trend == DimensionTrajectoryTrend.DECLINING }.keys.toList()
            affectedDimensions.addAll(decliningDims)

            if (decliningDims.isNotEmpty()) {
                status = ClassMaintenanceStatus.REVIEW_REQUIRED
                explanation = "Dimensões em declínio detectadas ($decliningDims). Revisão de manutenção requerida."
            } else {
                status = ClassMaintenanceStatus.MAINTAINED
                explanation = "Desempenho compatível com os critérios de sustentação da classe $currentClassId."
            }
        }

        return ClassMaintenanceAssessment(
            currentClassId = currentClassId,
            status = status,
            daysInClass = daysInClass,
            recentActivityCount = recentEvidences.size,
            affectedDimensions = affectedDimensions,
            explanation = explanation,
            calculatedAt = System.currentTimeMillis(),
            methodologyVersion = methodologyVersion
        )
    }

    /**
     * Avaliação de necessidade de revisão de regressão (NÃO executa downgrade automático).
     */
    fun evaluateRegressionReview(
        userId: String,
        currentClassId: String,
        evidences: List<DataCoreEvidence>,
        methodologyVersion: String = "1.0.0"
    ): RegressionReview {
        val trajectories = evaluateDimensionTrajectories(evidences, methodologyVersion)
        val decliningDims = trajectories.filter { it.value.trend == DimensionTrajectoryTrend.DECLINING }.keys.toList()

        val reviewStatus = if (decliningDims.size >= 2) {
            RegressionReviewStatus.REVIEW_REQUIRED
        } else if (decliningDims.size == 1) {
            RegressionReviewStatus.MONITOR
        } else {
            RegressionReviewStatus.NO_CONCERN
        }

        val totalSpanDays = if (evidences.isNotEmpty()) {
            val sorted = evidences.sortedBy { it.capturedAt }
            TimeUnit.MILLISECONDS.toDays(sorted.last().capturedAt - sorted.first().capturedAt)
        } else 0L

        return RegressionReview(
            id = "REG-REV-$userId-${System.currentTimeMillis()}",
            userId = userId,
            currentClass = currentClassId,
            affectedDimensions = decliningDims,
            evidenceIds = evidences.map { it.id },
            declineDurationDays = totalSpanDays,
            consistencyStatus = if (decliningDims.isEmpty()) "STABLE" else "DECLINING_IDENTIFIED",
            methodologyVersion = methodologyVersion,
            reviewStatus = reviewStatus,
            notes = "Downgrade automático desativado por design no V1. Somente revisão longitudinal.",
            reviewedAt = System.currentTimeMillis()
        )
    }
}

