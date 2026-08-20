package com.example.core.biomechanical.quality

import com.example.core.biomechanical.capture.BiomechanicalCaptureSession
import com.example.core.biomechanical.capture.CameraQualityStatus
import com.example.core.biomechanical.capture.VisionFrame
import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI CAPTURE QUALITY GATE
 *
 * Consolida a avaliação de qualidade óptica, biomecânica e de sinal.
 * Bloqueia sessões de baixa qualidade antes de alimentar a cadeia de evidências.
 */
data class BiomechanicalQualityGateResult(
    val evaluationId: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val overallStatus: CameraQualityStatus,
    val frameQualityScore: Double,
    val signalQualityScore: Double,
    val passedChecks: List<String>,
    val failedChecks: List<String>,
    val warnings: List<String>,
    val isApprovedForBiomechanicalAnalysis: Boolean,
    val evaluatedAt: Long = System.currentTimeMillis(),
    val integrityHash: String = ""
) {
    fun calculateIntegrityHash(): String {
        val payload = "$evaluationId|$sessionId|$overallStatus|$frameQualityScore|$signalQualityScore|$isApprovedForBiomechanicalAnalysis|$evaluatedAt"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun createWithHash(
            evaluationId: String = UUID.randomUUID().toString(),
            sessionId: String,
            overallStatus: CameraQualityStatus,
            frameQualityScore: Double,
            signalQualityScore: Double,
            passedChecks: List<String>,
            failedChecks: List<String>,
            warnings: List<String>,
            isApprovedForBiomechanicalAnalysis: Boolean,
            evaluatedAt: Long = System.currentTimeMillis()
        ): BiomechanicalQualityGateResult {
            val res = BiomechanicalQualityGateResult(
                evaluationId = evaluationId,
                sessionId = sessionId,
                overallStatus = overallStatus,
                frameQualityScore = frameQualityScore,
                signalQualityScore = signalQualityScore,
                passedChecks = passedChecks,
                failedChecks = failedChecks,
                warnings = warnings,
                isApprovedForBiomechanicalAnalysis = isApprovedForBiomechanicalAnalysis,
                evaluatedAt = evaluatedAt
            )
            return res.copy(integrityHash = res.calculateIntegrityHash())
        }
    }
}

object CaptureQualityGate {

    fun evaluateSession(
        session: BiomechanicalCaptureSession,
        frames: List<VisionFrame>,
        requiredFrameRate: Double = 30.0,
        serverTimestamp: Long = System.currentTimeMillis()
    ): BiomechanicalQualityGateResult {
        val passed = mutableListOf<String>()
        val failed = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (frames.isEmpty()) {
            return BiomechanicalQualityGateResult.createWithHash(
                sessionId = session.sessionId,
                overallStatus = CameraQualityStatus.INSUFFICIENT_QUALITY,
                frameQualityScore = 0.0,
                signalQualityScore = 0.0,
                passedChecks = emptyList(),
                failedChecks = listOf("NO_FRAMES_IN_SESSION"),
                warnings = emptyList(),
                isApprovedForBiomechanicalAnalysis = false,
                evaluatedAt = serverTimestamp
            )
        }

        // 1. Avaliação individual dos frames
        var acceptedFramesCount = 0
        var flaggedFramesCount = 0
        var rejectedFramesCount = 0
        var totalCompositeScore = 0.0

        frames.forEach { frame ->
            val eval = FrameQualityEvaluator.evaluateFrame(frame)
            totalCompositeScore += eval.compositeScore
            when (eval.status) {
                CameraQualityStatus.ACCEPTED -> acceptedFramesCount++
                CameraQualityStatus.FLAGGED -> flaggedFramesCount++
                CameraQualityStatus.REJECTED -> rejectedFramesCount++
                CameraQualityStatus.INSUFFICIENT_QUALITY -> rejectedFramesCount++
            }
        }

        val avgFrameScore = totalCompositeScore / frames.size
        val acceptedRatio = acceptedFramesCount.toDouble() / frames.size
        val rejectedRatio = rejectedFramesCount.toDouble() / frames.size

        if (rejectedRatio > 0.30) {
            failed.add("HIGH_REJECTED_FRAME_RATIO_${String.format("%.1f", rejectedRatio * 100)}%")
        } else if (rejectedRatio > 0.10) {
            warnings.add("MODERATE_REJECTED_FRAME_RATIO_${String.format("%.1f", rejectedRatio * 100)}%")
        } else {
            passed.add("OPTICAL_FRAME_QUALITY_SATISFACTORY")
        }

        // 2. Avaliação de Sinal e Continuidade
        val signalEval = SignalQualityEvaluator.evaluateSignalSequence(
            frames = frames,
            requiredFrameRate = requiredFrameRate,
            serverReferenceTimestamp = serverTimestamp
        )

        passed.addAll(signalEval.passedChecks)
        failed.addAll(signalEval.failedChecks)

        val signalScore = if (signalEval.isTemporalIntegrityValid) 1.0 else {
            (1.0 - (signalEval.failedChecks.size * 0.25)).coerceIn(0.0, 1.0)
        }

        // Determinação do status geral
        val overallStatus = when {
            failed.any { it.contains("FUTURE_TIMESTAMPS") || it.contains("OUT_OF_ORDER") || it.contains("SAMPLING_RATE_INSUFFICIENT") || it.contains("HIGH_REJECTED_FRAME_RATIO") } -> {
                CameraQualityStatus.REJECTED
            }
            failed.isNotEmpty() || warnings.isNotEmpty() || acceptedRatio < 0.80 -> {
                CameraQualityStatus.FLAGGED
            }
            avgFrameScore >= 0.70 && signalEval.isTemporalIntegrityValid -> {
                CameraQualityStatus.ACCEPTED
            }
            else -> CameraQualityStatus.INSUFFICIENT_QUALITY
        }

        val isApproved = overallStatus == CameraQualityStatus.ACCEPTED || overallStatus == CameraQualityStatus.FLAGGED

        return BiomechanicalQualityGateResult.createWithHash(
            sessionId = session.sessionId,
            overallStatus = overallStatus,
            frameQualityScore = avgFrameScore,
            signalQualityScore = signalScore,
            passedChecks = passed,
            failedChecks = failed,
            warnings = warnings,
            isApprovedForBiomechanicalAnalysis = isApproved,
            evaluatedAt = serverTimestamp
        )
    }
}
