package com.example.core.evidenceconsistency.policy

import com.example.core.datacore.metrics.MetricCatalog
import com.example.core.datacore.protocols.ProtocolCatalog
import com.example.core.evidenceconsistency.model.ConsistencyPolicy
import com.example.core.evidenceconsistency.model.EvidenceValidityPolicy

object EvidenceValidityPolicyRegistry {

    private const val ONE_DAY_MS = 86_400_000L

    private val policies = mutableMapOf<String, EvidenceValidityPolicy>()

    init {
        // Política Aprovada: Força Relativa por LPT Calibrado (Janela de 60 dias)
        registerPolicy(
            EvidenceValidityPolicy(
                policyId = "POL-VAL-LPT-STR-V1",
                metricId = MetricCatalog.METRIC_RELATIVE_STRENGTH,
                protocolId = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
                context = "STANDARDIZED_RESISTANCE_TRAINING",
                validityWindowMillis = 60 * ONE_DAY_MS,
                validityUnit = "DAYS",
                rationale = "Normativa neuromuscular padrão: adaptações e capacidade de pico de 1RM/BW mantêm representatividade temporal por até 60 dias sob protocolo controlado de LPT.",
                source = "PerformAI Physiological Standards Board - Guideline 2026.1",
                version = "1.0.0",
                status = "APPROVED",
                limitations = "Válido apenas quando mantida rotina de treino sem destreinamento agudo ou lesão documentada.",
                effectiveFrom = 1704067200000L
            )
        )

        // Política Aprovada: VO2 Max por Teste Submáximo / Rampa (Janela de 45 dias)
        registerPolicy(
            EvidenceValidityPolicy(
                policyId = "POL-VAL-RAMP-VO2-V1",
                metricId = MetricCatalog.METRIC_VO2_MAX,
                protocolId = ProtocolCatalog.PROTO_BASELINE_SUBMAX_VO2,
                context = "CARDIORESPIRATORY_ASSESSMENT",
                validityWindowMillis = 45 * ONE_DAY_MS,
                validityUnit = "DAYS",
                rationale = "Capacidade cardiorrespiratória máxima apresenta decaimento significativo de cinética em períodos superiores a 45 dias sem reavaliação laboratorial.",
                source = "PerformAI Endurance Consortium Protocol 2026",
                version = "1.0.0",
                status = "APPROVED",
                limitations = "Requer manutenção de volume de treinamento aeróbico constante no período.",
                effectiveFrom = 1704067200000L
            )
        )

        // Política PENDING_VALIDATION: RFD (Rate of Force Development)
        registerPolicy(
            EvidenceValidityPolicy(
                policyId = "POL-VAL-ISOM-RFD-PENDING",
                metricId = MetricCatalog.METRIC_RFD,
                protocolId = ProtocolCatalog.PROTO_IMTP_RFD,
                context = "EXPLOSIVE_FORCE_TESTING",
                validityWindowMillis = null,
                validityUnit = "DAYS",
                rationale = "Aguardando consenso metodológico sobre janela de validade para taxas de desenvolvimento de força isométrica.",
                source = "PerformAI Methodology Working Group",
                version = "1.0.0-draft",
                status = "PENDING_VALIDATION",
                limitations = "PENDING_CORE_METHODOLOGY_DECISION: Janela temporal de validade não homologada.",
                effectiveFrom = 1704067200000L
            )
        )

        // Política PENDING_VALIDATION: Critical Power
        registerPolicy(
            EvidenceValidityPolicy(
                policyId = "POL-VAL-ERG-CP-PENDING",
                metricId = MetricCatalog.METRIC_CRITICAL_POWER,
                protocolId = ProtocolCatalog.PROTO_CRITICAL_POWER_3MIN_ALL_OUT,
                context = "ANAEROBIC_WORK_CAPACITY",
                validityWindowMillis = null,
                validityUnit = "DAYS",
                rationale = "Parâmetros de Critical Power e W' dependem de validação de modelo de potência crítica multi-sessão vs 3-min all out.",
                source = "PerformAI Methodology Working Group",
                version = "1.0.0-draft",
                status = "PENDING_VALIDATION",
                limitations = "PENDING_CORE_METHODOLOGY_DECISION: Ausência de regra de expiração validada formalmente.",
                effectiveFrom = 1704067200000L
            )
        )
    }

    fun registerPolicy(policy: EvidenceValidityPolicy) {
        val key = "${policy.metricId}::${policy.protocolId}::${policy.version}"
        policies[key] = policy
    }

    fun getPolicy(metricId: String, protocolId: String, version: String = "1.0.0"): EvidenceValidityPolicy {
        val key = "$metricId::$protocolId::$version"
        val exactMatch = policies[key]
        if (exactMatch != null) return exactMatch

        // Busca por fallback para a métrica/protocolo
        val matchByMetricProtocol = policies.values.firstOrNull { it.metricId == metricId && it.protocolId == protocolId }
        if (matchByMetricProtocol != null) return matchByMetricProtocol

        // Default PENDING_VALIDATION para métricas sem política registrada
        return EvidenceValidityPolicy(
            policyId = "POL-VAL-GENERIC-PENDING",
            metricId = metricId,
            protocolId = protocolId,
            context = "DEFAULT",
            validityWindowMillis = null,
            validityUnit = "DAYS",
            rationale = "Nenhuma política de validade temporal homologada encontrada para o par métrica/protocolo.",
            source = "PerformAI Core Rules",
            version = "1.0.0",
            status = "PENDING_VALIDATION",
            limitations = "PENDING_CORE_METHODOLOGY_DECISION: Sem definição formal de janela temporal.",
            effectiveFrom = 1704067200000L
        )
    }

    fun getAllPolicies(): List<EvidenceValidityPolicy> = policies.values.toList()
}

object ConsistencyPolicyRegistry {

    private const val THIRTY_DAYS_MS = 30 * 86_400_000L

    private val policies = mutableMapOf<String, ConsistencyPolicy>()

    init {
        registerPolicy(
            ConsistencyPolicy(
                policyId = "POL-CON-LPT-STR-V1",
                metricId = MetricCatalog.METRIC_RELATIVE_STRENGTH,
                protocolId = ProtocolCatalog.PROTO_LPT_RELATIVE_STRENGTH,
                aggregationMethod = "WEIGHTED_RECENCY_MEAN",
                minimumObservations = 3,
                observationWindowMillis = THIRTY_DAYS_MS,
                outlierPolicy = "Z_SCORE_THRESHOLD_2_5",
                version = "1.0.0",
                status = "APPROVED",
                limitations = "Requer medições realizadas com o mesmo encoder calibrado e sob mesmo protocolo de repetições máximas.",
                effectiveFrom = 1704067200000L
            )
        )

        registerPolicy(
            ConsistencyPolicy(
                policyId = "POL-CON-RAMP-VO2-V1",
                metricId = MetricCatalog.METRIC_VO2_MAX,
                protocolId = ProtocolCatalog.PROTO_BASELINE_SUBMAX_VO2,
                aggregationMethod = "HIGHEST_VALIDATED_WITHIN_WINDOW",
                minimumObservations = 2,
                observationWindowMillis = THIRTY_DAYS_MS,
                outlierPolicy = "RESPIRATORY_EXCHANGE_RATIO_RER_CHECK",
                version = "1.0.0",
                status = "APPROVED",
                limitations = "Requer confirmação de critério de platô de VO2 ou RER > 1.10 para validação fisiológica.",
                effectiveFrom = 1704067200000L
            )
        )

        registerPolicy(
            ConsistencyPolicy(
                policyId = "POL-CON-GENERIC-PENDING",
                metricId = MetricCatalog.METRIC_RFD,
                protocolId = ProtocolCatalog.PROTO_IMTP_RFD,
                aggregationMethod = "PENDING_CORE_METHODOLOGY_DECISION",
                minimumObservations = null,
                observationWindowMillis = null,
                outlierPolicy = "PENDING_CORE_METHODOLOGY_DECISION",
                version = "1.0.0-draft",
                status = "PENDING_VALIDATION",
                limitations = "PENDING_CORE_METHODOLOGY_DECISION: Metodologia de agregação longitudinal de RFD não aprovada.",
                effectiveFrom = 1704067200000L
            )
        )
    }

    fun registerPolicy(policy: ConsistencyPolicy) {
        val key = "${policy.metricId}::${policy.protocolId}::${policy.version}"
        policies[key] = policy
    }

    fun getPolicy(metricId: String, protocolId: String, version: String = "1.0.0"): ConsistencyPolicy {
        val key = "$metricId::$protocolId::$version"
        val exactMatch = policies[key]
        if (exactMatch != null) return exactMatch

        val matchByMetricProtocol = policies.values.firstOrNull { it.metricId == metricId && it.protocolId == protocolId }
        if (matchByMetricProtocol != null) return matchByMetricProtocol

        return ConsistencyPolicy(
            policyId = "POL-CON-DEFAULT-PENDING",
            metricId = metricId,
            protocolId = protocolId,
            aggregationMethod = "PENDING_CORE_METHODOLOGY_DECISION",
            minimumObservations = null,
            observationWindowMillis = null,
            outlierPolicy = "PENDING_CORE_METHODOLOGY_DECISION",
            version = "1.0.0",
            status = "PENDING_VALIDATION",
            limitations = "PENDING_CORE_METHODOLOGY_DECISION: Sem metodologia de consistência aprovada para o par métrica/protocolo.",
            effectiveFrom = 1704067200000L
        )
    }

    fun getAllPolicies(): List<ConsistencyPolicy> = policies.values.toList()
}
