package com.example.core.exerciseengine.registry

import com.example.core.exerciseengine.model.ExerciseCategory
import com.example.core.exerciseengine.model.ExerciseDefinition
import com.example.core.exerciseengine.model.ExerciseDifficulty
import com.example.core.exerciseengine.model.ExerciseStatus
import com.example.core.exerciseengine.model.MovementPattern
import com.example.core.exerciseengine.model.MuscleGroup
import com.example.core.exerciseengine.validator.ExerciseValidator
import java.util.concurrent.ConcurrentHashMap

/**
 * EVOLUTION HUMAN AI — EXERCISE REGISTRY V1
 *
 * Repositório canônico em memória do catálogo de exercícios.
 * Garante:
 * - Unicidade de (exerciseId, version)
 * - Imutabilidade de versões publicadas
 * - Rastreabilidade completa de versões
 * - Consultas e filtros auditados
 */
object ExerciseRegistryV1 {

    // Chave: "$exerciseId@$version"
    private val exercisesByVersion = ConcurrentHashMap<String, ExerciseDefinition>()
    // Chave: exerciseId -> versão mais recente
    private val latestVersionMap = ConcurrentHashMap<String, String>()

    fun register(exercise: ExerciseDefinition): Boolean {
        val validation = ExerciseValidator.validate(exercise)
        if (!validation.isValid) {
            return false
        }

        val key = "${exercise.exerciseId}@${exercise.version}"
        val existing = exercisesByVersion[key]

        // Regra de Imutabilidade: Não permitir alteração silenciosa de exercício ativo/publicado
        if (existing != null && (existing.status == ExerciseStatus.ACTIVE || existing.status == ExerciseStatus.DEPRECATED)) {
            if (existing != exercise) {
                // Tentativa de modificar sem incrementar versão é rejeitada
                return false
            }
            return true
        }

        val prepared = if (exercise.checksum.isBlank()) {
            exercise.copy(checksum = exercise.calculateChecksum())
        } else {
            exercise
        }

        exercisesByVersion[key] = prepared
        latestVersionMap[exercise.exerciseId] = exercise.version
        return true
    }

    fun getByIdAndVersion(exerciseId: String, version: String): ExerciseDefinition? {
        return exercisesByVersion["$exerciseId@$version"]
    }

    fun getById(exerciseId: String): ExerciseDefinition? {
        val latestVer = latestVersionMap[exerciseId] ?: return null
        return exercisesByVersion["$exerciseId@$latestVer"]
    }

    fun getAllVersions(exerciseId: String): List<ExerciseDefinition> {
        return exercisesByVersion.values
            .filter { it.exerciseId == exerciseId }
            .sortedBy { it.version }
    }

    fun getAll(): List<ExerciseDefinition> {
        return latestVersionMap.mapNotNull { (id, ver) ->
            exercisesByVersion["$id@$ver"]
        }
    }

    fun getAllActive(): List<ExerciseDefinition> {
        return getAll().filter { it.status == ExerciseStatus.ACTIVE }
    }

    fun getByCategory(category: ExerciseCategory): List<ExerciseDefinition> {
        return getAllActive().filter { it.category == category }
    }

    fun getByMovementPattern(pattern: MovementPattern): List<ExerciseDefinition> {
        return getAllActive().filter { it.movementPattern == pattern }
    }

    fun getByPrimaryMuscle(muscle: MuscleGroup): List<ExerciseDefinition> {
        return getAllActive().filter { it.primaryMuscles.contains(muscle) }
    }

    fun getByDifficulty(difficulty: ExerciseDifficulty): List<ExerciseDefinition> {
        return getAllActive().filter { it.difficulty == difficulty }
    }

    fun count(): Int = latestVersionMap.size

    fun resetForTesting() {
        exercisesByVersion.clear()
        latestVersionMap.clear()
    }
}
