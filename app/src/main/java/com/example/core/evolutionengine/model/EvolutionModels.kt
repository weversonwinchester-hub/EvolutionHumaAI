package com.example.core.evolutionengine.model

import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.MaturityStatus
import com.example.core.evidenceconsistency.model.ValidityStatus

/**
 * PERFORMAI EVOLUTION ENGINE V1 - MODELOS DE DOMÍNIO
 *
 * Princípio Central:
 * O Evolution Engine NÃO pergunta "Qual classe parece adequada para este usuário?",
 * mas sim: "Os requisitos formalmente definidos para determinada classe foram comprovadamente satisfeitos?"
 *
 * Fluxo:
 * DATA CORE -> SCORE ENGINE -> EVIDENCE & CONSISTENCY ENGINE -> EVOLUTION ENGINE -> ELIGIBILITY RESULT -> [FUTURO TRIAL ENGINE]
 */

enum class ClassStatus {
    DEFINED,
    PENDING_VALIDATION,
    ACTIVE,
    DEPRECATED
}

enum class RequirementStatus {
    DEFINED,
    PENDING_VALIDATION,
    ACTIVE,
    DEPRECATED
}

enum class RequirementCategory {
    PERFORMANCE,
    EVIDENCE,
    CONSISTENCY,
    REPEATABILITY,
    MATURITY,
    ADAPTATION,
    BALANCE,
    PROTOCOL,
    TIME,
    TRIAL
}

enum class ComparisonOperator {
    GTE,          // >= Maior ou igual
    LTE,          // <= Menor ou igual
    EQUALS,       // == Igual
    CONTAINS,     // Contém item/protocolo
    MINIMUM_SET,  // Conjunto mínimo de observações
    STATUS_MATCH  // Comparação de status exato (ex: MATURE, STABLE)
}

enum class RequirementStatusResult {
    SATISFIED,              // Requisito comprovadamente atendido com dados e evidências válidas
    NOT_SATISFIED,          // Dados válidos existem, mas o limiar/critério não foi atingido
    INSUFFICIENT_EVIDENCE,  // Não há dados ou evidências suficientes para verificar o requisito
    PENDING_VALIDATION,     // Metodologia/threshold ainda não homologada pela ciência do Core
    INVALID,                // Evidência comprometida ou violada
    NOT_APPLICABLE          // Não aplicável para o contexto atual
}

enum class ClassEligibilityStatus {
    ELIGIBLE,               // Todos os requisitos obrigatórios foram comprovadamente satisfeitos
    NOT_ELIGIBLE,           // Um ou mais requisitos não foram atendidos (com evidências válidas)
    INSUFFICIENT_EVIDENCE,  // Faltam evidências/medições necessárias para avaliar
    PENDING_VALIDATION,     // Há requisitos pendentes de definição metodológica oficial
    BLOCKED                 // Bloqueado por pré-requisitos, trials pendentes ou falhas críticas
}

enum class ProgressionStatus {
    STABLE,                 // Usuário estável na classe atual
    ELIGIBLE_FOR_TRIAL,     // Requisitos atendidos, apto para o futuro Trial Engine
    ELIGIBLE_FOR_PROMOTION, // Elegível (requer decisão futura de transição)
    IN_PROGRESS,            // Requisitos em evolução
    BLOCKED,                // Bloqueios ativos
    PENDING_VALIDATION      // Status dependente de validação metodológica
}

enum class ProgressionMode {
    ALL_MANDATORY_SATISFIED,  // Todos os requisitos com isMandatory=true devem ser SATISFIED
    MULTIDIMENSIONAL_THRESHOLD // Requisitos balanceados em múltiplas dimensões
}

/**
 * Definição formal de uma Classe no Catálogo das 22 Classes
 */
data class ClassDefinition(
    val classId: String,
    val order: Int,
    val name: String,
    val description: String,
    val requirementPolicyId: String,
    val trialPolicyId: String?,
    val version: String,
    val status: ClassStatus
)

/**
 * Requisito individual de evolução
 */
data class EvolutionRequirement(
    val id: String,
    val classId: String,
    val category: RequirementCategory,
    val metricId: String? = null,
    val dimensionId: String? = null,
    val operator: ComparisonOperator = ComparisonOperator.GTE,
    val threshold: Double? = null, // null se PENDING_VALIDATION
    val textThreshold: String? = null,
    val minimumEvidenceCount: Int? = null,
    val validityPolicyId: String? = null,
    val consistencyPolicyId: String? = null,
    val maturityPolicyId: String? = null,
    val protocolRequirements: List<String> = emptyList(),
    val sourceRequirements: List<String> = emptyList(),
    val methodologyVersion: String = "1.0.0-evolution-v1",
    val status: RequirementStatus = RequirementStatus.PENDING_VALIDATION,
    val isMandatory: Boolean = true,
    val description: String = ""
)

/**
 * Explicação transparente de um requisito avaliado
 */
data class RequirementExplanation(
    val whatIsRequired: String,
    val rationale: String,
    val metricId: String?,
    val evidenceIds: List<String>,
    val protocolUsed: String?,
    val methodologyUsed: String,
    val currentValueDescription: String,
    val requiredValueDescription: String,
    val gapDescription: String,
    val limitations: List<String> = emptyList()
)

/**
 * Resultado da avaliação de um requisito individual
 */
data class RequirementResult(
    val requirementId: String,
    val category: RequirementCategory,
    val status: RequirementStatusResult,
    val isMandatory: Boolean,
    val actualValue: Double? = null,
    val expectedValue: Double? = null,
    val actualTextValue: String? = null,
    val expectedTextValue: String? = null,
    val evidenceIds: List<String> = emptyList(),
    val scoreSnapshotId: String? = null,
    val explanation: RequirementExplanation,
    val methodologyVersion: String = "1.0.0-evolution-v1",
    val evaluatedAt: Long = System.currentTimeMillis()
)

/**
 * Resultado completo de elegibilidade para uma classe específica
 */
data class ClassEligibilityResult(
    val userId: String,
    val classId: String,
    val currentClassId: String,
    val status: ClassEligibilityStatus,
    val requirementResults: List<RequirementResult>,
    val blockingRequirements: List<RequirementResult>,
    val satisfiedRequirements: List<RequirementResult>,
    val evidencePackageId: String?,
    val scoreSnapshotId: String?,
    val methodologyVersion: String = "1.0.0-evolution-v1",
    val evaluatedAt: Long = System.currentTimeMillis(),
    val coreVersion: String = "1.0.0-datacore-v1"
)

/**
 * Item individual no resumo de lacunas (Evolution Gap)
 */
data class EvolutionGapItem(
    val category: RequirementCategory,
    val statusSummary: String, // ex: "✓ SATISFIED", "NOT_SATISFIED: 1.45 / 1.80", "PENDING_VALIDATION"
    val requirementCount: Int,
    val satisfiedCount: Int,
    val pendingCount: Int,
    val details: List<String>
)

/**
 * Estrutura formal de Evolution Gap
 */
data class EvolutionGap(
    val targetClassId: String,
    val targetClassName: String,
    val categories: Map<RequirementCategory, EvolutionGapItem>,
    val summary: String,
    val trialStatus: String = "LOCKED"
)

/**
 * Estado oficial de evolução do usuário
 * (O Engine V1 calcula elegibilidade, mas NÃO altera automaticamente a classe do atleta)
 */
data class EvolutionState(
    val userId: String,
    val currentClass: ClassDefinition,
    val highestEligibleClass: ClassDefinition?,
    val nextTargetClass: ClassDefinition?,
    val progressionStatus: ProgressionStatus,
    val lastEvaluation: Long,
    val methodologyVersion: String,
    val activeGap: EvolutionGap?
)

/**
 * Política de progressão de classe imutável e versionada
 */
data class EvolutionPolicy(
    val policyId: String,
    val version: String,
    val classId: String,
    val requirements: List<EvolutionRequirement>,
    val progressionMode: ProgressionMode = ProgressionMode.ALL_MANDATORY_SATISFIED,
    val methodologyStatus: String = "PENDING_VALIDATION", // "APPROVED", "PENDING_VALIDATION"
    val effectiveFrom: Long = System.currentTimeMillis(),
    val source: String = "PERFORMAI_CORE_CONSORTIUM",
    val limitations: List<String> = emptyList()
)

/**
 * Modelo de Requisito de Trial preparado para o futuro Trial Engine
 */
data class TrialRequirement(
    val trialPolicyId: String,
    val classId: String,
    val status: String = "LOCKED", // LOCKED, ELIGIBLE, IN_PROGRESS, PASSED, FAILED
    val requirements: List<String> = emptyList(),
    val methodologyVersion: String = "1.0.0-trial-stub"
)

/**
 * Snapshot imutável de avaliação de evolução (Append-Only)
 */
data class EvolutionSnapshot(
    val id: String,
    val userId: String,
    val currentClass: String,
    val evaluatedClass: String,
    val eligibilityResult: ClassEligibilityResult,
    val requirementResults: List<RequirementResult>,
    val evidencePackageId: String?,
    val scoreSnapshotId: String?,
    val policyVersion: String,
    val coreVersion: String,
    val evaluatedAt: Long,
    val auditReference: String,
    val isMock: Boolean,
    val simulationMode: Boolean
)
