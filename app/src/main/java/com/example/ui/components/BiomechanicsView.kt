package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDivider
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.RecordRed
import com.example.ui.theme.StatusActive
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class BiomechanicsTab(val title: String, val subtitle: String) {
    MOTION("Motion", "Amplitude · Ângulo · Trajetória"),
    FORCE("Force", "Aceleração · Impulso · Produção"),
    SPEED("Speed", "Velocidade · Tempo · Variação"),
    PATTERN("Pattern", "Estabilidade · Assimetria · Repetibilidade · Adaptação")
}

@Composable
fun BiomechanicsView(
    onEvidenceRecorded: (metricType: String, value: Double) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(BiomechanicsTab.MOTION) }
    var isRecording by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(1080L) }
    
    // Live Kinematic / Sensor telemetry states
    var currentAngle by remember { mutableDoubleStateOf(94.2) }
    var currentRom by remember { mutableDoubleStateOf(118.0) }
    var currentAcceleration by remember { mutableDoubleStateOf(1.8) }
    var currentImpulse by remember { mutableDoubleStateOf(245.0) }
    var currentForceN by remember { mutableDoubleStateOf(680.0) }
    var currentVelocity by remember { mutableDoubleStateOf(2.4) }
    var currentStability by remember { mutableDoubleStateOf(92.4) }
    var currentAsymmetry by remember { mutableDoubleStateOf(3.8) }
    var currentRepeatability by remember { mutableDoubleStateOf(95.1) }
    var currentFatigueDrift by remember { mutableDoubleStateOf(1.2) }

    var evidenceSubmittedMessage by remember { mutableStateOf<String?>(null) }

    // Timer & live updates when recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            val startTime = System.currentTimeMillis() - elapsedMs
            while (isRecording) {
                elapsedMs = System.currentTimeMillis() - startTime
                val t = elapsedMs / 1000.0
                currentAngle = 90.0 + 35.0 * sin(t * 2.8)
                currentRom = 115.0 + 8.0 * cos(t * 1.2)
                currentAcceleration = 1.6 + 1.2 * (sin(t * 3.4) * 0.5 + 0.5)
                currentImpulse = 230.0 + 30.0 * sin(t * 2.1)
                currentForceN = 650.0 + 120.0 * (sin(t * 3.4) * 0.5 + 0.5)
                currentVelocity = 2.1 + 1.4 * (cos(t * 2.9) * 0.5 + 0.5)
                currentStability = 91.0 + 3.0 * cos(t * 1.5)
                currentAsymmetry = 3.5 + 0.8 * sin(t * 0.8)
                currentRepeatability = 94.0 + 2.0 * cos(t * 2.0)
                currentFatigueDrift = 1.0 + (elapsedMs / 10000.0) * 0.4
                delay(30)
            }
        }
    }

    val minutes = (elapsedMs / 60000) % 60
    val seconds = (elapsedMs / 1000) % 60
    val hundredths = (elapsedMs % 1000) / 10
    val formattedTime = String.format("%02d:%02d.%02d", minutes, seconds, hundredths)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Timer & Reset Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) RecordRed else StatusActive)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRecording) "GRAVANDO TELEMETRIA" else "SENSOR CALIBRADO",
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) RecordRed else StatusActive
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        elapsedMs = 0L
                        isRecording = false
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Zerar",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RESET",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Big Precision Timer (Centésimos)
        Text(
            text = formattedTime,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            color = if (isRecording) CyanPrimary else TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Biomechanical Arm Canvas with tracking joints
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianSurface)
                .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            KinematicArmCanvas(angleDeg = currentAngle, isRecording = isRecording)

            // Real-time Angle & ROM Overlays
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PitchBlack.copy(alpha = 0.85f))
                    .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = String.format("ÂNGULO: %.1f°", currentAngle),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PitchBlack.copy(alpha = 0.85f))
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = String.format("ROM: %.0f°", currentRom),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Analysis Tabs: Motion / Force / Speed / Pattern
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = ObsidianSurface,
            contentColor = CyanPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
        ) {
            BiomechanicsTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier.testTag("biomech_tab_${tab.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic Chart Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CAMADA DE ANÁLISE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Text(
                            text = selectedTab.subtitle,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    }

                    Text(
                        text = "100 Hz",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chart Canvas based on tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceElevated)
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                ) {
                    when (selectedTab) {
                        BiomechanicsTab.MOTION -> MotionChart(currentAngle)
                        BiomechanicsTab.FORCE -> ForceChart(currentAcceleration)
                        BiomechanicsTab.SPEED -> SpeedChart(currentVelocity)
                        BiomechanicsTab.PATTERN -> PatternChart(currentStability, currentAsymmetry)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Layer Metrics Details
                when (selectedTab) {
                    BiomechanicsTab.MOTION -> {
                        MetricDetailRow("Amplitude Articular (ROM)", String.format("%.1f°", currentRom), "Normal > 110°")
                        MetricDetailRow("Ângulo Instantâneo", String.format("%.1f°", currentAngle), "Flexão de Cotovelo")
                        MetricDetailRow("Trajetória Biomecânica", "Linear 98.4%", "Desvio < 2.5mm")
                    }
                    BiomechanicsTab.FORCE -> {
                        MetricDetailRow("Aceleração Média", String.format("%.2f m/s²", currentAcceleration), "Pico 2.8g")
                        MetricDetailRow("Impulso Neuromuscular", String.format("%.0f N·s", currentImpulse), "Área sob a curva")
                        MetricDetailRow("Produção de Força Pico", String.format("%.0f N", currentForceN), "RFD 1850 N/s")
                    }
                    BiomechanicsTab.SPEED -> {
                        MetricDetailRow("Velocidade Angular", String.format("%.2f m/s", currentVelocity), "Pico 3.8 m/s")
                        MetricDetailRow("Tempo até Velocidade Pico", "180 ms", "Explosividade Alta")
                        MetricDetailRow("Variação de Desaceleração", "-1.4 m/s²", "Fase Excêntrica")
                    }
                    BiomechanicsTab.PATTERN -> {
                        MetricDetailRow("Estabilidade Articular", String.format("%.1f%%", currentStability), "Padrão Excelente")
                        MetricDetailRow("Assimetria Bilateral", String.format("%.1f%%", currentAsymmetry), "Tolerância < 5.0%")
                        MetricDetailRow("Repetibilidade Motora", String.format("%.1f%%", currentRepeatability), "Consistência de Ciclo")
                        MetricDetailRow("Adaptação / Deriva Fadiga", String.format("+%.1f%%", currentFatigueDrift), "Estável sob carga")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Record Evidence & Telemetry Button (Pulsing Red)
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isRecording) 1.08f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Box(
            modifier = Modifier
                .size(72.dp)
                .clickable {
                    if (isRecording) {
                        isRecording = false
                        evidenceSubmittedMessage = "Evidência cinemática assinada e submetida ao Evidence Engine."
                        onEvidenceRecorded("BIOMECHANICAL_KINEMATICS", currentAngle)
                    } else {
                        isRecording = true
                        evidenceSubmittedMessage = null
                    }
                }
                .testTag("btn_record_telemetry"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((64 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) RecordRed.copy(alpha = 0.25f)
                        else CyanPrimary.copy(alpha = 0.15f)
                    )
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) RecordRed else CyanPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = if (isRecording) "Parar Gravação" else "Gravar Telemetria",
                    tint = PitchBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isRecording) "PARAR E SUBMETER AO CORE" else "GRAVAR PROVA BIOMECÂNICA",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = if (isRecording) RecordRed else CyanPrimary
        )

        if (evidenceSubmittedMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(GoldBorder)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = evidenceSubmittedMessage!!,
                        fontSize = 11.sp,
                        color = GoldAccent,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun MetricDetailRow(label: String, value: String, reference: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(text = reference, fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = CyanPrimary
        )
    }
}

@Composable
private fun KinematicArmCanvas(angleDeg: Double, isRecording: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val shoulderX = w * 0.3f
        val shoulderY = h * 0.4f

        val upperArmLen = w * 0.22f
        val forearmLen = w * 0.24f

        val upperArmAngleRad = 0.35
        val elbowX = shoulderX + (upperArmLen * cos(upperArmAngleRad)).toFloat()
        val elbowY = shoulderY + (upperArmLen * sin(upperArmAngleRad)).toFloat()

        val radAngle = Math.toRadians(angleDeg)
        val forearmAngleRad = upperArmAngleRad - radAngle
        val wristX = elbowX + (forearmLen * cos(forearmAngleRad)).toFloat()
        val wristY = elbowY + (forearmLen * sin(forearmAngleRad)).toFloat()

        // Connecting Bones (Obsidian & Cyan strokes)
        drawLine(
            color = Color(0xFF33333E),
            start = Offset(shoulderX, shoulderY),
            end = Offset(elbowX, elbowY),
            strokeWidth = 14f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        drawLine(
            color = CyanPrimary,
            start = Offset(elbowX, elbowY),
            end = Offset(wristX, wristY),
            strokeWidth = 10f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Articular Angle Arc
        drawArc(
            color = CyanPrimary.copy(alpha = 0.5f),
            startAngle = Math.toDegrees(upperArmAngleRad).toFloat() - angleDeg.toFloat(),
            sweepAngle = angleDeg.toFloat(),
            useCenter = false,
            topLeft = Offset(elbowX - 35f, elbowY - 35f),
            size = androidx.compose.ui.geometry.Size(70f, 70f),
            style = Stroke(width = 3f)
        )

        // Joints (Shoulder, Elbow, Wrist)
        drawCircle(color = Color(0xFF555566), radius = 10f, center = Offset(shoulderX, shoulderY))
        drawCircle(color = CyanPrimary, radius = 12f, center = Offset(elbowX, elbowY))
        drawCircle(color = Color.White, radius = 6f, center = Offset(elbowX, elbowY))
        drawCircle(color = CyanPrimary, radius = 8f, center = Offset(wristX, wristY))
    }
}

@Composable
private fun MotionChart(angle: Double) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val path = Path()

        val points = 30
        for (i in 0..points) {
            val x = (i.toFloat() / points) * w
            val normalized = (sin((i * 0.3) + (angle * 0.05)) * 0.4 + 0.5).toFloat()
            val y = (1f - normalized) * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = CyanPrimary,
            style = Stroke(width = 3f)
        )
    }
}

@Composable
private fun ForceChart(accel: Double) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val path = Path()

        val points = 30
        for (i in 0..points) {
            val x = (i.toFloat() / points) * w
            val normalized = ((sin((i * 0.5) + (accel * 0.3)) * 0.5 + 0.5) * 0.7 + 0.15).toFloat()
            val y = (1f - normalized) * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = CyanPrimary,
            style = Stroke(width = 3.5f)
        )
    }
}

@Composable
private fun SpeedChart(vel: Double) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val path = Path()

        path.moveTo(0f, h)
        val points = 25
        for (i in 0..points) {
            val x = (i.toFloat() / points) * w
            val normalized = ((cos((i * 0.35) + (vel * 0.2)) * 0.45 + 0.5) * 0.8).toFloat()
            val y = (1f - normalized) * h
            path.lineTo(x, y)
        }
        path.lineTo(w, h)
        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(CyanPrimary.copy(alpha = 0.5f), Color.Transparent)
            )
        )
    }
}

@Composable
private fun PatternChart(stability: Double, asymmetry: Double) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Horizontal midline for symmetry balance
        drawLine(
            color = Color(0xFF33333E),
            start = Offset(0f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = 2f
        )

        val path = Path()
        val points = 30
        for (i in 0..points) {
            val x = (i.toFloat() / points) * w
            // Stability variance around the symmetry midline
            val offset = (sin(i * 0.4) * (asymmetry * 0.05)).toFloat()
            val y = (h * 0.5f) + (offset * h * 0.4f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = CyanPrimary,
            style = Stroke(width = 3f)
        )
    }
}
