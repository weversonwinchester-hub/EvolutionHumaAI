package com.example.core.biomechanical.capture

import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI VISION FRAME
 *
 * Representa um quadro individual na sequência temporal de captura.
 * Não armazena bytes brutos de imagem desnecessariamente; mantém referências seguras.
 */
data class VisionFrame(
    val frameId: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val sequenceNumber: Long,
    val timestamp: Long, // Epoch ms ou monotonic timestamp em ms
    val imageReference: String? = null, // URI segura ou ponteiro de buffer efêmero
    val frameRate: Double = 30.0,
    val resolution: String = "1920x1080",
    val qualityScore: Double = 1.0, // [0.0, 1.0]
    val blurScore: Double = 0.0, // [0.0, 1.0] 0 = nítido, 1 = extremamente borrado
    val exposureScore: Double = 1.0, // [0.0, 1.0] 1 = ideal
    val visibilityScore: Double = 1.0, // [0.0, 1.0] 1 = corpo totalmente visível
    val isDroppedFrame: Boolean = false,
    val isDuplicateFrame: Boolean = false,
    val integrityHash: String = ""
) {
    fun calculateIntegrityHash(): String {
        val payload = "$frameId|$sessionId|$sequenceNumber|$timestamp|$imageReference|$frameRate|$resolution|$qualityScore|$blurScore|$exposureScore|$visibilityScore"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun createWithHash(
            frameId: String = UUID.randomUUID().toString(),
            sessionId: String,
            sequenceNumber: Long,
            timestamp: Long,
            imageReference: String? = null,
            frameRate: Double = 30.0,
            resolution: String = "1920x1080",
            qualityScore: Double = 1.0,
            blurScore: Double = 0.0,
            exposureScore: Double = 1.0,
            visibilityScore: Double = 1.0,
            isDroppedFrame: Boolean = false,
            isDuplicateFrame: Boolean = false
        ): VisionFrame {
            val frame = VisionFrame(
                frameId = frameId,
                sessionId = sessionId,
                sequenceNumber = sequenceNumber,
                timestamp = timestamp,
                imageReference = imageReference,
                frameRate = frameRate,
                resolution = resolution,
                qualityScore = qualityScore,
                blurScore = blurScore,
                exposureScore = exposureScore,
                visibilityScore = visibilityScore,
                isDroppedFrame = isDroppedFrame,
                isDuplicateFrame = isDuplicateFrame
            )
            return frame.copy(integrityHash = frame.calculateIntegrityHash())
        }
    }
}
