package com.example.core.datacore.protocols

import com.example.core.datacore.model.DataCoreProtocol
import com.example.core.datacore.model.ProtocolValidityRange
import com.example.core.datacore.model.ProtocolValidityRules

/**
 * ProtocolCatalog: Registro de métodos científicos validados para aquisição de dados no DATA CORE V1.
 *
 * Cada protocolo define suas regras de validade estritas (unidades aceitas, intervalo mínimo/máximo permitido,
 * tolerância de carimbo de tempo, frequência de amostragem e entradas obrigatórias).
 */
object ProtocolCatalog {

    const val PROTO_BASELINE_SUBMAX_VO2 = "PROTO_BASELINE_SUBMAX_VO2"
    const val PROTO_LPT_RELATIVE_STRENGTH = "PROTO_LPT_RELATIVE_STRENGTH"
    const val PROTO_IMTP_RFD = "PROTO_IMTP_RFD"
    const val PROTO_RESTING_HRV_RMSSD = "PROTO_RESTING_HRV_RMSSD"
    const val PROTO_CRITICAL_POWER_3MIN_ALL_OUT = "PROTO_CRITICAL_POWER_3MIN_ALL_OUT"
    const val PROTO_BIOMECHANICAL_ROM_STABILITY = "PROTO_BIOMECHANICAL_ROM_STABILITY"

    val BASELINE_SUBMAX_VO2 = DataCoreProtocol(
        id = PROTO_BASELINE_SUBMAX_VO2,
        name = "Protocolo Submáximo Conconi / Astrand em Cicloergômetro",
        version = "1.0.0",
        description = "Avaliação de capacidade aeróbia submáxima com monitoramento de frequência cardíaca progressiva para extrapolação de VO2 Max.",
        category = "PHYSIOLOGICAL_AEROBIC",
        requiredInputs = listOf("heart_rate_bpm", "workload_watts", "duration_seconds"),
        methodology = "Aplicação de cargas progressivas com duração mínima de 3 minutos por estágio até atingir 80-85% da FC máxima prevista.",
        validityRules = ProtocolValidityRules(
            allowedUnits = listOf("ml/kg/min"),
            valueRange = ProtocolValidityRange(
                minAllowed = 10.0,
                maxAllowed = 95.0,
                expectedUnit = "ml/kg/min",
                description = "Valores de VO2 Max fisiologicamente possíveis em humanos (10 a 95 ml/kg/min)."
            ),
            minSamplingDurationSeconds = 180,
            minSamplingRateHz = 1,
            maxClockSkewToleranceMs = 60_000L,
            maxDataAgeMs = 90L * 24 * 60 * 60 * 1000
        )
    )

    val LPT_RELATIVE_STRENGTH = DataCoreProtocol(
        id = PROTO_LPT_RELATIVE_STRENGTH,
        name = "Protocolo de Carga Progressiva com Transdutor Linear de Posição",
        version = "1.0.0",
        description = "Determinação da força dinâmica relativa e perfil de carga-velocidade a partir de encoder óptico linear.",
        category = "NEUROMUSCULAR_FORCE",
        requiredInputs = listOf("lift_weight_kg", "body_weight_kg", "mean_concentric_velocity_ms"),
        methodology = "Execução de repetições concêntricas máximas em cargas crescentes monitoradas por encoder linear em 1000 Hz.",
        validityRules = ProtocolValidityRules(
            allowedUnits = listOf("1RM/BW ratio", "xBW", "ratio"),
            valueRange = ProtocolValidityRange(
                minAllowed = 0.2,
                maxAllowed = 5.0,
                expectedUnit = "1RM/BW ratio",
                description = "Razão Força Relativa admissível (0.2x a 5.0x o peso corporal)."
            ),
            minSamplingDurationSeconds = 2,
            minSamplingRateHz = 200,
            maxClockSkewToleranceMs = 60_000L,
            maxDataAgeMs = 90L * 24 * 60 * 60 * 1000
        )
    )

    val IMTP_RFD = DataCoreProtocol(
        id = PROTO_IMTP_RFD,
        name = "Protocolo Padronizado IMTP (Isometric Mid-Thigh Pull)",
        version = "1.0.0",
        description = "Tração isométrica em barra fixa sobre plataforma de força biaxial para mensuração da Taxa de Desenvolvimento de Força.",
        category = "NEUROMUSCULAR_EXPLOSION",
        requiredInputs = listOf("force_time_curve", "knee_angle_deg", "hip_angle_deg"),
        methodology = "Esforço isométrico voluntário máximo por 5 segundos com ângulo de joelho fixado em 125-145° e quadril em 140-150°.",
        validityRules = ProtocolValidityRules(
            allowedUnits = listOf("N/s", "N/sec"),
            valueRange = ProtocolValidityRange(
                minAllowed = 100.0,
                maxAllowed = 15000.0,
                expectedUnit = "N/s",
                description = "Taxa de Desenvolvimento de Força admissível (100 a 15000 N/s)."
            ),
            minSamplingDurationSeconds = 5,
            minSamplingRateHz = 1000,
            maxClockSkewToleranceMs = 60_000L,
            maxDataAgeMs = 90L * 24 * 60 * 60 * 1000
        )
    )

    val RESTING_HRV_RMSSD = DataCoreProtocol(
        id = PROTO_RESTING_HRV_RMSSD,
        name = "Protocolo de Variabilidade Cardíaca em Repouso Matinal (5 min)",
        version = "1.0.0",
        description = "Registro de intervalos R-R em repouso supino matinal para cálculo da modulação autonômica parassimpática rMSSD.",
        category = "AUTONOMIC_RECOVERY",
        requiredInputs = listOf("rr_intervals_ms", "artifact_percentage", "body_position"),
        methodology = "Registro contínuo de 5 minutos de intervalos inter-batimentos após 2 minutos de aclimatação em repouso supino.",
        validityRules = ProtocolValidityRules(
            allowedUnits = listOf("ms"),
            valueRange = ProtocolValidityRange(
                minAllowed = 5.0,
                maxAllowed = 350.0,
                expectedUnit = "ms",
                description = "Intervalo fisiológico de rMSSD em humanos (5 a 350 ms)."
            ),
            minSamplingDurationSeconds = 300,
            minSamplingRateHz = 1,
            maxClockSkewToleranceMs = 60_000L,
            maxDataAgeMs = 90L * 24 * 60 * 60 * 1000
        )
    )

    val CRITICAL_POWER_3MIN = DataCoreProtocol(
        id = PROTO_CRITICAL_POWER_3MIN_ALL_OUT,
        name = "Protocolo 3-Minutos All-Out Test (3MT) para Potência Crítica",
        version = "1.0.0",
        description = "Determinação do Critical Power (CP) e da Capacidade de Trabalho Anaeróbio (W') através de teste all-out de 180 segundos.",
        category = "PHYSIOLOGICAL_METABOLIC",
        requiredInputs = listOf("power_curve_watts", "cadence_rpm", "time_seconds"),
        methodology = "Pedalagem em esforço máximo imediato contra freio linear por 180 segundos ininterruptos sem cadenciamento prévio.",
        validityRules = ProtocolValidityRules(
            allowedUnits = listOf("W", "Watts", "kJ"),
            valueRange = ProtocolValidityRange(
                minAllowed = 50.0,
                maxAllowed = 800.0,
                expectedUnit = "W",
                description = "Critical Power sustentável em humanos (50 a 800 W)."
            ),
            minSamplingDurationSeconds = 180,
            minSamplingRateHz = 1,
            maxClockSkewToleranceMs = 60_000L,
            maxDataAgeMs = 90L * 24 * 60 * 60 * 1000
        )
    )

    val BIOMECHANICAL_ROM_STABILITY = DataCoreProtocol(
        id = PROTO_BIOMECHANICAL_ROM_STABILITY,
        name = "Protocolo de Cinemática Biomecânica Dinâmica (100 Hz)",
        version = "1.0.0",
        description = "Aquisição contínua de trajetórias articulares, amplitude de movimento (ROM), acelerações e simetria bilateral em 100 Hz.",
        category = "BIOMECHANICAL_CONTROL",
        requiredInputs = listOf("joint_angle_deg", "acceleration_g", "velocity_ms", "symmetry_pct"),
        methodology = "Captura síncrona de no mínimo 5 repetições completas com amostragem cinemática contínua a 100 Hz.",
        validityRules = ProtocolValidityRules(
            allowedUnits = listOf("°", "deg", "%", "m/s", "m/s²", "g"),
            valueRange = ProtocolValidityRange(
                minAllowed = -180.0,
                maxAllowed = 360.0,
                expectedUnit = "°",
                description = "Intervalo angular e cinemático dinâmico."
            ),
            minSamplingDurationSeconds = 3,
            minSamplingRateHz = 100,
            maxClockSkewToleranceMs = 60_000L,
            maxDataAgeMs = 90L * 24 * 60 * 60 * 1000
        )
    )

    private val PROTOCOLS_MAP: Map<String, DataCoreProtocol> = listOf(
        BASELINE_SUBMAX_VO2,
        LPT_RELATIVE_STRENGTH,
        IMTP_RFD,
        RESTING_HRV_RMSSD,
        CRITICAL_POWER_3MIN,
        BIOMECHANICAL_ROM_STABILITY
    ).associateBy { it.id }

    fun getProtocolById(id: String): DataCoreProtocol? = PROTOCOLS_MAP[id]

    fun getAllProtocols(): List<DataCoreProtocol> = PROTOCOLS_MAP.values.toList()
}
