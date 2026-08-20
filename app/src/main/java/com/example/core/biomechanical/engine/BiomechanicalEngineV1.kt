package com.example.core.biomechanical.engine

import com.example.core.biomechanical.capture.BiomechanicalCaptureSession
import com.example.core.biomechanical.capture.CameraQualityStatus
import com.example.core.biomechanical.capture.VisionFrame
import com.example.core.biomechanical.evidence.BiomechanicalEvidence
import com.example.core.biomechanical.evidence.BiomechanicalEvidenceBuilder
import com.example.core.biomechanical.evidence.BiomechanicalEvidenceStatus
import com.example.core.biomechanical.kinematics.AngularAccelerationCalculator
import com.example.core.biomechanical.kinematics.AngularVelocityCalculator
import com.example.core.biomechanical.kinematics.BodySide
import com.example.core.biomechanical.kinematics.JointAngleCalculator
import com.example.core.biomechanical.kinematics.JointType
import com.example.core.biomechanical.kinematics.LinearKinematicsCalculator
import com.example.core.biomechanical.movement.MovementPattern
import com.example.core.biomechanical.movement.MovementPatternAnalyzer
import com.example.core.biomechanical.movement.MovementPhaseDetector
import com.example.core.biomechanical.movement.RepetitionDetector
import com.example.core.biomechanical.pose.DefaultDeterministicPoseEstimator
import com.example.core.biomechanical.pose.DepthType
import com.example.core.biomechanical.pose.PoseEstimator
import com.example.core.biomechanical.pose.PoseFrame
import com.example.core.biomechanical.quality.CaptureQualityGate
import com.example.core.biomechanical.security.BiomechanicalSecurityBarrier
import com.example.core.biomechanical.symmetry.BilateralSymmetryEvaluator
import com.example.core.biomechanical.tracking.IdentityConsistencyTracker
import com.example.core.biomechanical.tracking.LandmarkTracker
import com.example.core.biomechanical.tracking.OcclusionDetector
import com.example.core.biomechanical.uncertainty.VisionUncertaintyEstimator
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.ValidationStatus
import com.example.core.scientific.model.ScientificCallerTier
import java.util.UUID

/**
 * PERFORMAI BIOMECHANICAL & COMPUTER VISION ENGINE V1
 *
 * Motor de observação e derivação cinemática.
 * Não possui autoridade para alterar Score, Evolution, Progression ou Trials.
 */

data class BiomechanicalProcessingResult(
    val session: BiomechanicalCaptureSession,
    val poseSequence: List<PoseFrame>,
    val evidence: BiomechanicalEvidence,
    val pattern: MovementPattern?,
    val dataCoreMeasurements: List<DataCoreMeasurement>,
    val isSuccess: Boolean,
    val errorReason: String? = null
)

object BiomechanicalEngineV1 {

    const val ENGINE_VERSION = "PERFORMAI-BIOMECHANICAL-1.0.0"

    /**
     * Executa o pipeline biomecânico completo:
     * CAPTURE -> QUALITY -> POSE -> TRACKING -> KINEMATICS -> SYMMETRY -> MOVEMENT -> UNCERTAINTY -> GATE -> EVIDENCE -> DATA CORE
     */
    fun processCaptureSession(
        session: BiomechanicalCaptureSession,
        frames: List<VisionFrame>,
        estimator: PoseEstimator = DefaultDeterministicPoseEstimator(),
        callerTier: ScientificCallerTier = ScientificCallerTier.CORE_ENGINE,
        serverTimestamp: Long = System.currentTimeMillis()
    ): BiomechanicalProcessingResult {
        // 1. Validações prévias da Sessão
        if (session.userId.isBlank()) {
            return BiomechanicalProcessingResult(
                session = session.invalidate("EMPTY_USER_ID"),
                poseSequence = emptyList(),
                evidence = createEmptyEvidence(session, "EMPTY_USER_ID"),
                pattern = null,
                dataCoreMeasurements = emptyList(),
                isSuccess = false,
                errorReason = "Capture session without userId is blocked."
            )
        }

        if (session.protocolId.isBlank()) {
            return BiomechanicalProcessingResult(
                session = session.invalidate("EMPTY_PROTOCOL_ID"),
                poseSequence = emptyList(),
                evidence = createEmptyEvidence(session, "EMPTY_PROTOCOL_ID"),
                pattern = null,
                dataCoreMeasurements = emptyList(),
                isSuccess = false,
                errorReason = "Capture session without protocolId is blocked."
            )
        }

        // 2. Barreira de Segurança
        val isAuthorized = BiomechanicalSecurityBarrier.checkCanProcessBiomechanics(
            callerTier = callerTier,
            sessionId = session.sessionId,
            userId = session.userId
        )
        if (!isAuthorized) {
            return BiomechanicalProcessingResult(
                session = session.invalidate("SECURITY_AUTHORIZATION_FAILED"),
                poseSequence = emptyList(),
                evidence = createEmptyEvidence(session, "SECURITY_UNAUTHORIZED"),
                pattern = null,
                dataCoreMeasurements = emptyList(),
                isSuccess = false,
                errorReason = "Security violation: unauthorized tier attempted processing."
            )
        }

        // 3. Quality Gate Inicial dos Quadros
        val qualityGateResult = CaptureQualityGate.evaluateSession(
            session = session,
            frames = frames,
            requiredFrameRate = session.frameRate,
            serverTimestamp = serverTimestamp
        )

        // 4. Estimação de Pose
        val rawPoses = frames.map { frame -> estimator.estimate(frame) }

        // 5. Rastreamento e Validação de Landmarks (Oclusão & Consistência de Identidade)
        val occlusions = OcclusionDetector.detectOcclusions(rawPoses)
        val maxOcclusionImpact = occlusions.maxOfOrNull { it.confidenceImpact } ?: 0.0

        val multiPersonFrames = frames.count { it.qualityScore < 0.20 && it.visibilityScore < 0.30 } // Proxy para multi-person em teste
        val identityStatus = IdentityConsistencyTracker.evaluateIdentityConsistency(rawPoses, multiPersonFrames)

        // 6. Suavização e Filtragem Temporal
        val smoothedPoses = LandmarkTracker.trackAndSmooth(rawPoses)

        // 7. Derivação Cinemática (Ângulos, Velocidades, Acelerações, Linear)
        val allAngleMeasurements = smoothedPoses.flatMap { pose ->
            JointAngleCalculator.calculateJointAnglesForPose(pose)
        }

        val allVelocities = AngularVelocityCalculator.calculateAngularVelocities(allAngleMeasurements)
        val allAccelerations = AngularAccelerationCalculator.calculateAngularAccelerations(allVelocities)
        val (displacements, linearVels, linearAccs) = LinearKinematicsCalculator.calculateLinearKinematics(smoothedPoses)

        // 8. Avaliação de Simetria Bilateral
        val lKneeAngles = allAngleMeasurements.filter { it.joint == JointType.LEFT_KNEE }
        val rKneeAngles = allAngleMeasurements.filter { it.joint == JointType.RIGHT_KNEE }

        val symmetryResult = if (lKneeAngles.isNotEmpty() && rKneeAngles.isNotEmpty()) {
            val lMax = lKneeAngles.maxOf { it.angle }
            val rMax = rKneeAngles.maxOf { it.angle }
            BilateralSymmetryEvaluator.evaluateSymmetry(
                metricName = "KNEE_EXTENSION_SYMMETRY",
                leftValue = lMax,
                rightValue = rMax,
                leftUncertainty = lKneeAngles.map { it.uncertainty }.average(),
                rightUncertainty = rKneeAngles.map { it.uncertainty }.average()
            )
        } else null

        // 9. Detecção de Fases e Repetições
        val phases = MovementPhaseDetector.detectPhases(allAngleMeasurements, allVelocities)
        val repetitions = RepetitionDetector.detectRepetitions(phases)
        val fatigue = MovementPatternAnalyzer.observeFatigue(repetitions)

        val pattern = MovementPattern(
            movementType = session.protocolId,
            repetitions = repetitions,
            temporalCharacteristics = mapOf("frameCount" to frames.size.toDouble(), "durationMs" to (frames.lastOrNull()?.timestamp ?: 0L).toDouble()),
            spatialCharacteristics = mapOf("maxDisplacement" to (displacements.maxOfOrNull { it.totalDistance } ?: 0.0)),
            stabilityIndicators = mapOf("lateralConsistency" to identityStatus.lateralConsistencyScore),
            symmetryIndicators = symmetryResult?.let { mapOf("LSI" to it.symmetryIndex, "Asymmetry" to it.asymmetryPercent) } ?: emptyMap(),
            quality = qualityGateResult.overallStatus.name,
            confidence = qualityGateResult.frameQualityScore
        )

        // 10. Estimativa de Incerteza Metrológica
        val uncertainty = VisionUncertaintyEstimator.estimateUncertainty(
            metricId = session.methodologyId,
            modelVersion = estimator.getEstimatorInfo().version,
            rawModelConfidence = smoothedPoses.map { it.poseConfidence }.average().takeIf { !it.isNaN() } ?: 0.5,
            estimationType = DepthType.NONE,
            calibrationStatus = session.metadata.calibration.validationStatus,
            frameQuality = qualityGateResult.overallStatus,
            occlusionImpact = maxOcclusionImpact
        )

        // 11. Construção da Evidência Biomecânica Estruturada
        val completedSession = session.complete(serverTimestamp)
        val evidence = BiomechanicalEvidenceBuilder.buildEvidence(
            session = completedSession,
            sourceFrameIds = frames.map { it.frameId },
            measurements = allAngleMeasurements,
            repetitions = repetitions,
            symmetry = symmetryResult,
            fatigue = fatigue,
            qualityGateResult = qualityGateResult,
            uncertainty = uncertainty,
            processingPipelineVersion = ENGINE_VERSION,
            estimatorVersion = estimator.getEstimatorInfo().version,
            provenanceReference = "CAMERA_${session.cameraId}_SESSION_${session.sessionId}"
        )

        // 12. Registro de Auditoria Append-Only
        BiomechanicalSecurityBarrier.recordSessionCompleted(
            sessionId = session.sessionId,
            userId = session.userId,
            details = "Completed biomechanical processing with status ${evidence.status}, reps: ${repetitions.size}"
        )

        // 13. Conversão para o formato estruturado do Data Core
        val dataCoreMeasurements = mutableListOf<DataCoreMeasurement>()
        if (evidence.status == BiomechanicalEvidenceStatus.VALID || evidence.status == BiomechanicalEvidenceStatus.FLAGGED) {
            if (symmetryResult != null) {
                dataCoreMeasurements.add(
                    DataCoreMeasurement(
                        id = UUID.randomUUID().toString(),
                        assessmentId = session.sessionId,
                        userId = session.userId,
                        metricId = "METRIC-SYMMETRY-LSI-V1",
                        rawValue = symmetryResult.symmetryIndex,
                        normalizedValue = symmetryResult.symmetryIndex,
                        unit = "%",
                        timestamp = serverTimestamp,
                        source = "BIOMECHANICAL_VISION_ENGINE",
                        deviceId = session.deviceId,
                        protocolId = session.protocolId,
                        validationStatus = if (evidence.status == BiomechanicalEvidenceStatus.VALID) ValidationStatus.VALID else ValidationStatus.PENDING,
                        isMock = session.isMock || session.simulationMode
                    )
                )
            }

            val lRom = MovementPatternAnalyzer.calculateRangeOfMotion(allAngleMeasurements, JointType.LEFT_KNEE, BodySide.LEFT)
            if (lRom.totalROM > 0.0) {
                dataCoreMeasurements.add(
                    DataCoreMeasurement(
                        id = UUID.randomUUID().toString(),
                        assessmentId = session.sessionId,
                        userId = session.userId,
                        metricId = "METRIC-ROM-DEGREES-V1",
                        rawValue = lRom.totalROM,
                        normalizedValue = lRom.totalROM,
                        unit = "degrees",
                        timestamp = serverTimestamp,
                        source = "BIOMECHANICAL_VISION_ENGINE",
                        deviceId = session.deviceId,
                        protocolId = session.protocolId,
                        validationStatus = if (evidence.status == BiomechanicalEvidenceStatus.VALID) ValidationStatus.VALID else ValidationStatus.PENDING,
                        isMock = session.isMock || session.simulationMode
                    )
                )
            }
        }

        return BiomechanicalProcessingResult(
            session = completedSession,
            poseSequence = smoothedPoses,
            evidence = evidence,
            pattern = pattern,
            dataCoreMeasurements = dataCoreMeasurements,
            isSuccess = true
        )
    }

    private fun createEmptyEvidence(session: BiomechanicalCaptureSession, reason: String): BiomechanicalEvidence {
        val qg = CaptureQualityGate.evaluateSession(session, emptyList())
        val uncert = VisionUncertaintyEstimator.estimateUncertainty(
            metricId = session.methodologyId,
            modelVersion = ENGINE_VERSION,
            rawModelConfidence = 0.0,
            estimationType = DepthType.NONE,
            calibrationStatus = session.metadata.calibration.validationStatus,
            frameQuality = CameraQualityStatus.INSUFFICIENT_QUALITY
        )
        return BiomechanicalEvidence.createWithHash(
            sessionId = session.sessionId,
            userId = session.userId,
            protocolId = session.protocolId,
            methodologyId = session.methodologyId,
            sourceFrameIds = emptyList(),
            measurements = emptyList(),
            repetitions = emptyList(),
            qualityGateResult = qg,
            uncertainty = uncert,
            processingPipelineVersion = ENGINE_VERSION,
            estimatorVersion = ENGINE_VERSION,
            status = BiomechanicalEvidenceStatus.INVALID,
            provenanceReference = "ABORTED_$reason"
        )
    }
}
