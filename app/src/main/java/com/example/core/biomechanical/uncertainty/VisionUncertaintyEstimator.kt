package com.example.core.biomechanical.uncertainty

import com.example.core.biomechanical.capture.CalibrationStatus
import com.example.core.biomechanical.capture.CameraQualityStatus
import com.example.core.biomechanical.pose.DepthType

/**
 * PERFORMAI VISION MEASUREMENT UNCERTAINTY & CONFIDENCE EVALUATOR
 *
 * REGRA CRÍTICA INEGOCIÁVEL:
 * NUNCA tratar 'confidence' como 'accuracy'.
 * A confiança da rede neural de visão é uma probabilidade bayesiana do modelo e NÃO
 * a precisão física metrológica da medição articular.
 */

enum class UncertaintyStatus {
    MEASURED,
    ESTIMATED,
    LOW_CONFIDENCE,
    INSUFFICIENT_DATA,
    PENDING_VALIDATION
}

data class VisionMeasurementUncertainty(
    val metricId: String,
    val source: String,
    val modelVersion: String,
    val confidence: Double,           // [0.0, 1.0] Confiança do modelo
    val uncertainty: Double,          // Incerteza expandida (ex.: +/- graus ou +/- %)
    val estimationType: DepthType,
    val calibrationStatus: CalibrationStatus,
    val frameQuality: CameraQualityStatus,
    val occlusionImpact: Double,
    val status: UncertaintyStatus
)

object VisionUncertaintyEstimator {

    fun estimateUncertainty(
        metricId: String,
        modelVersion: String,
        rawModelConfidence: Double,
        estimationType: DepthType,
        calibrationStatus: CalibrationStatus,
        frameQuality: CameraQualityStatus,
        occlusionImpact: Double = 0.0
    ): VisionMeasurementUncertainty {
        // Base uncertainty em graus ou percentual
        var baseUncertainty = when (estimationType) {
            DepthType.MEASURED -> 1.5   // Rig 3D / Sensor calibrado
            DepthType.ESTIMATED -> 5.0  // 3D estimado por rede monocular
            DepthType.NONE -> 3.5       // Goniometria 2D planar
        }

        // Penalidade por Calibração
        val calibMultiplier = when (calibrationStatus) {
            CalibrationStatus.RIGOROUSLY_CALIBRATED -> 1.0
            CalibrationStatus.FACTORY_CALIBRATED -> 1.2
            CalibrationStatus.CONDITIONAL -> 1.5
            CalibrationStatus.UNCALIBRATED -> 2.0
        }

        // Penalidade por Qualidade de Frame
        val qualityMultiplier = when (frameQuality) {
            CameraQualityStatus.ACCEPTED -> 1.0
            CameraQualityStatus.FLAGGED -> 1.4
            CameraQualityStatus.REJECTED -> 3.0
            CameraQualityStatus.INSUFFICIENT_QUALITY -> 4.0
        }

        // Penalidade por Oclusão
        val occlusionMultiplier = 1.0 + (occlusionImpact * 2.0)

        // Incerteza combinada final
        val expandedUncertainty = baseUncertainty * calibMultiplier * qualityMultiplier * occlusionMultiplier

        // Status metrológico
        val status = when {
            frameQuality == CameraQualityStatus.INSUFFICIENT_QUALITY || frameQuality == CameraQualityStatus.REJECTED -> {
                UncertaintyStatus.INSUFFICIENT_DATA
            }
            rawModelConfidence < 0.40 -> UncertaintyStatus.LOW_CONFIDENCE
            calibrationStatus == CalibrationStatus.UNCALIBRATED -> UncertaintyStatus.ESTIMATED
            estimationType == DepthType.MEASURED && calibrationStatus == CalibrationStatus.RIGOROUSLY_CALIBRATED -> {
                UncertaintyStatus.MEASURED
            }
            else -> UncertaintyStatus.ESTIMATED
        }

        return VisionMeasurementUncertainty(
            metricId = metricId,
            source = "PERFORMAI_COMPUTER_VISION_PIPELINE",
            modelVersion = modelVersion,
            confidence = rawModelConfidence.coerceIn(0.0, 1.0),
            uncertainty = expandedUncertainty,
            estimationType = estimationType,
            calibrationStatus = calibrationStatus,
            frameQuality = frameQuality,
            occlusionImpact = occlusionImpact,
            status = status
        )
    }
}
