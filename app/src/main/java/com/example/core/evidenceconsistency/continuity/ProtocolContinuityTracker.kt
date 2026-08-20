package com.example.core.evidenceconsistency.continuity

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.evidenceconsistency.model.ProtocolContinuityAssessment
import com.example.core.evidenceconsistency.model.ProtocolContinuityFlag

class ProtocolContinuityTracker {

    fun analyzeContinuity(
        metricId: String,
        measurements: List<DataCoreMeasurement>,
        evidences: List<DataCoreEvidence>
    ): ProtocolContinuityAssessment {
        if (measurements.isEmpty()) {
            return ProtocolContinuityAssessment(
                metricId = metricId,
                flags = listOf(ProtocolContinuityFlag.CONTINUOUS),
                details = listOf("Nenhuma medição registrada para análise de continuidade."),
                isCompatibleForDirectComparison = false,
                firstObservedProtocol = "N/A",
                lastObservedProtocol = "N/A",
                firstObservedDevice = null,
                lastObservedDevice = null
            )
        }

        val sortedMeasurements = measurements
            .filter { it.metricId == metricId }
            .sortedBy { it.timestamp }

        if (sortedMeasurements.isEmpty()) {
            return ProtocolContinuityAssessment(
                metricId = metricId,
                flags = listOf(ProtocolContinuityFlag.CONTINUOUS),
                details = listOf("Nenhuma medição correspondente à métrica $metricId."),
                isCompatibleForDirectComparison = false,
                firstObservedProtocol = "N/A",
                lastObservedProtocol = "N/A",
                firstObservedDevice = null,
                lastObservedDevice = null
            )
        }

        val flags = mutableSetOf<ProtocolContinuityFlag>()
        val details = mutableListOf<String>()

        val firstMsr = sortedMeasurements.first()
        val lastMsr = sortedMeasurements.last()

        val observedProtocols = sortedMeasurements.map { it.protocolId }.distinct()
        val observedDevices = sortedMeasurements.mapNotNull { it.deviceId }.distinct()
        val observedUnits = sortedMeasurements.map { it.unit }.distinct()
        val observedSources = sortedMeasurements.map { it.source }.distinct()

        if (observedProtocols.size > 1) {
            flags.add(ProtocolContinuityFlag.PROTOCOL_CHANGED)
            details.add("Descontinuidade de Protocolo: múltiplos protocolos detectados na série ($observedProtocols). Comparações diretas podem ser espúrias.")
        }

        if (observedDevices.size > 1) {
            flags.add(ProtocolContinuityFlag.DEVICE_CHANGED)
            details.add("Mudança de Dispositivo/Sensor: medições capturadas por equipamentos distintos ($observedDevices). Pode haver viés de calibração instrumental.")
        }

        if (observedUnits.size > 1) {
            flags.add(ProtocolContinuityFlag.UNIT_CHANGED)
            details.add("Incompatibilidade de Unidade: unidades de medida distintas detectadas ($observedUnits).")
        }

        if (observedSources.size > 1) {
            flags.add(ProtocolContinuityFlag.METHOD_CHANGED)
            details.add("Mudança de Fonte/Método: fontes de dados distintas observadas ($observedSources).")
        }

        if (flags.isEmpty()) {
            flags.add(ProtocolContinuityFlag.CONTINUOUS)
            details.add("Continuidade de Protocolo Perfeita: mesmo protocolo (${firstMsr.protocolId}), dispositivo (${firstMsr.deviceId ?: "PADRÃO"}) e unidade (${firstMsr.unit}) preservados em toda a série temporal.")
        }

        val isCompatible = !flags.contains(ProtocolContinuityFlag.PROTOCOL_CHANGED) && !flags.contains(ProtocolContinuityFlag.UNIT_CHANGED)

        return ProtocolContinuityAssessment(
            metricId = metricId,
            flags = flags.toList(),
            details = details,
            isCompatibleForDirectComparison = isCompatible,
            firstObservedProtocol = firstMsr.protocolId,
            lastObservedProtocol = lastMsr.protocolId,
            firstObservedDevice = firstMsr.deviceId,
            lastObservedDevice = lastMsr.deviceId
        )
    }
}
