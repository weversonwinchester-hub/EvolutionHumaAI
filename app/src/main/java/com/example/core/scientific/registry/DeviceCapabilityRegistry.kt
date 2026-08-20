package com.example.core.scientific.registry

import com.example.core.scientific.model.DeviceCapability
import com.example.core.scientific.model.MethodologyValidationStatus

/**
 * PERFORMAI DEVICE CAPABILITY REGISTRY
 *
 * Determina rigorosamente o que cada tipo de hardware é metodologicamente
 * capaz de capturar e com qual qualidade.
 */
object DeviceCapabilityRegistry {

    private val capabilities: MutableMap<String, DeviceCapability> = mutableMapOf()

    init {
        registerDefaultCapabilities()
    }

    private fun registerDefaultCapabilities() {
        // 1. SMARTPHONE (Câmera integrada, acelerômetro/giroscópio interno, microfone)
        register(
            DeviceCapability(
                deviceType = "SMARTPHONE",
                supportedMetrics = listOf("ROM", "MEAN_PROPULSIVE_VELOCITY", "ACCELERATION"),
                captureMethods = listOf("ESTIMATED_VIDEO", "ESTIMATED_IMU"),
                minimumSamplingRate = 30.0,
                qualityTier = "TIER_4_OPTICAL_MOBILE",
                limitations = listOf(
                    "Taxa de quadros variável dependendo da iluminação e aquecimento térmico",
                    "Acelerômetro interno sofre com ruídos mecânicos e orientação manual instável",
                    "Medições ópticas são classificadas como ESTIMATED_MEASUREMENT"
                ),
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 2. SMARTWATCH (PPG óptico de pulso, acelerômetro de pulso)
        register(
            DeviceCapability(
                deviceType = "SMARTWATCH",
                supportedMetrics = listOf("HRV_RMSSD"),
                captureMethods = listOf("ESTIMATED_PPG_WRIST"),
                minimumSamplingRate = 25.0,
                qualityTier = "TIER_3_WEARABLE",
                limitations = listOf(
                    "PPG de pulso tem baixa precisão durante movimento (artefatos de perfusão e fricção)",
                    "Válido exclusivamente para repouso absoluto prolongado (sono ou repouso matinal)",
                    "Não substitui cinta peitoral para registros dinâmicos"
                ),
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 3. IMU_SENSOR (Unidade de Medição Inercial dedicada de alta frequência)
        register(
            DeviceCapability(
                deviceType = "IMU_SENSOR",
                supportedMetrics = listOf("ROM", "MEAN_PROPULSIVE_VELOCITY", "ACCELERATION", "JOINT_STABILITY"),
                captureMethods = listOf("DIRECT_INERTIAL", "DERIVED_KINEMATIC"),
                minimumSamplingRate = 100.0,
                qualityTier = "TIER_2_DEDICATED_SENSOR",
                limitations = listOf(
                    "Drift de integração numérica em períodos longos (>10s)",
                    "Exige algoritmo de fusão de sensores (filtro de Kalman/Madgwick) calibrado"
                ),
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 4. CAMERA_OPTICAL (Câmera dedicada de alta velocidade com montagem fixa)
        register(
            DeviceCapability(
                deviceType = "CAMERA_OPTICAL",
                supportedMetrics = listOf("ROM", "MEAN_PROPULSIVE_VELOCITY", "ACCELERATION"),
                captureMethods = listOf("DERIVED_OPTICAL_TRACKING", "ESTIMATED_COMPUTER_VISION"),
                minimumSamplingRate = 60.0,
                qualityTier = "TIER_2_DEDICATED_SENSOR",
                limitations = listOf(
                    "Exige calibração de escala espacial no plano de movimento",
                    "Sensível a oclusões e paralaxe fora do eixo ortogonal"
                ),
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 5. EXTERNAL_SENSOR (Cinta Peitoral Bluetooth, Encoder Linear, Fotocélulas, Load Cells)
        register(
            DeviceCapability(
                deviceType = "EXTERNAL_SENSOR",
                supportedMetrics = listOf("HRV_RMSSD", "RELATIVE_FORCE", "RFD", "MEAN_PROPULSIVE_VELOCITY", "ACCELERATION", "CRITICAL_POWER", "W_PRIME"),
                captureMethods = listOf("DIRECT_MEASUREMENT", "DERIVED_MEASUREMENT"),
                minimumSamplingRate = 500.0,
                qualityTier = "TIER_2_DEDICATED_SENSOR",
                limitations = listOf(
                    "Requer conexão Bluetooth/Ant+ sem perda de pacotes",
                    "Calibração de tara periódica"
                ),
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )

        // 6. LAB_EQUIPMENT (Plataformas de Força laboratoriais, Ergoespirômetros, Dinamômetros Isocinéticos)
        register(
            DeviceCapability(
                deviceType = "LAB_EQUIPMENT",
                supportedMetrics = listOf("VO2_MAX", "RELATIVE_FORCE", "RFD", "SYMMETRY", "JOINT_STABILITY", "CRITICAL_POWER", "W_PRIME"),
                captureMethods = listOf("DIRECT_MEASUREMENT"),
                minimumSamplingRate = 1000.0,
                qualityTier = "TIER_1_DIRECT_LAB",
                limitations = listOf(
                    "Exige ambiente controlado e operador certificado",
                    "Protocolos estritos de calibração periódica"
                ),
                validationStatus = MethodologyValidationStatus.ACTIVE
            )
        )
    }

    fun register(capability: DeviceCapability) {
        capabilities[capability.deviceType] = capability
    }

    fun getCapability(deviceType: String): DeviceCapability? {
        return capabilities[deviceType]
    }

    fun getAllCapabilities(): List<DeviceCapability> {
        return capabilities.values.toList()
    }

    fun isMetricSupportedByDevice(deviceType: String, metricId: String): Boolean {
        val cap = capabilities[deviceType] ?: return false
        return cap.supportedMetrics.contains(metricId)
    }
}
