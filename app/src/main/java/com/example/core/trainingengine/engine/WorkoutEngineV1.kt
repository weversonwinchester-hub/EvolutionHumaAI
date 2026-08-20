package com.example.core.trainingengine.engine

import com.example.core.exerciseengine.catalog.ExerciseCatalogV1
import com.example.core.exerciseengine.model.ExerciseCategory
import com.example.core.exerciseengine.model.ExerciseDifficulty
import com.example.core.trainingengine.model.*
import java.util.UUID

/**
 * EVOLUTION HUMAN AI — WORKOUT ENGINE V1
 *
 * Domain engine responsible for creating, editing, validating,
 * and managing workout templates and session plans.
 *
 * ARCHITECTURAL CONSTRAINTS:
 * - Has ZERO authority to alter Scientific Score, Evolution, Progression, or Classes.
 * - Operates strictly on planned workout structures.
 */
class WorkoutEngineV1 {

    /**
     * Creates a new workout with validated items.
     */
    fun createWorkout(
        name: String,
        description: String = "",
        category: ExerciseCategory = ExerciseCategory.STRENGTH,
        difficulty: ExerciseDifficulty = ExerciseDifficulty.INTERMEDIATE,
        items: List<WorkoutItem> = emptyList(),
        estimatedDurationMinutes: Int = 45
    ): Workout {
        require(name.isNotBlank()) { "O nome do workout não pode ser vazio." }

        val orderedItems = items.mapIndexed { index, item ->
            item.copy(order = index + 1)
        }

        val targetMuscles: List<String> = orderedItems.flatMap { item ->
            val ex = ExerciseCatalogV1.getExerciseById(item.exerciseId)
            ((ex?.primaryMuscles ?: emptyList()) + (ex?.secondaryMuscles ?: emptyList())).map { it.name }
        }.distinct()

        return Workout(
            workoutId = UUID.randomUUID().toString(),
            name = name.trim(),
            description = description.trim(),
            category = category,
            difficulty = difficulty,
            targetMuscles = targetMuscles,
            items = orderedItems,
            estimatedDurationMinutes = estimatedDurationMinutes.coerceAtLeast(5),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Adds an exercise item to an existing workout.
     */
    fun addExerciseItem(
        workout: Workout,
        exerciseId: String,
        targetSets: Int = 3,
        prescription: TrainingPrescription = TrainingPrescription(),
        restBetweenSetsSeconds: Int = 90,
        notes: String = ""
    ): Workout {
        val exercise = ExerciseCatalogV1.getExerciseById(exerciseId)
        val exerciseName = exercise?.canonicalName ?: exerciseId

        val newItem = WorkoutItem(
            itemId = UUID.randomUUID().toString(),
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            order = workout.items.size + 1,
            targetSets = targetSets.coerceAtLeast(1),
            prescription = prescription,
            restBetweenSetsSeconds = restBetweenSetsSeconds.coerceAtLeast(0),
            notes = notes
        )

        val updatedItems = workout.items + newItem
        return workout.copy(
            items = updatedItems,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Removes an item and re-indexes the orders.
     */
    fun removeItem(workout: Workout, itemId: String): Workout {
        val filtered = workout.items.filterNot { it.itemId == itemId }
        val reordered = filtered.mapIndexed { index, item ->
            item.copy(order = index + 1)
        }
        return workout.copy(
            items = reordered,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Reorders items within a workout.
     */
    fun reorderItems(workout: Workout, itemIdsInOrder: List<String>): Workout {
        val itemMap = workout.items.associateBy { it.itemId }
        val reordered = itemIdsInOrder.mapNotNull { itemMap[it] }.mapIndexed { index, item ->
            item.copy(order = index + 1)
        }
        return workout.copy(
            items = reordered,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Generates a starter template library of standard canonical workouts.
     */
    fun getStarterTemplates(): List<Workout> {
        ExerciseCatalogV1.initializeCanonicalCatalog()

        val fullBodyStarter = Workout(
            workoutId = "WRK-TEMPLATE-FULLBODY-001",
            name = "Full Body Fundacional V1",
            description = "Sessão fundacional com foco em padrões de movimento fundamentais.",
            category = ExerciseCategory.STRENGTH,
            difficulty = ExerciseDifficulty.BEGINNER,
            targetMuscles = listOf("Quadríceps", "Glúteos", "Peitoral", "Dorsal", "Core"),
            items = listOf(
                WorkoutItem(
                    itemId = "ITEM-FB-01",
                    exerciseId = "EX-SQ-BW-001-V1",
                    exerciseName = "Bodyweight Squat",
                    order = 1,
                    targetSets = 3,
                    prescription = TrainingPrescription(
                        executionType = ExecutionPrescriptionType.REPETITIONS,
                        targetReps = ValueState.Recorded(12),
                        targetRpe = ValueState.Recorded(7.0)
                    ),
                    restBetweenSetsSeconds = 60
                ),
                WorkoutItem(
                    itemId = "ITEM-FB-02",
                    exerciseId = "EX-PU-BW-001-V1",
                    exerciseName = "Standard Push-Up",
                    order = 2,
                    targetSets = 3,
                    prescription = TrainingPrescription(
                        executionType = ExecutionPrescriptionType.REPETITIONS,
                        targetReps = ValueState.Recorded(10),
                        targetRpe = ValueState.Recorded(7.5)
                    ),
                    restBetweenSetsSeconds = 60
                ),
                WorkoutItem(
                    itemId = "ITEM-FB-03",
                    exerciseId = "EX-PLK-ISO-001-V1",
                    exerciseName = "Forearm Plank",
                    order = 3,
                    targetSets = 3,
                    prescription = TrainingPrescription(
                        executionType = ExecutionPrescriptionType.ISOMETRIC_HOLD,
                        targetDurationSeconds = ValueState.Recorded(30),
                        targetRpe = ValueState.Recorded(7.0)
                    ),
                    restBetweenSetsSeconds = 60
                )
            ),
            estimatedDurationMinutes = 30,
            isTemplate = true
        )

        val lowerBodyFocus = Workout(
            workoutId = "WRK-TEMPLATE-LOWER-001",
            name = "Inferiores & Estabilidade V1",
            description = "Foco em membros inferiores, agachamento, dobradiça e estabilidade pélvica.",
            category = ExerciseCategory.STRENGTH,
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            targetMuscles = listOf("Quadríceps", "Isquiotibiais", "Glúteos", "Panturrilhas"),
            items = listOf(
                WorkoutItem(
                    itemId = "ITEM-LOW-01",
                    exerciseId = "EX-SQ-BW-001-V1",
                    exerciseName = "Bodyweight Squat",
                    order = 1,
                    targetSets = 4,
                    prescription = TrainingPrescription(
                        executionType = ExecutionPrescriptionType.REPETITIONS,
                        targetReps = ValueState.Recorded(15),
                        targetRpe = ValueState.Recorded(8.0)
                    ),
                    restBetweenSetsSeconds = 90
                ),
                WorkoutItem(
                    itemId = "ITEM-LOW-02",
                    exerciseId = "EX-DL-CONV-001-V1",
                    exerciseName = "Conventional Deadlift",
                    order = 2,
                    targetSets = 3,
                    prescription = TrainingPrescription(
                        executionType = ExecutionPrescriptionType.LOAD,
                        targetReps = ValueState.Recorded(8),
                        targetLoadKg = ValueState.Recorded(60.0),
                        targetRpe = ValueState.Recorded(8.0)
                    ),
                    restBetweenSetsSeconds = 120
                )
            ),
            estimatedDurationMinutes = 40,
            isTemplate = true
        )

        return listOf(fullBodyStarter, lowerBodyFocus)
    }
}
