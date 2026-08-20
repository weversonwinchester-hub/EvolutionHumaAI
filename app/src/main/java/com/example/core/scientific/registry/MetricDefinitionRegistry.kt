package com.example.core.scientific.registry

import com.example.core.scientific.model.EvidenceLevel
import com.example.core.scientific.model.MethodologySource
import com.example.core.scientific.model.MethodologyValidationStatus
import com.example.core.scientific.model.MetricDefinition

/**
 * PERFORMAI METRIC DEFINITION REGISTRY
 *
 * Fichas estruturadas completas das 11 métricas centrais do Core.
 * Cada métrica possui:
 * - O QUE É
 * - COMO É MEDIDA
 * - UNIDADE
 * - INSTRUMENTO
 * - PROTOCOLO
 * - MÉTODO DE CÁLCULO
 * - FREQUÊNCIA DE CAPTURA
 * - CONDIÇÕES
 * - LIMITAÇÕES
 * - FONTES
 * - VERSÃO METODOLÓGICA
 * - NÍVEL DE EVIDÊNCIA
 */
object MetricDefinitionRegistry {

    private val metrics: MutableMap<String, MetricDefinition> = mutableMapOf()

    init {
        registerDefaultMetricDefinitions()
    }

    private fun registerDefaultMetricDefinitions() {
        // 1. VO2 Max
        register(
            MetricDefinition(
                metricId = "VO2_MAX",
                name = "Consumo Máximo de Oxigênio (VO2 Max)",
                category = "CARDIORESPIRATORY",
                whatItIs = "Taxa máxima de oxigênio que o organismo consegue captar, transportar e utilizar durante exercício aeróbio exaustivo.",
                howItIsMeasured = "Medição contínua breath-by-breath das frações expiradas de O2 e CO2 e ventilação minuto durante teste incremental máximo.",
                standardUnit = "ml/kg/min",
                acceptableUnits = listOf("ml/kg/min", "L/min"),
                primaryInstruments = listOf("INST-METABOLIC-CART", "INST-CYCLE-ERGOMETER"),
                referenceProtocols = listOf("PROT-VO2-RAMP-V1"),
                calculationMethod = "VO2 = VE * (FiO2 - FeO2); critério de platô (< 150 ml/min com aumento de carga).",
                captureFrequency = "Trimestral ou Semestral",
                requiredConditions = listOf("Jejum de 2-3h", "Ambiente controlado 20-22C", "Sem exercício vigoroso nas 24h anteriores"),
                limitations = listOf("Custo laboratorial elevado", "Exige motivação máxima do atleta até a exaustão volitiva"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-ACSM-2021",
                        title = "ACSM's Guidelines for Exercise Testing and Prescription",
                        authors = listOf("American College of Sports Medicine"),
                        publicationYear = 2021,
                        publicationType = "CLINICAL_GUIDELINE",
                        identifier = "ISBN: 978-1975150181",
                        sourceAuthority = "ACSM",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 2. Relative Force
        register(
            MetricDefinition(
                metricId = "RELATIVE_FORCE",
                name = "Força Isométrica Relativa",
                category = "NEUROMUSCULAR",
                whatItIs = "Pico de força isométrica máxima produzido dividido pela massa corporal total do atleta.",
                howItIsMeasured = "Registro da componente vertical da força de reação do solo (Fz) durante o Isometric Mid-Thigh Pull (IMTP).",
                standardUnit = "N/kg",
                acceptableUnits = listOf("N/kg", "ratio"),
                primaryInstruments = listOf("INST-FORCE-PLATE-1000HZ"),
                referenceProtocols = listOf("PROT-IMTP-FORCE-V1"),
                calculationMethod = "Peak Force (N) / Body Mass (kg)",
                captureFrequency = "Mensal ou Bimestral",
                requiredConditions = listOf("Aquecimento neuromuscular", "Barra rígida fixada sem folga", "Ângulo de joelho 125-145 graus"),
                limitations = listOf("Específico ao ângulo articular do teste", "Sensível ao uso de straps e pegada"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-COMFORT-2019",
                        title = "Methods of evaluating isometric mid-thigh pull performance",
                        authors = listOf("Comfort P", "Dos'Santos T", "Beckham GK", "Stone MH"),
                        publicationYear = 2019,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1519/SSC.0000000000000433",
                        sourceAuthority = "NSCA",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 3. RFD (Rate of Force Development)
        register(
            MetricDefinition(
                metricId = "RFD",
                name = "Taxa de Desenvolvimento de Força (RFD)",
                category = "NEUROMUSCULAR_EXPLOSIVE",
                whatItIs = "Velocidade com que a força muscular é desenvolvida no início da contração voluntária (capacidade explosiva pura).",
                howItIsMeasured = "Derivada temporal da força registrada a >= 1000 Hz nos intervalos de 0-50ms, 0-100ms e 0-200ms a partir do início da contração.",
                standardUnit = "N/s",
                acceptableUnits = listOf("N/s"),
                primaryInstruments = listOf("INST-FORCE-PLATE-1000HZ"),
                referenceProtocols = listOf("PROT-RFD-ISOM-V1"),
                calculationMethod = "RFD = (F_t - F_onset) / delta_t",
                captureFrequency = "Mensal",
                requiredConditions = listOf("Onset identificado estritamente por threshold de ruído", "Sem contramovimento prévio"),
                limitations = listOf("Extremamente sensível ao algoritmo de detecção do onset e frequência de amostragem"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-AAGAARD-2002",
                        title = "Increased rate of force development and neural drive of human skeletal muscle",
                        authors = listOf("Aagaard P", "Simonsen EB", "Andersen JL", "Magnusson P", "Halkjaer-Kristensen J"),
                        publicationYear = 2002,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1152/japplphysiol.00283.2002",
                        sourceAuthority = "Journal of Applied Physiology",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 4. HRV rMSSD
        register(
            MetricDefinition(
                metricId = "HRV_RMSSD",
                name = "VFC rMSSD (Variabilidade da Frequência Cardíaca)",
                category = "AUTONOMIC_RECOVERY",
                whatItIs = "Marcador autonômico de modulação parassimpática cardíaca baseado na variabilidade entre batimentos normais sucessivos.",
                howItIsMeasured = "Registro contínuo de intervalos R-R em repouso por 5 minutos com remoção de artefatos ectópicos.",
                standardUnit = "ms",
                acceptableUnits = listOf("ms"),
                primaryInstruments = listOf("INST-HEART-RATE-CHEST-STRAP"),
                referenceProtocols = listOf("PROT-HRV-REST-V1"),
                calculationMethod = "rMSSD = sqrt( (1 / (N - 1)) * sum( (RR_{i+1} - RR_i)^2 ) )",
                captureFrequency = "Diária (Repouso Matinal)",
                requiredConditions = listOf("Repouso de 5 min pré-registro", "Sem cafeína aguda", "Respiração espontânea calma"),
                limitations = listOf("Influenciado por fatores externos não-físicos como sono, estresse emocional e temperatura"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-TASKFORCE-1996",
                        title = "Heart rate variability: standards of measurement",
                        authors = listOf("Task Force of ESC and NASPE"),
                        publicationYear = 1996,
                        publicationType = "CONSENSUS_STATEMENT",
                        identifier = "DOI: 10.1161/01.CIR.93.5.1043",
                        sourceAuthority = "Circulation",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 5. Critical Power
        register(
            MetricDefinition(
                metricId = "CRITICAL_POWER",
                name = "Potência Crítica (Critical Power - CP)",
                category = "BIOENERGETICS",
                whatItIs = "Taxa de trabalho sustentável no estado estável metabólico, separando os domínios de intensidade pesado e severo.",
                howItIsMeasured = "Teste All-Out de 3 minutos ou regressão linear/hiperbólica de múltiplos esforços exaustivos (2 a 15 min).",
                standardUnit = "W",
                acceptableUnits = listOf("W", "W/kg"),
                primaryInstruments = listOf("INST-CYCLE-ERGOMETER"),
                referenceProtocols = listOf("PROT-CP-3MIN-V1"),
                calculationMethod = "Média de potência nos últimos 30 segundos do teste all-out de 3 minutos em cicloergômetro.",
                captureFrequency = "Bimestral",
                requiredConditions = listOf("Aquecimento padronizado", "Esforço all-out absoluto sem economia de ritmo inicial"),
                limitations = listOf("Exige esforço extenuante e motivação psicológica extrema"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-JONES-2010",
                        title = "Critical power: concepts and applications",
                        authors = listOf("Jones AM", "Vanhatalo A", "Burnley M", "Morton RH", "Poole DC"),
                        publicationYear = 2010,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.2165/11531380-000000000-00000",
                        sourceAuthority = "Sports Medicine",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 6. W' (W Prime)
        register(
            MetricDefinition(
                metricId = "W_PRIME",
                name = "Capacidade de Trabalho Anaeróbio (W')",
                category = "BIOENERGETICS",
                whatItIs = "Reserva fixa finita de energia realizável acima da Potência Crítica antes da exaustão neuromuscular e metabólica.",
                howItIsMeasured = "Integração do excesso de potência acima de CP durante esforço máximo exaustivo.",
                standardUnit = "kJ",
                acceptableUnits = listOf("kJ", "J"),
                primaryInstruments = listOf("INST-CYCLE-ERGOMETER"),
                referenceProtocols = listOf("PROT-CP-3MIN-V1"),
                calculationMethod = "W' = Integral(Potência(t) - CP) dt",
                captureFrequency = "Bimestral",
                requiredConditions = listOf("Calculado simultaneamente com CP no teste all-out ou testes de tempo limite"),
                limitations = listOf("Recarga de W' durante recuperação é dependente da intensidade abaixo de CP e não é linear"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-MONOD-1965",
                        title = "The work capacity of a synergic muscular group",
                        authors = listOf("Monod H", "Scherrer J"),
                        publicationYear = 1965,
                        publicationType = "JOURNAL",
                        identifier = "PMID: 14316275",
                        sourceAuthority = "Ergonomics",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 7. Joint Stability
        register(
            MetricDefinition(
                metricId = "JOINT_STABILITY",
                name = "Estabilidade Articular e Controle Postural Dinâmico",
                category = "BIOMECHANICAL_CONTROL",
                whatItIs = "Capacidade do sistema neuromuscular de manter o alinhamento articular e absorver forças durante tarefas dinâmicas.",
                howItIsMeasured = "Índice de Estabilidade Postural Dinâmica (DPSI) via plataforma de força 3D ou matriz inercial em aterrissagem unipodal.",
                standardUnit = "score",
                acceptableUnits = listOf("score", "index", "ratio"),
                primaryInstruments = listOf("INST-FORCE-PLATE-1000HZ", "INST-DIGITAL-GONIOMETER"),
                referenceProtocols = listOf("PROT-IMTP-FORCE-V1"),
                calculationMethod = "DPSI = sqrt( (sum(Fx^2) + sum(Fy^2) + sum(Fz - BW)^2) / (N * BW^2) )",
                captureFrequency = "Mensal",
                requiredConditions = listOf("Superfície estável", "Massa corporal aferida imediatamente antes"),
                limitations = listOf("Requer plataformas triaxiais calibradas para cálculo do vetor tridimensional completo"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-WIKSTROM-2005",
                        title = "Development and reliability of the dynamic postural stability index",
                        authors = listOf("Wikstrom EA", "Tillman MD", "Smith AN", "Borsa PA"),
                        publicationYear = 2005,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.4085/1062-6050-40.4.305",
                        sourceAuthority = "Journal of Athletic Training",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 8. Symmetry
        register(
            MetricDefinition(
                metricId = "SYMMETRY",
                name = "Índice de Simetria de Membros (Limb Symmetry Index - LSI)",
                category = "BIOMECHANICAL_BALANCE",
                whatItIs = "Razão proporcional de capacidade de força, potência ou absorção entre membros contralaterais.",
                howItIsMeasured = "Comparação direta entre valores obtidos simultaneamente em plataformas de força duplas ou testes sequenciais.",
                standardUnit = "%",
                acceptableUnits = listOf("%", "ratio", "index"),
                primaryInstruments = listOf("INST-FORCE-PLATE-1000HZ"),
                referenceProtocols = listOf("PROT-IMTP-FORCE-V1"),
                calculationMethod = "LSI = (Membro_A / Membro_B) * 100",
                captureFrequency = "Mensal",
                requiredConditions = listOf("Mesmas condições de teste para ambos os membros"),
                limitations = listOf("Pode mascarar déficits bilaterais se ambos os membros estiverem descondicionados"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-MYER-2011",
                        title = "Utilization of modified NFL Combine testing to identify functional deficits",
                        authors = listOf("Myer GD", "Schmitt LC", "Brent JL", "Ford KR"),
                        publicationYear = 2011,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1177/0363546510390150",
                        sourceAuthority = "AJSM",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 9. ROM (Range of Motion)
        register(
            MetricDefinition(
                metricId = "ROM",
                name = "Amplitude de Movimento Articular (ROM)",
                category = "BIOMECHANICS_FLEXIBILITY",
                whatItIs = "Excursão angular máxima realizada por uma articulação em torno do seu eixo anatômico.",
                howItIsMeasured = "Goniometria digital, inclinometria ou cinemetria óptica a partir de marcos ósseos.",
                standardUnit = "deg",
                acceptableUnits = listOf("deg", "degrees", "rad"),
                primaryInstruments = listOf("INST-DIGITAL-GONIOMETER", "INST-CAMERA-HIGH-SPEED-120FPS"),
                referenceProtocols = listOf("PROT-ROM-GONIO-V1"),
                calculationMethod = "Delta_Angle = abs(theta_final - theta_inicial)",
                captureFrequency = "Mensal",
                requiredConditions = listOf("Alinhamento anatômico estrito com marcos ósseos"),
                limitations = listOf("Variação inter-examinador se pontos anatômicos não forem padronizados"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-NORKIN-2016",
                        title = "Measurement of Joint Motion: A Guide to Goniometry",
                        authors = listOf("Norkin CC", "White DJ"),
                        publicationYear = 2016,
                        publicationType = "BOOK",
                        identifier = "ISBN: 978-0803645622",
                        sourceAuthority = "F.A. Davis",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 10. Mean Propulsive Velocity
        register(
            MetricDefinition(
                metricId = "MEAN_PROPULSIVE_VELOCITY",
                name = "Velocidade Média Propulsiva (MPV)",
                category = "NEUROMUSCULAR_VBT",
                whatItIs = "Velocidade média calculada exclusivamente durante a fase de aceleração da barra (aceleração >= -g).",
                howItIsMeasured = "Encoder linear óptico ou câmera de alta velocidade monitorando a trajetória vertical do implemento.",
                standardUnit = "m/s",
                acceptableUnits = listOf("m/s"),
                primaryInstruments = listOf("INST-LINEAR-ENCODER-500HZ", "INST-CAMERA-HIGH-SPEED-120FPS"),
                referenceProtocols = listOf("PROT-VBT-PROP-V1"),
                calculationMethod = "MPV = (1 / t_propulsive) * integral(v dt) para a >= -9.81 m/s²",
                captureFrequency = "Por Sessão de Treinamento",
                requiredConditions = listOf("Intenção máxima de aceleração na fase concêntrica"),
                limitations = listOf("Sensível ao desvio angular do cabo do encoder (>5 graus invalida a repetição)"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-SANCHEZ-2010",
                        title = "Importance of the propulsive phase in strength assessment",
                        authors = listOf("Sanchez-Medina L", "Perez CE", "Gonzalez-Badillo JJ"),
                        publicationYear = 2010,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1055/s-0029-1242815",
                        sourceAuthority = "IJSM",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 11. Acceleration
        register(
            MetricDefinition(
                metricId = "ACCELERATION",
                name = "Aceleração Linear Inicial de Sprint",
                category = "BIOMECHANICS_LOCOMOTION",
                whatItIs = "Taxa máxima de variação da velocidade horizontal nos primeiros metros de deslocamento em sprint.",
                howItIsMeasured = "Fotocélulas de feixe duplo em 0m e 10m ou radar Doppler de alta frequência.",
                standardUnit = "m/s2",
                acceptableUnits = listOf("m/s2", "m/s^2"),
                primaryInstruments = listOf("INST-OPTICAL-TIMING-GATES"),
                referenceProtocols = listOf("PROT-ACCEL-10M-V1"),
                calculationMethod = "a = (v_final - v_inicial) / delta_t",
                captureFrequency = "Mensal",
                requiredConditions = listOf("Largada estática", "Piso de aderência padronizado", "Vento < 2.0 m/s"),
                limitations = listOf("Sensível à altura de disparo da célula fotoelétrica"),
                sources = listOf(
                    MethodologySource(
                        sourceId = "SRC-SAMOZINO-2016-SPRINT",
                        title = "A simple method for measuring power, force, velocity properties in sprint",
                        authors = listOf("Samozino P", "Rabita G", "Morin JB"),
                        publicationYear = 2016,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1111/sms.12490",
                        sourceAuthority = "SJMS",
                        sourceStatus = "VALIDATED"
                    )
                ),
                methodologyVersion = "1.0.0",
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )
    }

    fun register(metricDefinition: MetricDefinition) {
        metrics[metricDefinition.metricId] = metricDefinition
    }

    fun getMetricDefinition(metricId: String): MetricDefinition? {
        return metrics[metricId]
    }

    fun getAllMetricDefinitions(): List<MetricDefinition> {
        return metrics.values.toList()
    }

    fun containsMetric(metricId: String): Boolean {
        return metrics.containsKey(metricId)
    }
}
