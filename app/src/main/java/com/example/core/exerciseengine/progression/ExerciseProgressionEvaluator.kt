package com.example.core.exerciseengine.progression

import com.example.core.exerciseengine.model.ExerciseDefinition
import com.example.core.exerciseengine.registry.ExerciseRegistryV1

data class ExerciseProgressionPath(
    val baseExerciseId: String,
    val regressions: List<ExerciseDefinition>,
    val currentExercise: ExerciseDefinition?,
    val progressions: List<ExerciseDefinition>,
    val variations: List<ExerciseDefinition>
)

/**
 * EVOLUTION HUMAN AI — EXERCISE PROGRESSION EVALUATOR
 *
 * Avalia cadeias técnicas e biomecânicas de regressão e progressão de exercícios.
 *
 * IMPORTANTE (REGRA ARQUITETURAL):
 * Exercise Progression ≠ Athlete Evolution.
 * A progressão de complexidade de um exercício não determina nem altera diretamente
 * a classe ou rank de evolução do atleta no Evolution Engine.
 */
object ExerciseProgressionEvaluator {

    fun evaluatePath(exerciseId: String): ExerciseProgressionPath {
        val current = ExerciseRegistryV1.getById(exerciseId)

        val regressions = current?.regressionIds?.mapNotNull { ExerciseRegistryV1.getById(it) } ?: emptyList()
        val progressions = current?.progressionIds?.mapNotNull { ExerciseRegistryV1.getById(it) } ?: emptyList()
        val variations = current?.variationIds?.mapNotNull { ExerciseRegistryV1.getById(it) } ?: emptyList()

        return ExerciseProgressionPath(
            baseExerciseId = exerciseId,
            regressions = regressions,
            currentExercise = current,
            progressions = progressions,
            variations = variations
        )
    }

    fun findFullProgressionTree(startExerciseId: String, maxDepth: Int = 5): List<String> {
        val visited = mutableSetOf<String>()
        val result = mutableListOf<String>()

        fun dfs(id: String, depth: Int) {
            if (depth > maxDepth || visited.contains(id)) return
            visited.add(id)
            result.add(id)

            val exercise = ExerciseRegistryV1.getById(id) ?: return
            for (nextId in exercise.progressionIds) {
                dfs(nextId, depth + 1)
            }
        }

        dfs(startExerciseId, 0)
        return result
    }

    fun findFullRegressionTree(startExerciseId: String, maxDepth: Int = 5): List<String> {
        val visited = mutableSetOf<String>()
        val result = mutableListOf<String>()

        fun dfs(id: String, depth: Int) {
            if (depth > maxDepth || visited.contains(id)) return
            visited.add(id)
            result.add(id)

            val exercise = ExerciseRegistryV1.getById(id) ?: return
            for (prevId in exercise.regressionIds) {
                dfs(prevId, depth + 1)
            }
        }

        dfs(startExerciseId, 0)
        return result
    }
}
