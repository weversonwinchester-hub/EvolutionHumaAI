package com.example.core.datacore.metrics

import com.example.core.datacore.model.DataCoreMetric
import com.example.core.datacore.model.MetricExplainability

/**
 * MetricCatalog: Registro central de métricas científicas suportadas no DATA CORE V1.
 *
 * Cada métrica possui identidade própria, definição rigorosa, protocolos associados
 * e uma estrutura completa de Explainability Sheet (O que é, Como é medida, Unidade,
 * Protocolo, Instrumento/Sensor, Fonte, Método de cálculo, Versão e Evidência).
 */
object MetricCatalog {

    const val METRIC_VO2_MAX = "METRIC_VO2_MAX"
    const val METRIC_RELATIVE_STRENGTH = "METRIC_RELATIVE_STRENGTH"
    const val METRIC_RFD = "METRIC_RFD"
    const val METRIC_HRV_RMSSD = "METRIC_HRV_RMSSD"
    const val METRIC_CRITICAL_POWER = "METRIC_CRITICAL_POWER"
    const val METRIC_W_PRIME = "METRIC_W_PRIME"
    const val METRIC_JOINT_STABILITY = "METRIC_JOINT_STABILITY"
    const val METRIC_ROM = "METRIC_ROM"
    const val METRIC_VELOCITY = "METRIC_VELOCITY"
    const val METRIC_ACCELERATION = "METRIC_ACCELERATION"

    val VO2_MAX = DataCoreMetric(
        id = METRIC_VO2_MAX,
        name = "VO2 Max Estimado",
        definition = "Volume máximo de oxigênio que o organismo capta, transporta e utiliza por quilograma de peso corporal por minuto sob demanda máxima.",
        unit = "ml/kg/min",
        category = "PHYSIOLOGICAL_AEROBIC",
        calculationMethod = "Regressão linear de frequência cardíaca vs potência submáxima com extrapolação para FC máx teórica por idade.",
        protocolIds = listOf("PROTO_BASELINE_SUBMAX_VO2", "PROTO_RAMP_CYCLE_ERGOMETER"),
        evidenceRequirements = "Mínimo de 3 estágios submáximos estáveis (>= 3 min cada) com desvio padrão de FC < 3 bpm.",
        explainability = MetricExplainability(
            whatIsIt = "Volume máximo de consumo de oxigênio por quilograma de peso corporal por minuto.",
            howIsMeasured = "Medido via teste ergométrico submáximo com monitoramento eletrocardiográfico contínuo ou sensor óptico calibrado.",
            unit = "ml/kg/min",
            protocol = "Protocolo Submáximo Conconi / Astrand em Cicloergômetro",
            instrumentOrSensor = "Sensor Cardíaco ECG de Cinta Torácica (ex: Polar H10) ou Analisador Metabólico de Gases",
            source = "Sinal BLE sincronizado em tempo real com carimbo de tempo autenticado",
            howIsCalculated = "Extrapolação da curva linear FC-Potência até a FC máxima predita pela fórmula de Gellish (207 - 0.7 * idade).",
            methodVersion = "Method-VO2-v1.0",
            evidenceRequirement = "Série temporal contínua de FC e potência de carga com Provenance de sensor verificado.",
            physiologicalBasis = "Expressa a capacidade integrada dos sistemas cardiovascular, respiratório e mitocondrial muscular.",
            performanceImpact = "Determina o teto aeróbio, resistência à fadiga em esforços prolongados e velocidade de recuperação fosfogênica."
        ),
        version = "1.0.0"
    )

    val RELATIVE_STRENGTH = DataCoreMetric(
        id = METRIC_RELATIVE_STRENGTH,
        name = "Força Dinâmica Relativa (1RM/BW)",
        definition = "Razão entre a força máxima concêntrica (1 Repetição Máxima estimada ou direta) e o peso corporal do atleta.",
        unit = "1RM/BW ratio",
        category = "NEUROMUSCULAR_FORCE",
        calculationMethod = "Carga máxima (kg) dividida pela massa corporal total (kg) auditada no perfil.",
        protocolIds = listOf("PROTO_LPT_RELATIVE_STRENGTH", "PROTO_PROGRESSIVE_LOAD_1RM"),
        evidenceRequirements = "Registro de deslocamento vertical por transdutor linear com velocidade média concêntrica >= 0.15 m/s.",
        explainability = MetricExplainability(
            whatIsIt = "Proporção de força máxima exercida normalizada pela massa corporal total.",
            howIsMeasured = "Transdutor linear de posição e velocidade medindo deslocamento da barra durante teste progressivo com carga.",
            unit = "1RM/BW ratio",
            protocol = "Protocolo de Carga Progressiva com Encoder Linear de Alta Frequência",
            instrumentOrSensor = "Transdutor Linear de Posição (LPT 1000Hz) ou Plataforma Óptica de Deslocamento",
            source = "Telemetria direta de encoder via interface de telemetria serial/BLE",
            howIsCalculated = "Carga de 1RM estimada pela curva Força-Velocidade linear dividida pela massa corporal.",
            methodVersion = "Method-RelStrength-v1.0",
            evidenceRequirement = "Sinal cinemático de velocidade média concêntrica com carimbo temporal e curva completa da repetição.",
            physiologicalBasis = "Reflete o recrutamento de unidades motoras rápidas tipo IIx e eficiência de alavanca biomecânica sem massa inerte parasita.",
            performanceImpact = "Preditivo fundamental de aceleração linear, capacidade de frenagem excêntrica e salto vertical."
        ),
        version = "1.0.0"
    )

    val RFD = DataCoreMetric(
        id = METRIC_RFD,
        name = "Taxa de Desenvolvimento de Força (RFD)",
        definition = "Gradiente de força mecânica produzida por unidade de tempo nos primeiros 100-200 milissegundos após o início da contração.",
        unit = "N/s",
        category = "NEUROMUSCULAR_EXPLOSION",
        calculationMethod = "Derivada temporal da força no intervalo de 0 a 100ms e 0 a 200ms: ΔForça / ΔTempo.",
        protocolIds = listOf("PROTO_IMTP_RFD"),
        evidenceRequirements = "Plataforma de força biaxial com taxa de amostragem mínima de 1000 Hz e linha de base estável por 2 segundos antes do disparo.",
        explainability = MetricExplainability(
            whatIsIt = "Capacidade neuromuscular de produzir o maior pico de força no menor intervalo de tempo possível.",
            howIsMeasured = "Teste de Tração Isométrica em Meio da Coxa (IMTP) sobre plataforma de força calibrada.",
            unit = "N/s (Newtons por segundo)",
            protocol = "Protocolo Padronizado IMTP (Isometric Mid-Thigh Pull)",
            instrumentOrSensor = "Plataforma de Força Isométrica Biaxial (Amostragem >= 1000Hz)",
            source = "Transmissão digital direta dos canais de célula de carga",
            howIsCalculated = "Cálculo da inclinação angular média (ΔF/Δt) da curva força-tempo nos primeiros 100ms após o limiar de 5SD da linha de base.",
            methodVersion = "Method-RFD-v1.0",
            evidenceRequirement = "Curva força-tempo bruta de alta resolução com hash de integridade e calibragem zero confirmada.",
            physiologicalBasis = "Taxa de disparo inicial das unidades motoras (frequência de condução de potenciais de ação) e rigidez da junção miotendínea.",
            performanceImpact = "Capacidade de aceleração explosiva instantânea, mudanças rápidas de direção e impactos elásticos."
        ),
        version = "1.0.0"
    )

    val HRV_RMSSD = DataCoreMetric(
        id = METRIC_HRV_RMSSD,
        name = "Variabilidade Cardíaca (HRV rMSSD)",
        definition = "Raiz quadrada da média das diferenças quadráticas entre intervalos R-R sucessivos no sinal eletrocardiográfico.",
        unit = "ms",
        category = "AUTONOMIC_RECOVERY",
        calculationMethod = "sqrt( (1/N) * sum( (RR[i+1] - RR[i])^2 ) ) após filtragem de batimentos ectópicos.",
        protocolIds = listOf("PROTO_RESTING_HRV_RMSSD"),
        evidenceRequirements = "Registro estático de 5 minutos em repouso supino matinal com menos de 2% de artefatos de sinal.",
        explainability = MetricExplainability(
            whatIsIt = "Métrica no domínio do tempo que quantifica a modulação parassimpática (tônus vagal) sobre o nó sinoatrial.",
            howIsMeasured = "Intervalos entre picos R (onda R do complexo QRS) medidos continuamente por sensor torácico de ECG.",
            unit = "ms (milissegundos)",
            protocol = "Protocolo Padronizado de Repouso Matinal de 5 Minutos (Supino/Sentado)",
            instrumentOrSensor = "Sensor Eletrocardiográfico Torácico com precisão de 1ms de resolução R-R",
            source = "Sinal BLE bruto de intervalo R-R com identificador de dispositivo",
            howIsCalculated = "Aplicação do filtro de correção Kubios/Task Force sobre os intervalos inter-batimentos seguido pelo cálculo do rMSSD.",
            methodVersion = "Method-HRV-v1.0",
            evidenceRequirement = "Vetor de intervalos R-R brutos com relatório de ruído de artefato e hora de captura pós-despertar.",
            physiologicalBasis = "Ativação do nervo vago indicando prontidão adaptativa do sistema nervoso autônomo e recuperação homeostática.",
            performanceImpact = "Sinalizador biológico para modulação de volume/intensidade de treino e prevenção de overtraining sistêmico."
        ),
        version = "1.0.0"
    )

    val CRITICAL_POWER = DataCoreMetric(
        id = METRIC_CRITICAL_POWER,
        name = "Critical Power (CP)",
        definition = "Limite assintótico de potência sustentável em estado fisiológico estável sem exaustão metabólica progressiva.",
        unit = "W",
        category = "PHYSIOLOGICAL_METABOLIC",
        calculationMethod = "Modelo hiperbólico linear de Potência-Tempo baseado em múltiplos esforços exaustivos (ou teste 3-min All-Out).",
        protocolIds = listOf("PROTO_CRITICAL_POWER_3MIN_ALL_OUT", "PROTO_MULTI_TRIAL_CP"),
        evidenceRequirements = "Registro de potência a 1 Hz com esforço total confirmado por queda assintótica e FC próxima à máxima.",
        explainability = MetricExplainability(
            whatIsIt = "Taxa máxima de trabalho metabólico que pode ser mantida sem depleção contínua da capacidade de trabalho anaeróbio.",
            howIsMeasured = "Cicloergômetro ou medidor de potência direta com freio eletromagnético.",
            unit = "W (Watts)",
            protocol = "Protocolo 3-Minutos All-Out Test (3MT)",
            instrumentOrSensor = "Medidor de Potência Direto Calibrado (strain gauge no pedivela/cubo)",
            source = "Canal de telemetria de potência ANT+/BLE",
            howIsCalculated = "Média da potência nos últimos 30 segundos do teste 3MT (Watts) = Critical Power.",
            methodVersion = "Method-CP-v1.0",
            evidenceRequirement = "Curva contínua segundo a segundo de potência e cadência com certificação de calibração zero.",
            physiologicalBasis = "Fronteira entre os domínios de intensidade pesada e severa; saturação de oxigenação muscular no ponto crítico.",
            performanceImpact = "Determina o ritmo ótimo de sustentabilidade máxima em competições de média e longa duração."
        ),
        version = "1.0.0"
    )

    val W_PRIME = DataCoreMetric(
        id = METRIC_W_PRIME,
        name = "Capacidade de Trabalho Anaeróbio (W')",
        definition = "Quantidade finita de trabalho mecânico que pode ser realizado em intensidades estritamente superiores ao Critical Power.",
        unit = "kJ",
        category = "PHYSIOLOGICAL_ANAEROBIC",
        calculationMethod = "Integral do excesso de potência acima do Critical Power: Integral((P(t) - CP) dt) até a exaustão.",
        protocolIds = listOf("PROTO_CRITICAL_POWER_3MIN_ALL_OUT"),
        evidenceRequirements = "Integral da área de trabalho acima do platô assintótico dos últimos 30 segundos do teste 3MT.",
        explainability = MetricExplainability(
            whatIsIt = "Reserva fixa de energia anaeróbia disponível para esforços acima da intensidade sustentável.",
            howIsMeasured = "Calculado a partir da área supramáxima durante o teste de potência crítica all-out de 3 minutos.",
            unit = "kJ (quilojoules)",
            protocol = "Protocolo 3-Minutos All-Out Test (3MT)",
            instrumentOrSensor = "Medidor de Potência Direto Calibrado",
            source = "Série temporal de potência integrada do sensor de força",
            howIsCalculated = "Soma cumulativa de (P(t) - CP) * 1 segundo ao longo dos primeiros 150 segundos do teste.",
            methodVersion = "Method-WPrime-v1.0",
            evidenceRequirement = "Série temporal de esforço máximo supralimiar com confirmação de esgotamento total.",
            physiologicalBasis = "Depleção de fosfocreatina (PCr), acúmulo de íons H+ e glicólise anaeróbia intramuscular.",
            performanceImpact = "Capacidade de responder a ataques, sprints decisivos e subidas de alta inclinação antes da fadiga."
        ),
        version = "1.0.0"
    )

    val JOINT_STABILITY = DataCoreMetric(
        id = METRIC_JOINT_STABILITY,
        name = "Estabilidade Articular & Simetria",
        definition = "Consistência e controle cinemático do alinhamento articular durante ciclos motores repetidos sob carga dinâmica.",
        unit = "%",
        category = "BIOMECHANICAL_CONTROL",
        calculationMethod = "100 - (Coeficiente de Variação da trajetória angular * 100 + Desvio de Assimetria Bilateral).",
        protocolIds = listOf("PROTO_BIOMECHANICAL_ROM_STABILITY"),
        evidenceRequirements = "Captura cinemática em 100 Hz de no mínimo 5 repetições consecutivas.",
        explainability = MetricExplainability(
            whatIsIt = "Grau de consistência neuromuscular e simetria bilateral no controle de trajetórias articulares.",
            howIsMeasured = "Sensores inerciais (IMU 9-DOF) ou visão computacional com detecção de marcadores anatômicos.",
            unit = "% (Porcentagem de estabilidade / simetria)",
            protocol = "Protocolo de Avaliação Cinemática Biomecânica Dinâmica",
            instrumentOrSensor = "Sensor Inercial IMU ou Sistema de Visão Cinemática de Alta Taxa (100Hz)",
            source = "Stream vetorial de rotação e aceleração linear em 3 eixos",
            howIsCalculated = "Cálculo da variância angular nos pontos de inversão de fase e comparação de torque bilateral.",
            methodVersion = "Method-Stability-v1.0",
            evidenceRequirement = "Registro vetorial contínuo de trajetória angular articular com validação de ruído.",
            physiologicalBasis = "Coativação de músculos estabilizadores profundos e propriocepção dos mecanorreceptores articulares.",
            performanceImpact = "Prevenção direta de lesões por sobrecarga assimétrica e transferência de força eficiente."
        ),
        version = "1.0.0"
    )

    val ROM = DataCoreMetric(
        id = METRIC_ROM,
        name = "Amplitude de Movimento Articular (ROM)",
        definition = "Ângulo total percorrido por uma articulação entre a posição de flexão/extensão máxima durante o gesto motor.",
        unit = "°",
        category = "BIOMECHANICAL_MOBILITY",
        calculationMethod = "Diferença angular absoluta: max(angulo) - min(angulo) ao longo do ciclo de repetição.",
        protocolIds = listOf("PROTO_BIOMECHANICAL_ROM_STABILITY"),
        evidenceRequirements = "Goniometria digital ou rastreamento inercial calibrado com resolução de 0.5 grau.",
        explainability = MetricExplainability(
            whatIsIt = "Extensão angular do arco percorrido pela articulação.",
            howIsMeasured = "Sensor inercial (IMU) ou goniômetro digital fixado nos eixos segmentares.",
            unit = "° (graus angulares)",
            protocol = "Protocolo de Goniometria e Cinemática Dinâmica",
            instrumentOrSensor = "Sensor Inercial IMU / Goniômetro Digital Calibrado",
            source = "Quatérnions de rotação espacial convertidos para ângulos de Euler",
            howIsCalculated = "Diferença entre o ângulo máximo e mínimo sustentados por >= 100ms no ciclo.",
            methodVersion = "Method-ROM-v1.0",
            evidenceRequirement = "Série temporal de ângulos articulares calibrada no plano anatômico correto.",
            physiologicalBasis = "Flexibilidade miotendínea, conformação capsular articular e ausência de bloqueios mecânicos.",
            performanceImpact = "Permite produção de força através de arcos motores mais amplos e eficientes."
        ),
        version = "1.0.0"
    )

    val VELOCITY = DataCoreMetric(
        id = METRIC_VELOCITY,
        name = "Velocidade Média/Pico Propulsiva",
        definition = "Velocidade linear ou angular desenvolvida durante a fase propulsiva concêntrica do movimento.",
        unit = "m/s",
        category = "BIOMECHANICAL_VELOCITY",
        calculationMethod = "Derivada do deslocamento em relação ao tempo na fase onde a aceleração é >= -g.",
        protocolIds = listOf("PROTO_LPT_RELATIVE_STRENGTH", "PROTO_BIOMECHANICAL_ROM_STABILITY"),
        evidenceRequirements = "Sinal de encoder linear ou acelerometria integrada com amostragem >= 200 Hz.",
        explainability = MetricExplainability(
            whatIsIt = "Taxa de variação de posição do implemento ou segmento corporal durante a fase ativa de aceleração.",
            howIsMeasured = "Encoder óptico linear de cabo ou sensor inercial de alta precisão.",
            unit = "m/s (metros por segundo)",
            protocol = "Protocolo de Medição de Velocidade Baseada em Deslocamento (VBT)",
            instrumentOrSensor = "Transdutor Linear de Posição / Velocímetro Óptico",
            source = "Transmissão digital instantânea do encoder",
            howIsCalculated = "Média aritmética das velocidades instantâneas amostradas durante a fase concêntrica propulsiva.",
            methodVersion = "Method-Velocity-v1.0",
            evidenceRequirement = "Curva contínua de velocidade-tempo da repetição com identificação dos pontos de transição de fase.",
            physiologicalBasis = "Velocidade de ciclagem de pontes cruzadas actina-miosina e transmissão neural.",
            performanceImpact = "Regulador mestre para treinamento de força baseado em velocidade (VBT) e controle de fadiga aguda."
        ),
        version = "1.0.0"
    )

    val ACCELERATION = DataCoreMetric(
        id = METRIC_ACCELERATION,
        name = "Aceleração Instantânea & Pico",
        definition = "Taxa de variação temporal da velocidade durante o ciclo propulsivo motor.",
        unit = "m/s²",
        category = "BIOMECHANICAL_KINEMATICS",
        calculationMethod = "Derivada temporal da velocidade instantânea: dv/dt ou leitura direta do acelerômetro triaxial compensada pela gravidade.",
        protocolIds = listOf("PROTO_BIOMECHANICAL_ROM_STABILITY"),
        evidenceRequirements = "Leitura de acelerômetro triaxial com escala dinâmica mínima de ±16g e filtro passa-baixa Butterworth.",
        explainability = MetricExplainability(
            whatIsIt = "Intensidade da variação rápida de velocidade produzida pela aplicação de força externa e interna.",
            howIsMeasured = "Acelerômetro MEMS triaxial calibrado fixado no segmento corporal ou implemento.",
            unit = "m/s² (metros por segundo ao quadrado)",
            protocol = "Protocolo de Acelerometria Cinemática Triaxial",
            instrumentOrSensor = "Acelerômetro MEMS Triaxial de Baixo Ruído (100-1000Hz)",
            source = "Sinal bruto de aceleração calibrado em 3 eixos (X, Y, Z)",
            howIsCalculated = "Vetor magnitude Euclidiana sqrt(ax^2 + ay^2 + az^2) com subtração do vetor gravitacional estático.",
            methodVersion = "Method-Accel-v1.0",
            evidenceRequirement = "Sinal temporal de aceleração em 3 eixos com carimbo de tempo e calibração estática comprovada.",
            physiologicalBasis = "Capacidade de geração rápida de tensão e superação da inércia mecânica da massa corporal.",
            performanceImpact = "Capacidade de partida explosiva, impulsão e absorção de choques em desacelerações abruptas."
        ),
        version = "1.0.0"
    )

    private val METRICS_MAP: Map<String, DataCoreMetric> = listOf(
        VO2_MAX,
        RELATIVE_STRENGTH,
        RFD,
        HRV_RMSSD,
        CRITICAL_POWER,
        W_PRIME,
        JOINT_STABILITY,
        ROM,
        VELOCITY,
        ACCELERATION
    ).associateBy { it.id }

    fun getMetricById(id: String): DataCoreMetric? = METRICS_MAP[id]

    fun getAllMetrics(): List<DataCoreMetric> = METRICS_MAP.values.toList()

    fun getExplainability(metricId: String): MetricExplainability? = METRICS_MAP[metricId]?.explainability
}
