package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AuditLog
import com.example.core.model.AuditSeverity
import com.example.core.model.EvolutionState
import com.example.core.model.INITIAL_EVOLUTION_CLASS
import com.example.core.model.Profile
import com.example.ui.theme.CobaltSecondary
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentGlow
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDivider
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSleeping
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScientificMetric(
    val id: String,
    val name: String,
    val value: String,
    val unit: String,
    val protocol: String,
    val instrument: String,
    val physiologicalBasis: String,
    val performanceImpact: String,
    val validityRange: String,
    val color: Color
)

val SCIENTIFIC_METRICS_LIST = listOf(
    ScientificMetric(
        id = "vo2_max",
        name = "VO2 Max Estimado",
        value = "48.2",
        unit = "ml/kg/min",
        protocol = "Protocolo Submáximo Conconi em Cicloergômetro",
        instrument = "Sensor Cardíaco Óptico / PPG Calibrado",
        physiologicalBasis = "Volume máximo de oxigênio que o organismo capta, transporta e utiliza por quilograma de peso corporal por minuto.",
        performanceImpact = "Determina o teto aeróbio e a velocidade de ressintese de fosfocreatina nos intervalos de esforço.",
        validityRange = "Percentil 70-80 (Normal Atlética: 45 - 60 ml/kg/min)",
        color = CyanPrimary
    ),
    ScientificMetric(
        id = "relative_strength",
        name = "Força Dinâmica Relativa (1RM/BW)",
        value = "1.42x",
        unit = "Razão Peso Corporal",
        protocol = "Teste de Carga Progressiva com Encoder Linear",
        instrument = "Transdutor Linear de Posição (LPT 1000Hz)",
        physiologicalBasis = "Capacidade de gerar tensão muscular concêntrica máxima normalizada pela massa corporal total.",
        performanceImpact = "Fator primordial para aceleração, eficiência biomecânica e prevenção de desequilíbrios estruturais.",
        validityRange = "Classificação: Superior (Normal: 1.2x - 1.8x)",
        color = GoldAccent
    ),
    ScientificMetric(
        id = "rfd",
        name = "Taxa de Desenv. de Força (RFD)",
        value = "1850",
        unit = "N/s",
        protocol = "Tração Isométrica em Meio da Coxa (IMTP)",
        instrument = "Plataforma de Força Isométrica Biaxial",
        physiologicalBasis = "Gradiente de força produzido nos primeiros 100–200 milissegundos após o início da ativação neural.",
        performanceImpact = "Capacidade de explosão pura e disparo instantâneo de unidades motoras de alto limiar.",
        validityRange = "Excelente para transição para o estágio Campeão",
        color = GoldAccent
    ),
    ScientificMetric(
        id = "hrv",
        name = "Variabilidade Cardíaca (HRV rMSSD)",
        value = "68",
        unit = "ms",
        protocol = "Gravação de 5 minutos em Repouso Supino Matinal",
        instrument = "Fotopletismografia (PPG) de Alta Resolução",
        physiologicalBasis = "Raiz quadrada da média das diferenças sucessivas entre intervalos RR normais, medindo tônus parassimpático.",
        performanceImpact = "Indicador padrão-ouro de prontidão do sistema nervoso autônomo e adaptação ao estresse de treino.",
        validityRange = "Equilíbrio Homeostático Ótimo (Referência: 55 - 85 ms)",
        color = StatusActive
    ),
    ScientificMetric(
        id = "critical_power_w_prime",
        name = "Capacidade de Trabalho Crítico (W')",
        value = "18.4",
        unit = "kJ",
        protocol = "Modelo Matemático de Potência Crítica de 3 Parâmetros",
        instrument = "Dinamômetro Isocinético & Potenciômetro",
        physiologicalBasis = "Reserva finita de energia anaeróbia disponível para trabalho acima do Limiar Crítico (CP).",
        performanceImpact = "Determina quantas arrancadas ou esforços supra-máximos podem ser sustentados antes da exaustão.",
        validityRange = "Adequado para esportes de padrão intermitente",
        color = CyanPrimary
    ),
    ScientificMetric(
        id = "joint_stability",
        name = "Índice de Estabilidade Articular",
        value = "91%",
        unit = "Score Multi-eixo",
        protocol = "Mapeamento Cinemático Tridimensional com IA Biomecânica",
        instrument = "Visão Computacional + IMU Inercial 9 Eixos",
        physiologicalBasis = "Controle neuromuscular e proprioceptivo na estabilização dos eixos rotacionais sob perturbação.",
        performanceImpact = "Garante integridade ligamentar e eficiência na transferência vetorial de força.",
        validityRange = "Excelente (Assimetria < 4%)",
        color = TextPrimary
    )
)

enum class HubSection(val title: String) {
    PROGRESSION("Progression"),
    CORE_ENGINES("Core Engines"),
    AI_GATEWAY("AI Gateway"),
    AUDIT("Auditoria")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressionHubView(
    profile: Profile?,
    evolutionState: EvolutionState?,
    auditLogs: List<AuditLog>,
    onTestUnauthorizedPromotion: () -> Unit,
    onStartAssessment: () -> Unit
) {
    var activeSubSection by remember { mutableStateOf(HubSection.PROGRESSION) }
    var selectedMetricForDetail by remember { mutableStateOf<ScientificMetric?>(null) }
    var openedModuleDialog by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentLevel = evolutionState?.currentLevel ?: 6
    val currentXp = evolutionState?.currentXp ?: 900L
    val requiredXp = evolutionState?.requiredXpForNextLevel ?: 1300L
    val progressFraction = (currentXp.toFloat() / requiredXp.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Level & Catalysts Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianSurface)
                .border(1.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LEVEL $currentLevel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ObsidianDivider)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(GoldAccent)
                    )
                }
            }

            Text(
                text = "CATALISADORES: $currentXp / $requiredXp",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subtabs: Progression / Core Engines / AI Gateway / Audit
        TabRow(
            selectedTabIndex = activeSubSection.ordinal,
            containerColor = ObsidianSurface,
            contentColor = GoldAccent,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, ObsidianBorder, RoundedCornerShape(10.dp))
        ) {
            HubSection.values().forEach { section ->
                Tab(
                    selected = activeSubSection == section,
                    onClick = { activeSubSection = section },
                    text = {
                        Text(
                            text = section.title,
                            fontWeight = if (activeSubSection == section) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeSubSection) {
            HubSection.PROGRESSION -> ProgressionOverview(
                profile = profile,
                onOpenModule = { module -> openedModuleDialog = module },
                onSelectMetric = { metric -> selectedMetricForDetail = metric }
            )
            HubSection.CORE_ENGINES -> CoreEnginesList(
                onTestUnauthorizedPromotion = onTestUnauthorizedPromotion,
                onStartAssessment = onStartAssessment
            )
            HubSection.AI_GATEWAY -> AIGatewayConsultationView(evolutionState = evolutionState)
            HubSection.AUDIT -> AuditLogsList(logs = auditLogs)
        }
    }

    // Modal Sheet for Metric Explainability
    if (selectedMetricForDetail != null) {
        val metric = selectedMetricForDetail!!
        ModalBottomSheet(
            onDismissRequest = { selectedMetricForDetail = null },
            sheetState = sheetState,
            containerColor = ObsidianSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "METROLOGIA & BASE CIENTÍFICA",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = metric.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "${metric.value} ${metric.unit}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = metric.color
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ObsidianDivider)
                Spacer(modifier = Modifier.height(14.dp))

                MetricDetailItem("Definição Fisiológica:", metric.physiologicalBasis)
                MetricDetailItem("Protocolo de Teste:", metric.protocol)
                MetricDetailItem("Instrumento / Sensor:", metric.instrument)
                MetricDetailItem("Impacto na Performance:", metric.performanceImpact)
                MetricDetailItem("Faixa de Validade:", metric.validityRange)

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PitchBlack),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Todos os dados acima possuem correlação empírica direta e são assinados pelo Evidence Engine.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Sheet for Hub Modules
    if (openedModuleDialog != null) {
        ModalBottomSheet(
            onDismissRequest = { openedModuleDialog = null },
            sheetState = sheetState,
            containerColor = ObsidianSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = openedModuleDialog?.uppercase() ?: "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        color = GoldAccent
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccentGlow)
                            .border(1.dp, GoldBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EVOLUTION HUMAN AI CORE",
                            fontSize = 10.sp,
                            color = GoldAccent,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (openedModuleDialog) {
                    "INVENTORY" -> {
                        Text(text = "Protocolos & Baselines Ativos:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ModuleItemRow("Protocolo de Iniciação Foundation", "Ativo no Core")
                        ModuleItemRow("Baseline Biomecânico de Articulações", "Calibrado")
                        ModuleItemRow("Sensor Cinemático Inercial", "Conectado 100Hz")
                    }
                    "MAP" -> {
                        Text(text = "Mapeamento Biomecânico Corporal:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ModuleItemRow("Cadeia Posterior & Vigor", "Nível 1 · Baseline")
                        ModuleItemRow("Estabilidade Escapular & Ombro", "Nível 2 · Em Avaliação")
                        ModuleItemRow("Controle de Potência Neuromuscular", "Nível 1 · Inicial")
                    }
                    "JOURNAL" -> {
                        Text(text = "Registro Fisiológico & Recuperação:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ModuleItemRow("Qualidade de Sono & HRV (rMSSD 68ms)", "Calibrado")
                        ModuleItemRow("Carga de Treino Recente", "Registrada no Core")
                        ModuleItemRow("Índice de Estamina Diário", "74 / 100")
                    }
                    "SKILLS" -> {
                        Text(text = "Árvore de Habilidades & Evidências:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        ModuleItemRow("Contração Isométrica de Pico", "Desbloqueado (Corpo Adormecido)")
                        ModuleItemRow("Prova Biomecânica de Campeão", "Em Progresso (900/1300 XP)")
                        ModuleItemRow("Domínio de Potência Crítica (W')", "Bloqueado (Requer Campeão)")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MetricDetailItem(label: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = content,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ModuleItemRow(title: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurfaceElevated)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        Text(text = status, fontSize = 11.sp, color = GoldAccent, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ProgressionOverview(
    profile: Profile?,
    onOpenModule: (String) -> Unit,
    onSelectMetric: (ScientificMetric) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 4 Golden RPG Hub Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GoldenTile(
                    title = "INVENTORY",
                    icon = Icons.Default.MilitaryTech,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenModule("INVENTORY") }
                )
                GoldenTile(
                    title = "MAP",
                    icon = Icons.Default.Map,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenModule("MAP") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GoldenTile(
                    title = "JOURNAL",
                    icon = Icons.Default.Book,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenModule("JOURNAL") }
                )
                GoldenTile(
                    title = "SKILLS",
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenModule("SKILLS") }
                )
            }
        }

        // Scientific Performance Attributes (Explicit, Measurable, Explainable)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ATRIBUTOS CIENTÍFICOS EXPLICÁVEIS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = "TOQUE P/ DETALHES",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SCIENTIFIC_METRICS_LIST.forEach { metric ->
                        ScientificStatRow(metric = metric, onClick = { onSelectMetric(metric) })
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ScientificStatRow(metric: ScientificMetric, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Explicar",
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = metric.name,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
        }
        Text(
            text = "${metric.value} ${metric.unit}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = metric.color
        )
    }
}

@Composable
private fun GoldenTile(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianSurface)
            .border(1.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("hub_tile_${title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = GoldAccent,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                color = GoldAccent
            )
        }
    }
}

@Composable
private fun AIGatewayConsultationView(evolutionState: EvolutionState?) {
    var promptInput by remember { mutableStateOf("") }
    var consultationResponse by remember {
        mutableStateOf(
            "AI GATEWAY CONSULTIVO [v1.0.0]\n" +
            "Papel: Contextualizar padrões, interpretar fadiga biomecânica e orientar sobre requisitos do Core.\n" +
            "Restrição: A IA não tem permissão para alterar estados ou promover usuários. Promoções pertencem ao Evolution Engine."
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(GoldBorder)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CONTRATO ARQUITETURAL: CORE VS AI GATEWAY",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "EVOLUTION HUMAN AI CORE (Score, Evidence & Evolution Engines)\n" +
                               "  ├── Tem autoridade oficial sobre Estados & Promoções\n" +
                               "  └── Valida Provas Criptográficas e Rituais\n" +
                               "EVOLUTION INTELLIGENCE (Consultivo)\n" +
                               "  └── Interpretação de dados, sugestões e contexto sem poder de mutação.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "RESPOSTA DA CONSULTA",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = consultationResponse,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Ex: O que falta para a classe Campeão?", fontSize = 11.sp, color = TextMuted) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            consultationResponse = "Consulta: \"$promptInput\"\n\n" +
                                    "ANÁLISE DO GATEWAY: Seus sinais fisiológicos indicam prontidão para o estágio Campeão. " +
                                    "No entanto, o Evolution Engine requer que você conclua a Prova Biomecânica no menu 'Biomecânica' e complete os 400 Catalisadores restantes (900/1300)."
                            promptInput = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = PitchBlack)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Consultar", modifier = Modifier.size(16.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CoreEnginesList(
    onTestUnauthorizedPromotion: () -> Unit,
    onStartAssessment: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "CONTRATOS MODULARES DO CORE",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Motores fundamentais preparados para validação empírica de performance.",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        item {
            EngineItemCard("Score Engine Contract", "v1.0.0-foundation", "Computação oficial de prontidão, estamina e carga neuromuscular.", "Pronto")
        }
        item {
            EngineItemCard("Evidence Engine Contract", "v1.0.0-foundation", "Assinatura criptográfica e integridade de telemetria.", "Pronto")
        }
        item {
            EngineItemCard("Evolution Engine Contract", "v1.0.0-foundation", "Autoridade exclusiva para promoções a partir de 'Corpo Adormecido'.", "Pronto")
        }
        item {
            EngineItemCard("Mission & Trial Engines", "v1.0.0-foundation", "Rituais de passagem e missões cinematográficas de evolução.", "Pronto")
        }
        item {
            EngineItemCard("AI Gateway Abstraction", "v1.0.0-foundation", "Recomendações e contexto consultivo sem autoridade de mutação de estado.", "Ativo")
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onTestUnauthorizedPromotion,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_test_security_mutation"),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Testar Bloqueio de Promoção Não Autorizada", color = GoldAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EngineItemCard(title: String, version: String, description: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = version, fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StatusActive))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = status, fontSize = 10.sp, color = StatusActive, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun AuditLogsList(logs: List<AuditLog>) {
    val currentDateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CORE EVENT / $currentDateStr",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Text(
                    text = "LOGS CRIPTOGRÁFICOS",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
            }
        }

        // Baseline sequence matching user's requested system terminal style
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    TerminalLogLine("08:42:13", "ASSESSMENT COMPLETED", CyanPrimary)
                    TerminalLogLine("08:42:15", "EVIDENCE VALIDATED", StatusActive)
                    TerminalLogLine("08:42:16", "PERFORMANCE STATE UPDATED", GoldAccent)
                    TerminalLogLine("08:42:17", "EVOLUTION GATE CHECKED", TextPrimary)
                    TerminalLogLine("08:42:18", "TRIAL STATUS: LOCKED", TextMuted)
                }
            }
        }

        if (logs.isNotEmpty()) {
            items(logs) { log ->
                val severityColor = when (log.severity) {
                    AuditSeverity.INFO -> CyanPrimary
                    AuditSeverity.WARNING -> StatusWarning
                    AuditSeverity.SECURITY_VIOLATION, AuditSeverity.CRITICAL -> StatusCritical
                }
                val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = log.action, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = severityColor, fontFamily = FontFamily.Monospace)
                            Text(text = formattedTime, fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = log.detailsJson, fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TerminalLogLine(time: String, action: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = action,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
