package com.example.core.exerciseengine.media.security

import com.example.core.exerciseengine.media.model.ExerciseMediaReference
import com.example.core.exerciseengine.media.model.MediaLifecycleStatus
import com.example.core.exerciseengine.media.registry.ExerciseMediaRegistryV1
import com.example.core.exerciseengine.security.ExerciseCallerTier
import java.security.MessageDigest
import java.util.UUID

/**
 * EVOLUTION HUMAN AI — EXERCISE MEDIA SECURITY BARRIER
 *
 * Garante a integridade, privacidade e controle de autoridade da camada de mídia demonstrativa.
 *
 * REGRAS CRÍTICAS:
 * - CLIENT e AI_GATEWAY são restritos ao modo READ_ONLY.
 * - SYSTEM e ADMIN possuem autoridade de publicação/modificação mediante auditoria.
 * - Nenhuma imagem ou captura do atleta/câmera pode ser inserida nesta camada (preserva BiomechanicalPrivacyPolicy).
 * - Modos de simulação não podem registrar ou alterar mídia oficial.
 * - Toda mutação gera registro imutável com checksum SHA-256.
 */
object ExerciseMediaSecurityBarrier {

    data class MediaAuditEntry(
        val auditId: String = UUID.randomUUID().toString(),
        val action: String,
        val mediaId: String,
        val exerciseId: String,
        val callerTier: ExerciseCallerTier,
        val callerId: String,
        val success: Boolean,
        val reason: String,
        val securityViolation: Boolean = false,
        val simulationMode: Boolean = false,
        val timestamp: Long = System.currentTimeMillis(),
        val checksumSha256: String
    )

    private val auditLogs = mutableListOf<MediaAuditEntry>()

    fun recordAudit(
        action: String,
        mediaId: String,
        exerciseId: String,
        callerTier: ExerciseCallerTier,
        callerId: String,
        success: Boolean,
        reason: String,
        securityViolation: Boolean = false,
        simulationMode: Boolean = false
    ): MediaAuditEntry {
        val payload = "$action|$mediaId|$exerciseId|$callerTier|$callerId|$success|$reason|$securityViolation|$simulationMode"
        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val entry = MediaAuditEntry(
            action = action,
            mediaId = mediaId,
            exerciseId = exerciseId,
            callerTier = callerTier,
            callerId = callerId,
            success = success,
            reason = reason,
            securityViolation = securityViolation,
            simulationMode = simulationMode,
            checksumSha256 = checksum
        )
        synchronized(auditLogs) {
            auditLogs.add(entry)
        }
        return entry
    }

    fun getAuditLogs(): List<MediaAuditEntry> = synchronized(auditLogs) {
        auditLogs.toList()
    }

    fun registerOrUpdateMedia(
        callerTier: ExerciseCallerTier,
        callerId: String,
        media: ExerciseMediaReference,
        simulationMode: Boolean = false
    ): Boolean {
        if (simulationMode) {
            recordAudit(
                action = "MUTATE_MEDIA",
                mediaId = media.mediaId,
                exerciseId = media.exerciseId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "SIMULATION_MODE_CANNOT_ALTER_OFFICIAL_MEDIA_CATALOG",
                securityViolation = true,
                simulationMode = true
            )
            return false
        }

        if (callerTier == ExerciseCallerTier.CLIENT || callerTier == ExerciseCallerTier.AI_GATEWAY) {
            recordAudit(
                action = "SECURITY_VIOLATION_MEDIA_MUTATION_ATTEMPT",
                mediaId = media.mediaId,
                exerciseId = media.exerciseId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "CALLER_TIER_${callerTier}_UNAUTHORIZED_FOR_MEDIA_MUTATION",
                securityViolation = true
            )
            return false
        }

        val registered = ExerciseMediaRegistryV1.register(media)
        if (registered) {
            recordAudit(
                action = "MEDIA_REGISTERED_OR_UPDATED",
                mediaId = media.mediaId,
                exerciseId = media.exerciseId,
                callerTier = callerTier,
                callerId = callerId,
                success = true,
                reason = "MEDIA_MUTATION_AUTHORIZED"
            )
        } else {
            recordAudit(
                action = "MEDIA_REGISTRATION_REJECTED",
                mediaId = media.mediaId,
                exerciseId = media.exerciseId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "VALIDATION_OR_IMMUTABILITY_RULE_FAILED"
            )
        }
        return registered
    }

    fun resetForTesting() {
        synchronized(auditLogs) {
            auditLogs.clear()
        }
    }
}
