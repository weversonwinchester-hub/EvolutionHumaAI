package com.example.core.evolutionengine.engine

import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.DataCoreAuditLog
import com.example.core.evidenceconsistency.model.EvolutionEvidencePackage
import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.evaluator.ClassEligibilityEvaluator
import com.example.core.evolutionengine.evaluator.RequirementEvaluator
import com.example.core.evolutionengine.model.ClassDefinition
import com.example.core.evolutionengine.model.ClassEligibilityResult
import com.example.core.evolutionengine.model.ClassEligibilityStatus
import com.example.core.evolutionengine.model.EvolutionGap
import com.example.core.evolutionengine.model.EvolutionPolicy
import com.example.core.evolutionengine.model.EvolutionSnapshot
import com.example.core.evolutionengine.model.EvolutionState
import com.example.core.evolutionengine.model.ProgressionStatus
import com.example.core.evolutionengine.policy.EvolutionPolicyRegistry
import com.example.core.scoreengine.model.ScoreSnapshot
import java.util.UUID

/**
 * PERFORMAI EVOLUTION ENGINE V1 - MOTOR CENTRAL DE EVOLUÇÃO
 *
 * Princípios de Autoridade e Governança:
 * 1. O Engine NÃO pergunta "Qual classe parece adequada?", mas sim: "Os requisitos foram comprovadamente satisfeitos?"
 * 2. Somente o Core Engine possui autoridade para computar elegibilidade e evolução.
 * 3. Bloqueio absoluto contra mutações disparadas por CLIENT, USER ou AI_GATEWAY.
 * 4. Isolamento estrito de Mock Data (não gera elegibilidade oficial nem promoção).
 * 5. Não altera automaticamente a classe do atleta (emite Elegibilidade auditada e Gap de evolução).
 */
class EvolutionEngineV1(
    private val policyRegistry: EvolutionPolicyRegistry = EvolutionPolicyRegistry,
    private val classEligibilityEvaluator: ClassEligibilityEvaluator = ClassEligibilityEvaluator(RequirementEvaluator()),
    private val auditLogger: (DataCoreAuditLog) -> Unit = {}
) {

    companion object {
        const val ENGINE_VERSION = "1.0.0-evolution-v1"
        const val CORE_VERSION = "1.0.0-datacore-v1"
    }

    /**
     * Avalia a elegibilidade de um atleta para uma classe-alvo específica
     */
    fun evaluateClass(
        userId: String,
        targetClassId: String,
        currentClassId: String,
        scoreSnapshot: ScoreSnapshot?,
        evidencePackage: EvolutionEvidencePackage?,
        actor: ActorType = ActorType.CORE_ENGINE,
        isMock: Boolean = false,
        simulationMode: Boolean = false,
        evaluationTimestamp: Long = System.currentTimeMillis()
    ): EvolutionSnapshot {
        // Validação de Autoridade
        validateActorAuthority(actor, action = "EVALUATE_CLASS_ELIGIBILITY", entityId = targetClassId)

        val targetClass = ClassCatalog.getClassById(targetClassId)
            ?: throw IllegalArgumentException("Classe alvo desconhecida: $targetClassId")
        val currentClass = ClassCatalog.getClassById(currentClassId)
            ?: ClassCatalog.getInitialClass()

        val policy = policyRegistry.getPolicyForClass(targetClassId)

        // Verificação de Mock Data
        val effectiveIsMock = isMock || simulationMode || (scoreSnapshot?.isMock == true) || (evidencePackage?.isMock == true)
        val effectiveSimulationMode = simulationMode || effectiveIsMock

        // Avaliação de Elegibilidade
        val eligibilityResult = classEligibilityEvaluator.evaluateClassEligibility(
            userId = userId,
            targetClass = targetClass,
            currentClass = currentClass,
            policy = policy,
            scoreSnapshot = scoreSnapshot,
            evidencePackage = evidencePackage,
            evaluationTimestamp = evaluationTimestamp,
            coreVersion = CORE_VERSION
        )

        // Se estiver em modo Mock/Simulação, garante que não seja elegível oficialmente
        val finalEligibilityResult = if (effectiveIsMock && eligibilityResult.status == ClassEligibilityStatus.ELIGIBLE) {
            eligibilityResult.copy(
                status = ClassEligibilityStatus.PENDING_VALIDATION
            )
        } else {
            eligibilityResult
        }

        val snapshotId = "EVOSNAP-${UUID.randomUUID()}"
        val auditRef = "AUDIT-EVO-${UUID.randomUUID()}"

        val snapshot = EvolutionSnapshot(
            id = snapshotId,
            userId = userId,
            currentClass = currentClass.classId,
            evaluatedClass = targetClass.classId,
            eligibilityResult = finalEligibilityResult,
            requirementResults = finalEligibilityResult.requirementResults,
            evidencePackageId = evidencePackage?.id,
            scoreSnapshotId = scoreSnapshot?.id,
            policyVersion = policy.version,
            coreVersion = CORE_VERSION,
            evaluatedAt = evaluationTimestamp,
            auditReference = auditRef,
            isMock = effectiveIsMock,
            simulationMode = effectiveSimulationMode
        )

        // Registro de Auditoria Imutável
        val auditLog = DataCoreAuditLog(
            id = auditRef,
            actorType = actor,
            actorId = "EvolutionEngineV1",
            action = if (effectiveSimulationMode) "EVALUATE_CLASS_SIMULATION" else "EVALUATE_CLASS_OFFICIAL",
            entityType = "EvolutionSnapshot",
            entityId = snapshotId,
            previousState = currentClass.classId,
            newState = "${targetClass.classId}:${finalEligibilityResult.status}",
            timestamp = evaluationTimestamp,
            systemVersion = ENGINE_VERSION
        )
        auditLogger(auditLog)

        return snapshot
    }

    /**
     * Avalia o Estado Geral de Evolução do atleta (Current Class, Next Target, Highest Eligible e Gap)
     */
    fun evaluateEvolutionState(
        userId: String,
        currentClassId: String? = null,
        scoreSnapshot: ScoreSnapshot?,
        evidencePackage: EvolutionEvidencePackage?,
        actor: ActorType = ActorType.CORE_ENGINE,
        isMock: Boolean = false,
        simulationMode: Boolean = false,
        evaluationTimestamp: Long = System.currentTimeMillis()
    ): EvolutionState {
        validateActorAuthority(actor, action = "EVALUATE_EVOLUTION_STATE", entityId = userId)

        val currentClass = if (currentClassId != null) {
            ClassCatalog.getClassById(currentClassId) ?: ClassCatalog.getInitialClass()
        } else {
            ClassCatalog.getInitialClass()
        }

        val nextTargetClass = ClassCatalog.getNextClass(currentClass.classId)

        // Avalia elegibilidade para a próxima classe alvo
        val nextClassSnapshot = if (nextTargetClass != null) {
            evaluateClass(
                userId = userId,
                targetClassId = nextTargetClass.classId,
                currentClassId = currentClass.classId,
                scoreSnapshot = scoreSnapshot,
                evidencePackage = evidencePackage,
                actor = actor,
                isMock = isMock,
                simulationMode = simulationMode,
                evaluationTimestamp = evaluationTimestamp
            )
        } else null

        val activeGap: EvolutionGap? = if (nextTargetClass != null && nextClassSnapshot != null) {
            classEligibilityEvaluator.buildEvolutionGap(nextTargetClass, nextClassSnapshot.eligibilityResult)
        } else null

        val progressionStatus = when {
            nextClassSnapshot == null -> ProgressionStatus.STABLE
            nextClassSnapshot.eligibilityResult.status == ClassEligibilityStatus.ELIGIBLE -> {
                if (nextTargetClass?.trialPolicyId != null) ProgressionStatus.ELIGIBLE_FOR_TRIAL
                else ProgressionStatus.ELIGIBLE_FOR_PROMOTION
            }
            nextClassSnapshot.eligibilityResult.status == ClassEligibilityStatus.BLOCKED -> ProgressionStatus.BLOCKED
            nextClassSnapshot.eligibilityResult.status == ClassEligibilityStatus.PENDING_VALIDATION -> ProgressionStatus.PENDING_VALIDATION
            else -> ProgressionStatus.IN_PROGRESS
        }

        return EvolutionState(
            userId = userId,
            currentClass = currentClass,
            highestEligibleClass = if (nextClassSnapshot?.eligibilityResult?.status == ClassEligibilityStatus.ELIGIBLE) nextTargetClass else currentClass,
            nextTargetClass = nextTargetClass,
            progressionStatus = progressionStatus,
            lastEvaluation = evaluationTimestamp,
            methodologyVersion = ENGINE_VERSION,
            activeGap = activeGap
        )
    }

    /**
     * BARREIRAS DE SEGURANÇA E PROTEÇÃO DE AUTORIDADE
     * Rejeita e audita qualquer tentativa não autorizada do Cliente ou IA.
     */
    fun attemptDirectClassModification(
        userId: String,
        requestedClassId: String,
        actor: ActorType
    ) {
        val auditLog = DataCoreAuditLog(
            id = "SEC-EVO-${UUID.randomUUID()}",
            actorType = actor,
            actorId = actor.name,
            action = "UNAUTHORIZED_CLASS_MODIFICATION_BLOCKED",
            entityType = "EvolutionState",
            entityId = userId,
            previousState = "N/A",
            newState = "REJECTED_ATTEMPT_TO_SET_$requestedClassId",
            timestamp = System.currentTimeMillis(),
            systemVersion = ENGINE_VERSION
        )
        auditLogger(auditLog)
        throw SecurityException("Segurança PERFORMAI: Ator '$actor' não possui autoridade para alterar a classe do atleta.")
    }

    fun attemptDirectRequirementSatisfaction(
        requirementId: String,
        actor: ActorType
    ) {
        val auditLog = DataCoreAuditLog(
            id = "SEC-EVO-${UUID.randomUUID()}",
            actorType = actor,
            actorId = actor.name,
            action = "UNAUTHORIZED_REQUIREMENT_MUTATION_BLOCKED",
            entityType = "EvolutionRequirement",
            entityId = requirementId,
            previousState = "N/A",
            newState = "REJECTED_ATTEMPT_TO_FORCE_SATISFIED",
            timestamp = System.currentTimeMillis(),
            systemVersion = ENGINE_VERSION
        )
        auditLogger(auditLog)
        throw SecurityException("Segurança PERFORMAI: Requisitos só podem ser satisfeitos por avaliação matemática e probatória do Core.")
    }

    fun attemptDirectThresholdModification(
        policyId: String,
        actor: ActorType
    ) {
        val auditLog = DataCoreAuditLog(
            id = "SEC-EVO-${UUID.randomUUID()}",
            actorType = actor,
            actorId = actor.name,
            action = "UNAUTHORIZED_THRESHOLD_MUTATION_BLOCKED",
            entityType = "EvolutionPolicy",
            entityId = policyId,
            previousState = "N/A",
            newState = "REJECTED_THRESHOLD_OVERRIDE",
            timestamp = System.currentTimeMillis(),
            systemVersion = ENGINE_VERSION
        )
        auditLogger(auditLog)
        throw SecurityException("Segurança PERFORMAI: Limiares de políticas são imutáveis e gerenciados exclusivamente pelo Consórcio Científico do Core.")
    }

    fun attemptDirectEligibilityModification(
        userId: String,
        targetClassId: String,
        actor: ActorType
    ) {
        val auditLog = DataCoreAuditLog(
            id = "SEC-EVO-${UUID.randomUUID()}",
            actorType = actor,
            actorId = actor.name,
            action = "UNAUTHORIZED_ELIGIBILITY_MUTATION_BLOCKED",
            entityType = "ClassEligibilityResult",
            entityId = "$userId:$targetClassId",
            previousState = "N/A",
            newState = "REJECTED_FORCED_ELIGIBILITY",
            timestamp = System.currentTimeMillis(),
            systemVersion = ENGINE_VERSION
        )
        auditLogger(auditLog)
        throw SecurityException("Segurança PERFORMAI: Elegibilidade é um resultado probatório não mutável por $actor.")
    }

    private fun validateActorAuthority(actor: ActorType, action: String, entityId: String) {
        if (actor != ActorType.CORE_ENGINE && actor != ActorType.SYSTEM) {
            val auditLog = DataCoreAuditLog(
                id = "SEC-EVO-${UUID.randomUUID()}",
                actorType = actor,
                actorId = actor.name,
                action = "UNAUTHORIZED_${action}_BLOCKED",
                entityType = "EvolutionEngine",
                entityId = entityId,
                previousState = "UNAUTHORIZED",
                newState = "BLOCKED",
                timestamp = System.currentTimeMillis(),
                systemVersion = ENGINE_VERSION
            )
            auditLogger(auditLog)
            throw SecurityException("Acesso Negado: Apenas CORE_ENGINE possui autoridade para executar '$action'. Ator recebido: $actor")
        }
    }
}
