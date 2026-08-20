package com.example.core.exerciseengine.media.registry

import com.example.core.exerciseengine.catalog.ExerciseCatalogV1
import com.example.core.exerciseengine.media.model.*
import com.example.core.exerciseengine.media.validator.ExerciseMediaValidator
import java.util.concurrent.ConcurrentHashMap

/**
 * EVOLUTION HUMAN AI — EXERCISE MEDIA REGISTRY V1
 *
 * Repositório canônico em memória para referências de mídia e demonstração dos exercícios.
 * Mantém integridade, versionamento, auditoria e desacoplamento total da definição científica do exercício.
 */
object ExerciseMediaRegistryV1 {

    // Chave: "$mediaId@$version"
    private val mediaByVersion = ConcurrentHashMap<String, ExerciseMediaReference>()
    // Chave: mediaId -> versão mais recente
    private val latestVersionMap = ConcurrentHashMap<String, String>()

    init {
        initializeInitialCatalog()
    }

    fun register(media: ExerciseMediaReference): Boolean {
        val validation = ExerciseMediaValidator.validate(media)
        if (!validation.isValid) {
            return false
        }

        val key = "${media.mediaId}@${media.version}"
        val existing = mediaByVersion[key]

        // Regra de Imutabilidade: Não permitir alteração silenciosa de mídia ativa/aprovada
        if (existing != null && (existing.status == MediaLifecycleStatus.ACTIVE || existing.status == MediaLifecycleStatus.APPROVED)) {
            if (existing != media) {
                return false
            }
            return true
        }

        val prepared = if (media.checksumSha256.isBlank()) {
            media.copy(checksumSha256 = media.calculateChecksum())
        } else {
            media
        }

        mediaByVersion[key] = prepared
        latestVersionMap[media.mediaId] = media.version
        return true
    }

    fun getById(mediaId: String): ExerciseMediaReference? {
        val latestVer = latestVersionMap[mediaId] ?: return null
        return mediaByVersion["$mediaId@$latestVer"]
    }

    fun getByIdAndVersion(mediaId: String, version: String): ExerciseMediaReference? {
        return mediaByVersion["$mediaId@$version"]
    }

    fun getAllVersions(mediaId: String): List<ExerciseMediaReference> {
        return mediaByVersion.values
            .filter { it.mediaId == mediaId }
            .sortedBy { it.version }
    }

    fun getAll(): List<ExerciseMediaReference> {
        return latestVersionMap.mapNotNull { (id, ver) ->
            mediaByVersion["$id@$ver"]
        }
    }

    fun getAllActive(): List<ExerciseMediaReference> {
        return getAll().filter { it.status == MediaLifecycleStatus.ACTIVE }
    }

    fun getByExerciseId(exerciseId: String): List<ExerciseMediaReference> {
        return getAll().filter { it.exerciseId == exerciseId }
    }

    fun getActiveByExerciseId(exerciseId: String): List<ExerciseMediaReference> {
        return getAllActive().filter { it.exerciseId == exerciseId }
    }

    fun count(): Int = latestVersionMap.size

    fun resetForTesting() {
        mediaByVersion.clear()
        latestVersionMap.clear()
    }

    /**
     * Inicializa referências oficiais para os 20 exercícios fundamentais do catálogo canônico.
     * Mídias sem asset físico real são registradas como NONE, garantindo que não há criação de dados falsos ou URLs fictícias.
     */
    fun initializeInitialCatalog() {
        ExerciseCatalogV1.initializeCanonicalCatalog()

        val exercises = ExerciseCatalogV1.CANONICAL_CATALOG

        for (ex in exercises) {
            val mediaId = "MED-${ex.exerciseId.removePrefix("EX-")}"
            // Não inventar URLs nem vídeos falsos. Registra tipo NONE oficial quando não houver asset.
            val initialMedia = ExerciseMediaReference(
                mediaId = mediaId,
                exerciseId = ex.exerciseId,
                exerciseVersion = ex.version,
                mediaType = ExerciseMediaType.NONE,
                status = MediaLifecycleStatus.ACTIVE,
                isOfficial = true,
                licenseInfo = MediaLicenseInfo(
                    source = "EVOLUTION_HUMAN_AI_OFFICIAL_CATALOG",
                    license = "PROPRIETARY",
                    author = "EVOLUTION_AI_CORE"
                ),
                recommendedCameraAngles = listOf(CameraPreset.FRONT, CameraPreset.SIDE)
            )
            register(initialMedia)
        }
    }
}
