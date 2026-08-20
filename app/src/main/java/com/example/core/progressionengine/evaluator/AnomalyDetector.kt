package com.example.core.progressionengine.evaluator

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreProvenance
import com.example.core.progressionengine.model.AnomalySeverity
import com.example.core.progressionengine.model.ProgressionAnomaly
import com.example.core.progressionengine.model.ProgressionAnomalyType
import com.example.core.progressionengine.model.ProgressionTimePolicy
import java.util.concurrent.TimeUnit

/**
 * PERFORMAI PROGRESSION ANOMALY DETECTOR
 *
 * Mecanismo de proteção contra "speedrun", saltos abruptos artificiais,
 * descontinuidade de protocolos/dispositivos e picos isolados.
 */
object AnomalyDetector {

    fun detectAnomalies(
        userId: String,
        currentClassId: String,
        targetClassId: String,
        timeInClassDays: Long,
        timePolicy: ProgressionTimePolicy?,
        evidences: List<DataCoreEvidence>,
        provenances: Map<String, DataCoreProvenance> = emptyMap()
    ): List<ProgressionAnomaly> {
        val anomalies = mutableListOf<ProgressionAnomaly>()

        if (evidences.isEmpty()) {
            anomalies.add(
                ProgressionAnomaly(
                    anomalyId = "ANOM-$userId-NODATA-${System.currentTimeMillis()}",
                    userId = userId,
                    type = ProgressionAnomalyType.INSUFFICIENT_LONGITUDINAL_DATA,
                    severity = AnomalySeverity.HIGH,
                    evidenceIds = emptyList(),
                    affectedSnapshots = emptyList(),
                    detectedAt = System.currentTimeMillis(),
                    explanation = "Nenhuma evidência registrada para avaliar progressão para $targetClassId."
                )
            )
            return anomalies
        }

        val sortedEvidences = evidences.sortedBy { it.capturedAt }
        val count = sortedEvidences.size
        val spanMillis = sortedEvidences.last().capturedAt - sortedEvidences.first().capturedAt
        val spanDays = TimeUnit.MILLISECONDS.toDays(spanMillis).coerceAtLeast(0)

        // 1. Anti-Speedrun: Salto rápido de classe sem cumprir tempo de maturação
        if (timePolicy != null && timeInClassDays < timePolicy.minimumTimeInClassDays) {
            anomalies.add(
                ProgressionAnomaly(
                    anomalyId = "ANOM-$userId-RAPID-${System.currentTimeMillis()}",
                    userId = userId,
                    type = ProgressionAnomalyType.RAPID_PROGRESSION,
                    severity = AnomalySeverity.HIGH,
                    evidenceIds = sortedEvidences.map { it.id },
                    affectedSnapshots = emptyList(),
                    detectedAt = System.currentTimeMillis(),
                    explanation = "Tentativa de progressão em apenas $timeInClassDays dias (mínimo da política: ${timePolicy.minimumTimeInClassDays} dias)."
                )
            )
        }

        // 2. Insufficient Longitudinal Data (span ou contagem abaixo do exigido)
        if (timePolicy != null && (spanDays < timePolicy.minimumEvidenceSpanDays || count < timePolicy.minimumObservationCount)) {
            anomalies.add(
                ProgressionAnomaly(
                    anomalyId = "ANOM-$userId-LONGITUDINAL-${System.currentTimeMillis()}",
                    userId = userId,
                    type = ProgressionAnomalyType.INSUFFICIENT_LONGITUDINAL_DATA,
                    severity = AnomalySeverity.MEDIUM,
                    evidenceIds = sortedEvidences.map { it.id },
                    affectedSnapshots = emptyList(),
                    detectedAt = System.currentTimeMillis(),
                    explanation = "Evidências abrangem apenas $spanDays dias e $count observações (exigido: ${timePolicy.minimumEvidenceSpanDays} dias e ${timePolicy.minimumObservationCount} observações)."
                )
            )
        }

        // 3. Performance Spike: Pico isolado desproporcional
        val values = sortedEvidences.map { it.confidenceScore ?: 0.0 }
        val peak = values.maxOrNull() ?: 0.0
        val avg = values.average()
        if (count >= 3 && (peak - avg > 0.35)) {
            anomalies.add(
                ProgressionAnomaly(
                    anomalyId = "ANOM-$userId-SPIKE-${System.currentTimeMillis()}",
                    userId = userId,
                    type = ProgressionAnomalyType.PERFORMANCE_SPIKE,
                    severity = AnomalySeverity.MEDIUM,
                    evidenceIds = sortedEvidences.filter { (it.confidenceScore ?: 0.0) == peak }.map { it.id },
                    affectedSnapshots = emptyList(),
                    detectedAt = System.currentTimeMillis(),
                    explanation = "Pico isolado de performance ($peak) desproporcional à média histórica longitudinal ($avg)."
                )
            )
        }

        // 4. Data Gap: Intervalos excessivos entre coletas (> 30 dias sem registros contínuos)
        for (i in 0 until sortedEvidences.size - 1) {
            val gapDays = TimeUnit.MILLISECONDS.toDays(sortedEvidences[i + 1].capturedAt - sortedEvidences[i].capturedAt)
            if (gapDays > 30) {
                anomalies.add(
                    ProgressionAnomaly(
                        anomalyId = "ANOM-$userId-GAP-$i-${System.currentTimeMillis()}",
                        userId = userId,
                        type = ProgressionAnomalyType.DATA_GAP,
                        severity = AnomalySeverity.LOW,
                        evidenceIds = listOf(sortedEvidences[i].id, sortedEvidences[i + 1].id),
                        affectedSnapshots = emptyList(),
                        detectedAt = System.currentTimeMillis(),
                        explanation = "Intervalo sem coletas de $gapDays dias detectado entre medições longitudinais."
                    )
                )
                break
            }
        }

        // 5. Device Change: Mudança abrupta de dispositivo de captura
        val distinctDevices = sortedEvidences.mapNotNull { ev ->
            provenances[ev.provenanceId]?.deviceIdentifier
        }.distinct()

        if (distinctDevices.size > 2) {
            anomalies.add(
                ProgressionAnomaly(
                    anomalyId = "ANOM-$userId-DEVCHANGE-${System.currentTimeMillis()}",
                    userId = userId,
                    type = ProgressionAnomalyType.DEVICE_CHANGE,
                    severity = AnomalySeverity.MEDIUM,
                    evidenceIds = sortedEvidences.map { it.id },
                    affectedSnapshots = emptyList(),
                    detectedAt = System.currentTimeMillis(),
                    explanation = "Múltiplas trocas de dispositivos de captura registradas na janela longitudinal: $distinctDevices."
                )
            )
        }

        // 6. Protocol Change: Mudança de protocolo experimental
        val distinctProtocols = sortedEvidences.mapNotNull { ev ->
            provenances[ev.provenanceId]?.protocolId
        }.distinct()

        if (distinctProtocols.size > 2) {
            anomalies.add(
                ProgressionAnomaly(
                    anomalyId = "ANOM-$userId-PROTCHANGE-${System.currentTimeMillis()}",
                    userId = userId,
                    type = ProgressionAnomalyType.PROTOCOL_CHANGE,
                    severity = AnomalySeverity.MEDIUM,
                    evidenceIds = sortedEvidences.map { it.id },
                    affectedSnapshots = emptyList(),
                    detectedAt = System.currentTimeMillis(),
                    explanation = "Descontinuidade e alternância de protocolos experimentais: $distinctProtocols."
                )
            )
        }

        return anomalies
    }
}

