package com.example.core.biomechanical.evidence

import com.example.core.biomechanical.capture.BiomechanicalCaptureSession
import com.example.core.biomechanical.capture.CameraQualityStatus
import com.example.core.biomechanical.kinematics.JointAngleMeasurement
import com.example.core.biomechanical.movement.FatigueObservation
import com.example.core.biomechanical.movement.MovementRepetition
import com.example.core.biomechanical.quality.BiomechanicalQualityGateResult
import com.example.core.biomechanical.symmetry.BilateralSymmetryResult
import com.example.core.biomechanical.uncertainty.VisionMeasurementUncertainty
import com.example.core.scientific.model.MethodologyValidationStatus
import com.example.core.scientific.model.ProtocolValidationStatus
import com.example.core.scientific.registry.ScientificMethodologyRegistry
import com.example.core.scientific.registry.ScientificProtocolRegistry

/**
 * PERFORMAI BIOMECHANICAL EVIDENCE BUILDER
 *
 * Construtor rigoroso de evidência biomecânica com validação junto ao Scientific Registry.
 */
object BiomechanicalEvidenceBuilder {

    fun buildEvidence(
        session: BiomechanicalCaptureSession,
        sourceFrameIds: List<String>,
        measurements: List<JointAngleMeasurement>,
        repetitions: List<MovementRepetition>,
        symmetry: BilateralSymmetryResult? = null,
        fatigue: FatigueObservation? = null,
        qualityGateResult: BiomechanicalQualityGateResult,
        uncertainty: VisionMeasurementUncertainty,
        processingPipelineVersion: String = "PERFORMAI-KINEMATICS-V1.0",
        estimatorVersion: String = "PERFORMAI-VISION-1.0.0",
        provenanceReference: String = "SYSTEM_AUTOMATED_CAPTURE"
    ): BiomechanicalEvidence {
        // 1. Checagem de Proveniência obrigatória
        require(provenanceReference.isNotBlank()) {
            "Evidence must contain a non-blank provenance reference."
        }

        // 2. Validação junto ao Scientific Registry
        val methodology = ScientificMethodologyRegistry.getMethodology(session.methodologyId)
        val protocol = ScientificProtocolRegistry.getProtocol(session.protocolId)

        val isMethodologyValid = methodology != null && methodology.validationStatus == MethodologyValidationStatus.VALIDATED
        val isProtocolValid = protocol != null && protocol.validationStatus == ProtocolValidationStatus.ACTIVE

        val calculatedStatus = when {
            session.isMock || session.simulationMode -> {
                // Mock / Simulação NUNCA produz evidência VALID oficial
                BiomechanicalEvidenceStatus.FLAGGED
            }
            !isMethodologyValid || !isProtocolValid -> {
                BiomechanicalEvidenceStatus.PENDING_VALIDATION
            }
            qualityGateResult.overallStatus == CameraQualityStatus.REJECTED || qualityGateResult.overallStatus == CameraQualityStatus.INSUFFICIENT_QUALITY -> {
                BiomechanicalEvidenceStatus.INVALID
            }
            qualityGateResult.overallStatus == CameraQualityStatus.FLAGGED -> {
                BiomechanicalEvidenceStatus.FLAGGED
            }
            qualityGateResult.isApprovedForBiomechanicalAnalysis && isMethodologyValid && isProtocolValid -> {
                BiomechanicalEvidenceStatus.VALID
            }
            else -> BiomechanicalEvidenceStatus.PENDING_VALIDATION
        }

        return BiomechanicalEvidence.createWithHash(
            sessionId = session.sessionId,
            userId = session.userId,
            protocolId = session.protocolId,
            methodologyId = session.methodologyId,
            sourceFrameIds = sourceFrameIds,
            measurements = measurements,
            repetitions = repetitions,
            symmetry = symmetry,
            fatigue = fatigue,
            qualityGateResult = qualityGateResult,
            uncertainty = uncertainty,
            processingPipelineVersion = processingPipelineVersion,
            estimatorVersion = estimatorVersion,
            isMock = session.isMock,
            simulationMode = session.simulationMode,
            status = calculatedStatus,
            provenanceReference = provenanceReference
        )
    }
}
