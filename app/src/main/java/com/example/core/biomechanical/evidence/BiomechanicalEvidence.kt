package com.example.core.biomechanical.evidence

import com.example.core.biomechanical.kinematics.JointAngleMeasurement
import com.example.core.biomechanical.movement.FatigueObservation
import com.example.core.biomechanical.movement.MovementRepetition
import com.example.core.biomechanical.quality.BiomechanicalQualityGateResult
import com.example.core.biomechanical.symmetry.BilateralSymmetryResult
import com.example.core.biomechanical.uncertainty.VisionMeasurementUncertainty
import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI BIOMECHANICAL EVIDENCE
 *
 * Pacote estruturado e imutável de evidência biomecânica gerado pela camada de visão.
 * Pronto para envio ao Data Core.
 */

enum class BiomechanicalEvidenceStatus {
    VALID,
    FLAGGED,
    INVALID,
    PENDING_VALIDATION
}

data class BiomechanicalEvidence(
    val evidenceId: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val userId: String,
    val protocolId: String,
    val methodologyId: String,
    val sourceFrameIds: List<String>,
    val measurements: List<JointAngleMeasurement>,
    val repetitions: List<MovementRepetition>,
    val symmetry: BilateralSymmetryResult? = null,
    val fatigue: FatigueObservation? = null,
    val qualityGateResult: BiomechanicalQualityGateResult,
    val uncertainty: VisionMeasurementUncertainty,
    val processingPipelineVersion: String,
    val estimatorVersion: String,
    val isMock: Boolean = false,
    val simulationMode: Boolean = false,
    val status: BiomechanicalEvidenceStatus = BiomechanicalEvidenceStatus.PENDING_VALIDATION,
    val provenanceReference: String = "",
    val integrityHash: String = ""
) {
    fun calculateIntegrityHash(): String {
        val payload = "$sessionId|$userId|$protocolId|$methodologyId|$isMock|$simulationMode|$status|$processingPipelineVersion|$estimatorVersion|$provenanceReference"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun createWithHash(
            evidenceId: String = UUID.randomUUID().toString(),
            sessionId: String,
            userId: String,
            protocolId: String,
            methodologyId: String,
            sourceFrameIds: List<String>,
            measurements: List<JointAngleMeasurement>,
            repetitions: List<MovementRepetition>,
            symmetry: BilateralSymmetryResult? = null,
            fatigue: FatigueObservation? = null,
            qualityGateResult: BiomechanicalQualityGateResult,
            uncertainty: VisionMeasurementUncertainty,
            processingPipelineVersion: String,
            estimatorVersion: String,
            isMock: Boolean = false,
            simulationMode: Boolean = false,
            status: BiomechanicalEvidenceStatus,
            provenanceReference: String
        ): BiomechanicalEvidence {
            val ev = BiomechanicalEvidence(
                evidenceId = evidenceId,
                sessionId = sessionId,
                userId = userId,
                protocolId = protocolId,
                methodologyId = methodologyId,
                sourceFrameIds = sourceFrameIds,
                measurements = measurements,
                repetitions = repetitions,
                symmetry = symmetry,
                fatigue = fatigue,
                qualityGateResult = qualityGateResult,
                uncertainty = uncertainty,
                processingPipelineVersion = processingPipelineVersion,
                estimatorVersion = estimatorVersion,
                isMock = isMock,
                simulationMode = simulationMode,
                status = status,
                provenanceReference = provenanceReference
            )
            return ev.copy(integrityHash = ev.calculateIntegrityHash())
        }
    }
}
