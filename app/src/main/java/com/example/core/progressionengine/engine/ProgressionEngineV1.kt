package com.example.core.progressionengine.engine

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.progressionengine.evaluator.AnomalyDetector
import com.example.core.progressionengine.evaluator.PromotionGate
import com.example.core.progressionengine.evaluator.TrajectoryEvaluator
import com.example.core.progressionengine.model.*
import com.example.core.progressionengine.policy.ProgressionPolicyRegistry
import com.example.core.trialengine.model.TrialSnapshot
import java.security.MessageDigest

/**
 * PERFORMAI PROGRESSION ENGINE V1
 *
 * Motor central de governança longitudinal de progressão.
 * Modela a evolução real do atleta e avalia elegibilidade sem promoção automática nesta V1.
 *
 * Princípios de segurança e autoridade:
 * - CLIENT e AI_GATEWAY não possuem autoridade para mutação de estado.
 * - XP não possui autoridade sobre elegibilidade ou classe.
 * - Modos de simulação / mock são estritamente isolados.
 * - Histórico de progressão é append-only.
 */
class ProgressionEngineV1(
    val coreVersion: String = "1.0.0",
    val methodologyVersion: String = "1.0.0",
    val progressionPolicyVersion: String = "1.0.0",
    val evolutionPolicyVersion: String = "1.0.0",
    val trialPolicyVersion: String = "1.0.0",
    val scoreVersion: String = "1.0.0",
    val auditLogger: (String) -> Unit = {}
) {

    /**
     * Avalia a progressão longitudinal completa do atleta e gera snapshot determinístico.
     */
    fun assessProgression(
        userId: String,
        currentClassId: String,
        targetClassId: String,
        currentClassSince: Long,
        evidences: List<DataCoreEvidence>,
        provenances: Map<String, DataCoreProvenance> = emptyMap(),
        trialSnapshot: TrialSnapshot? = null,
        scoreSnapshotId: String? = null,
        evidencePackageId: String? = null,
        callerTier: CallerTier = CallerTier.CORE_ENGINE,
        isMock: Boolean = false,
        simulationMode: Boolean = false
    ): ProgressionAssessmentSnapshot {
        // Barreira de Autoridade
        if (callerTier == CallerTier.CLIENT || callerTier == CallerTier.AI_GATEWAY) {
            val violation = "SECURITY_VIOLATION_PROGRESSION_MANIPULATION_ATTEMPT: Caller tier $callerTier tried to trigger progression assessment."
            auditLogger(violation)
            throw SecurityException(violation)
        }

        if (userId.isBlank()) {
            throw IllegalArgumentException("userId não pode ser vazio para avaliação de progressão.")
        }

        val assessmentId = "PROG-ASSESS-$userId-${System.currentTimeMillis()}"
        val timePolicy = ProgressionPolicyRegistry.getPolicyForClass(currentClassId)

        // 1. Avaliação de Trajetórias Dimensionais Independentes
        val trajectories = TrajectoryEvaluator.evaluateDimensionTrajectories(evidences, methodologyVersion)

        // 2. Avaliação de Sustentabilidade de Desempenho
        val sustainability = TrajectoryEvaluator.evaluateSustainability(evidences, methodologyVersion)

        // 3. Avaliação de Manutenção da Classe Atual
        val maintenance = TrajectoryEvaluator.evaluateClassMaintenance(
            currentClassId = currentClassId,
            classSinceTimestamp = currentClassSince,
            evidences = evidences,
            methodologyVersion = methodologyVersion
        )

        // 4. Revisão de Regressão Longitudinal (sem downgrade automático)
        val regressionReview = TrajectoryEvaluator.evaluateRegressionReview(
            userId = userId,
            currentClassId = currentClassId,
            evidences = evidences,
            methodologyVersion = methodologyVersion
        )

        // 5. Detecção de Anomalias / Anti-Speedrun
        val daysInClass = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - currentClassSince
        ).coerceAtLeast(0)

        val anomalies = AnomalyDetector.detectAnomalies(
            userId = userId,
            currentClassId = currentClassId,
            targetClassId = targetClassId,
            timeInClassDays = daysInClass,
            timePolicy = timePolicy,
            evidences = evidences,
            provenances = provenances
        )

        if (anomalies.isNotEmpty()) {
            auditLogger("PROGRESSION_ANOMALY_DETECTED: user=$userId count=${anomalies.size} types=${anomalies.map { it.type }}")
        }

        // 6. Gate Formal de Elegibilidade de Promoção
        val candidate = PromotionGate.evaluatePromotionGate(
            userId = userId,
            currentClassId = currentClassId,
            targetClassId = targetClassId,
            currentClassSince = currentClassSince,
            timePolicy = timePolicy,
            evidences = evidences,
            provenances = provenances,
            trialSnapshot = trialSnapshot,
            scoreSnapshotId = scoreSnapshotId,
            evidencePackageId = evidencePackageId,
            methodologyVersion = methodologyVersion
        ).copy(
            isMock = isMock,
            simulationMode = simulationMode
        )

        // 7. Derivação do Estado de Progressão
        val progressionStatus = when (candidate.status) {
            PromotionCandidateStatus.ELIGIBLE -> EvolutionProgressionStatus.ELIGIBLE_FOR_PROMOTION
            PromotionCandidateStatus.TRIAL_REQUIRED -> EvolutionProgressionStatus.TRIAL_REQUIRED
            PromotionCandidateStatus.BLOCKED -> EvolutionProgressionStatus.AT_RISK
            PromotionCandidateStatus.NOT_READY -> {
                if (anomalies.any { it.type == ProgressionAnomalyType.INSUFFICIENT_LONGITUDINAL_DATA }) {
                    EvolutionProgressionStatus.INSUFFICIENT_EVIDENCE
                } else if (trajectories.values.any { it.trend == DimensionTrajectoryTrend.IMPROVING }) {
                    EvolutionProgressionStatus.IMPROVING
                } else {
                    EvolutionProgressionStatus.STABLE
                }
            }
            PromotionCandidateStatus.UNDER_REVIEW -> EvolutionProgressionStatus.READY_FOR_EVALUATION
            PromotionCandidateStatus.READY -> EvolutionProgressionStatus.READY_FOR_EVALUATION
            PromotionCandidateStatus.PENDING_VALIDATION -> EvolutionProgressionStatus.PENDING_VALIDATION
        }

        val progressionState = EvolutionProgressionState(
            id = "PROG-STATE-$userId-${System.currentTimeMillis()}",
            userId = userId,
            currentClassId = currentClassId,
            currentClassSince = currentClassSince,
            highestEligibleClassId = if (candidate.status == PromotionCandidateStatus.ELIGIBLE) targetClassId else currentClassId,
            nextTargetClassId = targetClassId,
            progressionStatus = progressionStatus,
            progressionPhase = "LONGITUDINAL_MATURATION",
            lastAssessmentAt = System.currentTimeMillis(),
            methodologyVersion = methodologyVersion,
            coreVersion = coreVersion,
            isMock = isMock,
            simulationMode = simulationMode
        )

        val auditHash = computeAuditHash(
            userId,
            currentClassId,
            targetClassId,
            candidate.status.name,
            progressionState.lastAssessmentAt
        )

        val snapshot = ProgressionAssessmentSnapshot(
            id = assessmentId,
            userId = userId,
            currentClassId = currentClassId,
            targetClassId = targetClassId,
            progressionState = progressionState,
            candidate = candidate,
            trajectories = trajectories,
            sustainability = sustainability,
            maintenance = maintenance,
            regressionReview = regressionReview,
            anomalies = anomalies,
            calculatedAt = progressionState.lastAssessmentAt,
            coreVersion = coreVersion,
            auditReference = auditHash,
            isMock = isMock,
            simulationMode = simulationMode
        )

        auditLogger("PROGRESSION_ASSESSMENT_COMPLETED: user=$userId current=$currentClassId target=$targetClassId status=${candidate.status} auditRef=$auditHash isMock=$isMock")

        return snapshot
    }

    /**
     * Tenta mutar estado a partir de caller não autorizado - lança SecurityException imediatamente.
     */
    fun mutateStateByExternal(
        callerTier: CallerTier,
        action: String
    ) {
        val violation = "SECURITY_VIOLATION_PROGRESSION_MANIPULATION_ATTEMPT: External caller $callerTier attempted action: $action"
        auditLogger(violation)
        throw SecurityException(violation)
    }

    /**
     * Cria registro append-only de histórico de evolução quando homologado.
     */
    fun createHistoryEntry(
        userId: String,
        previousClass: String,
        newClass: String,
        reason: String,
        evidencePackageId: String?,
        scoreSnapshotId: String?,
        trialSnapshotId: String?,
        progressionAssessmentId: String,
        callerTier: CallerTier = CallerTier.CORE_ENGINE,
        isMock: Boolean = false,
        simulationMode: Boolean = false
    ): EvolutionHistoryEntry {
        if (callerTier != CallerTier.CORE_ENGINE) {
            val violation = "SECURITY_VIOLATION_PROGRESSION_MANIPULATION_ATTEMPT: Caller $callerTier cannot create history entry."
            auditLogger(violation)
            throw SecurityException(violation)
        }

        val entryId = "EVOL-HIST-$userId-${System.currentTimeMillis()}"
        val auditRef = computeAuditHash(userId, previousClass, newClass, reason, System.currentTimeMillis())

        val entry = EvolutionHistoryEntry(
            id = entryId,
            userId = userId,
            timestamp = System.currentTimeMillis(),
            previousClass = previousClass,
            newClass = newClass,
            reason = reason,
            evidencePackageId = evidencePackageId,
            scoreSnapshotId = scoreSnapshotId,
            trialSnapshotId = trialSnapshotId,
            progressionAssessmentId = progressionAssessmentId,
            policyVersion = progressionPolicyVersion,
            methodologyVersion = methodologyVersion,
            auditReference = auditRef,
            isMock = isMock,
            simulationMode = simulationMode
        )

        auditLogger("EVOLUTION_HISTORY_ENTRY_RECORDED: user=$userId from=$previousClass to=$newClass auditRef=$auditRef")
        return entry
    }

    private fun computeAuditHash(vararg parts: Any): String {
        val raw = parts.joinToString(":") { it.toString() }
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
