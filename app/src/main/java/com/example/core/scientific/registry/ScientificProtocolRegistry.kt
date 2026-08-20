package com.example.core.scientific.registry

import com.example.core.scientific.model.ProtocolValidationStatus
import com.example.core.scientific.model.ScientificProtocol

/**
 * PERFORMAI SCIENTIFIC PROTOCOL REGISTRY
 *
 * Registrador central de protocolos científicos.
 * PROTOCOL VERSIONING: Protocolos são imutáveis e nunca sobrescritos.
 * Exemplo: PROT-VO2-RAMP-V1 e PROT-VO2-RAMP-V2 coexistem de forma independente.
 * Resultados históricos preservam a referência à versão sob a qual foram coletados.
 */
object ScientificProtocolRegistry {

    private val protocols: MutableMap<String, ScientificProtocol> = mutableMapOf()

    init {
        registerDefaultProtocols()
    }

    private fun registerDefaultProtocols() {
        // 1. VO2 Max - Protocolo Rampa em Cicloergômetro
        register(
            ScientificProtocol(
                protocolId = "PROT-VO2-RAMP-V1",
                name = "Protocolo Rampa Contínuo de Cicloergômetro para Determinação de VO2max",
                version = "1.0.0",
                methodologyId = "METH-VO2MAX-CPX-V1",
                metricId = "VO2_MAX",
                purpose = "Avaliação da capacidade aeróbia máxima e determinação de limiares ventilatórios em atletas.",
                preparationRequirements = listOf("Repouso de 24h sem esforço vigoroso", "Jejum de 2h", "Hidratação prévia 500ml de água"),
                equipmentRequirements = listOf("METABOLIC_CART", "CYCLE_ERGOMETER_ELECTROMAGNETIC", "HEART_RATE_SENSOR_ECG"),
                executionSteps = listOf(
                    "3 minutos de repouso basal pré-teste",
                    "3 minutos de aquecimento a 50W (cadência 70-80 rpm)",
                    "Incremento contínuo de 20-30W/minuto até exaustão voluntária",
                    "5 minutos de recuperação ativa a 30W"
                ),
                samplingRate = 1.0, // breath-by-breath interpolado a 1 Hz
                duration = 720, // ~12 minutos
                repetitions = 1,
                restInterval = 0,
                environmentalRequirements = listOf("Temperatura 18-22C", "Umidade relativa 40-60%"),
                exclusionCriteria = listOf("Hipertensão arterial não controlada (>180/110 mmHg)", "Sintomas cardiorrespiratórios agudos", "Lesão musculoesquelética limitante"),
                qualityRequirements = listOf("Critério de platô de VO2 ou 2 critérios secundários (RER > 1.10, FC > 90% máx predita)"),
                acceptedDevices = listOf("METABOLIC_CART", "LAB_CYCLE_ERGOMETER"),
                acceptedSources = listOf("LABORATORY_METABOLIC_CART", "VALIDATED_PORTABLE_SPIROMETER"),
                validationStatus = ProtocolValidationStatus.ACTIVE
            )
        )

        // 2. Força Isométrica Relativa - Isometric Mid-Thigh Pull (IMTP)
        register(
            ScientificProtocol(
                protocolId = "PROT-IMTP-FORCE-V1",
                name = "Protocolo Padronizado de Puxada Isométrica no Meio da Coxa (IMTP)",
                version = "1.0.0",
                methodologyId = "METH-REL-FORCE-IMTP-V1",
                metricId = "RELATIVE_FORCE",
                purpose = "Quantificação da força máxima isométrica e taxa de produção de força sem impacto dinâmico.",
                preparationRequirements = listOf("Aquecimento geral em ergômetro por 5 min", "3 contrações submáximas progressivas (50%, 75%, 90%)"),
                equipmentRequirements = listOf("FORCE_PLATE_DUAL_OR_SINGLE", "RIGID_BAR_RACK", "STRAPS_TO_AVOID_GRIP_LIMITATION"),
                executionSteps = listOf(
                    "Posicionar atleta com barra na altura do terço superior da coxa",
                    "Ajustar ângulo de joelho (125-145 graus) e quadril (140-150 graus)",
                    "Pré-tensão sem contramovimento",
                    "Comando verbal: 'Puxar o mais rápido e forte possível por 5 segundos'",
                    "Repouso de 2 a 3 minutos entre tentativas"
                ),
                samplingRate = 1000.0, // 1000 Hz
                duration = 5,
                repetitions = 3,
                restInterval = 120,
                environmentalRequirements = listOf("Superfície rígida nivelada"),
                exclusionCriteria = listOf("Dor articular aguda no joelho, quadril ou coluna"),
                qualityRequirements = listOf("Variação de força na linha de base < 10 N nos 500ms prévios ao início"),
                acceptedDevices = listOf("FORCE_PLATE", "CALIBRATED_LOAD_CELL"),
                acceptedSources = listOf("FORCE_PLATE", "CALIBRATED_LOAD_CELL"),
                validationStatus = ProtocolValidationStatus.ACTIVE
            )
        )

        // 3. RFD Isométrico
        register(
            ScientificProtocol(
                protocolId = "PROT-RFD-ISOM-V1",
                name = "Protocolo de Taxa de Desenvolvimento de Força Explosiva Isométrica",
                version = "1.0.0",
                methodologyId = "METH-RFD-ISOM-V1",
                metricId = "RFD",
                purpose = "Avaliação da capacidade neuromuscular de produzir força nos primeiros 50 a 200 ms.",
                preparationRequirements = listOf("Aquecimento neuromuscular de ativação balística", "Instrução enfática em explosão pura"),
                equipmentRequirements = listOf("FORCE_PLATE_HIGH_FREQ_1000HZ", "RIGID_FRAME"),
                executionSteps = listOf(
                    "Atleta imóvel na postura prescrita",
                    "Comando: 'Explodir instantaneamente no sinal sonoro'",
                    "Manter esforço máximo por 3 segundos",
                    "Realizar 3 tentativas válidas"
                ),
                samplingRate = 1000.0, // 1000 Hz
                duration = 3,
                repetitions = 3,
                restInterval = 180,
                environmentalRequirements = listOf("Isolamento de vibrações externas"),
                exclusionCriteria = listOf("Contramovimento detectado (queda prévia de força > 5N)"),
                qualityRequirements = listOf("Início abrupto sem pré-tensão excessiva"),
                acceptedDevices = listOf("FORCE_PLATE"),
                acceptedSources = listOf("FORCE_PLATE_1000HZ"),
                validationStatus = ProtocolValidationStatus.ACTIVE
            )
        )

        // 4. HRV em Repouso
        register(
            ScientificProtocol(
                protocolId = "PROT-HRV-REST-V1",
                name = "Protocolo Padronizado de Registro de VFC em Repouso Matinal",
                version = "1.0.0",
                methodologyId = "METH-HRV-RMSSD-V1",
                metricId = "HRV_RMSSD",
                purpose = "Quantificação do tônus autonômico parassimpático basal.",
                preparationRequirements = listOf("Bexiga vazia", "Sem ingestão de estimulantes ou refeições pesadas", "10 minutos após acordar"),
                equipmentRequirements = listOf("VALIDATED_CHEST_STRAP_HRM", "ECG_LEAD"),
                executionSteps = listOf(
                    "Atleta deita em decúbito dorsal em ambiente calmo",
                    "2 minutos de estabilização sem registro",
                    "5 minutos de registro contínuo de intervalos R-R em respiração espontânea",
                    "Filtragem e exclusão de artefatos (> 5% de ectopias invalida o registro)"
                ),
                samplingRate = 1000.0, // 1 ms resolução R-R
                duration = 300, // 5 minutos
                repetitions = 1,
                restInterval = 0,
                environmentalRequirements = listOf("Temperatura 20-24C", "Ambiente silencioso sem iluminação excessiva"),
                exclusionCriteria = listOf("Arritmias cardíacas conhecidas (fibrilação atrial)", "Uso de betabloqueadores"),
                qualityRequirements = listOf("Taxa de artefatos < 2% dos batimentos"),
                acceptedDevices = listOf("CHEST_STRAP_BLUETOOTH", "ECG_RECORDER"),
                acceptedSources = listOf("ECG", "CHEST_STRAP_BLUETOOTH", "VALIDATED_OPTICAL_HRV"),
                validationStatus = ProtocolValidationStatus.ACTIVE
            )
        )

        // 5. Critical Power - 3-Minute All-Out Test
        register(
            ScientificProtocol(
                protocolId = "PROT-CP-3MIN-V1",
                name = "Protocolo de Teste Máximo All-Out de 3 Minutos em Cicloergômetro",
                version = "1.0.0",
                methodologyId = "METH-CRITICAL-POWER-V1",
                metricId = "CRITICAL_POWER",
                purpose = "Determinação simultânea de Critical Power (CP) e Capacidade de Trabalho Anaeróbio (W').",
                preparationRequirements = listOf("Aquecimento de 5 min a 100W", "5 minutos de repouso pré-teste"),
                equipmentRequirements = listOf("CYCLE_ERGOMETER_ISOKINETIC_OR_LINEAR_RESISTANCE"),
                executionSteps = listOf(
                    "Acelerar até a cadência máxima nos 5 segundos finais da contagem regressiva",
                    "Aplicação imediata da resistência linear calculada com base na massa corporal",
                    "Manter esforço MÁXIMO absoluto durante todos os 180 segundos sem poupar ritmo",
                    "A média dos últimos 30 segundos define a Potência Crítica (CP)",
                    "A integral da potência acima de CP define W'"
                ),
                samplingRate = 10.0, // 10 Hz
                duration = 180,
                repetitions = 1,
                restInterval = 0,
                environmentalRequirements = listOf("Ventilação forçada adequada"),
                exclusionCriteria = listOf("Ritmo conservado intencionalmente no início do teste (queda de cadência < 20 rpm invalida perfil all-out)"),
                qualityRequirements = listOf("Perfil de potência decrescente característico com platô terminal claro"),
                acceptedDevices = listOf("CYCLE_ERGOMETER_ELECTROMAGNETIC"),
                acceptedSources = listOf("LAB_CYCLE_ERGOMETER", "DUAL_SIDED_POWER_PEDALS"),
                validationStatus = ProtocolValidationStatus.ACTIVE
            )
        )

        // 6. ROM Goniométrico
        register(
            ScientificProtocol(
                protocolId = "PROT-ROM-GONIO-V1",
                name = "Protocolo de Avaliação de Amplitude de Movimento Ativa e Passiva",
                version = "1.0.0",
                methodologyId = "METH-ROM-GONIO-V1",
                metricId = "ROM",
                purpose = "Determinação de amplitude angular máxima em graus para articulações-chave.",
                preparationRequirements = listOf("Palpação e marcação de marcos ósseos de referência"),
                equipmentRequirements = listOf("DIGITAL_GONIOMETER", "INCLINOMETER"),
                executionSteps = listOf(
                    "Posicionar o braço fixo do goniômetro paralelo ao eixo proximal",
                    "Alinhar o fulcro com o eixo articular",
                    "Posicionar o braço móvel paralelo ao eixo distal",
                    "Executar movimento lento e contínuo até a barreira motora final",
                    "Registrar ângulo em 3 repetições consecutivas"
                ),
                samplingRate = 1.0,
                duration = 10,
                repetitions = 3,
                restInterval = 30,
                environmentalRequirements = listOf("Maca rígida de avaliação"),
                exclusionCriteria = listOf("Dor aguda inflamatória no arco de movimento"),
                qualityRequirements = listOf("Repetibilidade entre medidas < 3 graus"),
                acceptedDevices = listOf("DIGITAL_GONIOMETER", "OPTICAL_MOTION_ANALYSIS"),
                acceptedSources = listOf("DIGITAL_GONIOMETER", "CALIBRATED_VIDEO_PHOTOGRAMMETRY", "IMU_SENSOR"),
                validationStatus = ProtocolValidationStatus.ACTIVE
            )
        )

        // 7. VBT - Velocidade Propulsiva Média
        register(
            ScientificProtocol(
                protocolId = "PROT-VBT-PROP-V1",
                name = "Protocolo de Treinamento Baseado em Velocidade (VBT) com Encoder Linear",
                version = "1.0.0",
                methodologyId = "METH-VBT-MPV-V1",
                metricId = "MEAN_PROPULSIVE_VELOCITY",
                purpose = "Monitoramento da velocidade propulsiva média concêntrica para estimativa de prontidão e 1RM.",
                preparationRequirements = listOf("Fixação vertical perpendicular do cabo do encoder na barra"),
                equipmentRequirements = listOf("LINEAR_POSITION_TRANSDUCER"),
                executionSteps = listOf(
                    "Posicionar o atleta com a carga prescrita",
                    "Fase excêntrica controlada (2 segundos)",
                    "Pausa isométrica de 1s na transição (para evitar aproveitamento elástico se teste for estrito)",
                    "Fase concêntrica explosiva à máxima velocidade intencional",
                    "Descarte de repetições com inclinação do cabo > 5 graus"
                ),
                samplingRate = 500.0, // 500 Hz
                duration = 5,
                repetitions = 3,
                restInterval = 90,
                environmentalRequirements = listOf("Barra guiada ou pesos livres com plano de movimento calibrado"),
                exclusionCriteria = listOf("Desaceleração voluntária antes do ponto propulsivo"),
                qualityRequirements = listOf("Fase propulsiva identificada com aceleração >= -9.81 m/s²"),
                acceptedDevices = listOf("LINEAR_ENCODER", "HIGH_SPEED_OPTICAL_TRACKER"),
                acceptedSources = listOf("LINEAR_ENCODER", "HIGH_SPEED_OPTICAL_TRACKER"),
                validationStatus = ProtocolValidationStatus.ACTIVE
            )
        )

        // 8. Aceleração em Sprint 10m
        register(
            ScientificProtocol(
                protocolId = "PROT-ACCEL-10M-V1",
                name = "Protocolo de Aceleração Linear de 10 Metros com Fotocélulas Duplas",
                version = "1.0.0",
                methodologyId = "METH-ACCELERATION-SPRINT-V1",
                metricId = "ACCELERATION",
                purpose = "Quantificação da capacidade de aceleração inicial a partir de posição estática.",
                preparationRequirements = listOf("Aquecimento de corrida e drills balísticos de aceleração"),
                equipmentRequirements = listOf("DUAL_BEAM_TIMING_GATES", "MEASURING_TAPE_LASER"),
                executionSteps = listOf(
                    "Instalar fotocélulas nas marcas de 0m (ou largada a 50cm) e 10m na altura de 0.8m a 1.0m",
                    "Atleta assume postura de largada em pé estática com pé dianteiro na linha de 0.5m",
                    "Largada espontânea sem sinal sonoro para evitar tempo de reação",
                    "Sprint máximo cruzando a linha de 10m",
                    "Realizar 3 tentativas com 3 minutos de recuperação"
                ),
                samplingRate = 1000.0,
                duration = 5,
                repetitions = 3,
                restInterval = 180,
                environmentalRequirements = listOf("Pista de atletismo ou quadra antiderrapante", "Sem vento contrário > 2m/s"),
                exclusionCriteria = listOf("Falso disparo da célula de largada por movimento de braço"),
                qualityRequirements = listOf("Melhor tempo com validação de disparo do tronco"),
                acceptedDevices = listOf("DUAL_BEAM_TIMING_GATES", "DOPPLER_RADAR"),
                acceptedSources = listOf("DUAL_BEAM_TIMING_GATES", "DOPPLER_RADAR"),
                validationStatus = ProtocolValidationStatus.ACTIVE
            )
        )
    }

    fun register(protocol: ScientificProtocol) {
        protocols[protocol.protocolId] = protocol
    }

    fun getProtocol(protocolId: String): ScientificProtocol? {
        return protocols[protocolId]
    }

    fun getProtocolsForMetric(metricId: String): List<ScientificProtocol> {
        return protocols.values.filter { it.metricId == metricId }
    }

    fun getProtocolsForMethodology(methodologyId: String): List<ScientificProtocol> {
        return protocols.values.filter { it.methodologyId == methodologyId }
    }

    fun getAllProtocols(): List<ScientificProtocol> {
        return protocols.values.toList()
    }

    fun containsProtocol(protocolId: String): Boolean {
        return protocols.containsKey(protocolId)
    }
}
