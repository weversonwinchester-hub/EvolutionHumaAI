package com.example.core.progressionengine.evaluator

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.progressionengine.model.*
import com.example.core.trialengine.model.TrialResultStatus
import com.example.core.trialengine.model.TrialSnapshot
import com.example.core.trialengine.policy.TrialPolicyRegistry
import java.util.concurrent.TimeUnit

/**
 * PERFORMAI PROMOTION GATE
 *
 * Gate formal e determinístico de elegibilidade de promoção.
 *
 * UMA PROMOÇÃO SÓ É CONSIDERADA ELEGÍVEL QUANDO TODOS OS REQUISITOS OBRIGATÓRIOS
 * DA POLÍTICA ESTIVEREM SATISFEITOS.
 *
 * Se qualquer requisito obrigatório estiver bloqueado -> PROMOTION NOT ELIGIBLE.
 * A promoção oficial NÃO ocorre automaticamente nesta V1.
 */
object PromotionGate {

    fun evaluatePromotionGate(
        userId: String,
        currentClassId: String,
        targetClassId: String,
        currentClassSince: Long,
        timePolicy: ProgressionTimePolicy?,
        evidences: List<DataCoreEvidence>,
        provenances: Map<String, DataCoreProvenance> = emptyMap(),
        trialSnapshot: TrialSnapshot? = null,
        scoreSnapshotId: String? = null,
        evidencePackageId: String? = null,
        methodologyVersion: String = "1.0.0"
    ): PromotionCandidate {
        val candidateId = "PROM-CAND-$userId-${System.currentTimeMillis()}"
        val assessmentId = "PROG-ASSESS-$userId-${System.currentTimeMillis()}"

        val satisfiedRequirements = mutableListOf<String>()
        val blockingRequirements = mutableListOf<String>()

        val daysInClass = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - currentClassSince).coerceAtLeast(0)

        // 1. Verificação de Integridade dos Dados
        val hasTampered = evidences.any { it.integrityStatus == IntegrityStatus.TAMPERED }
        if (hasTampered) {
            blockingRequirements.add("INTEGRITY_VIOLATION: Evidência adulterada detectada.")
        } else if (evidences.isNotEmpty()) {
            satisfiedRequirements.add("DATA_INTEGRITY_VERIFIED")
        }

        // 2. Verificação de Tempo Mínimo (Maturidade Temporal)
        val timePolicyResult: String
        if (timePolicy != null) {
            if (daysInClass < timePolicy.minimumTimeInClassDays) {
                timePolicyResult = "TIME_NOT_MET: $daysInClass / ${timePolicy.minimumTimeInClassDays} dias necessários."
                blockingRequirements.add("MINIMUM_TIME_IN_CLASS_NOT_MET")
            } else {
                timePolicyResult = "TIME_CRITERIA_SATISFIED: $daysInClass dias na classe."
                satisfiedRequirements.add("MINIMUM_TIME_IN_CLASS_MET")
            }
        } else {
            timePolicyResult = "NO_TIME_POLICY_FOUND"
            blockingRequirements.add("POLICY_NOT_FOUND")
        }

        // 3. Verificação de Evidências Longitudinais (Span e Quantidade)
        val spanDays = if (evidences.isNotEmpty()) {
            val sorted = evidences.sortedBy { it.capturedAt }
            TimeUnit.MILLISECONDS.toDays(sorted.last().capturedAt - sorted.first().capturedAt)
        } else 0L

        if (timePolicy != null) {
            if (evidences.size < timePolicy.minimumObservationCount) {
                blockingRequirements.add("INSUFFICIENT_OBSERVATION_COUNT: ${evidences.size} / ${timePolicy.minimumObservationCount} exigidas.")
            } else {
                satisfiedRequirements.add("OBSERVATION_COUNT_SATISFIED")
            }

            if (spanDays < timePolicy.minimumEvidenceSpanDays) {
                blockingRequirements.add("INSUFFICIENT_EVIDENCE_SPAN: $spanDays / ${timePolicy.minimumEvidenceSpanDays} dias exigidos.")
            } else {
                satisfiedRequirements.add("EVIDENCE_SPAN_SATISFIED")
            }
        }

        // 4. Verificação de Consistência e Trajetória
        val trajectories = TrajectoryEvaluator.evaluateDimensionTrajectories(evidences, methodologyVersion)
        val decliningDims = trajectories.filter { it.value.trend == DimensionTrajectoryTrend.DECLINING }.keys.toList()
        val consistencyResult: String
        val balanceResult: String

        if (decliningDims.isNotEmpty()) {
            consistencyResult = "DECLINE_DETECTED: $decliningDims"
            balanceResult = "IMBALANCED: Dimensões $decliningDims em declínio."
            blockingRequirements.add("DECLINING_DIMENSIONS_DETECTED: $decliningDims")
        } else if (trajectories.values.any { it.trend == DimensionTrajectoryTrend.INSUFFICIENT_DATA }) {
            consistencyResult = "INSUFFICIENT_TRAJECTORY_DATA"
            balanceResult = "PENDING_DATA"
            blockingRequirements.add("INSUFFICIENT_DIMENSIONAL_DATA")
        } else {
            consistencyResult = "CONSISTENT_OR_IMPROVING"
            balanceResult = "BALANCED_MULTI_DIMENSIONAL_DEVELOPMENT"
            satisfiedRequirements.add("TRAJECTORY_CONSISTENCY_SATISFIED")
            satisfiedRequirements.add("DIMENSIONAL_BALANCE_SATISFIED")
        }

        // 5. Verificação de Sustentabilidade de Performance (Pico vs Sustentado)
        val sustainability = TrajectoryEvaluator.evaluateSustainability(evidences, methodologyVersion)
        if (!sustainability.isSustained) {
            blockingRequirements.add("PERFORMANCE_NOT_SUSTAINED: ${sustainability.explanation}")
        } else {
            satisfiedRequirements.add("PERFORMANCE_SUSTAINED")
        }

        // 6. Verificação de Adaptação
        val maturityResult: String
        val adaptationResult: String
        val minSpan = timePolicy?.minimumEvidenceSpanDays ?: 7
        val minCount = timePolicy?.minimumObservationCount ?: 5
        if (evidences.size >= minCount && spanDays >= minSpan) {
            maturityResult = "MATURE_LONGITUDINAL_BASE"
            adaptationResult = "ADAPTATION_DEMONSTRATED"
            satisfiedRequirements.add("PHYSIOLOGICAL_MATURITY_DEMONSTRATED")
            satisfiedRequirements.add("ADAPTIVE_RESPONSE_VERIFIED")
        } else {
            maturityResult = "DEVELOPING_BASE"
            adaptationResult = "ADAPTATION_PENDING_LONGITUDINAL_EVIDENCE"
            blockingRequirements.add("INSUFFICIENT_MATURITY_AND_ADAPTATION")
        }

        // 7. Verificação de Trial Controlado (quando exigido para a classe alvo)
        val trialRequired = TrialPolicyRegistry.isTrialRequiredForClass(targetClassId)
        val trialSummaryText: String

        if (trialRequired) {
            if (trialSnapshot == null) {
                blockingRequirements.add("TRIAL_REQUIRED_BUT_MISSING")
                trialSummaryText = "REQUIRED: Prova de Desempenho obrigatória para ascender à classe $targetClassId."
            } else if (trialSnapshot.result.resultStatus != TrialResultStatus.QUALIFIED) {
                blockingRequirements.add("TRIAL_NOT_QUALIFIED: Status atual: ${trialSnapshot.result.resultStatus}")
                trialSummaryText = "NOT_QUALIFIED: Prova realizada porém não qualificada (${trialSnapshot.result.resultStatus})."
            } else if (trialSnapshot.isMock || trialSnapshot.simulationMode) {
                blockingRequirements.add("TRIAL_SIMULATION_BLOCKED: Prova executada em modo simulação/mock.")
                trialSummaryText = "INVALID_SIMULATION: Tentativa em modo simulação não possui validade oficial."
            } else {
                satisfiedRequirements.add("TRIAL_OFFICIALLY_QUALIFIED")
                trialSummaryText = "QUALIFIED: Prova controlada qualificada com hash de integridade ${trialSnapshot.auditReference}."
            }
        } else {
            satisfiedRequirements.add("NO_TRIAL_REQUIRED_FOR_CLASS")
            trialSummaryText = "NOT_REQUIRED: Esta classe não exige prova de desempenho formal."
        }

        // 8. Determinação do Status de Elegibilidade
        val finalStatus: PromotionCandidateStatus
        if (hasTampered) {
            finalStatus = PromotionCandidateStatus.BLOCKED
        } else if (trialRequired && (trialSnapshot == null || trialSnapshot.result.resultStatus != TrialResultStatus.QUALIFIED)) {
            finalStatus = PromotionCandidateStatus.TRIAL_REQUIRED
        } else if (blockingRequirements.isNotEmpty()) {
            finalStatus = PromotionCandidateStatus.NOT_READY
        } else {
            finalStatus = PromotionCandidateStatus.ELIGIBLE
        }

        val explanation = ProgressionExplanation(
            performanceSummary = if (sustainability.isSustained) "SUSTAINED" else "UNSUSTAINED_OR_ISOLATED_PEAK",
            consistencySummary = consistencyResult,
            evidenceSummary = "${evidences.size} observações ao longo de $spanDays dias.",
            maturitySummary = maturityResult,
            adaptationSummary = adaptationResult,
            timeRequirementSummary = timePolicyResult,
            trialSummary = trialSummaryText,
            balanceSummary = balanceResult,
            overallOutcome = when (finalStatus) {
                PromotionCandidateStatus.ELIGIBLE -> "ELIGIBLE_FOR_PROMOTION (Decisão produzida para homologação futura)"
                PromotionCandidateStatus.TRIAL_REQUIRED -> "TRIAL_REQUIRED (Aguardando realização de Prova Controlada)"
                PromotionCandidateStatus.BLOCKED -> "BLOCKED (Violações de integridade ou regras mandatórias)"
                PromotionCandidateStatus.UNDER_REVIEW -> "UNDER_REVIEW"
                PromotionCandidateStatus.READY -> "READY"
                PromotionCandidateStatus.PENDING_VALIDATION -> "PENDING_VALIDATION"
                PromotionCandidateStatus.NOT_READY -> "NOT_READY (Requisitos obrigatórios pendentes)"
            },
            detailedBlockers = blockingRequirements
        )

        return PromotionCandidate(
            id = candidateId,
            userId = userId,
            currentClassId = currentClassId,
            targetClassId = targetClassId,
            satisfiedRequirements = satisfiedRequirements,
            blockingRequirements = blockingRequirements,
            evidencePackageId = evidencePackageId,
            scoreSnapshotId = scoreSnapshotId,
            trialSnapshotId = trialSnapshot?.id,
            progressionAssessmentId = assessmentId,
            timePolicyResult = timePolicyResult,
            consistencyResult = consistencyResult,
            maturityResult = maturityResult,
            adaptationResult = adaptationResult,
            balanceResult = balanceResult,
            status = finalStatus,
            explanation = explanation,
            methodologyVersion = methodologyVersion,
            createdAt = System.currentTimeMillis(),
            isMock = false,
            simulationMode = false
        )
    }
}
