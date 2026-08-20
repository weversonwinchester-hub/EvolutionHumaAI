package com.example.core.biomechanical.engine

import com.example.core.biomechanical.evidence.BiomechanicalEvidence
import com.example.core.biomechanical.privacy.BiomechanicalPrivacyPolicy
import com.example.core.biomechanical.privacy.PrivacySanitizedAiPayload

/**
 * PERFORMAI BIOMECHANICAL EXPLAINER (AI GATEWAY INTEGRATION)
 *
 * Provê interface estritamente somente-leitura e descritiva para o AI Gateway.
 * A IA pode:
 * - Explicar a análise;
 * - Descrever o movimento observado;
 * - Explicar métricas e limitações;
 * - Resumir padrões observados;
 * - Sugerir quais dados adicionais seriam úteis.
 *
 * A IA NÃO PODE:
 * - Alterar landmarks;
 * - Alterar medições;
 * - Aumentar confiança;
 * - Reduzir incerteza;
 * - Validar protocolo ou instrumento;
 * - Criar evidência oficial;
 * - Alterar Score, Evolution ou Progression.
 */
object BiomechanicalExplainer {

    fun generateExplanation(evidence: BiomechanicalEvidence): String {
        val sanitized = BiomechanicalPrivacyPolicy.sanitizeForAiGateway(evidence)

        val repInfo = if (evidence.repetitions.isNotEmpty()) {
            "Foram detectadas ${evidence.repetitions.size} repetições completas com amplitude média de ${String.format("%.1f", sanitized.avgRangeOfMotionDegrees)} graus."
        } else {
            "Não foram detectadas repetições completas com amplitude suficiente."
        }

        val symmetryInfo = sanitized.symmetryIndexPercent?.let {
            "Índice de Simetria Límbica (LSI) observado: ${String.format("%.1f", it)}% (Observação cinemática descritiva)."
        } ?: "Simetria bilateral não aplicável a esta captura."

        val uncertaintyInfo = sanitized.uncertaintySummary

        val limitationsInfo = "Limitações da captura: ${evidence.qualityGateResult.warnings.ifEmpty { listOf("Nenhuma advertência crítica") }.joinToString(", ")}. Modelo: ${evidence.estimatorVersion}."

        return """
            === RELATÓRIO DESCRITIVO DE BIOMECÂNICA E VISÃO COMPUTACIONAL ===
            Protocolo: ${sanitized.protocolId} | Metodologia: ${sanitized.methodologyId}
            Status da Evidência: ${evidence.status}
            $repInfo
            $symmetryInfo
            $uncertaintyInfo
            $limitationsInfo
            
            AVISO: Esta análise é estritamente descritiva da cinemática visual e não constitui diagnóstico médico, prescrição clínica ou julgamento de lesão.
        """.trimIndent()
    }

    fun getSanitizedPayload(evidence: BiomechanicalEvidence): PrivacySanitizedAiPayload {
        return BiomechanicalPrivacyPolicy.sanitizeForAiGateway(evidence)
    }
}
