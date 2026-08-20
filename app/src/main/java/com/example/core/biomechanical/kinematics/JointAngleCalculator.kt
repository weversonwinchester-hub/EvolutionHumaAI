package com.example.core.biomechanical.kinematics

import com.example.core.biomechanical.pose.CoordinateSystem
import com.example.core.biomechanical.pose.DepthType
import com.example.core.biomechanical.pose.Landmark
import com.example.core.biomechanical.pose.LandmarkType
import com.example.core.biomechanical.pose.PoseFrame
import java.util.UUID

/**
 * PERFORMAI JOINT ANGLE KINEMATICS
 */

enum class JointType {
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    TRUNK,
    NECK
}

enum class BodySide {
    LEFT,
    RIGHT,
    BILATERAL,
    CENTRAL
}

enum class AngleUnit {
    DEGREES,
    RADIANS
}

data class JointAngleMeasurement(
    val measurementId: String = UUID.randomUUID().toString(),
    val joint: JointType,
    val side: BodySide,
    val angle: Double,
    val unit: AngleUnit = AngleUnit.DEGREES,
    val timestamp: Long,
    val confidence: Double,
    val uncertainty: Double,
    val methodId: String,
    val sourceFrameIds: List<String>,
    val coordinateSystem: CoordinateSystem = CoordinateSystem.NORMALIZED_2D,
    val depthType: DepthType = DepthType.NONE
)

object JointAngleCalculator {

    /**
     * Calcula o ângulo entre 3 pontos anatômicos: A (proximal), B (vértice da articulação), C (distal).
     * Exemplo: Joelho -> A = Quadril, B = Joelho, C = Tornozelo.
     * Retorna ângulo em graus [0.0, 180.0] ou radianos.
     */
    fun calculateAngle(
        pA: Landmark,
        pB: Landmark,
        pC: Landmark,
        unit: AngleUnit = AngleUnit.DEGREES
    ): Double {
        // Vetor BA = A - B
        val vBAx = pA.x - pB.x
        val vBAy = pA.y - pB.y
        val vBAz = (pA.z ?: 0.0) - (pB.z ?: 0.0)

        // Vetor BC = C - B
        val vBCx = pC.x - pB.x
        val vBCy = pC.y - pB.y
        val vBCz = (pC.z ?: 0.0) - (pB.z ?: 0.0)

        val dotProduct = (vBAx * vBCx) + (vBAy * vBCy) + (vBAz * vBCz)
        val magBA = Math.sqrt(vBAx * vBAx + vBAy * vBAy + vBAz * vBAz)
        val magBC = Math.sqrt(vBCx * vBCx + vBCy * vBCy + vBCz * vBCz)

        if (magBA == 0.0 || magBC == 0.0) return 0.0

        val cosine = (dotProduct / (magBA * magBC)).coerceIn(-1.0, 1.0)
        val angleRad = Math.acos(cosine)

        return if (unit == AngleUnit.DEGREES) {
            Math.toDegrees(angleRad)
        } else {
            angleRad
        }
    }

    fun calculateJointAnglesForPose(
        pose: PoseFrame,
        methodId: String = "METH-ROM-GONIOMETRY-V1",
        unit: AngleUnit = AngleUnit.DEGREES
    ): List<JointAngleMeasurement> {
        val measurements = mutableListOf<JointAngleMeasurement>()

        // 1. Joelho Esquerdo (Quadril -> Joelho -> Tornozelo)
        val lHip = pose.landmarks[LandmarkType.LEFT_HIP]
        val lKnee = pose.landmarks[LandmarkType.LEFT_KNEE]
        val lAnkle = pose.landmarks[LandmarkType.LEFT_ANKLE]
        if (lHip != null && lKnee != null && lAnkle != null) {
            val angle = calculateAngle(lHip, lKnee, lAnkle, unit)
            val conf = (lHip.confidence * lKnee.confidence * lAnkle.confidence).coerceIn(0.0, 1.0)
            val uncert = ((1.0 - conf) * 15.0).coerceAtLeast(1.5) // Incerteza angular básica em graus
            measurements.add(
                JointAngleMeasurement(
                    joint = JointType.LEFT_KNEE,
                    side = BodySide.LEFT,
                    angle = angle,
                    unit = unit,
                    timestamp = pose.timestamp,
                    confidence = conf,
                    uncertainty = uncert,
                    methodId = methodId,
                    sourceFrameIds = listOf(pose.frameId),
                    coordinateSystem = pose.coordinateSystem,
                    depthType = lKnee.depthType
                )
            )
        }

        // 2. Joelho Direito (Quadril -> Joelho -> Tornozelo)
        val rHip = pose.landmarks[LandmarkType.RIGHT_HIP]
        val rKnee = pose.landmarks[LandmarkType.RIGHT_KNEE]
        val rAnkle = pose.landmarks[LandmarkType.RIGHT_ANKLE]
        if (rHip != null && rKnee != null && rAnkle != null) {
            val angle = calculateAngle(rHip, rKnee, rAnkle, unit)
            val conf = (rHip.confidence * rKnee.confidence * rAnkle.confidence).coerceIn(0.0, 1.0)
            val uncert = ((1.0 - conf) * 15.0).coerceAtLeast(1.5)
            measurements.add(
                JointAngleMeasurement(
                    joint = JointType.RIGHT_KNEE,
                    side = BodySide.RIGHT,
                    angle = angle,
                    unit = unit,
                    timestamp = pose.timestamp,
                    confidence = conf,
                    uncertainty = uncert,
                    methodId = methodId,
                    sourceFrameIds = listOf(pose.frameId),
                    coordinateSystem = pose.coordinateSystem,
                    depthType = rKnee.depthType
                )
            )
        }

        // 3. Cotovelo Esquerdo (Ombro -> Cotovelo -> Punho)
        val lShoulder = pose.landmarks[LandmarkType.LEFT_SHOULDER]
        val lElbow = pose.landmarks[LandmarkType.LEFT_ELBOW]
        val lWrist = pose.landmarks[LandmarkType.LEFT_WRIST]
        if (lShoulder != null && lElbow != null && lWrist != null) {
            val angle = calculateAngle(lShoulder, lElbow, lWrist, unit)
            val conf = (lShoulder.confidence * lElbow.confidence * lWrist.confidence).coerceIn(0.0, 1.0)
            val uncert = ((1.0 - conf) * 15.0).coerceAtLeast(1.5)
            measurements.add(
                JointAngleMeasurement(
                    joint = JointType.LEFT_ELBOW,
                    side = BodySide.LEFT,
                    angle = angle,
                    unit = unit,
                    timestamp = pose.timestamp,
                    confidence = conf,
                    uncertainty = uncert,
                    methodId = methodId,
                    sourceFrameIds = listOf(pose.frameId),
                    coordinateSystem = pose.coordinateSystem,
                    depthType = lElbow.depthType
                )
            )
        }

        // 4. Cotovelo Direito (Ombro -> Cotovelo -> Punho)
        val rShoulder = pose.landmarks[LandmarkType.RIGHT_SHOULDER]
        val rElbow = pose.landmarks[LandmarkType.RIGHT_ELBOW]
        val rWrist = pose.landmarks[LandmarkType.RIGHT_WRIST]
        if (rShoulder != null && rElbow != null && rWrist != null) {
            val angle = calculateAngle(rShoulder, rElbow, rWrist, unit)
            val conf = (rShoulder.confidence * rElbow.confidence * rWrist.confidence).coerceIn(0.0, 1.0)
            val uncert = ((1.0 - conf) * 15.0).coerceAtLeast(1.5)
            measurements.add(
                JointAngleMeasurement(
                    joint = JointType.RIGHT_ELBOW,
                    side = BodySide.RIGHT,
                    angle = angle,
                    unit = unit,
                    timestamp = pose.timestamp,
                    confidence = conf,
                    uncertainty = uncert,
                    methodId = methodId,
                    sourceFrameIds = listOf(pose.frameId),
                    coordinateSystem = pose.coordinateSystem,
                    depthType = rElbow.depthType
                )
            )
        }

        return measurements
    }
}
