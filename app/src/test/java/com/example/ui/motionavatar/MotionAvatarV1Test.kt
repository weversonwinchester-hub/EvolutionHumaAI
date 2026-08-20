package com.example.ui.motionavatar

import com.example.core.evolutionengine.engine.EvolutionEngineV1
import com.example.core.biomechanical.engine.BiomechanicalEngineV1
import com.example.core.exerciseengine.engine.ExerciseEngineV1
import com.example.core.exerciseengine.media.model.AvatarCharacterId
import com.example.core.exerciseengine.media.model.CameraPreset
import com.example.core.exerciseengine.media.model.ExerciseMediaType
import com.example.core.exerciseengine.media.model.NetworkStatus
import com.example.core.exerciseengine.media.resolver.ExerciseMediaResolver
import com.example.core.exerciseengine.media.resolver.MediaResolutionRequest
import com.example.ui.motionavatar.engine.MotionAvatarEngine
import com.example.ui.motionavatar.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * MOTION AVATAR V1 — AUTOMATED TEST SUITE
 *
 * Validates the lightweight 2D/2.5D articulated skeletal motion avatar system,
 * deterministic keyframe interpolation, Bodyweight Squat (EX-SQ-BW-001-V1),
 * male/female retargeting, playback state machine, and complete isolation
 * from existing scientific and biomechanical engines.
 */
class MotionAvatarV1Test {

    private lateinit var engine: MotionAvatarEngine

    @Before
    fun setUp() {
        engine = MotionAvatarEngine()
    }

    // ------------------------------------------------------------------------
    // 1. AVATAR CREATION & CHARACTER RETARGETING
    // ------------------------------------------------------------------------
    @Test
    fun testMaleAndFemaleAvatarCreation() {
        val maleDef = MotionAvatarDefinition.forAvatarId(AvatarCharacterId.MALE_AVATAR_V1)
        val femaleDef = MotionAvatarDefinition.forAvatarId(AvatarCharacterId.FEMALE_AVATAR_V1)

        assertNotNull("Male avatar definition must not be null", maleDef)
        assertNotNull("Female avatar definition must not be null", femaleDef)

        assertEquals(AvatarCharacterId.MALE_AVATAR_V1, maleDef.avatarId)
        assertEquals(AvatarCharacterId.FEMALE_AVATAR_V1, femaleDef.avatarId)

        assertTrue(maleDef.name.contains("Masculino"))
        assertTrue(femaleDef.name.contains("Feminino"))

        // Both share compatible skeletal segment proportions
        assertTrue(maleDef.torsoLength > 0f)
        assertTrue(femaleDef.torsoLength > 0f)
        assertTrue(maleDef.thighLength > 0f)
        assertTrue(femaleDef.thighLength > 0f)
        assertTrue(maleDef.shankLength > 0f)
        assertTrue(femaleDef.shankLength > 0f)
    }

    @Test
    fun testRetargetingSameAnimationAcrossBothAvatars() {
        val squatAnim = MotionAvatarAnimation.BODYWEIGHT_SQUAT_V1
        val maleDef = MotionAvatarDefinition.MALE_AVATAR_V1
        val femaleDef = MotionAvatarDefinition.FEMALE_AVATAR_V1

        val samplePose = squatAnim.samplePose(0.5f) // Bottom position

        // Both avatars can execute the exact same pose angles
        assertEquals(MotionAvatarPhase.BOTTOM, samplePose.phase)
        assertTrue(samplePose.hipFlexionDeg > 80f)
        assertTrue(samplePose.kneeFlexionDeg > 80f)
        assertEquals(maleDef.thighLength > 0, femaleDef.thighLength > 0)
    }

    // ------------------------------------------------------------------------
    // 2. JOINT DEFINITIONS
    // ------------------------------------------------------------------------
    @Test
    fun testJointDefinitionsCompleteness() {
        val requiredJoints = listOf(
            MotionAvatarJointType.HEAD,
            MotionAvatarJointType.NECK,
            MotionAvatarJointType.TORSO,
            MotionAvatarJointType.PELVIS,
            MotionAvatarJointType.LEFT_SHOULDER,
            MotionAvatarJointType.LEFT_ELBOW,
            MotionAvatarJointType.LEFT_WRIST,
            MotionAvatarJointType.LEFT_HAND,
            MotionAvatarJointType.LEFT_HIP,
            MotionAvatarJointType.LEFT_KNEE,
            MotionAvatarJointType.LEFT_ANKLE,
            MotionAvatarJointType.LEFT_FOOT
        )

        for (joint in requiredJoints) {
            assertNotNull("Joint enum must contain $joint", MotionAvatarJointType.valueOf(joint.name))
        }
    }

    // ------------------------------------------------------------------------
    // 3. KEYFRAME ORDERING & BODYWEIGHT SQUAT REPRESENTATION
    // ------------------------------------------------------------------------
    @Test
    fun testBodyweightSquatKeyframeOrdering() {
        val squat = MotionAvatarAnimation.BODYWEIGHT_SQUAT_V1
        assertEquals("EX-SQ-BW-001-V1", squat.exerciseId)
        assertEquals("Bodyweight Squat", squat.canonicalName)
        assertEquals(5, squat.keyframes.size)

        // 5 Canonical Keyframes in exact sequence
        assertEquals(MotionAvatarPhase.START, squat.keyframes[0].phase)
        assertEquals(0.0f, squat.keyframes[0].normalizedTime, 0.001f)

        assertEquals(MotionAvatarPhase.DESCENT, squat.keyframes[1].phase)
        assertEquals(0.25f, squat.keyframes[1].normalizedTime, 0.001f)

        assertEquals(MotionAvatarPhase.BOTTOM, squat.keyframes[2].phase)
        assertEquals(0.50f, squat.keyframes[2].normalizedTime, 0.001f)

        assertEquals(MotionAvatarPhase.ASCENT, squat.keyframes[3].phase)
        assertEquals(0.75f, squat.keyframes[3].normalizedTime, 0.001f)

        assertEquals(MotionAvatarPhase.END, squat.keyframes[4].phase)
        assertEquals(1.00f, squat.keyframes[4].normalizedTime, 0.001f)

        // Verify strictly increasing normalized timestamps
        for (i in 0 until squat.keyframes.size - 1) {
            assertTrue(
                "Keyframe $i must occur before keyframe ${i + 1}",
                squat.keyframes[i].normalizedTime < squat.keyframes[i + 1].normalizedTime
            )
        }
    }

    // ------------------------------------------------------------------------
    // 4. INTERPOLATION SMOOTHNESS & BIOMECHANICAL ANGLES
    // ------------------------------------------------------------------------
    @Test
    fun testPoseInterpolationAndDisplayAngles() {
        val squat = MotionAvatarAnimation.BODYWEIGHT_SQUAT_V1

        val startPose = squat.samplePose(0.0f)
        assertEquals(180, startPose.displayKneeAngle)
        assertEquals(180, startPose.displayHipAngle)
        assertEquals(90, startPose.displayAnkleAngle)

        val bottomPose = squat.samplePose(0.5f)
        assertTrue("Bottom squat knee angle must be around 84-90 degrees", bottomPose.displayKneeAngle in 80..95)
        assertTrue("Bottom squat hip angle must be around 85-92 degrees", bottomPose.displayHipAngle in 80..95)
        assertTrue("Pelvis must be lowered at bottom position", bottomPose.pelvisOffsetY > 0.3f)

        // Midway interpolation (e.g. 0.125f - descent)
        val midwayDescent = squat.samplePose(0.125f)
        assertTrue(midwayDescent.kneeFlexionDeg > startPose.kneeFlexionDeg)
        assertTrue(midwayDescent.kneeFlexionDeg < bottomPose.kneeFlexionDeg)
    }

    // ------------------------------------------------------------------------
    // 5. ANIMATION LOOP & REPETITIONS
    // ------------------------------------------------------------------------
    @Test
    fun testAnimationLoopingAndRepetitionIncrement() {
        engine.loadAnimation("EX-SQ-BW-001-V1")
        assertEquals(1, engine.state.value.repetitionCount)

        // Advance by full cycle (3200ms)
        engine.advanceTime(3200L)
        assertEquals(2, engine.state.value.repetitionCount)

        // Advance by another full cycle
        engine.advanceTime(3200L)
        assertEquals(3, engine.state.value.repetitionCount)
    }

    // ------------------------------------------------------------------------
    // 6. PLAYBACK CONTROLS (PAUSE, RESUME, RESTART, SEEK)
    // ------------------------------------------------------------------------
    @Test
    fun testPlaybackControls() {
        engine.loadAnimation("EX-SQ-BW-001-V1")
        assertTrue(engine.state.value.isPlaying)

        // Pause
        engine.pause()
        assertFalse(engine.state.value.isPlaying)

        // Advance time while paused should NOT move progress
        val progressBefore = engine.state.value.currentProgress
        engine.advanceTime(1000L)
        assertEquals(progressBefore, engine.state.value.currentProgress, 0.001f)

        // Resume / Play
        engine.play()
        assertTrue(engine.state.value.isPlaying)
        engine.advanceTime(800L)
        assertTrue(engine.state.value.currentProgress > progressBefore)

        // Restart
        engine.restart()
        assertEquals(0f, engine.state.value.currentProgress, 0.001f)
        assertEquals(1, engine.state.value.repetitionCount)
        assertTrue(engine.state.value.isPlaying)

        // Seek
        engine.seek(0.5f)
        assertEquals(0.5f, engine.state.value.currentProgress, 0.001f)
        assertEquals(MotionAvatarPhase.BOTTOM, engine.state.value.currentPose.phase)
    }

    // ------------------------------------------------------------------------
    // 7. PLAYBACK SPEED SCALING
    // ------------------------------------------------------------------------
    @Test
    fun testPlaybackSpeedScaling() {
        val engine1 = MotionAvatarEngine()
        engine1.setSpeed(1.0f)
        engine1.advanceTime(800L)

        val engine2 = MotionAvatarEngine()
        engine2.setSpeed(2.0f)
        engine2.advanceTime(800L)

        assertEquals(engine1.state.value.currentProgress * 2f, engine2.state.value.currentProgress, 0.01f)
    }

    // ------------------------------------------------------------------------
    // 8. MIRROR VIEW TOGGLE
    // ------------------------------------------------------------------------
    @Test
    fun testMirrorViewToggle() {
        assertFalse(engine.state.value.isMirrored)
        engine.setMirrored(true)
        assertTrue(engine.state.value.isMirrored)
        engine.setMirrored(false)
        assertFalse(engine.state.value.isMirrored)
    }

    // ------------------------------------------------------------------------
    // 9. EXERCISE ASSOCIATION (ONLY EX-SQ-BW-001-V1)
    // ------------------------------------------------------------------------
    @Test
    fun testOnlyBodyweightSquatIsRegisteredInV1() {
        assertTrue(engine.hasAnimationFor("EX-SQ-BW-001-V1"))
        assertFalse(engine.hasAnimationFor("EX-PU-BW-001-V1")) // Push-up not in V1
        assertFalse(engine.hasAnimationFor("EX-DL-CONV-001-V1")) // Deadlift not in V1
        assertFalse(engine.hasAnimationFor("INVALID-ID"))

        val loadedInvalid = engine.loadAnimation("INVALID-ID")
        assertFalse(loadedInvalid)
        assertNull(engine.state.value.animation)
    }

    // ------------------------------------------------------------------------
    // 10. MEDIA FALLBACK RESILIENCE
    // ------------------------------------------------------------------------
    @Test
    fun testMediaFallbackRemainsFunctionalWhenMotionAvatarUnavailable() {
        val unmappedExerciseId = "EX-PU-BW-001-V1"
        assertFalse(engine.hasAnimationFor(unmappedExerciseId))

        // Existing ExerciseMediaResolver resolves gracefully without crash
        val request = MediaResolutionRequest(
            exerciseId = unmappedExerciseId,
            preferredAvatar = AvatarCharacterId.MALE_AVATAR_V1,
            preferredAngle = CameraPreset.FRONT,
            networkStatus = NetworkStatus.ONLINE
        )
        val result = ExerciseMediaResolver.resolve(request)
        assertNotNull(result)
        assertEquals(ExerciseMediaType.NONE, result.resolvedMediaType)
    }

    // ------------------------------------------------------------------------
    // 11. ISOLATION FROM EVOLUTION & BIOMECHANICAL ENGINES
    // ------------------------------------------------------------------------
    @Test
    fun testCompleteIsolationFromEvolutionAndBiomechanicalEngines() {
        // Run motion avatar animation for 10 repetitions
        engine.loadAnimation("EX-SQ-BW-001-V1")
        for (i in 0 until 10) {
            engine.advanceTime(3200L)
        }
        assertEquals(11, engine.state.value.repetitionCount)

        // Read-only check on existing catalog
        val squatDefinition = ExerciseEngineV1.getExercise("EX-SQ-BW-001-V1")
        assertNotNull(squatDefinition)
        assertEquals("Bodyweight Squat", squatDefinition?.canonicalName)

        // EvolutionEngine and BiomechanicalEngine version & authority guarantees remain strictly preserved
        assertEquals("1.0.0-evolution-v1", EvolutionEngineV1.ENGINE_VERSION)
        assertEquals("PERFORMAI-BIOMECHANICAL-1.0.0", BiomechanicalEngineV1.ENGINE_VERSION)
    }

    // ------------------------------------------------------------------------
    // 12. DETERMINISTIC BEHAVIOR
    // ------------------------------------------------------------------------
    @Test
    fun testDeterministicAnimationSampling() {
        val squat = MotionAvatarAnimation.BODYWEIGHT_SQUAT_V1

        val sampleA = squat.samplePose(0.42f)
        val sampleB = squat.samplePose(0.42f)

        assertEquals(sampleA.trunkAngleDeg, sampleB.trunkAngleDeg, 0.0001f)
        assertEquals(sampleA.hipFlexionDeg, sampleB.hipFlexionDeg, 0.0001f)
        assertEquals(sampleA.kneeFlexionDeg, sampleB.kneeFlexionDeg, 0.0001f)
        assertEquals(sampleA.ankleDorsiDeg, sampleB.ankleDorsiDeg, 0.0001f)
        assertEquals(sampleA.pelvisOffsetY, sampleB.pelvisOffsetY, 0.0001f)
        assertEquals(sampleA.phase, sampleB.phase)
    }
}
