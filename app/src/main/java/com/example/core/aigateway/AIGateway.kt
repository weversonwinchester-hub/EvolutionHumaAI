package com.example.core.aigateway

import com.example.core.error.AppError
import com.example.core.error.AppResult
import com.example.core.model.AIInteraction

/**
 * AI Gateway Contract:
 *
 * REGRA CRÍTICA DE ARQUITETURA:
 * A IA atua exclusivamente como consultor/analista gerando recomendações e insights.
 * A IA NÃO POSSUI autoridade para alterar diretamente ou unilateralmente o estado oficial
 * do usuário (pontuação, classe evolutiva, XP oficial ou permissões).
 * Todas as sugestões devem ser processadas e validadas pelo PERFORMAI CORE.
 */
interface AIGateway {
    suspend fun analyzePerformanceContext(userId: String, context: String): AppResult<AIInteraction>
}

class FoundationAIGateway : AIGateway {
    override suspend fun analyzePerformanceContext(userId: String, context: String): AppResult<AIInteraction> {
        // AI Gateway Foundation: Recomendações consultivas sem poder de mutação direta
        val interaction = AIInteraction(
            id = "ai_${System.currentTimeMillis()}",
            userId = userId,
            promptContext = context,
            suggestedAction = "Análise consultiva: Avaliação inicial necessária para calibrar baseline de performance.",
            confidence = 0.95,
            processedByCore = true,
            appliedStateChange = false // Garantia explícita de não-mutação direta
        )
        return AppResult.Success(interaction)
    }
}
