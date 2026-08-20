package com.example.core.evolutionengine.catalog

import com.example.core.evolutionengine.model.ClassDefinition
import com.example.core.evolutionengine.model.ClassStatus

/**
 * CATÁLOGO OFICIAL DAS 22 CLASSES DO PERFORMAI
 *
 * Estrutura formal rigorosa para progressão de atletas.
 * Cada classe possui identificador único, ordem de progressão, nome canônico,
 * política de requisitos associada e identificador de trial para classes que o exigirem.
 */
object ClassCatalog {

    const val CLASS_01 = "CLASS_01_CORPO_ADORMECIDO"
    const val CLASS_02 = "CLASS_02_SOBREVIVENTE"
    const val CLASS_03 = "CLASS_03_DESPERTO"
    const val CLASS_04 = "CLASS_04_INICIADO"
    const val CLASS_05 = "CLASS_05_EXPLORADOR"
    const val CLASS_06 = "CLASS_06_APRENDIZ"
    const val CLASS_07 = "CLASS_07_DISCIPULO"
    const val CLASS_08 = "CLASS_08_ATLETA_EMERGENTE"
    const val CLASS_09 = "CLASS_09_COMPETIDOR"
    const val CLASS_10 = "CLASS_10_ATLETA"
    const val CLASS_11 = "CLASS_11_ESPECIALISTA"
    const val CLASS_12 = "CLASS_12_PREDADOR_ATLETICO"
    const val CLASS_13 = "CLASS_13_GUERREIRO"
    const val CLASS_14 = "CLASS_14_GLADIADOR"
    const val CLASS_15 = "CLASS_15_CAMPEAO"
    const val CLASS_16 = "CLASS_16_TITA"
    const val CLASS_17 = "CLASS_17_COLOSSO"
    const val CLASS_18 = "CLASS_18_HEROI"
    const val CLASS_19 = "CLASS_19_HEROI_ASCENDENTE"
    const val CLASS_20 = "CLASS_20_LENDA"
    const val CLASS_21 = "CLASS_21_ASCENDENTE"
    const val CLASS_22 = "CLASS_22_SEMIDEUS"

    val CLASSES: List<ClassDefinition> = listOf(
        ClassDefinition(
            classId = CLASS_01,
            order = 1,
            name = "01 Corpo Adormecido",
            description = "Estado inicial de repouso e adaptação basal.",
            requirementPolicyId = "POL-REQ-CLASS-01-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_02,
            order = 2,
            name = "02 Sobrevivente",
            description = "Primeiro estágio de engajamento físico ativo e resiliência inicial.",
            requirementPolicyId = "POL-REQ-CLASS-02-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_03,
            order = 3,
            name = "03 Desperto",
            description = "Consciência motora estabelecida e adaptações fisiológicas primárias registradas.",
            requirementPolicyId = "POL-REQ-CLASS-03-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_04,
            order = 4,
            name = "04 Iniciado",
            description = "Familiaridade com rotinas de avaliação e consistência postural comprovada.",
            requirementPolicyId = "POL-REQ-CLASS-04-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_05,
            order = 5,
            name = "05 Explorador",
            description = "Exploração de múltiplas valências físicas e expansão de capacidade aeróbica e de força.",
            requirementPolicyId = "POL-REQ-CLASS-05-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_06,
            order = 6,
            name = "06 Aprendiz",
            description = "Consolidação técnica sob protocolos padronizados de mensuração direta.",
            requirementPolicyId = "POL-REQ-CLASS-06-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_07,
            order = 7,
            name = "07 Discípulo",
            description = "Disciplina contínua e repetibilidade intra-individual estabelecida.",
            requirementPolicyId = "POL-REQ-CLASS-07-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_08,
            order = 8,
            name = "08 Atleta Emergente",
            description = "Capacidades biomotoras superando a média populacional em múltiplos testes validados.",
            requirementPolicyId = "POL-REQ-CLASS-08-V1",
            trialPolicyId = "TRIAL-POL-CLASS-08-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_09,
            order = 9,
            name = "09 Competidor",
            description = "Perfil atlético estruturado com capacidade de sustentar altas cargas de trabalho.",
            requirementPolicyId = "POL-REQ-CLASS-09-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_10,
            order = 10,
            name = "10 Atleta",
            description = "Nível atlético comprovado com histórico longitudinal consistente.",
            requirementPolicyId = "POL-REQ-CLASS-10-V1",
            trialPolicyId = "TRIAL-POL-CLASS-10-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_11,
            order = 11,
            name = "11 Especialista",
            description = "Excelência comprovada em dimensão de performance específica aliada à base sólida.",
            requirementPolicyId = "POL-REQ-CLASS-11-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_12,
            order = 12,
            name = "12 Predador Atlético",
            description = "Potência neuromuscular e capacidade de resposta rápida de alta magnitude.",
            requirementPolicyId = "POL-REQ-CLASS-12-V1",
            trialPolicyId = "TRIAL-POL-CLASS-12-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_13,
            order = 13,
            name = "13 Guerreiro",
            description = "Resistência avançada a fadiga e estabilidade biomecânica sob estresse máximo.",
            requirementPolicyId = "POL-REQ-CLASS-13-V1",
            trialPolicyId = null,
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_14,
            order = 14,
            name = "14 Gladiador",
            description = "Domínio de força máxima relativa e taxa de desenvolvimento de força de elite.",
            requirementPolicyId = "POL-REQ-CLASS-14-V1",
            trialPolicyId = "TRIAL-POL-CLASS-14-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_15,
            order = 15,
            name = "15 Campeão",
            description = "Performance multidimensional comprovada em nível de topo regional/nacional.",
            requirementPolicyId = "POL-REQ-CLASS-15-V1",
            trialPolicyId = "TRIAL-POL-CLASS-15-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_16,
            order = 16,
            name = "16 Titã",
            description = "Potência e robustez fisiológica de magnitude excepcional.",
            requirementPolicyId = "POL-REQ-CLASS-16-V1",
            trialPolicyId = "TRIAL-POL-CLASS-16-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_17,
            order = 17,
            name = "17 Colosso",
            description = "Capacidades de força e resistência que atingem o limiar superior da fisiologia humana.",
            requirementPolicyId = "POL-REQ-CLASS-17-V1",
            trialPolicyId = "TRIAL-POL-CLASS-17-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_18,
            order = 18,
            name = "18 Herói",
            description = "Excelência atlética consolidada com rastreabilidade longitudinal impecável.",
            requirementPolicyId = "POL-REQ-CLASS-18-V1",
            trialPolicyId = "TRIAL-POL-CLASS-18-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_19,
            order = 19,
            name = "19 Herói Ascendente",
            description = "Superação de marcos fisiológicos de elite em testes laboratoriais calibrados.",
            requirementPolicyId = "POL-REQ-CLASS-19-V1",
            trialPolicyId = "TRIAL-POL-CLASS-19-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_20,
            order = 20,
            name = "20 Lenda",
            description = "Status lendário fundamentado em evidências de confiabilidade Tier 1 ao longo de anos.",
            requirementPolicyId = "POL-REQ-CLASS-20-V1",
            trialPolicyId = "TRIAL-POL-CLASS-20-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_21,
            order = 21,
            name = "21 Ascendente",
            description = "Limiar transcendental de performance física com consistência absoluta comprovada.",
            requirementPolicyId = "POL-REQ-CLASS-21-V1",
            trialPolicyId = "TRIAL-POL-CLASS-21-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        ),
        ClassDefinition(
            classId = CLASS_22,
            order = 22,
            name = "22 Semideus",
            description = "Ápice supremo do potencial físico humano registrado por sensores diretos calibrados.",
            requirementPolicyId = "POL-REQ-CLASS-22-V1",
            trialPolicyId = "TRIAL-POL-CLASS-22-V1",
            version = "1.0.0",
            status = ClassStatus.ACTIVE
        )
    )

    private val classMap: Map<String, ClassDefinition> = CLASSES.associateBy { it.classId }

    fun getClassById(classId: String): ClassDefinition? = classMap[classId]

    fun getInitialClass(): ClassDefinition = CLASSES.first()

    fun getNextClass(currentClassId: String): ClassDefinition? {
        val current = getClassById(currentClassId) ?: return null
        return CLASSES.firstOrNull { it.order == current.order + 1 }
    }

    fun getPreviousClass(currentClassId: String): ClassDefinition? {
        val current = getClassById(currentClassId) ?: return null
        return CLASSES.firstOrNull { it.order == current.order - 1 }
    }
}
