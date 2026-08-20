package com.example.core.datacore.reliability

import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.ReliabilityAssessment
import com.example.core.datacore.model.SourceTier
import com.example.core.datacore.model.ValidationStatus

/**
 * ReliabilityFramework: Estrutura arquitetural de confiabilidade do DATA CORE V1.
 *
 * AVALIAÇÃO CONCEITUAL:
 * Não inventa fórmulas científicas definitivas nesta etapa.
 * Estrutura as dimensões que serão computadas pelos motores futuros:
 * 1. Qualidade da Fonte (Source Tier)
 * 2. Integridade e Criptografia da Provenance
 * 3. Consistência com o protocolo
 * 4. Repetibilidade e ruído de amostragem
 * 5. Indicador de Confiança Composta Provisória
 */
object ReliabilityFramework {

    /**
     * Determina o Tier da fonte com base na origem declarada e comprovada.
     */
    fun classifySourceTier(sourceType: String): SourceTier {
        return when (sourceType.uppercase()) {
            "DIRECT_CALIBRATED_SENSOR", "LINEAR_POSITION_TRANSDUCER", "DUAL_FORCE_PLATE" -> SourceTier.TIER_1_DIRECT_SENSOR
            "CLINICAL_WEARABLE", "ECG_CHEST_STRAP", "METABOLIC_CART", "BLE_POLAR_H10" -> SourceTier.TIER_2_CLINICAL_WEARABLE
            "CONSUMER_OPTICAL", "SMARTWATCH_PPG", "WEARABLE_WRIST" -> SourceTier.TIER_3_CONSUMER_OPTICAL
            "VIDEO_CV_ESTIMATE", "COMPUTER_VISION", "OPTICAL_TRACKING" -> SourceTier.TIER_4_VIDEO_CV_ESTIMATE
            else -> SourceTier.TIER_5_MANUAL_INPUT
        }
    }

    /**
     * Avalia a confiabilidade da medição no contexto do framework estrutural.
     * Retorna a avaliação com notas explícitas de que os índices são provisórios (Core V1 Framework).
     */
    fun evaluateReliability(
        measurement: DataCoreMeasurement,
        provenance: DataCoreProvenance
    ): ReliabilityAssessment {
        val tier = classifySourceTier(provenance.sourceType)

        val sourceQualityWeight = when (tier) {
            SourceTier.TIER_1_DIRECT_SENSOR -> 0.95
            SourceTier.TIER_2_CLINICAL_WEARABLE -> 0.85
            SourceTier.TIER_3_CONSUMER_OPTICAL -> 0.70
            SourceTier.TIER_4_VIDEO_CV_ESTIMATE -> 0.60
            SourceTier.TIER_5_MANUAL_INPUT -> 0.35
        }

        val hasIntegrityHash = !provenance.integrityHash.isNullOrBlank()
        val isValidStatus = measurement.validationStatus == ValidationStatus.VALID

        val consistency = if (isValidStatus) 0.90 else 0.10
        val repeatability = if (tier <= SourceTier.TIER_2_CLINICAL_WEARABLE) 0.90 else 0.65

        val compositeScore = if (isValidStatus && hasIntegrityHash) {
            (sourceQualityWeight * 0.4 + consistency * 0.3 + repeatability * 0.3)
        } else if (isValidStatus) {
            (sourceQualityWeight * 0.3 + consistency * 0.3 + repeatability * 0.2)
        } else {
            0.0
        }

        return ReliabilityAssessment(
            sourceTier = tier,
            sourceQualityScore = sourceQualityWeight,
            measurementConsistency = consistency,
            integrityValid = isValidStatus && hasIntegrityHash,
            repeatabilityFactor = repeatability,
            compositeConfidenceScore = compositeScore,
            isPeerAudited = false,
            notes = "Framework Reliability V1 (Indicador estrutural; aguardando calibração definitiva do Score Engine)."
        )
    }
}
