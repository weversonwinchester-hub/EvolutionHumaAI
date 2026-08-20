package com.example.core.evidenceconsistency.longitudinal

import com.example.core.datacore.model.DataCoreEvidence
import com.example.core.datacore.model.DataCoreMeasurement
import com.example.core.datacore.model.ValidationStatus
import com.example.core.evidenceconsistency.consistency.EvidenceConsistencyEngine
import com.example.core.evidenceconsistency.continuity.ProtocolContinuityTracker
import com.example.core.evidenceconsistency.model.LongitudinalMetricSequence
import com.example.core.evidenceconsistency.repeatability.RepeatabilityEvaluator

class LongitudinalTracker(
    private val continuityTracker: ProtocolContinuityTracker = ProtocolContinuityTracker(),
    private val consistencyEngine: EvidenceConsistencyEngine = EvidenceConsistencyEngine(),
    private val repeatabilityEvaluator: RepeatabilityEvaluator = RepeatabilityEvaluator()
) {

    fun buildLongitudinalSequence(
        metricId: String,
        measurements: List<DataCoreMeasurement>,
        evidences: List<DataCoreEvidence>,
        evaluationTimestamp: Long = System.currentTimeMillis()
    ): LongitudinalMetricSequence {
        val validMsrs = measurements
            .filter { it.metricId == metricId && it.validationStatus == ValidationStatus.VALID }
            .sortedBy { it.timestamp }

        if (validMsrs.isEmpty()) {
            val continuity = continuityTracker.analyzeContinuity(metricId, emptyList(), evidences)
            val consistency = consistencyEngine.evaluateMetricConsistency(metricId, emptyList(), evidences, evaluationTimestamp)
            val repeatability = repeatabilityEvaluator.evaluateRepeatability(metricId, emptyList(), evidences)

            return LongitudinalMetricSequence(
                metricId = metricId,
                firstValidMeasurementTimestamp = null,
                lastValidMeasurementTimestamp = null,
                measurementCount = 0,
                averageIntervalMillis = null,
                maxGapMillis = null,
                continuityAssessment = continuity,
                consistencyAssessment = consistency,
                repeatabilityAssessment = repeatability
            )
        }

        val firstTimestamp = validMsrs.first().timestamp
        val lastTimestamp = validMsrs.last().timestamp

        // Cálculo de intervalos e lacunas entre medições consecutivas
        var totalIntervalMillis = 0L
        var maxGapMillis = 0L
        val intervalsCount = validMsrs.size - 1

        for (i in 0 until intervalsCount) {
            val gap = validMsrs[i + 1].timestamp - validMsrs[i].timestamp
            totalIntervalMillis += gap
            if (gap > maxGapMillis) {
                maxGapMillis = gap
            }
        }

        val avgIntervalMillis = if (intervalsCount > 0) totalIntervalMillis / intervalsCount else null

        val continuity = continuityTracker.analyzeContinuity(metricId, validMsrs, evidences)
        val consistency = consistencyEngine.evaluateMetricConsistency(metricId, validMsrs, evidences, evaluationTimestamp)
        val repeatability = repeatabilityEvaluator.evaluateRepeatability(metricId, validMsrs, evidences)

        return LongitudinalMetricSequence(
            metricId = metricId,
            firstValidMeasurementTimestamp = firstTimestamp,
            lastValidMeasurementTimestamp = lastTimestamp,
            measurementCount = validMsrs.size,
            averageIntervalMillis = avgIntervalMillis,
            maxGapMillis = if (intervalsCount > 0) maxGapMillis else null,
            continuityAssessment = continuity,
            consistencyAssessment = consistency,
            repeatabilityAssessment = repeatability
        )
    }
}
