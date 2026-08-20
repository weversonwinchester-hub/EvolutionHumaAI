package com.example.core.evidenceconsistency.validity

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.ValidationStatus
import com.example.core.evidenceconsistency.model.EvidenceValidityAssessment
import com.example.core.evidenceconsistency.model.ValidityStatus
import com.example.core.evidenceconsistency.policy.EvidenceValidityPolicyRegistry

class EvidenceValidityEngine(
    private val policyRegistry: EvidenceValidityPolicyRegistry = EvidenceValidityPolicyRegistry
) {

    fun evaluateEvidence(
        evidence: DataCoreEvidence,
        measurements: List<DataCoreMeasurement>,
        evaluationTimestamp: Long = System.currentTimeMillis()
    ): EvidenceValidityAssessment {
        val primaryMeasurement = measurements.firstOrNull { it.id in evidence.measurementIds }
            ?: measurements.firstOrNull()

        val metricId = primaryMeasurement?.metricId ?: "UNKNOWN_METRIC"
        val protocolId = primaryMeasurement?.protocolId ?: "UNKNOWN_PROTOCOL"

        // 1. Verificação de Integridade Criptográfica e Validação Básica
        if (evidence.integrityStatus != IntegrityStatus.VALID) {
            return EvidenceValidityAssessment(
                evidenceId = evidence.id,
                metricId = metricId,
                protocolId = protocolId,
                capturedAt = evidence.capturedAt,
                evaluatedAt = evaluationTimestamp,
                validityStatus = ValidityStatus.INVALID,
                policyId = "POLICY_INTEGRITY_CHECK",
                policyVersion = "1.0.0",
                expirationTimestamp = null,
                ageMillis = evaluationTimestamp - evidence.capturedAt,
                rejectionReason = "Evidência reprovada na verificação de integridade: status=${evidence.integrityStatus}",
                limitations = "Evidências sem integridade validada são estritamente rejeitadas."
            )
        }

        // 2. Verificação de status de validação das medições associadas
        val invalidMeasurements = measurements.filter { it.id in evidence.measurementIds && it.validationStatus != ValidationStatus.VALID }
        if (invalidMeasurements.isNotEmpty()) {
            return EvidenceValidityAssessment(
                evidenceId = evidence.id,
                metricId = metricId,
                protocolId = protocolId,
                capturedAt = evidence.capturedAt,
                evaluatedAt = evaluationTimestamp,
                validityStatus = ValidityStatus.INVALID,
                policyId = "POLICY_MEASUREMENT_VALIDITY_CHECK",
                policyVersion = "1.0.0",
                expirationTimestamp = null,
                ageMillis = evaluationTimestamp - evidence.capturedAt,
                rejectionReason = "Medição associada com status inválido: ${invalidMeasurements.map { it.validationStatus }}",
                limitations = "Medições com falhas de validação básica não são elegíveis."
            )
        }

        // 3. Verificação de Anomalia Temporal (Timestamp no Futuro)
        val ageMillis = evaluationTimestamp - evidence.capturedAt
        if (ageMillis < 0) {
            return EvidenceValidityAssessment(
                evidenceId = evidence.id,
                metricId = metricId,
                protocolId = protocolId,
                capturedAt = evidence.capturedAt,
                evaluatedAt = evaluationTimestamp,
                validityStatus = ValidityStatus.INVALID,
                policyId = "POLICY_TEMPORAL_CONSISTENCY",
                policyVersion = "1.0.0",
                expirationTimestamp = null,
                ageMillis = ageMillis,
                rejectionReason = "Anomalia temporal detectada: carimbo de captura no futuro (capturedAt=${evidence.capturedAt} > evaluatedAt=$evaluationTimestamp)",
                limitations = "Carimbos temporais futuros são rejeitados imediatamente."
            )
        }

        // 4. Consulta a Política de Validade Temporal para a Métrica/Protocolo
        val policy = policyRegistry.getPolicy(metricId, protocolId)

        if (policy.status != "APPROVED" || policy.validityWindowMillis == null) {
            return EvidenceValidityAssessment(
                evidenceId = evidence.id,
                metricId = metricId,
                protocolId = protocolId,
                capturedAt = evidence.capturedAt,
                evaluatedAt = evaluationTimestamp,
                validityStatus = ValidityStatus.PENDING_VALIDATION,
                policyId = policy.policyId,
                policyVersion = policy.version,
                expirationTimestamp = null,
                ageMillis = ageMillis,
                rejectionReason = null,
                limitations = "PENDING_CORE_METHODOLOGY_DECISION: ${policy.limitations}"
            )
        }

        val windowMillis = policy.validityWindowMillis
        val expirationTimestamp = evidence.capturedAt + windowMillis

        val status = when {
            ageMillis > windowMillis -> ValidityStatus.EXPIRED
            ageMillis >= (windowMillis * 0.8).toLong() -> ValidityStatus.AGING
            else -> ValidityStatus.CURRENT
        }

        return EvidenceValidityAssessment(
            evidenceId = evidence.id,
            metricId = metricId,
            protocolId = protocolId,
            capturedAt = evidence.capturedAt,
            evaluatedAt = evaluationTimestamp,
            validityStatus = status,
            policyId = policy.policyId,
            policyVersion = policy.version,
            expirationTimestamp = expirationTimestamp,
            ageMillis = ageMillis,
            rejectionReason = if (status == ValidityStatus.EXPIRED) "Janela de validade de ${policy.validityUnit} expirada (idade: ${ageMillis / 86400000}d > limite: ${windowMillis / 86400000}d)" else null,
            limitations = policy.limitations
        )
    }
}
