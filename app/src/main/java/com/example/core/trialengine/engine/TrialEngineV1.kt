package com.example.core.trialengine.engine

import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.DataCoreAuditLog
import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.trialengine.evaluator.TrialResultEvaluator
import com.example.core.trialengine.model.RestPeriodRecord
import com.example.core.trialengine.model.TrialAbortReason
import com.example.core.trialengine.model.TrialAttempt
import com.example.core.trialengine.model.TrialAttemptValidationStatus
import com.example.core.trialengine.model.TrialPolicy
import com.example.core.trialengine.model.TrialResult
import com.example.core.trialengine.model.TrialResultStatus
import com.example.core.trialengine.model.TrialSession
import com.example.core.trialengine.model.TrialSessionStatus
import com.example.core.trialengine.model.TrialSnapshot
import com.example.core.trialengine.policy.TrialPolicyRegistry
import com.example.core.trialengine.validator.TrialStateValidator
import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI TRIAL ENGINE V1 - MOTOR CENTRAL DE PROVAS CONTROLADAS
 *
 * Responsável por orquestrar sessões, validar tentativas, auditar execuções e derivar
 * resultados com isolamento total contra manipulações externas ou por inteligência artificial.
 */
class TrialEngineV1(
    private val policyRegistry: TrialPolicyRegistry = TrialPolicyRegistry,
    private val resultEvaluator: TrialResultEvaluator = TrialResultEvaluator(),
    private val auditLogger: (DataCoreAuditLog) -> Unit = {}
) {

    private val coreVersion = "1.0.0-datacore-v1"
    private val engineVersion = "1.0.0-trial-v1"

    // =========================================================================
    // 1. CRIAÇÃO E INICIALIZAÇÃO DE SESSÃO
    // =========================================================================
    fun createSession(
        userId: String,
        classId: String,
        trialPolicyId: String,
        deviceId: String,
        protocolId: String,
        actor: ActorType = ActorType.CORE_ENGINE,
        isMock: Boolean = false,
        simulationMode: Boolean = false,
        activeSessionCheck: Boolean = false
    ): TrialSession {
        validateActorAuthority(actor, "CREATE_TRIAL_SESSION")

        val policy = policyRegistry.getPolicyById(trialPolicyId)
        val precondition = TrialStateValidator.validatePreconditions(
            userId = userId,
            policy = policy,
            deviceId = deviceId,
            protocolId = protocolId,
            hasActiveSession = activeSessionCheck
        )

        if (precondition is TrialStateValidator.PreconditionResult.Failure) {
            logAudit(
                actor = actor,
                action = "TRIAL_SESSION_CREATION_FAILED",
                entityType = "TrialSession",
                entityId = "FAILED-$userId",
                previousState = null,
                newState = "REJECTED: ${precondition.reason}"
            )
            throw IllegalArgumentException("Pré-condição de Trial não satisfeita: ${precondition.reason}")
        }

        val sessionId = "TS-${UUID.randomUUID()}"
        val initialStatus = if (simulationMode) TrialSessionStatus.SIMULATION else TrialSessionStatus.CREATED
        val auditRef = "AUDIT-TS-${sessionId.take(8)}-${System.currentTimeMillis()}"

        val session = TrialSession(
            id = sessionId,
            userId = userId,
            classId = classId,
            trialPolicyId = trialPolicyId,
            policyVersion = policy!!.version,
            startedAt = System.currentTimeMillis(),
            completedAt = null,
            status = initialStatus,
            attemptCount = 0,
            deviceId = deviceId,
            protocolId = protocolId,
            sessionIntegrity = IntegrityStatus.VALID,
            isMock = isMock,
            simulationMode = simulationMode,
            auditReference = auditRef
        )

        logAudit(
            actor = actor,
            action = if (simulationMode) "TRIAL_SESSION_SIMULATION_CREATED" else "TRIAL_SESSION_OFFICIAL_CREATED",
            entityType = "TrialSession",
            entityId = sessionId,
            previousState = null,
            newState = "STATUS=${session.status}; POLICY=$trialPolicyId; USER=$userId"
        )

        return session
    }

    // =========================================================================
    // 2. TRANSIÇÃO DE ESTADOS DA SESSÃO
    // =========================================================================
    fun transitionSession(
        session: TrialSession,
        nextStatus: TrialSessionStatus,
        actor: ActorType = ActorType.CORE_ENGINE
    ): TrialSession {
        validateActorAuthority(actor, "TRANSITION_TRIAL_SESSION")

        if (!TrialStateValidator.isValidTransition(session.status, nextStatus)) {
            logAudit(
                actor = actor,
                action = "INVALID_SESSION_TRANSITION_BLOCKED",
                entityType = "TrialSession",
                entityId = session.id,
                previousState = session.status.name,
                newState = "BLOCKED_ATTEMPT_TO_${nextStatus.name}"
            )
            throw IllegalStateException("Transição inválida de estado de sessão: de ${session.status} para $nextStatus.")
        }

        val updated = session.copy(status = nextStatus)

        logAudit(
            actor = actor,
            action = "TRIAL_SESSION_STATUS_CHANGED",
            entityType = "TrialSession",
            entityId = session.id,
            previousState = session.status.name,
            newState = nextStatus.name
        )

        return updated
    }

    // =========================================================================
    // 3. REGISTRO E VALIDAÇÃO DE TENTATIVA (ATTEMPT)
    // =========================================================================
    fun recordAttempt(
        session: TrialSession,
        attemptNumber: Int,
        rawEvidenceIds: List<String>,
        measurementIds: List<String>,
        resultValue: Double?,
        unit: String?,
        deviceId: String,
        protocolId: String,
        startedAt: Long,
        completedAt: Long,
        restSecondsBeforeAttempt: Long = 0L,
        existingAttempts: List<TrialAttempt> = emptyList(),
        actor: ActorType = ActorType.CORE_ENGINE
    ): Pair<TrialSession, TrialAttempt> {
        validateActorAuthority(actor, "RECORD_TRIAL_ATTEMPT")

        if (session.status != TrialSessionStatus.RUNNING && session.status != TrialSessionStatus.SIMULATION) {
            throw IllegalStateException("Tentativas só podem ser registradas em sessões em status RUNNING ou SIMULATION (atual=${session.status}).")
        }

        val policy = policyRegistry.getPolicyById(session.trialPolicyId)
            ?: throw IllegalStateException("Política ${session.trialPolicyId} não encontrada.")

        // Checagem de tentativa duplicada
        if (existingAttempts.any { it.attemptNumber == attemptNumber }) {
            logAudit(
                actor = actor,
                action = "DUPLICATE_ATTEMPT_REJECTED",
                entityType = "TrialAttempt",
                entityId = "${session.id}-ATT-$attemptNumber",
                previousState = null,
                newState = "DUPLICATE_ATTEMPT_NUMBER_$attemptNumber"
            )
            throw IllegalArgumentException("Tentativa de número $attemptNumber já foi registrada nesta sessão.")
        }

        // Checagem de limite máximo de tentativas
        if (attemptNumber > policy.maximumAttempts) {
            throw IllegalArgumentException("Número de tentativas ($attemptNumber) excede o máximo permitido (${policy.maximumAttempts}).")
        }

        // Checagem de período de descanso obrigatório (se attemptNumber > 1)
        var restRecord: RestPeriodRecord? = null
        if (attemptNumber > 1 && policy.restPeriodSeconds > 0) {
            val validRest = restSecondsBeforeAttempt >= policy.restPeriodSeconds
            restRecord = RestPeriodRecord(
                restStartedAt = startedAt - (restSecondsBeforeAttempt * 1000L),
                restCompletedAt = startedAt,
                requiredRestSeconds = policy.restPeriodSeconds,
                actualRestSeconds = restSecondsBeforeAttempt,
                isValid = validRest
            )
            if (!validRest) {
                logAudit(
                    actor = actor,
                    action = "REST_PERIOD_VIOLATION_BLOCKED",
                    entityType = "TrialAttempt",
                    entityId = "${session.id}-ATT-$attemptNumber",
                    previousState = null,
                    newState = "ACTUAL_REST=${restSecondsBeforeAttempt}s; REQUIRED=${policy.restPeriodSeconds}s"
                )
                throw IllegalStateException("Período mínimo de descanso não atingido ($restSecondsBeforeAttempt s de ${policy.restPeriodSeconds} s exigidos).")
            }
        }

        // Validação de dispositivo e protocolo
        var validationStatus = TrialAttemptValidationStatus.VALID
        var invalidationReason: String? = null

        if (deviceId != session.deviceId) {
            logAudit(
                actor = actor,
                action = "DEVICE_CHANGED_DURING_SESSION",
                entityType = "TrialSession",
                entityId = session.id,
                previousState = session.deviceId,
                newState = deviceId
            )
            validationStatus = TrialAttemptValidationStatus.DEVICE_VIOLATION
            invalidationReason = "Dispositivo alterado durante a execução da sessão ($deviceId != ${session.deviceId})."
        }

        if (protocolId != policy.protocolId) {
            logAudit(
                actor = actor,
                action = "PROTOCOL_VIOLATION_DURING_ATTEMPT",
                entityType = "TrialAttempt",
                entityId = "${session.id}-ATT-$attemptNumber",
                previousState = policy.protocolId,
                newState = protocolId
            )
            validationStatus = TrialAttemptValidationStatus.PROTOCOL_VIOLATION
            invalidationReason = "Violação de protocolo: executado $protocolId, esperado ${policy.protocolId}."
        }

        val attemptId = "ATT-${UUID.randomUUID()}"
        val rawHashInput = "$attemptId:${session.id}:$attemptNumber:$resultValue:$startedAt:$completedAt:$deviceId"
        val integrityHash = MessageDigest.getInstance("SHA-256")
            .digest(rawHashInput.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val attempt = TrialAttempt(
            id = attemptId,
            sessionId = session.id,
            attemptNumber = attemptNumber,
            startedAt = startedAt,
            completedAt = completedAt,
            rawEvidenceIds = rawEvidenceIds,
            measurementIds = measurementIds,
            resultValue = resultValue,
            unit = unit,
            validationStatus = validationStatus,
            invalidationReason = invalidationReason,
            integrityHash = integrityHash,
            deviceId = deviceId,
            protocolId = protocolId,
            restPeriodBeforeAttempt = restRecord,
            createdAt = System.currentTimeMillis()
        )

        val updatedSession = session.copy(attemptCount = session.attemptCount + 1)

        logAudit(
            actor = actor,
            action = "TRIAL_ATTEMPT_RECORDED",
            entityType = "TrialAttempt",
            entityId = attemptId,
            previousState = null,
            newState = "ATTEMPT=$attemptNumber; STATUS=$validationStatus; VALUE=$resultValue"
        )

        return Pair(updatedSession, attempt)
    }

    // =========================================================================
    // 4. ABORTO E INTERRUPÇÃO DE SESSÃO
    // =========================================================================
    fun abortSession(
        session: TrialSession,
        reason: TrialAbortReason,
        actor: ActorType = ActorType.CORE_ENGINE
    ): TrialSession {
        validateActorAuthority(actor, "ABORT_TRIAL_SESSION")

        val targetStatus = when (reason) {
            TrialAbortReason.SESSION_EXPIRED -> TrialSessionStatus.EXPIRED
            TrialAbortReason.INTEGRITY_VIOLATION -> TrialSessionStatus.INVALIDATED
            TrialAbortReason.PROTOCOL_VIOLATION -> TrialSessionStatus.FAILED
            TrialAbortReason.DEVICE_CHANGED,
            TrialAbortReason.DEVICE_DISCONNECTED,
            TrialAbortReason.SENSOR_FAILURE,
            TrialAbortReason.SAFETY_HAZARD,
            TrialAbortReason.DATA_LOSS,
            TrialAbortReason.USER_ABORTED -> TrialSessionStatus.CANCELLED
        }

        val aborted = session.copy(
            status = targetStatus,
            completedAt = System.currentTimeMillis()
        )

        logAudit(
            actor = actor,
            action = "TRIAL_SESSION_ABORTED",
            entityType = "TrialSession",
            entityId = session.id,
            previousState = session.status.name,
            newState = "STATUS=$targetStatus; REASON=$reason"
        )

        return aborted
    }

    // =========================================================================
    // 5. CONCLUSÃO E AVALIAÇÃO DE RESULTADO (TRIAL SNAPSHOT)
    // =========================================================================
    fun completeSession(
        session: TrialSession,
        attempts: List<TrialAttempt>,
        evidences: List<DataCoreEvidence>,
        measurements: List<DataCoreMeasurement>,
        provenances: Map<String, DataCoreProvenance>,
        actor: ActorType = ActorType.CORE_ENGINE
    ): TrialSnapshot {
        validateActorAuthority(actor, "COMPLETE_TRIAL_SESSION")

        val policy = policyRegistry.getPolicyById(session.trialPolicyId)
            ?: throw IllegalStateException("Trial Policy ${session.trialPolicyId} não encontrada.")

        // Checagem de expiração da sessão
        if (TrialStateValidator.isSessionExpired(session, policy)) {
            val expiredSession = abortSession(session, TrialAbortReason.SESSION_EXPIRED, actor)
            val result = TrialResult(
                id = "TR-${UUID.randomUUID()}",
                sessionId = session.id,
                userId = session.userId,
                classId = session.classId,
                bestAttemptId = null,
                qualifyingAttempts = emptyList(),
                failedAttempts = attempts.map { it.id },
                metricResults = emptyMap(),
                evidenceIds = evidences.map { it.id },
                protocolVersion = policy.protocolId,
                trialPolicyVersion = policy.version,
                methodologyVersion = policy.version,
                resultStatus = TrialResultStatus.INVALID,
                explanation = "A sessão expirou antes da conclusão formal.",
                limitations = listOf("SESSION_EXPIRED"),
                calculatedAt = System.currentTimeMillis(),
                auditReference = "AUDIT-EXP-${session.id}",
                isMock = session.isMock,
                simulationMode = session.simulationMode
            )
            return TrialSnapshot(
                id = "SNAP-EXP-${session.id}",
                sessionId = session.id,
                userId = session.userId,
                classId = session.classId,
                trialPolicyId = session.trialPolicyId,
                trialPolicyVersion = policy.version,
                result = result,
                attempts = attempts,
                sessionIntegrity = IntegrityStatus.TAMPERED,
                calculatedAt = System.currentTimeMillis(),
                coreVersion = coreVersion,
                auditReference = "AUDIT-SNAP-EXP-${session.id}",
                isMock = session.isMock,
                simulationMode = session.simulationMode
            )
        }

        val result = resultEvaluator.evaluateSessionResult(
            session = session,
            policy = policy,
            attempts = attempts,
            evidences = evidences,
            measurements = measurements,
            provenances = provenances
        )

        val snapshotId = "TSNAP-${UUID.randomUUID()}"
        val auditRef = "AUDIT-SNAP-${session.id.take(8)}-${System.currentTimeMillis()}"

        val snapshot = TrialSnapshot(
            id = snapshotId,
            sessionId = session.id,
            userId = session.userId,
            classId = session.classId,
            trialPolicyId = session.trialPolicyId,
            trialPolicyVersion = policy.version,
            result = result,
            attempts = attempts,
            sessionIntegrity = session.sessionIntegrity,
            calculatedAt = System.currentTimeMillis(),
            coreVersion = coreVersion,
            auditReference = auditRef,
            isMock = session.isMock,
            simulationMode = session.simulationMode
        )

        logAudit(
            actor = actor,
            action = if (session.simulationMode) "TRIAL_COMPLETED_SIMULATION" else "TRIAL_COMPLETED_OFFICIAL",
            entityType = "TrialSnapshot",
            entityId = snapshotId,
            previousState = session.status.name,
            newState = "STATUS=${result.resultStatus}; POLICY=${policy.trialPolicyId}; ATTEMPTS=${attempts.size}"
        )

        return snapshot
    }

    // =========================================================================
    // 6. BLOQUEIOS DE SEGURANÇA E TENTATIVAS DE MANIPULAÇÃO
    // =========================================================================
    fun attemptDirectResultModification(
        sessionId: String,
        attemptedStatus: TrialResultStatus,
        actor: ActorType = ActorType.CLIENT
    ) {
        logAudit(
            actor = actor,
            action = "UNAUTHORIZED_TRIAL_RESULT_MUTATION_BLOCKED",
            entityType = "TrialResult",
            entityId = sessionId,
            previousState = null,
            newState = "ATTEMPTED_STATUS: $attemptedStatus; REJECTED=True"
        )
        throw SecurityException("Segurança PERFORMAI: Atores externos ($actor) não possuem autoridade para alterar resultados de Trial.")
    }

    fun attemptDirectThresholdModification(
        policyId: String,
        attemptedThreshold: Double,
        actor: ActorType = ActorType.CLIENT
    ) {
        logAudit(
            actor = actor,
            action = "UNAUTHORIZED_THRESHOLD_MUTATION_BLOCKED",
            entityType = "TrialPolicy",
            entityId = policyId,
            previousState = null,
            newState = "ATTEMPTED_THRESHOLD: $attemptedThreshold; REJECTED=True"
        )
        throw SecurityException("Segurança PERFORMAI: Thresholds de Trial são imutáveis e gerenciados exclusivamente pelo Core.")
    }

    private fun validateActorAuthority(actor: ActorType, operation: String) {
        if (actor == ActorType.CLIENT || actor == ActorType.AI_GATEWAY) {
            logAudit(
                actor = actor,
                action = "SECURITY_AUTHORITY_VIOLATION_BLOCKED",
                entityType = "TrialEngine",
                entityId = "OPERATION_$operation",
                previousState = null,
                newState = "ACTOR=$actor; BLOCKED=True"
            )
            throw SecurityException("Segurança PERFORMAI: O ator $actor não tem permissão para invocar $operation no Trial Engine.")
        }
    }

    private fun logAudit(
        actor: ActorType,
        action: String,
        entityType: String,
        entityId: String,
        previousState: String?,
        newState: String?
    ) {
        val log = DataCoreAuditLog(
            id = "AUDIT-TRIAL-${UUID.randomUUID()}",
            actorType = actor,
            actorId = actor.name,
            action = action,
            entityType = entityType,
            entityId = entityId,
            previousState = previousState,
            newState = newState,
            timestamp = System.currentTimeMillis(),
            requestId = "REQ-TR-${UUID.randomUUID().toString().take(8)}",
            systemVersion = engineVersion
        )
        auditLogger(log)
    }
}
