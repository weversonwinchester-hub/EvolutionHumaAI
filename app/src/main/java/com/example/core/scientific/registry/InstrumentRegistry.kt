package com.example.core.scientific.registry

import com.example.core.scientific.model.InstrumentValidationStatus
import com.example.core.scientific.model.MeasurementInstrument

/**
 * PERFORMAI INSTRUMENT REGISTRY
 *
 * Registrador de instrumentos de medição física.
 * Não cadastra fabricantes ou modelos fictícios.
 * Suporta instrumentos genéricos padronizados e homologados para biometria e biomecânica.
 */
object InstrumentRegistry {

    private val instruments: MutableMap<String, MeasurementInstrument> = mutableMapOf()

    init {
        registerDefaultInstruments()
    }

    private fun registerDefaultInstruments() {
        // 1. Plataforma de Força Isométrica/Dinâmica de Alta Frequência
        register(
            MeasurementInstrument(
                instrumentId = "INST-FORCE-PLATE-1000HZ",
                manufacturer = "GENERIC_SCIENTIFIC",
                model = "DUAL_FORCE_PLATE_PIEZO_1000HZ",
                instrumentType = "FORCE_PLATE",
                sensorType = "PIEZOELECTRIC_STRAIN_GAUGE",
                supportedMetrics = listOf("RELATIVE_FORCE", "RFD", "SYMMETRY", "JOINT_STABILITY"),
                samplingRate = 1000.0,
                calibrationRequirement = true,
                calibrationIntervalDays = 180,
                accuracySpecification = "Linearidade < 0.1% Fundo de Escala, Resolução 0.1 N",
                firmware = "1.0.0",
                validationStatus = InstrumentValidationStatus.VALIDATED
            )
        )

        // 2. Transdutor Linear de Posição / Encoder Linear (VBT)
        register(
            MeasurementInstrument(
                instrumentId = "INST-LINEAR-ENCODER-500HZ",
                manufacturer = "GENERIC_SCIENTIFIC",
                model = "OPTICAL_LINEAR_TRANSDUCER_500HZ",
                instrumentType = "LINEAR_TRANSDUCER",
                sensorType = "ROTARY_OPTICAL_ENCODER",
                supportedMetrics = listOf("MEAN_PROPULSIVE_VELOCITY", "ACCELERATION"),
                samplingRate = 500.0,
                calibrationRequirement = true,
                calibrationIntervalDays = 365,
                accuracySpecification = "Resolução espacial < 0.05 mm, Precisão de velocidade < 0.01 m/s",
                firmware = "1.0.0",
                validationStatus = InstrumentValidationStatus.VALIDATED
            )
        )

        // 3. Cinta Peitoral de Frequência Cardíaca R-R
        register(
            MeasurementInstrument(
                instrumentId = "INST-HEART-RATE-CHEST-STRAP",
                manufacturer = "GENERIC_SCIENTIFIC",
                model = "BLUETOOTH_ECG_CHEST_STRAP_1000HZ",
                instrumentType = "HEART_RATE_SENSOR",
                sensorType = "BIOPOTENTIAL_ECG_ELECTRODES",
                supportedMetrics = listOf("HRV_RMSSD"),
                samplingRate = 1000.0,
                calibrationRequirement = false,
                calibrationIntervalDays = null,
                accuracySpecification = "Resolução R-R de 1 ms, Correlação com ECG clínico r > 0.99",
                firmware = "1.0.0",
                validationStatus = InstrumentValidationStatus.VALIDATED
            )
        )

        // 4. Analisador de Trocas Gasosas / Ergoespirometria (Metabolic Cart)
        register(
            MeasurementInstrument(
                instrumentId = "INST-METABOLIC-CART",
                manufacturer = "GENERIC_SCIENTIFIC",
                model = "BREATH_BY_BREATH_METABOLIC_CART",
                instrumentType = "METABOLIC_CART",
                sensorType = "PNEUMOTACHOMETER_PARAMAGNETIC_O2_NDIR_CO2",
                supportedMetrics = listOf("VO2_MAX"),
                samplingRate = 1.0,
                calibrationRequirement = true,
                calibrationIntervalDays = 1, // Calibração diária de 2 pontos (gás de calibração e volume de seringa 3L)
                accuracySpecification = "Precisão O2/CO2 < 0.03%, Precisão de volume < 1%",
                firmware = "1.0.0",
                validationStatus = InstrumentValidationStatus.VALIDATED
            )
        )

        // 5. Fotocélulas / Barreiras Ópticas de Cronometragem
        register(
            MeasurementInstrument(
                instrumentId = "INST-OPTICAL-TIMING-GATES",
                manufacturer = "GENERIC_SCIENTIFIC",
                model = "DUAL_BEAM_OPTICAL_GATES_1000HZ",
                instrumentType = "OPTICAL_TIMING_GATE",
                sensorType = "INFRARED_DUAL_BEAM",
                supportedMetrics = listOf("ACCELERATION"),
                samplingRate = 1000.0,
                calibrationRequirement = true,
                calibrationIntervalDays = 365,
                accuracySpecification = "Precisão temporal < 1 ms, Imune a falsos disparos de membros isolados",
                firmware = "1.0.0",
                validationStatus = InstrumentValidationStatus.VALIDATED
            )
        )

        // 6. Cicloergômetro Eletromagnético
        register(
            MeasurementInstrument(
                instrumentId = "INST-CYCLE-ERGOMETER",
                manufacturer = "GENERIC_SCIENTIFIC",
                model = "ELECTROMAGNETIC_BRAKE_ERGOMETER",
                instrumentType = "CYCLE_ERGOMETER",
                sensorType = "TORQUE_LOAD_CELL_CADENCE_REED",
                supportedMetrics = listOf("CRITICAL_POWER", "W_PRIME", "VO2_MAX"),
                samplingRate = 10.0,
                calibrationRequirement = true,
                calibrationIntervalDays = 90,
                accuracySpecification = "Erro de potência < 1.5% na faixa de 50-1500W",
                firmware = "1.0.0",
                validationStatus = InstrumentValidationStatus.VALIDATED
            )
        )

        // 7. Goniômetro Digital / Inclinômetro
        register(
            MeasurementInstrument(
                instrumentId = "INST-DIGITAL-GONIOMETER",
                manufacturer = "GENERIC_SCIENTIFIC",
                model = "HIGH_PRECISION_DIGITAL_GONIOMETER",
                instrumentType = "IMU",
                sensorType = "MEMS_ACCELEROMETER_GYROSCOPE",
                supportedMetrics = listOf("ROM"),
                samplingRate = 100.0,
                calibrationRequirement = true,
                calibrationIntervalDays = 180,
                accuracySpecification = "Precisão angular < 0.5 graus",
                firmware = "1.0.0",
                validationStatus = InstrumentValidationStatus.VALIDATED
            )
        )

        // 8. Câmera de Alta Velocidade (Visão Computacional)
        register(
            MeasurementInstrument(
                instrumentId = "INST-CAMERA-HIGH-SPEED-120FPS",
                manufacturer = "GENERIC_SCIENTIFIC",
                model = "OPTICAL_CAMERA_1080P_120FPS",
                instrumentType = "CAMERA",
                sensorType = "CMOS_GLOBAL_SHUTTER",
                supportedMetrics = listOf("ROM", "MEAN_PROPULSIVE_VELOCITY", "ACCELERATION"),
                samplingRate = 120.0,
                calibrationRequirement = true,
                calibrationIntervalDays = 30,
                accuracySpecification = "Resolução espacial calibrada por checkerboard < 2mm",
                firmware = "1.0.0",
                validationStatus = InstrumentValidationStatus.VALIDATED
            )
        )
    }

    fun register(instrument: MeasurementInstrument) {
        instruments[instrument.instrumentId] = instrument
    }

    fun getInstrument(instrumentId: String): MeasurementInstrument? {
        return instruments[instrumentId]
    }

    fun getInstrumentsForMetric(metricId: String): List<MeasurementInstrument> {
        return instruments.values.filter { it.supportedMetrics.contains(metricId) }
    }

    fun getAllInstruments(): List<MeasurementInstrument> {
        return instruments.values.toList()
    }

    fun containsInstrument(instrumentId: String): Boolean {
        return instruments.containsKey(instrumentId)
    }
}
