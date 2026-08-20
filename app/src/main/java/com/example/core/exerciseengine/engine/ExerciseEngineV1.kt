package com.example.core.exerciseengine.engine

import com.example.core.exerciseengine.catalog.ExerciseCatalogV1
import com.example.core.exerciseengine.model.*
import com.example.core.exerciseengine.prescription.ExercisePrescription
import com.example.core.exerciseengine.progression.ExerciseProgressionEvaluator
import com.example.core.exerciseengine.progression.ExerciseProgressionPath
import com.example.core.exerciseengine.registry.ExerciseRegistryV1
import com.example.core.exerciseengine.security.ExerciseCallerTier
import com.example.core.exerciseengine.security.ExerciseSecurityBarrier

/**
 * EVOLUTION HUMAN AI — EXERCISE ENGINE V1
 *
 * Módulo de coordenação e execução do catálogo oficial de exercícios.
 * Desacoplado de Score Engine, Evolution Engine e Gamification.
 */
object ExerciseEngineV1 {

    init {
        // Inicialização estrita do catálogo canônico V1
        if (ExerciseRegistryV1.count() == 0) {
            ExerciseCatalogV1.initializeCanonicalCatalog()
        }
    }

    fun ensureInitialized() {
        if (ExerciseRegistryV1.count() == 0) {
            ExerciseCatalogV1.initializeCanonicalCatalog()
        }
    }

    fun getExercise(exerciseId: String): ExerciseDefinition? {
        ensureInitialized()
        return ExerciseRegistryV1.getById(exerciseId)
    }

    fun getExerciseByVersion(exerciseId: String, version: String): ExerciseDefinition? {
        ensureInitialized()
        return ExerciseRegistryV1.getByIdAndVersion(exerciseId, version)
    }

    fun listActiveExercises(): List<ExerciseDefinition> {
        ensureInitialized()
        return ExerciseRegistryV1.getAllActive()
    }

    fun listByCategory(category: ExerciseCategory): List<ExerciseDefinition> {
        ensureInitialized()
        return ExerciseRegistryV1.getByCategory(category)
    }

    fun listByPattern(pattern: MovementPattern): List<ExerciseDefinition> {
        ensureInitialized()
        return ExerciseRegistryV1.getByMovementPattern(pattern)
    }

    fun listByMuscle(muscle: MuscleGroup): List<ExerciseDefinition> {
        ensureInitialized()
        return ExerciseRegistryV1.getByPrimaryMuscle(muscle)
    }

    fun getProgressionPath(exerciseId: String): ExerciseProgressionPath {
        ensureInitialized()
        return ExerciseProgressionEvaluator.evaluatePath(exerciseId)
    }

    fun registerNewOfficialExercise(
        callerTier: ExerciseCallerTier,
        callerId: String,
        exercise: ExerciseDefinition,
        simulationMode: Boolean = false
    ): Boolean {
        return ExerciseSecurityBarrier.registerOrUpdateExercise(
            callerTier = callerTier,
            callerId = callerId,
            exercise = exercise,
            simulationMode = simulationMode
        )
    }

    /**
     * Interface consultiva para a Evolution Intelligence (IA).
     * Retorna resumo estruturado sem poder de alteração de estado oficial.
     */
    fun getConsultativeExplanation(exerciseId: String): String {
        ensureInitialized()
        val exercise = ExerciseRegistryV1.getById(exerciseId)
            ?: return "Exercício não catalogado no registro oficial do EvolutionHumanAI."

        val muscles = exercise.primaryMuscles.joinToString(", ") { it.name }
        val goals = exercise.trainingGoals.joinToString(", ") { it.name }
        val setup = exercise.instructions.setup.joinToString(" ")
        val execution = exercise.instructions.execution.joinToString(" ")
        val errors = exercise.commonErrors.joinToString("; ") { "${it.description} -> Correção: ${it.correction}" }

        return "Exercício Oficial: ${exercise.displayName} (${exercise.canonicalName})\n" +
                "Categoria: ${exercise.category} | Padrão: ${exercise.movementPattern} | Dificuldade: ${exercise.difficulty}\n" +
                "Músculos Principais: $muscles\n" +
                "Objetivos Motores: $goals\n" +
                "Setup: $setup\n" +
                "Execução: $execution\n" +
                (if (errors.isNotBlank()) "Atenção a Erros Comuns: $errors\n" else "") +
                "Nota: A execução técnica desenvolve capacidade motora, mas não altera automaticamente a classe do atleta sem validação de evidência pelo Core."
    }
}
