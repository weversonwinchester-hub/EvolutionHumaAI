package com.example.core.evolutionengine.explanation

import com.example.core.evolutionengine.catalog.ClassCatalog

/**
 * EVOLUTION HUMAN AI — CLASS EXPLANATION SYSTEM V1
 *
 * Immutable, structured registry for formal human-readable explanations of all 22 public classes.
 * Ensures AI Gateway and presentation layers never invent scientific criteria.
 */
data class ClassExplanation(
    val classId: String,
    val order: Int,
    val name: String,
    val meaning: String,
    val description: String,
    val whyInThisClass: String,
    val evidencesConsidered: List<String>,
    val defaultCriteriaSatisfied: List<String>,
    val defaultCriteriaPending: List<String>,
    val whatItDoesNotMean: String,
    val nextClassId: String?,
    val nextClassName: String?,
    val progressionRequirements: List<String>,
    val evidenceState: String,
    val minimumTenureWeeks: Int = 0
)

object ClassExplanationRegistryV1 {

    private val explanations: Map<String, ClassExplanation> = mapOf(
        ClassCatalog.CLASS_01 to ClassExplanation(
            classId = ClassCatalog.CLASS_01,
            order = 1,
            name = "01 Corpo Adormecido",
            meaning = "Estado basal de repouso pré-adaptação física.",
            description = "Ponto de partida universal. O organismo ainda não foi submetido a estímulos estruturados com registro formal de evidências.",
            whyInThisClass = "Você iniciou a sua jornada ou está em fase de coleta dos dados basais de calibração.",
            evidencesConsidered = listOf("Cadastro inicial do atleta", "Perfil biomecânico preliminar"),
            defaultCriteriaSatisfied = listOf("Perfil de atleta criado"),
            defaultCriteriaPending = listOf("Primeira sessão de treinamento registrada", "Avaliação cinemática básica"),
            whatItDoesNotMean = "Não significa incapacidade ou limitação permanente; é apenas o estado inicial de calibração.",
            nextClassId = ClassCatalog.CLASS_02,
            nextClassName = "02 Sobrevivente",
            progressionRequirements = listOf("Completar 3 sessões de treino", "Executar teste de consistência postural básico"),
            evidenceState = "EVIDÊNCIA BASAL INICIAL",
            minimumTenureWeeks = 0
        ),
        ClassCatalog.CLASS_02 to ClassExplanation(
            classId = ClassCatalog.CLASS_02,
            order = 2,
            name = "02 Sobrevivente",
            meaning = "Primeiro estágio de engajamento físico ativo e resiliência inicial.",
            description = "O atleta demonstrou capacidade de aderência básica a rotinas de movimento e estabilidade postural primária.",
            whyInThisClass = "Você completou as primeiras sessões de ativação motora comprovadas por evidências locais.",
            evidencesConsidered = listOf("Histórico de sessões iniciais", "Grau de adesão ao cronograma"),
            defaultCriteriaSatisfied = listOf("3+ sessões concluídas", "Execução básica de agachamento e flexão"),
            defaultCriteriaPending = listOf("Consistência de 2 semanas consecutivas", "Qualidade cinemática estável"),
            whatItDoesNotMean = "Não significa que você é iniciante em todos os esportes, apenas que seus registros formais no sistema estão no estágio 2.",
            nextClassId = ClassCatalog.CLASS_03,
            nextClassName = "03 Desperto",
            progressionRequirements = listOf("Acumular 6 sessões com evidência", "Manter índice de completude > 80%"),
            evidenceState = "EM CONSTRUÇÃO DE HISTÓRICO",
            minimumTenureWeeks = 1
        ),
        ClassCatalog.CLASS_03 to ClassExplanation(
            classId = ClassCatalog.CLASS_03,
            order = 3,
            name = "03 Desperto",
            meaning = "Consciência motora e adaptações fisiológicas primárias registradas.",
            description = "O atleta apresenta padrão biomecânico estável em movimentos fundamentais sem compensações severas.",
            whyInThisClass = "Sua consistência e controle de execução básica foram validados pelo motor de evidência.",
            evidencesConsidered = listOf("Evidência cinemática de ângulo articular", "Volume de repetições acumulado"),
            defaultCriteriaSatisfied = listOf("Controle de cadência excêntrica", "Registro de volume semanal constante"),
            defaultCriteriaPending = listOf("Superação de testes de fadiga", "Variabilidade de estímulo"),
            whatItDoesNotMean = "Não significa que você atingiu o pico de força, mas sim que sua fundação de movimento é sólida.",
            nextClassId = ClassCatalog.CLASS_04,
            nextClassName = "04 Iniciado",
            progressionRequirements = listOf("Consistência de 4 semanas", "Qualidade de movimento > 75 pts"),
            evidenceState = "EVIDÊNCIA EM MATURAÇÃO",
            minimumTenureWeeks = 2
        ),
        ClassCatalog.CLASS_04 to ClassExplanation(
            classId = ClassCatalog.CLASS_04,
            order = 4,
            name = "04 Iniciado",
            meaning = "Familiaridade com rotinas de avaliação e consistência postural comprovada.",
            description = "Capacidade de manter técnica estável sob cargas progressivas e demandas metabólicas moderadas.",
            whyInThisClass = "Evidências comprovam regularidade semanal e precisão angular nos movimentos canônicos.",
            evidencesConsidered = listOf("Score de simetria bilateral", "Volume de sobrecarga progressiva"),
            defaultCriteriaSatisfied = listOf("Simetria bilateral > 80%", "Adesão regular a microciclos"),
            defaultCriteriaPending = listOf("Protocolo de força máxima relativa", "Consolidação de volume intermediário"),
            whatItDoesNotMean = "Não significa estagnação; você está na fase de expansão de capacidade de trabalho.",
            nextClassId = ClassCatalog.CLASS_05,
            nextClassName = "05 Explorador",
            progressionRequirements = listOf("Completar 16 sessões registradas", "Score de consistência > 80 pts"),
            evidenceState = "EVIDÊNCIA CONSISTENTE",
            minimumTenureWeeks = 4
        ),
        ClassCatalog.CLASS_05 to ClassExplanation(
            classId = ClassCatalog.CLASS_05,
            order = 5,
            name = "05 Explorador",
            meaning = "Exploração de múltiplas valências físicas e expansão de capacidade aeróbica e de força.",
            description = "Atleta apto a treinar com maior volume, múltiplas intensidades e padrões de movimento complexos.",
            whyInThisClass = "Seus registros demonstram domínio dos exercícios fundamentais e capacidade de recuperação adequada.",
            evidencesConsidered = listOf("Diversidade de padrões motores", "Volume de treino e RPE médio"),
            defaultCriteriaSatisfied = listOf("Domínio de 5+ padrões motores", "Relação trabalho/descanso balanceada"),
            defaultCriteriaPending = listOf("Trial de qualificação Classe 06", "Validação sob fadiga extrema"),
            whatItDoesNotMean = "Não significa limite de especialização; você está expandindo seu repertório atlético.",
            nextClassId = ClassCatalog.CLASS_06,
            nextClassName = "06 Aprendiz",
            progressionRequirements = listOf("Executar Trial de Força & Estabilidade", "Score geral > 82 pts"),
            evidenceState = "EVIDÊNCIA AUDITADA",
            minimumTenureWeeks = 6
        ),
        ClassCatalog.CLASS_06 to ClassExplanation(
            classId = ClassCatalog.CLASS_06,
            order = 6,
            name = "06 Aprendiz",
            meaning = "Consolidação técnica sob protocolos padronizados de mensuração direta.",
            description = "Entrada no nível intermediário com métricas objetivas de velocidade e estabilidade mecânica.",
            whyInThisClass = "Você superou com sucesso os testes de validação do nível fundamental.",
            evidencesConsidered = listOf("Evidência de Trial formal", "Série temporal de dados biométricos"),
            defaultCriteriaSatisfied = listOf("Trial Classe 05 aprovado", "Estabilidade de core e postura comprovadas"),
            defaultCriteriaPending = listOf("Sobrecarga crônica avançada", "Velocidade de barra / deslocamento"),
            whatItDoesNotMean = "Não é um título iniciante, e sim o início da maestria metodológica rigorosa.",
            nextClassId = ClassCatalog.CLASS_07,
            nextClassName = "07 Discípulo",
            progressionRequirements = listOf("Completar 24 sessões intermediárias", "Consistência longitudinal de 8 semanas"),
            evidenceState = "EVIDÊNCIA CONSOLIDADA",
            minimumTenureWeeks = 8
        ),
        ClassCatalog.CLASS_07 to ClassExplanation(
            classId = ClassCatalog.CLASS_07,
            order = 7,
            name = "07 Discípulo",
            meaning = "Disciplina rigorosa e capacidade de trabalho neuromuscular elevada.",
            description = "O atleta sustenta alta densidade de treino com qualidade de execução resiliente à fadiga.",
            whyInThisClass = "Consistência auditada de volume e adaptações neuromusculares mensuráveis.",
            evidencesConsidered = listOf("Densidade de treino", "Qualidade técnica sob estresse"),
            defaultCriteriaSatisfied = listOf("8+ semanas de consistência ininterrupta", "Zero desvios críticos de protocolo"),
            defaultCriteriaPending = listOf("Trial de Potência e Resistência Específica"),
            whatItDoesNotMean = "Não significa imunidade a overtraining; exige monitoramento de recuperação.",
            nextClassId = ClassCatalog.CLASS_08,
            nextClassName = "08 Atleta Emergente",
            progressionRequirements = listOf("Índice de Prontidão e Volume > 85 pts"),
            evidenceState = "EVIDÊNCIA MADURA",
            minimumTenureWeeks = 10
        ),
        ClassCatalog.CLASS_08 to ClassExplanation(
            classId = ClassCatalog.CLASS_08,
            order = 8,
            name = "08 Atleta Emergente",
            meaning = "Transição para o nível competitivo e alta eficiência mecânica.",
            description = "Desempenho físico significativamente acima da média populacional com precisão cinemática.",
            whyInThisClass = "Demonstração de força relativa, potência e controle corporal avançado.",
            evidencesConsidered = listOf("Força relativa / massa magra", "Tempo sob tensão e velocidade concêntrica"),
            defaultCriteriaSatisfied = listOf("Força relativa nível 8", "Excelente simetria e controle dinâmico"),
            defaultCriteriaPending = listOf("Trial Oficial Classe 09"),
            whatItDoesNotMean = "Não é garantia de vitória esportiva, mas sim certificação de capacidade motora avançada.",
            nextClassId = ClassCatalog.CLASS_09,
            nextClassName = "09 Competidor",
            progressionRequirements = listOf("Trial de Qualificação Competitiva", "Score Global > 88 pts"),
            evidenceState = "EVIDÊNCIA DE ALTO DESEMPENHO",
            minimumTenureWeeks = 12
        ),
        ClassCatalog.CLASS_09 to ClassExplanation(
            classId = ClassCatalog.CLASS_09,
            order = 9,
            name = "09 Competidor",
            meaning = "Perfil atlético com prontidão para demandas de alta intensidade.",
            description = "Alta resistência à fadiga com velocidade de execução e técnica preservadas.",
            whyInThisClass = "Aprovação em protocolos de trial de alta intensidade com evidência criptografada.",
            evidencesConsidered = listOf("Resultados de Trial oficial", "Tolerância a altas cargas"),
            defaultCriteriaSatisfied = listOf("Trial de alta intensidade aprovado", "Histórico de 3+ meses consistente"),
            defaultCriteriaPending = listOf("Requisitos para Classe 10 Atleta"),
            whatItDoesNotMean = "Não exclui necessidade de periodização inteligente.",
            nextClassId = ClassCatalog.CLASS_10,
            nextClassName = "10 Atleta",
            progressionRequirements = listOf("Completar bloco de periodização avançado"),
            evidenceState = "EVIDÊNCIA OFICIAL CERTIFICADA",
            minimumTenureWeeks = 14
        ),
        ClassCatalog.CLASS_10 to ClassExplanation(
            classId = ClassCatalog.CLASS_10,
            order = 10,
            name = "10 Atleta",
            meaning = "Marco central do sistema: atleta plenamente formado e adaptado.",
            description = "Excelência em força, mobilidade, capacidade cardiorrespiratória e consistência motora.",
            whyInThisClass = "Validação completa de todas as valências físicas fundamentais.",
            evidencesConsidered = listOf("Bateria completa de testes físicos", "Audit log ininterrupto"),
            defaultCriteriaSatisfied = listOf("Perfil atlético equilibrado em todas as 7 dimensões"),
            defaultCriteriaPending = listOf("Especialização para classes superiores (11-22)"),
            whatItDoesNotMean = "Não é o fim da evolução, mas sim a graduação para o alto rendimento.",
            nextClassId = ClassCatalog.CLASS_11,
            nextClassName = "11 Especialista",
            progressionRequirements = listOf("Especialização em domínio de performance específico"),
            evidenceState = "EVIDÊNCIA MASTER",
            minimumTenureWeeks = 16
        )
    )

    fun getExplanation(classId: String): ClassExplanation? {
        val existing = explanations[classId]
        if (existing != null) return existing

        // Fallback dinâmico para as classes 11 a 22
        val classDef = ClassCatalog.CLASSES.find { it.classId == classId } ?: return null
        val nextClass = ClassCatalog.CLASSES.find { it.order == classDef.order + 1 }

        return ClassExplanation(
            classId = classDef.classId,
            order = classDef.order,
            name = classDef.name,
            meaning = "Nível avançado/elite de maestria física e atlética (${classDef.name}).",
            description = classDef.description,
            whyInThisClass = "Seu desempenho histórico e validações científicas satisfizeram os requisitos da ordem ${classDef.order}.",
            evidencesConsidered = listOf("Bateria de Trials Avançados", "Consistência Longitudinal Superior"),
            defaultCriteriaSatisfied = listOf("Requisitos da Classe ${classDef.order} atendidos com evidência oficial"),
            defaultCriteriaPending = if (nextClass != null) listOf("Requisitos específicos da Classe ${nextClass.name}") else emptyList(),
            whatItDoesNotMean = "Não é um limite estático; o atleta continua em aperfeiçoamento contínuo.",
            nextClassId = nextClass?.classId,
            nextClassName = nextClass?.name,
            progressionRequirements = if (nextClass != null) listOf("Satisfazer política de elegibilidade ${nextClass.requirementPolicyId}") else listOf("Classe máxima atingida"),
            evidenceState = "EVIDÊNCIA ELITE HOMOLOGADA",
            minimumTenureWeeks = classDef.order * 2
        )
    }

    fun getAllExplanations(): List<ClassExplanation> {
        return ClassCatalog.CLASSES.mapNotNull { getExplanation(it.classId) }
    }
}
