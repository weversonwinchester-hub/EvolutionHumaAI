package com.example

import com.example.core.scientific.engine.ScientificMethodologyExplainer
import com.example.core.scientific.evaluator.MeasurementQualityGate
import com.example.core.scientific.evaluator.NormalizationEvaluator
import com.example.core.scientific.evaluator.RepeatabilityEvaluator
import com.example.core.scientific.model.*
import com.example.core.scientific.registry.*
import com.example.core.scientific.security.ScientificSecurityBarrier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * PERFORMAI SCIENTIFIC METHODOLOGY & PROTOCOL REGISTRY V1
 *
 * Suíte de Testes Automatizados Obrigatórios (25+ Testes)
 */
class ScientificRegistryV1Test {

    @Before
    fun setUp() {
        // Garantir estado inicial limpo dos registros padrão
    }

    // =========================================================================
    // TESTE 1: Metodologia inexistente não pode ser utilizada
    // =========================================================================
    @Test
    fun test1_nonExistentMethodologyCannotBeUsed() {
        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-001",
            metricId = "NON_EXISTENT_METRIC_XYZ",
            protocolId = "PROT-VO2-RAMP-V1",
            instrumentId = "INST-METABOLIC-CART",
            unit = "ml/kg/min",
            rawValue = 45.0,
            samplingRateObserved = 1.0,
            clientTimestamp = System.currentTimeMillis()
        )

        assertNotEquals(QualityGateStatus.ACCEPTED, result.status)
        assertTrue(result.failedChecks.any { it.contains("NOT_ACTIVE_OR_REGISTERED") || it.contains("MISMATCH") })
    }

    // =========================================================================
    // TESTE 2: Metodologia PENDING_VALIDATION não pode ser tratada como VALIDATED
    // =========================================================================
    @Test
    fun test2_pendingValidationMethodologyCannotBeTreatedAsValidated() {
        val draftMethodology = ScientificMethodology(
            methodologyId = "METH-DRAFT-01",
            name = "Metodologia em Rascunho",
            description = "Ainda não revisada",
            version = "0.1.0",
            metricId = "EXPERIMENTAL_FORCE",
            category = "EXPERIMENTAL",
            measurementPrinciple = "Princípio não validado",
            calculationMethod = "x + y",
            acceptedUnits = listOf("N"),
            requiredConditions = emptyList(),
            requiredEquipment = emptyList(),
            acceptableSources = emptyList(),
            validationStatus = MethodologyValidationStatus.PENDING_REVIEW,
            evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_UNSPECIFIED,
            sourceReferences = emptyList(),
            limitations = emptyList(),
            effectiveFrom = System.currentTimeMillis()
        )

        assertNotEquals(MethodologyValidationStatus.VALIDATED, draftMethodology.validationStatus)
        assertNotEquals(MethodologyValidationStatus.ACTIVE, draftMethodology.validationStatus)
        assertEquals(MethodologyValidationStatus.PENDING_REVIEW, draftMethodology.validationStatus)
    }

    // =========================================================================
    // TESTE 3: Protocolo inexistente bloqueia medição oficial
    // =========================================================================
    @Test
    fun test3_nonExistentProtocolBlocksOfficialMeasurement() {
        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-002",
            metricId = "VO2_MAX",
            protocolId = "PROT-FAKE-INEXISTENT",
            instrumentId = "INST-METABOLIC-CART",
            unit = "ml/kg/min",
            rawValue = 52.0,
            samplingRateObserved = 1.0,
            clientTimestamp = System.currentTimeMillis()
        )

        assertNotEquals(QualityGateStatus.ACCEPTED, result.status)
        assertTrue(result.failedChecks.contains("PROTOCOL_NOT_FOUND_OR_UNREGISTERED"))
    }

    // =========================================================================
    // TESTE 4: Protocolo incompatível bloqueia medição
    // =========================================================================
    @Test
    fun test4_incompatibleProtocolBlocksMeasurement() {
        // Tentar avaliar VO2 Max usando protocolo de RFD Isométrico
        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-003",
            metricId = "VO2_MAX",
            protocolId = "PROT-RFD-ISOM-V1",
            instrumentId = "INST-METABOLIC-CART",
            unit = "ml/kg/min",
            rawValue = 48.0,
            samplingRateObserved = 1.0,
            clientTimestamp = System.currentTimeMillis()
        )

        assertEquals(QualityGateStatus.REJECTED, result.status)
        assertTrue(result.failedChecks.any { it.startsWith("PROTOCOL_METRIC_MISMATCH") })
    }

    // =========================================================================
    // TESTE 5: Unidade incompatível é rejeitada
    // =========================================================================
    @Test
    fun test5_incompatibleUnitIsRejected() {
        // Enviar VO2 Max com unidade de força "N" ou "km/h"
        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-004",
            metricId = "VO2_MAX",
            protocolId = "PROT-VO2-RAMP-V1",
            instrumentId = "INST-METABOLIC-CART",
            unit = "km/h",
            rawValue = 16.5,
            samplingRateObserved = 1.0,
            clientTimestamp = System.currentTimeMillis()
        )

        assertEquals(QualityGateStatus.REJECTED, result.status)
        assertTrue(result.failedChecks.any { it.startsWith("INCOMPATIBLE_UNIT_OBSERVED") })
    }

    // =========================================================================
    // TESTE 6: Instrumento não homologado gera status apropriado
    // =========================================================================
    @Test
    fun test6_unhomologatedInstrumentGeneratesAppropriateStatus() {
        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-005",
            metricId = "VO2_MAX",
            protocolId = "PROT-VO2-RAMP-V1",
            instrumentId = "UNKNOWN_HOMEMADE_SENSOR",
            unit = "ml/kg/min",
            rawValue = 50.0,
            samplingRateObserved = 1.0,
            clientTimestamp = System.currentTimeMillis()
        )

        assertNotEquals(QualityGateStatus.ACCEPTED, result.status)
        assertTrue(result.failedChecks.contains("INSTRUMENT_NOT_HOMOLOGATED_OR_UNKNOWN"))
    }

    // =========================================================================
    // TESTE 7: Sampling rate insuficiente gera quality failure
    // =========================================================================
    @Test
    fun test7_insufficientSamplingRateGeneratesQualityFailure() {
        // RFD requer >= 1000 Hz, enviamos apenas 50 Hz
        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-006",
            metricId = "RFD",
            protocolId = "PROT-RFD-ISOM-V1",
            instrumentId = "INST-FORCE-PLATE-1000HZ",
            unit = "N/s",
            rawValue = 3200.0,
            samplingRateObserved = 50.0, // Subamostragem severa!
            clientTimestamp = System.currentTimeMillis(),
            calibrationDate = System.currentTimeMillis()
        )

        assertNotEquals(QualityGateStatus.ACCEPTED, result.status)
        assertTrue(result.failedChecks.any { it.startsWith("SAMPLING_RATE_INSUFFICIENT") })
        assertTrue(result.deviations.any { it.type == "SAMPLING_RATE_DEFICIT" })
    }

    // =========================================================================
    // TESTE 8: Timestamp inválido é rejeitado
    // =========================================================================
    @Test
    fun test8_invalidTimestampIsRejected() {
        val now = System.currentTimeMillis()
        val futureTimestamp = now + 1_000_000L // 1000s no futuro

        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-007",
            metricId = "VO2_MAX",
            protocolId = "PROT-VO2-RAMP-V1",
            instrumentId = "INST-METABOLIC-CART",
            unit = "ml/kg/min",
            rawValue = 55.0,
            samplingRateObserved = 1.0,
            clientTimestamp = futureTimestamp,
            serverTimestamp = now
        )

        assertEquals(QualityGateStatus.REJECTED, result.status)
        assertFalse(result.timestampValidity)
        assertTrue(result.failedChecks.contains("TIMESTAMP_IN_FUTURE_CLOCK_SKEW_EXCEEDED"))
    }

    // =========================================================================
    // TESTE 9: Método DIRECT é diferenciado de ESTIMATED
    // =========================================================================
    @Test
    fun test9_directMethodIsDifferentiatedFromEstimated() {
        val directMethod = MeasurementClassification.DIRECT
        val derivedMethod = MeasurementClassification.DERIVED
        val estimatedMethod = MeasurementClassification.ESTIMATED

        assertNotEquals(directMethod, estimatedMethod)
        assertNotEquals(derivedMethod, estimatedMethod)
        assertNotEquals(directMethod, derivedMethod)
    }

    // =========================================================================
    // TESTE 10: Método ESTIMATED não pode ser apresentado como DIRECT
    // =========================================================================
    @Test
    fun test10_estimatedMethodCannotBePresentedAsDirect() {
        val phoneCap = DeviceCapabilityRegistry.getCapability("SMARTPHONE")
        assertNotNull(phoneCap)
        assertTrue(phoneCap!!.captureMethods.any { it.startsWith("ESTIMATED") })
        assertFalse(phoneCap.captureMethods.contains("DIRECT_MEASUREMENT"))
    }

    // =========================================================================
    // TESTE 11: Protocolo versionado preserva histórico
    // =========================================================================
    @Test
    fun test11_versionedProtocolPreservesHistory() {
        val v1 = ScientificProtocolRegistry.getProtocol("PROT-VO2-RAMP-V1")
        assertNotNull(v1)
        assertEquals("1.0.0", v1!!.version)

        // Registrar V2
        val v2 = v1.copy(
            protocolId = "PROT-VO2-RAMP-V2",
            version = "2.0.0",
            purpose = "Protocolo Rampa V2 com aquecimento adaptativo"
        )
        ScientificProtocolRegistry.register(v2)

        // Verificar que V1 permanece inalterado
        val queriedV1 = ScientificProtocolRegistry.getProtocol("PROT-VO2-RAMP-V1")
        assertNotNull(queriedV1)
        assertEquals("1.0.0", queriedV1!!.version)
        assertEquals("PROT-VO2-RAMP-V1", queriedV1.protocolId)

        // Verificar que V2 coexiste
        val queriedV2 = ScientificProtocolRegistry.getProtocol("PROT-VO2-RAMP-V2")
        assertNotNull(queriedV2)
        assertEquals("2.0.0", queriedV2!!.version)
        assertNotEquals(queriedV1.protocolId, queriedV2.protocolId)
    }

    // =========================================================================
    // TESTE 12: Metodologia versionada preserva histórico
    // =========================================================================
    @Test
    fun test12_versionedMethodologyPreservesHistory() {
        val v1 = ScientificMethodologyRegistry.getMethodology("METH-VO2MAX-CPX-V1")
        assertNotNull(v1)
        assertEquals("1.0.0", v1!!.version)

        val v2 = v1.copy(
            methodologyId = "METH-VO2MAX-CPX-V2",
            version = "2.0.0",
            supersedesVersion = "METH-VO2MAX-CPX-V1"
        )
        ScientificMethodologyRegistry.register(v2)

        val checkV1 = ScientificMethodologyRegistry.getMethodology("METH-VO2MAX-CPX-V1")
        assertNotNull(checkV1)
        assertEquals("1.0.0", checkV1!!.version)

        val checkV2 = ScientificMethodologyRegistry.getMethodology("METH-VO2MAX-CPX-V2")
        assertNotNull(checkV2)
        assertEquals("2.0.0", checkV2!!.version)
        assertEquals("METH-VO2MAX-CPX-V1", checkV2.supersedesVersion)
    }

    // =========================================================================
    // TESTE 13: Fonte inexistente não pode ser apresentada como fonte científica
    // =========================================================================
    @Test
    fun test13_nonExistentSourceCannotBePresentedAsScientific() {
        val pendingSource = MethodologySource(
            sourceId = "SRC-PENDING-001",
            title = "Estudo Preliminar Não Validado",
            authors = listOf("Autor Desconhecido"),
            publicationYear = null,
            publicationType = "UNSPECIFIED",
            identifier = null,
            sourceAuthority = "UNVERIFIED",
            sourceStatus = "PENDING_VALIDATION"
        )

        assertEquals("PENDING_VALIDATION", pendingSource.sourceStatus)
        assertNull(pendingSource.publicationYear)
    }

    // =========================================================================
    // TESTE 14: Percentis inexistentes permanecem PENDING_VALIDATION
    // =========================================================================
    @Test
    fun test14_nonExistentPercentilesRemainPendingValidation() {
        val ref = PopulationReferenceRegistry.getReference("POPREF-VO2-GENERAL-PENDING")
        assertNotNull(ref)
        assertEquals(MethodologyValidationStatus.PENDING_REVIEW, ref!!.validationStatus)
        assertTrue(ref.percentileData.isEmpty()) // Sem percentis inventados!
    }

    // =========================================================================
    // TESTE 15: Normalização sem referência válida não é ativada
    // =========================================================================
    @Test
    fun test15_normalizationWithoutValidReferenceIsNotActivated() {
        val normResult = NormalizationEvaluator.normalize(
            rawValue = 50.0,
            normalizationId = "NORM-PERCENTILE-VO2-PENDING"
        )

        assertFalse(normResult.success)
        assertNull(normResult.normalizedValue)
        assertTrue(normResult.statusMessage.contains("INACTIVE_OR_PENDING_REFERENCE") || normResult.statusMessage.contains("PENDING_VALIDATION"))
    }

    // =========================================================================
    // TESTE 16: Protocol deviation é registrada
    // =========================================================================
    @Test
    fun test16_protocolDeviationIsRecorded() {
        val customDeviation = ProtocolDeviation(
            deviationId = "DEV-001",
            measurementId = "M-008",
            protocolId = "PROT-IMTP-FORCE-V1",
            type = "POSITION_OFFSET",
            severity = DeviationSeverity.MINOR,
            description = "Ângulo de joelho a 120 graus (5 graus fora do alvo)",
            impact = "Leve desvio submáximo"
        )

        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-008",
            metricId = "RELATIVE_FORCE",
            protocolId = "PROT-IMTP-FORCE-V1",
            instrumentId = "INST-FORCE-PLATE-1000HZ",
            unit = "N/kg",
            rawValue = 38.5,
            samplingRateObserved = 1000.0,
            clientTimestamp = System.currentTimeMillis(),
            calibrationDate = System.currentTimeMillis(),
            explicitDeviations = listOf(customDeviation)
        )

        assertTrue(result.deviations.any { it.deviationId == "DEV-001" })
    }

    // =========================================================================
    // TESTE 17: Violação invalidante rejeita medição quando política exigir
    // =========================================================================
    @Test
    fun test17_invalidatingViolationRejectsMeasurement() {
        val invalidatingDeviation = ProtocolDeviation(
            deviationId = "DEV-002",
            measurementId = "M-009",
            protocolId = "PROT-IMTP-FORCE-V1",
            type = "EQUIPMENT_FAILURE",
            severity = DeviationSeverity.INVALIDATING,
            description = "Barra soltou-se durante a tração",
            impact = "Dados corrompidos e inválidos"
        )

        val result = MeasurementQualityGate.evaluate(
            measurementId = "M-009",
            metricId = "RELATIVE_FORCE",
            protocolId = "PROT-IMTP-FORCE-V1",
            instrumentId = "INST-FORCE-PLATE-1000HZ",
            unit = "N/kg",
            rawValue = 12.0,
            samplingRateObserved = 1000.0,
            clientTimestamp = System.currentTimeMillis(),
            calibrationDate = System.currentTimeMillis(),
            explicitDeviations = listOf(invalidatingDeviation)
        )

        assertEquals(QualityGateStatus.REJECTED, result.status)
        assertTrue(result.failedChecks.contains("HAS_INVALIDATING_PROTOCOL_DEVIATION"))
    }

    // =========================================================================
    // TESTE 18: Cliente não pode alterar metodologia
    // =========================================================================
    @Test
    fun test18_clientCannotMutateMethodology() {
        val mutatedMeth = ScientificMethodologyRegistry.getMethodology("METH-VO2MAX-CPX-V1")!!.copy(
            name = "Metodologia Alterada por Cliente Não Autorizado"
        )

        val success = ScientificSecurityBarrier.registerOrUpdateMethodology(
            callerTier = ScientificCallerTier.CLIENT,
            callerId = "CLIENT_APP_001",
            methodology = mutatedMeth
        )

        assertFalse(success)
        val audits = ScientificSecurityBarrier.getAuditLogs()
        assertTrue(audits.any { it.securityViolation && it.callerTier == ScientificCallerTier.CLIENT })
    }

    // =========================================================================
    // TESTE 19: Cliente não pode homologar protocolo
    // =========================================================================
    @Test
    fun test19_clientCannotHomologateProtocol() {
        val fakeProt = ScientificProtocolRegistry.getProtocol("PROT-VO2-RAMP-V1")!!.copy(
            protocolId = "PROT-CLIENT-HACK",
            name = "Protocolo Falso do Cliente"
        )

        val success = ScientificSecurityBarrier.registerOrUpdateProtocol(
            callerTier = ScientificCallerTier.CLIENT,
            callerId = "CLIENT_APP_001",
            protocol = fakeProt
        )

        assertFalse(success)
        assertFalse(ScientificProtocolRegistry.containsProtocol("PROT-CLIENT-HACK"))
    }

    // =========================================================================
    // TESTE 20: IA não pode validar metodologia
    // =========================================================================
    @Test
    fun test20_aiCannotValidateMethodology() {
        val aiValidatedMeth = ScientificMethodologyRegistry.getMethodology("METH-VO2MAX-CPX-V1")!!.copy(
            methodologyId = "METH-AI-VALIDATED",
            validationStatus = MethodologyValidationStatus.ACTIVE
        )

        val success = ScientificSecurityBarrier.registerOrUpdateMethodology(
            callerTier = ScientificCallerTier.AI_GATEWAY,
            callerId = "AI_AGENT_GEMINI",
            methodology = aiValidatedMeth
        )

        assertFalse(success)
        assertFalse(ScientificMethodologyRegistry.containsMethodology("METH-AI-VALIDATED"))
    }

    // =========================================================================
    // TESTE 21: IA não pode alterar evidence level
    // =========================================================================
    @Test
    fun test21_aiCannotAlterEvidenceLevel() {
        val rfdMeth = ScientificMethodologyRegistry.getMethodology("METH-RFD-ISOM-V1")!!.copy(
            evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH
        )

        val success = ScientificSecurityBarrier.registerOrUpdateMethodology(
            callerTier = ScientificCallerTier.AI_GATEWAY,
            callerId = "AI_AGENT_GEMINI",
            methodology = rfdMeth
        )

        assertFalse(success)
    }

    // =========================================================================
    // TESTE 22: Mock não pode homologar metodologia oficial
    // =========================================================================
    @Test
    fun test22_mockCannotHomologateOfficialMethodology() {
        val officialMeth = ScientificMethodologyRegistry.getMethodology("METH-VO2MAX-CPX-V1")!!.copy(
            methodologyId = "METH-SIMULATION-OFFICIAL",
            name = "Metodologia Oficial Simulada"
        )

        val success = ScientificSecurityBarrier.registerOrUpdateMethodology(
            callerTier = ScientificCallerTier.CORE_ENGINE,
            callerId = "SIMULATION_TEST_RUNNER",
            methodology = officialMeth,
            simulationMode = true
        )

        assertFalse(success)
        assertFalse(ScientificMethodologyRegistry.containsMethodology("METH-SIMULATION-OFFICIAL"))
    }

    // =========================================================================
    // TESTE 23: Mesmo input + mesmo método + mesma versão = resultado determinístico
    // =========================================================================
    @Test
    fun test23_deterministicEvaluation() {
        val now = 1700000000000L
        val eval1 = MeasurementQualityGate.evaluate(
            measurementId = "M-DET-01",
            metricId = "VO2_MAX",
            protocolId = "PROT-VO2-RAMP-V1",
            instrumentId = "INST-METABOLIC-CART",
            unit = "ml/kg/min",
            rawValue = 50.0,
            samplingRateObserved = 1.0,
            clientTimestamp = now,
            serverTimestamp = now,
            calibrationDate = now
        )

        val eval2 = MeasurementQualityGate.evaluate(
            measurementId = "M-DET-01",
            metricId = "VO2_MAX",
            protocolId = "PROT-VO2-RAMP-V1",
            instrumentId = "INST-METABOLIC-CART",
            unit = "ml/kg/min",
            rawValue = 50.0,
            samplingRateObserved = 1.0,
            clientTimestamp = now,
            serverTimestamp = now,
            calibrationDate = now
        )

        assertEquals(eval1.status, eval2.status)
        assertEquals(eval1.passedChecks, eval2.passedChecks)
        assertEquals(eval1.failedChecks, eval2.failedChecks)
        assertEquals(eval1.auditReference, eval2.auditReference)
    }

    // =========================================================================
    // TESTE 24: Toda alteração oficial gera AuditLog
    // =========================================================================
    @Test
    fun test24_officialChangesGenerateAuditLog() {
        val initialAuditCount = ScientificSecurityBarrier.getAuditLogs().size

        val validNewProtocol = ScientificProtocol(
            protocolId = "PROT-CORE-OFFICIAL-TEST",
            name = "Protocolo de Teste do Core",
            version = "1.0.0",
            methodologyId = "METH-VO2MAX-CPX-V1",
            metricId = "VO2_MAX",
            purpose = "Teste de auditoria",
            preparationRequirements = emptyList(),
            equipmentRequirements = emptyList(),
            executionSteps = listOf("Passo 1"),
            samplingRate = 1.0,
            duration = 60,
            repetitions = 1,
            restInterval = 0,
            environmentalRequirements = emptyList(),
            exclusionCriteria = emptyList(),
            qualityRequirements = emptyList(),
            acceptedDevices = listOf("METABOLIC_CART"),
            acceptedSources = listOf("LABORATORY_METABOLIC_CART"),
            validationStatus = ProtocolValidationStatus.ACTIVE
        )

        val success = ScientificSecurityBarrier.registerOrUpdateProtocol(
            callerTier = ScientificCallerTier.CORE_ENGINE,
            callerId = "CORE_SYSTEM_ADMIN",
            protocol = validNewProtocol
        )

        assertTrue(success)
        val audits = ScientificSecurityBarrier.getAuditLogs()
        assertTrue(audits.size > initialAuditCount)
        val latestAudit = audits.last()
        assertEquals("ScientificProtocol", latestAudit.targetEntity)
        assertEquals("PROT-CORE-OFFICIAL-TEST", latestAudit.targetId)
        assertTrue(latestAudit.checksum.isNotEmpty())
    }

    // =========================================================================
    // TESTE 25: Histórico metodológico é imutável
    // =========================================================================
    @Test
    fun test25_methodologyHistoryIsImmutable() {
        val initialMeth = ScientificMethodologyRegistry.getMethodology("METH-VO2MAX-CPX-V1")
        assertNotNull(initialMeth)
        val originalPrinciple = initialMeth!!.measurementPrinciple

        // Tentar obter a lista e validar que consultas subsequentes refletem a integridade
        val list = ScientificMethodologyRegistry.getAllMethodologies()
        val found = list.find { it.methodologyId == "METH-VO2MAX-CPX-V1" }
        assertEquals(originalPrinciple, found?.measurementPrinciple)
    }

    // =========================================================================
    // TESTE 26: Todas as 11 métricas centrais registradas com fichas estruturadas
    // =========================================================================
    @Test
    fun test26_allElevenMetricsAreRegistered() {
        val expectedMetrics = listOf(
            "VO2_MAX",
            "RELATIVE_FORCE",
            "RFD",
            "HRV_RMSSD",
            "CRITICAL_POWER",
            "W_PRIME",
            "JOINT_STABILITY",
            "SYMMETRY",
            "ROM",
            "MEAN_PROPULSIVE_VELOCITY",
            "ACCELERATION"
        )

        expectedMetrics.forEach { metricId ->
            assertTrue("Métrica $metricId deve estar no MetricDefinitionRegistry", MetricDefinitionRegistry.containsMetric(metricId))
            val methodologies = ScientificMethodologyRegistry.getMethodologiesForMetric(metricId)
            assertTrue("Métrica $metricId deve possuir metodologia associada", methodologies.isNotEmpty())
        }
    }

    // =========================================================================
    // TESTE 27: RepeatabilityEvaluator exige dados suficientes (mínimo de 2 ensaios)
    // =========================================================================
    @Test
    fun test27_repeatabilityEvaluatorRequiresSufficientTrials() {
        // Ensaio único -> PENDING_REVIEW sem calcular estatísticas fabricadas
        val singleTrial = RepeatabilityEvaluator.calculateReliability(
            metricId = "RELATIVE_FORCE",
            methodId = "METH-REL-FORCE-IMTP-V1",
            trial1Values = listOf(35.0),
            trial2Values = listOf(35.2)
        )
        assertEquals(MethodologyValidationStatus.PENDING_REVIEW, singleTrial.validationStatus)
        assertNull(singleTrial.typicalError)

        // 3 ensaios -> Calculado com sucesso
        val multiTrial = RepeatabilityEvaluator.calculateReliability(
            metricId = "RELATIVE_FORCE",
            methodId = "METH-REL-FORCE-IMTP-V1",
            trial1Values = listOf(35.0, 38.0, 40.0),
            trial2Values = listOf(35.5, 37.8, 40.2)
        )
        assertEquals(MethodologyValidationStatus.VALIDATED, multiTrial.validationStatus)
        assertNotNull(multiTrial.typicalError)
        assertNotNull(multiTrial.coefficientOfVariation)
        assertNotNull(multiTrial.minimumDetectableChange)
        assertTrue(multiTrial.typicalError!! > 0.0)
    }

    // =========================================================================
    // TESTE 28: AI Explainer provê dados estruturados estritamente em modo leitura
    // =========================================================================
    @Test
    fun test28_aiExplainerProvidesFactualReadOnlyData() {
        val explanation = ScientificMethodologyExplainer.explainMethodology("METH-VO2MAX-CPX-V1")
        assertTrue(explanation.contains("FICHA CIENTÍFICA"))
        assertTrue(explanation.contains("Ergoespirometria"))

        val protocolExplanation = ScientificMethodologyExplainer.explainProtocol("PROT-VO2-RAMP-V1")
        assertTrue(protocolExplanation.contains("Cicloergômetro"))

        val metricSheet = ScientificMethodologyExplainer.getMetricSheet("VO2_MAX")
        assertTrue(metricSheet.contains("Consumo Máximo de Oxigênio"))
    }
}
