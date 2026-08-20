package com.example.core.biomechanical.pose

import java.security.MessageDigest
import java.util.UUID

/**
 * PERFORMAI LANDMARK ENUMS AND MODELS
 */

enum class LandmarkType {
    HEAD,
    NOSE,
    NECK,
    LEFT_EYE,
    RIGHT_EYE,
    LEFT_EAR,
    RIGHT_EAR,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_HAND,
    RIGHT_HAND,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_HEEL,
    RIGHT_HEEL,
    LEFT_FOOT,
    RIGHT_FOOT,
    CHEST,
    MID_HIP
}

enum class CoordinateSystem {
    NORMALIZED_2D, // [0.0, 1.0] em relação à largura e altura do frame
    PIXEL_2D,      // Coordenadas absolutas em pixels
    CAMERA_3D,    // Metros ou mm relativo ao centro óptico da câmera
    WORLD_3D      // Metros calibrados no plano do solo/ambiente
}

enum class DepthType {
    MEASURED,  // Medido diretamente por sensor ToF / LiDAR / Rig Estéreo calibrado
    ESTIMATED, // Inferido por rede neural monocular (NÃO é medição física direta)
    NONE       // Apenas coordenadas 2D disponíveis
}

data class Landmark(
    val landmarkId: String = UUID.randomUUID().toString(),
    val type: LandmarkType,
    val x: Double,
    val y: Double,
    val z: Double? = null,
    val visibility: Double = 1.0, // [0.0, 1.0] probabilidade do ponto estar dentro do frame
    val confidence: Double = 1.0, // [0.0, 1.0] confiança do estimador no ponto (NÃO é acurácia física)
    val coordinateSystem: CoordinateSystem = CoordinateSystem.NORMALIZED_2D,
    val depthType: DepthType = if (z != null) DepthType.ESTIMATED else DepthType.NONE,
    val timestamp: Long = System.currentTimeMillis()
)

data class PoseFrame(
    val frameId: String,
    val sessionId: String,
    val timestamp: Long,
    val landmarks: Map<LandmarkType, Landmark>,
    val poseConfidence: Double, // Confiança geral da pose
    val bodyVisibility: Double, // Proporção de pontos essenciais visíveis
    val detectedBodyParts: List<LandmarkType>,
    val occludedBodyParts: List<LandmarkType>,
    val coordinateSystem: CoordinateSystem = CoordinateSystem.NORMALIZED_2D,
    val estimatorVersion: String,
    val integrityHash: String = ""
) {
    fun calculateIntegrityHash(): String {
        val landmarksStr = landmarks.entries.sortedBy { it.key.name }
            .joinToString(";") { "${it.key}:${it.value.x},${it.value.y},${it.value.z},${it.value.confidence}" }
        val payload = "$frameId|$sessionId|$timestamp|$poseConfidence|$bodyVisibility|$coordinateSystem|$estimatorVersion|$landmarksStr"
        return MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun createWithHash(
            frameId: String,
            sessionId: String,
            timestamp: Long,
            landmarks: Map<LandmarkType, Landmark>,
            poseConfidence: Double,
            bodyVisibility: Double,
            detectedBodyParts: List<LandmarkType>,
            occludedBodyParts: List<LandmarkType>,
            coordinateSystem: CoordinateSystem = CoordinateSystem.NORMALIZED_2D,
            estimatorVersion: String
        ): PoseFrame {
            val pose = PoseFrame(
                frameId = frameId,
                sessionId = sessionId,
                timestamp = timestamp,
                landmarks = landmarks,
                poseConfidence = poseConfidence,
                bodyVisibility = bodyVisibility,
                detectedBodyParts = detectedBodyParts,
                occludedBodyParts = occludedBodyParts,
                coordinateSystem = coordinateSystem,
                estimatorVersion = estimatorVersion
            )
            return pose.copy(integrityHash = pose.calculateIntegrityHash())
        }
    }
}
