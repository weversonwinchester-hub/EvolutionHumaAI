package com.example.core.scientific.engine

import com.example.core.scientific.model.ScientificMethodology
import com.example.core.scientific.model.ScientificProtocol
import com.example.core.scientific.registry.MetricDefinitionRegistry
import com.example.core.scientific.registry.ScientificMethodologyRegistry
import com.example.core.scientific.registry.ScientificProtocolRegistry

/**
 * PERFORMAI SCIENTIFIC METHODOLOGY EXPLAINER (AI GATEWAY INTEGRATION)
 *
 * Provê interface estritamente READ-ONLY para o AI Gateway e interfaces de usuário.
 * Permite explicar metodologias, traduzir termos, comparar métodos e resumir protocolos,
 * sem qualquer capacidade de mutação científica.
 */
object ScientificMethodologyExplainer {

    fun explainMethodology(methodologyId: String): String {
        val meth = ScientificMethodologyRegistry.getMethodology(methodologyId)
            ?: return "Metodologia com identificador $methodologyId não encontrada no registro oficial."

        return buildString {
            appendLine("=== FICHA CIENTÍFICA DA METODOLOGIA ===")
            appendLine("ID: ${meth.methodologyId}")
            appendLine("Nome: ${meth.name}")
            appendLine("Versão: ${meth.version}")
            appendLine("Métrica Associada: ${meth.metricId}")
            appendLine("Categoria: ${meth.category}")
            appendLine("Status de Validação: ${meth.validationStatus}")
            appendLine("Nível de Evidência: ${meth.evidenceLevel}")
            appendLine("\nPrincípio de Medição:\n${meth.measurementPrinciple}")
            appendLine("\nMétodo de Cálculo:\n${meth.calculationMethod}")
            appendLine("\nUnidades Aceitas: ${meth.acceptedUnits.joinToString(", ")}")
            appendLine("Equipamentos Requeridos: ${meth.requiredEquipment.joinToString(", ")}")
            appendLine("Condições Requeridas: ${meth.requiredConditions.joinToString("; ")}")
            appendLine("Limitações Metodológicas: ${meth.limitations.joinToString("; ")}")
            if (meth.sourceReferences.isNotEmpty()) {
                appendLine("\nFontes e Referências:")
                meth.sourceReferences.forEach { src ->
                    appendLine("- ${src.title} (${src.publicationYear ?: "N/D"}), por ${src.authors.joinToString(", ")}. [${src.sourceAuthority}] ${src.identifier ?: ""}")
                }
            }
        }
    }

    fun explainProtocol(protocolId: String): String {
        val prot = ScientificProtocolRegistry.getProtocol(protocolId)
            ?: return "Protocolo com identificador $protocolId não encontrado no registro oficial."

        return buildString {
            appendLine("=== PROTOCOLO CIENTÍFICO OFICIAL ===")
            appendLine("ID: ${prot.protocolId}")
            appendLine("Nome: ${prot.name}")
            appendLine("Versão: ${prot.version}")
            appendLine("Métrica: ${prot.metricId}")
            appendLine("Objetivo: ${prot.purpose}")
            appendLine("Status: ${prot.validationStatus}")
            appendLine("Taxa de Amostragem Mínima: ${prot.samplingRate ?: "N/A"} Hz")
            appendLine("Duração: ${prot.duration ?: "N/A"} s | Repetições: ${prot.repetitions ?: 1} | Intervalo: ${prot.restInterval ?: 0} s")
            appendLine("\nPassos de Execução:")
            prot.executionSteps.forEachIndexed { idx, step ->
                appendLine("${idx + 1}. $step")
            }
            appendLine("\nRequisitos de Preparação: ${prot.preparationRequirements.joinToString("; ")}")
            appendLine("Critérios de Exclusão: ${prot.exclusionCriteria.joinToString("; ")}")
            appendLine("Requisitos de Qualidade: ${prot.qualityRequirements.joinToString("; ")}")
        }
    }

    fun compareMethods(metricId: String): String {
        val methodologies = ScientificMethodologyRegistry.getMethodologiesForMetric(metricId)
        if (methodologies.isEmpty()) {
            return "Nenhuma metodologia registrada para a métrica $metricId."
        }

        return buildString {
            appendLine("=== COMPARAÇÃO METODOLÓGICA PARA A MÉTRICA: $metricId ===")
            methodologies.forEach { meth ->
                appendLine("\n[${meth.methodologyId}] - ${meth.name} (v${meth.version})")
                appendLine("- Nível de Evidência: ${meth.evidenceLevel}")
                appendLine("- Status: ${meth.validationStatus}")
                appendLine("- Princípio: ${meth.measurementPrinciple}")
                appendLine("- Limitações: ${meth.limitations.joinToString("; ")}")
            }
        }
    }

    fun getMetricSheet(metricId: String): String {
        val def = MetricDefinitionRegistry.getMetricDefinition(metricId)
            ?: return "Ficha da métrica $metricId não encontrada."

        return buildString {
            appendLine("=== DEFINIÇÃO ESTRUTURADA DE MÉTRICA: ${def.name} ===")
            appendLine("O QUE É: ${def.whatItIs}")
            appendLine("COMO É MEDIDA: ${def.howItIsMeasured}")
            appendLine("UNIDADE PADRÃO: ${def.standardUnit} (Aceitas: ${def.acceptableUnits.joinToString(", ")})")
            appendLine("INSTRUMENTOS: ${def.primaryInstruments.joinToString(", ")}")
            appendLine("PROTOCOLOS DE REFERÊNCIA: ${def.referenceProtocols.joinToString(", ")}")
            appendLine("MÉTODO DE CÁLCULO: ${def.calculationMethod}")
            appendLine("FREQUÊNCIA DE CAPTURA: ${def.captureFrequency}")
            appendLine("CONDIÇÕES REQUERIDAS: ${def.requiredConditions.joinToString("; ")}")
            appendLine("LIMITAÇÕES: ${def.limitations.joinToString("; ")}")
            appendLine("VERSÃO METODOLÓGICA: ${def.methodologyVersion}")
            appendLine("NÍVEL DE EVIDÊNCIA: ${def.evidenceLevel}")
            appendLine("STATUS: ${def.validationStatus}")
        }
    }
}
