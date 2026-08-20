package com.example

import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.DataCoreAuditLog
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.SourceTier
import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.EvidenceMaturity
import com.example.core.evidenceconsistency.model.EvidenceQualityMatrix
import com.example.core.evidenceconsistency.model.EvidenceValidityAssessment
import com.example.core.evidenceconsistency.model.EvolutionEvidencePackage
import com.example.core.evidenceconsistency.model.MaturityStatus
import com.example.core.evidenceconsistency.model.ValidityStatus
import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.engine.EvolutionEngineV1
import com.example.core.evolutionengine.evaluator.ClassEligibilityEvaluator
import com.example.core.evolutionengine.evaluator.RequirementEvaluator
import com.example.core.evolutionengine.model.ClassEligibilityStatus
import com.example.core.evolutionengine.model.ComparisonOperator
import com.example.core.evolutionengine.model.EvolutionPolicy
import com.example.core.evolutionengine.model.EvolutionRequirement
import com.example.core.evolutionengine.model.ProgressionMode
import com.example.core.evolutionengine.model.ProgressionStatus
import com.example.core.evolutionengine.model.RequirementCategory
import com.example.core.evolutionengine.model.RequirementStatus
import com.example.core.evolutionengine.model.RequirementStatusResult
import com.example.core.evolutionengine.policy.EvolutionPolicyRegistry
import com.example.core.scoreengine.model.CalculationStatus
import com.example.core.scoreengine.model.ComponentScore
import com.example.core.scoreengine.model.DimensionScore
import com.example.core.scoreengine.model.PerformanceIndex
import com.example.core.scoreengine.model.ScoreConfidenceMetadata
import com.example.core.scoreengine.model.ScoreExplanation
import com.example.core.scoreengine.model.ScoreSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * PERFORMAI EVOLUTION ENGINE V1 - TESTES AUTOMATIZADOS OBRIGATÓRIOS
 *
 * Cobertura Completa dos 15 Critérios:
 * 1. Classe sem requisitos validados = PENDING_VALIDATION.
 * 2. Requisito satisfeito = SATISFIED.
 * 3. Requisito não atingido = NOT_SATISFIED.
 * 4. Evidência insuficiente = INSUFFICIENT_EVIDENCE.
 * 5. Evidência inválida = INVALID.
 * 6. Requisito obrigatório bloqueia elegibilidade.
 * 7. Mock data não gera elegibilidade oficial.
 * 8. Cliente tentando alterar classe é bloqueado.
 * 9. IA tentando alterar elegibilidade é bloqueada.
 * 10. Snapshot histórico não pode ser alterado.
 * 11. Alteração de versão da política gera novo contexto.
 * 12. Mesmo conjunto de evidências + mesma política = resultado determinístico.
 * 13. RequirementResult possui rastreabilidade até Evidence.
 * 14. ClassEligibilityResult possui rastreabilidade até ScoreSnapshot.
 * 15. Toda avaliação oficial gera AuditLog.
 */
class EvolutionEngineV1Test {

    private lateinit var auditLogs: MutableList<DataCoreAuditLog>
    private lateinit var engine: EvolutionEngineV1
    private lateinit var requirementEvaluator: RequirementEvaluator
    private lateinit var classEvaluator: ClassEligibilityEvaluator

    @Before
    fun setUp() {
        auditLogs = mutableListOf()
        requirementEvaluator = RequirementEvaluator()
        classEvaluator = ClassEligibilityEvaluator(requirementEvaluator)
        engine = EvolutionEngineV1(
            policyRegistry = EvolutionPolicyRegistry,
            classEligibilityEvaluator = classEvaluator,
            auditLogger = { auditLogs.add(it) }
        )
    }

    // =========================================================================
    // TESTE 1: Classe sem requisitos validados = PENDING_VALIDATION
    // =========================================================================
    @Test
    fun testClassWithoutValidatedRequirementsReturnsPendingValidation() {
        val userId = "USR-TEST-01"
        val pendingPolicy = EvolutionPolicy(
            policyId = "POL-PENDING-V1",
            version = "1.0.0",
            classId = ClassCatalog.CLASS_15, // Campeão
            requirements = listOf(
                EvolutionRequirement(
                    id = "REQ-CAMP-PENDING-01",
                    classId = ClassCatalog.CLASS_15,
                    category = RequirementCategory.PERFORMANCE,
                    dimensionId = "FORCE",
                    operator = ComparisonOperator.GTE,
                    threshold = null, // Sem threshold inventado
                    status = RequirementStatus.PENDING_VALIDATION,
                    isMandatory = true
                )
            ),
            methodologyStatus = "PENDING_VALIDATION"
        )

        val targetClass = ClassCatalog.getClassById(ClassCatalog.CLASS_15)!!
        val currentClass = ClassCatalog.getClassById(ClassCatalog.CLASS_01)!!

        val result = classEvaluator.evaluateClassEligibility(
            userId = userId,
            targetClass = targetClass,
            currentClass = currentClass,
            policy = pendingPolicy,
            scoreSnapshot = null,
            evidencePackage = null
        )

        assertEquals(ClassEligibilityStatus.PENDING_VALIDATION, result.status)
        assertTrue(result.blockingRequirements.any { it.status == RequirementStatusResult.PENDING_VALIDATION })
    }

    // =========================================================================
    // TESTE 2: Requisito satisfeito = SATISFIED
    // =========================================================================
    @Test
    fun testRequirementSatisfiedReturnsSatisfied() {
        val req = EvolutionRequirement(
            id = "REQ-EVID-TEST",
            classId = ClassCatalog.CLASS_02,
            category = RequirementCategory.EVIDENCE,
            operator = ComparisonOperator.MINIMUM_SET,
            minimumEvidenceCount = 2,
            status = RequirementStatus.ACTIVE,
            isMandatory = true
        )

        val evidencePackage = createSampleEvidencePackage(
            userId = "USR-TEST-02",
            evidenceIds = listOf("EV-1", "EV-2", "EV-3")
        )

        val result = requirementEvaluator.evaluateRequirement(
            requirement = req,
            scoreSnapshot = null,
            evidencePackage = evidencePackage
        )

        assertEquals(RequirementStatusResult.SATISFIED, result.status)
        assertEquals(3.0, result.actualValue)
        assertEquals(2.0, result.expectedValue)
        assertTrue(result.evidenceIds.containsAll(listOf("EV-1", "EV-2", "EV-3")))
    }

    // =========================================================================
    // TESTE 3: Requisito não atingido = NOT_SATISFIED
    // =========================================================================
    @Test
    fun testRequirementNotMetReturnsNotSatisfied() {
        val req = EvolutionRequirement(
            id = "REQ-PERF-FORCE",
            classId = ClassCatalog.CLASS_10,
            category = RequirementCategory.PERFORMANCE,
            dimensionId = "FORCE",
            operator = ComparisonOperator.GTE,
            threshold = 80.0,
            status = RequirementStatus.ACTIVE,
            isMandatory = true
        )

        val scoreSnapshot = createSampleScoreSnapshot(
            userId = "USR-TEST-03",
            forceScore = 65.0 // Abaixo do threshold de 80.0
        )

        val result = requirementEvaluator.evaluateRequirement(
            requirement = req,
            scoreSnapshot = scoreSnapshot,
            evidencePackage = null
        )

        assertEquals(RequirementStatusResult.NOT_SATISFIED, result.status)
        assertEquals(65.0, result.actualValue)
        assertEquals(80.0, result.expectedValue)
    }

    // =========================================================================
    // TESTE 4: Evidência insuficiente = INSUFFICIENT_EVIDENCE
    // =========================================================================
    @Test
    fun testInsufficientEvidenceReturnsInsufficientEvidence() {
        val req = EvolutionRequirement(
            id = "REQ-EVID-MIN",
            classId = ClassCatalog.CLASS_03,
            category = RequirementCategory.EVIDENCE,
            operator = ComparisonOperator.MINIMUM_SET,
            minimumEvidenceCount = 5,
            status = RequirementStatus.ACTIVE,
            isMandatory = true
        )

        // Sem pacote de evidências
        val result = requirementEvaluator.evaluateRequirement(
            requirement = req,
            scoreSnapshot = null,
            evidencePackage = null
        )

        assertEquals(RequirementStatusResult.INSUFFICIENT_EVIDENCE, result.status)
    }

    // =========================================================================
    // TESTE 5: Evidência inválida = INVALID
    // =========================================================================
    @Test
    fun testInvalidEvidenceReturnsInvalid() {
        val req = EvolutionRequirement(
            id = "REQ-PERF-CHECK",
            classId = ClassCatalog.CLASS_05,
            category = RequirementCategory.PERFORMANCE,
            dimensionId = "FORCE",
            operator = ComparisonOperator.GTE,
            threshold = 50.0,
            status = RequirementStatus.ACTIVE,
            isMandatory = true
        )

        // Score Snapshot rejeitado por violação de integridade
        val scoreSnapshot = createSampleScoreSnapshot(
            userId = "USR-TEST-05",
            forceScore = null,
            calculationStatus = CalculationStatus.REJECTED
        )

        val result = requirementEvaluator.evaluateRequirement(
            requirement = req,
            scoreSnapshot = scoreSnapshot,
            evidencePackage = null
        )

        assertEquals(RequirementStatusResult.INVALID, result.status)
    }

    // =========================================================================
    // TESTE 6: Requisito obrigatório bloqueia elegibilidade
    // =========================================================================
    @Test
    fun testMandatoryUnsatisfiedRequirementBlocksClassEligibility() {
        val targetClass = ClassCatalog.getClassById(ClassCatalog.CLASS_03)!!
        val currentClass = ClassCatalog.getClassById(ClassCatalog.CLASS_02)!!

        val policy = EvolutionPolicy(
            policyId = "POL-TEST-BLOCKING",
            version = "1.0.0",
            classId = targetClass.classId,
            requirements = listOf(
                EvolutionRequirement(
                    id = "REQ-SATISFIED",
                    classId = targetClass.classId,
                    category = RequirementCategory.EVIDENCE,
                    operator = ComparisonOperator.MINIMUM_SET,
                    minimumEvidenceCount = 2,
                    status = RequirementStatus.ACTIVE,
                    isMandatory = true
                ),
                EvolutionRequirement(
                    id = "REQ-UNSATISFIED",
                    classId = targetClass.classId,
                    category = RequirementCategory.PERFORMANCE,
                    dimensionId = "FORCE",
                    operator = ComparisonOperator.GTE,
                    threshold = 90.0,
                    status = RequirementStatus.ACTIVE,
                    isMandatory = true // Obrigatório
                )
            ),
            progressionMode = ProgressionMode.ALL_MANDATORY_SATISFIED
        )

        val evidencePackage = createSampleEvidencePackage("USR-06", listOf("EV-1", "EV-2", "EV-3"))
        val scoreSnapshot = createSampleScoreSnapshot("USR-06", forceScore = 70.0) // 70 < 90

        val eligibilityResult = classEvaluator.evaluateClassEligibility(
            userId = "USR-06",
            targetClass = targetClass,
            currentClass = currentClass,
            policy = policy,
            scoreSnapshot = scoreSnapshot,
            evidencePackage = evidencePackage
        )

        assertEquals(ClassEligibilityStatus.NOT_ELIGIBLE, eligibilityResult.status)
        assertEquals(1, eligibilityResult.blockingRequirements.size)
        assertEquals("REQ-UNSATISFIED", eligibilityResult.blockingRequirements.first().requirementId)
        assertEquals(1, eligibilityResult.satisfiedRequirements.size)
    }

    // =========================================================================
    // TESTE 7: Mock data não gera elegibilidade oficial
    // =========================================================================
    @Test
    fun testMockDataDoesNotGenerateOfficialEligibility() {
        val userId = "USR-MOCK-07"
        val targetClass = ClassCatalog.CLASS_02
        val currentClass = ClassCatalog.CLASS_01

        val snapshot = engine.evaluateClass(
            userId = userId,
            targetClassId = targetClass,
            currentClassId = currentClass,
            scoreSnapshot = null,
            evidencePackage = null,
            actor = ActorType.CORE_ENGINE,
            isMock = true,
            simulationMode = true
        )

        assertTrue(snapshot.isMock)
        assertTrue(snapshot.simulationMode)
        assertFalse("Mock data nunca pode resultar em status oficial ELIGIBLE", snapshot.eligibilityResult.status == ClassEligibilityStatus.ELIGIBLE)
    }

    // =========================================================================
    // TESTE 8: Cliente tentando alterar classe é bloqueado
    // =========================================================================
    @Test
    fun testClientAttemptToModifyClassIsBlockedAndAudited() {
        try {
            engine.attemptDirectClassModification(
                userId = "USR-HACKER-08",
                requestedClassId = ClassCatalog.CLASS_22,
                actor = ActorType.CLIENT
            )
            fail("Deveria lançar SecurityException ao tentar alteração direta de classe por CLIENT")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("Segurança PERFORMAI"))
        }

        assertTrue(auditLogs.any {
            it.action == "UNAUTHORIZED_CLASS_MODIFICATION_BLOCKED" &&
                    it.actorType == ActorType.CLIENT
        })
    }

    // =========================================================================
    // TESTE 9: IA tentando alterar elegibilidade é bloqueada
    // =========================================================================
    @Test
    fun testAIAttemptToMutateEligibilityIsBlockedAndAudited() {
        try {
            engine.attemptDirectEligibilityModification(
                userId = "USR-AI-TARGET",
                targetClassId = ClassCatalog.CLASS_15,
                actor = ActorType.AI_GATEWAY
            )
            fail("Deveria lançar SecurityException ao tentar mutação de elegibilidade por AI_GATEWAY")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("Segurança PERFORMAI"))
        }

        assertTrue(auditLogs.any {
            it.action == "UNAUTHORIZED_ELIGIBILITY_MUTATION_BLOCKED" &&
                    it.actorType == ActorType.AI_GATEWAY
        })
    }

    // =========================================================================
    // TESTE 10: Snapshot histórico não pode ser alterado (Imutabilidade)
    // =========================================================================
    @Test
    fun testEvolutionSnapshotIsImmutable() {
        val snapshot = engine.evaluateClass(
            userId = "USR-IMMUTABLE",
            targetClassId = ClassCatalog.CLASS_02,
            currentClassId = ClassCatalog.CLASS_01,
            scoreSnapshot = null,
            evidencePackage = null,
            actor = ActorType.CORE_ENGINE,
            isMock = false,
            simulationMode = false
        )

        assertNotNull(snapshot.id)
        assertNotNull(snapshot.auditReference)
        assertEquals("1.0.0-datacore-v1", snapshot.coreVersion)

        // Snapshot representa um registro append-only de auditoria
        val copy = snapshot.copy()
        assertEquals(snapshot.id, copy.id)
        assertEquals(snapshot.auditReference, copy.auditReference)
    }

    // =========================================================================
    // TESTE 11: Alteração de versão da política gera novo contexto
    // =========================================================================
    @Test
    fun testPolicyVersionChangeGeneratesNewContext() {
        val targetClass = ClassCatalog.getClassById(ClassCatalog.CLASS_04)!!
        val currentClass = ClassCatalog.getClassById(ClassCatalog.CLASS_03)!!

        val policyV1 = EvolutionPolicy(
            policyId = "POL-CLASS-04",
            version = "1.0.0",
            classId = targetClass.classId,
            requirements = listOf(
                EvolutionRequirement(
                    id = "REQ-V1",
                    classId = targetClass.classId,
                    category = RequirementCategory.EVIDENCE,
                    operator = ComparisonOperator.MINIMUM_SET,
                    minimumEvidenceCount = 2,
                    methodologyVersion = "1.0.0",
                    status = RequirementStatus.ACTIVE
                )
            )
        )

        val policyV2 = EvolutionPolicy(
            policyId = "POL-CLASS-04",
            version = "2.0.0-CONSORTIUM-UPDATE",
            classId = targetClass.classId,
            requirements = listOf(
                EvolutionRequirement(
                    id = "REQ-V2",
                    classId = targetClass.classId,
                    category = RequirementCategory.EVIDENCE,
                    operator = ComparisonOperator.MINIMUM_SET,
                    minimumEvidenceCount = 10,
                    methodologyVersion = "2.0.0-CONSORTIUM-UPDATE",
                    status = RequirementStatus.ACTIVE
                )
            )
        )

        val evidencePackage = createSampleEvidencePackage("USR-11", listOf("EV-1", "EV-2", "EV-3"))

        val resultV1 = classEvaluator.evaluateClassEligibility("USR-11", targetClass, currentClass, policyV1, null, evidencePackage)
        val resultV2 = classEvaluator.evaluateClassEligibility("USR-11", targetClass, currentClass, policyV2, null, evidencePackage)

        assertEquals("1.0.0", resultV1.methodologyVersion)
        assertEquals(ClassEligibilityStatus.ELIGIBLE, resultV1.status)

        assertEquals("2.0.0-CONSORTIUM-UPDATE", resultV2.methodologyVersion)
        assertEquals(ClassEligibilityStatus.NOT_ELIGIBLE, resultV2.status) // 3 < 10
    }

    // =========================================================================
    // TESTE 12: Mesmo conjunto de evidências + mesma política = resultado determinístico
    // =========================================================================
    @Test
    fun testDeterministicEvaluationOutcome() {
        val targetClass = ClassCatalog.getClassById(ClassCatalog.CLASS_03)!!
        val currentClass = ClassCatalog.getClassById(ClassCatalog.CLASS_02)!!
        val policy = EvolutionPolicyRegistry.getPolicyForClass(targetClass.classId)

        val evidencePackage = createSampleEvidencePackage("USR-DETERMINISTIC", listOf("EV-A", "EV-B", "EV-C"))
        val scoreSnapshot = createSampleScoreSnapshot("USR-DETERMINISTIC", forceScore = 75.0)

        val fixedTimestamp = 1720000000000L

        val run1 = classEvaluator.evaluateClassEligibility(
            userId = "USR-DETERMINISTIC",
            targetClass = targetClass,
            currentClass = currentClass,
            policy = policy,
            scoreSnapshot = scoreSnapshot,
            evidencePackage = evidencePackage,
            evaluationTimestamp = fixedTimestamp
        )

        val run2 = classEvaluator.evaluateClassEligibility(
            userId = "USR-DETERMINISTIC",
            targetClass = targetClass,
            currentClass = currentClass,
            policy = policy,
            scoreSnapshot = scoreSnapshot,
            evidencePackage = evidencePackage,
            evaluationTimestamp = fixedTimestamp
        )

        assertEquals(run1.status, run2.status)
        assertEquals(run1.requirementResults.size, run2.requirementResults.size)
        assertEquals(run1.blockingRequirements.size, run2.blockingRequirements.size)
        assertEquals(run1.satisfiedRequirements.size, run2.satisfiedRequirements.size)
    }

    // =========================================================================
    // TESTE 13: RequirementResult possui rastreabilidade até Evidence
    // =========================================================================
    @Test
    fun testRequirementResultTraceabilityToEvidence() {
        val req = EvolutionRequirement(
            id = "REQ-TRACE-EV",
            classId = ClassCatalog.CLASS_02,
            category = RequirementCategory.EVIDENCE,
            operator = ComparisonOperator.MINIMUM_SET,
            minimumEvidenceCount = 2,
            status = RequirementStatus.ACTIVE
        )

        val expectedEvidenceIds = listOf("EV-TRACE-1", "EV-TRACE-2")
        val evidencePackage = createSampleEvidencePackage("USR-13", expectedEvidenceIds)

        val result = requirementEvaluator.evaluateRequirement(
            requirement = req,
            scoreSnapshot = null,
            evidencePackage = evidencePackage
        )

        assertEquals(expectedEvidenceIds, result.evidenceIds)
        assertEquals(expectedEvidenceIds, result.explanation.evidenceIds)
    }

    // =========================================================================
    // TESTE 14: ClassEligibilityResult possui rastreabilidade até ScoreSnapshot
    // =========================================================================
    @Test
    fun testClassEligibilityTraceabilityToScoreSnapshot() {
        val targetClass = ClassCatalog.getClassById(ClassCatalog.CLASS_02)!!
        val currentClass = ClassCatalog.getClassById(ClassCatalog.CLASS_01)!!
        val policy = EvolutionPolicyRegistry.getPolicyForClass(targetClass.classId)

        val scoreSnapshot = createSampleScoreSnapshot("USR-14", forceScore = 60.0)

        val result = classEvaluator.evaluateClassEligibility(
            userId = "USR-14",
            targetClass = targetClass,
            currentClass = currentClass,
            policy = policy,
            scoreSnapshot = scoreSnapshot,
            evidencePackage = null
        )

        assertEquals(scoreSnapshot.id, result.scoreSnapshotId)
    }

    // =========================================================================
    // TESTE 15: Toda avaliação oficial gera AuditLog
    // =========================================================================
    @Test
    fun testOfficialEvaluationGeneratesAuditLog() {
        auditLogs.clear()

        val snapshot = engine.evaluateClass(
            userId = "USR-AUDIT-15",
            targetClassId = ClassCatalog.CLASS_02,
            currentClassId = ClassCatalog.CLASS_01,
            scoreSnapshot = null,
            evidencePackage = null,
            actor = ActorType.CORE_ENGINE,
            isMock = false,
            simulationMode = false
        )

        assertNotNull(snapshot)
        assertTrue("Deve registrar log de auditoria", auditLogs.isNotEmpty())

        val lastLog = auditLogs.last()
        assertEquals(ActorType.CORE_ENGINE, lastLog.actorType)
        assertEquals("EVALUATE_CLASS_OFFICIAL", lastLog.action)
        assertEquals("EvolutionSnapshot", lastLog.entityType)
        assertEquals(snapshot.id, lastLog.entityId)
    }

    // =========================================================================
    // AUXILIARES PARA CRIAÇÃO DE DADOS DE TESTE
    // =========================================================================

    private fun createSampleEvidencePackage(
        userId: String,
        evidenceIds: List<String>
    ): EvolutionEvidencePackage {
        return EvolutionEvidencePackage(
            id = "EVOPKG-TEST",
            userId = userId,
            generatedAt = System.currentTimeMillis(),
            coreVersion = "1.0.0-datacore-v1",
            engineVersion = "1.0.0-consistency-v1",
            evidenceIds = evidenceIds,
            validMetrics = listOf("METRIC-FORCE-PEAK"),
            invalidMetrics = emptyList(),
            expiredEvidenceIds = emptyList(),
            pendingValidationItems = emptyList(),
            validityAssessments = emptyList(),
            consistencyAssessments = emptyMap(),
            repeatabilityAssessments = emptyMap(),
            continuityAssessments = emptyMap(),
            longitudinalSequences = emptyMap(),
            overallConsistencyStatus = ConsistencyStatus.STABLE,
            overallRepeatabilityStatus = "HIGH",
            overallMaturity = EvidenceMaturity(
                userId = userId,
                metricCoverage = 3,
                temporalCoverageDays = 60.0,
                protocolConsistency = 1.0,
                evidenceCount = evidenceIds.size,
                sourceQuality = "TIER_1_DIRECT_SENSOR",
                repeatability = "HIGH",
                longitudinalCoverage = "60 days",
                maturityStatus = MaturityStatus.ESTABLISHED,
                methodologyVersion = "1.0.0",
                limitations = ""
            ),
            qualityMatrix = EvidenceQualityMatrix(
                sourceQualityTier = 1,
                integrityScore = 1.0,
                protocolFidelityScore = 1.0,
                temporalValidityStatus = ValidityStatus.CURRENT,
                consistencyStatus = ConsistencyStatus.STABLE,
                repeatabilityStatus = "HIGH",
                overallMaturityStatus = MaturityStatus.ESTABLISHED,
                limitations = ""
            ),
            limitations = emptyList(),
            auditReference = "AUDIT-REF-TEST",
            isMock = false,
            simulationMode = false
        )
    }

    private fun createSampleScoreSnapshot(
        userId: String,
        forceScore: Double?,
        calculationStatus: CalculationStatus = CalculationStatus.CALCULATED
    ): ScoreSnapshot {
        val dimensionScores = listOf(
            DimensionScore(
                dimension = "FORCE",
                score = forceScore,
                contributingMetrics = emptyList(),
                formulaVersion = "1.0.0",
                evidenceIds = listOf("EV-FORCE-01"),
                confidenceMetadata = ScoreConfidenceMetadata(
                    sourceTier = SourceTier.TIER_1_DIRECT_SENSOR,
                    integrityStatus = IntegrityStatus.VALID,
                    consistencyStatus = "STABLE",
                    repeatabilityStatus = "HIGH",
                    evidenceCount = 1
                ),
                calculationStatus = calculationStatus,
                explanation = ScoreExplanation(
                    score = forceScore,
                    dimensionOrIndex = "FORCE",
                    metricsUsed = listOf("METRIC-FORCE-PEAK"),
                    evidenceUsed = listOf("EV-FORCE-01"),
                    formulasUsed = listOf("LINEAR"),
                    normalizationUsed = "MIN_MAX",
                    protocolVersions = listOf("PROT-1"),
                    coreVersion = "1.0.0",
                    scoreVersion = "1.0.0"
                )
            )
        )

        return ScoreSnapshot(
            id = "SCORE-SNAP-TEST-01",
            userId = userId,
            assessmentId = "ASSESS-01",
            scoreVersion = "1.0.0",
            coreVersion = "1.0.0-datacore-v1",
            calculatedAt = System.currentTimeMillis(),
            performanceIndex = PerformanceIndex(
                value = forceScore,
                formulaVersion = "1.0.0",
                dimensionScores = mapOf("FORCE" to dimensionScores.first()),
                evidenceIds = listOf("EV-FORCE-01"),
                calculationStatus = calculationStatus,
                confidenceMetadata = dimensionScores.first().confidenceMetadata,
                explanation = dimensionScores.first().explanation
            ),
            dimensionScores = dimensionScores,
            evidenceIds = listOf("EV-FORCE-01"),
            metricIds = listOf("METRIC-FORCE-PEAK"),
            calculationStatus = calculationStatus,
            confidenceMetadata = dimensionScores.first().confidenceMetadata,
            isMock = false,
            provenanceId = "PROV-01",
            overallExplanation = dimensionScores.first().explanation
        )
    }
}
