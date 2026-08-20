package com.example

import com.example.core.biomechanical.capture.BiomechanicalCaptureSession
import com.example.core.biomechanical.capture.CalibrationStatus
import com.example.core.biomechanical.capture.CameraCalibration
import com.example.core.biomechanical.capture.CameraOrientation
import com.example.core.biomechanical.capture.CameraQualityStatus
import com.example.core.biomechanical.capture.CaptureMetadata
import com.example.core.biomechanical.capture.CaptureSessionStatus
import com.example.core.biomechanical.capture.DeviceCaptureType
import com.example.core.biomechanical.capture.VisionFrame
import com.example.core.biomechanical.engine.BiomechanicalEngineV1
import com.example.core.biomechanical.engine.BiomechanicalExplainer
import com.example.core.biomechanical.evidence.BiomechanicalEvidenceBuilder
import com.example.core.biomechanical.evidence.BiomechanicalEvidenceStatus
import com.example.core.biomechanical.kinematics.AngleUnit
import com.example.core.biomechanical.kinematics.AngularAccelerationCalculator
import com.example.core.biomechanical.kinematics.AngularVelocityCalculator
import com.example.core.biomechanical.kinematics.BodySide
import com.example.core.biomechanical.kinematics.JointAngleCalculator
import com.example.core.biomechanical.kinematics.JointType
import com.example.core.biomechanical.kinematics.LinearKinematicsCalculator
import com.example.core.biomechanical.movement.MovementPatternAnalyzer
import com.example.core.biomechanical.movement.MovementPhaseDetector
import com.example.core.biomechanical.movement.MovementPhaseType
import com.example.core.biomechanical.movement.RepetitionDetector
import com.example.core.biomechanical.movement.RepetitionQualityStatus
import com.example.core.biomechanical.pose.CoordinateSystem
import com.example.core.biomechanical.pose.DefaultDeterministicPoseEstimator
import com.example.core.biomechanical.pose.DepthType
import com.example.core.biomechanical.pose.Landmark
import com.example.core.biomechanical.pose.LandmarkType
import com.example.core.biomechanical.pose.PoseFrame
import com.example.core.biomechanical.privacy.BiomechanicalPrivacyPolicy
import com.example.core.biomechanical.quality.CaptureQualityGate
import com.example.core.biomechanical.quality.FrameQualityEvaluator
import com.example.core.biomechanical.quality.SignalQualityEvaluator
import com.example.core.biomechanical.security.BiomechanicalSecurityBarrier
import com.example.core.biomechanical.symmetry.BilateralSymmetryEvaluator
import com.example.core.biomechanical.tracking.IdentityAnomalyType
import com.example.core.biomechanical.tracking.IdentityConsistencyTracker
import com.example.core.biomechanical.tracking.LandmarkTracker
import com.example.core.biomechanical.tracking.OcclusionDetector
import com.example.core.biomechanical.tracking.OcclusionSeverity
import com.example.core.biomechanical.uncertainty.UncertaintyStatus
import com.example.core.biomechanical.uncertainty.VisionUncertaintyEstimator
import com.example.core.scientific.model.ScientificCallerTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * TESTES COMPLETOS DE HOMOLOGAÇÃO: BIOMECHANICAL & COMPUTER VISION ENGINE V1
 *
 * Cobre no mínimo 35 testes funcionais, testes de privacidade e testes de determinismo.
 */
class BiomechanicalEngineV1Test {

    private val validUserId = "user-biomech-test-123"
    private val validProtocolId = "PROT-SINGLE-LEG-HOP-V1"
    private val validMethodologyId = "METH-ROM-GONIOMETRY-V1"

    @Before
    fun setUp() {
        BiomechanicalSecurityBarrier.clearAuditLogsForTest()
    }

    private fun createValidSession(
        userId: String = validUserId,
        protocolId: String = validProtocolId,
        methodologyId: String = validMethodologyId,
        isMock: Boolean = false,
        simulationMode: Boolean = false
    ): BiomechanicalCaptureSession {
        return BiomechanicalCaptureSession(
            sessionId = UUID.randomUUID().toString(),
            userId = userId,
            deviceId = "PIXEL_8_PRO",
            deviceType = DeviceCaptureType.SMARTPHONE_CAMERA,
            cameraId = "CAM_BACK_0",
            resolution = "1920x1080",
            frameRate = 30.0,
            timestampStart = 1000000L,
            orientation = CameraOrientation.PORTRAIT,
            protocolId = protocolId,
            methodologyId = methodologyId,
            simulationMode = simulationMode,
            isMock = isMock,
            metadata = CaptureMetadata(
                calibration = CameraCalibration(cameraId = "CAM_BACK_0", validationStatus = CalibrationStatus.FACTORY_CALIBRATED)
            )
        )
    }

    private fun createSyntheticFrames(
        sessionId: String,
        count: Int = 10,
        fps: Double = 30.0,
        startTs: Long = 1000000L,
        blurScore: Double = 0.1,
        exposureScore: Double = 0.9,
        visibilityScore: Double = 0.9,
        resolution: String = "1920x1080"
    ): List<VisionFrame> {
        val dt = (1000.0 / fps).toLong()
        return (0 until count).map { i ->
            VisionFrame.createWithHash(
                sessionId = sessionId,
                sequenceNumber = i.toLong(),
                timestamp = startTs + (i * dt),
                frameRate = fps,
                resolution = resolution,
                qualityScore = 0.95,
                blurScore = blurScore,
                exposureScore = exposureScore,
                visibilityScore = visibilityScore
            )
        }
    }

    // =========================================================================
    // 1. CAPTURA E SESSÃO
    // =========================================================================

    @Test
    fun test01_CapturaSemUsuario_EhBloqueada() {
        val session = createValidSession(userId = "")
        val frames = createSyntheticFrames(session.sessionId)

        val result = BiomechanicalEngineV1.processCaptureSession(session, frames)
        assertFalse(result.isSuccess)
        assertEquals(CaptureSessionStatus.INVALIDATED, result.session.status)
        assertTrue(result.errorReason?.contains("userId") == true)
    }

    @Test
    fun test02_SessaoSemProtocolo_EhBloqueada() {
        val session = createValidSession(protocolId = "")
        val frames = createSyntheticFrames(session.sessionId)

        val result = BiomechanicalEngineV1.processCaptureSession(session, frames)
        assertFalse(result.isSuccess)
        assertEquals(CaptureSessionStatus.INVALIDATED, result.session.status)
        assertTrue(result.errorReason?.contains("protocolId") == true)
    }

    @Test
    fun test03_MetodologiaInexistente_BloqueiaEvidenciaOficial() {
        val session = createValidSession(methodologyId = "METH-NON-EXISTENT-V999")
        val frames = createSyntheticFrames(session.sessionId)

        val result = BiomechanicalEngineV1.processCaptureSession(session, frames)
        assertTrue(result.isSuccess)
        assertNotEquals(BiomechanicalEvidenceStatus.VALID, result.evidence.status)
        assertEquals(BiomechanicalEvidenceStatus.PENDING_VALIDATION, result.evidence.status)
    }

    @Test
    fun test04_MetodologiaPendingValidation_NaoProduzEvidenciaValid() {
        val session = createValidSession(methodologyId = "METH-PENDING-UNTESTED")
        val frames = createSyntheticFrames(session.sessionId)

        val result = BiomechanicalEngineV1.processCaptureSession(session, frames)
        assertTrue(result.isSuccess)
        assertNotEquals(BiomechanicalEvidenceStatus.VALID, result.evidence.status)
    }

    // =========================================================================
    // 2. QUALIDADE DE FRAME E SINAL
    // =========================================================================

    @Test
    fun test05_FrameComTimestampFuturo_EhRejeitado() {
        val session = createValidSession()
        val serverNow = 1000000L
        val frames = listOf(
            VisionFrame.createWithHash(sessionId = session.sessionId, sequenceNumber = 0, timestamp = serverNow),
            VisionFrame.createWithHash(sessionId = session.sessionId, sequenceNumber = 1, timestamp = serverNow + 100000L) // +100s no futuro
        )

        val signalEval = SignalQualityEvaluator.evaluateSignalSequence(frames, serverReferenceTimestamp = serverNow)
        assertFalse(signalEval.isTemporalIntegrityValid)
        assertTrue(signalEval.futureTimestampCount > 0)
    }

    @Test
    fun test06_FrameDuplicado_EhDetectado() {
        val session = createValidSession()
        val ts = 1000000L
        val frames = listOf(
            VisionFrame.createWithHash(sessionId = session.sessionId, sequenceNumber = 0, timestamp = ts),
            VisionFrame.createWithHash(sessionId = session.sessionId, sequenceNumber = 1, timestamp = ts) // Duplicado
        )

        val signalEval = SignalQualityEvaluator.evaluateSignalSequence(frames)
        assertFalse(signalEval.isTemporalIntegrityValid)
        assertEquals(1, signalEval.duplicateFramesCount)
    }

    @Test
    fun test07_FrameRateInsuficiente_GeraQualityFailure() {
        val session = createValidSession()
        // 2 frames com 1 segundo de intervalo = 1 FPS (exigido 30)
        val frames = listOf(
            VisionFrame.createWithHash(sessionId = session.sessionId, sequenceNumber = 0, timestamp = 1000L),
            VisionFrame.createWithHash(sessionId = session.sessionId, sequenceNumber = 1, timestamp = 2000L)
        )

        val signalEval = SignalQualityEvaluator.evaluateSignalSequence(frames, requiredFrameRate = 30.0)
        assertFalse(signalEval.isTemporalIntegrityValid)
        assertTrue(signalEval.failedChecks.any { it.contains("SAMPLING_RATE_INSUFFICIENT") })
    }

    @Test
    fun test08_ResolucaoInsuficiente_GeraQualityFailure() {
        val frame = VisionFrame.createWithHash(
            sessionId = "sess-1",
            sequenceNumber = 0,
            timestamp = 1000L,
            resolution = "320x240"
        )
        val eval = FrameQualityEvaluator.evaluateFrame(frame, minResolutionWidth = 1280, minResolutionHeight = 720)
        assertTrue(eval.failedChecks.any { it.contains("RESOLUTION_INSUFFICIENT") })
        assertEquals(CameraQualityStatus.REJECTED, eval.status)
    }

    @Test
    fun test09_BlurExcessivo_GeraFlaggedOuRejected() {
        val frame = VisionFrame.createWithHash(
            sessionId = "sess-1",
            sequenceNumber = 0,
            timestamp = 1000L,
            blurScore = 0.95 // 95% borrado
        )
        val eval = FrameQualityEvaluator.evaluateFrame(frame)
        assertTrue(eval.failedChecks.any { it.contains("EXCESSIVE_BLUR") })
        assertTrue(eval.status in listOf(CameraQualityStatus.FLAGGED, CameraQualityStatus.REJECTED, CameraQualityStatus.INSUFFICIENT_QUALITY))
    }

    // =========================================================================
    // 3. TRACKING, OCLUSÃO E CONSISTÊNCIA DE IDENTIDADE
    // =========================================================================

    @Test
    fun test10_OclusaoSevera_EhDetectada() {
        val estimator = DefaultDeterministicPoseEstimator()
        val frames = createSyntheticFrames("sess-occl", count = 10)
        val poses = frames.map { estimator.estimate(it) }.toMutableList()

        // Forçar perda do joelho esquerdo por 5 frames consecutivos (> 300ms)
        for (i in 2..6) {
            val p = poses[i]
            val modLandmarks = p.landmarks.toMutableMap()
            modLandmarks.remove(LandmarkType.LEFT_KNEE)
            poses[i] = p.copy(landmarks = modLandmarks)
        }

        val occlusions = OcclusionDetector.detectOcclusions(poses)
        assertTrue(occlusions.isNotEmpty())
        assertTrue(occlusions.any { it.bodyPart == LandmarkType.LEFT_KNEE && (it.severity == OcclusionSeverity.MODERATE || it.severity == OcclusionSeverity.SEVERE) })
    }

    @Test
    fun test11_PerdaTemporariaDeLandmark_EhRegistrada() {
        val estimator = DefaultDeterministicPoseEstimator()
        val frames = createSyntheticFrames("sess-temp-loss", count = 5)
        val poses = frames.map { estimator.estimate(it) }.toMutableList()

        // Perda em apenas 1 frame
        val p = poses[2]
        val modLandmarks = p.landmarks.toMutableMap()
        modLandmarks.remove(LandmarkType.LEFT_ELBOW)
        poses[2] = p.copy(landmarks = modLandmarks)

        val occlusions = OcclusionDetector.detectOcclusions(poses, criticalJoints = listOf(LandmarkType.LEFT_ELBOW))
        assertTrue(occlusions.any { it.bodyPart == LandmarkType.LEFT_ELBOW && it.severity == OcclusionSeverity.MINOR })
    }

    @Test
    fun test12_IdentitySwitch_EhDetectado() {
        val estimator = DefaultDeterministicPoseEstimator()
        val frames = createSyntheticFrames("sess-switch", count = 4)
        val poses = frames.map { estimator.estimate(it) }.toMutableList()

        // Posição no frame 1 salta abruptamente (teletransporte / troca de pessoa)
        val p1 = poses[1]
        val mod = p1.landmarks.toMutableMap()
        val hip = mod[LandmarkType.MID_HIP]!!
        mod[LandmarkType.MID_HIP] = hip.copy(x = 0.95, y = 0.95) // Salto enorme em 33ms
        poses[1] = p1.copy(landmarks = mod)

        val identityStatus = IdentityConsistencyTracker.evaluateIdentityConsistency(poses)
        assertTrue(identityStatus.anomaliesDetected.contains(IdentityAnomalyType.IDENTITY_SWITCH))
    }

    @Test
    fun test13_MultiPerson_EhDetectado() {
        val estimator = DefaultDeterministicPoseEstimator()
        val frames = createSyntheticFrames("sess-multi", count = 3)
        val poses = frames.map { estimator.estimate(it) }

        val identityStatus = IdentityConsistencyTracker.evaluateIdentityConsistency(poses, multiPersonDetectedCount = 5)
        assertTrue(identityStatus.anomaliesDetected.contains(IdentityAnomalyType.MULTI_PERSON_DETECTED))
        assertFalse(identityStatus.isValidSingleIdentity)
    }

    @Test
    fun test14_LandmarkEsquerdoDireito_MantemIdentidade() {
        val estimator = DefaultDeterministicPoseEstimator()
        val frames = createSyntheticFrames("sess-bilateral", count = 3)
        val poses = frames.map { estimator.estimate(it) }

        val identityStatus = IdentityConsistencyTracker.evaluateIdentityConsistency(poses)
        assertEquals(1.0, identityStatus.lateralConsistencyScore, 0.001)
        assertFalse(identityStatus.anomaliesDetected.contains(IdentityAnomalyType.LATERAL_INVERSION))
    }

    // =========================================================================
    // 4. CINEMÁTICA (ÂNGULOS, VELOCIDADE, ACELERAÇÃO, LINEAR, ROM)
    // =========================================================================

    @Test
    fun test15_AnguloArticular_EhCalculadoDeterministicamente() {
        val pA = Landmark(type = LandmarkType.LEFT_HIP, x = 0.0, y = 1.0)
        val pB = Landmark(type = LandmarkType.LEFT_KNEE, x = 0.0, y = 0.0) // Vértice
        val pC = Landmark(type = LandmarkType.LEFT_ANKLE, x = 1.0, y = 0.0)

        // Triângulo retângulo: ângulo de 90 graus
        val angleDeg = JointAngleCalculator.calculateAngle(pA, pB, pC, AngleUnit.DEGREES)
        assertEquals(90.0, angleDeg, 0.001)
    }

    @Test
    fun test16_VelocidadeAngular_EhDerivadaCorretamente() {
        val angle1 = com.example.core.biomechanical.kinematics.JointAngleMeasurement(
            joint = JointType.LEFT_KNEE,
            side = BodySide.LEFT,
            angle = 90.0,
            unit = AngleUnit.DEGREES,
            timestamp = 1000L,
            confidence = 1.0,
            uncertainty = 1.5,
            methodId = "METH",
            sourceFrameIds = listOf("f1")
        )
        val angle2 = angle1.copy(
            measurementId = UUID.randomUUID().toString(),
            angle = 120.0, // +30 graus
            timestamp = 1100L // em 100ms = 0.1s
        )

        val vels = AngularVelocityCalculator.calculateAngularVelocities(listOf(angle1, angle2))
        assertEquals(1, vels.size)
        // 30 deg / 0.1 s = 300 deg/s
        assertEquals(300.0, vels.first().value, 0.01)
        assertEquals("deg/s", vels.first().unit)
    }

    @Test
    fun test17_AceleraçãoAngular_EhDerivadaCorretamente() {
        val v1 = com.example.core.biomechanical.kinematics.AngularVelocityMeasurement(
            joint = JointType.LEFT_KNEE,
            side = BodySide.LEFT,
            value = 100.0,
            unit = "deg/s",
            timestamp = 1000L,
            sourceMeasurements = listOf("a1", "a2")
        )
        val v2 = v1.copy(
            velocityId = UUID.randomUUID().toString(),
            value = 200.0, // +100 deg/s
            timestamp = 1200L // em 200ms = 0.2s
        )

        val accs = AngularAccelerationCalculator.calculateAngularAccelerations(listOf(v1, v2))
        assertEquals(1, accs.size)
        // 100 / 0.2 = 500 deg/s^2
        assertEquals(500.0, accs.first().value, 0.01)
    }

    @Test
    fun test18_ROM_EhCalculadoDeterministicamente() {
        val measurements = listOf(
            com.example.core.biomechanical.kinematics.JointAngleMeasurement(
                joint = JointType.LEFT_KNEE,
                side = BodySide.LEFT,
                angle = 60.0,
                timestamp = 1000L,
                confidence = 0.9,
                uncertainty = 2.0,
                methodId = "METH",
                sourceFrameIds = listOf("f1")
            ),
            com.example.core.biomechanical.kinematics.JointAngleMeasurement(
                joint = JointType.LEFT_KNEE,
                side = BodySide.LEFT,
                angle = 140.0,
                timestamp = 1500L,
                confidence = 0.95,
                uncertainty = 2.0,
                methodId = "METH",
                sourceFrameIds = listOf("f2")
            )
        )

        val rom = MovementPatternAnalyzer.calculateRangeOfMotion(measurements, JointType.LEFT_KNEE, BodySide.LEFT)
        assertEquals(60.0, rom.minimumAngle, 0.01)
        assertEquals(140.0, rom.maximumAngle, 0.01)
        assertEquals(80.0, rom.totalROM, 0.01)
    }

    @Test
    fun test19_UnidadeAngular_EhValidada() {
        val pA = Landmark(type = LandmarkType.LEFT_HIP, x = 0.0, y = 1.0)
        val pB = Landmark(type = LandmarkType.LEFT_KNEE, x = 0.0, y = 0.0)
        val pC = Landmark(type = LandmarkType.LEFT_ANKLE, x = 1.0, y = 0.0)

        val angleRad = JointAngleCalculator.calculateAngle(pA, pB, pC, AngleUnit.RADIANS)
        assertEquals(Math.PI / 2.0, angleRad, 0.001)
    }

    @Test
    fun test20_Sistema2D_NaoEhApresentadoComo3DMedido() {
        val landmark = Landmark(type = LandmarkType.LEFT_KNEE, x = 0.5, y = 0.5, z = null)
        assertEquals(CoordinateSystem.NORMALIZED_2D, landmark.coordinateSystem)
        assertEquals(DepthType.NONE, landmark.depthType)
    }

    @Test
    fun test21_ProfundidadeEstimada_EhMarcadaComoESTIMATED() {
        val landmark = Landmark(type = LandmarkType.LEFT_KNEE, x = 0.5, y = 0.5, z = 1.2)
        assertEquals(DepthType.ESTIMATED, landmark.depthType)
    }

    @Test
    fun test22_FiltroAplicado_EhRegistrado() {
        val estimator = DefaultDeterministicPoseEstimator()
        val frames = createSyntheticFrames("sess-filter", count = 3)
        val poses = frames.map { estimator.estimate(it) }

        val smoothed = LandmarkTracker.trackAndSmooth(poses)
        assertTrue(smoothed.first().estimatorVersion.contains("EMA-V1.0"))
    }

    @Test
    fun test23_MesmoPipeline_ProduzResultadoDeterministico() {
        val session = createValidSession()
        val frames = createSyntheticFrames(session.sessionId, count = 5)

        val res1 = BiomechanicalEngineV1.processCaptureSession(session, frames)
        val res2 = BiomechanicalEngineV1.processCaptureSession(session, frames)

        assertEquals(res1.evidence.integrityHash, res2.evidence.integrityHash)
        assertEquals(res1.poseSequence.size, res2.poseSequence.size)
    }

    // =========================================================================
    // 5. MOVIMENTO, FASES, REPETIÇÕES E PADRÕES
    // =========================================================================

    @Test
    fun test24_Repeticoes_SaoDetectadasDeterministicamente() {
        val phases = listOf(
            com.example.core.biomechanical.movement.MovementPhase(
                phaseType = MovementPhaseType.ECCENTRIC,
                startTimestamp = 1000L,
                endTimestamp = 1500L,
                startAngle = 160.0,
                endAngle = 80.0,
                peakVelocity = 120.0
            ),
            com.example.core.biomechanical.movement.MovementPhase(
                phaseType = MovementPhaseType.CONCENTRIC,
                startTimestamp = 1500L,
                endTimestamp = 2000L,
                startAngle = 80.0,
                endAngle = 160.0,
                peakVelocity = 140.0
            ),
            com.example.core.biomechanical.movement.MovementPhase(
                phaseType = MovementPhaseType.TRANSITION,
                startTimestamp = 2000L,
                endTimestamp = 2200L,
                startAngle = 160.0,
                endAngle = 160.0
            )
        )

        val reps = RepetitionDetector.detectRepetitions(phases)
        assertEquals(1, reps.size)
        assertEquals(80.0, reps.first().rangeOfMotion, 0.01)
        assertEquals(RepetitionQualityStatus.ACCEPTED, reps.first().qualityStatus)
    }

    @Test
    fun test25_FasesDesconhecidas_PermanecemUNKNOWN() {
        val emptyAngles = listOf(
            com.example.core.biomechanical.kinematics.JointAngleMeasurement(
                joint = JointType.LEFT_KNEE,
                side = BodySide.LEFT,
                angle = 90.0,
                timestamp = 1000L,
                confidence = 0.5,
                uncertainty = 3.0,
                methodId = "METH",
                sourceFrameIds = listOf("f1")
            )
        )
        val phases = MovementPhaseDetector.detectPhases(emptyAngles, emptyList())
        assertEquals(1, phases.size)
        assertEquals(MovementPhaseType.UNKNOWN, phases.first().phaseType)
    }

    @Test
    fun test26_Assimetria_NaoGeraDiagnosticoClinico() {
        val symmetry = BilateralSymmetryEvaluator.evaluateSymmetry(
            metricName = "KNEE_EXTENSION_ROM",
            leftValue = 120.0,
            rightValue = 140.0
        )
        assertFalse(symmetry.isClinicalDiagnosis)
        assertTrue(symmetry.note.contains("NÃO CONSTITUI DIAGNÓSTICO MÉDICO"))
        assertEquals(85.71, symmetry.symmetryIndex, 0.1) // 120 / 140 * 100
    }

    // =========================================================================
    // 6. INCERTEZA, MOCK E SEGURANÇA
    // =========================================================================

    @Test
    fun test27_Confidence_NaoEhConvertidaAutomaticamenteEmAccuracy() {
        val uncert = VisionUncertaintyEstimator.estimateUncertainty(
            metricId = "METH-ROM-GONIOMETRY-V1",
            modelVersion = "V1",
            rawModelConfidence = 0.99, // Alta confiança do modelo
            estimationType = DepthType.ESTIMATED, // Porém 3D estimado em câmera monocular
            calibrationStatus = CalibrationStatus.UNCALIBRATED,
            frameQuality = CameraQualityStatus.ACCEPTED
        )
        // A acurácia metrológica é penalizada pela falta de calibração mesmo com modelo confiante
        assertTrue(uncert.uncertainty >= 5.0)
        assertNotEquals(uncert.confidence, 1.0 - (uncert.uncertainty / 100.0))
    }

    @Test
    fun test28_IncertezaDesconhecida_PermanecePendingValidationOuEstimated() {
        val uncert = VisionUncertaintyEstimator.estimateUncertainty(
            metricId = "METH-ROM",
            modelVersion = "V1",
            rawModelConfidence = 0.30, // Baixíssima confiança
            estimationType = DepthType.NONE,
            calibrationStatus = CalibrationStatus.UNCALIBRATED,
            frameQuality = CameraQualityStatus.INSUFFICIENT_QUALITY
        )
        assertEquals(UncertaintyStatus.INSUFFICIENT_DATA, uncert.status)
    }

    @Test
    fun test29_EvidenciaSemProvenance_NaoEhAceita() {
        val session = createValidSession()
        val qg = CaptureQualityGate.evaluateSession(session, emptyList())
        val uncert = VisionUncertaintyEstimator.estimateUncertainty(
            metricId = session.methodologyId,
            modelVersion = "V1",
            rawModelConfidence = 1.0,
            estimationType = DepthType.NONE,
            calibrationStatus = CalibrationStatus.FACTORY_CALIBRATED,
            frameQuality = CameraQualityStatus.ACCEPTED
        )

        var thrown = false
        try {
            BiomechanicalEvidenceBuilder.buildEvidence(
                session = session,
                sourceFrameIds = emptyList(),
                measurements = emptyList(),
                repetitions = emptyList(),
                qualityGateResult = qg,
                uncertainty = uncert,
                provenanceReference = "   " // Vazia
            )
        } catch (e: IllegalArgumentException) {
            thrown = true
        }
        assertTrue(thrown)
    }

    @Test
    fun test30_Mock_NaoProduzEvidenciaOficial() {
        val session = createValidSession(isMock = true)
        val frames = createSyntheticFrames(session.sessionId)

        val result = BiomechanicalEngineV1.processCaptureSession(session, frames)
        assertTrue(result.isSuccess)
        assertNotEquals(BiomechanicalEvidenceStatus.VALID, result.evidence.status)
        assertEquals(BiomechanicalEvidenceStatus.FLAGGED, result.evidence.status)
    }

    @Test
    fun test31_Cliente_NaoPodeModificarMedicao() {
        val authorized = BiomechanicalSecurityBarrier.checkCanMutateMeasurement(
            callerTier = ScientificCallerTier.CLIENT,
            sessionId = "sess-1",
            userId = validUserId
        )
        assertFalse(authorized)
        val auditLogs = BiomechanicalSecurityBarrier.getAuditLogs()
        assertTrue(auditLogs.any { it.action == "SECURITY_VIOLATION_BIOMECHANICAL_MANIPULATION_ATTEMPT" })
    }

    @Test
    fun test32_IA_NaoPodeModificarMedicao() {
        val authorized = BiomechanicalSecurityBarrier.checkCanMutateMeasurement(
            callerTier = ScientificCallerTier.AI_GATEWAY,
            sessionId = "sess-1",
            userId = validUserId
        )
        assertFalse(authorized)
    }

    @Test
    fun test33_IA_NaoPodeAlterarConfianca() {
        val authorized = BiomechanicalSecurityBarrier.checkCanAlterConfidenceOrUncertainty(
            callerTier = ScientificCallerTier.AI_GATEWAY,
            sessionId = "sess-1",
            userId = validUserId
        )
        assertFalse(authorized)
    }

    @Test
    fun test34_TodaEvidenciaPossuiHashEProvenance() {
        val session = createValidSession()
        val frames = createSyntheticFrames(session.sessionId)

        val result = BiomechanicalEngineV1.processCaptureSession(session, frames)
        assertTrue(result.evidence.integrityHash.isNotBlank())
        assertTrue(result.evidence.provenanceReference.isNotBlank())
        assertEquals(64, result.evidence.integrityHash.length) // SHA-256 hex string
    }

    @Test
    fun test35_AuditoriaEhGeradaNaConclusaoDaSessao() {
        val session = createValidSession()
        val frames = createSyntheticFrames(session.sessionId)

        BiomechanicalEngineV1.processCaptureSession(session, frames)
        val logs = BiomechanicalSecurityBarrier.getAuditLogs()
        assertTrue(logs.any { it.action == "BIOMECHANICAL_SESSION_COMPLETED" && it.sessionId == session.sessionId })
    }

    // =========================================================================
    // 7. PRIVACIDADE E HIGIENIZAÇÃO DE DADOS
    // =========================================================================

    @Test
    fun test36_Privacidade_IsolamentoEstritoPorUsuario() {
        val userA = "user-alice-123"
        val userB = "user-bob-456"

        assertTrue(BiomechanicalPrivacyPolicy.validateUserAccess(userA, userA))
        assertFalse(BiomechanicalPrivacyPolicy.validateUserAccess(userA, userB))
        assertFalse(BiomechanicalPrivacyPolicy.validateUserAccess("", userA))
    }

    @Test
    fun test37_Privacidade_ExpurgoDeImagensBrutas() {
        val frames = listOf(
            VisionFrame.createWithHash(sessionId = "s1", sequenceNumber = 0, timestamp = 1000L, imageReference = "file:///data/camera/raw_frame_0.jpg")
        )
        val purged = BiomechanicalPrivacyPolicy.applyRetentionPolicy(frames, purgeRawImages = true)
        assertEquals(null, purged.first().imageReference)
    }

    @Test
    fun test38_Privacidade_DadosBiometricosNaoSaoExpostosParaAIGateway() {
        val session = createValidSession()
        val frames = createSyntheticFrames(session.sessionId)
        val result = BiomechanicalEngineV1.processCaptureSession(session, frames)

        val sanitized = BiomechanicalPrivacyPolicy.sanitizeForAiGateway(result.evidence)
        assertTrue(sanitized.isRedactedForPrivacy)

        val explanation = BiomechanicalExplainer.generateExplanation(result.evidence)
        assertFalse(explanation.contains("raw_frame"))
        assertFalse(explanation.contains("file://"))
    }

    @Test
    fun test39_Privacidade_LogsNaoContemBiometriaBruta() {
        val session = createValidSession()
        val frames = createSyntheticFrames(session.sessionId)
        BiomechanicalEngineV1.processCaptureSession(session, frames)

        val logs = BiomechanicalSecurityBarrier.getAuditLogs()
        logs.forEach { log ->
            assertFalse(log.details.contains("landmarks"))
            assertFalse(log.details.contains("file://"))
        }
    }

    @Test
    fun test40_Determinismo_MesmoInputProduzMesmoHashEContagem() {
        val session = createValidSession()
        val frames = createSyntheticFrames(session.sessionId, count = 6)

        val resA = BiomechanicalEngineV1.processCaptureSession(session, frames)
        val resB = BiomechanicalEngineV1.processCaptureSession(session, frames)

        assertEquals(resA.evidence.integrityHash, resB.evidence.integrityHash)
        assertEquals(resA.evidence.measurements.size, resB.evidence.measurements.size)
        assertEquals(resA.evidence.repetitions.size, resB.evidence.repetitions.size)
    }
}
