package com.example.core.exerciseengine.security

import com.example.core.exerciseengine.model.ExerciseDefinition
import com.example.core.exerciseengine.registry.ExerciseRegistryV1
import java.security.MessageDigest
import java.util.UUID

enum class ExerciseCallerTier {
    CORE_ENGINE,
    SYSTEM,
    ADMIN,
    AI_GATEWAY,
    CLIENT
}

/**
 * EVOLUTION HUMAN AI — EXERCISE SECURITY BARRIER
 *
 * Garante que:
 * - Somente CORE_ENGINE, SYSTEM e ADMIN possuem autoridade para registrar, atualizar ou arquivar exercícios oficiais.
 * - CLIENT e AI_GATEWAY são restritos ao modo leitura/consulta explicativa.
 * - Modos de simulação não podem homologar ou alterar exercícios oficiais.
 * - Todo evento gera log de auditoria imutável com checksum SHA-256.
 */
object ExerciseSecurityBarrier {

    data class ExerciseAuditEntry(
        val auditId: String = UUID.randomUUID().toString(),
        val action: String,
        val targetEntity: String,
        val targetId: String,
        val callerTier: ExerciseCallerTier,
        val callerId: String,
        val success: Boolean,
        val reason: String,
        val securityViolation: Boolean = false,
        val simulationMode: Boolean = false,
        val timestamp: Long = System.currentTimeMillis(),
        val checksum: String
    )

    private val auditLogs: MutableList<ExerciseAuditEntry> = mutableListOf()

    fun recordAudit(
        action: String,
        targetEntity: String,
        targetId: String,
        callerTier: ExerciseCallerTier,
        callerId: String,
        success: Boolean,
        reason: String,
        securityViolation: Boolean = false,
        simulationMode: Boolean = false
    ): ExerciseAuditEntry {
        val payload = "$action|$targetEntity|$targetId|$callerTier|$callerId|$success|$reason|$securityViolation|$simulationMode"
        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val entry = ExerciseAuditEntry(
            action = action,
            targetEntity = targetEntity,
            targetId = targetId,
            callerTier = callerTier,
            callerId = callerId,
            success = success,
            reason = reason,
            securityViolation = securityViolation,
            simulationMode = simulationMode,
            checksum = checksum
        )
        synchronized(auditLogs) {
            auditLogs.add(entry)
        }
        return entry
    }

    fun getAuditLogs(): List<ExerciseAuditEntry> = synchronized(auditLogs) {
        auditLogs.toList()
    }

    fun registerOrUpdateExercise(
        callerTier: ExerciseCallerTier,
        callerId: String,
        exercise: ExerciseDefinition,
        simulationMode: Boolean = false
    ): Boolean {
        if (simulationMode) {
            recordAudit(
                action = "MUTATE_EXERCISE",
                targetEntity = "ExerciseDefinition",
                targetId = exercise.exerciseId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "SIMULATION_MODE_CANNOT_ALTER_OFFICIAL_CATALOG",
                securityViolation = true,
                simulationMode = true
            )
            return false
        }

        if (callerTier != ExerciseCallerTier.CORE_ENGINE && callerTier != ExerciseCallerTier.SYSTEM && callerTier != ExerciseCallerTier.ADMIN) {
            recordAudit(
                action = "SECURITY_VIOLATION_EXERCISE_MUTATION_ATTEMPT",
                targetEntity = "ExerciseDefinition",
                targetId = exercise.exerciseId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "CALLER_TIER_${callerTier}_UNAUTHORIZED_FOR_EXERCISE_MUTATION",
                securityViolation = true
            )
            return false
        }

        val registered = ExerciseRegistryV1.register(exercise)
        if (registered) {
            recordAudit(
                action = "EXERCISE_REGISTERED_OR_UPDATED",
                targetEntity = "ExerciseDefinition",
                targetId = exercise.exerciseId,
                callerTier = callerTier,
                callerId = callerId,
                success = true,
                reason = "EXERCISE_MUTATION_AUTHORIZED"
            )
        } else {
            recordAudit(
                action = "EXERCISE_REGISTRATION_REJECTED",
                targetEntity = "ExerciseDefinition",
                targetId = exercise.exerciseId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "VALIDATION_OR_IMMUTABILITY_RULE_FAILED"
            )
        }
        return registered
    }
}
