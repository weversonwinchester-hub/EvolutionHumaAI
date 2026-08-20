package com.example.core.biomechanical.kinematics

import com.example.core.biomechanical.pose.CoordinateSystem
import com.example.core.biomechanical.pose.LandmarkType
import com.example.core.biomechanical.pose.PoseFrame
import java.util.UUID

/**
 * PERFORMAI LINEAR KINEMATICS AND SIGNAL PROCESSING PIPELINE
 */

data class LinearDisplacement(
    val displacementId: String = UUID.randomUUID().toString(),
    val landmark: LandmarkType,
    val dx: Double,
    val dy: Double,
    val dz: Double? = null,
    val totalDistance: Double,
    val unit: String = "normalized_units",
    val coordinateSystem: CoordinateSystem = CoordinateSystem.NORMALIZED_2D,
    val timestamp: Long,
    val uncertainty: Double
)

data class LinearVelocity(
    val velocityId: String = UUID.randomUUID().toString(),
    val landmark: LandmarkType,
    val vx: Double,
    val vy: Double,
    val vz: Double? = null,
    val speed: Double,
    val unit: String = "norm_units/s",
    val timestamp: Long,
    val uncertainty: Double
)

data class LinearAcceleration(
    val accelerationId: String = UUID.randomUUID().toString(),
    val landmark: LandmarkType,
    val ax: Double,
    val ay: Double,
    val az: Double? = null,
    val totalAcceleration: Double,
    val unit: String = "norm_units/s^2",
    val timestamp: Long,
    val uncertainty: Double
)

data class SignalProcessingPipeline(
    val pipelineId: String = UUID.randomUUID().toString(),
    val inputType: String,
    val filterType: String = "BUTTERWORTH_LOWPASS",
    val cutoffFrequencyHz: Double = 6.0,
    val order: Int = 4,
    val samplingRateHz: Double = 60.0,
    val preprocessingVersion: String = "KINEMATIC_FILTER_V1.0",
    val isDeterministic: Boolean = true
)

object LinearKinematicsCalculator {

    fun calculateLinearKinematics(
        poseSequence: List<PoseFrame>,
        targetLandmark: LandmarkType = LandmarkType.MID_HIP
    ): Triple<List<LinearDisplacement>, List<LinearVelocity>, List<LinearAcceleration>> {
        val displacements = mutableListOf<LinearDisplacement>()
        val velocities = mutableListOf<LinearVelocity>()
        val accelerations = mutableListOf<LinearAcceleration>()

        if (poseSequence.size < 2) return Triple(displacements, velocities, accelerations)

        // 1. Deslocamentos e Velocidades
        for (i in 1 until poseSequence.size) {
            val prevPose = poseSequence[i - 1]
            val currPose = poseSequence[i]

            val pPrev = prevPose.landmarks[targetLandmark]
            val pCurr = currPose.landmarks[targetLandmark]

            if (pPrev != null && pCurr != null) {
                val dx = pCurr.x - pPrev.x
                val dy = pCurr.y - pPrev.y
                val dz = if (pCurr.z != null && pPrev.z != null) pCurr.z - pPrev.z else null
                val dist = Math.sqrt(dx * dx + dy * dy + (dz?.let { it * it } ?: 0.0))

                val dtSec = (currPose.timestamp - prevPose.timestamp).toDouble() / 1000.0
                if (dtSec > 0.0) {
                    val vx = dx / dtSec
                    val vy = dy / dtSec
                    val vz = dz?.let { it / dtSec }
                    val speed = dist / dtSec

                    displacements.add(
                        LinearDisplacement(
                            landmark = targetLandmark,
                            dx = dx,
                            dy = dy,
                            dz = dz,
                            totalDistance = dist,
                            coordinateSystem = currPose.coordinateSystem,
                            timestamp = currPose.timestamp,
                            uncertainty = (1.0 - pCurr.confidence) * 0.05
                        )
                    )

                    velocities.add(
                        LinearVelocity(
                            landmark = targetLandmark,
                            vx = vx,
                            vy = vy,
                            vz = vz,
                            speed = speed,
                            timestamp = currPose.timestamp,
                            uncertainty = (1.0 - pCurr.confidence) * 0.10
                        )
                    )
                }
            }
        }

        // 2. Acelerações Lineares
        for (i in 1 until velocities.size) {
            val vPrev = velocities[i - 1]
            val vCurr = velocities[i]

            val dtSec = (vCurr.timestamp - vPrev.timestamp).toDouble() / 1000.0
            if (dtSec > 0.0) {
                val ax = (vCurr.vx - vPrev.vx) / dtSec
                val ay = (vCurr.vy - vPrev.vy) / dtSec
                val az = if (vCurr.vz != null && vPrev.vz != null) (vCurr.vz - vPrev.vz) / dtSec else null
                val totalAcc = Math.sqrt(ax * ax + ay * ay + (az?.let { it * it } ?: 0.0))

                accelerations.add(
                    LinearAcceleration(
                        landmark = targetLandmark,
                        ax = ax,
                        ay = ay,
                        az = az,
                        totalAcceleration = totalAcc,
                        timestamp = vCurr.timestamp,
                        uncertainty = (vCurr.uncertainty * 1.5)
                    )
                )
            }
        }

        return Triple(displacements, velocities, accelerations)
    }
}
