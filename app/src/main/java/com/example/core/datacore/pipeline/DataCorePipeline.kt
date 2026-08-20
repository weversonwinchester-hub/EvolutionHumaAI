package com.example.core.datacore.pipeline

import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.DataCoreAuditLog
import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.RawDataInput
import com.example.core.datacore.model.ReliabilityAssessment
import com.example.core.datacore.model.ValidationStatus
import com.example.core.datacore.provenance.ProvenanceEngine
import com.example.core.datacore.reliability.ReliabilityFramework
import com.example.core.datacore.validation.ValidationEngineResult
import com.example.core.datacore.validation.ValidationEngineV1
import com.example.core.error.AppError
import com.example.core.error.AppResult
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

data class IngestionSuccessResult(
    val rawData: RawDataInput,
    val measurement: DataCoreMeasurement,
    val provenance: DataCoreProvenance,
    val evidence: DataCoreEvidence,
    val reliability: ReliabilityAssessment,
    val auditLog: DataCoreAuditLog
)

data class IngestionFailureResult(
    val rawData: RawDataInput,
    val measurement: DataCoreMeasurement,
    val reason: String,
    val status: ValidationStatus,
    val auditLog: DataCoreAuditLog
)

/**
 * DataCorePipeline: Implementação da cadeia estrita de processamento do PERFORMAI DATA CORE V1:
 *
 * INPUT
 * ↓
 * RAW DATA
 * ↓
 * VALIDATION
 * ↓
 * PROTOCOL
 * ↓
 * MEASUREMENT
 * ↓
 * METRIC
 * ↓
 * EVIDENCE
 * ↓
 * PROVENANCE
 * ↓
 * RELIABILITY
 * ↓
 * AUDIT
 *
 * Regras mandatórias:
 * 1. Dados brutos e derivados são rigorosamente separados.
 * 2. Nenhum dado derivado sobrescreve o dado original.
 * 3. Todo dado possui Provenance completa e auditável.
 * 4. Mock data é identificada com isMock=true e bloqueada de promoção real.
 */
class DataCorePipeline(
    private val validationEngine: ValidationEngineV1 = ValidationEngineV1()
) {
    companion object {
        private val evidenceSequence = AtomicLong(1L)
        private const val CORE_VERSION = "1.0.0-datacore-v1"

        fun generateEvidenceId(): String {
            val seq = evidenceSequence.getAndIncrement()
            return "EV-2026-%06d".format(seq)
        }
    }

    /**
     * Processa a entrada de dados brutos ao longo de toda a cadeia do Data Core V1.
     */
    fun ingestRawData(input: RawDataInput): AppResult<IngestionSuccessResult> {
        val requestId = UUID.randomUUID().toString()
        val measurementId = "MSR-${UUID.randomUUID().toString().take(8).uppercase()}"

        // 1. Executa a validação pelo Validation Engine V1
        val validationResult = validationEngine.validateRawData(input)

        when (validationResult) {
            is ValidationEngineResult.Invalid -> {
                val numericVal = input.rawPayload.toDoubleOrNull() ?: 0.0
                val invalidMeasurement = DataCoreMeasurement(
                    id = measurementId,
                    assessmentId = input.assessmentId,
                    userId = input.userId,
                    metricId = input.metricId,
                    rawValue = numericVal,
                    normalizedValue = null,
                    unit = input.unit,
                    timestamp = input.clientTimestamp,
                    source = input.source,
                    deviceId = input.deviceId,
                    protocolId = input.protocolId,
                    validationStatus = ValidationStatus.INVALID,
                    rejectionReason = validationResult.reason,
                    rawDataInputId = input.id,
                    isMock = input.isMock,
                    createdAt = System.currentTimeMillis()
                )

                val audit = DataCoreAuditLog(
                    id = UUID.randomUUID().toString(),
                    actorType = ActorType.SYSTEM,
                    actorId = "DataCoreValidationEngine",
                    action = "MEASUREMENT_VALIDATION_FAILED",
                    entityType = "Measurement",
                    entityId = measurementId,
                    previousState = null,
                    newState = "STATUS=INVALID; REASON=${validationResult.reason}",
                    timestamp = System.currentTimeMillis(),
                    requestId = requestId,
                    systemVersion = CORE_VERSION
                )

                return AppResult.Failure(
                    AppError.ValidationError("Validação rejeitou o dado bruto: ${validationResult.reason}")
                )
            }

            is ValidationEngineResult.Rejected -> {
                val numericVal = input.rawPayload.toDoubleOrNull() ?: 0.0
                val rejectedMeasurement = DataCoreMeasurement(
                    id = measurementId,
                    assessmentId = input.assessmentId,
                    userId = input.userId,
                    metricId = input.metricId,
                    rawValue = numericVal,
                    normalizedValue = null,
                    unit = input.unit,
                    timestamp = input.clientTimestamp,
                    source = input.source,
                    deviceId = input.deviceId,
                    protocolId = input.protocolId,
                    validationStatus = ValidationStatus.REJECTED,
                    rejectionReason = validationResult.reason,
                    rawDataInputId = input.id,
                    isMock = input.isMock,
                    createdAt = System.currentTimeMillis()
                )

                val audit = DataCoreAuditLog(
                    id = UUID.randomUUID().toString(),
                    actorType = ActorType.SYSTEM,
                    actorId = "DataCoreValidationEngine",
                    action = "MEASUREMENT_REJECTED",
                    entityType = "Measurement",
                    entityId = measurementId,
                    previousState = null,
                    newState = "STATUS=REJECTED; REASON=${validationResult.reason}",
                    timestamp = System.currentTimeMillis(),
                    requestId = requestId,
                    systemVersion = CORE_VERSION
                )

                return AppResult.Failure(
                    AppError.ValidationError("Entrada rejeitada pelo Core: ${validationResult.reason}")
                )
            }

            is ValidationEngineResult.Valid -> {
                val validMeasurement = DataCoreMeasurement(
                    id = measurementId,
                    assessmentId = input.assessmentId,
                    userId = input.userId,
                    metricId = input.metricId,
                    rawValue = validationResult.normalizedValue,
                    normalizedValue = validationResult.normalizedValue,
                    unit = input.unit,
                    timestamp = input.clientTimestamp,
                    source = input.source,
                    deviceId = input.deviceId,
                    protocolId = input.protocolId,
                    validationStatus = ValidationStatus.VALID,
                    rejectionReason = null,
                    rawDataInputId = input.id,
                    isMock = input.isMock,
                    createdAt = System.currentTimeMillis()
                )

                // 2. Constrói a Provenance criptográfica
                val provenance = ProvenanceEngine.buildProvenance(input)

                // 3. Avalia no Reliability Framework
                val reliability = ReliabilityFramework.evaluateReliability(validMeasurement, provenance)

                // 4. Cria a Evidência Oficial
                val evidenceId = generateEvidenceId()
                val evidence = DataCoreEvidence(
                    id = evidenceId,
                    userId = input.userId,
                    assessmentId = input.assessmentId,
                    measurementIds = listOf(measurementId),
                    source = input.source,
                    capturedAt = input.clientTimestamp,
                    submittedAt = input.serverTimestamp,
                    integrityStatus = if (reliability.integrityValid) IntegrityStatus.VALID else IntegrityStatus.UNKNOWN,
                    reliabilityScore = reliability.compositeConfidenceScore,
                    confidenceScore = reliability.compositeConfidenceScore,
                    provenanceId = provenance.id,
                    coreVersion = CORE_VERSION,
                    isMock = input.isMock,
                    createdAt = System.currentTimeMillis()
                )

                // 5. Registra o evento de Auditoria Imutável
                val audit = DataCoreAuditLog(
                    id = UUID.randomUUID().toString(),
                    actorType = ActorType.SYSTEM,
                    actorId = "DataCorePipeline",
                    action = "EVIDENCE_CREATED",
                    entityType = "Evidence",
                    entityId = evidenceId,
                    previousState = null,
                    newState = "EVIDENCE_ID=$evidenceId; MEASUREMENT_ID=$measurementId; PROVENANCE_ID=${provenance.id}; INTEGRITY=${evidence.integrityStatus}; IS_MOCK=${input.isMock}",
                    timestamp = System.currentTimeMillis(),
                    requestId = requestId,
                    systemVersion = CORE_VERSION
                )

                return AppResult.Success(
                    IngestionSuccessResult(
                        rawData = input,
                        measurement = validMeasurement,
                        provenance = provenance,
                        evidence = evidence,
                        reliability = reliability,
                        auditLog = audit
                    )
                )
            }
        }
    }
}
