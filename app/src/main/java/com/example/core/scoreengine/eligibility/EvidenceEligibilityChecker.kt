package com.example.core.scoreengine.eligibility

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.ValidationStatus
import com.example.core.scoreengine.model.EligibilityCheckResult

/**
 * EvidenceEligibilityChecker: Responsável por verificar se medições e evidências
 * são formalmente elegíveis para processamento pelo Score Engine V1.
 *
 * REGRA CRÍTICA:
 * - Medições inválidas ou rejeitadas NUNCA entram no Score.
 * - Evidências sem Provenance verificada não entram no Score oficial.
 * - Dados Mock são identificados e bloqueados de gerar Score oficial.
 */
object EvidenceEligibilityChecker {

    fun checkEligibility(
        measurements: List<DataCoreMeasurement>,
        evidences: List<DataCoreEvidence>,
        provenances: Map<String, DataCoreProvenance>,
        allowMockForDemo: Boolean = false
    ): EligibilityCheckResult {
        val rejectionReasons = mutableListOf<String>()
        val verifiedEvidenceIds = mutableListOf<String>()
        val verifiedMeasurementIds = mutableListOf<String>()
        var isMockDetected = false

        val evidenceById = evidences.associateBy { it.id }

        for (measurement in measurements) {
            // 1. Verificação de Mock
            if (measurement.isMock) {
                isMockDetected = true
                if (!allowMockForDemo) {
                    rejectionReasons.add("Rejeição: Medição ${measurement.id} é MOCK e não é elegível para Score oficial.")
                    continue
                }
            }

            // 2. Verificação do Status de Validação do Data Core
            if (measurement.validationStatus != ValidationStatus.VALID) {
                rejectionReasons.add(
                    "Rejeição: Medição ${measurement.id} (${measurement.metricId}) não possui status VALID. " +
                    "Status atual: ${measurement.validationStatus}, Motivo: ${measurement.rejectionReason ?: "N/A"}."
                )
                continue
            }

            // 3. Verificação de Evidência Associada
            val matchingEvidence = evidences.find { it.measurementIds.contains(measurement.id) }
            if (matchingEvidence == null) {
                rejectionReasons.add("Rejeição: Medição ${measurement.id} não possui registro de Evidência (Evidence) correspondente no Data Core.")
                continue
            }

            if (matchingEvidence.isMock) {
                isMockDetected = true
                if (!allowMockForDemo) {
                    rejectionReasons.add("Rejeição: Evidência ${matchingEvidence.id} está marcada como MOCK.")
                    continue
                }
            }

            // 4. Verificação de Integridade da Evidência
            if (matchingEvidence.integrityStatus != IntegrityStatus.VALID) {
                rejectionReasons.add(
                    "Rejeição de Integridade: Evidência ${matchingEvidence.id} possui status de integridade '${matchingEvidence.integrityStatus}'."
                )
                continue
            }

            // 5. Verificação da Provenance
            val provenance = provenances[matchingEvidence.provenanceId]
            if (provenance == null) {
                rejectionReasons.add("Rejeição de Provenance: Provenance '${matchingEvidence.provenanceId}' não encontrada para a Evidência ${matchingEvidence.id}.")
                continue
            }

            if (provenance.integrityHash.isNullOrBlank()) {
                rejectionReasons.add("Rejeição de Provenance: Hash de integridade ausente no registro de proveniência ${provenance.id}.")
                continue
            }

            // 6. Verificação de Timestamp
            val currentTime = System.currentTimeMillis()
            if (measurement.timestamp > currentTime + 60_000L) {
                rejectionReasons.add("Rejeição Temporal: Medição ${measurement.id} possui timestamp no futuro.")
                continue
            }

            // Aprovado na checagem de elegibilidade
            verifiedMeasurementIds.add(measurement.id)
            if (!verifiedEvidenceIds.contains(matchingEvidence.id)) {
                verifiedEvidenceIds.add(matchingEvidence.id)
            }
        }

        val isEligible = verifiedMeasurementIds.isNotEmpty() && (allowMockForDemo || !isMockDetected)

        return EligibilityCheckResult(
            isEligible = isEligible,
            verifiedEvidenceIds = verifiedEvidenceIds,
            verifiedMeasurementIds = verifiedMeasurementIds,
            rejectedReasons = rejectionReasons,
            isMockDetected = isMockDetected
        )
    }
}
