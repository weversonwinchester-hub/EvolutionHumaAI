package com.example.core.evidenceconsistency.engine

import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.DataCoreAuditLog
import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.ValidationStatus
import com.example.core.error.AppError
import com.example.core.error.AppResult
import com.example.core.evidenceconsistency.consistency.EvidenceConsistencyEngine
import com.example.core.evidenceconsistency.continuity.ProtocolContinuityTracker
import com.example.core.evidenceconsistency.longitudinal.LongitudinalTracker
import com.example.core.evidenceconsistency.maturity.EvidenceMaturityEvaluator
import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.EvidenceMaturity
import com.example.core.evidenceconsistency.model.EvidenceQualityMatrix
import com.example.core.evidenceconsistency.model.EvidenceValidityAssessment
import com.example.core.evidenceconsistency.model.EvolutionEvidencePackage
import com.example.core.evidenceconsistency.model.LongitudinalMetricSequence
import com.example.core.evidenceconsistency.model.MaturityStatus
import com.example.core.evidenceconsistency.model.MetricConsistencyAssessment
import com.example.core.evidenceconsistency.model.ProtocolContinuityAssessment
import com.example.core.evidenceconsistency.model.ProtocolContinuityFlag
import com.example.core.evidenceconsistency.model.RepeatabilityAssessment
import com.example.core.evidenceconsistency.model.ValidityStatus
import com.example.core.evidenceconsistency.repeatability.RepeatabilityEvaluator
import com.example.core.evidenceconsistency.validity.EvidenceValidityEngine
import java.util.UUID

data class ConsistencyEngineResult(
    val evidencePackage: EvolutionEvidencePackage,
    val auditLog: DataCoreAuditLog
)

class EvidenceConsistencyEngineV1(
    val engineVersion: String = "1.0.0-consistency-v1",
    val coreVersion: String = "1.0.0-datacore-v1",
    private val validityEngine: EvidenceValidityEngine = EvidenceValidityEngine(),
    private val continuityTracker: ProtocolContinuityTracker = ProtocolContinuityTracker(),
    private val consistencyEngine: EvidenceConsistencyEngine = EvidenceConsistencyEngine(),
    private val repeatabilityEvaluator: RepeatabilityEvaluator = RepeatabilityEvaluator(),
    private val maturityEvaluator: EvidenceMaturityEvaluator = EvidenceMaturityEvaluator(),
    private val longitudinalTracker: LongitudinalTracker = LongitudinalTracker()
) {

    fun generateEvidencePackage(
        userId: String,
        measurements: List<DataCoreMeasurement>,
        evidences: List<DataCoreEvidence>,
        provenances: Map<String, DataCoreProvenance>,
        isSimulationMode: Boolean = false,
        evaluationTimestamp: Long = System.currentTimeMillis()
    ): AppResult<ConsistencyEngineResult> {

        if (userId.isBlank()) {
            return AppResult.Failure(AppError.ValidationError("userId é obrigatório para geração do Evidence Package."))
        }

        // 1. ISOLAMENTO ESTRITO DE MOCK DATA
        val hasMockEvidence = evidences.any { it.isMock } || measurements.any { it.isMock }
        if (hasMockEvidence && !isSimulationMode) {
            return AppResult.Failure(
                AppError.InvalidEvidence(
                    "Violação de Isolamento: Evidências mock detectadas no fluxo oficial de consistência. Mock data é estritamente proibida no cálculo oficial de elegibilidade."
                )
            )
        }

        val effectiveMeasurements = if (isSimulationMode) measurements else measurements.filter { !it.isMock }
        val effectiveEvidences = if (isSimulationMode) evidences else evidences.filter { !it.isMock }

        // 2. AVALIAÇÃO DE VALIDADE TEMPORAL E INTEGRIDADE DE CADA EVIDÊNCIA
        val validityAssessments = effectiveEvidences.map { ev ->
            val associatedMsrs = effectiveMeasurements.filter { it.id in ev.measurementIds }
            validityEngine.evaluateEvidence(ev, associatedMsrs, evaluationTimestamp)
        }

        val expiredEvidenceIds = validityAssessments.filter { it.validityStatus == ValidityStatus.EXPIRED }.map { it.evidenceId }
        val invalidEvidenceIds = validityAssessments.filter { it.validityStatus == ValidityStatus.INVALID }.map { it.evidenceId }
        val pendingValidationEvidenceIds = validityAssessments.filter { it.validityStatus == ValidityStatus.PENDING_VALIDATION }.map { it.evidenceId }
        val currentOrAgingEvidenceIds = validityAssessments.filter { it.validityStatus == ValidityStatus.CURRENT || it.validityStatus == ValidityStatus.AGING }.map { it.evidenceId }

        // Medições associadas a evidências válidas (CURRENT ou AGING)
        val validCurrentMeasurements = effectiveMeasurements.filter { msr ->
            msr.validationStatus == ValidationStatus.VALID &&
                    effectiveEvidences.any { ev ->
                        ev.id in currentOrAgingEvidenceIds && msr.id in ev.measurementIds
                    }
        }

        val allObservedMetrics = effectiveMeasurements.map { it.metricId }.distinct()
        val validMetrics = validCurrentMeasurements.map { it.metricId }.distinct()
        val invalidMetrics = allObservedMetrics.filter { !validMetrics.contains(it) }

        // 3. AVALIAÇÕES LONGITUDINAIS, DE CONTINUIDADE, CONSISTÊNCIA E REPETIBILIDADE POR MÉTRICA
        val consistencyAssessments = mutableMapOf<String, MetricConsistencyAssessment>()
        val repeatabilityAssessments = mutableMapOf<String, RepeatabilityAssessment>()
        val continuityAssessments = mutableMapOf<String, ProtocolContinuityAssessment>()
        val longitudinalSequences = mutableMapOf<String, LongitudinalMetricSequence>()
        val pendingValidationItems = mutableListOf<String>()

        for (metricId in allObservedMetrics) {
            val metricMsrs = effectiveMeasurements.filter { it.metricId == metricId }
            val metricEvs = effectiveEvidences.filter { ev -> ev.measurementIds.any { msrId -> metricMsrs.any { it.id == msrId } } }

            val seq = longitudinalTracker.buildLongitudinalSequence(metricId, metricMsrs, metricEvs, evaluationTimestamp)
            longitudinalSequences[metricId] = seq
            consistencyAssessments[metricId] = seq.consistencyAssessment
            repeatabilityAssessments[metricId] = seq.repeatabilityAssessment
            continuityAssessments[metricId] = seq.continuityAssessment

            if (seq.consistencyAssessment.consistencyStatus == ConsistencyStatus.PENDING_VALIDATION) {
                pendingValidationItems.add("Consistência da métrica $metricId: ${seq.consistencyAssessment.limitations}")
            }
        }

        for (pendingEvId in pendingValidationEvidenceIds) {
            val assess = validityAssessments.firstOrNull { it.evidenceId == pendingEvId }
            if (assess != null) {
                pendingValidationItems.add("Validade temporal da evidência $pendingEvId (${assess.metricId}): ${assess.limitations}")
            }
        }

        // 4. AVALIAÇÃO DE MATURIDADE
        val overallMaturity = maturityEvaluator.evaluateMaturity(
            userId = userId,
            measurements = validCurrentMeasurements,
            evidences = effectiveEvidences.filter { it.id in currentOrAgingEvidenceIds },
            methodologyVersion = engineVersion
        )

        // 5. STATUS GERAL DE CONSISTÊNCIA E REPETIBILIDADE
        val overallConsistency = when {
            validMetrics.isEmpty() -> ConsistencyStatus.INSUFFICIENT_DATA
            consistencyAssessments.values.any { it.consistencyStatus == ConsistencyStatus.PENDING_VALIDATION } -> ConsistencyStatus.PENDING_VALIDATION
            consistencyAssessments.values.all { it.consistencyStatus == ConsistencyStatus.STABLE } -> ConsistencyStatus.STABLE
            consistencyAssessments.values.any { it.consistencyStatus == ConsistencyStatus.VARIABLE } -> ConsistencyStatus.VARIABLE
            else -> ConsistencyStatus.UNDETERMINED
        }

        val overallRepeatability = when {
            repeatabilityAssessments.values.all { it.result == "HIGH" } -> "HIGH"
            repeatabilityAssessments.values.any { it.result == "MODERATE" } -> "MODERATE"
            repeatabilityAssessments.values.any { it.result == "LOW" } -> "LOW"
            repeatabilityAssessments.values.any { it.result == "INSUFFICIENT_OBSERVATIONS" } -> "INSUFFICIENT_OBSERVATIONS"
            else -> "UNDETERMINED"
        }

        // 6. MATRIZ DE QUALIDADE ESTRUTURADA
        val avgIntegrityScore = if (effectiveEvidences.isNotEmpty()) {
            effectiveEvidences.map { if (it.integrityStatus == IntegrityStatus.VALID) 1.0 else 0.0 }.average()
        } else 0.0

        val qualityMatrix = EvidenceQualityMatrix(
            sourceQualityTier = if (effectiveEvidences.isNotEmpty()) 1 else 5,
            integrityScore = avgIntegrityScore,
            protocolFidelityScore = overallMaturity.protocolConsistency,
            temporalValidityStatus = if (expiredEvidenceIds.isNotEmpty()) ValidityStatus.AGING else ValidityStatus.CURRENT,
            consistencyStatus = overallConsistency,
            repeatabilityStatus = overallRepeatability,
            overallMaturityStatus = overallMaturity.maturityStatus,
            limitations = "Matriz construída sem notas arbitrárias. Metadados estruturados para consumo do Evolution Engine."
        )

        val packageId = "PKG-${UUID.randomUUID().toString().take(12).uppercase()}"
        val auditRequestId = UUID.randomUUID().toString()

        val limitationsList = mutableListOf(
            "Pacote de Evidências gerado estritamente para avaliação prévia de elegibilidade.",
            "Nenhuma promoção de classe ou alteração de estado foi executada.",
            "Itens pendentes de validação metodológica permanecem com valor nulo/pendente."
        )
        limitationsList.addAll(pendingValidationItems)

        val evidencePackage = EvolutionEvidencePackage(
            id = packageId,
            userId = userId,
            generatedAt = evaluationTimestamp,
            coreVersion = coreVersion,
            engineVersion = engineVersion,
            evidenceIds = effectiveEvidences.map { it.id },
            validMetrics = validMetrics,
            invalidMetrics = invalidMetrics,
            expiredEvidenceIds = expiredEvidenceIds,
            pendingValidationItems = pendingValidationItems,
            validityAssessments = validityAssessments,
            consistencyAssessments = consistencyAssessments,
            repeatabilityAssessments = repeatabilityAssessments,
            continuityAssessments = continuityAssessments,
            longitudinalSequences = longitudinalSequences,
            overallConsistencyStatus = overallConsistency,
            overallRepeatabilityStatus = overallRepeatability,
            overallMaturity = overallMaturity,
            qualityMatrix = qualityMatrix,
            limitations = limitationsList,
            auditReference = auditRequestId,
            isMock = hasMockEvidence || isSimulationMode,
            simulationMode = isSimulationMode
        )

        // 7. AUDIT TRAIL IMUTÁVEL
        val auditLog = DataCoreAuditLog(
            id = UUID.randomUUID().toString(),
            actorType = ActorType.CORE_ENGINE,
            actorId = "EvidenceConsistencyEngineV1",
            action = if (isSimulationMode) "SIMULATION_EVIDENCE_PACKAGE_GENERATED" else "EVOLUTION_EVIDENCE_PACKAGE_GENERATED",
            entityType = "EvolutionEvidencePackage",
            entityId = packageId,
            previousState = null,
            newState = "USER=$userId; VALID_METRICS=${validMetrics.size}; EXPIRED_EVS=${expiredEvidenceIds.size}; MATURITY=${overallMaturity.maturityStatus}; CONSISTENCY=$overallConsistency; SIMULATION=$isSimulationMode",
            timestamp = evaluationTimestamp,
            requestId = auditRequestId,
            systemVersion = engineVersion
        )

        return AppResult.Success(
            ConsistencyEngineResult(
                evidencePackage = evidencePackage,
                auditLog = auditLog
            )
        )
    }
}
