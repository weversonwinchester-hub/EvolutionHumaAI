package com.example.core.evidenceconsistency.model

import com.example.core.datacore.model.IntegrityStatus

// ==========================================
// 1. ESTADOS DE VALIDADE TEMPORAL
// ==========================================
enum class ValidityStatus {
    CURRENT,             // Evidência dentro do prazo de validade de política aprovada
    AGING,               // Evidência próxima da expiração (zona de atenção metodológica)
    EXPIRED,             // Evidência ultrapassou a janela aprovada por política formal
    UNKNOWN,             // Contexto temporal não pôde ser determinado
    PENDING_VALIDATION,  // Não existe política aprovada de validade temporal para a métrica/protocolo
    INVALID              // Evidência falhou na integridade, proveniência ou validação
}

// ==========================================
// 2. ESTADOS DE CONSISTÊNCIA TEMPORAL
// ==========================================
enum class ConsistencyStatus {
    INSUFFICIENT_DATA,   // Número de medições insuficiente para análise de consistência
    STABLE,              // Medições consistentes e estáveis segundo metodologia aprovada
    VARIABLE,            // Alta variabilidade observada segundo metodologia aprovada
    IMPROVING,           // Tendência longitudinal positiva confirmada metodologicamente
    DECLINING,           // Tendência longitudinal de declínio confirmada metodologicamente
    UNDETERMINED,        // Dados presentes mas inconclusivos
    PENDING_VALIDATION   // Metodologia de agregação/consistência ainda pendente de validação
}

// ==========================================
// 3. ESTADOS DE MATURIDADE DE EVIDÊNCIAS
// ==========================================
enum class MaturityStatus {
    INITIAL,             // Evidências iniciais / pontuais registradas
    DEVELOPING,          // Cobertura em desenvolvimento com múltiplas medições
    ESTABLISHED,         // Histórico consistente e protocolo estável comprovado
    MATURE,              // Histórico longitudinal maduro com alta repetibilidade
    UNDETERMINED,        // Estado de maturidade indeterminado
    PENDING_VALIDATION   // Critérios de maturidade pendentes de validação formal
}

// ==========================================
// 4. FLAGS DE CONTINUIDADE DE PROTOCOLO
// ==========================================
enum class ProtocolContinuityFlag {
    CONTINUOUS,          // Todos os parâmetros de protocolo e dispositivo mantiveram-se idênticos
    PROTOCOL_CHANGED,    // Protocolo de teste foi alterado entre medições
    DEVICE_CHANGED,      // Instrumento ou sensor de captura foi alterado
    METHOD_CHANGED,      // Método de cálculo ou processamento foi alterado
    UNIT_CHANGED,        // Unidade de medida foi alterada
    CONTEXT_CHANGED      // População, ambiente ou contexto de referência alterado
}

// ==========================================
// 5. POLÍTICA DE VALIDADE TEMPORAL
// ==========================================
data class EvidenceValidityPolicy(
    val policyId: String,
    val metricId: String,
    val protocolId: String,
    val context: String = "GENERAL_POPULATION",
    val validityWindowMillis: Long?,
    val validityUnit: String,
    val rationale: String,
    val source: String,
    val version: String,
    val status: String, // "APPROVED", "PENDING_VALIDATION"
    val limitations: String,
    val effectiveFrom: Long
)

// ==========================================
// 6. AVALIAÇÃO DE VALIDADE DE EVIDÊNCIA
// ==========================================
data class EvidenceValidityAssessment(
    val evidenceId: String,
    val metricId: String,
    val protocolId: String,
    val capturedAt: Long,
    val evaluatedAt: Long,
    val validityStatus: ValidityStatus,
    val policyId: String,
    val policyVersion: String,
    val expirationTimestamp: Long?,
    val ageMillis: Long,
    val rejectionReason: String? = null,
    val limitations: String
)

// ==========================================
// 7. POLÍTICA DE CONSISTÊNCIA TEMPORAL
// ==========================================
data class ConsistencyPolicy(
    val policyId: String,
    val metricId: String,
    val protocolId: String,
    val aggregationMethod: String,
    val minimumObservations: Int?,
    val observationWindowMillis: Long?,
    val outlierPolicy: String,
    val version: String,
    val status: String, // "APPROVED", "PENDING_VALIDATION"
    val limitations: String,
    val effectiveFrom: Long
)

// ==========================================
// 8. AVALIAÇÃO DE CONSISTÊNCIA DE MÉTRICA
// ==========================================
data class MetricConsistencyAssessment(
    val metricId: String,
    val protocolId: String,
    val evidenceIds: List<String>,
    val measurementCount: Int,
    val consistencyStatus: ConsistencyStatus,
    val policyId: String,
    val policyVersion: String,
    val variationCoefficient: Double?,
    val trendDirection: String?,
    val methodologyVersion: String,
    val limitations: String
)

// ==========================================
// 9. AVALIAÇÃO DE REPETIBILIDADE
// ==========================================
data class RepeatabilityAssessment(
    val metricId: String,
    val protocolId: String,
    val evidenceIds: List<String>,
    val result: String, // "HIGH", "MODERATE", "LOW", "UNDETERMINED", "PENDING_VALIDATION"
    val repeatabilityScore: Double?,
    val methodologyVersion: String,
    val status: ConsistencyStatus,
    val limitations: String
)

// ==========================================
// 10. AVALIAÇÃO DE CONTINUIDADE DE PROTOCOLO
// ==========================================
data class ProtocolContinuityAssessment(
    val metricId: String,
    val flags: List<ProtocolContinuityFlag>,
    val details: List<String>,
    val isCompatibleForDirectComparison: Boolean,
    val firstObservedProtocol: String,
    val lastObservedProtocol: String,
    val firstObservedDevice: String?,
    val lastObservedDevice: String?
)

// ==========================================
// 11. SEQUÊNCIA MÉTRICA LONGITUDINAL
// ==========================================
data class LongitudinalMetricSequence(
    val metricId: String,
    val firstValidMeasurementTimestamp: Long?,
    val lastValidMeasurementTimestamp: Long?,
    val measurementCount: Int,
    val averageIntervalMillis: Long?,
    val maxGapMillis: Long?,
    val continuityAssessment: ProtocolContinuityAssessment,
    val consistencyAssessment: MetricConsistencyAssessment,
    val repeatabilityAssessment: RepeatabilityAssessment
)

// ==========================================
// 12. MATRIZ DE QUALIDADE DE EVIDÊNCIA
// ==========================================
data class EvidenceQualityMatrix(
    val sourceQualityTier: Int,
    val integrityScore: Double,
    val protocolFidelityScore: Double,
    val temporalValidityStatus: ValidityStatus,
    val consistencyStatus: ConsistencyStatus,
    val repeatabilityStatus: String,
    val overallMaturityStatus: MaturityStatus,
    val limitations: String
)

// ==========================================
// 13. MATURIDADE DE EVIDÊNCIAS
// ==========================================
data class EvidenceMaturity(
    val userId: String,
    val metricCoverage: Int,
    val temporalCoverageDays: Double,
    val protocolConsistency: Double,
    val evidenceCount: Int,
    val sourceQuality: String,
    val repeatability: String,
    val longitudinalCoverage: String,
    val maturityStatus: MaturityStatus,
    val methodologyVersion: String,
    val limitations: String
)

// ==========================================
// 14. EVOLUTION EVIDENCE PACKAGE (IMUTÁVEL)
// Pacote estruturado para o futuro Evolution Engine
// ==========================================
data class EvolutionEvidencePackage(
    val id: String,
    val userId: String,
    val generatedAt: Long,
    val coreVersion: String,
    val engineVersion: String,
    val evidenceIds: List<String>,
    val validMetrics: List<String>,
    val invalidMetrics: List<String>,
    val expiredEvidenceIds: List<String>,
    val pendingValidationItems: List<String>,
    val validityAssessments: List<EvidenceValidityAssessment>,
    val consistencyAssessments: Map<String, MetricConsistencyAssessment>,
    val repeatabilityAssessments: Map<String, RepeatabilityAssessment>,
    val continuityAssessments: Map<String, ProtocolContinuityAssessment>,
    val longitudinalSequences: Map<String, LongitudinalMetricSequence>,
    val overallConsistencyStatus: ConsistencyStatus,
    val overallRepeatabilityStatus: String,
    val overallMaturity: EvidenceMaturity,
    val qualityMatrix: EvidenceQualityMatrix,
    val limitations: List<String>,
    val auditReference: String,
    val isMock: Boolean,
    val simulationMode: Boolean
)
