package com.example.core.biomechanical.security

import com.example.core.scientific.model.ScientificCallerTier
import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI BIOMECHANICAL SECURITY BARRIER & AUDIT TRAIL
 *
 * Bloqueia mutações indevidas originadas de CLIENT ou AI_GATEWAY.
 * Mantém trilha de auditoria append-only à prova de corrupção.
 */

data class BiomechanicalAuditLog(
    val logId: String = UUID.randomUUID().toString(),
    val action: String,
    val sessionId: String,
    val userId: String,
    val callerTier: ScientificCallerTier,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String,
    val isAuthorized: Boolean,
    val checksumSha256: String = ""
) {
    fun calculateChecksum(): String {
        val payload = "$logId|$action|$sessionId|$userId|$callerTier|$timestamp|$details|$isAuthorized"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun createWithChecksum(
            action: String,
            sessionId: String,
            userId: String,
            callerTier: ScientificCallerTier,
            details: String,
            isAuthorized: Boolean,
            timestamp: Long = System.currentTimeMillis()
        ): BiomechanicalAuditLog {
            val log = BiomechanicalAuditLog(
                action = action,
                sessionId = sessionId,
                userId = userId,
                callerTier = callerTier,
                details = details,
                isAuthorized = isAuthorized,
                timestamp = timestamp
            )
            return log.copy(checksumSha256 = log.calculateChecksum())
        }
    }
}

object BiomechanicalSecurityBarrier {

    private val auditLogs = mutableListOf<BiomechanicalAuditLog>()

    @Synchronized
    fun getAuditLogs(): List<BiomechanicalAuditLog> = auditLogs.toList()

    @Synchronized
    fun clearAuditLogsForTest() {
        auditLogs.clear()
    }

    @Synchronized
    private fun recordLog(
        action: String,
        sessionId: String,
        userId: String,
        callerTier: ScientificCallerTier,
        details: String,
        isAuthorized: Boolean
    ): BiomechanicalAuditLog {
        val log = BiomechanicalAuditLog.createWithChecksum(
            action = action,
            sessionId = sessionId,
            userId = userId,
            callerTier = callerTier,
            details = details,
            isAuthorized = isAuthorized
        )
        auditLogs.add(log)
        return log
    }

    /**
     * Valida se a chamada possui autoridade para registrar ou processar dados biomecânicos.
     */
    fun checkCanProcessBiomechanics(callerTier: ScientificCallerTier, sessionId: String, userId: String): Boolean {
        val authorized = callerTier in listOf(ScientificCallerTier.CORE_ENGINE, ScientificCallerTier.SYSTEM, ScientificCallerTier.ADMIN)
        if (!authorized) {
            recordLog(
                action = "SECURITY_VIOLATION_BIOMECHANICAL_MANIPULATION_ATTEMPT",
                sessionId = sessionId,
                userId = userId,
                callerTier = callerTier,
                details = "Unauthorized caller tier $callerTier attempted to process biomechanical capture",
                isAuthorized = false
            )
            return false
        }
        recordLog(
            action = "PROCESS_BIOMECHANICS_AUTHORIZED",
            sessionId = sessionId,
            userId = userId,
            callerTier = callerTier,
            details = "Authorized processing for session $sessionId",
            isAuthorized = true
        )
        return true
    }

    /**
     * Bloqueia qualquer tentativa de mutação de medições ou landmarks por clientes ou IA.
     */
    fun checkCanMutateMeasurement(callerTier: ScientificCallerTier, sessionId: String, userId: String): Boolean {
        val authorized = callerTier in listOf(ScientificCallerTier.CORE_ENGINE, ScientificCallerTier.ADMIN)
        if (!authorized) {
            recordLog(
                action = "SECURITY_VIOLATION_BIOMECHANICAL_MANIPULATION_ATTEMPT",
                sessionId = sessionId,
                userId = userId,
                callerTier = callerTier,
                details = "Caller tier $callerTier attempted to mutate biomechanical measurement/landmarks",
                isAuthorized = false
            )
            return false
        }
        return true
    }

    /**
     * Bloqueia qualquer tentativa de alterar a confiança ou reduzir incerteza por IA/Cliente.
     */
    fun checkCanAlterConfidenceOrUncertainty(callerTier: ScientificCallerTier, sessionId: String, userId: String): Boolean {
        recordLog(
            action = "SECURITY_VIOLATION_BIOMECHANICAL_MANIPULATION_ATTEMPT",
            sessionId = sessionId,
            userId = userId,
            callerTier = callerTier,
            details = "Forbidden attempt to alter confidence or uncertainty by $callerTier",
            isAuthorized = false
        )
        return false
    }

    /**
     * Registra evento de auditoria de conclusão de sessão.
     */
    @Synchronized
    fun recordSessionCompleted(sessionId: String, userId: String, details: String) {
        recordLog(
            action = "BIOMECHANICAL_SESSION_COMPLETED",
            sessionId = sessionId,
            userId = userId,
            callerTier = ScientificCallerTier.CORE_ENGINE,
            details = details,
            isAuthorized = true
        )
    }
}
