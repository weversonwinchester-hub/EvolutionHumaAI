package com.example.core.evolutionengine.evaluator

import com.example.core.evidenceconsistency.model.EvolutionEvidencePackage
import com.example.core.evolutionengine.model.ClassDefinition
import com.example.core.evolutionengine.model.ClassEligibilityResult
import com.example.core.evolutionengine.model.ClassEligibilityStatus
import com.example.core.evolutionengine.model.EvolutionGap
import com.example.core.evolutionengine.model.EvolutionGapItem
import com.example.core.evolutionengine.model.EvolutionPolicy
import com.example.core.evolutionengine.model.RequirementCategory
import com.example.core.evolutionengine.model.RequirementResult
import com.example.core.evolutionengine.model.RequirementStatusResult
import com.example.core.scoreengine.model.ScoreSnapshot

class ClassEligibilityEvaluator(
    private val requirementEvaluator: RequirementEvaluator = RequirementEvaluator()
) {

    fun evaluateClassEligibility(
        userId: String,
        targetClass: ClassDefinition,
        currentClass: ClassDefinition,
        policy: EvolutionPolicy,
        scoreSnapshot: ScoreSnapshot?,
        evidencePackage: EvolutionEvidencePackage?,
        evaluationTimestamp: Long = System.currentTimeMillis(),
        coreVersion: String = "1.0.0-datacore-v1"
    ): ClassEligibilityResult {

        // 1. Avalia cada requisito individual da política
        val requirementResults: List<RequirementResult> = policy.requirements.map { req ->
            requirementEvaluator.evaluateRequirement(
                requirement = req,
                scoreSnapshot = scoreSnapshot,
                evidencePackage = evidencePackage,
                evaluationTimestamp = evaluationTimestamp
            )
        }

        // 2. Segrega requisitos satisfeitos e bloqueantes
        val satisfiedRequirements = requirementResults.filter { it.status == RequirementStatusResult.SATISFIED }
        val blockingRequirements = requirementResults.filter { reqResult ->
            reqResult.isMandatory && reqResult.status != RequirementStatusResult.SATISFIED
        }

        // 3. Determina o status geral de elegibilidade da classe de forma estrita e sem ambiguidade
        val overallStatus: ClassEligibilityStatus = determineEligibilityStatus(
            policy = policy,
            requirementResults = requirementResults,
            blockingRequirements = blockingRequirements
        )

        return ClassEligibilityResult(
            userId = userId,
            classId = targetClass.classId,
            currentClassId = currentClass.classId,
            status = overallStatus,
            requirementResults = requirementResults,
            blockingRequirements = blockingRequirements,
            satisfiedRequirements = satisfiedRequirements,
            evidencePackageId = evidencePackage?.id,
            scoreSnapshotId = scoreSnapshot?.id,
            methodologyVersion = policy.version,
            evaluatedAt = evaluationTimestamp,
            coreVersion = coreVersion
        )
    }

    private fun determineEligibilityStatus(
        policy: EvolutionPolicy,
        requirementResults: List<RequirementResult>,
        blockingRequirements: List<RequirementResult>
    ): ClassEligibilityStatus {
        if (requirementResults.isEmpty()) {
            return ClassEligibilityStatus.PENDING_VALIDATION
        }

        // Se todos os obrigatórios foram satisfeitos
        if (blockingRequirements.isEmpty()) {
            return ClassEligibilityStatus.ELIGIBLE
        }

        // Se há bloqueios críticos de integridade ou rejeição de evidências
        if (blockingRequirements.any { it.status == RequirementStatusResult.INVALID }) {
            return ClassEligibilityStatus.BLOCKED
        }

        // Se há requisitos pendentes de homologação científica
        if (blockingRequirements.any { it.status == RequirementStatusResult.PENDING_VALIDATION }) {
            return ClassEligibilityStatus.PENDING_VALIDATION
        }

        // Se faltam dados/evidências
        if (blockingRequirements.any { it.status == RequirementStatusResult.INSUFFICIENT_EVIDENCE }) {
            return ClassEligibilityStatus.INSUFFICIENT_EVIDENCE
        }

        // Se dados existem mas os critérios não foram satisfeitos
        return ClassEligibilityStatus.NOT_ELIGIBLE
    }

    fun buildEvolutionGap(
        targetClass: ClassDefinition,
        eligibilityResult: ClassEligibilityResult
    ): EvolutionGap {
        val groupedByCategory = eligibilityResult.requirementResults.groupBy { it.category }

        val gapCategories = mutableMapOf<RequirementCategory, EvolutionGapItem>()

        RequirementCategory.values().forEach { category ->
            val results = groupedByCategory[category] ?: emptyList()
            if (results.isNotEmpty()) {
                val satisfied = results.count { it.status == RequirementStatusResult.SATISFIED }
                val pending = results.count { it.status == RequirementStatusResult.PENDING_VALIDATION }
                val details = results.map { "${it.requirementId}: ${it.status} -> ${it.explanation.gapDescription}" }

                val summaryText = when {
                    satisfied == results.size -> "✓ SATISFIED"
                    pending > 0 -> "PENDING_VALIDATION"
                    results.any { it.status == RequirementStatusResult.INSUFFICIENT_EVIDENCE } -> "INSUFFICIENT_EVIDENCE"
                    else -> "NOT_SATISFIED ($satisfied/${results.size})"
                }

                gapCategories[category] = EvolutionGapItem(
                    category = category,
                    statusSummary = summaryText,
                    requirementCount = results.size,
                    satisfiedCount = satisfied,
                    pendingCount = pending,
                    details = details
                )
            }
        }

        val trialStatus = if (targetClass.trialPolicyId != null) {
            if (eligibilityResult.status == ClassEligibilityStatus.ELIGIBLE) "READY_FOR_TRIAL" else "LOCKED"
        } else {
            "NO_TRIAL_REQUIRED"
        }

        val summary = when (eligibilityResult.status) {
            ClassEligibilityStatus.ELIGIBLE -> "Todos os requisitos obrigatórios para ${targetClass.name} foram comprovados."
            ClassEligibilityStatus.NOT_ELIGIBLE -> "Existem ${eligibilityResult.blockingRequirements.size} requisito(s) não atendido(s)."
            ClassEligibilityStatus.INSUFFICIENT_EVIDENCE -> "Dados insuficientes no Data Core para comprovar a classe ${targetClass.name}."
            ClassEligibilityStatus.PENDING_VALIDATION -> "Requisitos de metodologia científica estão em PENDING_VALIDATION."
            ClassEligibilityStatus.BLOCKED -> "Progressão bloqueada por inconsistência de dados ou violação de protocolo."
        }

        return EvolutionGap(
            targetClassId = targetClass.classId,
            targetClassName = targetClass.name,
            categories = gapCategories,
            summary = summary,
            trialStatus = trialStatus
        )
    }
}
