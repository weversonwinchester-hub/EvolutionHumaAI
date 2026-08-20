package com.example.core.datacore.validation

import com.example.core.datacore.metrics.MetricCatalog
import com.example.core.datacore.model.RawDataInput
import com.example.core.datacore.model.ValidationStatus
import com.example.core.datacore.protocols.ProtocolCatalog

sealed class ValidationEngineResult {
    data class Valid(
        val normalizedValue: Double,
        val validationNotes: String = "Validação completa com sucesso pelo Validation Engine V1."
    ) : ValidationEngineResult()

    data class Invalid(
        val reason: String,
        val validationStatus: ValidationStatus = ValidationStatus.INVALID
    ) : ValidationEngineResult()

    data class Rejected(
        val reason: String,
        val validationStatus: ValidationStatus = ValidationStatus.REJECTED
    ) : ValidationEngineResult()
}

/**
 * ValidationEngineV1: Camada oficial de validação do PERFORMAI DATA CORE V1.
 *
 * Executa checagens determinísticas:
 * 1. Campos obrigatórios e integridade estrutural
 * 2. Existência e compatibilidade da Métrica e do Protocolo
 * 3. Compatibilidade estrita de unidade
 * 4. Intervalo numérico permitido pelo protocolo
 * 5. Consistência temporal (rejeição de timestamps futuros ou expirados)
 * 6. Origem e identificação de fonte/dispositivo
 * 7. Detecção de duplicação
 *
 * REGRA CRÍTICA: Um valor inválido NUNCA é corrigido silenciosamente.
 * Ele é explicitamente marcado como INVALID ou REJECTED com registro do motivo.
 */
class ValidationEngineV1(
    private val knownSignatures: MutableSet<String> = mutableSetOf()
) {

    fun validateRawData(input: RawDataInput): ValidationEngineResult {
        val currentTime = System.currentTimeMillis()

        // 1. Campos Obrigatórios
        if (input.userId.isBlank()) {
            return ValidationEngineResult.Rejected("Rejeição: 'userId' é obrigatório e não pode ser vazio.")
        }
        if (input.metricId.isBlank()) {
            return ValidationEngineResult.Rejected("Rejeição: 'metricId' é obrigatório.")
        }
        if (input.protocolId.isBlank()) {
            return ValidationEngineResult.Rejected("Rejeição: 'protocolId' é obrigatório.")
        }
        if (input.unit.isBlank()) {
            return ValidationEngineResult.Rejected("Rejeição: 'unit' (unidade de medida) é obrigatória.")
        }
        if (input.source.isBlank() || input.sourceType.isBlank() || input.sourceIdentifier.isBlank()) {
            return ValidationEngineResult.Rejected("Rejeição: Origem incompleta. 'source', 'sourceType' e 'sourceIdentifier' são obrigatórios para auditoria e rastreabilidade.")
        }

        // 2. Verificação de Existência no Catálogo de Métricas
        val metric = MetricCatalog.getMetricById(input.metricId)
            ?: return ValidationEngineResult.Rejected("Rejeição: Métrica '${input.metricId}' não reconhecida pelo Core Engine.")

        // 3. Verificação de Existência no Catálogo de Protocolos
        val protocol = ProtocolCatalog.getProtocolById(input.protocolId)
            ?: return ValidationEngineResult.Rejected("Rejeição: Protocolo '${input.protocolId}' não reconhecido pelo Core Engine.")

        // 4. Verificação de Associação Métrica x Protocolo
        if (!metric.protocolIds.contains(protocol.id)) {
            return ValidationEngineResult.Rejected(
                "Rejeição: Protocolo '${protocol.id}' não é válido para a métrica '${metric.id}'. " +
                "Protocolos aceitos para ${metric.name}: ${metric.protocolIds.joinToString(", ")}."
            )
        }

        // 5. Verificação de Tipo e Parse do Valor Numérico Bruto
        val numericValue = input.rawPayload.toDoubleOrNull()
            ?: return ValidationEngineResult.Invalid(
                "Inválido: O valor bruto fornecido ('${input.rawPayload}') não é um número válido."
            )

        // 6. Verificação de Compatibilidade de Unidade
        val isUnitAllowed = protocol.validityRules.allowedUnits.any { it.equals(input.unit.trim(), ignoreCase = true) }
        if (!isUnitAllowed) {
            return ValidationEngineResult.Rejected(
                "Rejeição de Unidade: Unidade '${input.unit}' incompatível com o protocolo '${protocol.id}'. " +
                "Unidades aceitas: ${protocol.validityRules.allowedUnits.joinToString(", ")}."
            )
        }

        // 7. Verificação de Intervalo Fisiológico / Protocolar
        val range = protocol.validityRules.valueRange
        if (numericValue < range.minAllowed || numericValue > range.maxAllowed) {
            return ValidationEngineResult.Invalid(
                "Inválido: Valor $numericValue ${input.unit} fora do intervalo permitido pelo protocolo '${protocol.id}' " +
                "[Mínimo: ${range.minAllowed}, Máximo: ${range.maxAllowed}]. Motivo: ${range.description}"
            )
        }

        // 8. Verificação Temporal (Clock Skew e Idade Máxima)
        val maxFutureSkew = protocol.validityRules.maxClockSkewToleranceMs
        if (input.clientTimestamp > currentTime + maxFutureSkew) {
            val diffSec = (input.clientTimestamp - currentTime) / 1000
            return ValidationEngineResult.Rejected(
                "Rejeição Temporal: Timestamp do cliente no futuro detectado (+${diffSec}s além da tolerância de ${maxFutureSkew / 1000}s)."
            )
        }

        val maxAge = protocol.validityRules.maxDataAgeMs
        if (input.clientTimestamp < currentTime - maxAge) {
            val ageDays = (currentTime - input.clientTimestamp) / (1000L * 60 * 60 * 24)
            return ValidationEngineResult.Rejected(
                "Rejeição Temporal: Dado expirado. Captura realizada há $ageDays dias (máximo permitido: ${maxAge / (1000L * 60 * 60 * 24)} dias)."
            )
        }

        // 9. Verificação de Duplicação (Idempotency Fingerprint)
        val fingerprint = "${input.userId}:${input.metricId}:${numericValue}:${input.clientTimestamp}:${input.sourceIdentifier}"
        if (knownSignatures.contains(fingerprint)) {
            return ValidationEngineResult.Rejected(
                "Rejeição de Duplicação: Medição idêntica já foi submetida e processada anteriormente (Fingerprint: $fingerprint)."
            )
        }
        knownSignatures.add(fingerprint)

        // Validação Aprovada
        return ValidationEngineResult.Valid(
            normalizedValue = numericValue,
            validationNotes = "Validação concluída com sucesso segundo o protocolo ${protocol.name} v${protocol.version}."
        )
    }
}
