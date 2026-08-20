package com.example.core.exerciseengine

import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.policy.EvolutionPolicyRegistry
import com.example.core.exerciseengine.catalog.ExerciseCatalogV1
import com.example.core.exerciseengine.media.cache.CachedMediaEntry
import com.example.core.exerciseengine.media.cache.ExerciseMediaCache
import com.example.core.exerciseengine.media.model.*
import com.example.core.exerciseengine.media.registry.ExerciseMediaRegistryV1
import com.example.core.exerciseengine.media.resolver.ExerciseMediaResolver
import com.example.core.exerciseengine.media.resolver.MediaResolutionRequest
import com.example.core.exerciseengine.media.security.ExerciseMediaSecurityBarrier
import com.example.core.exerciseengine.media.validator.ExerciseMediaValidator
import com.example.core.exerciseengine.registry.ExerciseRegistryV1
import com.example.core.exerciseengine.security.ExerciseCallerTier
import com.example.core.scientific.registry.ScientificMethodologyRegistry
import com.example.core.scoreengine.catalog.ScoreDimensionCatalog
import com.example.ui.exercise.ExerciseDetailUiState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * EVOLUTION HUMAN AI — EXERCISE MEDIA & DEMONSTRATION SYSTEM V1 TEST SUITE
 *
 * Validação rigorosa dos 27 cenários exigidos pela especificação.
 */
class ExerciseMediaV1Test {

    @Before
    fun setUp() {
        ExerciseRegistryV1.resetForTesting()
        ExerciseCatalogV1.initializeCanonicalCatalog()
        ExerciseMediaRegistryV1.resetForTesting()
        ExerciseMediaRegistryV1.initializeInitialCatalog()
        ExerciseMediaSecurityBarrier.resetForTesting()
        ExerciseMediaCache.clearAll()
    }

    // 1. Mídia válida é registrada
    @Test
    fun testValidMediaRegistration() {
        val media = ExerciseMediaReference(
            mediaId = "MED-TEST-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ILLUSTRATION,
            uri = "https://assets.evolutionai.internal/illustrations/squat.svg",
            status = MediaLifecycleStatus.ACTIVE,
            licenseInfo = MediaLicenseInfo(
                source = "EVOLUTION_INTERNAL",
                license = "PROPRIETARY",
                author = "BIOMECHANICS_TEAM"
            )
        )

        val registered = ExerciseMediaRegistryV1.register(media)
        assertTrue("Mídia válida com exercício existente deve ser registrada", registered)
        val retrieved = ExerciseMediaRegistryV1.getById("MED-TEST-001")
        assertNotNull(retrieved)
        assertEquals("EX-SQ-BW-001-V1", retrieved?.exerciseId)
    }

    // 2. Mídia sem exerciseId é rejeitada
    @Test
    fun testMediaWithoutExerciseIdIsRejected() {
        val invalidMedia = ExerciseMediaReference(
            mediaId = "MED-INVALID-NO-EX",
            exerciseId = "", // Blank
            mediaType = ExerciseMediaType.ILLUSTRATION
        )

        val validation = ExerciseMediaValidator.validate(invalidMedia)
        assertFalse("Mídia sem exerciseId não pode ser válida", validation.isValid)
        assertTrue(validation.errors.contains("EXERCISE_ID_CANNOT_BE_BLANK"))

        val registered = ExerciseMediaRegistryV1.register(invalidMedia)
        assertFalse("Registro deve falhar", registered)
    }

    // 3. Mídia vinculada a exercício inexistente é rejeitada
    @Test
    fun testMediaWithNonExistentExerciseIdIsRejected() {
        val nonExistentMedia = ExerciseMediaReference(
            mediaId = "MED-GHOST-001",
            exerciseId = "EX-NON-EXISTENT-999-V1",
            mediaType = ExerciseMediaType.VIDEO_2D
        )

        val validation = ExerciseMediaValidator.validate(nonExistentMedia)
        assertFalse("Mídia associada a exercício inexistente deve ser rejeitada", validation.isValid)
        assertTrue(validation.errors.any { it.contains("EXERCISE_DOES_NOT_EXIST") })
    }

    // 4. Mídia duplicada é rejeitada (quando tenta mutação silenciosa na mesma versão)
    @Test
    fun testDuplicateMediaRejectedWhenSilentlyMutated() {
        val media1 = ExerciseMediaReference(
            mediaId = "MED-DUP-001",
            exerciseId = "EX-SQ-BW-001-V1",
            version = "V1",
            mediaType = ExerciseMediaType.ILLUSTRATION,
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(media1)

        val mutated = media1.copy(uri = "https://tampered.internal/asset.png")
        val success = ExerciseMediaRegistryV1.register(mutated)
        assertFalse("Mutação silenciosa de mídia ACTIVE na mesma versão deve ser rejeitada", success)
    }

    // 5. Mídia NONE funciona corretamente
    @Test
    fun testMediaNoneTypeFunctionsCorrectly() {
        val noneMedia = ExerciseMediaReference(
            mediaId = "MED-NONE-TEST",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.NONE,
            status = MediaLifecycleStatus.ACTIVE
        )

        val validation = ExerciseMediaValidator.validate(noneMedia)
        assertTrue("Mídia NONE deve ser considerada válida", validation.isValid)

        val registered = ExerciseMediaRegistryV1.register(noneMedia)
        assertTrue(registered)
    }

    // 6. Ausência de mídia não quebra ExerciseDetail
    @Test
    fun testAbsenceOfMediaDoesNotBreakExerciseDetail() {
        val state = ExerciseDetailUiState(
            isLoading = false,
            exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT,
            resolvedMedia = null
        )

        assertNotNull(state.exercise)
        assertEquals("Agachamento Livre (Peso Corporal)", state.exercise?.displayName)
        assertEquals("Bodyweight Squat", state.exercise?.canonicalName)
        assertNull(state.resolvedMedia)
    }

    // 7. Mídia ACTIVE pode ser resolvida
    @Test
    fun testActiveMediaCanBeResolved() {
        val media = ExerciseMediaReference(
            mediaId = "MED-ACTIVE-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ILLUSTRATION,
            uri = "https://assets.evolutionai.internal/active.png",
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(media)

        val result = ExerciseMediaResolver.resolve(
            MediaResolutionRequest(exerciseId = "EX-SQ-BW-001-V1")
        )

        assertNotNull(result.selectedMedia)
        assertEquals(ExerciseMediaType.ILLUSTRATION, result.resolvedMediaType)
    }

    // 8. Mídia DRAFT não aparece como oficial
    @Test
    fun testDraftMediaDoesNotAppearAsOfficial() {
        ExerciseMediaRegistryV1.resetForTesting()

        val draftMedia = ExerciseMediaReference(
            mediaId = "MED-DRAFT-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ANIMATION_3D,
            status = MediaLifecycleStatus.DRAFT
        )
        ExerciseMediaRegistryV1.register(draftMedia)

        val activeList = ExerciseMediaRegistryV1.getActiveByExerciseId("EX-SQ-BW-001-V1")
        assertTrue("Mídia DRAFT não deve estar no catálogo ativo", activeList.isEmpty())

        val resolved = ExerciseMediaResolver.resolve(
            MediaResolutionRequest(exerciseId = "EX-SQ-BW-001-V1")
        )
        assertEquals(ExerciseMediaType.NONE, resolved.resolvedMediaType)
    }

    // 9. Mídia REJECTED não aparece
    @Test
    fun testRejectedMediaDoesNotAppear() {
        ExerciseMediaRegistryV1.resetForTesting()

        val rejectedMedia = ExerciseMediaReference(
            mediaId = "MED-REJECTED-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.VIDEO_2D,
            status = MediaLifecycleStatus.REJECTED
        )
        ExerciseMediaRegistryV1.register(rejectedMedia)

        val activeList = ExerciseMediaRegistryV1.getActiveByExerciseId("EX-SQ-BW-001-V1")
        assertTrue("Mídia REJECTED não deve constar nos ativos", activeList.isEmpty())
    }

    // 10. Fallback funciona (3D -> Video -> GIF -> Image Sequence -> Illustration -> NONE)
    @Test
    fun testMediaFallbackCascade() {
        ExerciseMediaRegistryV1.resetForTesting()

        // Registra apenas ILLUSTRATION para o exercício
        val illustrationMedia = ExerciseMediaReference(
            mediaId = "MED-ILLUST-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ILLUSTRATION,
            uri = "https://assets.evolutionai.internal/squat_illustration.png",
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(illustrationMedia)

        // Usuário solicita 3D, mas sistema faz fallback para ILLUSTRATION
        val resolution = ExerciseMediaResolver.resolve(
            MediaResolutionRequest(
                exerciseId = "EX-SQ-BW-001-V1",
                preferredMediaType = ExerciseMediaType.ANIMATION_3D
            )
        )

        assertNotNull(resolution.selectedMedia)
        assertEquals(ExerciseMediaType.ILLUSTRATION, resolution.resolvedMediaType)
    }

    // 11. Modo OFFLINE utiliza mídia local
    @Test
    fun testOfflineModeUsesLocalCachedMedia() {
        val cachedMedia = ExerciseMediaReference(
            mediaId = "MED-OFFLINE-LOCAL-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.VIDEO_2D,
            uri = "https://remote.cdn/squat.mp4",
            localCachedPath = "/data/user/0/com.example/cache/squat.mp4",
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(cachedMedia)

        val result = ExerciseMediaResolver.resolve(
            MediaResolutionRequest(
                exerciseId = "EX-SQ-BW-001-V1",
                networkStatus = NetworkStatus.OFFLINE
            )
        )

        assertTrue("Deve usar fallback offline", result.isOfflineFallback)
        assertTrue("Deve ser reconhecido como asset local", result.isLocalAsset)
        assertEquals("/data/user/0/com.example/cache/squat.mp4", result.effectiveUri)
    }

    // 12. Modo OFFLINE funciona sem mídia
    @Test
    fun testOfflineModeWorksWithoutMediaWithoutCrashing() {
        ExerciseMediaRegistryV1.resetForTesting()

        val result = ExerciseMediaResolver.resolve(
            MediaResolutionRequest(
                exerciseId = "EX-SQ-BW-001-V1",
                networkStatus = NetworkStatus.OFFLINE
            )
        )

        assertTrue(result.isOfflineFallback)
        assertEquals(ExerciseMediaType.NONE, result.resolvedMediaType)
    }

    // 13. Baixa largura de banda seleciona mídia adequada
    @Test
    fun testLowBandwidthSelectsLightweightMedia() {
        ExerciseMediaRegistryV1.resetForTesting()

        // Registra um vídeo pesado e uma ilustração leve
        val heavyVideo = ExerciseMediaReference(
            mediaId = "MED-HEAVY-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.VIDEO_3D,
            fileSize = 50_000_000L,
            status = MediaLifecycleStatus.ACTIVE
        )
        val lightIllustration = ExerciseMediaReference(
            mediaId = "MED-LIGHT-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ILLUSTRATION,
            fileSize = 150_000L,
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(heavyVideo)
        ExerciseMediaRegistryV1.register(lightIllustration)

        val result = ExerciseMediaResolver.resolve(
            MediaResolutionRequest(
                exerciseId = "EX-SQ-BW-001-V1",
                networkStatus = NetworkStatus.LOW_BANDWIDTH
            )
        )

        assertTrue(result.isLowBandwidthFallback)
        assertEquals(ExerciseMediaType.ILLUSTRATION, result.resolvedMediaType)
    }

    // 14. Checksum inválido é detectado
    @Test
    fun testInvalidChecksumIsDetected() {
        val corruptedMedia = ExerciseMediaReference(
            mediaId = "MED-CORRUPT-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ILLUSTRATION,
            checksumSha256 = "invalid_fake_checksum_not_64_chars"
        )

        val validation = ExerciseMediaValidator.validate(corruptedMedia)
        assertFalse("Checksum inválido deve ser detectado e rejeitado", validation.isValid)
        assertTrue(validation.errors.contains("INVALID_CHECKSUM_SHA256"))
    }

    // 15. CLIENT não pode publicar mídia
    @Test
    fun testClientCannotPublishMedia() {
        val media = ExerciseMediaReference(
            mediaId = "MED-CLIENT-UNAUTHORIZED",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ILLUSTRATION
        )

        val success = ExerciseMediaSecurityBarrier.registerOrUpdateMedia(
            callerTier = ExerciseCallerTier.CLIENT,
            callerId = "untrusted_mobile_client_01",
            media = media
        )

        assertFalse("CLIENT não pode registrar nem modificar mídia", success)
        val audits = ExerciseMediaSecurityBarrier.getAuditLogs()
        assertTrue(audits.any { it.securityViolation && it.callerTier == ExerciseCallerTier.CLIENT })
    }

    // 16. AI_GATEWAY não pode publicar mídia
    @Test
    fun testAiGatewayCannotPublishMedia() {
        val media = ExerciseMediaReference(
            mediaId = "MED-AI-UNAUTHORIZED",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.IMAGE_SEQUENCE
        )

        val success = ExerciseMediaSecurityBarrier.registerOrUpdateMedia(
            callerTier = ExerciseCallerTier.AI_GATEWAY,
            callerId = "gemini_pro_agent",
            media = media
        )

        assertFalse("AI_GATEWAY não pode publicar mídia", success)
        val audits = ExerciseMediaSecurityBarrier.getAuditLogs()
        assertTrue(audits.any { it.securityViolation && it.callerTier == ExerciseCallerTier.AI_GATEWAY })
    }

    // 17. Histórico de mídia permanece imutável
    @Test
    fun testMediaHistoryRemainsImmutable() {
        val v1 = ExerciseMediaReference(
            mediaId = "MED-HIST-001",
            exerciseId = "EX-SQ-BW-001-V1",
            version = "V1",
            mediaType = ExerciseMediaType.ILLUSTRATION,
            uri = "https://assets.evolutionai.internal/v1.png",
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(v1)

        val v2 = v1.copy(
            version = "V2",
            uri = "https://assets.evolutionai.internal/v2.png"
        )
        ExerciseMediaRegistryV1.register(v2)

        val v1Retrieved = ExerciseMediaRegistryV1.getByIdAndVersion("MED-HIST-001", "V1")
        val v2Retrieved = ExerciseMediaRegistryV1.getByIdAndVersion("MED-HIST-001", "V2")

        assertNotNull(v1Retrieved)
        assertNotNull(v2Retrieved)
        assertEquals("https://assets.evolutionai.internal/v1.png", v1Retrieved?.uri)
        assertEquals("https://assets.evolutionai.internal/v2.png", v2Retrieved?.uri)
    }

    // 18. Nova versão de mídia cria novo contexto
    @Test
    fun testNewMediaVersionCreatesNewContext() {
        val v1 = ExerciseMediaReference(
            mediaId = "MED-CTX-001",
            exerciseId = "EX-SQ-BW-001-V1",
            version = "V1",
            mediaType = ExerciseMediaType.GIF,
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(v1)

        val v2 = v1.copy(version = "V2", mediaType = ExerciseMediaType.ANIMATION_3D)
        ExerciseMediaRegistryV1.register(v2)

        val allVersions = ExerciseMediaRegistryV1.getAllVersions("MED-CTX-001")
        assertEquals(2, allVersions.size)
        assertEquals("V2", ExerciseMediaRegistryV1.getById("MED-CTX-001")?.version)
    }

    // 19. MALE_AVATAR_V1 pode ser associado
    @Test
    fun testMaleAvatarCanBeAssociated() {
        val maleAnim = ExerciseMediaReference(
            mediaId = "MED-ANIM-MALE-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ANIMATION_3D,
            animation3DMetadata = Animation3DMetadata(
                avatarId = AvatarCharacterId.MALE_AVATAR_V1,
                modelId = "MODEL-MALE-V1",
                animationClipId = "ANIM-SQUAT-BASIC-V1"
            ),
            status = MediaLifecycleStatus.ACTIVE
        )

        val registered = ExerciseMediaRegistryV1.register(maleAnim)
        assertTrue(registered)
        assertEquals(AvatarCharacterId.MALE_AVATAR_V1, maleAnim.animation3DMetadata?.avatarId)
    }

    // 20. FEMALE_AVATAR_V1 pode ser associado
    @Test
    fun testFemaleAvatarCanBeAssociated() {
        val femaleAnim = ExerciseMediaReference(
            mediaId = "MED-ANIM-FEMALE-001",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ANIMATION_3D,
            animation3DMetadata = Animation3DMetadata(
                avatarId = AvatarCharacterId.FEMALE_AVATAR_V1,
                modelId = "MODEL-FEMALE-V1",
                animationClipId = "ANIM-SQUAT-BASIC-V1"
            ),
            status = MediaLifecycleStatus.ACTIVE
        )

        val registered = ExerciseMediaRegistryV1.register(femaleAnim)
        assertTrue(registered)
        assertEquals(AvatarCharacterId.FEMALE_AVATAR_V1, femaleAnim.animation3DMetadata?.avatarId)
    }

    // 21. Mesmo exercício funciona com ambos os avatares
    @Test
    fun testSameExerciseWorksWithBothAvatars() {
        val maleAnim = ExerciseMediaReference(
            mediaId = "MED-SQ-MALE-V1",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ANIMATION_3D,
            animation3DMetadata = Animation3DMetadata(
                avatarId = AvatarCharacterId.MALE_AVATAR_V1,
                animationClipId = "ANIM-SQUAT-BASIC-V1"
            ),
            status = MediaLifecycleStatus.ACTIVE
        )
        val femaleAnim = ExerciseMediaReference(
            mediaId = "MED-SQ-FEMALE-V1",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ANIMATION_3D,
            animation3DMetadata = Animation3DMetadata(
                avatarId = AvatarCharacterId.FEMALE_AVATAR_V1,
                animationClipId = "ANIM-SQUAT-BASIC-V1"
            ),
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(maleAnim)
        ExerciseMediaRegistryV1.register(femaleAnim)

        // Resolução para Masculino
        val maleResolution = ExerciseMediaResolver.resolve(
            MediaResolutionRequest(
                exerciseId = "EX-SQ-BW-001-V1",
                preferredAvatar = AvatarCharacterId.MALE_AVATAR_V1
            )
        )
        assertEquals(AvatarCharacterId.MALE_AVATAR_V1, maleResolution.selectedMedia?.animation3DMetadata?.avatarId)

        // Resolução para Feminino
        val femaleResolution = ExerciseMediaResolver.resolve(
            MediaResolutionRequest(
                exerciseId = "EX-SQ-BW-001-V1",
                preferredAvatar = AvatarCharacterId.FEMALE_AVATAR_V1
            )
        )
        assertEquals(AvatarCharacterId.FEMALE_AVATAR_V1, femaleResolution.selectedMedia?.animation3DMetadata?.avatarId)
    }

    // 22. Avatar não cria novo ExerciseDefinition
    @Test
    fun testAvatarDoesNotCreateNewExerciseDefinition() {
        val exerciseCountBefore = ExerciseRegistryV1.getAll().size

        val femaleAnim = ExerciseMediaReference(
            mediaId = "MED-SQ-FEMALE-NEW",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.ANIMATION_3D,
            animation3DMetadata = Animation3DMetadata(avatarId = AvatarCharacterId.FEMALE_AVATAR_V1),
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(femaleAnim)

        val exerciseCountAfter = ExerciseRegistryV1.getAll().size
        assertEquals("O número de ExerciseDefinitions deve permanecer inalterado", exerciseCountBefore, exerciseCountAfter)
    }

    // 23. Mídia não altera ExerciseDefinition
    @Test
    fun testMediaDoesNotAlterExerciseDefinition() {
        val exerciseBefore = ExerciseRegistryV1.getById("EX-SQ-BW-001-V1")
        assertNotNull(exerciseBefore)

        val newMedia = ExerciseMediaReference(
            mediaId = "MED-INTERACTIVE-NEW",
            exerciseId = "EX-SQ-BW-001-V1",
            mediaType = ExerciseMediaType.INTERACTIVE_3D,
            status = MediaLifecycleStatus.ACTIVE
        )
        ExerciseMediaRegistryV1.register(newMedia)

        val exerciseAfter = ExerciseRegistryV1.getById("EX-SQ-BW-001-V1")
        assertEquals(exerciseBefore, exerciseAfter)
    }

    // 24. Mídia não altera Evolution Engine
    @Test
    fun testMediaDoesNotAlterEvolutionEngine() {
        val classCountBefore = ClassCatalog.CLASSES.size
        val policyCountBefore = EvolutionPolicyRegistry.getAllPolicies().size

        // Registra nova mídia e valida que estruturas e políticas do motor de evolução permanecem intactas
        ExerciseMediaRegistryV1.register(
            ExerciseMediaReference(
                mediaId = "MED-EVO-CHECK",
                exerciseId = "EX-SQ-BW-001-V1",
                mediaType = ExerciseMediaType.VIDEO_3D,
                status = MediaLifecycleStatus.ACTIVE
            )
        )

        val classCountAfter = ClassCatalog.CLASSES.size
        val policyCountAfter = EvolutionPolicyRegistry.getAllPolicies().size

        assertEquals(classCountBefore, classCountAfter)
        assertEquals(policyCountBefore, policyCountAfter)
    }

    // 25. Mídia não altera Score Engine
    @Test
    fun testMediaDoesNotAlterScoreEngine() {
        val dimensionsBefore = ScoreDimensionCatalog.MAPPINGS.size

        ExerciseMediaRegistryV1.register(
            ExerciseMediaReference(
                mediaId = "MED-SCORE-CHECK",
                exerciseId = "EX-PSH-STD-001-V1",
                mediaType = ExerciseMediaType.GIF,
                status = MediaLifecycleStatus.ACTIVE
            )
        )

        val dimensionsAfter = ScoreDimensionCatalog.MAPPINGS.size
        assertEquals(dimensionsBefore, dimensionsAfter)
    }

    // 26. Mídia não altera Scientific Registry
    @Test
    fun testMediaDoesNotAlterScientificRegistry() {
        val methodologiesBefore = ScientificMethodologyRegistry.getAllMethodologies().size
        assertTrue(methodologiesBefore > 0)

        ExerciseMediaRegistryV1.register(
            ExerciseMediaReference(
                mediaId = "MED-SCI-CHECK",
                exerciseId = "EX-SQ-BW-001-V1",
                mediaType = ExerciseMediaType.MOTION_CAPTURE,
                status = MediaLifecycleStatus.ACTIVE
            )
        )

        val methodologiesAfter = ScientificMethodologyRegistry.getAllMethodologies().size
        assertEquals(methodologiesBefore, methodologiesAfter)
    }

    // 27. Mesmo input produz resolução determinística
    @Test
    fun testDeterministicMediaResolution() {
        val request = MediaResolutionRequest(
            exerciseId = "EX-SQ-BW-001-V1",
            preferredAvatar = AvatarCharacterId.MALE_AVATAR_V1,
            preferredAngle = CameraPreset.SIDE,
            deviceCapabilities = DeviceRenderingCapability.STANDARD_3D,
            networkStatus = NetworkStatus.ONLINE
        )

        val res1 = ExerciseMediaResolver.resolve(request)
        val res2 = ExerciseMediaResolver.resolve(request)
        val res3 = ExerciseMediaResolver.resolve(request)

        assertEquals(res1.resolvedMediaType, res2.resolvedMediaType)
        assertEquals(res2.resolvedMediaType, res3.resolvedMediaType)
        assertEquals(res1.selectedMedia?.mediaId, res2.selectedMedia?.mediaId)
        assertEquals(res2.selectedMedia?.mediaId, res3.selectedMedia?.mediaId)
        assertEquals(res1.effectiveUri, res2.effectiveUri)
    }
}
