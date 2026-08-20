package com.example.core.biomechanical.quality

import com.example.core.biomechanical.capture.VisionFrame

/**
 * PERFORMAI SIGNAL QUALITY EVALUATOR
 *
 * Avalia consistência temporal, taxas de amostragem, frames duplicados,
 * quadros perdidos (dropped frames) e desvios de relógio (clock drift / jitter).
 */
object SignalQualityEvaluator {

    data class SignalQualityEvaluation(
        val sessionId: String,
        val totalFrames: Int,
        val validFrames: Int,
        val droppedFramesCount: Int,
        val duplicateFramesCount: Int,
        val outOfOrderCount: Int,
        val futureTimestampCount: Int,
        val averageFrameRateObserved: Double,
        val frameRateRequired: Double,
        val jitterMs: Double,
        val isTemporalIntegrityValid: Boolean,
        val passedChecks: List<String>,
        val failedChecks: List<String>
    )

    fun evaluateSignalSequence(
        frames: List<VisionFrame>,
        requiredFrameRate: Double = 30.0,
        toleranceFrameRatePercent: Double = 0.20,
        serverReferenceTimestamp: Long = System.currentTimeMillis()
    ): SignalQualityEvaluation {
        if (frames.isEmpty()) {
            return SignalQualityEvaluation(
                sessionId = "UNKNOWN",
                totalFrames = 0,
                validFrames = 0,
                droppedFramesCount = 0,
                duplicateFramesCount = 0,
                outOfOrderCount = 0,
                futureTimestampCount = 0,
                averageFrameRateObserved = 0.0,
                frameRateRequired = requiredFrameRate,
                jitterMs = 0.0,
                isTemporalIntegrityValid = false,
                passedChecks = emptyList(),
                failedChecks = listOf("EMPTY_FRAME_SEQUENCE")
            )
        }

        val sessionId = frames.first().sessionId
        val passed = mutableListOf<String>()
        val failed = mutableListOf<String>()

        var droppedCount = 0
        var duplicateCount = 0
        var outOfOrderCount = 0
        var futureTimestampCount = 0
        val intervals = mutableListOf<Long>()
        val timestampsSeen = mutableSetOf<Long>()

        for (i in 0 until frames.size) {
            val current = frames[i]

            // Checagem de timestamp no futuro (> 5 segundos tolerância de skew)
            if (current.timestamp > serverReferenceTimestamp + 5000L) {
                futureTimestampCount++
            }

            // Checagem de frame duplicado por timestamp
            if (timestampsSeen.contains(current.timestamp) || current.isDuplicateFrame) {
                duplicateCount++
            } else {
                timestampsSeen.add(current.timestamp)
            }

            // Checagem de dropped frame marcado
            if (current.isDroppedFrame) {
                droppedCount++
            }

            // Checagem de ordem temporal
            if (i > 0) {
                val prev = frames[i - 1]
                val dt = current.timestamp - prev.timestamp
                if (dt < 0) {
                    outOfOrderCount++
                } else {
                    intervals.add(dt)
                }
            }
        }

        if (futureTimestampCount > 0) {
            failed.add("FUTURE_TIMESTAMPS_DETECTED_COUNT_$futureTimestampCount")
        } else {
            passed.add("NO_FUTURE_TIMESTAMPS")
        }

        if (duplicateCount > 0) {
            failed.add("DUPLICATE_FRAMES_DETECTED_COUNT_$duplicateCount")
        } else {
            passed.add("NO_DUPLICATE_FRAMES")
        }

        if (outOfOrderCount > 0) {
            failed.add("OUT_OF_ORDER_FRAMES_DETECTED_COUNT_$outOfOrderCount")
        } else {
            passed.add("STRICT_CHRONOLOGICAL_ORDER_PRESERVED")
        }

        // Cálculo de Frame Rate Efetivo
        val totalDurationMs = if (frames.size > 1) {
            (frames.last().timestamp - frames.first().timestamp).coerceAtLeast(1L)
        } else 1000L

        val observedFps = if (frames.size > 1) {
            ((frames.size - 1).toDouble() / (totalDurationMs.toDouble() / 1000.0))
        } else frames.first().frameRate

        val minAllowedFps = requiredFrameRate * (1.0 - toleranceFrameRatePercent)
        if (observedFps >= minAllowedFps) {
            passed.add("SAMPLING_RATE_SUFFICIENT_OBSERVED_${String.format("%.1f", observedFps)}_REQ_${requiredFrameRate}")
        } else {
            failed.add("SAMPLING_RATE_INSUFFICIENT_OBSERVED_${String.format("%.1f", observedFps)}_REQ_${requiredFrameRate}")
        }

        // Cálculo de Jitter
        val avgInterval = if (intervals.isNotEmpty()) intervals.average() else 33.33
        val jitter = if (intervals.size > 1) {
            Math.sqrt(intervals.map { Math.pow(it - avgInterval, 2.0) }.average())
        } else 0.0

        if (jitter <= 25.0) {
            passed.add("JITTER_ACCEPTABLE_${String.format("%.1f", jitter)}ms")
        } else {
            failed.add("HIGH_FRAME_JITTER_DETECTED_${String.format("%.1f", jitter)}ms")
        }

        val isTemporalValid = outOfOrderCount == 0 && futureTimestampCount == 0 && duplicateCount == 0 && observedFps >= minAllowedFps

        return SignalQualityEvaluation(
            sessionId = sessionId,
            totalFrames = frames.size,
            validFrames = frames.size - (droppedCount + duplicateCount + outOfOrderCount),
            droppedFramesCount = droppedCount,
            duplicateFramesCount = duplicateCount,
            outOfOrderCount = outOfOrderCount,
            futureTimestampCount = futureTimestampCount,
            averageFrameRateObserved = observedFps,
            frameRateRequired = requiredFrameRate,
            jitterMs = jitter,
            isTemporalIntegrityValid = isTemporalValid,
            passedChecks = passed,
            failedChecks = failed
        )
    }
}
