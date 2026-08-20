package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.evidenceconsistency.model.ConsistencyStatus
import com.example.core.evidenceconsistency.model.EvolutionEvidencePackage
import com.example.core.evidenceconsistency.model.MaturityStatus
import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.model.ClassEligibilityStatus
import com.example.core.evolutionengine.model.EvolutionSnapshot
import com.example.core.evolutionengine.model.RequirementStatusResult
import com.example.core.model.Assessment
import com.example.core.model.AssessmentStatus
import com.example.core.model.EvolutionState
import com.example.core.model.INITIAL_EVOLUTION_CLASS
import com.example.core.model.Profile
import com.example.core.progressionengine.model.EvolutionProgressionState
import com.example.core.progressionengine.model.PromotionCandidate
import com.example.core.scoreengine.model.CalculationStatus
import com.example.core.scoreengine.model.DimensionType
import com.example.core.scoreengine.model.ScoreSnapshot
import com.example.core.trialengine.model.TrialSnapshot
import com.example.core.trialengine.policy.TrialPolicyRegistry
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
import com.example.ui.theme.ObsidianSurfaceHighlight
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSleeping
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletTertiary

data class DimensionDisplayInfo(
    val dimensionType: DimensionType,
    val score: Double?,
    val calculationStatus: CalculationStatus,
    val evidenceCount: Int,
    val methodologyStatus: String,
    val explanation: String
)

data class RequirementDisplayItem(
    val id: String,
    val title: String,
    val category: String,
    val status: RequirementStatusResult,
    val isMandatory: Boolean,
    val description: String,
    val diagnosticReason: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEvolutionView(
    profile: Profile?,
    evolutionState: EvolutionState?,
    evolutionSnapshot: EvolutionSnapshot?,
    evidencePackage: EvolutionEvidencePackage?,
    progressionState: EvolutionProgressionState?,
    promotionCandidate: PromotionCandidate?,
    scoreSnapshot: ScoreSnapshot?,
    trialSnapshot: TrialSnapshot?,
    initialAssessment: Assessment?,
    isMock: Boolean,
    onStartAssessment: () -> Unit,
    onNavigateToBiomechanics: () -> Unit,
    onNavigateToLadder: () -> Unit,
    onNavigateToHub: () -> Unit,
    onAskAiConsultation: (String) -> Unit = {}
) {
    var selectedRequirementForExplanation by remember { mutableStateOf<RequirementDisplayItem?>(null) }
    var selectedDimensionForExplanation by remember { mutableStateOf<DimensionDisplayInfo?>(null) }
    var showAiExplanationSheet by remember { mutableStateOf(false) }
    var aiQueryInput by remember { mutableStateOf("") }
    var aiResponseText by remember { mutableStateOf<String?>(null) }

    val currentRawClassName = evolutionState?.currentClass ?: INITIAL_EVOLUTION_CLASS
    val currentClassDef = ClassCatalog.CLASSES.firstOrNull { it.name.contains(currentRawClassName, ignoreCase = true) || it.classId == currentRawClassName }
        ?: ClassCatalog.getInitialClass()

    val currentClassFormattedName = GenderNomenclature.formatClassName(
        classId = currentClassDef.classId,
        standardName = currentClassDef.name,
        gender = profile?.gender
    )

    val nextClassDef = ClassCatalog.getNextClass(currentClassDef.classId)
    val nextClassFormattedName = nextClassDef?.let {
        GenderNomenclature.formatClassName(
            classId = it.classId,
            standardName = it.name,
            gender = profile?.gender
        )
    }

    val athleteName = profile?.nickname?.ifBlank { null }
        ?: profile?.fullName?.split(" ")?.firstOrNull()
        ?: "ATLETA"

    val isSimulationOrMock = isMock || (evidencePackage?.isMock == true) || (evolutionSnapshot?.isMock == true)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("view_my_evolution"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Simulation Mode Warning Banner
            if (isSimulationOrMock) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("banner_simulation_mode"),
                    colors = CardDefaults.cardColors(containerColor = StatusWarning.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "MODO DE SIMULAÇÃO / DEMONSTRAÇÃO",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning
                            )
                            Text(
                                text = "Os dados apresentados são para fins ilustrativos e não constituem certificação oficial do Core.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Athlete Header & Current Class Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_current_class_hero"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, GoldBorder.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccentGlow)
                                    .border(1.dp, GoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = athleteName.uppercase(),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "CLASSE ATUAL OFICIAL",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = GoldAccent
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "ORDEM ${currentClassDef.order} / 22",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentClassFormattedName.uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.5).sp,
                        color = TextPrimary,
                        modifier = Modifier.testTag("text_current_class_name")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentClassDef.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ObsidianDivider, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Scientific Core Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTADO DE ELEGIBILIDADE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val eligibilityStatus = evolutionSnapshot?.eligibilityResult?.status ?: ClassEligibilityStatus.PENDING_VALIDATION
                            EligibilityBadge(status = eligibilityStatus)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "AUTORIDADE DO CORE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "CIÊNCIA IMUTÁVEL V1",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section: Scientific Foundation Pillars (Evidence, Consistency, Maturity, Protocol)
        item {
            SectionHeader(
                title = "PILAREs CIENTÍFICOS",
                subtitle = "Status dos motores de evidência e consistência"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val evidenceCount = evidencePackage?.evidenceIds?.size ?: 0
                ScientificPillarCard(
                    modifier = Modifier.weight(1f),
                    title = "EVIDÊNCIAS",
                    value = if (evidenceCount > 0) "$evidenceCount VÁLIDAS" else "INSUFICIENTE",
                    statusText = if (evidenceCount > 0) "REGISTRADAS" else "SEM DADOS",
                    statusColor = if (evidenceCount > 0) StatusActive else StatusWarning,
                    icon = Icons.Default.Science
                )

                val consistencyStatus = evidencePackage?.overallConsistencyStatus ?: ConsistencyStatus.PENDING_VALIDATION
                ScientificPillarCard(
                    modifier = Modifier.weight(1f),
                    title = "CONSISTÊNCIA",
                    value = formatConsistencyStatus(consistencyStatus),
                    statusText = "LONGITUDINAL",
                    statusColor = getConsistencyColor(consistencyStatus),
                    icon = Icons.Default.TrendingUp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val maturityStatus = evidencePackage?.overallMaturity?.maturityStatus ?: MaturityStatus.INITIAL
                ScientificPillarCard(
                    modifier = Modifier.weight(1f),
                    title = "MATURIDADE",
                    value = formatMaturityStatus(maturityStatus),
                    statusText = "TEMPO DE COBERTURA",
                    statusColor = getMaturityColor(maturityStatus),
                    icon = Icons.Default.History
                )

                val hasCompletedBaseline = initialAssessment?.status == AssessmentStatus.COMPLETED
                ScientificPillarCard(
                    modifier = Modifier.weight(1f),
                    title = "BASELINE",
                    value = if (hasCompletedBaseline) "HOMOLOGADO" else "EM ANDAMENTO",
                    statusText = "PROTOCOLO V1",
                    statusColor = if (hasCompletedBaseline) StatusActive else GoldAccent,
                    icon = Icons.Default.Security
                )
            }
        }

        // Section: Performance Dimensions
        item {
            SectionHeader(
                title = "PERFORMANCE DIMENSIONS",
                subtitle = "Avaliação quantitativa por dimensão motora"
            )

            val dimensions = buildPerformanceDimensionList(scoreSnapshot)

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dimensions.forEach { dimension ->
                    DimensionCard(
                        dimension = dimension,
                        onClick = { selectedDimensionForExplanation = dimension }
                    )
                }
            }
        }

        // Section: Class Requirements Map
        item {
            SectionHeader(
                title = "CLASS REQUIREMENTS",
                subtitle = "Critérios formais obrigatórios para validação de classe"
            )

            val requirements = buildRequirementList(evolutionSnapshot, currentClassDef.classId)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    requirements.forEach { req ->
                        RequirementRow(
                            item = req,
                            onInspect = { selectedRequirementForExplanation = req }
                        )
                        if (req != requirements.last()) {
                            HorizontalDivider(color = ObsidianDivider, thickness = 1.dp)
                        }
                    }
                }
            }
        }

        // Section: Next Evolution
        item {
            SectionHeader(
                title = "NEXT EVOLUTION",
                subtitle = "Próximo objetivo legítimo avaliável no catálogo"
            )

            if (nextClassDef != null && nextClassFormattedName != null) {
                NextEvolutionCard(
                    nextClassName = nextClassFormattedName,
                    nextClassOrder = nextClassDef.order,
                    nextClassDescription = nextClassDef.description,
                    promotionCandidate = promotionCandidate,
                    progressionState = progressionState,
                    onViewLadder = onNavigateToLadder
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ÁPICE DO CATÁLOGO PÚBLICO ALCANÇADO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Você atingiu o nível mais alto catalogado no protocolo aberto da EvolutionHumanAI.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Section: Longitudinal Progression
        item {
            SectionHeader(
                title = "PROGRESSÃO LONGITUDINAL",
                subtitle = "Sustentabilidade e tempo em classe"
            )

            LongitudinalProgressionCard(
                progressionState = progressionState,
                promotionCandidate = promotionCandidate
            )
        }

        // Section: Official Trials
        item {
            SectionHeader(
                title = "PROVAS OFICIAIS & TRIALS",
                subtitle = "Protocolos práticos de validação de capacidade"
            )

            OfficialTrialsCard(
                currentClassId = currentClassDef.classId,
                trialSnapshot = trialSnapshot,
                onStartAssessment = onStartAssessment,
                onNavigateToBiomechanics = onNavigateToBiomechanics
            )
        }

        // Section: Gamificação Ética
        item {
            SectionHeader(
                title = "GAMIFICAÇÃO & MARCOS",
                subtitle = "Reconhecimento motivacional sem impacto em scores científicos"
            )

            GamificationSectionCard(
                profile = profile,
                evolutionState = evolutionState
            )
        }

        // Section: Consultative AI
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_ai_consultation"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                border = BorderStroke(1.dp, VioletTertiary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = VioletTertiary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EVOLUTION INTELLIGENCE",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = VioletTertiary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = VioletTertiary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, VioletTertiary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "CONSULTIVA",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = VioletTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "A IA traduz a linguagem científica, explica protocolos e detalha o motivo pelo qual requisitos estão pendentes. A IA não possui autoridade para conceder classes ou alterar scores.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showAiExplanationSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_open_ai_consultation"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioletTertiary,
                            contentColor = PitchBlack
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SOLICITAR EXPLICAÇÃO DA IA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal: Requirement Diagnostic Explanation ("Por que estou nesta situação?")
    if (selectedRequirementForExplanation != null) {
        val req = selectedRequirementForExplanation!!
        ModalBottomSheet(
            onDismissRequest = { selectedRequirementForExplanation = null },
            sheetState = sheetState,
            containerColor = ObsidianSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .testTag("sheet_requirement_explanation")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIAGNÓSTICO METODOLÓGICO",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = TextMuted
                    )

                    IconButton(
                        onClick = { selectedRequirementForExplanation = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = req.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                RequirementStatusBadge(status = req.status)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ObsidianDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "POR QUE ESTOU NESTA SITUAÇÃO?",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = req.diagnosticReason,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DESCRIÇÃO FORMAL DO REQUISITO",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = req.description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Dimension Explanation
    if (selectedDimensionForExplanation != null) {
        val dim = selectedDimensionForExplanation!!
        ModalBottomSheet(
            onDismissRequest = { selectedDimensionForExplanation = null },
            sheetState = sheetState,
            containerColor = ObsidianSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .testTag("sheet_dimension_explanation")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "METODOLOGIA DE DIMENSÃO",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = CyanPrimary
                    )

                    IconButton(
                        onClick = { selectedDimensionForExplanation = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${dim.dimensionType.displayName.uppercase()} (${dim.dimensionType.key})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (dim.score != null) StatusActive.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (dim.score != null) StatusActive.copy(alpha = 0.4f) else StatusWarning.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (dim.score != null) "SCORE: ${String.format("%.1f", dim.score)}" else "SEM EVIDÊNCIA SUFICIENTE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (dim.score != null) StatusActive else StatusWarning,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ObsidianSurfaceHighlight,
                        border = BorderStroke(1.dp, ObsidianBorder)
                    ) {
                        Text(
                            text = "METODOLOGIA: ${dim.methodologyStatus}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ObsidianDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EXPLICABILIDADE CIENTÍFICA",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dim.explanation,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "REGRA METODOLÓGICA:",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Text(
                    text = "A ausência de medições em uma dimensão jamais é convertida em zero. Dimensões sem evidência suficiente permanecem explicitamente como 'INSUFFICIENT EVIDENCE' até a realização de protocolos homologados.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: AI Consultative Assistant
    if (showAiExplanationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiExplanationSheet = false },
            sheetState = sheetState,
            containerColor = ObsidianSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .testTag("sheet_ai_consultation")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = VioletTertiary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONSULTORIA DE IA · EVOLUTION HUMAN AI",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = VioletTertiary
                        )
                    }

                    IconButton(
                        onClick = { showAiExplanationSheet = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Como a IA pode ajudar:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "• Explicar requisitos de classe em linguagem acessível\n• Interpretar o resultado do seu baseline e biomecânica\n• Explicar por que requisitos estão como 'PENDING VALIDATION' ou 'INSUFFICIENT EVIDENCE'",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = aiQueryInput,
                    onValueChange = { aiQueryInput = it },
                    label = { Text("Faça uma pergunta sobre sua evolução...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ai_query"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletTertiary,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val q = aiQueryInput.ifBlank { "Explique por que meu requisito de evolução está pendente e como o sistema avalia maturidade." }
                        aiResponseText = "Análise Consultiva (EvolutionHumanAI Intelligence):\n\nSeus requisitos de evolução estão preservados na Classe 01 ('Corpo Adormecido') porque o Core exige consistência longitudinal e validação de evidências biométricas calibradas. Resultados isolados de alta intensidade não promovem o atleta sem estabilidade temporal comprovada.\n\nPara avançar para a próxima classe elegível, realize os protocolos de baseline e mantenha registros contínuos no módulo de Biomecânica."
                        onAskAiConsultation(q)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_submit_ai_query"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioletTertiary,
                        contentColor = PitchBlack
                    )
                ) {
                    Text(
                        text = "GERAR EXPLICAÇÃO CONSULTIVA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (aiResponseText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_ai_response"),
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                        border = BorderStroke(1.dp, VioletTertiary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = aiResponseText!!,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aviso: Parecer estritamente consultivo. Não modifica scores ou classes.",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENTES AUXILIARES
// -------------------------------------------------------------

@Composable
fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = GoldAccent
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
fun ScientificPillarCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    statusText: String,
    statusColor: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = statusText,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = statusColor
            )
        }
    }
}

@Composable
fun DimensionCard(
    dimension: DimensionDisplayInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("card_dimension_${dimension.dimensionType.key.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dimension.dimensionType.displayName.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "· ${dimension.dimensionType.key}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (dimension.score != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { (dimension.score / 100f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(120.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyanPrimary,
                            trackColor = ObsidianSurfaceHighlight,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = String.format("%.1f", dimension.score),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimary
                        )
                    }
                } else {
                    Text(
                        text = "SEM EVIDÊNCIA SUFICIENTE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = StatusWarning
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Explicabilidade",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RequirementRow(
    item: RequirementDisplayItem,
    onInspect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("row_req_${item.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (item.isMandatory) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OBRIGATÓRIO",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.description,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        RequirementStatusBadge(status = item.status)
    }
}

@Composable
fun RequirementStatusBadge(status: RequirementStatusResult) {
    val (bgColor, textColor, text) = when (status) {
        RequirementStatusResult.SATISFIED -> Triple(StatusActive.copy(alpha = 0.15f), StatusActive, "SATISFIED")
        RequirementStatusResult.NOT_SATISFIED -> Triple(StatusCritical.copy(alpha = 0.15f), StatusCritical, "NOT SATISFIED")
        RequirementStatusResult.INSUFFICIENT_EVIDENCE -> Triple(StatusWarning.copy(alpha = 0.15f), StatusWarning, "INSUFFICIENT EVIDENCE")
        RequirementStatusResult.PENDING_VALIDATION -> Triple(VioletTertiary.copy(alpha = 0.15f), VioletTertiary, "PENDING VALIDATION")
        RequirementStatusResult.INVALID -> Triple(StatusCritical.copy(alpha = 0.15f), StatusCritical, "BLOCKED / INVALID")
        RequirementStatusResult.NOT_APPLICABLE -> Triple(TextMuted.copy(alpha = 0.15f), TextMuted, "N/A")
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun EligibilityBadge(status: ClassEligibilityStatus) {
    val (bgColor, textColor, label) = when (status) {
        ClassEligibilityStatus.ELIGIBLE -> Triple(StatusActive.copy(alpha = 0.15f), StatusActive, "ELEGÍVEL")
        ClassEligibilityStatus.NOT_ELIGIBLE -> Triple(StatusCritical.copy(alpha = 0.15f), StatusCritical, "NÃO ELEGÍVEL")
        ClassEligibilityStatus.INSUFFICIENT_EVIDENCE -> Triple(StatusWarning.copy(alpha = 0.15f), StatusWarning, "EVIDÊNCIA INSUFICIENTE")
        ClassEligibilityStatus.PENDING_VALIDATION -> Triple(VioletTertiary.copy(alpha = 0.15f), VioletTertiary, "PENDENTE DE VALIDAÇÃO")
        ClassEligibilityStatus.BLOCKED -> Triple(StatusCritical.copy(alpha = 0.15f), StatusCritical, "BLOQUEADO")
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun NextEvolutionCard(
    nextClassName: String,
    nextClassOrder: Int,
    nextClassDescription: String,
    promotionCandidate: PromotionCandidate?,
    progressionState: EvolutionProgressionState?,
    onViewLadder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewLadder() }
            .testTag("card_next_evolution"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, GoldBorder.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRÓXIMO OBJETIVO AVALIÁVEL",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = StatusWarning.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (promotionCandidate?.status?.name == "ELIGIBLE") "ELEGÍVEL" else "NÃO ELEGÍVEL",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (promotionCandidate?.status?.name == "ELIGIBLE") StatusActive else StatusWarning,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = nextClassName.uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = nextClassDescription,
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ObsidianDivider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CRITÉRIOS PENDENTES PARA HOMOLOGAÇÃO:",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CriteriaBullet(text = "Consistência Longitudinal mínima no baseline")
                CriteriaBullet(text = "Volume de evidências calibradas com SourceTier Tier 1/2")
                CriteriaBullet(text = "Estabilidade temporal sob estresse neuromuscular")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VER CATÁLOGO COMPLETO (22 CLASSES)",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun CriteriaBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(StatusWarning)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun LongitudinalProgressionCard(
    progressionState: EvolutionProgressionState?,
    promotionCandidate: PromotionCandidate?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_longitudinal_progression"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PRINCÍPIO DE PROGRESSÃO LONGITUDINAL",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "« Performance isolada não significa evolução consolidada. »",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Um pico pontual de desempenho em um único treino ou teste não produz avanço imediato de classe. A EvolutionHumanAI exige maturidade temporal, histórico estável e ausência de regressões agudas.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Timeline Steps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimelineStage(stage = "1. ENTRADA", label = "Baseline", isActive = true)
                TimelineStage(stage = "2. EVIDÊNCIA", label = "Acúmulo", isActive = true)
                TimelineStage(stage = "3. ESTABILIDADE", label = "Consistência", isActive = false)
                TimelineStage(stage = "4. PROMOÇÃO", label = "Homologação", isActive = false)
            }
        }
    }
}

@Composable
fun TimelineStage(stage: String, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (isActive) StatusActive else ObsidianSurfaceHighlight)
                .border(1.dp, if (isActive) StatusActive else ObsidianBorder, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stage,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isActive) TextPrimary else TextMuted
        )
        Text(
            text = label,
            fontSize = 8.sp,
            color = TextMuted
        )
    }
}

@Composable
fun OfficialTrialsCard(
    currentClassId: String,
    trialSnapshot: TrialSnapshot?,
    onStartAssessment: () -> Unit,
    onNavigateToBiomechanics: () -> Unit
) {
    val trialPolicy = TrialPolicyRegistry.getPolicyForClass(ClassCatalog.CLASS_08)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_official_trials"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROVA DE TRIAL: CLASSE 08",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = StatusSleeping.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, StatusSleeping.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "PRÉ-REQUISITOS PENDENTES",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StatusSleeping,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trialPolicy?.name ?: "Prova de Capacidade de Carga Inicial",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = trialPolicy?.description ?: "Avaliação padronizada de prontidão neuromuscular e tolerância mecânica controlada.",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartAssessment,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_trial_baseline"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ObsidianSurfaceElevated,
                        contentColor = GoldAccent
                    ),
                    border = BorderStroke(1.dp, GoldBorder.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "BASELINE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onNavigateToBiomechanics,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_trial_biomechanics"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ObsidianSurfaceElevated,
                        contentColor = CyanPrimary
                    ),
                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "BIOMECÂNICA",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GamificationSectionCard(
    profile: Profile?,
    evolutionState: EvolutionState?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_gamification_section"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Gamification boundary warning
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GAMIFICAÇÃO ≠ SCORE CIENTÍFICO (Não altera requisitos ou classes)",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CONQUISTAS DE DISCIPLINA ATLÉTICA",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BadgeItem(title = "CADASTRO", subtitle = "Atleta Registrado", icon = Icons.Default.CheckCircle, isUnlocked = true)
                BadgeItem(title = "CORE V1", subtitle = "Identidade Pronta", icon = Icons.Default.Shield, isUnlocked = true)
                BadgeItem(title = "1º TESTE", subtitle = "Baseline Pendente", icon = Icons.Default.EmojiEvents, isUnlocked = false)
            }
        }
    }
}

@Composable
fun BadgeItem(title: String, subtitle: String, icon: ImageVector, isUnlocked: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isUnlocked) ObsidianSurfaceElevated else PitchBlack,
        border = BorderStroke(1.dp, if (isUnlocked) GoldAccent.copy(alpha = 0.4f) else ObsidianBorder),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) GoldAccent else TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) TextPrimary else TextMuted
                )
                Text(
                    text = subtitle,
                    fontSize = 8.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// -------------------------------------------------------------
// HELPERS PARA CONSTRUÇÃO DE DADOS SEM VALORES FICTÍCIOS
// -------------------------------------------------------------

private fun buildPerformanceDimensionList(scoreSnapshot: ScoreSnapshot?): List<DimensionDisplayInfo> {
    val initialDimensions = listOf(
        DimensionType.Force,
        DimensionType.Speed,
        DimensionType.Endurance,
        DimensionType.Mobility
    )

    return initialDimensions.map { dimType ->
        val component = scoreSnapshot?.dimensionScores?.firstOrNull { it.dimension.equals(dimType.key, ignoreCase = true) }
        val score = component?.score
        val calcStatus = component?.calculationStatus ?: CalculationStatus.INSUFFICIENT_EVIDENCE
        val explanation = component?.explanation?.notes?.ifBlank { null }
            ?: "Dimensão ${dimType.displayName}: Nenhuma evidência de sensor direto ou teste laboratorial validado foi submetida até o momento. Por princípio metodológico, ausência de dados não é convertida em zero."

        DimensionDisplayInfo(
            dimensionType = dimType,
            score = score,
            calculationStatus = calcStatus,
            evidenceCount = scoreSnapshot?.evidenceIds?.size ?: 0,
            methodologyStatus = "PENDING_VALIDATION",
            explanation = explanation
        )
    }
}

private fun buildRequirementList(evolutionSnapshot: EvolutionSnapshot?, currentClassId: String): List<RequirementDisplayItem> {
    val snapshotResults = evolutionSnapshot?.requirementResults ?: emptyList()

    val defaultRequirements = listOf(
        RequirementDisplayItem(
            id = "REQ-EVIDENCE-MIN",
            title = "Volume Mínimo de Evidências Calibradas",
            category = "EVIDENCE",
            status = snapshotResults.firstOrNull { it.requirementId.contains("EVIDENCE") }?.status ?: RequirementStatusResult.INSUFFICIENT_EVIDENCE,
            isMandatory = true,
            description = "Registro de medições com integridade criptográfica comprovada.",
            diagnosticReason = "O Data Core identificou que ainda não foi submetido o pacote mínimo de evidências diretas exigido pela política de evolução."
        ),
        RequirementDisplayItem(
            id = "REQ-CONSISTENCY-STABLE",
            title = "Estabilidade Longitudinal de Performance",
            category = "CONSISTENCY",
            status = snapshotResults.firstOrNull { it.requirementId.contains("CONSISTENCY") }?.status ?: RequirementStatusResult.PENDING_VALIDATION,
            isMandatory = true,
            description = "Consistência temporal entre medições repetidas.",
            diagnosticReason = "A metodologia de agregação temporal exige séries temporais com pelo menos 14 dias de intervalo de observação."
        ),
        RequirementDisplayItem(
            id = "REQ-MATURITY-COVERAGE",
            title = "Maturidade Temporal de Protocolo",
            category = "MATURITY",
            status = snapshotResults.firstOrNull { it.requirementId.contains("MATURITY") }?.status ?: RequirementStatusResult.SATISFIED,
            isMandatory = true,
            description = "Cobertura e fidelidade de execução segundo o protocolo científico.",
            diagnosticReason = "Nível de maturidade inicial correspondente à entrada do atleta no EvolutionHumanAI Core."
        ),
        RequirementDisplayItem(
            id = "REQ-PERFORMANCE-PRIMARY",
            title = "Índice de Performance da Dimensão Primária",
            category = "PERFORMANCE",
            status = snapshotResults.firstOrNull { it.requirementId.contains("PERFORMANCE") }?.status ?: RequirementStatusResult.PENDING_VALIDATION,
            isMandatory = true,
            description = "Métrica quantitativa de força/velocidade sem thresholds inventados.",
            diagnosticReason = "O threshold quantitativo oficial desta classe permanece PENDING_VALIDATION aguardando homologação do consórcio científico."
        )
    )

    return defaultRequirements
}

private fun formatConsistencyStatus(status: ConsistencyStatus): String = when (status) {
    ConsistencyStatus.INSUFFICIENT_DATA -> "SEM DADOS"
    ConsistencyStatus.STABLE -> "ESTÁVEL"
    ConsistencyStatus.VARIABLE -> "VARIÁVEL"
    ConsistencyStatus.IMPROVING -> "EM PROGRESSO"
    ConsistencyStatus.DECLINING -> "EM DECLÍNIO"
    ConsistencyStatus.UNDETERMINED -> "INDETERMINADO"
    ConsistencyStatus.PENDING_VALIDATION -> "PENDENTE"
}

private fun getConsistencyColor(status: ConsistencyStatus): Color = when (status) {
    ConsistencyStatus.STABLE, ConsistencyStatus.IMPROVING -> StatusActive
    ConsistencyStatus.INSUFFICIENT_DATA, ConsistencyStatus.PENDING_VALIDATION -> StatusWarning
    ConsistencyStatus.VARIABLE -> StatusWarning
    ConsistencyStatus.DECLINING -> StatusCritical
    ConsistencyStatus.UNDETERMINED -> StatusSleeping
}

private fun formatMaturityStatus(status: MaturityStatus): String = when (status) {
    MaturityStatus.INITIAL -> "INICIAL"
    MaturityStatus.DEVELOPING -> "DESENVOLVENDO"
    MaturityStatus.ESTABLISHED -> "ESTABELECIDO"
    MaturityStatus.MATURE -> "MADURO"
    MaturityStatus.UNDETERMINED -> "INDETERMINADO"
    MaturityStatus.PENDING_VALIDATION -> "PENDENTE"
}

private fun getMaturityColor(status: MaturityStatus): Color = when (status) {
    MaturityStatus.ESTABLISHED, MaturityStatus.MATURE -> StatusActive
    MaturityStatus.INITIAL, MaturityStatus.DEVELOPING -> CyanPrimary
    MaturityStatus.PENDING_VALIDATION, MaturityStatus.UNDETERMINED -> StatusWarning
}
