package com.example.core.exerciseengine.media.resolver

import com.example.core.exerciseengine.media.model.*
import com.example.core.exerciseengine.media.registry.ExerciseMediaRegistryV1

data class MediaResolutionRequest(
    val exerciseId: String,
    val exerciseVersion: String = "V1",
    val preferredMediaType: ExerciseMediaType? = null,
    val preferredAvatar: AvatarCharacterId = AvatarCharacterId.MALE_AVATAR_V1,
    val preferredAngle: CameraPreset = CameraPreset.FRONT,
    val deviceCapabilities: DeviceRenderingCapability = DeviceRenderingCapability.STANDARD_3D,
    val networkStatus: NetworkStatus = NetworkStatus.ONLINE
)

data class MediaResolutionResult(
    val selectedMedia: ExerciseMediaReference?,
    val resolvedMediaType: ExerciseMediaType,
    val effectiveUri: String?,
    val effectiveThumbnailUri: String?,
    val isOfflineFallback: Boolean,
    val isLowBandwidthFallback: Boolean,
    val isLocalAsset: Boolean,
    val resolutionReason: String
)

/**
 * EVOLUTION HUMAN AI — EXERCISE MEDIA RESOLVER
 *
 * Seleciona determinística e robustamente a melhor representação visual de demonstração disponível.
 *
 * Princípios:
 * - A escolha da mídia NÃO altera a classificação científica do exercício.
 * - Modo OFFLINE nunca quebra a visualização: recorre a cache local, ilustrações ou instruções estruturadas.
 * - Fallback gradual e ordenado: 3D -> Video -> GIF -> Image Sequence -> Illustration -> NONE.
 */
object ExerciseMediaResolver {

    private val defaultPriorityOrder: List<ExerciseMediaType> = listOf(
        ExerciseMediaType.INTERACTIVE_3D,
        ExerciseMediaType.ANIMATION_3D,
        ExerciseMediaType.VIDEO_3D,
        ExerciseMediaType.VIDEO_2D,
        ExerciseMediaType.GIF,
        ExerciseMediaType.IMAGE_SEQUENCE,
        ExerciseMediaType.ILLUSTRATION,
        ExerciseMediaType.NONE
    )

    private val lowBandwidthPriorityOrder: List<ExerciseMediaType> = listOf(
        ExerciseMediaType.IMAGE_SEQUENCE,
        ExerciseMediaType.ILLUSTRATION,
        ExerciseMediaType.GIF,
        ExerciseMediaType.VIDEO_2D,
        ExerciseMediaType.NONE
    )

    fun resolve(request: MediaResolutionRequest): MediaResolutionResult {
        val allActiveMedia = ExerciseMediaRegistryV1.getActiveByExerciseId(request.exerciseId)

        if (allActiveMedia.isEmpty()) {
            return MediaResolutionResult(
                selectedMedia = null,
                resolvedMediaType = ExerciseMediaType.NONE,
                effectiveUri = null,
                effectiveThumbnailUri = null,
                isOfflineFallback = request.networkStatus == NetworkStatus.OFFLINE,
                isLowBandwidthFallback = request.networkStatus == NetworkStatus.LOW_BANDWIDTH,
                isLocalAsset = false,
                resolutionReason = "NO_ACTIVE_MEDIA_REGISTERED_FOR_EXERCISE"
            )
        }

        // 1. Caso OFFLINE
        if (request.networkStatus == NetworkStatus.OFFLINE) {
            // Filtrar mídias que possuem arquivo local em cache ou são estáticas embutidas
            val localMedia = allActiveMedia.filter {
                it.localCachedPath != null && it.localCachedPath.isNotBlank()
            }

            if (localMedia.isNotEmpty()) {
                val matched = selectBestFromList(localMedia, request, defaultPriorityOrder)
                if (matched != null) {
                    return MediaResolutionResult(
                        selectedMedia = matched,
                        resolvedMediaType = matched.mediaType,
                        effectiveUri = matched.localCachedPath ?: matched.uri,
                        effectiveThumbnailUri = matched.thumbnailUri,
                        isOfflineFallback = true,
                        isLowBandwidthFallback = false,
                        isLocalAsset = true,
                        resolutionReason = "RESOLVED_LOCAL_OFFLINE_CACHE"
                    )
                }
            }

            // Fallback para ilustrações / thumbnails estáticos se disponíveis
            val staticFallback = allActiveMedia.firstOrNull {
                it.mediaType == ExerciseMediaType.ILLUSTRATION || it.mediaType == ExerciseMediaType.IMAGE_SEQUENCE
            } ?: allActiveMedia.firstOrNull { it.mediaType == ExerciseMediaType.NONE }

            return MediaResolutionResult(
                selectedMedia = staticFallback,
                resolvedMediaType = staticFallback?.mediaType ?: ExerciseMediaType.NONE,
                effectiveUri = staticFallback?.localCachedPath ?: staticFallback?.uri,
                effectiveThumbnailUri = staticFallback?.thumbnailUri,
                isOfflineFallback = true,
                isLowBandwidthFallback = false,
                isLocalAsset = staticFallback?.localCachedPath != null,
                resolutionReason = if (staticFallback != null) "OFFLINE_FALLBACK_TO_STATIC" else "OFFLINE_FALLBACK_TO_NONE"
            )
        }

        // 2. Caso LOW_BANDWIDTH
        if (request.networkStatus == NetworkStatus.LOW_BANDWIDTH) {
            val matched = selectBestFromList(allActiveMedia, request, lowBandwidthPriorityOrder)
            if (matched != null) {
                return MediaResolutionResult(
                    selectedMedia = matched,
                    resolvedMediaType = matched.mediaType,
                    effectiveUri = matched.localCachedPath ?: matched.uri,
                    effectiveThumbnailUri = matched.thumbnailUri,
                    isOfflineFallback = false,
                    isLowBandwidthFallback = true,
                    isLocalAsset = matched.localCachedPath != null,
                    resolutionReason = "RESOLVED_FOR_LOW_BANDWIDTH"
                )
            }
        }

        // 3. Caso ONLINE normal
        // Se usuário tem preferência específica e o dispositivo suporta
        if (request.preferredMediaType != null) {
            val preferredMatches = allActiveMedia.filter { it.mediaType == request.preferredMediaType }
            if (preferredMatches.isNotEmpty()) {
                val bestMatch = filterByAvatarAndAngle(preferredMatches, request)
                return MediaResolutionResult(
                    selectedMedia = bestMatch,
                    resolvedMediaType = bestMatch.mediaType,
                    effectiveUri = bestMatch.localCachedPath ?: bestMatch.uri,
                    effectiveThumbnailUri = bestMatch.thumbnailUri,
                    isOfflineFallback = false,
                    isLowBandwidthFallback = false,
                    isLocalAsset = bestMatch.localCachedPath != null,
                    resolutionReason = "RESOLVED_PREFERRED_MEDIA_TYPE"
                )
            }
        }

        // 4. Resolução por ordem de prioridade padrão
        val selected = selectBestFromList(allActiveMedia, request, defaultPriorityOrder)
        return MediaResolutionResult(
            selectedMedia = selected,
            resolvedMediaType = selected?.mediaType ?: ExerciseMediaType.NONE,
            effectiveUri = selected?.localCachedPath ?: selected?.uri,
            effectiveThumbnailUri = selected?.thumbnailUri,
            isOfflineFallback = false,
            isLowBandwidthFallback = false,
            isLocalAsset = selected?.localCachedPath != null,
            resolutionReason = "RESOLVED_BY_DEFAULT_PRIORITY_CASCADE"
        )
    }

    private fun selectBestFromList(
        mediaList: List<ExerciseMediaReference>,
        request: MediaResolutionRequest,
        priorityOrder: List<ExerciseMediaType>
    ): ExerciseMediaReference? {
        for (type in priorityOrder) {
            // Se o dispositivo não suporta 3D e o tipo for 3D, pula
            if (!isDeviceCapable(type, request.deviceCapabilities)) {
                continue
            }

            val matchingType = mediaList.filter { it.mediaType == type }
            if (matchingType.isNotEmpty()) {
                return filterByAvatarAndAngle(matchingType, request)
            }
        }
        return mediaList.firstOrNull()
    }

    private fun isDeviceCapable(type: ExerciseMediaType, capability: DeviceRenderingCapability): Boolean {
        return when (type) {
            ExerciseMediaType.INTERACTIVE_3D, ExerciseMediaType.ANIMATION_3D, ExerciseMediaType.VIDEO_3D -> {
                capability == DeviceRenderingCapability.HIGH_PERFORMANCE_3D || capability == DeviceRenderingCapability.STANDARD_3D
            }
            else -> true
        }
    }

    private fun filterByAvatarAndAngle(
        candidates: List<ExerciseMediaReference>,
        request: MediaResolutionRequest
    ): ExerciseMediaReference {
        // Tenta combinar avatar
        val avatarMatches = candidates.filter {
            it.animation3DMetadata?.avatarId == request.preferredAvatar
        }
        val pool = if (avatarMatches.isNotEmpty()) avatarMatches else candidates

        // Tenta combinar ângulo de câmera
        val angleMatch = pool.firstOrNull {
            it.recommendedCameraAngles.contains(request.preferredAngle) || it.animation3DMetadata?.cameraPreset == request.preferredAngle
        }

        return angleMatch ?: pool.first()
    }
}
