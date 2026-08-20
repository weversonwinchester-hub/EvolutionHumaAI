package com.example.core.evolutionengine

import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.explanation.ClassExplanationRegistryV1
import org.junit.Assert.*
import org.junit.Test

/**
 * EVOLUTION HUMAN AI — CLASS EXPLANATION REGISTRY V1 TEST SUITE
 *
 * Verifies that all 22 public classes have immutable, complete,
 * and structured explanations with all mandatory 12 fields populated.
 */
class ClassExplanationRegistryV1Test {

    @Test
    fun testAll22ClassesHaveValidExplanations() {
        assertEquals(22, ClassCatalog.CLASSES.size)

        for (classDef in ClassCatalog.CLASSES) {
            val explanation = ClassExplanationRegistryV1.getExplanation(classDef.classId)
            assertNotNull("Explicação para a classe ${classDef.classId} (${classDef.name}) não pode ser nula.", explanation)

            explanation?.let { exp ->
                // 1. Nome
                assertEquals(classDef.name, exp.name)
                assertTrue("Nome da classe não pode ser vazio", exp.name.isNotBlank())

                // 2. Significado
                assertTrue("Significado não pode ser vazio para ${exp.name}", exp.meaning.isNotBlank())

                // 3. Descrição
                assertTrue("Descrição não pode ser vazia para ${exp.name}", exp.description.isNotBlank())

                // 4. Por que o atleta está nessa classe
                assertTrue("whyInThisClass não pode ser vazio para ${exp.name}", exp.whyInThisClass.isNotBlank())

                // 5. Evidências consideradas
                assertTrue("evidencesConsidered deve conter ao menos 1 item para ${exp.name}", exp.evidencesConsidered.isNotEmpty())

                // 6. Critérios cumpridos
                assertTrue("defaultCriteriaSatisfied deve conter ao menos 1 item para ${exp.name}", exp.defaultCriteriaSatisfied.isNotEmpty())

                // 7. O que NÃO significa estar naquela classe
                assertTrue("whatItDoesNotMean não pode ser vazio para ${exp.name}", exp.whatItDoesNotMean.isNotBlank())

                // 8. Estado da evidência
                assertTrue("evidenceState não pode ser vazio para ${exp.name}", exp.evidenceState.isNotBlank())

                // 9. Ordem e requisitos de progressão
                if (exp.order < 22) {
                    assertNotNull("nextClassId deve existir para classes de ordem 1 a 21 (${exp.name})", exp.nextClassId)
                    assertNotNull("nextClassName deve existir para classes de ordem 1 a 21 (${exp.name})", exp.nextClassName)
                    assertTrue("progressionRequirements não pode ser vazio para ${exp.name}", exp.progressionRequirements.isNotEmpty())
                } else {
                    assertNull("nextClassId deve ser nulo para classe de ordem 22", exp.nextClassId)
                }
            }
        }
    }

    @Test
    fun testGetAllExplanationsReturns22Items() {
        val all = ClassExplanationRegistryV1.getAllExplanations()
        assertEquals(22, all.size)
    }
}
