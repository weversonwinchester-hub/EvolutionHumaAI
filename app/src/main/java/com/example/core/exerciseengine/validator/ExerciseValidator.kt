package com.example.core.exerciseengine.validator

import com.example.core.exerciseengine.model.ExerciseDefinition
import com.example.core.exerciseengine.model.ExerciseStatus

data class ExerciseValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

object ExerciseValidator {

    fun validate(exercise: ExerciseDefinition, knownExerciseIds: Set<String>? = null): ExerciseValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 1. Identificador
        if (exercise.exerciseId.isBlank()) {
            errors.add("EXERCISE_ID_CANNOT_BE_BLANK")
        } else if (!exercise.exerciseId.matches(Regex("^[A-Z0-9_-]+$"))) {
            errors.add("INVALID_EXERCISE_ID_FORMAT: ${exercise.exerciseId}")
        }

        // 2. Versão
        if (exercise.version.isBlank()) {
            errors.add("VERSION_CANNOT_BE_BLANK")
        }

        // 3. Nomes
        if (exercise.canonicalName.isBlank()) {
            errors.add("CANONICAL_NAME_CANNOT_BE_BLANK")
        }
        if (exercise.displayName.isBlank()) {
            errors.add("DISPLAY_NAME_CANNOT_BE_BLANK")
        }

        // 4. Músculos
        if (exercise.primaryMuscles.isEmpty()) {
            errors.add("PRIMARY_MUSCLES_CANNOT_BE_EMPTY")
        }

        // 5. Equipamentos
        if (exercise.equipment.isEmpty()) {
            errors.add("EQUIPMENT_LIST_CANNOT_BE_EMPTY")
        }

        // 6. Progressões / Regressões
        if (exercise.progressionIds.contains(exercise.exerciseId)) {
            errors.add("SELF_REFERENCING_PROGRESSION_NOT_ALLOWED")
        }
        if (exercise.regressionIds.contains(exercise.exerciseId)) {
            errors.add("SELF_REFERENCING_REGRESSION_NOT_ALLOWED")
        }

        // Validação contra catálogo conhecido (se fornecido)
        if (knownExerciseIds != null) {
            val invalidProgressions = exercise.progressionIds.filter { !knownExerciseIds.contains(it) }
            if (invalidProgressions.isNotEmpty()) {
                errors.add("INVALID_PROGRESSION_REFERENCES: $invalidProgressions")
            }
            val invalidRegressions = exercise.regressionIds.filter { !knownExerciseIds.contains(it) }
            if (invalidRegressions.isNotEmpty()) {
                errors.add("INVALID_REGRESSION_REFERENCES: $invalidRegressions")
            }
        }

        // 7. Mídia
        for (media in exercise.mediaReferences) {
            if (media.mediaId.isBlank()) {
                errors.add("MEDIA_ID_CANNOT_BE_BLANK")
            }
        }

        // 8. Instruções
        if (exercise.status == ExerciseStatus.ACTIVE && exercise.instructions.execution.isEmpty()) {
            warnings.add("ACTIVE_EXERCISE_HAS_NO_EXECUTION_INSTRUCTIONS")
        }

        return ExerciseValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}
