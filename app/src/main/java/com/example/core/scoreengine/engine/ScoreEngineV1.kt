package com.example.core.scoreengine.engine

import com.example.core.datacore.metrics.MetricCatalog
import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.DataCoreAuditLog
import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.SourceTier
import com.example.core.datacore.reliability.ReliabilityFramework
import com.example.core.error.AppError
import com.example.core.error.AppResult
import com.example.core.scoreengine.catalog.ScoreDimensionCatalog
import com.example.core.scoreengine.eligibility.EvidenceEligibilityChecker
import com.example.core.scoreengine.model.CalculationStatus
import com.example.core.scoreengine.model.ComponentScore
import com.example.core.scoreengine.model.ConfidenceStatus
import com.example.core.scoreengine.model.DimensionScore
import com.example.core.scoreengine.model.DimensionType
import com.example.core.scoreengine.model.PerformanceIndex
import com.example.core.scoreengine.model.ScoreConfidenceMetadata
import com.example.core.scoreengine.model.ScoreExplanation
import com.example.core.scoreengine.model.ScoreSnapshot
import com.example.core.scoreengine.normalization.ScoreNormalizationEngine
import java.util.UUID

data class ScoreEngineComputationResult(
    val snapshot: ScoreSnapshot,
    val auditLog: DataCoreAuditLog
)

/**
 * ScoreEngineV1: Implementação oficial do motor de cálculo quantitativo do PERFORMAI.
 *
 * FLUXO DE EXECUÇÃO:
 * Data Core -> Eligibility Check -> Metric Validation -> Normalization ->
 * Component Score -> Dimension Score -> Performance Index -> Score Snapshot -> Audit
 *
 * REGRAS INVIOLÁVEIS:
 * 1. O Score Engine NÃO é a IA e não recebe outputs arbitrários de IA para definir notas.
 * 2. Quando fórmulas ou pesos ainda não foram validados pela ciência, retorna PENDING_VALIDATION com score null.
 * 3. Não inventa números fictícios (ex: 76.4 oficial sem fórmula validada).
 * 4. Mock Data é explicitamente isolada com isMock = true e bloqueada de gerar elegibilidade oficial.
 * 5. Toda computação é 100% reproduzível e determinística.
 */
class ScoreEngineV1(
    val scoreEngineVersion: String = "1.0.0-score-v1",
    val coreVersion: String = "1.0.0-datacore-v1"
) {

    /**
     * Executa o cálculo oficial de Score a partir de medições e evidências do Data Core.
     */
    fun computeScore(
        userId: String,
        assessmentId: String?,
        measurements: List<DataCoreMeasurement>,
        evidences: List<DataCoreEvidence>,
        provenances: Map<String, DataCoreProvenance>,
        isMockMode: Boolean = false,
        calculationTimestamp: Long = System.currentTimeMillis()
    ): AppResult<ScoreEngineComputationResult> {

        if (userId.isBlank()) {
            return AppResult.Failure(AppError.ValidationError("userId é obrigatório para cálculo de score."))
        }

        val requestId = UUID.randomUUID().toString()

        // 1. ELIGIBILITY CHECK
        val eligibility = EvidenceEligibilityChecker.checkEligibility(
            measurements = measurements,
            evidences = evidences,
            provenances = provenances,
            allowMockForDemo = isMockMode
        )

        if (!eligibility.isEligible && !isMockMode) {
            val failureReason = eligibility.rejectedReasons.joinToString("; ")
            return AppResult.Failure(
                AppError.ValidationError("Evidências inelegíveis para cálculo de Score oficial: $failureReason")
            )
        }

        val eligibleMeasurements = measurements.filter { eligibility.verifiedMeasurementIds.contains(it.id) }
        val eligibleEvidences = evidences.filter { eligibility.verifiedEvidenceIds.contains(it.id) }

        // 2. METRIC VALIDATION & NORMALIZATION & COMPONENT SCORES
        val componentScores = mutableListOf<ComponentScore>()
        val evidenceIdsUsed = eligibleEvidences.map { it.id }.distinct()
        val metricIdsUsed = eligibleMeasurements.map { it.metricId }.distinct()

        for (msr in eligibleMeasurements) {
            val metric = MetricCatalog.getMetricById(msr.metricId) ?: continue
            val mapping = ScoreDimensionCatalog.getMapping(msr.metricId)
            val normResult = ScoreNormalizationEngine.normalize(msr)
            val matchingEvidence = eligibleEvidences.find { it.measurementIds.contains(msr.id) }

            val primaryDim = mapping?.primaryDimension ?: "GENERAL"

            val compScore = ComponentScore(
                metricId = msr.metricId,
                metricName = metric.name,
                dimension = primaryDim,
                rawValue = msr.rawValue,
                normalizedValue = normResult.normalizedValue,
                weight = null, // Em validação científica (PENDING_VALIDATION)
                normalizationMethod = normResult.normalizationMethod,
                validityRange = normResult.validityRange,
                populationContext = normResult.populationContext,
                protocolVersion = msr.protocolId,
                formulaVersion = normResult.formulaVersion,
                calculationStatus = if (isMockMode) CalculationStatus.MOCK_DEMO else normResult.calculationStatus,
                evidenceId = matchingEvidence?.id,
                validationStatus = msr.validationStatus
            )
            componentScores.add(compScore)
        }

        // 3. CONFIDENCE METADATA (CONSUMINDO RELIABILITY FRAMEWORK DO DATA CORE)
        val highestTier = eligibleEvidences.mapNotNull { ev ->
            provenances[ev.provenanceId]?.let { ReliabilityFramework.classifySourceTier(it.sourceType) }
        }.minOrNull() ?: SourceTier.TIER_5_MANUAL_INPUT

        val confidenceStatus = when (highestTier) {
            SourceTier.TIER_1_DIRECT_SENSOR -> ConfidenceStatus.VERIFIED_HIGH
            SourceTier.TIER_2_CLINICAL_WEARABLE -> ConfidenceStatus.VERIFIED_MEDIUM
            SourceTier.TIER_3_CONSUMER_OPTICAL, SourceTier.TIER_4_VIDEO_CV_ESTIMATE -> ConfidenceStatus.UNVERIFIED_LOW
            SourceTier.TIER_5_MANUAL_INPUT -> ConfidenceStatus.UNDETERMINED
        }

        val confidenceMeta = ScoreConfidenceMetadata(
            sourceTier = highestTier,
            integrityStatus = if (eligibleEvidences.all { it.integrityStatus == IntegrityStatus.VALID }) IntegrityStatus.VALID else IntegrityStatus.UNKNOWN,
            consistencyStatus = "Auditada pelo Data Core Validation Engine V1",
            repeatabilityStatus = "Classificação de repetibilidade por tier de sensor",
            evidenceCount = eligibleEvidences.size,
            confidenceStatus = confidenceStatus,
            compositeScore = eligibleEvidences.mapNotNull { it.confidenceScore }.average().takeIf { !it.isNaN() },
            limitations = listOf(
                "Pesos científicos de ponderação entre componentes estão em fase de validação (Core V1).",
                "Scores oficiais refletem rigorosamente evidências verificadas sem interpolação de IA."
            )
        )

        // 4. DIMENSION SCORES (FORCE, SPEED, ENDURANCE, MOBILITY + EXTENSÍVEIS)
        val dimensionsToCompute = listOf(
            DimensionType.Force.key,
            DimensionType.Speed.key,
            DimensionType.Endurance.key,
            DimensionType.Mobility.key
        )

        val dimensionScores = mutableListOf<DimensionScore>()
        val dimensionScoresMap = mutableMapOf<String, DimensionScore>()

        for (dimKey in dimensionsToCompute) {
            val dimComponents = componentScores.filter { it.dimension.equals(dimKey, ignoreCase = true) }
            val dimEvidenceIds = dimComponents.mapNotNull { it.evidenceId }.distinct()

            val dimStatus: CalculationStatus
            val dimValue: Double?
            val dimFormulaVersion = "SCORE-$dimKey-1.0"

            if (isMockMode) {
                dimStatus = CalculationStatus.MOCK_DEMO
                // Em mock mode demonstração explícita
                dimValue = when (dimKey) {
                    "FORCE" -> 81.0
                    "SPEED" -> 79.0
                    "ENDURANCE" -> 74.0
                    "MOBILITY" -> 63.0
                    else -> 70.0
                }
            } else if (dimComponents.isEmpty()) {
                dimStatus = CalculationStatus.INSUFFICIENT_EVIDENCE
                dimValue = null
            } else {
                // Como os pesos científicos estão em validação (Core V1), o status é formalmente PENDING_VALIDATION
                dimStatus = CalculationStatus.PENDING_VALIDATION
                dimValue = null // Não inventa score fictício sem fórmula validada
            }

            val explanation = ScoreExplanation(
                score = dimValue,
                dimensionOrIndex = dimKey,
                metricsUsed = dimComponents.map { it.metricName },
                evidenceUsed = dimEvidenceIds,
                formulasUsed = listOf(dimFormulaVersion),
                normalizationUsed = "Metodologia de normalização por dimensão $dimKey",
                protocolVersions = dimComponents.map { it.protocolVersion }.distinct(),
                coreVersion = coreVersion,
                scoreVersion = scoreEngineVersion,
                limitations = listOf(
                    "Ponderação científica definitiva entre métricas da dimensão $dimKey em fase de revisão metodológica.",
                    "Não há aplicação de notas arbitrárias."
                ),
                notes = if (isMockMode) "DEMONSTRAÇÃO VISUAL ISOLADA (MOCK). Sem elegibilidade oficial." else "Score auditável do Score Engine V1."
            )

            val dimScoreObj = DimensionScore(
                dimension = dimKey,
                score = dimValue,
                contributingMetrics = dimComponents,
                formulaVersion = dimFormulaVersion,
                evidenceIds = dimEvidenceIds,
                confidenceMetadata = confidenceMeta,
                calculationStatus = dimStatus,
                explanation = explanation,
                calculatedAt = calculationTimestamp
            )

            dimensionScores.add(dimScoreObj)
            dimensionScoresMap[dimKey] = dimScoreObj
        }

        // 5. PERFORMANCE INDEX
        val perfIndexValue: Double?
        val perfIndexStatus: CalculationStatus
        val perfFormulaVersion = "SCORE-PERF-INDEX-1.0"

        if (isMockMode) {
            perfIndexValue = 76.4
            perfIndexStatus = CalculationStatus.MOCK_DEMO
        } else {
            perfIndexValue = null // Sem fórmula de agregação aprovada, permanece null
            perfIndexStatus = CalculationStatus.PENDING_VALIDATION
        }

        val perfIndexExplanation = ScoreExplanation(
            score = perfIndexValue,
            dimensionOrIndex = "PERFORMANCE_INDEX",
            metricsUsed = metricIdsUsed,
            evidenceUsed = evidenceIdsUsed,
            formulasUsed = listOf(perfFormulaVersion),
            normalizationUsed = "Agregação dimensional multidimensional",
            protocolVersions = eligibleMeasurements.map { it.protocolId }.distinct(),
            coreVersion = coreVersion,
            scoreVersion = scoreEngineVersion,
            limitations = listOf(
                "Fórmula oficial de agregação global (Performance Index) em validação comitê científico.",
                "Não inventa médias ponderadas arbitrárias sem protocolo formal aprovado."
            ),
            notes = if (isMockMode) "Índice de Performance demonstrativo (MOCK)." else "Performance Index oficial pendente de fórmula validada."
        )

        val performanceIndex = PerformanceIndex(
            value = perfIndexValue,
            formulaVersion = perfFormulaVersion,
            dimensionScores = dimensionScoresMap,
            evidenceIds = evidenceIdsUsed,
            calculationStatus = perfIndexStatus,
            confidenceMetadata = confidenceMeta,
            explanation = perfIndexExplanation
        )

        // 6. SCORE SNAPSHOT (IMUTÁVEL)
        val snapshotId = "SNAP-${UUID.randomUUID().toString().take(8).uppercase()}"
        val snapshot = ScoreSnapshot(
            id = snapshotId,
            userId = userId,
            assessmentId = assessmentId,
            scoreVersion = scoreEngineVersion,
            coreVersion = coreVersion,
            calculatedAt = calculationTimestamp,
            performanceIndex = performanceIndex,
            dimensionScores = dimensionScores,
            evidenceIds = evidenceIdsUsed,
            metricIds = metricIdsUsed,
            calculationStatus = if (isMockMode) CalculationStatus.MOCK_DEMO else CalculationStatus.PENDING_VALIDATION,
            confidenceMetadata = confidenceMeta,
            isMock = isMockMode || eligibility.isMockDetected,
            provenanceId = eligibleEvidences.firstOrNull()?.provenanceId,
            overallExplanation = perfIndexExplanation
        )

        // 7. AUDIT LOG
        val auditLog = DataCoreAuditLog(
            id = UUID.randomUUID().toString(),
            actorType = ActorType.CORE_ENGINE,
            actorId = "ScoreEngineV1",
            action = if (isMockMode) "SCORE_SNAPSHOT_MOCK_GENERATED" else "SCORE_SNAPSHOT_CALCULATED",
            entityType = "ScoreSnapshot",
            entityId = snapshotId,
            previousState = null,
            newState = "SNAPSHOT_ID=$snapshotId; STATUS=${snapshot.calculationStatus}; IS_MOCK=${snapshot.isMock}; EVIDENCE_COUNT=${evidenceIdsUsed.size}; FORMULA=$scoreEngineVersion",
            timestamp = calculationTimestamp,
            requestId = requestId,
            systemVersion = scoreEngineVersion
        )

        return AppResult.Success(
            ScoreEngineComputationResult(
                snapshot = snapshot,
                auditLog = auditLog
            )
        )
    }

    /**
     * Verificador de determinismo e reprodutibilidade:
     * Compara dois cálculos com os mesmos inputs para comprovar igualdade exata.
     */
    fun verifyReproducibility(
        result1: ScoreEngineComputationResult,
        result2: ScoreEngineComputationResult
    ): Boolean {
        val s1 = result1.snapshot
        val s2 = result2.snapshot

        return s1.scoreVersion == s2.scoreVersion &&
                s1.coreVersion == s2.coreVersion &&
                s1.isMock == s2.isMock &&
                s1.calculationStatus == s2.calculationStatus &&
                s1.performanceIndex.value == s2.performanceIndex.value &&
                s1.performanceIndex.calculationStatus == s2.performanceIndex.calculationStatus &&
                s1.dimensionScores.map { it.dimension to it.score } == s2.dimensionScores.map { it.dimension to it.score } &&
                s1.evidenceIds == s2.evidenceIds &&
                s1.metricIds == s2.metricIds
    }
}
