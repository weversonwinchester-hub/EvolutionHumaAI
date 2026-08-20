package com.example.core.datacore.provenance

import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.datacore.model.RawDataInput
import java.security.MessageDigest
import java.util.UUID

/**
 * ProvenanceEngine: Responsável pela rastreabilidade e integridade criptográfica de dados.
 *
 * Rastreia a linhagem completa:
 * INPUT -> SOURCE -> CAPTURE -> PROCESSING -> METRIC -> EVIDENCE -> RESULT
 */
object ProvenanceEngine {

    private const val CORE_PROCESSING_VERSION = "1.0.0-datacore-v1"

    fun generateIntegrityHash(
        userId: String,
        rawPayload: String,
        sourceIdentifier: String,
        captureTimestamp: Long,
        protocolId: String
    ): String {
        val rawString = "$userId|$rawPayload|$sourceIdentifier|$captureTimestamp|$protocolId"
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawString.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun buildProvenance(input: RawDataInput): DataCoreProvenance {
        val integrityHash = generateIntegrityHash(
            userId = input.userId,
            rawPayload = input.rawPayload,
            sourceIdentifier = input.sourceIdentifier,
            captureTimestamp = input.clientTimestamp,
            protocolId = input.protocolId
        )

        return DataCoreProvenance(
            id = "PROV-${UUID.randomUUID().toString().take(8).uppercase()}",
            sourceType = input.sourceType,
            sourceIdentifier = input.sourceIdentifier,
            deviceIdentifier = input.deviceId,
            captureTimestamp = input.clientTimestamp,
            processingTimestamp = System.currentTimeMillis(),
            processingVersion = CORE_PROCESSING_VERSION,
            protocolId = input.protocolId,
            integrityHash = integrityHash,
            createdAt = System.currentTimeMillis()
        )
    }
}
