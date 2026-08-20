package com.example.core.exerciseengine.media.model

import java.security.MessageDigest

/**
 * EVOLUTION HUMAN AI — EXERCISE MEDIA & DEMONSTRATION SYSTEM V1
 *
 * Modelos fundamentais da camada de mídia e demonstração visual.
 * Princípio: ExerciseDefinition = definição do exercício.
 *            MediaReference = representação visual do exercício.
 * A mídia NÃO define a verdade científica do exercício.
 * A ausência de mídia NÃO invalida um ExerciseDefinition.
 */

enum class ExerciseMediaType {
    ILLUSTRATION,
    IMAGE_SEQUENCE,
    GIF,
    VIDEO_2D,
    VIDEO_3D,
    ANIMATION_3D,
    MOTION_CAPTURE,
    INTERACTIVE_3D,
    NONE
}

enum class MediaLifecycleStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    ACTIVE,
    DEPRECATED,
    REJECTED,
    ARCHIVED
}

enum class CameraPreset {
    FRONT,
    SIDE,
    THREE_QUARTER,
    BACK,
    TOP,
    CUSTOM,
    NOT_SPECIFIED
}

enum class NetworkStatus {
    ONLINE,
    OFFLINE,
    LOW_BANDWIDTH
}

enum class DeviceRenderingCapability {
    HIGH_PERFORMANCE_3D,
    STANDARD_3D,
    BASIC_2D,
    MINIMAL
}

enum class AvatarCharacterId {
    MALE_AVATAR_V1,
    FEMALE_AVATAR_V1,
    GENERIC_NEUTRAL_V1,
    NOT_APPLICABLE
}

enum class AnimationLoopMode {
    LOOP,
    ONCE,
    PING_PONG,
    STEP_BY_STEP
}

data class AnimationPhaseKeyframe(
    val phaseName: String, // e.g. "START_POSITION", "ECCENTRIC_PHASE", "TRANSITION", "CONCENTRIC_PHASE", "END_POSITION"
    val timestampMs: Long,
    val description: String? = null
)

/**
 * Metadados preparados para futuros Avatares 3D e renderização tridimensional.
 * Não contém models 3D fictícios, apenas a infraestrutura tipada necessária.
 */
data class Animation3DMetadata(
    val avatarId: AvatarCharacterId = AvatarCharacterId.NOT_APPLICABLE,
    val modelId: String? = null,
    val skeletonId: String? = null,
    val animationClipId: String? = null,
    val rigVersion: String = "V1",
    val animationVersion: String = "V1",
    val loopMode: AnimationLoopMode = AnimationLoopMode.LOOP,
    val cameraPreset: CameraPreset = CameraPreset.FRONT,
    val environmentPreset: String = "STUDIO_NEUTRAL",
    val keyframes: List<AnimationPhaseKeyframe> = emptyList()
)

data class MediaLicenseInfo(
    val source: String = "OFFICIAL_EVOLUTION_AI",
    val license: String = "PROPRIETARY_EVOLUTION_AI",
    val author: String = "EVOLUTION_HUMAN_AI_CORE",
    val usageRights: String = "ALL_RIGHTS_RESERVED"
)

/**
 * Referência canônica imutável de mídia para exercícios.
 * Ausência de valores desconhecidos é preservada como null ou NOT_SPECIFIED.
 */
data class ExerciseMediaReference(
    val mediaId: String,
    val exerciseId: String,
    val exerciseVersion: String = "V1",
    val mediaType: ExerciseMediaType,
    val uri: String? = null,
    val localCachedPath: String? = null,
    val thumbnailUri: String? = null,
    val posterUri: String? = null,
    val durationMs: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val frameRate: Double? = null,
    val fileSize: Long? = null,
    val format: String? = null,
    val licenseInfo: MediaLicenseInfo = MediaLicenseInfo(),
    val version: String = "V1",
    val status: MediaLifecycleStatus = MediaLifecycleStatus.ACTIVE,
    val isOfficial: Boolean = true,
    val recommendedCameraAngles: List<CameraPreset> = listOf(CameraPreset.FRONT),
    val animation3DMetadata: Animation3DMetadata? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val checksumSha256: String = ""
) {
    fun calculateChecksum(): String {
        val payload = "$mediaId|$exerciseId|$exerciseVersion|$mediaType|$uri|$version|$status|$isOfficial|${licenseInfo.license}"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
