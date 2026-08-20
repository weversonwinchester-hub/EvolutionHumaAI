package com.example.core.biomechanical.capture

import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI BIOMECHANICAL CAPTURE SESSION
 *
 * Sessão de captura biomecânica e visão computacional.
 * Sessões concluídas são estritamente imutáveis.
 */
enum class CaptureSessionStatus {
    CREATED,
    READY,
    CAPTURING,
    PAUSED,
    COMPLETED,
    FAILED,
    INVALIDATED,
    SIMULATION
}

enum class DeviceCaptureType {
    SMARTPHONE_CAMERA,
    TABLET_CAMERA,
    EXTERNAL_MONOCULAR_CAMERA,
    STEREO_CAMERA_RIG,
    DEPTH_SENSOR_CAMERA,
    OPTICAL_MOTION_CAPTURE,
    SIMULATION_FEED
}

enum class CameraOrientation {
    PORTRAIT,
    LANDSCAPE,
    PORTRAIT_INVERTED,
    LANDSCAPE_INVERTED
}

enum class CalibrationStatus {
    UNCALIBRATED,
    FACTORY_CALIBRATED,
    RIGOROUSLY_CALIBRATED,
    CONDITIONAL
}

enum class CameraQualityStatus {
    ACCEPTED,
    FLAGGED,
    REJECTED,
    INSUFFICIENT_QUALITY
}

data class CameraCalibration(
    val calibrationId: String = UUID.randomUUID().toString(),
    val cameraId: String,
    val intrinsicParameters: Map<String, Double>? = null,
    val distortionParameters: List<Double>? = null,
    val extrinsicParameters: Map<String, Double>? = null,
    val calibrationMethod: String? = null,
    val calibrationTimestamp: Long? = null,
    val validationStatus: CalibrationStatus = CalibrationStatus.UNCALIBRATED
)

data class CaptureMetadata(
    val cameraDetails: String? = null,
    val calibration: CameraCalibration = CameraCalibration(cameraId = "DEFAULT"),
    val lightingCondition: String = "STANDARD_INDOOR",
    val distanceToSubjectMeters: Double? = null,
    val backgroundComplexity: String = "LOW",
    val processingFlags: List<String> = emptyList()
)

data class BiomechanicalCaptureSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val userId: String,
    val deviceId: String,
    val deviceType: DeviceCaptureType,
    val cameraId: String,
    val resolution: String, // ex: "1920x1080"
    val frameRate: Double, // ex: 60.0 fps
    val timestampStart: Long = System.currentTimeMillis(),
    val timestampEnd: Long? = null,
    val orientation: CameraOrientation = CameraOrientation.PORTRAIT,
    val captureEnvironment: String = "INDOOR",
    val protocolId: String,
    val methodologyId: String,
    val simulationMode: Boolean = false,
    val isMock: Boolean = false,
    val metadata: CaptureMetadata = CaptureMetadata(),
    val status: CaptureSessionStatus = CaptureSessionStatus.CREATED,
    val integrityHash: String = ""
) {
    fun calculateIntegrityHash(): String {
        val payload = "$sessionId|$userId|$deviceId|$deviceType|$cameraId|$resolution|$frameRate|$timestampStart|$timestampEnd|$protocolId|$methodologyId|$simulationMode|$isMock|$status"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun complete(endTime: Long = System.currentTimeMillis()): BiomechanicalCaptureSession {
        val targetStatus = if (simulationMode || isMock) CaptureSessionStatus.SIMULATION else CaptureSessionStatus.COMPLETED
        val completed = this.copy(
            timestampEnd = endTime,
            status = targetStatus
        )
        return completed.copy(integrityHash = completed.calculateIntegrityHash())
    }

    fun invalidate(reason: String): BiomechanicalCaptureSession {
        val invalidated = this.copy(
            timestampEnd = System.currentTimeMillis(),
            status = CaptureSessionStatus.INVALIDATED
        )
        return invalidated.copy(integrityHash = invalidated.calculateIntegrityHash())
    }
}
