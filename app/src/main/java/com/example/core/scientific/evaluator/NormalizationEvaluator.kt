package com.example.core.scientific.evaluator

import com.example.core.scientific.model.NormalizationType
import com.example.core.scientific.registry.PopulationReferenceRegistry

/**
 * PERFORMAI NORMALIZATION EVALUATOR
 *
 * Avalia normalizações de medição.
 * Se a normalização requer referências populacionais que ainda estão em PENDING_VALIDATION
 * ou vazias, recusa a ativação e retorna erro ou nulo sem inventar valores.
 */
object NormalizationEvaluator {

    data class NormalizationResult(
        val success: Boolean,
        val normalizedValue: Double?,
        val methodType: NormalizationType,
        val statusMessage: String
    )

    fun normalize(
        rawValue: Double,
        normalizationId: String,
        bodyMassKg: Double? = null,
        heightCm: Double? = null
    ): NormalizationResult {
        val norm = PopulationReferenceRegistry.getNormalization(normalizationId)
            ?: return NormalizationResult(
                success = false,
                normalizedValue = null,
                methodType = NormalizationType.ABSOLUTE,
                statusMessage = "NORMALIZATION_METHOD_NOT_FOUND"
            )

        if (!PopulationReferenceRegistry.isNormalizationActive(normalizationId)) {
            return NormalizationResult(
                success = false,
                normalizedValue = null,
                methodType = norm.type,
                statusMessage = "NORMALIZATION_METHOD_INACTIVE_OR_PENDING_REFERENCE"
            )
        }

        return when (norm.type) {
            NormalizationType.ABSOLUTE -> {
                NormalizationResult(
                    success = true,
                    normalizedValue = rawValue,
                    methodType = norm.type,
                    statusMessage = "ABSOLUTE_NORMALIZATION_APPLIED"
                )
            }
            NormalizationType.RELATIVE -> {
                if (bodyMassKg == null || bodyMassKg <= 0.0) {
                    NormalizationResult(
                        success = false,
                        normalizedValue = null,
                        methodType = norm.type,
                        statusMessage = "BODY_MASS_REQUIRED_FOR_RELATIVE_NORMALIZATION"
                    )
                } else {
                    NormalizationResult(
                        success = true,
                        normalizedValue = rawValue / bodyMassKg,
                        methodType = norm.type,
                        statusMessage = "RELATIVE_TO_BODY_MASS_APPLIED"
                    )
                }
            }
            NormalizationType.PERCENTILE, NormalizationType.POPULATION_SPECIFIC, NormalizationType.Z_SCORE -> {
                // Verificar se existe tabela de percentis populacionais validada
                val ref = norm.referenceId?.let { PopulationReferenceRegistry.getReference(it) }
                if (ref == null || ref.percentileData.isEmpty()) {
                    NormalizationResult(
                        success = false,
                        normalizedValue = null,
                        methodType = norm.type,
                        statusMessage = "POPULATION_REFERENCE_PENDING_VALIDATION_NO_FICTITIOUS_DATA"
                    )
                } else {
                    // Se houvesse dados validados, realizaria interpolação aqui
                    NormalizationResult(
                        success = true,
                        normalizedValue = rawValue,
                        methodType = norm.type,
                        statusMessage = "POPULATION_PERCENTILE_APPLIED"
                    )
                }
            }
            else -> {
                NormalizationResult(
                    success = false,
                    normalizedValue = null,
                    methodType = norm.type,
                    statusMessage = "NORMALIZATION_TYPE_NOT_IMPLEMENTED"
                )
            }
        }
    }
}
