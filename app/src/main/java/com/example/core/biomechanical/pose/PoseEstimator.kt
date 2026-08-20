package com.example.core.biomechanical.pose

import com.example.core.biomechanical.capture.VisionFrame

/**
 * PERFORMAI POSE ESTIMATOR INTERFACE
 *
 * Contrato agnóstico a fornecedor para estimação de pose humana.
 * Nenhum fornecedor de visão possui autoridade de Score, Evolution ou Progression.
 */

data class EstimatorInfo(
    val providerName: String,
    val modelName: String,
    val version: String,
    val supportedLandmarks: List<LandmarkType>,
    val outputCoordinateSystem: CoordinateSystem,
    val defaultDepthType: DepthType
)

interface PoseEstimator {
    fun getEstimatorInfo(): EstimatorInfo
    fun estimate(frame: VisionFrame): PoseFrame
}

/**
 * Implementação Padrão/Simulada de PoseEstimator para testes e execução interna determinística.
 */
class DefaultDeterministicPoseEstimator(
    private val modelVersion: String = "PERFORMAI-VISION-CORE-1.0.0"
) : PoseEstimator {

    override fun getEstimatorInfo(): EstimatorInfo = EstimatorInfo(
        providerName = "PERFORMAI_CORE_VISION",
        modelName = "DETERMINISTIC_KINEMATIC_SOLVER",
        version = modelVersion,
        supportedLandmarks = LandmarkType.values().toList(),
        outputCoordinateSystem = CoordinateSystem.NORMALIZED_2D,
        defaultDepthType = DepthType.NONE
    )

    override fun estimate(frame: VisionFrame): PoseFrame {
        // Gera landmarks anatômicos estruturados baseados no frame
        val landmarks = mutableMapOf<LandmarkType, Landmark>()
        val detected = mutableListOf<LandmarkType>()
        val occluded = mutableListOf<LandmarkType>()

        val baseVisibility = frame.visibilityScore
        val baseConfidence = frame.qualityScore * (1.0 - frame.blurScore).coerceIn(0.0, 1.0)

        // Estrutura anatômica básica no plano 2D normalizado [0, 1]
        val landmarkPositions = mapOf(
            LandmarkType.HEAD to Pair(0.5, 0.15),
            LandmarkType.NECK to Pair(0.5, 0.22),
            LandmarkType.CHEST to Pair(0.5, 0.32),
            LandmarkType.LEFT_SHOULDER to Pair(0.42, 0.25),
            LandmarkType.RIGHT_SHOULDER to Pair(0.58, 0.25),
            LandmarkType.LEFT_ELBOW to Pair(0.38, 0.38),
            LandmarkType.RIGHT_ELBOW to Pair(0.62, 0.38),
            LandmarkType.LEFT_WRIST to Pair(0.36, 0.50),
            LandmarkType.RIGHT_WRIST to Pair(0.64, 0.50),
            LandmarkType.LEFT_HIP to Pair(0.45, 0.52),
            LandmarkType.RIGHT_HIP to Pair(0.55, 0.52),
            LandmarkType.MID_HIP to Pair(0.50, 0.52),
            LandmarkType.LEFT_KNEE to Pair(0.44, 0.70),
            LandmarkType.RIGHT_KNEE to Pair(0.56, 0.70),
            LandmarkType.LEFT_ANKLE to Pair(0.44, 0.88),
            LandmarkType.RIGHT_ANKLE to Pair(0.56, 0.88),
            LandmarkType.LEFT_FOOT to Pair(0.42, 0.92),
            LandmarkType.RIGHT_FOOT to Pair(0.58, 0.92)
        )

        landmarkPositions.forEach { (type, pos) ->
            val isVis = baseVisibility >= 0.3
            if (isVis) {
                detected.add(type)
                landmarks[type] = Landmark(
                    type = type,
                    x = pos.first,
                    y = pos.second,
                    z = null,
                    visibility = baseVisibility,
                    confidence = baseConfidence,
                    coordinateSystem = CoordinateSystem.NORMALIZED_2D,
                    depthType = DepthType.NONE,
                    timestamp = frame.timestamp
                )
            } else {
                occluded.add(type)
            }
        }

        return PoseFrame.createWithHash(
            frameId = frame.frameId,
            sessionId = frame.sessionId,
            timestamp = frame.timestamp,
            landmarks = landmarks,
            poseConfidence = baseConfidence,
            bodyVisibility = if (detected.isNotEmpty()) detected.size.toDouble() / landmarkPositions.size else 0.0,
            detectedBodyParts = detected,
            occludedBodyParts = occluded,
            coordinateSystem = CoordinateSystem.NORMALIZED_2D,
            estimatorVersion = modelVersion
        )
    }
}
