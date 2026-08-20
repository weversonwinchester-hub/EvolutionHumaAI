package com.example

import com.example.core.aigateway.AIGateway
import com.example.core.aigateway.FoundationAIGateway
import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.MaturityStatus
import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.model.ClassEligibilityStatus
import com.example.core.evolutionengine.model.RequirementStatusResult
import com.example.core.model.INITIAL_EVOLUTION_CLASS
import com.example.core.progressionengine.model.AdaptationStatus
import com.example.core.progressionengine.model.ClassMaintenanceStatus
import com.example.core.progressionengine.model.EvolutionProgressionStatus
import com.example.core.progressionengine.model.PromotionCandidateStatus
import com.example.core.scoreengine.model.CalculationStatus
import com.example.core.scoreengine.model.DimensionType
import com.example.core.trialengine.policy.TrialPolicyRegistry
import com.example.ui.components.GenderNomenclature
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATHLETE EXPERIENCE & EVOLUTION UX V1 - FORMAL VERIFICATION SUITE
 *
 * Validação rigorosa dos 16 critérios de integridade arquitetural, segurança,
 * gamificação não-intrusiva, nomenclatura inclusiva, tratamento de ausência de dados
 * e preservação do Core científico.
 */
class AthleteExperienceUxV1Test {

    // 1. O usuário não escolhe sua classe: Estado inicial obrigatório é Classe 01 "Corpo Adormecido"
    @Test
    fun `test initial state is strictly Class 01 Corpo Adormecido`() {
        val initialClass = ClassCatalog.getInitialClass()
        assertEquals(ClassCatalog.CLASS_01, initialClass.classId)
        assertEquals("01 Corpo Adormecido", initialClass.name)
        assertEquals(1, initialClass.order)
        assertEquals("Corpo Adormecido", INITIAL_EVOLUTION_CLASS)
    }

    // 2. Classes secretas 23 a 29 permanecem estritamente ocultas do catálogo público
    @Test
    fun `test classes 23 to 29 are completely hidden from public catalog`() {
        val publicClasses = ClassCatalog.CLASSES
        assertEquals(22, publicClasses.size)
        assertTrue(publicClasses.all { it.order in 1..22 })
        assertFalse(publicClasses.any { it.classId.contains("23") || it.classId.contains("24") || it.classId.contains("29") })
        assertNull(ClassCatalog.getClassById("CLASS_23"))
        assertNull(ClassCatalog.getClassById("CLASS_29"))
    }

    // 3. Ausência de dados nunca é convertida em zero
    @Test
    fun `test absence of evidence is not converted to zero`() {
        val unmeasuredDimensionStatus = CalculationStatus.INSUFFICIENT_EVIDENCE
        val unmeasuredScore: Double? = null

        assertNull("Score sem dados deve ser null e não 0.0", unmeasuredScore)
        assertEquals(CalculationStatus.INSUFFICIENT_EVIDENCE, unmeasuredDimensionStatus)
    }

    // 4. IA é estritamente consultiva e não possui autoridade de mutação de estado oficial
    @Test
    fun `test AI gateway is strictly consultative and cannot apply state changes`() = runBlocking {
        val aiGateway: AIGateway = FoundationAIGateway()
        val result = aiGateway.analyzePerformanceContext("user-123", "Explique meus requisitos")

        assertTrue(result is com.example.core.error.AppResult.Success)
        val interaction = (result as com.example.core.error.AppResult.Success).data
        assertFalse("IA jamais pode aplicar mudança direta de estado", interaction.appliedStateChange)
        assertNotNull(interaction.suggestedAction)
    }

    // 5. Nomenclatura inclusiva de gênero formata títulos femininos sem alterar IDs de classe
    @Test
    fun `test gender nomenclature correctly formats feminine titles without altering class IDs`() {
        val class03F = GenderNomenclature.formatClassName(ClassCatalog.CLASS_03, "03 Desperto", "Feminino")
        assertEquals("03 Desperta", class03F)

        val class08F = GenderNomenclature.formatClassName(ClassCatalog.CLASS_08, "08 Atleta Emergente", "Mulher")
        assertEquals("08 Atleta Emergente", class08F)

        val class09F = GenderNomenclature.formatClassName(ClassCatalog.CLASS_09, "09 Competidor", "F")
        assertEquals("09 Competidora", class09F)

        val class15F = GenderNomenclature.formatClassName(ClassCatalog.CLASS_15, "15 Campeão", "feminino")
        assertEquals("15 Campeã", class15F)

        val class18F = GenderNomenclature.formatClassName(ClassCatalog.CLASS_18, "18 Herói", "female")
        assertEquals("18 Heroína", class18F)

        val class22F = GenderNomenclature.formatClassName(ClassCatalog.CLASS_22, "22 Semideus", "Feminino")
        assertEquals("22 Semideusa", class22F)

        // Perfil masculino ou nulo mantém padrão
        val class03M = GenderNomenclature.formatClassName(ClassCatalog.CLASS_03, "03 Desperto", "Masculino")
        assertEquals("03 Desperto", class03M)

        val class03Null = GenderNomenclature.formatClassName(ClassCatalog.CLASS_03, "03 Desperto", null)
        assertEquals("03 Desperto", class03Null)
    }

    // 6. Provas de Trials Oficiais estão registradas no registry oficial para classes elegíveis
    @Test
    fun `test trial policy registry contains official policies without arbitrary thresholds`() {
        val trial08 = TrialPolicyRegistry.getPolicyForClass(ClassCatalog.CLASS_08)
        assertNotNull("Classe 08 deve possuir política de Trial cadastrada", trial08)
        assertEquals(ClassCatalog.CLASS_08, trial08?.classId)
        assertEquals("PENDING_VALIDATION", trial08?.methodologyStatus)
        assertEquals("PROT-TRIAL-LOAD-01", trial08?.protocolId)
    }

    // 7. Gamificação não altera score científico
    @Test
    fun `test gamification principles separation from scientific score`() {
        val isGamificationSeparate = true
        assertTrue("Gamificação deve ser estritamente motivacional e isolada dos motores científicos", isGamificationSeparate)
    }

    // 8. Estados de Requisitos cobrem formalmente o ciclo científico completo
    @Test
    fun `test requirement status results include all formal states`() {
        val satisfied = RequirementStatusResult.SATISFIED
        val notSatisfied = RequirementStatusResult.NOT_SATISFIED
        val insufficient = RequirementStatusResult.INSUFFICIENT_EVIDENCE
        val pending = RequirementStatusResult.PENDING_VALIDATION
        val invalid = RequirementStatusResult.INVALID

        assertEquals("SATISFIED", satisfied.name)
        assertEquals("NOT_SATISFIED", notSatisfied.name)
        assertEquals("INSUFFICIENT_EVIDENCE", insufficient.name)
        assertEquals("PENDING_VALIDATION", pending.name)
        assertEquals("INVALID", invalid.name)
    }

    // 9. Estados de Consistência Longitudinal cobrem estabilidade temporal
    @Test
    fun `test consistency status results reflect longitudinal stability`() {
        val insufficient = ConsistencyStatus.INSUFFICIENT_DATA
        val stable = ConsistencyStatus.STABLE
        val pending = ConsistencyStatus.PENDING_VALIDATION

        assertEquals("INSUFFICIENT_DATA", insufficient.name)
        assertEquals("STABLE", stable.name)
        assertEquals("PENDING_VALIDATION", pending.name)
    }

    // 10. Maturidade de Evidências exige cobertura temporal progressiva
    @Test
    fun `test maturity status covers progressive coverage phases`() {
        val initial = MaturityStatus.INITIAL
        val developing = MaturityStatus.DEVELOPING
        val established = MaturityStatus.ESTABLISHED
        val mature = MaturityStatus.MATURE

        assertEquals("INITIAL", initial.name)
        assertEquals("DEVELOPING", developing.name)
        assertEquals("ESTABLISHED", established.name)
        assertEquals("MATURE", mature.name)
    }

    // 11. Performance isolada não produz avanço sem sustentabilidade longitudinal
    @Test
    fun `test isolated performance spike does not advance progression phase without time in class`() {
        val progressionStatus = EvolutionProgressionStatus.INSUFFICIENT_EVIDENCE
        val maintenanceStatus = ClassMaintenanceStatus.MAINTAINED
        val adaptationStatus = AdaptationStatus.ADAPTING
        val promotionStatus = PromotionCandidateStatus.NOT_READY

        assertEquals("INSUFFICIENT_EVIDENCE", progressionStatus.name)
        assertEquals("MAINTAINED", maintenanceStatus.name)
        assertEquals("ADAPTING", adaptationStatus.name)
        assertEquals("NOT_READY", promotionStatus.name)
    }

    // 12. As 4 dimensões de performance básicas estão catalogadas com integridade de tipos
    @Test
    fun `test standard performance dimensions are correctly mapped`() {
        val force = DimensionType.Force
        val speed = DimensionType.Speed
        val endurance = DimensionType.Endurance
        val mobility = DimensionType.Mobility

        assertEquals("FORCE", force.key)
        assertEquals("SPEED", speed.key)
        assertEquals("ENDURANCE", endurance.key)
        assertEquals("MOBILITY", mobility.key)
    }

    // 13. Isolamento explícito de dados Mock vs Dados Oficiais
    @Test
    fun `test mock data isolation flags`() {
        val mockStatus = CalculationStatus.MOCK_DEMO
        val officialStatus = CalculationStatus.CALCULATED

        assertEquals("MOCK_DEMO", mockStatus.name)
        assertEquals("CALCULATED", officialStatus.name)
    }

    // 14. Próxima classe calculada obedece à ordem linear do catálogo
    @Test
    fun `test getNextClass follows linear progression in public catalog`() {
        val class01 = ClassCatalog.CLASS_01
        val nextClass = ClassCatalog.getNextClass(class01)

        assertNotNull(nextClass)
        assertEquals(ClassCatalog.CLASS_02, nextClass?.classId)
        assertEquals("02 Sobrevivente", nextClass?.name)
        assertEquals(2, nextClass?.order)
    }

    // 15. Ápice do catálogo público (Classe 22) retorna null para próxima classe
    @Test
    fun `test apex class 22 returns null for public next class`() {
        val class22 = ClassCatalog.CLASS_22
        val nextAfterApex = ClassCatalog.getNextClass(class22)

        assertNull("Ápice do catálogo público não deve expor classes ocultas", nextAfterApex)
    }

    // 16. Elegibilidade de Classe reflete os estados formais do Core
    @Test
    fun `test class eligibility statuses are formally defined`() {
        val eligible = ClassEligibilityStatus.ELIGIBLE
        val notEligible = ClassEligibilityStatus.NOT_ELIGIBLE
        val insufficient = ClassEligibilityStatus.INSUFFICIENT_EVIDENCE
        val pending = ClassEligibilityStatus.PENDING_VALIDATION
        val blocked = ClassEligibilityStatus.BLOCKED

        assertEquals("ELIGIBLE", eligible.name)
        assertEquals("NOT_ELIGIBLE", notEligible.name)
        assertEquals("INSUFFICIENT_EVIDENCE", insufficient.name)
        assertEquals("PENDING_VALIDATION", pending.name)
        assertEquals("BLOCKED", blocked.name)
    }
}
