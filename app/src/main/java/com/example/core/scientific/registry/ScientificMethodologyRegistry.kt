package com.example.core.scientific.registry

import com.example.core.scientific.model.*

/**
 * PERFORMAI SCIENTIFIC METHODOLOGY REGISTRY
 *
 * Registrador central e imutável de metodologias científicas oficiais.
 * Contém princípios de medição, métodos de cálculo, fontes e níveis de evidência.
 */
object ScientificMethodologyRegistry {

    private val methodologies: MutableMap<String, ScientificMethodology> = mutableMapOf()

    init {
        registerDefaultMethodologies()
    }

    private fun registerDefaultMethodologies() {
        // 1. VO2 Max - Teste Cardiorrespiratório com Análise de Gases / Ergoespirometria
        register(
            ScientificMethodology(
                methodologyId = "METH-VO2MAX-CPX-V1",
                name = "Consumo Máximo de Oxigênio via Ergoespirometria Direta",
                description = "Determinação direta do VO2max através de análise respiratória breath-by-breath durante teste incremental máximo.",
                version = "1.0.0",
                metricId = "VO2_MAX",
                category = "CARDIORESPIRATORY",
                measurementPrinciple = "Análise de trocas gasosas respiratórias (O2 e CO2) por pneumotacômetro e analisadores paramagnéticos/infravermelhos em esteira ou cicloergômetro.",
                calculationMethod = "VO2 = VE * (FiO2 - FeO2); platô de VO2 (< 150 ml/min ou < 2.1 ml/kg/min com aumento de carga), RER > 1.10, FC > 90% da predita.",
                acceptedUnits = listOf("ml/kg/min", "L/min"),
                requiredConditions = listOf("Jejum de 2-3h", "Ambiente climatizado 20-22C", "Sem exercício extenuante nas 24h anteriores"),
                requiredEquipment = listOf("METABOLIC_CART", "CALIBRATED_GAS_ANALYZER", "CYCLE_ERGOMETER_OR_TREADMILL", "ECG_OR_HR_MONITOR"),
                acceptableSources = listOf("LABORATORY_METABOLIC_CART", "VALIDATED_PORTABLE_SPIROMETER"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-ACSM-GETP11",
                        title = "ACSM's Guidelines for Exercise Testing and Prescription (11th ed.)",
                        authors = listOf("American College of Sports Medicine"),
                        publicationYear = 2021,
                        publicationType = "CLINICAL_GUIDELINE",
                        identifier = "ISBN: 978-1975150181",
                        sourceAuthority = "American College of Sports Medicine",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Custo elevado", "Exige operadores especializados", "Critérios de platô nem sempre atingidos por todos os indivíduos"),
                effectiveFrom = 1700000000000L
            )
        )

        // 2. Força Relativa (Relative Force) - Força Máxima Normalizada por Massa Corporal
        register(
            ScientificMethodology(
                methodologyId = "METH-REL-FORCE-IMTP-V1",
                name = "Força Isométrica Relativa via Isometric Mid-Thigh Pull (IMTP)",
                description = "Pico de força isométrica normalizado pela massa corporal do atleta obtido em plataforma de força calibrada.",
                version = "1.0.0",
                metricId = "RELATIVE_FORCE",
                category = "NEUROMUSCULAR",
                measurementPrinciple = "Registro de força vertical de reação do solo (Fz) durante tração isométrica máxima em barra fixada a 125-145 graus de flexão de joelho.",
                calculationMethod = "Peak Force / Body Mass (N/kg)",
                acceptedUnits = listOf("N/kg", "ratio"),
                requiredConditions = listOf("Aquecimento neuromuscular padronizado", "Fixação estável da barra", "Ângulos articulares verificados"),
                requiredEquipment = listOf("FORCE_PLATE_DUAL_OR_SINGLE", "RIGID_POWER_RACK", "STRAPS_IF_PROTOCOL_SPECIFIED"),
                acceptableSources = listOf("FORCE_PLATE", "CALIBRATED_LOAD_CELL"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-COMFORT-2019-IMTP",
                        title = "Methods of evaluating isometric mid-thigh pull performance",
                        authors = listOf("Comfort P", "Dos'Santos T", "Beckham GK", "Stone MH"),
                        publicationYear = 2019,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1519/SSC.0000000000000433",
                        sourceAuthority = "National Strength and Conditioning Association (NSCA)",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Específico ao ângulo articular testado", "Depende de instrução verbal padronizada ('puxar o mais rápido e forte possível')"),
                effectiveFrom = 1700000000000L
            )
        )

        // 3. RFD (Rate of Force Development) - Taxa de Desenvolvimento de Força
        register(
            ScientificMethodology(
                methodologyId = "METH-RFD-ISOM-V1",
                name = "Taxa de Desenvolvimento de Força em Janelas Temporais Fixas (0-50ms, 0-100ms, 0-200ms)",
                description = "Derivada temporal da curva força-tempo (dF/dt) a partir do início da contração muscular voluntária.",
                version = "1.0.0",
                metricId = "RFD",
                category = "NEUROMUSCULAR_EXPLOSIVE",
                measurementPrinciple = "Cálculo da inclinação da curva força-tempo registrada por transdutor piezoelétrico ou extensométrico com amostragem >= 1000 Hz.",
                calculationMethod = "RFD(t) = (Force(t) - Force(onset)) / delta_t",
                acceptedUnits = listOf("N/s"),
                requiredConditions = listOf("Identificação determinística de onset (ex: 5x desvio padrão da linha de base de repouso ou threshold de 5-10 N)", "Sem contramovimento antes do início"),
                requiredEquipment = listOf("FORCE_PLATE_HIGH_FREQ", "ISOMETRIC_LOAD_CELL"),
                acceptableSources = listOf("FORCE_PLATE_1000HZ", "LOAD_CELL_1000HZ"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-AAGAARD-2002-RFD",
                        title = "Increased rate of force development and neural drive of human skeletal muscle following resistance training",
                        authors = listOf("Aagaard P", "Simonsen EB", "Andersen JL", "Magnusson P", "Halkjaer-Kristensen J"),
                        publicationYear = 2002,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1152/japplphysiol.00283.2002",
                        sourceAuthority = "Journal of Applied Physiology",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Extrema sensibilidade ao método de detecção do onset", "Requer taxa de amostragem mínima de 1000 Hz para janelas de 0-50ms"),
                effectiveFrom = 1700000000000L
            )
        )

        // 4. HRV rMSSD - Variabilidade da Frequência Cardíaca (Raiz Quadrada da Média dos Quadrados das Diferenças Sucessivas)
        register(
            ScientificMethodology(
                methodologyId = "METH-HRV-RMSSD-V1",
                name = "VFC no Domínio do Tempo via rMSSD em Repouso",
                description = "Quantificação da modulação autonômica parassimpática cardíaca através de intervalos R-R consecutivos limpos de ectopias.",
                version = "1.0.0",
                metricId = "HRV_RMSSD",
                category = "AUTONOMIC_RECOVERY",
                measurementPrinciple = "Detecção óptica ou elétrica de batimentos cardíacos sucessivos com resolução de milissegundos e filtro de artefatos.",
                calculationMethod = "rMSSD = sqrt( (1 / (N - 1)) * sum( (RR_{i+1} - RR_i)^2 ) )",
                acceptedUnits = listOf("ms"),
                requiredConditions = listOf("Repouso de 5 minutos pré-registro", "Posição padronizada (supino ou sentado)", "Respiração espontânea sem hiperventilação", "Sem ingestão aguda de cafeína"),
                requiredEquipment = listOf("ECG_LEAD", "VALIDATED_CHEST_STRAP_HRM", "HIGH_RESOLUTION_PPG_SENSOR"),
                acceptableSources = listOf("ECG", "CHEST_STRAP_BLUETOOTH", "VALIDATED_OPTICAL_HRV"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-TASKFORCE-1996-HRV",
                        title = "Heart rate variability: standards of measurement, physiological interpretation and clinical use",
                        authors = listOf("Task Force of the European Society of Cardiology and The North American Society of Pacing and Electrophysiology"),
                        publicationYear = 1996,
                        publicationType = "CONSENSUS_STATEMENT",
                        identifier = "DOI: 10.1161/01.CIR.93.5.1043",
                        sourceAuthority = "Circulation",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Sensível a estressores não-físicos (sono, ansiedade, temperatura)", "Requer remoção rigorosa de batimentos ectópicos"),
                effectiveFrom = 1700000000000L
            )
        )

        // 5. Critical Power (CP) - Potência Crítica
        register(
            ScientificMethodology(
                methodologyId = "METH-CRITICAL-POWER-V1",
                name = "Determinação de Potência Crítica via Modelo Hiperbólico de 2 Parâmetros",
                description = "Limite assintótico de taxa de trabalho que pode ser sustentado sem esgotamento contínuo de W'.",
                version = "1.0.0",
                metricId = "CRITICAL_POWER",
                category = "BIOENERGETICS",
                measurementPrinciple = "Relação tempo-limite (Tlim) vs potência (P) em 3 a 5 testes até exaustão ou protocolo 3-min all-out.",
                calculationMethod = "P = CP + (W' / t)  <=>  Work = (CP * t) + W'",
                acceptedUnits = listOf("W", "W/kg"),
                requiredConditions = listOf("Recuperação completa entre esforços (> 24h ou 30 min em protocolo all-out padronizado)", "Calibração de torque do ergômetro"),
                requiredEquipment = listOf("CYCLE_ERGOMETER_ELECTROMAGNETIC", "POWER_METER_CALIBRATED"),
                acceptableSources = listOf("LAB_CYCLE_ERGOMETER", "DUAL_SIDED_POWER_PEDALS"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-JONES-2010-CP",
                        title = "Critical power: concepts and applications",
                        authors = listOf("Jones AM", "Vanhatalo A", "Burnley M", "Morton RH", "Poole DC"),
                        publicationYear = 2010,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.2165/11531380-000000000-00000",
                        sourceAuthority = "Sports Medicine",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Protocolo multi-ensaio exige alta motivação", "Protocolo 3-min all-out é extremamente exaustivo"),
                effectiveFrom = 1700000000000L
            )
        )

        // 6. W' (W Prime / Capacidade de Trabalho Anaeróbio)
        register(
            ScientificMethodology(
                methodologyId = "METH-W-PRIME-V1",
                name = "Capacidade de Trabalho acima da Potência Crítica (W')",
                description = "Quantidade finita de trabalho que pode ser realizada acima de CP, governada por substratos anaeróbios e metabólitos de fadiga.",
                version = "1.0.0",
                metricId = "W_PRIME",
                category = "BIOENERGETICS",
                measurementPrinciple = "Integração da curva de potência acima de CP durante ensaios exaustivos.",
                calculationMethod = "W' = t * (P - CP) (expresso em Joules ou kJ)",
                acceptedUnits = listOf("kJ", "J"),
                requiredConditions = listOf("Determinação simultânea e congruente com Critical Power"),
                requiredEquipment = listOf("CYCLE_ERGOMETER_ELECTROMAGNETIC", "POWER_METER_CALIBRATED"),
                acceptableSources = listOf("LAB_CYCLE_ERGOMETER", "DUAL_SIDED_POWER_PEDALS"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-MONOD-SCHERRER-1965",
                        title = "The work capacity of a synergic muscular group",
                        authors = listOf("Monod H", "Scherrer J"),
                        publicationYear = 1965,
                        publicationType = "JOURNAL",
                        identifier = "PMID: 14316275",
                        sourceAuthority = "Ergonomics",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Depende da precisão da estimativa de CP", "Cinética de reconstituição de W' é não-linear"),
                effectiveFrom = 1700000000000L
            )
        )

        // 7. Estabilidade Articular (Joint Stability)
        register(
            ScientificMethodology(
                methodologyId = "METH-JOINT-STABILITY-V1",
                name = "Índice de Estabilidade Dinâmica e Controle Postural Articular",
                description = "Avaliação de desvios angulares, controle motor e excursão do centro de pressão durante tarefas unipodais.",
                version = "1.0.0",
                metricId = "JOINT_STABILITY",
                category = "BIOMECHANICAL_CONTROL",
                measurementPrinciple = "Registro cinemático e cinético de oscilação postural e estabilização após aterrissagem (Dynamic Postural Stability Index - DPSI).",
                calculationMethod = "DPSI = sqrt( (sum(Fx^2) + sum(Fy^2) + sum(Fz - BW)^2) / (N * BW^2) )",
                acceptedUnits = listOf("score", "index", "ratio"),
                requiredConditions = listOf("Aterrissagem padronizada de salto ou apoio unipodal descalço"),
                requiredEquipment = listOf("FORCE_PLATE_3D", "OPTICAL_MOTION_CAPTURE_OR_IMU"),
                acceptableSources = listOf("LAB_FORCE_PLATE", "VALIDATED_IMU_ARRAY"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-WIKSTROM-2005-DPSI",
                        title = "Development and reliability of the dynamic postural stability index",
                        authors = listOf("Wikstrom EA", "Tillman MD", "Smith AN", "Borsa PA"),
                        publicationYear = 2005,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.4085/1062-6050-40.4.305",
                        sourceAuthority = "Journal of Athletic Training",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Exige calibração de massa corporal imediatamente antes da medição"),
                effectiveFrom = 1700000000000L
            )
        )

        // 8. Simetria (Symmetry Index)
        register(
            ScientificMethodology(
                methodologyId = "METH-SYMMETRY-LSI-V1",
                name = "Limb Symmetry Index (LSI) e Índice de Assimetria Bilateral",
                description = "Comparação percentual de magnitude de força ou potência entre membros ipsilateral e contralateral.",
                version = "1.0.0",
                metricId = "SYMMETRY",
                category = "BIOMECHANICAL_BALANCE",
                measurementPrinciple = "Cálculo da razão entre membro envolvido/não-dominante vs membro não-envolvido/dominante.",
                calculationMethod = "LSI (%) = (Valor_Membro_A / Valor_Membro_B) * 100",
                acceptedUnits = listOf("%", "ratio", "index"),
                requiredConditions = listOf("Mesmo protocolo e condições de fadiga aplicadas a ambos os membros"),
                requiredEquipment = listOf("DUAL_FORCE_PLATES", "ISOKINETIC_DYNAMOMETER", "OPTICAL_CONTACT_MATS"),
                acceptableSources = listOf("DUAL_FORCE_PLATE", "ISOKINETIC_RIG"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-MYER-2011-SYMMETRY",
                        title = "Utilization of modified NFL Combine testing to identify functional deficits in athletes following ACL reconstruction",
                        authors = listOf("Myer GD", "Schmitt LC", "Brent JL", "Ford KR", "Barber Foss KD", "Gleason PE", "Hewett TE"),
                        publicationYear = 2011,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1177/0363546510390150",
                        sourceAuthority = "American Journal of Sports Medicine",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Não detecta déficits bilaterais absolutos simultâneos"),
                effectiveFrom = 1700000000000L
            )
        )

        // 9. ROM (Range of Motion / Amplitude de Movimento)
        register(
            ScientificMethodology(
                methodologyId = "METH-ROM-GONIO-V1",
                name = "Goniometria e Fotogrametria Angular de Amplitude de Movimento",
                description = "Quantificação de excursão angular articular máxima passiva ou ativa em graus sexagesimais.",
                version = "1.0.0",
                metricId = "ROM",
                category = "BIOMECHANICS_FLEXIBILITY",
                measurementPrinciple = "Identificação de eixos e centros articulares anatômicos via goniômetro mecânico, digital ou rastreamento de marcadores ópticos.",
                calculationMethod = "Angle = abs(theta_final - theta_inicial) em graus",
                acceptedUnits = listOf("deg", "degrees", "rad"),
                requiredConditions = listOf("Alinhamento anatômico rigoroso com marcos ósseos", "Aquecimento térmico passivo padronizado"),
                requiredEquipment = listOf("DIGITAL_GONIOMETER", "OPTICAL_MOTION_ANALYSIS", "INCLINOMETER"),
                acceptableSources = listOf("DIGITAL_GONIOMETER", "CALIBRATED_VIDEO_PHOTOGRAMMETRY", "IMU_SENSOR"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-NORKIN-WHITE-2016",
                        title = "Measurement of Joint Motion: A Guide to Goniometry (5th ed.)",
                        authors = listOf("Norkin CC", "White DJ"),
                        publicationYear = 2016,
                        publicationType = "BOOK",
                        identifier = "ISBN: 978-0803645622",
                        sourceAuthority = "F.A. Davis Company",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Variação inter-avaliador se pontos de referência anatômicos não forem padronizados"),
                effectiveFrom = 1700000000000L
            )
        )

        // 10. Velocidade Propulsiva Média (Mean Propulsive Velocity - MPV)
        register(
            ScientificMethodology(
                methodologyId = "METH-VBT-MPV-V1",
                name = "Velocidade Média Propulsiva no Treinamento Baseado em Velocidade (VBT)",
                description = "Velocidade média calculada exclusivamente durante a fase propulsiva do movimento concêntrico (onde a aceleração da barra é >= -9.81 m/s²).",
                version = "1.0.0",
                metricId = "MEAN_PROPULSIVE_VELOCITY",
                category = "NEUROMUSCULAR_VBT",
                measurementPrinciple = "Deslocamento linear da barra monitorado por transdutor linear de posição/velocidade óptico ou por corda.",
                calculationMethod = "MPV = (1 / t_propulsive) * integral_0^{t_propulsive} v(t) dt",
                acceptedUnits = listOf("m/s"),
                requiredConditions = listOf("Execução à máxima velocidade intencional na fase concêntrica", "Evitar desaceleração intencional prematura"),
                requiredEquipment = listOf("LINEAR_POSITION_TRANSDUCER", "LINEAR_VELOCITY_TRANSDUCER", "OPTICAL_BARBELL_TRACKER"),
                acceptableSources = listOf("LINEAR_ENCODER", "HIGH_SPEED_OPTICAL_TRACKER"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-SANCHEZ-MEDINA-2010",
                        title = "Importance of the propulsive phase in strength assessment",
                        authors = listOf("Sanchez-Medina L", "Perez CE", "Gonzalez-Badillo JJ"),
                        publicationYear = 2010,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1055/s-0029-1242815",
                        sourceAuthority = "International Journal of Sports Medicine",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Em cargas pesadas (>80% 1RM) a fase propulsiva coincide com a concêntrica total; em cargas leves (<50% 1RM) a diferença é crítica"),
                effectiveFrom = 1700000000000L
            )
        )

        // 11. Aceleração (Acceleration)
        register(
            ScientificMethodology(
                methodologyId = "METH-ACCELERATION-SPRINT-V1",
                name = "Aceleração Linear Inicial em Sprint via Células Fotoelétricas ou Radar",
                description = "Taxa de variação temporal da velocidade durante a fase de impulsão inicial (0-10m / 0-20m).",
                version = "1.0.0",
                metricId = "ACCELERATION",
                category = "BIOMECHANICS_LOCOMOTION",
                measurementPrinciple = "Registro de tempo de passagem por múltiplos feixes ópticos com precisão milissegunda ou radar Doppler contínuo.",
                calculationMethod = "a = (v_final - v_inicial) / delta_t  ou  v(t) = v_max * (1 - exp(-t / tau)) com a_max = v_max / tau",
                acceptedUnits = listOf("m/s^2", "m/s2"),
                requiredConditions = listOf("Posição inicial estática sem contramovimento prévio", "Piso de tração esportiva padronizado", "Vento < 2.0 m/s em pista aberta"),
                requiredEquipment = listOf("OPTICAL_TIMING_GATES_DUAL_BEAM", "RADAR_GUN_OR_LASER", "START_TRIGGER_PAD"),
                acceptableSources = listOf("DUAL_BEAM_TIMING_GATES", "DOPPLER_RADAR", "HIGH_PRECISION_GPS_10HZ_OR_HIGHER"),
                validationStatus = MethodologyValidationStatus.ACTIVE,
                evidenceLevel = EvidenceLevel.EVIDENCE_LEVEL_VERY_HIGH,
                sourceReferences = listOf(
                    MethodologySource(
                        sourceId = "SRC-SAMOZINO-2016",
                        title = "A simple method for measuring power, force, velocity properties, and mechanical effectiveness in sprint running",
                        authors = listOf("Samozino P", "Rabita G", "Dorel S", "Slawinski J", "Peyrot N", "Saez de Villarreal E", "Morin JB"),
                        publicationYear = 2016,
                        publicationType = "JOURNAL",
                        identifier = "DOI: 10.1111/sms.12490",
                        sourceAuthority = "Scandinavian Journal of Medicine & Science in Sports",
                        sourceStatus = "VALIDATED"
                    )
                ),
                limitations = listOf("Sensível ao posicionamento da célula inicial (altura de feixe) e largada em falso"),
                effectiveFrom = 1700000000000L
            )
        )
    }

    fun register(methodology: ScientificMethodology) {
        methodologies[methodology.methodologyId] = methodology
    }

    fun getMethodology(methodologyId: String): ScientificMethodology? {
        return methodologies[methodologyId]
    }

    fun getMethodologiesForMetric(metricId: String): List<ScientificMethodology> {
        return methodologies.values.filter { it.metricId == metricId }
    }

    fun getAllMethodologies(): List<ScientificMethodology> {
        return methodologies.values.toList()
    }

    fun containsMethodology(methodologyId: String): Boolean {
        return methodologies.containsKey(methodologyId)
    }
}
