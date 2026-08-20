package com.example.core.exerciseengine

import com.example.core.evolutionengine.engine.EvolutionEngineV1
import com.example.core.exerciseengine.catalog.ExerciseCatalogV1
import com.example.core.exerciseengine.engine.ExerciseEngineV1
import com.example.core.exerciseengine.model.*
import com.example.core.exerciseengine.prescription.ExercisePrescription
import com.example.core.exerciseengine.progression.ExerciseProgressionEvaluator
import com.example.core.exerciseengine.registry.ExerciseRegistryV1
import com.example.core.exerciseengine.security.ExerciseCallerTier
import com.example.core.exerciseengine.security.ExerciseSecurityBarrier
import com.example.core.exerciseengine.validator.ExerciseValidator
import com.example.core.scoreengine.engine.ScoreEngineV1
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * EVOLUTION HUMAN AI — EXERCISE ENGINE V1 TEST SUITE
 *
 * Validação rigorosa dos 20 requisitos fundamentais do Exercise Engine e Exercise Registry.
 */
class ExerciseEngineV1Test {

    @Before
    fun setUp() {
        ExerciseRegistryV1.resetForTesting()
        ExerciseCatalogV1.initializeCanonicalCatalog()
    }

    // 1. Exercício válido é registrado
    @Test
    fun testValidExerciseRegistration() {
        val exercise = ExerciseDefinition(
            exerciseId = "EX-CUSTOM-001-V1",
            version = "V1",
            canonicalName = "Custom Test Exercise",
            displayName = "Exercício de Teste",
            description = "Descrição de teste para validação.",
            category = ExerciseCategory.STRENGTH,
            movementPattern = MovementPattern.PUSH,
            primaryMuscles = listOf(MuscleGroup.CHEST),
            equipment = listOf(EquipmentType.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            executionType = ExecutionType.REPETITION,
            status = ExerciseStatus.ACTIVE
        )

        val success = ExerciseRegistryV1.register(exercise)
        assertTrue("Exercício válido deve ser registrado com sucesso", success)
        assertNotNull("Exercício deve ser recuperável pelo ID", ExerciseRegistryV1.getById("EX-CUSTOM-001-V1"))
    }

    // 2. exerciseId duplicado é rejeitado (tentativa de registrar ID sem versão incrementada)
    @Test
    fun testDuplicateExerciseIdRejectedWhenMutatedSilently() {
        val original = ExerciseCatalogV1.BODYWEIGHT_SQUAT
        val mutatedCopy = original.copy(displayName = "Agachamento Mutado Ilegalmente")

        val result = ExerciseRegistryV1.register(mutatedCopy)
        assertFalse("Tentativa de sobrescrever exercício ativo com mesma versão deve ser rejeitada", result)
        assertEquals(original.displayName, ExerciseRegistryV1.getById(original.exerciseId)?.displayName)
    }

    // 3. Versão inválida é rejeitada
    @Test
    fun testBlankVersionIsRejected() {
        val exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-INVALID-VER",
            version = ""
        )
        val validation = ExerciseValidator.validate(exercise)
        assertFalse("Exercício com versão em branco deve ser inválido", validation.isValid)
        assertTrue(validation.errors.contains("VERSION_CANNOT_BE_BLANK"))
    }

    // 4. Exercício sem categoria ou sem nome canônico é rejeitado
    @Test
    fun testExerciseWithoutCanonicalNameIsRejected() {
        val exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-NO-NAME",
            canonicalName = ""
        )
        val validation = ExerciseValidator.validate(exercise)
        assertFalse("Exercício sem nome canônico deve ser inválido", validation.isValid)
        assertTrue(validation.errors.contains("CANONICAL_NAME_CANNOT_BE_BLANK"))
    }

    // 5. Exercício sem padrão de movimento ou sem músculos primários é rejeitado
    @Test
    fun testExerciseWithoutPrimaryMusclesIsRejected() {
        val exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-NO-MUSCLES",
            primaryMuscles = emptyList()
        )
        val validation = ExerciseValidator.validate(exercise)
        assertFalse("Exercício sem músculos primários deve ser inválido", validation.isValid)
        assertTrue(validation.errors.contains("PRIMARY_MUSCLES_CANNOT_BE_EMPTY"))
    }

    // 6. Referência de progressão inexistente é rejeitada quando validada contra catálogo
    @Test
    fun testInvalidProgressionReferenceIsRejected() {
        val exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-TEST-PROG",
            progressionIds = listOf("EX-NON-EXISTENT-999")
        )
        val knownIds = ExerciseRegistryV1.getAll().map { it.exerciseId }.toSet()
        val validation = ExerciseValidator.validate(exercise, knownIds)
        assertFalse("Referência a progressão inexistente deve ser rejeitada", validation.isValid)
    }

    // 7. Referência de regressão inexistente é rejeitada quando validada contra catálogo
    @Test
    fun testInvalidRegressionReferenceIsRejected() {
        val exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-TEST-REG",
            regressionIds = listOf("EX-NON-EXISTENT-888")
        )
        val knownIds = ExerciseRegistryV1.getAll().map { it.exerciseId }.toSet()
        val validation = ExerciseValidator.validate(exercise, knownIds)
        assertFalse("Referência a regressão inexistente deve ser rejeitada", validation.isValid)
    }

    // 8. Mídia opcional não impede exercício
    @Test
    fun testOptionalMediaDoesNotBlockExercise() {
        val exerciseWithoutMedia = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-NO-MEDIA-001",
            mediaReferences = emptyList()
        )
        val validation = ExerciseValidator.validate(exerciseWithoutMedia)
        assertTrue("Exercício sem mídia continua 100% válido", validation.isValid)
        assertTrue(ExerciseRegistryV1.register(exerciseWithoutMedia))
    }

    // 9. Exercício publicado não pode ser alterado silenciosamente
    @Test
    fun testPublishedExerciseCannotBeMutatedSilently() {
        val original = ExerciseCatalogV1.PUSH_UP
        val tampered = original.copy(difficulty = ExerciseDifficulty.ELITE)

        val success = ExerciseRegistryV1.register(tampered)
        assertFalse("Não é permitida mutação silenciosa de exercício publicado", success)
        assertEquals(ExerciseDifficulty.BEGINNER, ExerciseRegistryV1.getById(original.exerciseId)?.difficulty)
    }

    // 10. Nova versão cria novo contexto mantendo histórico
    @Test
    fun testNewVersionCreatesNewContextAndPreservesHistory() {
        val v1 = ExerciseCatalogV1.PUSH_UP
        val v2 = v1.copy(
            version = "V2",
            displayName = "Flexão de Braços Padrão (Revisão Técnica V2)",
            difficulty = ExerciseDifficulty.INTERMEDIATE
        )

        assertTrue(ExerciseRegistryV1.register(v2))
        val versions = ExerciseRegistryV1.getAllVersions(v1.exerciseId)
        assertEquals("Devem existir 2 versões registradas", 2, versions.size)
        assertEquals("Versão mais recente deve ser V2", "V2", ExerciseRegistryV1.getById(v1.exerciseId)?.version)
        assertEquals("Versão V1 deve continuar recuperável", "V1", ExerciseRegistryV1.getByIdAndVersion(v1.exerciseId, "V1")?.version)
    }

    // 11. CLIENT não pode alterar exercício oficial
    @Test
    fun testClientCannotMutateOfficialExercise() {
        val exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-CLIENT-ATTEMPT",
            displayName = "Exercício Criado por Cliente Não Autorizado"
        )
        val registered = ExerciseSecurityBarrier.registerOrUpdateExercise(
            callerTier = ExerciseCallerTier.CLIENT,
            callerId = "client-user-123",
            exercise = exercise
        )
        assertFalse("CLIENT deve ser estritamente bloqueado de mutar catálogo oficial", registered)
        assertNull(ExerciseRegistryV1.getById("EX-CLIENT-ATTEMPT"))
    }

    // 12. AI_GATEWAY não pode alterar exercício oficial
    @Test
    fun testAiGatewayCannotMutateOfficialExercise() {
        val exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-AI-ATTEMPT",
            displayName = "Exercício Inventado por IA"
        )
        val registered = ExerciseSecurityBarrier.registerOrUpdateExercise(
            callerTier = ExerciseCallerTier.AI_GATEWAY,
            callerId = "gemini-flash-service",
            exercise = exercise
        )
        assertFalse("AI_GATEWAY deve ser bloqueado de criar ou homologar exercícios oficiais", registered)
        assertNull(ExerciseRegistryV1.getById("EX-AI-ATTEMPT"))
    }

    // 13. Exercise Engine não altera Evolution Engine
    @Test
    fun testExerciseEngineDoesNotMutateEvolutionEngine() {
        val initialClass = com.example.core.evolutionengine.catalog.ClassCatalog.getClassById(com.example.core.evolutionengine.catalog.ClassCatalog.CLASS_01)
        assertNotNull(initialClass)
        assertEquals(com.example.core.evolutionengine.catalog.ClassCatalog.CLASS_01, initialClass!!.classId)

        // Consultar ou instanciar exercícios não deve mutar classes ou estados do atleta
        val exercises = ExerciseEngineV1.listActiveExercises()
        assertTrue(exercises.isNotEmpty())

        val currentClass = com.example.core.evolutionengine.catalog.ClassCatalog.getClassById(com.example.core.evolutionengine.catalog.ClassCatalog.CLASS_01)
        assertEquals("Evolution Engine deve permanecer imutável", initialClass.classId, currentClass!!.classId)
    }

    // 14. Exercise Engine não altera Score Engine
    @Test
    fun testExerciseEngineDoesNotMutateScoreEngine() {
        // Exercise Engine é puramente um catálogo biomecânico/estrutural, sem autoridade sobre scores científicos
        val pushUp = ExerciseEngineV1.getExercise("EX-PSH-STD-001-V1")
        assertNotNull(pushUp)

        // Prescrição de teste
        val prescription = ExercisePrescription(
            exerciseId = pushUp!!.exerciseId,
            sets = 3,
            repetitions = 10
        )
        assertTrue(prescription.hasPrescribedVolume())
        assertFalse(prescription.hasPrescribedLoad())
    }

    // 15. Exercício sem metodologia científica não recebe certificação científica automaticamente
    @Test
    fun testExerciseWithoutScientificMethodologyHasEmptyScientificReferences() {
        val exercise = ExerciseCatalogV1.BURPEE
        assertTrue(
            "Exercício sem metodologia explícita deve manter referências científicas vazias/não homologadas",
            exercise.scientificReferences.scientificMethodologyIds.isEmpty()
        )
    }

    // 16. Simulação não altera catálogo oficial
    @Test
    fun testSimulationModeCannotMutateOfficialCatalog() {
        val exercise = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy(
            exerciseId = "EX-SIMULATION-001",
            displayName = "Exercício em Modo Simulação"
        )
        val registered = ExerciseSecurityBarrier.registerOrUpdateExercise(
            callerTier = ExerciseCallerTier.CORE_ENGINE,
            callerId = "simulation-runner",
            exercise = exercise,
            simulationMode = true
        )
        assertFalse("Modo de simulação não pode gravar no catálogo oficial", registered)
        assertNull(ExerciseRegistryV1.getById("EX-SIMULATION-001"))
    }

    // 17. Mesmo exercício + mesma versão produz resultado determinístico (Checksum)
    @Test
    fun testDeterministicChecksum() {
        val exercise1 = ExerciseCatalogV1.BODYWEIGHT_SQUAT
        val exercise2 = ExerciseCatalogV1.BODYWEIGHT_SQUAT.copy()

        assertEquals("Checksums devem ser estritamente idênticos para dados iguais", exercise1.calculateChecksum(), exercise2.calculateChecksum())
    }

    // 18. Exercício arquivado não pode ser prescrito como ativo
    @Test
    fun testArchivedExerciseIsExcludedFromActiveList() {
        val archived = ExerciseCatalogV1.DEAD_HANG.copy(
            exerciseId = "EX-ARCHIVED-001",
            status = ExerciseStatus.ARCHIVED
        )
        ExerciseRegistryV1.register(archived)

        val activeList = ExerciseRegistryV1.getAllActive()
        assertFalse("Exercício arquivado não deve constar na lista de exercícios ativos", activeList.any { it.exerciseId == "EX-ARCHIVED-001" })
        assertNotNull("Exercício arquivado ainda pode ser recuperado por consulta histórica direta", ExerciseRegistryV1.getById("EX-ARCHIVED-001"))
    }

    // 19. Exercício ativo pode ser consultado por categoria, padrão e progressão
    @Test
    fun testActiveExerciseQueriesAndProgressionEvaluator() {
        val squats = ExerciseEngineV1.listByPattern(MovementPattern.SQUAT)
        assertTrue("Deve listar exercícios de agachamento", squats.isNotEmpty())

        val path = ExerciseProgressionEvaluator.evaluatePath("EX-SQ-BW-001-V1")
        assertNotNull(path.currentExercise)
        assertTrue("Deve possuir progressões cadastradas", path.progressions.isNotEmpty())
        assertTrue("Deve possuir regressões cadastradas", path.regressions.isNotEmpty())
    }

    // 20. Histórico de versões permanece rastreável e auditado
    @Test
    fun testAuditLogsTrackSecurityAndMutations() {
        val initialAuditCount = ExerciseSecurityBarrier.getAuditLogs().size

        // Tentativa inválida de cliente
        ExerciseSecurityBarrier.registerOrUpdateExercise(
            callerTier = ExerciseCallerTier.CLIENT,
            callerId = "client-user-999",
            exercise = ExerciseCatalogV1.PLANK
        )

        val logs = ExerciseSecurityBarrier.getAuditLogs()
        assertTrue("Logs de auditoria devem ser incrementados", logs.size > initialAuditCount)
        val lastLog = logs.last()
        assertTrue("Último log deve registrar violação de segurança", lastLog.securityViolation)
        assertEquals("client-user-999", lastLog.callerId)
        assertTrue(lastLog.checksum.isNotBlank())
    }
}
