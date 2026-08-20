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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.INITIAL_EVOLUTION_CLASS
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
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class EvolutionTier(
    val rankNumber: Int,
    val name: String,
    val subtitle: String,
    val achieversCount: Int,
    val isMajorMilestone: Boolean,
    val isCurrent: Boolean,
    val isNext: Boolean,
    val isLocked: Boolean,
    val description: String,
    val requirements: List<String>,
    val catalystsRequired: Int
)

// Complete 22-class hierarchy
val ALL_EVOLUTION_CLASSES = listOf(
    EvolutionTier(
        rankNumber = 22,
        name = "Semideus",
        subtitle = "Pico Teórico do Potencial Humano",
        achieversCount = 3,
        isMajorMilestone = true,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Capacidade fisiológica, neuromuscular e homeostática no limiar máximo da espécie humana. Autoridade de referência global.",
        requirements = listOf("VO2 Max > 68 ml/kg/min", "Força Relativa > 2.8x Peso", "7 Provas Criptográficas Aprovadas"),
        catalystsRequired = 100000
    ),
    EvolutionTier(
        rankNumber = 21,
        name = "Ascendente",
        subtitle = "Domínio Metabólico e Neuromotor",
        achieversCount = 12,
        isMajorMilestone = true,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Excelente acoplamento neuromuscular e recuperação metabólica ultrarrápida sob estresse extremo.",
        requirements = listOf("Capacidade Crítica W' > 28 kJ", "Ritual de Passagem Ascendente"),
        catalystsRequired = 75000
    ),
    EvolutionTier(
        rankNumber = 20,
        name = "Lenda",
        subtitle = "Resistência Biomecânica de Elite",
        achieversCount = 22,
        isMajorMilestone = true,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Desempenho sustentado no percentil 99 da população atlética auditada.",
        requirements = listOf("Força Isométrica Pico > 2.2x Peso", "Prova de Estamina Contínua"),
        catalystsRequired = 52000
    ),
    EvolutionTier(
        rankNumber = 19,
        name = "Herói Ascendente",
        subtitle = "Transição para o Domínio Fisiológico",
        achieversCount = 41,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Consistência inabalável e resistência a microtraumas biomecânicos.",
        requirements = listOf("30 Provas Auditadas sem Regressão", "Índice de Estabilidade > 95%"),
        catalystsRequired = 38000
    ),
    EvolutionTier(
        rankNumber = 18,
        name = "Herói",
        subtitle = "Consistência e Alta Performance",
        achieversCount = 68,
        isMajorMilestone = true,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Conexão neuromuscular calibrada e tolerância absoluta ao lactato sob alta intensidade.",
        requirements = listOf("Avaliação Periódica Nível 4", "RFD > 2800 N/s"),
        catalystsRequired = 28000
    ),
    EvolutionTier(
        rankNumber = 17,
        name = "Colosso",
        subtitle = "Estrutura Biomecânica Consolidada",
        achieversCount = 145,
        isMajorMilestone = true,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Maturação dos tecidos conectivos e otimização dos vetores tridimensionais de força.",
        requirements = listOf("Mobilidade Escapular > 88%", "Força de Tração Validada"),
        catalystsRequired = 19000
    ),
    EvolutionTier(
        rankNumber = 16,
        name = "Titã",
        subtitle = "Potência e Densidade Muscular",
        achieversCount = 210,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Padrão motor estável com alta produção de torque em todas as cadeias cinéticas.",
        requirements = listOf("Potência Neuromuscular > 800W", "Assimetria Bilateral < 4%"),
        catalystsRequired = 14000
    ),
    EvolutionTier(
        rankNumber = 15,
        name = "Campeão",
        subtitle = "Sinal Detectado · Próximo Estágio do Core",
        achieversCount = 380,
        isMajorMilestone = true,
        isCurrent = false,
        isNext = true,
        isLocked = false,
        description = "Primeira consagração atlética. Transição completa de adaptação estrutural com dados verificados.",
        requirements = listOf("Conclusão do Protocolo de Baseline", "1ª Prova Biomecânica Auditada", "900 / 1.300 Catalisadores"),
        catalystsRequired = 1300
    ),
    EvolutionTier(
        rankNumber = 14,
        name = "Gladiador",
        subtitle = "Adaptação a Cargas Intensas",
        achieversCount = 490,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Eficiência biomecânica comprovada em repetições sob fadiga.",
        requirements = listOf("Tolerância a Carga RPE 8+", "Repetibilidade > 85%"),
        catalystsRequired = 1100
    ),
    EvolutionTier(
        rankNumber = 13,
        name = "Guerreiro",
        subtitle = "Disciplina de Treino Consolidada",
        achieversCount = 610,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Estabilização dos padrões motores essenciais e controle respiratório.",
        requirements = listOf("15 Sessões Auditadas Consecutivas", "HRV Baseline Calibrado"),
        catalystsRequired = 950
    ),
    EvolutionTier(
        rankNumber = 12,
        name = "Predador Atlético",
        subtitle = "Aceleração e Resposta Neuromuscular",
        achieversCount = 740,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Tempo de reação motor otimizado e rápida transição concêntrica-excêntrica.",
        requirements = listOf("Tempo de Contração < 180ms", "Estabilidade Articular 80%"),
        catalystsRequired = 800
    ),
    EvolutionTier(
        rankNumber = 11,
        name = "Especialista",
        subtitle = "Foco em Padrões Específicos",
        achieversCount = 890,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Domínio biomecânico refinado em pelo menos 3 gestos fundamentais.",
        requirements = listOf("Pontuação de Mobilidade > 70", "Check de Assimetria < 8%"),
        catalystsRequired = 650
    ),
    EvolutionTier(
        rankNumber = 10,
        name = "Atleta",
        subtitle = "Condicionamento Físico Pleno",
        achieversCount = 1020,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Capacidade de trabalho aeróbio e anaeróbio acima da média populacional.",
        requirements = listOf("VO2 Max Estimado > 45 ml/kg/min", "Recuperação Cardíaca em 60s"),
        catalystsRequired = 520
    ),
    EvolutionTier(
        rankNumber = 9,
        name = "Competidor",
        subtitle = "Mentalidade e Resiliência Fisiológica",
        achieversCount = 1180,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Início de testes sob pressão temporal e medições cinemáticas contínuas.",
        requirements = listOf("5 Provas de Velocidade Concluídas", "Consistência de Treino 3x/sem"),
        catalystsRequired = 400
    ),
    EvolutionTier(
        rankNumber = 8,
        name = "Atleta Emergente",
        subtitle = "Adaptação Neural Inicial",
        achieversCount = 1350,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Sinais evidentes de recrutamento de unidades motoras rápidas.",
        requirements = listOf("Gasto Energético Baseline Estabelecido", "Check de Prontidão Diária"),
        catalystsRequired = 300
    ),
    EvolutionTier(
        rankNumber = 7,
        name = "Discípulo",
        subtitle = "Aderência Rigorosa ao Protocolo",
        achieversCount = 1520,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Execução técnica repetível e registros diários de telemetria.",
        requirements = listOf("10 Registros de Biomecânica", "Alinhamento Postural Verificado"),
        catalystsRequired = 220
    ),
    EvolutionTier(
        rankNumber = 6,
        name = "Aprendiz",
        subtitle = "Aprendizado Motor Básico",
        achieversCount = 1700,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Coordenação intermuscular inicial e consciência corporal em movimento.",
        requirements = listOf("3 Medições de Amplitude Articular", "Compreensão de Carga"),
        catalystsRequired = 150
    ),
    EvolutionTier(
        rankNumber = 5,
        name = "Explorador",
        subtitle = "Exploração de Limites Biomecânicos",
        achieversCount = 1910,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Identificação de encurtamentos musculares e ativação das cadeias primárias.",
        requirements = listOf("Mapeamento Articular Completo", "Teste de Flexibilidade"),
        catalystsRequired = 90
    ),
    EvolutionTier(
        rankNumber = 4,
        name = "Iniciado",
        subtitle = "Início da Jornada Estruturada",
        achieversCount = 2150,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Conexão de sensores corporais e calibragem do perfil cinemático.",
        requirements = listOf("Calibração de Sensor Cinemático", "Consentimento de Auditoria"),
        catalystsRequired = 45
    ),
    EvolutionTier(
        rankNumber = 3,
        name = "Desperto",
        subtitle = "Quebra da Inércia Fisiológica",
        achieversCount = 2480,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Primeiros estímulos de carga e reconhecimento neuromuscular pelo Core.",
        requirements = listOf("Primeira Sessão de Movimento Registrada", "Dados Biométricos Preenchidos"),
        catalystsRequired = 20
    ),
    EvolutionTier(
        rankNumber = 2,
        name = "Sobrevivente",
        subtitle = "Resistência Inicial ao Esforço",
        achieversCount = 2890,
        isMajorMilestone = false,
        isCurrent = false,
        isNext = false,
        isLocked = true,
        description = "Superação do desconforto inicial e ativação do metabolismo de esforço.",
        requirements = listOf("Registro de Frequência Cardíaca em Repouso", "Teste de Estabilidade"),
        catalystsRequired = 5
    ),
    EvolutionTier(
        rankNumber = 1,
        name = "Corpo Adormecido",
        subtitle = "ESTADO ATUAL · Baseline Universal",
        achieversCount = 3450,
        isMajorMilestone = true,
        isCurrent = true,
        isNext = false,
        isLocked = false,
        description = "Ponto de partida universal na EvolutionHumanAI. Nenhuma promoção ocorre sem auditoria empírica pelo Core.",
        requirements = listOf("Cadastro Biométrico no Sistema", "Iniciação do Protocolo Foundation"),
        catalystsRequired = 0
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvolutionLadderView(
    currentClassName: String = INITIAL_EVOLUTION_CLASS,
    onClassSelected: (EvolutionTier) -> Unit = {}
) {
    var showAllClasses by remember { mutableStateOf(false) }
    var selectedTierForSheet by remember { mutableStateOf<EvolutionTier?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val displayedTiers = remember(showAllClasses, currentClassName) {
        ALL_EVOLUTION_CLASSES.map { tier ->
            tier.copy(
                isCurrent = tier.name.equals(currentClassName, ignoreCase = true),
                isNext = tier.rankNumber == 15 // Campeão
            )
        }.filter { tier ->
            if (showAllClasses) true else tier.isMajorMilestone
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Status & View Toggle Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(StatusActive)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STATUS: NORMAL",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = "22 CLASSES NO SISTEMA",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = GoldAccent,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // View Mode Filter Chips (Condensada vs 22 Classes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showAllClasses,
                onClick = { showAllClasses = false },
                label = { Text("Visão Condensada (Marcos)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ObsidianSurfaceElevated,
                    selectedLabelColor = GoldAccent,
                    containerColor = ObsidianSurface,
                    labelColor = TextMuted
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (!showAllClasses) GoldBorder else ObsidianBorder,
                    enabled = true,
                    selected = !showAllClasses
                ),
                modifier = Modifier.testTag("filter_condensed_view")
            )

            FilterChip(
                selected = showAllClasses,
                onClick = { showAllClasses = true },
                label = { Text("Todas as 22 Classes", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ObsidianSurfaceElevated,
                    selectedLabelColor = GoldAccent,
                    containerColor = ObsidianSurface,
                    labelColor = TextMuted
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (showAllClasses) GoldBorder else ObsidianBorder,
                    enabled = true,
                    selected = showAllClasses
                ),
                modifier = Modifier.testTag("filter_all_classes_view")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(displayedTiers) { index, tier ->
                EvolutionLadderItem(
                    tier = tier,
                    isLast = index == displayedTiers.lastIndex,
                    onClick = {
                        selectedTierForSheet = tier
                        onClassSelected(tier)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Modal Sheet for Inspecting Class
    if (selectedTierForSheet != null) {
        val tier = selectedTierForSheet!!
        ModalBottomSheet(
            onDismissRequest = { selectedTierForSheet = null },
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
                            text = String.format("CLASSE %02d / 22", tier.rankNumber),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (tier.isCurrent) StatusActive else if (tier.isNext) GoldAccent else TextMuted
                        )
                        Text(
                            text = tier.name.uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = if (tier.isCurrent) TextPrimary else if (tier.isNext) GoldAccent else TextSecondary
                        )
                    }

                    if (tier.isCurrent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusActive.copy(alpha = 0.15f))
                                .border(1.dp, StatusActive, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ATIVO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusActive,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else if (tier.isNext) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldAccentGlow)
                                .border(1.dp, GoldAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRÓXIMO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = tier.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "REQUISITOS OFICIAIS DO CORE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                tier.requirements.forEach { req ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (tier.isCurrent) Icons.Default.Check else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (tier.isCurrent) StatusActive else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = req,
                            fontSize = 12.sp,
                            color = if (tier.isCurrent) TextPrimary else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security Note
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
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Promoções são calculadas exclusivamente pelo Evolution Engine após testes empíricos auditados.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EvolutionLadderItem(
    tier: EvolutionTier,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val nodeColor = when {
        tier.isCurrent -> StatusActive
        tier.isNext -> GoldAccent
        tier.isLocked -> TextDisabled
        else -> TextMuted
    }

    val textColor = when {
        tier.isCurrent -> TextPrimary
        tier.isNext -> GoldAccent
        tier.isLocked -> TextMuted
        else -> TextSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tier_${tier.rankNumber}_${tier.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Diamond Icon Node (`◇` / `◈`)
            Box(
                modifier = Modifier
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tier.isCurrent) "◈" else "◇",
                    fontSize = if (tier.isCurrent) 18.sp else 16.sp,
                    color = nodeColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Class Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%02d", tier.rankNumber),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tier.name.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = if (tier.isCurrent || tier.isNext) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 1.2.sp,
                            fontFamily = FontFamily.Monospace,
                            color = textColor
                        )
                    }

                    if (tier.isCurrent) {
                        Text(
                            text = "ATIVO",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = StatusActive
                        )
                    } else if (tier.isNext) {
                        Text(
                            text = "SINAL DETECTADO",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    } else {
                        Text(
                            text = "${tier.achieversCount}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = tier.subtitle,
                    fontSize = 11.sp,
                    color = if (tier.isNext) GoldAccent.copy(alpha = 0.85f) else TextMuted
                )
            }
        }

        // Connecting Vertical Line
        if (!isLast) {
            Box(
                modifier = Modifier
                    .padding(start = 11.dp)
                    .width(2.dp)
                    .height(24.dp)
                    .background(ObsidianDivider)
            )
        }
    }
}
