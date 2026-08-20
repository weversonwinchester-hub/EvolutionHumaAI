package com.example.core.scientific.registry

import com.example.core.scientific.model.MethodologyValidationStatus
import com.example.core.scientific.model.NormalizationMethod
import com.example.core.scientific.model.NormalizationType
import com.example.core.scientific.model.PopulationReference

/**
 * PERFORMAI POPULATION REFERENCE & NORMALIZATION REGISTRY
 *
 * Princípio: Não inventar percentis fictícios.
 * Referências populacionais não validadas permanecem explicitamente como PENDING_VALIDATION.
 * Normalizações dependentes de referências sem validação formal NÃO são ativadas.
 */
object PopulationReferenceRegistry {

    private val populationReferences: MutableMap<String, PopulationReference> = mutableMapOf()
    private val normalizationMethods: MutableMap<String, NormalizationMethod> = mutableMapOf()

    init {
        registerDefaultReferences()
    }

    private fun registerDefaultReferences() {
        // Normalização Absoluta Básica (Ativa para todas as métricas padrão)
        registerNormalization(
            NormalizationMethod(
                normalizationId = "NORM-ABSOLUTE-DEFAULT",
                metricId = "ALL",
                type = NormalizationType.ABSOLUTE,
                formula = "value",
                referenceId = null,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // Normalização Relativa por Massa Corporal (Ativa para Força e Potência)
        registerNormalization(
            NormalizationMethod(
                normalizationId = "NORM-RELATIVE-BODYMASS",
                metricId = "RELATIVE_FORCE",
                type = NormalizationType.RELATIVE,
                formula = "raw_value / body_mass_kg",
                referenceId = null,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // Referência Populacional PENDENTE DE VALIDAÇÃO (Exemplo explícito: sem percentis inventados)
        registerReference(
            PopulationReference(
                referenceId = "POPREF-VO2-GENERAL-PENDING",
                metricId = "VO2_MAX",
                populationDefinition = "População atlética geral brasileira",
                ageRange = "18-35",
                sexCategory = "ALL",
                trainingStatus = "TRAINED",
                sportContext = "MULTI_SPORT",
                sampleSize = null,
                percentileData = emptyMap(), // Sem percentis inventados!
                source = "PENDING_FORMAL_DATASET",
                methodologyVersion = "1.0.0",
                validationStatus = MethodologyValidationStatus.PENDING_REVIEW
            )
        )

        // Normalização por Percentil Populacional correspondente (inativa até validação da referência)
        registerNormalization(
            NormalizationMethod(
                normalizationId = "NORM-PERCENTILE-VO2-PENDING",
                metricId = "VO2_MAX",
                type = NormalizationType.PERCENTILE,
                formula = "percentile_lookup(value, reference_table)",
                referenceId = "POPREF-VO2-GENERAL-PENDING",
                validationStatus = MethodologyValidationStatus.PENDING_REVIEW
            )
        )
    }

    fun registerReference(reference: PopulationReference) {
        populationReferences[reference.referenceId] = reference
    }

    fun getReference(referenceId: String): PopulationReference? {
        return populationReferences[referenceId]
    }

    fun getReferencesForMetric(metricId: String): List<PopulationReference> {
        return populationReferences.values.filter { it.metricId == metricId }
    }

    fun getAllReferences(): List<PopulationReference> {
        return populationReferences.values.toList()
    }

    fun registerNormalization(normalization: NormalizationMethod) {
        normalizationMethods[normalization.normalizationId] = normalization
    }

    fun getNormalization(normalizationId: String): NormalizationMethod? {
        return normalizationMethods[normalizationId]
    }

    fun getNormalizationsForMetric(metricId: String): List<NormalizationMethod> {
        return normalizationMethods.values.filter { it.metricId == metricId || it.metricId == "ALL" }
    }

    fun isNormalizationActive(normalizationId: String): Boolean {
        val norm = normalizationMethods[normalizationId] ?: return false
        if (norm.validationStatus != MethodologyValidationStatus.ACTIVE && norm.validationStatus != MethodologyValidationStatus.VALIDATED) {
            return false
        }
        // Se depende de uma referência populacional, esta também deve estar VALIDADA/ACTIVE
        norm.referenceId?.let { refId ->
            val ref = populationReferences[refId] ?: return false
            if (ref.validationStatus != MethodologyValidationStatus.ACTIVE && ref.validationStatus != MethodologyValidationStatus.VALIDATED) {
                return false
            }
            if (ref.percentileData.isEmpty()) {
                return false
            }
        }
        return true
    }
}
