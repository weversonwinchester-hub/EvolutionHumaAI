package com.example.ui.dashboard

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.AssessmentStatus
import com.example.core.model.INITIAL_EVOLUTION_CLASS
import com.example.core.model.ProfileStatus
import com.example.ui.components.BiomechanicsView
import com.example.ui.components.ErrorBanner
import com.example.ui.components.EvolutionLadderView
import com.example.ui.components.MyEvolutionView
import com.example.ui.components.PerformAIBadge
import com.example.ui.components.ProgressionHubView
import com.example.ui.theme.CobaltSecondary
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
import com.example.ui.theme.StatusSleeping
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "syncPulse")
    val syncAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "syncAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EVOLUTION HUMAN AI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Text(
                            text = " · CORE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GoldAccent
                        )
                    }
                },
                actions = {
                    // Pulsing green sync status dot
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusActive.copy(alpha = syncAlpha))
                    )

                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.testTag("btn_logout")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sair",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PitchBlack
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, ObsidianBorder, RoundedCornerShape(0.dp))
            ) {
                NavigationBarItem(
                    selected = state.activeTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "My Evolution",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("EVOLUÇÃO", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldAccent,
                        selectedTextColor = GoldAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = GoldAccentGlow
                    ),
                    modifier = Modifier.testTag("tab_my_evolution")
                )

                NavigationBarItem(
                    selected = state.activeTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Classes",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("CLASSES", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldAccent,
                        selectedTextColor = GoldAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = GoldAccentGlow
                    ),
                    modifier = Modifier.testTag("tab_classes")
                )

                NavigationBarItem(
                    selected = state.activeTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LinearScale,
                            contentDescription = "Biomecânica",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("BIOMECÂNICA", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = CyanPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_biomechanics")
                )

                NavigationBarItem(
                    selected = state.activeTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Hub & Core",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("CORE & AUDIT", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldAccent,
                        selectedTextColor = GoldAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = GoldAccentGlow
                    ),
                    modifier = Modifier.testTag("tab_hub")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack)
                .padding(innerPadding)
        ) {
            when (state.activeTab) {
                0 -> MyEvolutionView(
                    profile = state.profile,
                    evolutionState = state.evolutionState,
                    evolutionSnapshot = state.evolutionSnapshot,
                    evidencePackage = state.evidencePackage,
                    progressionState = state.progressionState,
                    promotionCandidate = state.promotionCandidate,
                    scoreSnapshot = state.latestScoreSnapshot,
                    trialSnapshot = state.trialSnapshot,
                    initialAssessment = state.initialAssessment,
                    isMock = !state.isOfficialScore,
                    onStartAssessment = { viewModel.startInitialAssessment() },
                    onNavigateToBiomechanics = { viewModel.selectTab(2) },
                    onNavigateToLadder = { viewModel.selectTab(1) },
                    onNavigateToHub = { viewModel.selectTab(3) },
                    onAskAiConsultation = { query -> viewModel.askAiConsultation(query) }
                )
                1 -> EvolutionLadderView(
                    currentClassName = state.evolutionState?.currentClass ?: INITIAL_EVOLUTION_CLASS
                )
                2 -> BiomechanicsView(
                    onEvidenceRecorded = { _, _ ->
                        // Telemetry recorded and signed
                    }
                )
                3 -> ProgressionHubView(
                    profile = state.profile,
                    evolutionState = state.evolutionState,
                    auditLogs = state.recentAuditLogs,
                    onTestUnauthorizedPromotion = { viewModel.testUnauthorizedMutationAttempt() },
                    onStartAssessment = { viewModel.startInitialAssessment() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HumanPerformanceOsView(
    state: DashboardUiState,
    viewModel: DashboardViewModel,
    onNavigateToEvolution: () -> Unit,
    onNavigateToBiomechanics: () -> Unit
) {
    var showInspectionMode by remember { mutableStateOf(false) }
    val inspectionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val userName = state.profile?.nickname?.ifBlank { null }
        ?: state.profile?.fullName?.split(" ")?.firstOrNull()
        ?: state.user?.email?.substringBefore("@")?.uppercase()
        ?: "ATLETA"

    val currentDateStr = SimpleDateFormat("dd MMM", Locale("pt", "BR"))
        .format(Date())
        .uppercase()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Error Banner if present
            if (state.errorMessage != null) {
                ErrorBanner(message = state.errorMessage)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Success Banner if present
            if (state.assessmentSuccessMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StatusActive.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StatusActive.copy(alpha = 0.5f))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusActive)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.assessmentSuccessMessage,
                            color = StatusActive,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // User Greeting Header
            Text(
                text = "OI, ${userName.uppercase()} · $currentDateStr",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ObsidianDivider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Category Label & Performance Index Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PERFORMANCE INDEX",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = TextMuted
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (state.isOfficialScore) StatusActive.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (state.isOfficialScore) StatusActive.copy(alpha = 0.5f) else StatusWarning.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (state.isOfficialScore) "SCORE OFICIAL V1" else "DEMO / MOCK",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isOfficialScore) StatusActive else StatusWarning,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "76.4",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1.5).sp,
                    color = TextPrimary
                )

                Text(
                    text = "↑ 4.8%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = GoldAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Text(
                text = "nas últimas 2 semanas · acima da média do baseline",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ObsidianDivider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 4 Vital Dimension Bars
            PerformanceDimensionRow(label = "FORCE", score = 81, maxScore = 100)
            Spacer(modifier = Modifier.height(12.dp))
            PerformanceDimensionRow(label = "SPEED", score = 79, maxScore = 100)
            Spacer(modifier = Modifier.height(12.dp))
            PerformanceDimensionRow(label = "ENDURANCE", score = 74, maxScore = 100)
            Spacer(modifier = Modifier.height(12.dp))
            PerformanceDimensionRow(label = "MOBILITY", score = 63, maxScore = 100)

            Spacer(modifier = Modifier.height(20.dp))

            // Next Evolution Banner with Inspection Trigger
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInspectionMode = true }
                    .testTag("banner_next_evolution"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(GoldBorder.copy(alpha = 0.8f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EVOLUTION SIGNAL: CAMPEÃO",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SIGNAL DETECTED · TOQUE P/ INSPECIONAR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Inspecionar",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Initial Protocol / Assessment State
            val isAssessmentDone = state.initialAssessment?.status == AssessmentStatus.COMPLETED
            val isAssessmentInProgress = state.initialAssessment?.status == AssessmentStatus.IN_PROGRESS

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                            text = "PROTOCOLO DE BASELINE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = if (isAssessmentDone) "CONCLUÍDO" else if (isAssessmentInProgress) "EM ANDAMENTO" else "PENDENTE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isAssessmentDone) StatusActive else if (isAssessmentInProgress) GoldAccent else StatusSleeping
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Realize o teste de avaliação inicial ou teste biomecânico para submeter evidências ao Core.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startInitialAssessment() },
                            enabled = !state.isInitiatingAssessment && !isAssessmentDone,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_start_assessment"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAssessmentDone) ObsidianSurfaceElevated else GoldAccent,
                                contentColor = if (isAssessmentDone) TextMuted else PitchBlack
                            )
                        ) {
                            if (state.isInitiatingAssessment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PitchBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isAssessmentDone) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAssessmentDone) "BASELINE VALIDADO" else "INICIAR PROTOCOLO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToBiomechanics,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_open_biomechanics"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianSurfaceElevated,
                                contentColor = CyanPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.LinearScale,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BIOMECÂNICA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Evolution Inspection Mode Modal Sheet
    if (showInspectionMode) {
        ModalBottomSheet(
            onDismissRequest = { showInspectionMode = false },
            sheetState = inspectionSheetState,
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
                            text = "EVOLUTION INSPECTION MODE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "CAMPEÃO",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GoldAccent
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccentGlow)
                            .border(1.dp, GoldAccent, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SIGNAL DETECTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = GoldAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ObsidianDivider)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CRITÉRIOS DE AUDITORIA DO CORE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Evaluation Matrix
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InspectionCriterionCard("PERFORMANCE", "APROVADO", true, Modifier.weight(1f))
                    InspectionCriterionCard("EVIDENCE", "VALIDADA", true, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InspectionCriterionCard("CONSISTENCY", "74 / 85", false, Modifier.weight(1f))
                    InspectionCriterionCard("ADAPTATION", "APROVADO", true, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InspectionCriterionCard("BALANCE", "APROVADO", true, Modifier.weight(1f))
                    InspectionCriterionCard("MATURITY", "68 / 80", false, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PitchBlack),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GoldBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORE VALIDATION",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Text(
                                text = "TRIAL LOCKED",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2 CONDITIONS REMAIN · Requer 1 Prova Biomecânica Auditada e 400 Catalisadores restantes.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showInspectionMode = false
                        onNavigateToBiomechanics()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = PitchBlack
                    )
                ) {
                    Text(
                        text = "EXECUTAR PROVA BIOMECÂNICA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InspectionCriterionCard(
    label: String,
    statusText: String,
    isPassed: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isPassed) StatusActive.copy(alpha = 0.5f) else ObsidianBorder
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isPassed) StatusActive else GoldAccent
                )
            }
            Icon(
                imageVector = if (isPassed) Icons.Default.Check else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isPassed) StatusActive else TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PerformanceDimensionRow(label: String, score: Int, maxScore: Int) {
    val progress = (score.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Text(
                text = "$score",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(ObsidianDivider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TextPrimary)
            )
        }
    }
}
