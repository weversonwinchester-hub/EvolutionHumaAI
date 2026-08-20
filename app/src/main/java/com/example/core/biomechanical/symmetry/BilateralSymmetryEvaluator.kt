package com.example.core.biomechanical.symmetry

import java.util.UUID

/**
 * PERFORMAI BILATERAL SYMMETRY & ASYMMETRY EVALUATOR
 *
 * Descreve observações de simetria biomecânica estritamente sem diagnósticos clínicos ou conclusões médicas.
 */

data class BilateralSymmetryResult(
    val resultId: String = UUID.randomUUID().toString(),
    val metric: String,
    val leftValue: Double,
    val rightValue: Double,
    val asymmetryPercent: Double, // |L - R| / max(L, R) * 100
    val symmetryIndex: Double,    // LSI = (L / R) * 100
    val uncertainty: Double,
    val methodologyVersion: String = "METH-SYMMETRY-LSI-V1",
    val isClinicalDiagnosis: Boolean = false, // SEMPRE FALSE
    val note: String = "OBSERVAÇÃO BIOMECÂNICA DESCRITIVA. NÃO CONSTITUI DIAGNÓSTICO MÉDICO."
)

object BilateralSymmetryEvaluator {

    fun evaluateSymmetry(
        metricName: String,
        leftValue: Double,
        rightValue: Double,
        leftUncertainty: Double = 0.0,
        rightUncertainty: Double = 0.0,
        methodologyVersion: String = "METH-SYMMETRY-LSI-V1"
    ): BilateralSymmetryResult {
        if (leftValue == 0.0 && rightValue == 0.0) {
            return BilateralSymmetryResult(
                metric = metricName,
                leftValue = 0.0,
                rightValue = 0.0,
                asymmetryPercent = 0.0,
                symmetryIndex = 100.0,
                uncertainty = 0.0,
                methodologyVersion = methodologyVersion
            )
        }

        // LSI: (Esquerda / Direita) * 100
        val lsi = if (rightValue != 0.0) {
            (leftValue / rightValue) * 100.0
        } else {
            0.0
        }

        // Assimetria Absoluta Percentual: |L - R| / max(L, R) * 100
        val maxVal = Math.max(Math.abs(leftValue), Math.abs(rightValue))
        val asymmetry = if (maxVal > 0.0) {
            (Math.abs(leftValue - rightValue) / maxVal) * 100.0
        } else 0.0

        val combinedUncertainty = Math.sqrt(leftUncertainty * leftUncertainty + rightUncertainty * rightUncertainty)

        return BilateralSymmetryResult(
            metric = metricName,
            leftValue = leftValue,
            rightValue = rightValue,
            asymmetryPercent = asymmetry,
            symmetryIndex = lsi,
            uncertainty = combinedUncertainty,
            methodologyVersion = methodologyVersion,
            isClinicalDiagnosis = false
        )
    }
}
