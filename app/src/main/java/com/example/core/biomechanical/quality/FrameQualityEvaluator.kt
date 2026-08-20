package com.example.core.biomechanical.quality

import com.example.core.biomechanical.capture.CameraQualityStatus
import com.example.core.biomechanical.capture.VisionFrame

/**
 * PERFORMAI FRAME QUALITY EVALUATOR
 *
 * Avalia atributos ópticos e de enquadramento de cada quadro individual.
 */
object FrameQualityEvaluator {

    data class FrameQualityEvaluation(
        val frameId: String,
        val status: CameraQualityStatus,
        val passedChecks: List<String>,
        val failedChecks: List<String>,
        val blurScore: Double,
        val exposureScore: Double,
        val visibilityScore: Double,
        val compositeScore: Double
    )

    fun evaluateFrame(
        frame: VisionFrame,
        minResolutionWidth: Int = 1280,
        minResolutionHeight: Int = 720,
        maxBlurThreshold: Double = 0.65,
        minExposureThreshold: Double = 0.35,
        minVisibilityThreshold: Double = 0.40
    ): FrameQualityEvaluation {
        val passed = mutableListOf<String>()
        val failed = mutableListOf<String>()

        // 1. Resolução
        val resParts = frame.resolution.split("x", "X")
        if (resParts.size == 2) {
            val width = resParts[0].toIntOrNull() ?: 0
            val height = resParts[1].toIntOrNull() ?: 0
            if (width >= minResolutionWidth && height >= minResolutionHeight) {
                passed.add("RESOLUTION_SUFFICIENT_${frame.resolution}")
            } else {
                failed.add("RESOLUTION_INSUFFICIENT_${frame.resolution}_REQ_${minResolutionWidth}x${minResolutionHeight}")
            }
        } else {
            failed.add("INVALID_RESOLUTION_FORMAT_${frame.resolution}")
        }

        // 2. Blur / Nitidez
        if (frame.blurScore <= maxBlurThreshold) {
            passed.add("BLUR_ACCEPTABLE_${String.format("%.2f", frame.blurScore)}")
        } else {
            failed.add("EXCESSIVE_BLUR_DETECTED_${String.format("%.2f", frame.blurScore)}")
        }

        // 3. Exposição / Iluminação
        if (frame.exposureScore >= minExposureThreshold) {
            passed.add("EXPOSURE_ADEQUATE_${String.format("%.2f", frame.exposureScore)}")
        } else {
            failed.add("POOR_EXPOSURE_DETECTED_${String.format("%.2f", frame.exposureScore)}")
        }

        // 4. Visibilidade do Corpo / Enquadramento
        if (frame.visibilityScore >= minVisibilityThreshold) {
            passed.add("BODY_VISIBILITY_ACCEPTABLE_${String.format("%.2f", frame.visibilityScore)}")
        } else {
            failed.add("BODY_VISIBILITY_INSUFFICIENT_${String.format("%.2f", frame.visibilityScore)}")
        }

        // 5. Score Composto
        val compositeScore = (
            (1.0 - frame.blurScore).coerceIn(0.0, 1.0) * 0.35 +
            frame.exposureScore.coerceIn(0.0, 1.0) * 0.25 +
            frame.visibilityScore.coerceIn(0.0, 1.0) * 0.40
        ).coerceIn(0.0, 1.0)

        val status = when {
            failed.any { it.contains("RESOLUTION_INSUFFICIENT") || it.contains("INVALID_RESOLUTION_FORMAT") } -> CameraQualityStatus.REJECTED
            compositeScore < 0.30 -> CameraQualityStatus.INSUFFICIENT_QUALITY
            failed.isEmpty() && compositeScore >= 0.70 -> CameraQualityStatus.ACCEPTED
            failed.size == 1 && compositeScore >= 0.40 -> CameraQualityStatus.FLAGGED
            else -> CameraQualityStatus.REJECTED
        }

        return FrameQualityEvaluation(
            frameId = frame.frameId,
            status = status,
            passedChecks = passed,
            failedChecks = failed,
            blurScore = frame.blurScore,
            exposureScore = frame.exposureScore,
            visibilityScore = frame.visibilityScore,
            compositeScore = compositeScore
        )
    }
}
