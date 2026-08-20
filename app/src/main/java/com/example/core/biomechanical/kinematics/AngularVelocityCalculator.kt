package com.example.core.biomechanical.kinematics

import java.util.UUID

/**
 * PERFORMAI ANGULAR VELOCITY AND ACCELERATION KINEMATICS
 */

data class AngularVelocityMeasurement(
    val velocityId: String = UUID.randomUUID().toString(),
    val joint: JointType,
    val side: BodySide,
    val value: Double, // deg/s ou rad/s
    val unit: String = "deg/s",
    val timestamp: Long,
    val calculationMethod: String = "FIRST_ORDER_CENTRAL_DIFFERENCE",
    val sourceMeasurements: List<String>,
    val filterApplied: String? = null
)

data class AngularAccelerationMeasurement(
    val accelerationId: String = UUID.randomUUID().toString(),
    val joint: JointType,
    val side: BodySide,
    val value: Double, // deg/s^2 ou rad/s^2
    val unit: String = "deg/s^2",
    val timestamp: Long,
    val calculationMethod: String = "SECOND_ORDER_DIFFERENCE",
    val sourceMeasurements: List<String>,
    val filterApplied: String? = null
)

object AngularVelocityCalculator {

    fun calculateAngularVelocities(
        angleMeasurements: List<JointAngleMeasurement>,
        filterInfo: String? = null
    ): List<AngularVelocityMeasurement> {
        val velocities = mutableListOf<AngularVelocityMeasurement>()
        if (angleMeasurements.size < 2) return velocities

        // Agrupa medições por articulação e lado
        val grouped = angleMeasurements.groupBy { Pair(it.joint, it.side) }

        grouped.forEach { (key, list) ->
            val sortedList = list.sortedBy { it.timestamp }
            for (i in 1 until sortedList.size) {
                val prev = sortedList[i - 1]
                val curr = sortedList[i]

                val dtSec = (curr.timestamp - prev.timestamp).toDouble() / 1000.0
                if (dtSec > 0.0) {
                    val dTheta = curr.angle - prev.angle
                    val velocity = dTheta / dtSec

                    velocities.add(
                        AngularVelocityMeasurement(
                            joint = key.first,
                            side = key.second,
                            value = velocity,
                            unit = if (curr.unit == AngleUnit.DEGREES) "deg/s" else "rad/s",
                            timestamp = curr.timestamp,
                            calculationMethod = "NUMERICAL_DIFFERENTIATION",
                            sourceMeasurements = listOf(prev.measurementId, curr.measurementId),
                            filterApplied = filterInfo
                        )
                    )
                }
            }
        }

        return velocities
    }
}

object AngularAccelerationCalculator {

    fun calculateAngularAccelerations(
        velocityMeasurements: List<AngularVelocityMeasurement>,
        filterInfo: String? = null
    ): List<AngularAccelerationMeasurement> {
        val accelerations = mutableListOf<AngularAccelerationMeasurement>()
        if (velocityMeasurements.size < 2) return accelerations

        val grouped = velocityMeasurements.groupBy { Pair(it.joint, it.side) }

        grouped.forEach { (key, list) ->
            val sortedList = list.sortedBy { it.timestamp }
            for (i in 1 until sortedList.size) {
                val prev = sortedList[i - 1]
                val curr = sortedList[i]

                val dtSec = (curr.timestamp - prev.timestamp).toDouble() / 1000.0
                if (dtSec > 0.0) {
                    val dOmega = curr.value - prev.value
                    val accel = dOmega / dtSec

                    accelerations.add(
                        AngularAccelerationMeasurement(
                            joint = key.first,
                            side = key.second,
                            value = accel,
                            unit = if (curr.unit.startsWith("deg")) "deg/s^2" else "rad/s^2",
                            timestamp = curr.timestamp,
                            calculationMethod = "SECOND_ORDER_DERIVATIVE",
                            sourceMeasurements = listOf(prev.velocityId, curr.velocityId),
                            filterApplied = filterInfo
                        )
                    )
                }
            }
        }

        return accelerations
    }
}
