package com.example.core.trialengine.validator

import com.example.core.trialengine.model.TrialPolicy
import com.example.core.trialengine.model.TrialSession
import com.example.core.trialengine.model.TrialSessionStatus

/**
 * PERFORMAI TRIAL ENGINE - STATE TRANSITION & PRECONDITION VALIDATOR
 *
 * Garante que nenhuma sessão transite para estados ilegais e que pré-condições sejam verificadas antes de qualquer execução.
 */
object TrialStateValidator {

    private val validTransitions: Map<TrialSessionStatus, Set<TrialSessionStatus>> = mapOf(
        TrialSessionStatus.CREATED to setOf(
            TrialSessionStatus.READY,
            TrialSessionStatus.CANCELLED,
            TrialSessionStatus.EXPIRED,
            TrialSessionStatus.SIMULATION
        ),
        TrialSessionStatus.READY to setOf(
            TrialSessionStatus.RUNNING,
            TrialSessionStatus.CANCELLED,
            TrialSessionStatus.EXPIRED
        ),
        TrialSessionStatus.RUNNING to setOf(
            TrialSessionStatus.PAUSED,
            TrialSessionStatus.COMPLETED,
            TrialSessionStatus.FAILED,
            TrialSessionStatus.INVALIDATED,
            TrialSessionStatus.CANCELLED,
            TrialSessionStatus.EXPIRED
        ),
        TrialSessionStatus.PAUSED to setOf(
            TrialSessionStatus.RUNNING,
            TrialSessionStatus.CANCELLED,
            TrialSessionStatus.EXPIRED
        ),
        // Estados terminais não transacionam
        TrialSessionStatus.COMPLETED to emptySet(),
        TrialSessionStatus.FAILED to emptySet(),
        TrialSessionStatus.INVALIDATED to emptySet(),
        TrialSessionStatus.CANCELLED to emptySet(),
        TrialSessionStatus.EXPIRED to emptySet(),
        TrialSessionStatus.SIMULATION to setOf(
            TrialSessionStatus.RUNNING,
            TrialSessionStatus.COMPLETED,
            TrialSessionStatus.CANCELLED
        )
    )

    fun isValidTransition(current: TrialSessionStatus, next: TrialSessionStatus): Boolean {
        if (current == next) return true
        return validTransitions[current]?.contains(next) ?: false
    }

    fun validatePreconditions(
        userId: String,
        policy: TrialPolicy?,
        deviceId: String,
        protocolId: String,
        hasActiveSession: Boolean
    ): PreconditionResult {
        if (userId.isBlank()) {
            return PreconditionResult.Failure("Usuário não autenticado.")
        }
        if (policy == null) {
            return PreconditionResult.Failure("Trial Policy inexistente.")
        }
        if (policy.status != "ACTIVE") {
            return PreconditionResult.Failure("Trial Policy inativa ou suspensa (status=${policy.status}).")
        }
        if (hasActiveSession) {
            return PreconditionResult.Failure("Já existe uma sessão de Trial ativa ou em execução para este usuário.")
        }
        if (!policy.allowedDevices.contains(deviceId)) {
            return PreconditionResult.Failure("Dispositivo '$deviceId' não autorizado para a política ${policy.trialPolicyId}.")
        }
        if (policy.protocolId != protocolId) {
            return PreconditionResult.Failure("Protocolo '$protocolId' divergente do protocolo exigido '${policy.protocolId}'.")
        }
        return PreconditionResult.Success
    }

    fun isSessionExpired(session: TrialSession, policy: TrialPolicy, currentTime: Long = System.currentTimeMillis()): Boolean {
        val windowMillis = policy.executionWindowSeconds * 1000L
        return (currentTime - session.startedAt) > windowMillis
    }

    sealed class PreconditionResult {
        data object Success : PreconditionResult()
        data class Failure(val reason: String) : PreconditionResult()
    }
}
