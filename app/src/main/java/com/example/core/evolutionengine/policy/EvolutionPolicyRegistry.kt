package com.example.core.evolutionengine.policy

import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.model.ComparisonOperator
import com.example.core.evolutionengine.model.EvolutionPolicy
import com.example.core.evolutionengine.model.EvolutionRequirement
import com.example.core.evolutionengine.model.ProgressionMode
import com.example.core.evolutionengine.model.RequirementCategory
import com.example.core.evolutionengine.model.RequirementStatus
import java.util.concurrent.ConcurrentHashMap

/**
 * REGISTRO OFICIAL DE POLÍTICAS DE EVOLUÇÃO (V1)
 *
 * Princípio Arquitetural:
 * - Não inventamos limiares numéricos ou regras arbitrárias (ex: "Campeão = Força 90") sem metodologia homologada.
 * - Toda política inicial é registrada com status PENDING_VALIDATION ou com requisitos estruturais formais.
 * - As políticas são versionadas e imutáveis uma vez consumidas por snapshots de evolução.
 */
object EvolutionPolicyRegistry {

    private val policies = ConcurrentHashMap<String, EvolutionPolicy>()

    init {
        registerDefaultPolicies()
    }

    private fun registerDefaultPolicies() {
        // Registra as políticas para cada uma das 22 classes
        ClassCatalog.CLASSES.forEach { classDef ->
            val policy = buildClassPolicy(classDef.classId, classDef.requirementPolicyId)
            registerPolicy(policy)
        }
    }

    private fun buildClassPolicy(classId: String, policyId: String): EvolutionPolicy {
        // Estrutura formal de requisitos multidimensionais por classe
        // Em V1, todas as classes possuem seus requisitos catalogados com PENDING_VALIDATION
        // aguardando homologação pelo consórcio metodológico.
        val requirements = listOf(
            EvolutionRequirement(
                id = "REQ-$classId-EVIDENCE-MIN",
                classId = classId,
                category = RequirementCategory.EVIDENCE,
                operator = ComparisonOperator.MINIMUM_SET,
                minimumEvidenceCount = when (classId) {
                    ClassCatalog.CLASS_01 -> 1
                    ClassCatalog.CLASS_02 -> 2
                    ClassCatalog.CLASS_03 -> 3
                    ClassCatalog.CLASS_04 -> 4
                    ClassCatalog.CLASS_05 -> 5
                    else -> 8
                },
                methodologyVersion = "1.0.0-evolution-v1",
                status = RequirementStatus.ACTIVE,
                isMandatory = true,
                description = "Volume mínimo de evidências válidas e calibradas registradas no Data Core."
            ),
            EvolutionRequirement(
                id = "REQ-$classId-MATURITY",
                classId = classId,
                category = RequirementCategory.MATURITY,
                operator = ComparisonOperator.STATUS_MATCH,
                textThreshold = when (classId) {
                    ClassCatalog.CLASS_01, ClassCatalog.CLASS_02 -> "INITIAL"
                    ClassCatalog.CLASS_03, ClassCatalog.CLASS_04, ClassCatalog.CLASS_05 -> "DEVELOPING"
                    ClassCatalog.CLASS_06, ClassCatalog.CLASS_07, ClassCatalog.CLASS_08, ClassCatalog.CLASS_09, ClassCatalog.CLASS_10 -> "ESTABLISHED"
                    else -> "MATURE"
                },
                methodologyVersion = "1.0.0-evolution-v1",
                status = RequirementStatus.ACTIVE,
                isMandatory = true,
                description = "Maturidade longitudinal e cobertura temporal mínima de evidências."
            ),
            EvolutionRequirement(
                id = "REQ-$classId-CONSISTENCY",
                classId = classId,
                category = RequirementCategory.CONSISTENCY,
                operator = ComparisonOperator.STATUS_MATCH,
                textThreshold = "STABLE",
                methodologyVersion = "1.0.0-evolution-v1",
                status = RequirementStatus.PENDING_VALIDATION,
                isMandatory = true,
                description = "Estabilidade longitudinal temporal sob políticas de consistência aprovadas."
            ),
            EvolutionRequirement(
                id = "REQ-$classId-PERFORMANCE-PRIMARY",
                classId = classId,
                category = RequirementCategory.PERFORMANCE,
                dimensionId = "FORCE",
                operator = ComparisonOperator.GTE,
                threshold = null, // PENDING_VALIDATION: Sem números fictícios inventados
                methodologyVersion = "1.0.0-evolution-v1",
                status = RequirementStatus.PENDING_VALIDATION,
                isMandatory = true,
                description = "Índice de performance quantitativo da dimensão primária da classe."
            )
        )

        val limitations = mutableListOf(
            "Requisitos quantitativos de performance permanecem PENDING_VALIDATION até homologação científica.",
            "Políticas de Trial são gerenciadas em etapa separada pelo Trial Engine."
        )

        return EvolutionPolicy(
            policyId = policyId,
            version = "1.0.0",
            classId = classId,
            requirements = requirements,
            progressionMode = ProgressionMode.ALL_MANDATORY_SATISFIED,
            methodologyStatus = "PENDING_VALIDATION",
            effectiveFrom = 1720000000000L,
            source = "PERFORMAI_CORE_CONSORTIUM",
            limitations = limitations
        )
    }

    fun registerPolicy(policy: EvolutionPolicy) {
        policies[policy.policyId] = policy
        // Também mapeia por classId para busca direta
        policies["CLASS_POLICY_${policy.classId}"] = policy
    }

    fun getPolicyByPolicyId(policyId: String): EvolutionPolicy? = policies[policyId]

    fun getPolicyForClass(classId: String): EvolutionPolicy {
        return policies["CLASS_POLICY_$classId"]
            ?: policies.values.firstOrNull { it.classId == classId }
            ?: buildClassPolicy(classId, "POL-REQ-$classId-DEFAULT")
    }

    fun getAllPolicies(): List<EvolutionPolicy> = policies.values.distinctBy { it.policyId }
}
