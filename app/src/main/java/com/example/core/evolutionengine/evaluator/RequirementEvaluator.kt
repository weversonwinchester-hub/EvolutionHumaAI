package com.example.core.evolutionengine.evaluator

import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.EvolutionEvidencePackage
import com.example.core.evidenceconsistency.model.MaturityStatus
import com.example.core.evidenceconsistency.model.ValidityStatus
import com.example.core.evolutionengine.model.ComparisonOperator
import com.example.core.evolutionengine.model.EvolutionRequirement
import com.example.core.evolutionengine.model.RequirementCategory
import com.example.core.evolutionengine.model.RequirementExplanation
import com.example.core.evolutionengine.model.RequirementResult
import com.example.core.evolutionengine.model.RequirementStatus
import com.example.core.evolutionengine.model.RequirementStatusResult
import com.example.core.scoreengine.model.CalculationStatus
import com.example.core.scoreengine.model.ScoreSnapshot

class RequirementEvaluator {

    fun evaluateRequirement(
        requirement: EvolutionRequirement,
        scoreSnapshot: ScoreSnapshot?,
        evidencePackage: EvolutionEvidencePackage?,
        evaluationTimestamp: Long = System.currentTimeMillis()
    ): RequirementResult {

        // 1. Se o próprio requisito está pendente de validação metodológica
        if (requirement.status == RequirementStatus.PENDING_VALIDATION ||
            requirement.status == RequirementStatus.DEFINED
        ) {
            return buildPendingValidationResult(
                requirement = requirement,
                scoreSnapshot = scoreSnapshot,
                evidencePackage = evidencePackage,
                reason = "Metodologia ou threshold para o requisito '${requirement.id}' permanece PENDING_VALIDATION no Core.",
                evaluationTimestamp = evaluationTimestamp
            )
        }

        // 2. Avaliação por Categoria
        return when (requirement.category) {
            RequirementCategory.EVIDENCE -> evaluateEvidenceRequirement(requirement, evidencePackage, scoreSnapshot, evaluationTimestamp)
            RequirementCategory.MATURITY -> evaluateMaturityRequirement(requirement, evidencePackage, evaluationTimestamp)
            RequirementCategory.CONSISTENCY -> evaluateConsistencyRequirement(requirement, evidencePackage, evaluationTimestamp)
            RequirementCategory.REPEATABILITY -> evaluateRepeatabilityRequirement(requirement, evidencePackage, evaluationTimestamp)
            RequirementCategory.PERFORMANCE -> evaluatePerformanceRequirement(requirement, scoreSnapshot, evaluationTimestamp)
            RequirementCategory.PROTOCOL -> evaluateProtocolRequirement(requirement, evidencePackage, evaluationTimestamp)
            RequirementCategory.TIME,
            RequirementCategory.ADAPTATION,
            RequirementCategory.BALANCE,
            RequirementCategory.TRIAL -> {
                // Categorias preparadas para expansões futuras do Core
                buildPendingValidationResult(
                    requirement = requirement,
                    scoreSnapshot = scoreSnapshot,
                    evidencePackage = evidencePackage,
                    reason = "Categoria '${requirement.category}' estruturada no V1, aguardando ativação de submódulos científicos dedicados.",
                    evaluationTimestamp = evaluationTimestamp
                )
            }
        }
    }

    private fun evaluateEvidenceRequirement(
        req: EvolutionRequirement,
        evidencePackage: EvolutionEvidencePackage?,
        scoreSnapshot: ScoreSnapshot?,
        evaluationTimestamp: Long
    ): RequirementResult {
        val minCount = req.minimumEvidenceCount ?: 1
        val evidenceIds = evidencePackage?.evidenceIds ?: scoreSnapshot?.evidenceIds ?: emptyList()
        val actualCount = evidenceIds.size.toDouble()

        val status = when {
            actualCount == 0.0 -> RequirementStatusResult.INSUFFICIENT_EVIDENCE
            actualCount >= minCount -> RequirementStatusResult.SATISFIED
            else -> RequirementStatusResult.NOT_SATISFIED
        }

        val gap = if (actualCount >= minCount) {
            "Requisito atendido com ${actualCount.toInt()} evidências (mínimo: $minCount)."
        } else {
            "Faltam ${(minCount - actualCount).toInt()} evidências válidas para atingir o mínimo de $minCount."
        }

        return RequirementResult(
            requirementId = req.id,
            category = req.category,
            status = status,
            isMandatory = req.isMandatory,
            actualValue = actualCount,
            expectedValue = minCount.toDouble(),
            actualTextValue = "${actualCount.toInt()} evidências",
            expectedTextValue = ">= $minCount evidências",
            evidenceIds = evidenceIds,
            scoreSnapshotId = scoreSnapshot?.id,
            explanation = RequirementExplanation(
                whatIsRequired = "Registrar no mínimo $minCount evidências válidas no Data Core.",
                rationale = "Garantir robustez amostral para comprovação atlética.",
                metricId = req.metricId,
                evidenceIds = evidenceIds,
                protocolUsed = req.protocolRequirements.firstOrNull(),
                methodologyUsed = req.methodologyVersion,
                currentValueDescription = "${actualCount.toInt()} evidências válidas encontradas",
                requiredValueDescription = ">= $minCount evidências",
                gapDescription = gap
            ),
            methodologyVersion = req.methodologyVersion,
            evaluatedAt = evaluationTimestamp
        )
    }

    private fun evaluateMaturityRequirement(
        req: EvolutionRequirement,
        evidencePackage: EvolutionEvidencePackage?,
        evaluationTimestamp: Long
    ): RequirementResult {
        if (evidencePackage == null || evidencePackage.evidenceIds.isEmpty()) {
            return buildInsufficientEvidenceResult(req, "Nenhum pacote de evidências ou evidências válidas disponíveis para avaliar maturidade.", evaluationTimestamp)
        }

        val maturity = evidencePackage.overallMaturity
        val actualStatus = maturity.maturityStatus
        val expectedStatusName = req.textThreshold ?: MaturityStatus.ESTABLISHED.name

        if (actualStatus == MaturityStatus.PENDING_VALIDATION || actualStatus == MaturityStatus.UNDETERMINED) {
            return buildPendingValidationResult(req, null, evidencePackage, "Maturidade geral está com status $actualStatus.", evaluationTimestamp)
        }

        val maturityRank = mapOf(
            MaturityStatus.INITIAL to 1,
            MaturityStatus.DEVELOPING to 2,
            MaturityStatus.ESTABLISHED to 3,
            MaturityStatus.MATURE to 4
        )

        val actualRank = maturityRank[actualStatus] ?: 0
        val expectedRank = maturityRank[runCatching { MaturityStatus.valueOf(expectedStatusName) }.getOrDefault(MaturityStatus.ESTABLISHED)] ?: 3

        val isSatisfied = actualRank >= expectedRank
        val status = if (isSatisfied) RequirementStatusResult.SATISFIED else RequirementStatusResult.NOT_SATISFIED

        return RequirementResult(
            requirementId = req.id,
            category = req.category,
            status = status,
            isMandatory = req.isMandatory,
            actualValue = actualRank.toDouble(),
            expectedValue = expectedRank.toDouble(),
            actualTextValue = actualStatus.name,
            expectedTextValue = ">= $expectedStatusName",
            evidenceIds = evidencePackage.evidenceIds,
            scoreSnapshotId = null,
            explanation = RequirementExplanation(
                whatIsRequired = "Maturidade longitudinal mínima: $expectedStatusName.",
                rationale = "Comprovar consistência ao longo de janelas temporais contínuas.",
                metricId = req.metricId,
                evidenceIds = evidencePackage.evidenceIds,
                protocolUsed = null,
                methodologyUsed = req.methodologyVersion,
                currentValueDescription = "Maturidade atual: ${actualStatus.name} (${maturity.longitudinalCoverage})",
                requiredValueDescription = "Maturidade requerida: >= $expectedStatusName",
                gapDescription = if (isSatisfied) "Maturidade satisfeita com sucesso." else "Requer evolução temporal de dados para atingir status $expectedStatusName."
            ),
            methodologyVersion = req.methodologyVersion,
            evaluatedAt = evaluationTimestamp
        )
    }

    private fun evaluateConsistencyRequirement(
        req: EvolutionRequirement,
        evidencePackage: EvolutionEvidencePackage?,
        evaluationTimestamp: Long
    ): RequirementResult {
        if (evidencePackage == null || evidencePackage.evidenceIds.isEmpty()) {
            return buildInsufficientEvidenceResult(req, "Pacote de evidências inexistente para avaliar consistência.", evaluationTimestamp)
        }

        val consistency = if (req.metricId != null) {
            evidencePackage.consistencyAssessments[req.metricId]?.consistencyStatus ?: ConsistencyStatus.INSUFFICIENT_DATA
        } else {
            evidencePackage.overallConsistencyStatus
        }

        val status = when (consistency) {
            ConsistencyStatus.PENDING_VALIDATION -> RequirementStatusResult.PENDING_VALIDATION
            ConsistencyStatus.INSUFFICIENT_DATA -> RequirementStatusResult.INSUFFICIENT_EVIDENCE
            ConsistencyStatus.UNDETERMINED -> RequirementStatusResult.NOT_SATISFIED
            ConsistencyStatus.STABLE,
            ConsistencyStatus.IMPROVING -> RequirementStatusResult.SATISFIED
            ConsistencyStatus.VARIABLE,
            ConsistencyStatus.DECLINING -> RequirementStatusResult.NOT_SATISFIED
        }

        return RequirementResult(
            requirementId = req.id,
            category = req.category,
            status = status,
            isMandatory = req.isMandatory,
            actualTextValue = consistency.name,
            expectedTextValue = req.textThreshold ?: "STABLE",
            evidenceIds = evidencePackage.evidenceIds,
            explanation = RequirementExplanation(
                whatIsRequired = "Consistência de dados: ${req.textThreshold ?: "STABLE"}.",
                rationale = "Evitar variações espúrias ou artefatos de medição.",
                metricId = req.metricId,
                evidenceIds = evidencePackage.evidenceIds,
                protocolUsed = null,
                methodologyUsed = req.methodologyVersion,
                currentValueDescription = "Consistência observada: ${consistency.name}",
                requiredValueDescription = "Consistência requerida: ${req.textThreshold ?: "STABLE"}",
                gapDescription = "Avaliação longitudinal: $consistency."
            ),
            methodologyVersion = req.methodologyVersion,
            evaluatedAt = evaluationTimestamp
        )
    }

    private fun evaluateRepeatabilityRequirement(
        req: EvolutionRequirement,
        evidencePackage: EvolutionEvidencePackage?,
        evaluationTimestamp: Long
    ): RequirementResult {
        if (evidencePackage == null || evidencePackage.evidenceIds.isEmpty()) {
            return buildInsufficientEvidenceResult(req, "Sem evidências para avaliar repetibilidade.", evaluationTimestamp)
        }

        val repeatability = if (req.metricId != null) {
            evidencePackage.repeatabilityAssessments[req.metricId]?.result ?: "INSUFFICIENT_OBSERVATIONS"
        } else {
            evidencePackage.overallRepeatabilityStatus
        }

        val status = when (repeatability) {
            "HIGH" -> RequirementStatusResult.SATISFIED
            "MODERATE" -> if (req.textThreshold == "MODERATE") RequirementStatusResult.SATISFIED else RequirementStatusResult.NOT_SATISFIED
            "INSUFFICIENT_OBSERVATIONS" -> RequirementStatusResult.INSUFFICIENT_EVIDENCE
            "PENDING_VALIDATION" -> RequirementStatusResult.PENDING_VALIDATION
            else -> RequirementStatusResult.NOT_SATISFIED
        }

        return RequirementResult(
            requirementId = req.id,
            category = req.category,
            status = status,
            isMandatory = req.isMandatory,
            actualTextValue = repeatability,
            expectedTextValue = req.textThreshold ?: "HIGH",
            evidenceIds = evidencePackage.evidenceIds,
            explanation = RequirementExplanation(
                whatIsRequired = "Repetibilidade teste-reteste: ${req.textThreshold ?: "HIGH"}.",
                rationale = "Comprovar confiabilidade de protocolo.",
                metricId = req.metricId,
                evidenceIds = evidencePackage.evidenceIds,
                protocolUsed = null,
                methodologyUsed = req.methodologyVersion,
                currentValueDescription = "Repetibilidade: $repeatability",
                requiredValueDescription = "Requerido: ${req.textThreshold ?: "HIGH"}",
                gapDescription = "Status de repetibilidade: $repeatability."
            ),
            methodologyVersion = req.methodologyVersion,
            evaluatedAt = evaluationTimestamp
        )
    }

    private fun evaluatePerformanceRequirement(
        req: EvolutionRequirement,
        scoreSnapshot: ScoreSnapshot?,
        evaluationTimestamp: Long
    ): RequirementResult {
        if (scoreSnapshot == null) {
            return buildInsufficientEvidenceResult(req, "Nenhum ScoreSnapshot oficial disponível para avaliar performance.", evaluationTimestamp)
        }

        if (req.threshold == null) {
            return buildPendingValidationResult(req, scoreSnapshot, null, "Threshold de performance pendente de homologação científica oficial.", evaluationTimestamp)
        }

        val dimensionScore = scoreSnapshot.dimensionScores.firstOrNull { it.dimension.equals(req.dimensionId, ignoreCase = true) }

        if (dimensionScore == null) {
            return buildInsufficientEvidenceResult(req, "Dimensão '${req.dimensionId}' não calculada no ScoreSnapshot.", evaluationTimestamp)
        }

        if (dimensionScore.calculationStatus == CalculationStatus.PENDING_VALIDATION) {
            return buildPendingValidationResult(req, scoreSnapshot, null, "Cálculo da dimensão '${req.dimensionId}' está PENDING_VALIDATION.", evaluationTimestamp)
        }

        if (dimensionScore.calculationStatus == CalculationStatus.INSUFFICIENT_EVIDENCE) {
            return buildInsufficientEvidenceResult(req, "Evidências insuficientes para dimension score '${req.dimensionId}'.", evaluationTimestamp)
        }

        if (dimensionScore.calculationStatus == CalculationStatus.REJECTED) {
            return RequirementResult(
                requirementId = req.id,
                category = req.category,
                status = RequirementStatusResult.INVALID,
                isMandatory = req.isMandatory,
                actualValue = dimensionScore.score,
                expectedValue = req.threshold,
                evidenceIds = dimensionScore.evidenceIds,
                scoreSnapshotId = scoreSnapshot.id,
                explanation = RequirementExplanation(
                    whatIsRequired = "Score da dimensão '${req.dimensionId}' >= ${req.threshold}.",
                    rationale = "Verificação de capacidade quantitativa.",
                    metricId = req.metricId,
                    evidenceIds = dimensionScore.evidenceIds,
                    protocolUsed = null,
                    methodologyUsed = req.methodologyVersion,
                    currentValueDescription = "Score Rejeitado por integridade/dados",
                    requiredValueDescription = "Score >= ${req.threshold}",
                    gapDescription = "Dimensão rejeitada durante cálculo."
                ),
                methodologyVersion = req.methodologyVersion,
                evaluatedAt = evaluationTimestamp
            )
        }

        val actualScore = dimensionScore.score ?: 0.0
        val isSatisfied = when (req.operator) {
            ComparisonOperator.GTE -> actualScore >= req.threshold
            ComparisonOperator.LTE -> actualScore <= req.threshold
            ComparisonOperator.EQUALS -> actualScore == req.threshold
            else -> actualScore >= req.threshold
        }

        val status = if (isSatisfied) RequirementStatusResult.SATISFIED else RequirementStatusResult.NOT_SATISFIED

        return RequirementResult(
            requirementId = req.id,
            category = req.category,
            status = status,
            isMandatory = req.isMandatory,
            actualValue = actualScore,
            expectedValue = req.threshold,
            evidenceIds = dimensionScore.evidenceIds,
            scoreSnapshotId = scoreSnapshot.id,
            explanation = RequirementExplanation(
                whatIsRequired = "Score da dimensão '${req.dimensionId}' ${req.operator} ${req.threshold}.",
                rationale = "Comprovação de capacidade funcional e biomotora.",
                metricId = req.metricId,
                evidenceIds = dimensionScore.evidenceIds,
                protocolUsed = null,
                methodologyUsed = req.methodologyVersion,
                currentValueDescription = "Score atual: %.1f".format(actualScore),
                requiredValueDescription = "Score requerido: %.1f".format(req.threshold),
                gapDescription = if (isSatisfied) "Requisito de performance atingido." else "Déficit de %.1f pontos para o threshold da classe.".format(req.threshold - actualScore)
            ),
            methodologyVersion = req.methodologyVersion,
            evaluatedAt = evaluationTimestamp
        )
    }

    private fun evaluateProtocolRequirement(
        req: EvolutionRequirement,
        evidencePackage: EvolutionEvidencePackage?,
        evaluationTimestamp: Long
    ): RequirementResult {
        if (evidencePackage == null || evidencePackage.evidenceIds.isEmpty()) {
            return buildInsufficientEvidenceResult(req, "Sem evidências para verificar protocolo.", evaluationTimestamp)
        }

        val requiredProtocols = req.protocolRequirements
        val observedProtocols = evidencePackage.validityAssessments.map { it.protocolId }.distinct()

        val missingProtocols = requiredProtocols.filter { !observedProtocols.contains(it) }
        val isSatisfied = missingProtocols.isEmpty()

        val status = if (isSatisfied) RequirementStatusResult.SATISFIED else RequirementStatusResult.NOT_SATISFIED

        return RequirementResult(
            requirementId = req.id,
            category = req.category,
            status = status,
            isMandatory = req.isMandatory,
            actualTextValue = observedProtocols.joinToString(","),
            expectedTextValue = requiredProtocols.joinToString(","),
            evidenceIds = evidencePackage.evidenceIds,
            explanation = RequirementExplanation(
                whatIsRequired = "Execução sob protocolos: ${requiredProtocols.joinToString(",")}.",
                rationale = "Padronização metodológica estrita.",
                metricId = req.metricId,
                evidenceIds = evidencePackage.evidenceIds,
                protocolUsed = observedProtocols.firstOrNull(),
                methodologyUsed = req.methodologyVersion,
                currentValueDescription = "Protocolos observados: ${observedProtocols.joinToString(",")}",
                requiredValueDescription = "Protocolos requeridos: ${requiredProtocols.joinToString(",")}",
                gapDescription = if (isSatisfied) "Protocolos atendidos com sucesso." else "Protocolos ausentes: ${missingProtocols.joinToString(",")}."
            ),
            methodologyVersion = req.methodologyVersion,
            evaluatedAt = evaluationTimestamp
        )
    }

    private fun buildPendingValidationResult(
        requirement: EvolutionRequirement,
        scoreSnapshot: ScoreSnapshot?,
        evidencePackage: EvolutionEvidencePackage?,
        reason: String,
        evaluationTimestamp: Long
    ): RequirementResult {
        return RequirementResult(
            requirementId = requirement.id,
            category = requirement.category,
            status = RequirementStatusResult.PENDING_VALIDATION,
            isMandatory = requirement.isMandatory,
            actualValue = null,
            expectedValue = requirement.threshold,
            evidenceIds = evidencePackage?.evidenceIds ?: scoreSnapshot?.evidenceIds ?: emptyList(),
            scoreSnapshotId = scoreSnapshot?.id,
            explanation = RequirementExplanation(
                whatIsRequired = requirement.description.ifBlank { "Requisito ${requirement.id}" },
                rationale = "Validação pendente no consórcio científico.",
                metricId = requirement.metricId,
                evidenceIds = evidencePackage?.evidenceIds ?: emptyList(),
                protocolUsed = requirement.protocolRequirements.firstOrNull(),
                methodologyUsed = requirement.methodologyVersion,
                currentValueDescription = "PENDING_VALIDATION",
                requiredValueDescription = "PENDING_VALIDATION",
                gapDescription = reason,
                limitations = listOf(reason)
            ),
            methodologyVersion = requirement.methodologyVersion,
            evaluatedAt = evaluationTimestamp
        )
    }

    private fun buildInsufficientEvidenceResult(
        requirement: EvolutionRequirement,
        reason: String,
        evaluationTimestamp: Long
    ): RequirementResult {
        return RequirementResult(
            requirementId = requirement.id,
            category = requirement.category,
            status = RequirementStatusResult.INSUFFICIENT_EVIDENCE,
            isMandatory = requirement.isMandatory,
            actualValue = 0.0,
            expectedValue = requirement.threshold,
            evidenceIds = emptyList(),
            scoreSnapshotId = null,
            explanation = RequirementExplanation(
                whatIsRequired = requirement.description.ifBlank { "Requisito ${requirement.id}" },
                rationale = "Comprovação empírica requer evidências no Data Core.",
                metricId = requirement.metricId,
                evidenceIds = emptyList(),
                protocolUsed = null,
                methodologyUsed = requirement.methodologyVersion,
                currentValueDescription = "INSUFFICIENT_EVIDENCE",
                requiredValueDescription = "Evidências e dados válidos",
                gapDescription = reason,
                limitations = listOf(reason)
            ),
            methodologyVersion = requirement.methodologyVersion,
            evaluatedAt = evaluationTimestamp
        )
    }
}
