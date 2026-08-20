package com.example.core.exerciseengine.media.validator

import com.example.core.exerciseengine.media.model.ExerciseMediaReference
import com.example.core.exerciseengine.media.model.ExerciseMediaType
import com.example.core.exerciseengine.media.model.MediaLifecycleStatus
import com.example.core.exerciseengine.registry.ExerciseRegistryV1

data class ExerciseMediaValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

object ExerciseMediaValidator {

    fun validate(
        media: ExerciseMediaReference,
        validateExerciseExistence: Boolean = true
    ): ExerciseMediaValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 1. Identificador da mídia
        if (media.mediaId.isBlank()) {
            errors.add("MEDIA_ID_CANNOT_BE_BLANK")
        }

        // 2. Identificador do exercício
        if (media.exerciseId.isBlank()) {
            errors.add("EXERCISE_ID_CANNOT_BE_BLANK")
        } else if (validateExerciseExistence) {
            val exercise = ExerciseRegistryV1.getById(media.exerciseId)
            if (exercise == null) {
                errors.add("EXERCISE_DOES_NOT_EXIST: ${media.exerciseId}")
            }
        }

        // 3. Licenciamento e Autoria
        if (media.isOfficial) {
            if (media.licenseInfo.license.isBlank() || media.licenseInfo.license == "UNKNOWN") {
                errors.add("OFFICIAL_MEDIA_REQUIRES_KNOWN_LICENSE")
            }
            if (media.licenseInfo.source.isBlank()) {
                errors.add("OFFICIAL_MEDIA_REQUIRES_SOURCE")
            }
        }

        // 4. Mídia física vs Mídia NONE
        if (media.mediaType == ExerciseMediaType.NONE) {
            // Mídia NONE é válida para exercícios sem asset visual cadastrado
            if (media.uri != null && media.uri.isNotBlank()) {
                warnings.add("MEDIA_TYPE_NONE_HAS_URI_ASSIGNED")
            }
        } else {
            // Mídia física não-NONE
            if (media.status == MediaLifecycleStatus.ACTIVE && (media.uri == null || media.uri.isBlank()) && (media.localCachedPath == null || media.localCachedPath.isBlank())) {
                warnings.add("ACTIVE_NON_NONE_MEDIA_HAS_NO_URI_OR_LOCAL_PATH")
            }
        }

        // 5. Checksum SHA-256
        if (media.checksumSha256.isNotBlank()) {
            val calculated = media.calculateChecksum()
            if (media.checksumSha256 != calculated && media.checksumSha256.length != 64) {
                errors.add("INVALID_CHECKSUM_SHA256")
            }
        }

        return ExerciseMediaValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}
