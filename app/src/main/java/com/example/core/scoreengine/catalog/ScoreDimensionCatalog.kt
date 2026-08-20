package com.example.core.scoreengine.catalog

import com.example.core.datacore.metrics.MetricCatalog
import com.example.core.datacore.protocols.ProtocolCatalog
import com.example.core.scoreengine.model.DimensionType

/**
 * MetricDimensionMapping: Estrutura que mapeia a relação entre Métrica, Dimensão(ões),
 * Método de Cálculo, Protocolo e Requisitos de Evidência.
 */
data class MetricDimensionMapping(
    val metricId: String,
    val metricName: String,
    val targetDimensions: List<String>,
    val primaryDimension: String,
    val defaultProtocolId: String,
    val calculationMethodName: String,
    val evidenceRequirement: String,
    val formulaVersion: String = "1.0.0-score-v1"
)

/**
 * ScoreDimensionCatalog: Registro central de mapeamento entre Métricas e Dimensões no PERFORMAI.
 *
 * Estrutura extensível: Permite associar métricas a uma ou múltiplas dimensões conforme evolução científica.
 */
object ScoreDimensionCatalog {

    val MAPPINGS: List<MetricDimensionMapping> = listOf(
        // ==========================================
        // 1. FORCE
        // ==========================================
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_RELATIVE_STRENGTH,
            metricName = "Força Dinâmica Relativa (1RM/BW)",
            targetDimensions = listOf(DimensionType.Force.key),
            primaryDimension = DimensionType.Force.key,
            defaultProtocolId = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
            calculationMethodName = "Força Concêntrica Normalizada por Massa Corporal",
            evidenceRequirement = "Transdutor linear LPT com velocidade média >= 0.15 m/s",
            formulaVersion = "SCORE-FORCE-RELSTR-1.0"
        ),
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_RFD,
            metricName = "Taxa de Desenvolvimento de Força (RFD)",
            targetDimensions = listOf(DimensionType.Force.key),
            primaryDimension = DimensionType.Force.key,
            defaultProtocolId = ProtocolCatalog.PROTO_IMTP_RFD,
            calculationMethodName = "Gradiente de Força Inicial nos primeiros 100-200ms",
            evidenceRequirement = "Plataforma de força biaxial >= 1000Hz com linha de base estável",
            formulaVersion = "SCORE-FORCE-RFD-1.0"
        ),

        // ==========================================
        // 2. SPEED
        // ==========================================
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_VELOCITY,
            metricName = "Velocidade Média/Pico Propulsiva",
            targetDimensions = listOf(DimensionType.Speed.key),
            primaryDimension = DimensionType.Speed.key,
            defaultProtocolId = ProtocolCatalog.PROTO_BIOMECHANICAL_ROM_STABILITY,
            calculationMethodName = "Velocidade Linear Propulsiva (VBT)",
            evidenceRequirement = "Encoder óptico ou acelerometria com amostragem >= 200 Hz",
            formulaVersion = "SCORE-SPEED-VEL-1.0"
        ),
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_ACCELERATION,
            metricName = "Aceleração Instantânea & Pico",
            targetDimensions = listOf(DimensionType.Speed.key),
            primaryDimension = DimensionType.Speed.key,
            defaultProtocolId = ProtocolCatalog.PROTO_BIOMECHANICAL_ROM_STABILITY,
            calculationMethodName = "Derivada Temporal da Velocidade / Acelerômetro Triaxial",
            evidenceRequirement = "Acelerômetro MEMS triaxial calibrado com filtro passa-baixa",
            formulaVersion = "SCORE-SPEED-ACC-1.0"
        ),

        // ==========================================
        // 3. ENDURANCE
        // ==========================================
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_VO2_MAX,
            metricName = "VO2 Max Estimado",
            targetDimensions = listOf(DimensionType.Endurance.key),
            primaryDimension = DimensionType.Endurance.key,
            defaultProtocolId = ProtocolCatalog.PROTO_BASELINE_SUBMAX_VO2,
            calculationMethodName = "Extrapolação FC-Potência para Teto Aeróbio",
            evidenceRequirement = "Mínimo 3 estágios submáximos estáveis com ECG contínuo",
            formulaVersion = "SCORE-ENDUR-VO2-1.0"
        ),
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_CRITICAL_POWER,
            metricName = "Critical Power (CP)",
            targetDimensions = listOf(DimensionType.Endurance.key),
            primaryDimension = DimensionType.Endurance.key,
            defaultProtocolId = ProtocolCatalog.PROTO_CRITICAL_POWER_3MIN_ALL_OUT,
            calculationMethodName = "Potência Sustentável Assintótica (3MT)",
            evidenceRequirement = "Registro de potência a 1Hz com esforço máximo nos 180s",
            formulaVersion = "SCORE-ENDUR-CP-1.0"
        ),
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_W_PRIME,
            metricName = "Capacidade de Trabalho Anaeróbio (W')",
            targetDimensions = listOf(DimensionType.Endurance.key),
            primaryDimension = DimensionType.Endurance.key,
            defaultProtocolId = ProtocolCatalog.PROTO_CRITICAL_POWER_3MIN_ALL_OUT,
            calculationMethodName = "Integral Supramáxima Acima do CP",
            evidenceRequirement = "Série temporal de esforço máximo com depleção documentada",
            formulaVersion = "SCORE-ENDUR-WPRIME-1.0"
        ),

        // ==========================================
        // 4. MOBILITY
        // ==========================================
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_ROM,
            metricName = "Amplitude de Movimento Articular (ROM)",
            targetDimensions = listOf(DimensionType.Mobility.key),
            primaryDimension = DimensionType.Mobility.key,
            defaultProtocolId = ProtocolCatalog.PROTO_BIOMECHANICAL_ROM_STABILITY,
            calculationMethodName = "Diferença Angular no Ciclo Motor",
            evidenceRequirement = "Goniometria digital ou IMU calibrado em 100Hz",
            formulaVersion = "SCORE-MOB-ROM-1.0"
        ),
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_JOINT_STABILITY,
            metricName = "Estabilidade Articular & Simetria",
            targetDimensions = listOf(DimensionType.Mobility.key),
            primaryDimension = DimensionType.Mobility.key,
            defaultProtocolId = ProtocolCatalog.PROTO_BIOMECHANICAL_ROM_STABILITY,
            calculationMethodName = "Controle Cinemático e Simetria Bilateral",
            evidenceRequirement = "Captura cinemática em 100Hz de >= 5 repetições consecutivas",
            formulaVersion = "SCORE-MOB-STAB-1.0"
        ),

        // ==========================================
        // 5. CONTEXTUAL / RECOVERY
        // ==========================================
        MetricDimensionMapping(
            metricId = MetricCatalog.METRIC_HRV_RMSSD,
            metricName = "Variabilidade Cardíaca (HRV rMSSD)",
            targetDimensions = listOf("RECOVERY", "AUTONOMIC_REGULATION"),
            primaryDimension = "AUTONOMIC_REGULATION",
            defaultProtocolId = ProtocolCatalog.PROTO_RESTING_HRV_RMSSD,
            calculationMethodName = "rMSSD em Repouso Matinal",
            evidenceRequirement = "Registro de 5 min supino matinal com < 2% de artefatos",
            formulaVersion = "SCORE-REC-HRV-1.0"
        )
    )

    private val MAPPINGS_BY_METRIC: Map<String, MetricDimensionMapping> = MAPPINGS.associateBy { it.metricId }

    fun getMapping(metricId: String): MetricDimensionMapping? = MAPPINGS_BY_METRIC[metricId]

    fun getMetricsForDimension(dimensionKey: String): List<MetricDimensionMapping> =
        MAPPINGS.filter { it.targetDimensions.contains(dimensionKey.uppercase()) }
}
