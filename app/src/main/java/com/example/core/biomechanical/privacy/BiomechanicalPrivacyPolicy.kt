package com.example.core.biomechanical.privacy

import com.example.core.biomechanical.capture.BiomechanicalCaptureSession
import com.example.core.biomechanical.capture.VisionFrame
import com.example.core.biomechanical.evidence.BiomechanicalEvidence

/**
 * PERFORMAI BIOMECHANICAL PRIVACY POLICY & USER DATA ISOLATION
 *
 * Garante minimização de dados, isolamento por userId, higienização de imagens
 * e proteção de dados biométricos sensíveis frente ao AI Gateway.
 */

data class PrivacySanitizedAiPayload(
    val evidenceId: String,
    val protocolId: String,
    val methodologyId: String,
    val movementSummary: String,
    val repetitionCount: Int,
    val avgRangeOfMotionDegrees: Double,
    val symmetryIndexPercent: Double?,
    val uncertaintySummary: String,
    val qualityStatus: String,
    // Note: NUNCA inclui imagem, bounding box facial ou dados pessoais
    val isRedactedForPrivacy: Boolean = true
)

object BiomechanicalPrivacyPolicy {

    /**
     * Valida isolamento estrito de usuário.
     * Retorna falso se o requisitante tentar acessar dados de outro usuário.
     */
    fun validateUserAccess(requestingUserId: String, targetResourceUserId: String): Boolean {
        if (requestingUserId.isBlank() || targetResourceUserId.isBlank()) return false
        return requestingUserId == targetResourceUserId
    }

    /**
     * Higieniza dados para consumo seguro pelo AI Gateway.
     * Remove qualquer ponteiro de imagem, coordenadas faciais e biometria bruta.
     */
    fun sanitizeForAiGateway(evidence: BiomechanicalEvidence): PrivacySanitizedAiPayload {
        val repCount = evidence.repetitions.size
        val avgRom = if (evidence.measurements.isNotEmpty()) {
            evidence.measurements.map { it.angle }.average()
        } else 0.0

        return PrivacySanitizedAiPayload(
            evidenceId = evidence.evidenceId,
            protocolId = evidence.protocolId,
            methodologyId = evidence.methodologyId,
            movementSummary = "Session processed with ${evidence.measurements.size} kinematic samples across $repCount detected cycles.",
            repetitionCount = repCount,
            avgRangeOfMotionDegrees = avgRom,
            symmetryIndexPercent = evidence.symmetry?.symmetryIndex,
            uncertaintySummary = "Expanded uncertainty: +/- ${String.format("%.1f", evidence.uncertainty.uncertainty)} (Status: ${evidence.uncertainty.status})",
            qualityStatus = evidence.qualityGateResult.overallStatus.name,
            isRedactedForPrivacy = true
        )
    }

    /**
     * Executa expurgo seguro de referências de imagens brutas de acordo com a política de retenção.
     */
    fun applyRetentionPolicy(
        frames: List<VisionFrame>,
        purgeRawImages: Boolean = true
    ): List<VisionFrame> {
        if (!purgeRawImages) return frames
        return frames.map { frame ->
            frame.copy(imageReference = null) // Remove qualquer referência à imagem original após extração de landmarks
        }
    }
}
