package com.example.core.aigateway.roles

import com.example.core.aigateway.AIGateway
import com.example.core.evolutionengine.explanation.ClassExplanationRegistryV1
import com.example.core.trainingengine.history.TrainingHistorySummary
import com.example.core.trainingengine.model.TrainingSession

/**
 * EVOLUTION HUMAN AI — AI ADVISORY ROLES V1
 *
 * Consultative intelligence layer for explanation, analysis, coaching, and planning.
 *
 * STRICT GOVERNANCE:
 * - AI has ZERO authority to alter Scientific Score, Evolution, Classes, Trials, or Baselines.
 * - AI functions purely as an explanatory and analytical assistant.
 */
enum class AIAdvisoryRole {
    AI_EXPLAINER,
    AI_ANALYST,
    AI_COACH,
    AI_PLANNER
}

data class AIAdvisoryResponse(
    val role: AIAdvisoryRole,
    val title: String,
    val content: String,
    val suggestions: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

class AIAdvisoryService(
    private val aiGateway: AIGateway
) {

    /**
     * AI_EXPLAINER: Explains the athlete's current class in clear, objective language.
     */
    suspend fun explainClass(classId: String, athleteName: String): AIAdvisoryResponse {
        val explanation = ClassExplanationRegistryV1.getExplanation(classId)
        val content = if (explanation != null) {
            """
            Olá $athleteName! Você está atualmente na classe "${explanation.name}".
            
            O que isto significa: ${explanation.meaning}
            
            Por que você está nesta classe: ${explanation.whyInThisClass}
            
            O que NÃO significa: ${explanation.whatItDoesNotMean}
            
            Próxima meta de evolução: ${explanation.nextClassName ?: "Nível máximo alcançado"}
            Requisitos: ${explanation.progressionRequirements.joinToString("; ")}
            """.trimIndent()
        } else {
            "Classe em processo de calibração basal."
        }

        return AIAdvisoryResponse(
            role = AIAdvisoryRole.AI_EXPLAINER,
            title = "Explicação da Classe de Evolução",
            content = content,
            suggestions = explanation?.defaultCriteriaPending ?: emptyList()
        )
    }

    /**
     * AI_ANALYST: Summarizes volume, frequency, and adherence trends without altering scientific score.
     */
    fun analyzeTrainingHistory(summary: TrainingHistorySummary): AIAdvisoryResponse {
        val content = """
        Resumo de Desempenho de Treino:
        - Sessões concluídas: ${summary.completedSessions} de ${summary.totalSessions}
        - Volume total acumulado: ${summary.totalVolumeKg} kg
        - Repetições totais: ${summary.totalReps}
        - Tempo total de treino: ${summary.totalDurationHours} horas
        - Consistência semanal: ${summary.weeklyConsistencyPercent.toInt()}%
        """.trimIndent()

        val suggestions = mutableListOf<String>()
        if (summary.weeklyConsistencyPercent < 70f) {
            suggestions.add("Busque manter ao menos 3 sessões semanais para consolidar adaptações neuromusculares.")
        } else {
            suggestions.add("Excelente consistência semanal! Mantenha a sobrecarga progressiva gradual.")
        }

        return AIAdvisoryResponse(
            role = AIAdvisoryRole.AI_ANALYST,
            title = "Análise Longitudinal de Treino",
            content = content,
            suggestions = suggestions
        )
    }

    /**
     * AI_COACH: Generates post-session feedback and encouragement.
     */
    fun generatePostSessionFeedback(session: TrainingSession): AIAdvisoryResponse {
        val completionText = "${(session.completionRate * 100).toInt()}%"
        val content = """
        Sessão "${session.sessionName}" finalizada com sucesso!
        - Taxa de conclusão: $completionText
        - Volume total: ${session.totalVolumeKg} kg
        - Duração ativa: ${session.totalDurationSeconds / 60} min
        """.trimIndent()

        return AIAdvisoryResponse(
            role = AIAdvisoryRole.AI_COACH,
            title = "Feedback de Sessão",
            content = content,
            suggestions = listOf("Hidrate-se e priorize o descanso para otimizar a síntese proteica.")
        )
    }
}
