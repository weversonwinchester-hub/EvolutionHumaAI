package com.example.core.scoreengine.model

import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.SourceTier
import com.example.core.datacore.model.ValidationStatus

/**
 * PERFORMAI SCORE ENGINE V1 - Modelos de Domínio
 *
 * Separação rigorosa:
 * DATA -> MEASUREMENT -> METRIC -> EVIDENCE -> SCORE -> EVOLUTION
 *
 * O Score Engine calcula performance quantitativa baseada em evidências.
 * Não promove classes nem avalia maturidade evolutiva (responsabilidade futura do Evolution Engine).
 */

enum class CalculationStatus {
    CALCULATED,          // Calculado com metodologia científica aprovada
    PENDING_VALIDATION,  // Metodologia/fórmula ainda não validada definitivamente pela ciência
    INSUFFICIENT_EVIDENCE, // Evidências insuficientes para gerar score nesta dimensão
    REJECTED,            // Dados violaram critérios de elegibilidade
    MOCK_DEMO            // Dados puramente demonstrativos / simulação isolada
}

enum class ConfidenceStatus {
    VERIFIED_HIGH,
    VERIFIED_MEDIUM,
    UNVERIFIED_LOW,
    UNDETERMINED
}

/**
 * Dimensões de Performance Extensíveis.
 * As 4 dimensões iniciais são FORCE, SPEED, ENDURANCE e MOBILITY.
 */
sealed class DimensionType(val key: String, val displayName: String) {
    object Force : DimensionType("FORCE", "Força")
    object Speed : DimensionType("SPEED", "Velocidade")
    object Endurance : DimensionType("ENDURANCE", "Resistência")
    object Mobility : DimensionType("MOBILITY", "Mobilidade")
    data class Custom(val customKey: String, val customName: String) : DimensionType(customKey, customName)

    companion object {
        val INITIAL_FOUR: List<DimensionType> = listOf(Force, Speed, Endurance, Mobility)

        fun fromKey(key: String): DimensionType = when (key.uppercase()) {
            "FORCE" -> Force
            "SPEED" -> Speed
            "ENDURANCE" -> Endurance
            "MOBILITY" -> Mobility
            else -> Custom(key, key.replaceFirstChar { it.uppercase() })
        }
    }
}

/**
 * Metadados estruturais de confiança consumidos do ReliabilityFramework.
 * Não inventa fórmulas fictícias de confiança.
 */
data class ScoreConfidenceMetadata(
    val sourceTier: SourceTier,
    val integrityStatus: IntegrityStatus,
    val consistencyStatus: String,
    val repeatabilityStatus: String,
    val evidenceCount: Int,
    val confidenceStatus: ConfidenceStatus = ConfidenceStatus.UNDETERMINED,
    val compositeScore: Double? = null,
    val limitations: List<String> = emptyList()
)

/**
 * Explicabilidade completa de um Score oficial.
 * Responde de forma transparente: "Por que esse valor foi produzido?"
 */
data class ScoreExplanation(
    val score: Double?,
    val dimensionOrIndex: String,
    val metricsUsed: List<String>,
    val evidenceUsed: List<String>,
    val formulasUsed: List<String>,
    val normalizationUsed: String,
    val protocolVersions: List<String>,
    val coreVersion: String,
    val scoreVersion: String,
    val limitations: List<String> = emptyList(),
    val notes: String = ""
)

/**
 * Component Score: Contribuição individual de uma métrica validada para uma dimensão.
 */
data class ComponentScore(
    val metricId: String,
    val metricName: String,
    val dimension: String,
    val rawValue: Double,
    val normalizedValue: Double?, // null se PENDING_VALIDATION
    val weight: Double?,          // null se metodologia ainda não definiu peso fixo
    val normalizationMethod: String,
    val validityRange: String,
    val populationContext: String,
    val protocolVersion: String,
    val formulaVersion: String,
    val calculationStatus: CalculationStatus,
    val evidenceId: String?,
    val validationStatus: ValidationStatus
)

/**
 * Dimension Score: Score consolidado de uma dimensão de performance (ex: FORCE, SPEED, ENDURANCE, MOBILITY).
 */
data class DimensionScore(
    val dimension: String,
    val score: Double?, // null se PENDING_VALIDATION
    val contributingMetrics: List<ComponentScore>,
    val formulaVersion: String,
    val evidenceIds: List<String>,
    val confidenceMetadata: ScoreConfidenceMetadata,
    val calculationStatus: CalculationStatus,
    val explanation: ScoreExplanation,
    val calculatedAt: Long = System.currentTimeMillis()
)

/**
 * Performance Index: Índice integrado de performance geral.
 * Se a fórmula oficial de agregação não estiver aprovada pela ciência, value permanece null e status PENDING_VALIDATION.
 */
data class PerformanceIndex(
    val value: Double?, // null se PENDING_VALIDATION (sem scores inventados)
    val formulaVersion: String,
    val dimensionScores: Map<String, DimensionScore>,
    val evidenceIds: List<String>,
    val calculationStatus: CalculationStatus,
    val confidenceMetadata: ScoreConfidenceMetadata,
    val explanation: ScoreExplanation
)

/**
 * Score Snapshot: Entidade imutável que representa o resultado auditável calculado pelo Score Engine.
 */
data class ScoreSnapshot(
    val id: String,
    val userId: String,
    val assessmentId: String?,
    val scoreVersion: String,
    val coreVersion: String,
    val calculatedAt: Long = System.currentTimeMillis(),
    val performanceIndex: PerformanceIndex,
    val dimensionScores: List<DimensionScore>,
    val evidenceIds: List<String>,
    val metricIds: List<String>,
    val calculationStatus: CalculationStatus,
    val confidenceMetadata: ScoreConfidenceMetadata,
    val isMock: Boolean = false,
    val provenanceId: String?,
    val overallExplanation: ScoreExplanation
)

/**
 * Resultado da verificação de elegibilidade de evidências e medições.
 */
data class EligibilityCheckResult(
    val isEligible: Boolean,
    val verifiedEvidenceIds: List<String>,
    val verifiedMeasurementIds: List<String>,
    val rejectedReasons: List<String> = emptyList(),
    val isMockDetected: Boolean = false
)
