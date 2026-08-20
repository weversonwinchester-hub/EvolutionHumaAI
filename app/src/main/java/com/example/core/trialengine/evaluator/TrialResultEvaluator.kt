package com.example.core.trialengine.evaluator

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.ValidationStatus
import com.example.core.trialengine.model.FatigueDegradationAnalysis
import com.example.core.trialengine.model.TrialAttempt
import com.example.core.trialengine.model.TrialAttemptValidationStatus
import com.example.core.trialengine.model.TrialPolicy
import com.example.core.trialengine.model.TrialResult
import com.example.core.trialengine.model.TrialResultStatus
import com.example.core.trialengine.model.TrialScoringMethod
import com.example.core.trialengine.model.TrialSession

/**
 * PERFORMAI TRIAL RESULT EVALUATOR
 *
 * Avaliador determinístico de provas controladas de desempenho.
 * Não aceita resultados digitados manualmente; deriva o resultado estritamente
 * das medições, evidências, proveniências e integridades capturadas durante as tentativas.
 */
class TrialResultEvaluator {

    fun evaluateSessionResult(
        session: TrialSession,
        policy: TrialPolicy,
        attempts: List<TrialAttempt>,
        evidences: List<DataCoreEvidence>,
        measurements: List<DataCoreMeasurement>,
        provenances: Map<String, DataCoreProvenance>,
        evaluationTimestamp: Long = System.currentTimeMillis()
    ): TrialResult {
        val limitations = mutableListOf<String>()
        val metricResults = mutableMapOf<String, Double>()

        // 1. Verificação de tentativas
        if (attempts.isEmpty()) {
            return createResult(
                session = session,
                policy = policy,
                status = TrialResultStatus.INSUFFICIENT_EVIDENCE,
                bestAttemptId = null,
                qualifying = emptyList(),
                failed = emptyList(),
                metricResults = emptyMap(),
                evidenceIds = emptyList(),
                explanation = "Nenhuma tentativa foi registrada para esta sessão de Trial.",
                limitations = listOf("SESSION_WITHOUT_ATTEMPTS"),
                timestamp = evaluationTimestamp
            )
        }

        // 2. Verificação de integridade e proveniência de cada tentativa
        val validAttempts = mutableListOf<TrialAttempt>()
        val failedAttempts = mutableListOf<TrialAttempt>()

        for (attempt in attempts) {
            // Verifica se as evidências da tentativa possuem proveniência
            val attemptEvidences = evidences.filter { attempt.rawEvidenceIds.contains(it.id) }
            val missingProvenance = attemptEvidences.any { !provenances.containsKey(it.provenanceId) }
            val tamperedEvidence = attemptEvidences.any { it.integrityStatus == IntegrityStatus.TAMPERED }

            // Verifica medições da tentativa
            val attemptMeasurements = measurements.filter { attempt.measurementIds.contains(it.id) }
            val invalidMeasurement = attemptMeasurements.any { it.validationStatus != ValidationStatus.VALID }

            if (missingProvenance) {
                limitations.add("ATTEMPT_${attempt.attemptNumber}_MISSING_PROVENANCE")
                failedAttempts.add(attempt)
            } else if (tamperedEvidence) {
                limitations.add("ATTEMPT_${attempt.attemptNumber}_TAMPERED_EVIDENCE")
                failedAttempts.add(attempt)
            } else if (invalidMeasurement) {
                limitations.add("ATTEMPT_${attempt.attemptNumber}_INVALID_MEASUREMENT")
                failedAttempts.add(attempt)
            } else if (attempt.validationStatus == TrialAttemptValidationStatus.VALID && attempt.resultValue != null) {
                validAttempts.add(attempt)
                metricResults["ATTEMPT_${attempt.attemptNumber}"] = attempt.resultValue
            } else {
                failedAttempts.add(attempt)
            }
        }

        // 3. Verificação de integridade global da sessão
        if (session.sessionIntegrity == IntegrityStatus.TAMPERED) {
            return createResult(
                session = session,
                policy = policy,
                status = TrialResultStatus.INVALID,
                bestAttemptId = null,
                qualifying = emptyList(),
                failed = attempts.map { it.id },
                metricResults = emptyMap(),
                evidenceIds = evidences.map { it.id },
                explanation = "A sessão de Trial foi invalidada por violação de integridade dos dados.",
                limitations = listOf("SESSION_INTEGRITY_TAMPERED"),
                timestamp = evaluationTimestamp
            )
        }

        // 4. Verificação de volume mínimo de tentativas válidas
        if (validAttempts.size < policy.minimumAttempts) {
            limitations.add("ATTEMPTS_COUNT_BELOW_MINIMUM_${validAttempts.size}_OF_${policy.minimumAttempts}")
            return createResult(
                session = session,
                policy = policy,
                status = TrialResultStatus.INSUFFICIENT_EVIDENCE,
                bestAttemptId = validAttempts.maxByOrNull { it.resultValue ?: 0.0 }?.id,
                qualifying = emptyList(),
                failed = failedAttempts.map { it.id },
                metricResults = metricResults,
                evidenceIds = evidences.map { it.id },
                explanation = "Número de tentativas válidas (${validAttempts.size}) inferior ao mínimo exigido pela política (${policy.minimumAttempts}).",
                limitations = limitations,
                timestamp = evaluationTimestamp
            )
        }

        // 5. Verificação de status metodológico (se não homologado = PENDING_VALIDATION)
        if (policy.methodologyStatus == "PENDING_VALIDATION" || policy.thresholdValue == null) {
            limitations.add("TRIAL_POLICY_METHODOLOGY_PENDING_HOMOLOGATION")
            val bestAttempt = validAttempts.maxByOrNull { it.resultValue ?: 0.0 }
            return createResult(
                session = session,
                policy = policy,
                status = TrialResultStatus.PENDING_VALIDATION,
                bestAttemptId = bestAttempt?.id,
                qualifying = emptyList(),
                failed = failedAttempts.map { it.id },
                metricResults = metricResults,
                evidenceIds = evidences.map { it.id },
                explanation = "Os dados da sessão e tentativas foram capturados com integridade, porém os thresholds científicos formais da política ${policy.trialPolicyId} estão pendentes de homologação.",
                limitations = limitations,
                timestamp = evaluationTimestamp
            )
        }

        // 6. Avaliação quantitativa baseada no método de scoring
        val threshold = policy.thresholdValue
        val isQualified: Boolean
        val bestAttempt = validAttempts.maxByOrNull { it.resultValue ?: 0.0 }

        when (policy.scoringMethod) {
            TrialScoringMethod.BEST_ATTEMPT -> {
                val bestValue = bestAttempt?.resultValue ?: 0.0
                isQualified = bestValue >= threshold
                metricResults["AGGREGATE_BEST"] = bestValue
            }
            TrialScoringMethod.MEAN_ATTEMPTS -> {
                val mean = validAttempts.mapNotNull { it.resultValue }.average()
                isQualified = mean >= threshold
                metricResults["AGGREGATE_MEAN"] = mean
            }
            TrialScoringMethod.ALL_QUALIFIED -> {
                isQualified = validAttempts.all { (it.resultValue ?: 0.0) >= threshold }
            }
            TrialScoringMethod.MINIMUM_ATTEMPT -> {
                val min = validAttempts.mapNotNull { it.resultValue }.minOrNull() ?: 0.0
                isQualified = min >= threshold
                metricResults["AGGREGATE_MIN"] = min
            }
            TrialScoringMethod.MAXIMUM_ATTEMPT -> {
                val max = validAttempts.mapNotNull { it.resultValue }.maxOrNull() ?: 0.0
                isQualified = max >= threshold
                metricResults["AGGREGATE_MAX"] = max
            }
            TrialScoringMethod.MEDIAN_ATTEMPTS,
            TrialScoringMethod.CONSISTENCY_ATTEMPTS -> {
                val mean = validAttempts.mapNotNull { it.resultValue }.average()
                isQualified = mean >= threshold
                metricResults["AGGREGATE_VALUE"] = mean
            }
        }

        val finalStatus = if (isQualified) TrialResultStatus.QUALIFIED else TrialResultStatus.NOT_QUALIFIED

        // 7. Análise estruturada de fadiga (quando aplicável)
        val fatigueAnalysis = calculateFatigueModel(validAttempts)

        return createResult(
            session = session,
            policy = policy,
            status = finalStatus,
            bestAttemptId = bestAttempt?.id,
            qualifying = if (isQualified) validAttempts.map { it.id } else emptyList(),
            failed = if (!isQualified) validAttempts.map { it.id } + failedAttempts.map { it.id } else failedAttempts.map { it.id },
            metricResults = metricResults,
            evidenceIds = evidences.map { it.id },
            explanation = "Avaliação oficial concluída sob a política ${policy.trialPolicyId} v${policy.version}. Método de scoring: ${policy.scoringMethod}. Status resultante: $finalStatus.",
            limitations = limitations,
            fatigueAnalysis = fatigueAnalysis,
            timestamp = evaluationTimestamp
        )
    }

    private fun calculateFatigueModel(attempts: List<TrialAttempt>): FatigueDegradationAnalysis? {
        if (attempts.size < 2) return null
        val values = attempts.mapNotNull { it.resultValue }
        if (values.size < 2) return null

        val first = values.first()
        val last = values.last()
        val dropPct = if (first > 0) ((first - last) / first) * 100.0 else 0.0

        return FatigueDegradationAnalysis(
            velocityDropPercentage = if (dropPct > 0) dropPct else 0.0,
            powerDropPercentage = null,
            techniqueAlterationScore = null,
            asymmetryIncreasePercentage = null,
            stabilityLossScore = null,
            motorPatternChangeDetected = dropPct > 15.0,
            methodologyStatus = "PENDING_VALIDATION",
            notes = "Análise preliminar de fadiga linear entre tentativas"
        )
    }

    private fun createResult(
        session: TrialSession,
        policy: TrialPolicy,
        status: TrialResultStatus,
        bestAttemptId: String?,
        qualifying: List<String>,
        failed: List<String>,
        metricResults: Map<String, Double>,
        evidenceIds: List<String>,
        explanation: String,
        limitations: List<String>,
        fatigueAnalysis: FatigueDegradationAnalysis? = null,
        timestamp: Long
    ): TrialResult {
        return TrialResult(
            id = "TR-${java.util.UUID.randomUUID()}",
            sessionId = session.id,
            userId = session.userId,
            classId = session.classId,
            bestAttemptId = bestAttemptId,
            qualifyingAttempts = qualifying,
            failedAttempts = failed,
            metricResults = metricResults,
            evidenceIds = evidenceIds,
            protocolVersion = policy.protocolId,
            trialPolicyVersion = policy.version,
            methodologyVersion = policy.version,
            resultStatus = status,
            explanation = explanation,
            limitations = limitations,
            fatigueAnalysis = fatigueAnalysis,
            calculatedAt = timestamp,
            auditReference = "AUDIT-TRIAL-RES-${session.id.take(8)}-$timestamp",
            isMock = session.isMock,
            simulationMode = session.simulationMode
        )
    }
}
