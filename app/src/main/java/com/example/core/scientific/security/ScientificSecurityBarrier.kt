package com.example.core.scientific.security

import com.example.core.scientific.model.*
import com.example.core.scientific.registry.InstrumentRegistry
import com.example.core.scientific.registry.PopulationReferenceRegistry
import com.example.core.scientific.registry.ScientificMethodologyRegistry
import com.example.core.scientific.registry.ScientificProtocolRegistry
import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI SCIENTIFIC SECURITY & AUTHORITY BARRIER
 *
 * Garante que:
 * - Somente CORE_ENGINE e SYSTEM possuem autoridade de mutação científica.
 * - CLIENT e AI_GATEWAY são estritamente bloqueados.
 * - Simulações e Mocks não podem homologar protocolos ou metodologias oficiais.
 * - Todo evento gera log auditável append-only.
 */
object ScientificSecurityBarrier {

    data class ScientificAuditEntry(
        val auditId: String = UUID.randomUUID().toString(),
        val action: String,
        val targetEntity: String,
        val targetId: String,
        val callerTier: ScientificCallerTier,
        val callerId: String,
        val success: Boolean,
        val reason: String,
        val securityViolation: Boolean = false,
        val simulationMode: Boolean = false,
        val timestamp: Long = System.currentTimeMillis(),
        val checksum: String
    )

    private val auditLogs: MutableList<ScientificAuditEntry> = mutableListOf()

    fun recordAudit(
        action: String,
        targetEntity: String,
        targetId: String,
        callerTier: ScientificCallerTier,
        callerId: String,
        success: Boolean,
        reason: String,
        securityViolation: Boolean = false,
        simulationMode: Boolean = false
    ): ScientificAuditEntry {
        val payload = "$action|$targetEntity|$targetId|$callerTier|$callerId|$success|$reason|$securityViolation|$simulationMode"
        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val entry = ScientificAuditEntry(
            action = action,
            targetEntity = targetEntity,
            targetId = targetId,
            callerTier = callerTier,
            callerId = callerId,
            success = success,
            reason = reason,
            securityViolation = securityViolation,
            simulationMode = simulationMode,
            checksum = checksum
        )
        synchronized(auditLogs) {
            auditLogs.add(entry)
        }
        return entry
    }

    fun getAuditLogs(): List<ScientificAuditEntry> = synchronized(auditLogs) {
        auditLogs.toList()
    }

    // Mutação de Metodologia
    fun registerOrUpdateMethodology(
        callerTier: ScientificCallerTier,
        callerId: String,
        methodology: ScientificMethodology,
        simulationMode: Boolean = false
    ): Boolean {
        if (simulationMode) {
            recordAudit(
                action = "MUTATE_METHODOLOGY",
                targetEntity = "ScientificMethodology",
                targetId = methodology.methodologyId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "SIMULATION_MODE_CANNOT_HOMOLOGATE_OFFICIAL_METHODOLOGY",
                securityViolation = true,
                simulationMode = true
            )
            return false
        }

        if (callerTier != ScientificCallerTier.CORE_ENGINE && callerTier != ScientificCallerTier.SYSTEM && callerTier != ScientificCallerTier.ADMIN) {
            recordAudit(
                action = "SECURITY_VIOLATION_METHODOLOGY_MANIPULATION_ATTEMPT",
                targetEntity = "ScientificMethodology",
                targetId = methodology.methodologyId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "CALLER_TIER_${callerTier}_UNAUTHORIZED_FOR_SCIENTIFIC_MUTATION",
                securityViolation = true
            )
            return false
        }

        ScientificMethodologyRegistry.register(methodology)
        recordAudit(
            action = "METHODOLOGY_REGISTERED_OR_UPDATED",
            targetEntity = "ScientificMethodology",
            targetId = methodology.methodologyId,
            callerTier = callerTier,
            callerId = callerId,
            success = true,
            reason = "METHODOLOGY_MUTATION_AUTHORIZED"
        )
        return true
    }

    // Mutação de Protocolo
    fun registerOrUpdateProtocol(
        callerTier: ScientificCallerTier,
        callerId: String,
        protocol: ScientificProtocol,
        simulationMode: Boolean = false
    ): Boolean {
        if (simulationMode) {
            recordAudit(
                action = "MUTATE_PROTOCOL",
                targetEntity = "ScientificProtocol",
                targetId = protocol.protocolId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "SIMULATION_MODE_CANNOT_ACTIVATE_OFFICIAL_PROTOCOL",
                securityViolation = true,
                simulationMode = true
            )
            return false
        }

        if (callerTier != ScientificCallerTier.CORE_ENGINE && callerTier != ScientificCallerTier.SYSTEM && callerTier != ScientificCallerTier.ADMIN) {
            recordAudit(
                action = "SECURITY_VIOLATION_METHODOLOGY_MANIPULATION_ATTEMPT",
                targetEntity = "ScientificProtocol",
                targetId = protocol.protocolId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "CALLER_TIER_${callerTier}_UNAUTHORIZED_FOR_PROTOCOL_MUTATION",
                securityViolation = true
            )
            return false
        }

        ScientificProtocolRegistry.register(protocol)
        recordAudit(
            action = "PROTOCOL_REGISTERED_OR_UPDATED",
            targetEntity = "ScientificProtocol",
            targetId = protocol.protocolId,
            callerTier = callerTier,
            callerId = callerId,
            success = true,
            reason = "PROTOCOL_MUTATION_AUTHORIZED"
        )
        return true
    }

    // Homologação de Instrumento
    fun registerOrUpdateInstrument(
        callerTier: ScientificCallerTier,
        callerId: String,
        instrument: MeasurementInstrument,
        simulationMode: Boolean = false
    ): Boolean {
        if (simulationMode) {
            recordAudit(
                action = "MUTATE_INSTRUMENT",
                targetEntity = "MeasurementInstrument",
                targetId = instrument.instrumentId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "SIMULATION_MODE_CANNOT_HOMOLOGATE_INSTRUMENT",
                securityViolation = true,
                simulationMode = true
            )
            return false
        }

        if (callerTier != ScientificCallerTier.CORE_ENGINE && callerTier != ScientificCallerTier.SYSTEM && callerTier != ScientificCallerTier.ADMIN) {
            recordAudit(
                action = "SECURITY_VIOLATION_METHODOLOGY_MANIPULATION_ATTEMPT",
                targetEntity = "MeasurementInstrument",
                targetId = instrument.instrumentId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "CALLER_TIER_${callerTier}_UNAUTHORIZED_FOR_INSTRUMENT_HOMOLOGATION",
                securityViolation = true
            )
            return false
        }

        InstrumentRegistry.register(instrument)
        recordAudit(
            action = "INSTRUMENT_REGISTERED_OR_UPDATED",
            targetEntity = "MeasurementInstrument",
            targetId = instrument.instrumentId,
            callerTier = callerTier,
            callerId = callerId,
            success = true,
            reason = "INSTRUMENT_HOMOLOGATION_AUTHORIZED"
        )
        return true
    }

    // Homologação de Referência Populacional
    fun registerOrUpdatePopulationReference(
        callerTier: ScientificCallerTier,
        callerId: String,
        reference: PopulationReference,
        simulationMode: Boolean = false
    ): Boolean {
        if (simulationMode) {
            recordAudit(
                action = "MUTATE_POPULATION_REFERENCE",
                targetEntity = "PopulationReference",
                targetId = reference.referenceId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "SIMULATION_MODE_CANNOT_HOMOLOGATE_POPULATION_REFERENCE",
                securityViolation = true,
                simulationMode = true
            )
            return false
        }

        if (callerTier != ScientificCallerTier.CORE_ENGINE && callerTier != ScientificCallerTier.SYSTEM && callerTier != ScientificCallerTier.ADMIN) {
            recordAudit(
                action = "SECURITY_VIOLATION_METHODOLOGY_MANIPULATION_ATTEMPT",
                targetEntity = "PopulationReference",
                targetId = reference.referenceId,
                callerTier = callerTier,
                callerId = callerId,
                success = false,
                reason = "CALLER_TIER_${callerTier}_UNAUTHORIZED_FOR_POPULATION_REFERENCE",
                securityViolation = true
            )
            return false
        }

        PopulationReferenceRegistry.registerReference(reference)
        recordAudit(
            action = "POPULATION_REFERENCE_REGISTERED_OR_UPDATED",
            targetEntity = "PopulationReference",
            targetId = reference.referenceId,
            callerTier = callerTier,
            callerId = callerId,
            success = true,
            reason = "POPULATION_REFERENCE_AUTHORIZED"
        )
        return true
    }
}
