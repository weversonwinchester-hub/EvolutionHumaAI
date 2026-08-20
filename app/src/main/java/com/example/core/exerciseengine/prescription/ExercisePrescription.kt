package com.example.core.exerciseengine.prescription

enum class LoadUnit {
    KG,
    LBS,
    BODYWEIGHT_PERCENT,
    RPE_BASED,
    NOT_SPECIFIED,
    UNKNOWN
}

/**
 * EVOLUTION HUMAN AI — EXERCISE PRESCRIPTION
 *
 * Estrutura formal de prescrição de exercício.
 * Importante: A ausência de valor NÃO deve ser interpretada como zero.
 * Valores desconhecidos ou não definidos permanecem nulos ou marcados como NOT_SPECIFIED / UNKNOWN.
 */
data class ExercisePrescription(
    val prescriptionId: String = java.util.UUID.randomUUID().toString(),
    val exerciseId: String,
    val sets: Int? = null,
    val repetitions: Int? = null,
    val durationSeconds: Int? = null,
    val distanceMeters: Double? = null,
    val load: Double? = null,
    val loadUnit: LoadUnit = LoadUnit.NOT_SPECIFIED,
    val restSeconds: Int? = null,
    val tempo: String? = null,
    val targetIntensity: Double? = null,
    val targetRPE: Double? = null,
    val targetVelocity: Double? = null,
    val targetROM: Double? = null,
    val notes: String? = null,
    val isValidated: Boolean = true
) {
    fun hasPrescribedLoad(): Boolean = load != null && loadUnit != LoadUnit.NOT_SPECIFIED && loadUnit != LoadUnit.UNKNOWN

    fun hasPrescribedVolume(): Boolean = sets != null || repetitions != null || durationSeconds != null || distanceMeters != null
}
