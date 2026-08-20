package com.example.core.scientific.evaluator

import com.example.core.scientific.model.MeasurementRepeatability
import com.example.core.scientific.model.MethodologyValidationStatus
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * PERFORMAI REPEATABILITY EVALUATOR
 *
 * Integração com o Evidence & Consistency Engine.
 * Permite registrar e calcular:
 * - Test-retest reliability
 * - Typical error (TE)
 * - Coeficiente de variação (CV%)
 * - Intraclass correlation (ICC)
 * - Minimum detectable change (MDC95)
 *
 * Princípio: Não calcular estatísticas sem dados suficientes (mínimo de 2 ensaios).
 */
object RepeatabilityEvaluator {

    fun calculateReliability(
        metricId: String,
        methodId: String,
        trial1Values: List<Double>,
        trial2Values: List<Double>
    ): MeasurementRepeatability {
        if (trial1Values.size != trial2Values.size || trial1Values.size < 2) {
            return MeasurementRepeatability(
                metricId = metricId,
                methodId = methodId,
                testRetestReliability = null,
                typicalError = null,
                coefficientOfVariation = null,
                intraclassCorrelation = null,
                minimumDetectableChange = null,
                sampleSize = trial1Values.size,
                validationStatus = MethodologyValidationStatus.PENDING_REVIEW
            )
        }

        val n = trial1Values.size
        val differences = trial1Values.zip(trial2Values) { a, b -> a - b }
        val meanDiff = differences.average()
        val sdDiff = sqrt(differences.map { (it - meanDiff).pow(2) }.sum() / (n - 1))

        // Typical Error (TE = SD_diff / sqrt(2))
        val typicalError = sdDiff / sqrt(2.0)

        // Média combinada de todos os testes
        val grandMean = (trial1Values.sum() + trial2Values.sum()) / (2.0 * n)
        val cvPercent = if (grandMean != 0.0) (typicalError / grandMean) * 100.0 else null

        // Minimum Detectable Change com 95% de confiança (MDC95 = 1.96 * sqrt(2) * TE = 1.96 * SD_diff)
        val mdc95 = 1.96 * sqrt(2.0) * typicalError

        // Pearson r / Test-retest correlation
        val mean1 = trial1Values.average()
        val mean2 = trial2Values.average()
        var num = 0.0
        var den1 = 0.0
        var den2 = 0.0
        for (i in 0 until n) {
            val d1 = trial1Values[i] - mean1
            val d2 = trial2Values[i] - mean2
            num += d1 * d2
            den1 += d1.pow(2)
            den2 += d2.pow(2)
        }
        val r = if (den1 > 0 && den2 > 0) num / sqrt(den1 * den2) else null

        return MeasurementRepeatability(
            metricId = metricId,
            methodId = methodId,
            testRetestReliability = r,
            typicalError = typicalError,
            coefficientOfVariation = cvPercent,
            intraclassCorrelation = r, // Para 2 ensaios em modelo two-way mixed ICC(3,1) aproxima r
            minimumDetectableChange = mdc95,
            sampleSize = n,
            validationStatus = MethodologyValidationStatus.VALIDATED
        )
    }
}
