package com.example.ui.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.exerciseengine.media.model.*
import com.example.core.exerciseengine.model.*
import com.example.ui.motionavatar.engine.MotionAvatarEngine
import com.example.ui.motionavatar.ui.MotionAvatarViewport
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    viewModel: ExerciseDetailViewModel,
    onNavigateBack: () -> Unit = {},
    onStartExecution: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.exercise?.displayName ?: "Detalhe do Exercício",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("exercise_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Indicador de Conexão / Modo Offline
                    AssistChip(
                        onClick = {
                            val nextStatus = when (state.networkStatus) {
                                NetworkStatus.ONLINE -> NetworkStatus.OFFLINE
                                NetworkStatus.OFFLINE -> NetworkStatus.LOW_BANDWIDTH
                                NetworkStatus.LOW_BANDWIDTH -> NetworkStatus.ONLINE
                            }
                            viewModel.setNetworkStatus(nextStatus)
                        },
                        label = {
                            Text(
                                text = state.networkStatus.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (state.networkStatus) {
                                    NetworkStatus.ONLINE -> StatusActive
                                    NetworkStatus.OFFLINE -> StatusCritical
                                    NetworkStatus.LOW_BANDWIDTH -> StatusWarning
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (state.networkStatus) {
                                    NetworkStatus.ONLINE -> Icons.Default.Wifi
                                    NetworkStatus.OFFLINE -> Icons.Default.WifiOff
                                    NetworkStatus.LOW_BANDWIDTH -> Icons.Default.NetworkCheck
                                },
                                contentDescription = "Status de Rede",
                                modifier = Modifier.size(16.dp),
                                tint = when (state.networkStatus) {
                                    NetworkStatus.ONLINE -> StatusActive
                                    NetworkStatus.OFFLINE -> StatusCritical
                                    NetworkStatus.LOW_BANDWIDTH -> StatusWarning
                                }
                            )
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("network_status_chip")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            state.exercise?.let { ex ->
                Surface(
                    color = ObsidianSurface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = { onStartExecution(ex.exerciseId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_start_exercise_execution"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPrimary,
                                contentColor = Color(0xFF0A0E17)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INICIAR EXECUÇÃO DO EXERCÍCIO",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.exercise == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "Exercício não encontrado.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val exercise = state.exercise!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // ----------------------------------------------------
                // 1. DEMONSTRAÇÃO VISUAL / VIEWPORT
                // ----------------------------------------------------
                VisualDemonstrationViewport(
                    state = state,
                    motionAvatarEngine = viewModel.motionAvatarEngine,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onSelectAvatar = { viewModel.setAvatar(it) },
                    onSelectAngle = { viewModel.setCameraAngle(it) },
                    onToggleFullscreen = { viewModel.toggleFullscreen() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // ----------------------------------------------------
                    // 2. CABEÇALHO / NOME DO EXERCÍCIO
                    // ----------------------------------------------------
                    Text(
                        text = exercise.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = exercise.canonicalName + " • " + exercise.version,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badges de Classificação
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("badge_category")
                        ) {
                            Text(
                                text = exercise.category.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("badge_difficulty")
                        ) {
                            Text(
                                text = exercise.difficulty.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ObsidianSurfaceElevated,
                            modifier = Modifier.testTag("badge_pattern")
                        ) {
                            Text(
                                text = exercise.movementPattern.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ----------------------------------------------------
                    // 3. OBJETIVO DO EXERCÍCIO
                    // ----------------------------------------------------
                    SectionHeader(title = "OBJETIVO")
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (exercise.trainingGoals.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            exercise.trainingGoals.forEach { goal ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
                                ) {
                                    Text(
                                        text = goal.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ----------------------------------------------------
                    // 4. COMO EXECUTAR (INSTRUÇÕES)
                    // ----------------------------------------------------
                    SectionHeader(title = "COMO EXECUTAR")

                    if (exercise.instructions.setup.isNotEmpty()) {
                        Text(
                            text = "Preparação (Setup):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        exercise.instructions.setup.forEachIndexed { index, step ->
                            InstructionStepItem(stepNumber = index + 1, text = step)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (exercise.instructions.execution.isNotEmpty()) {
                        Text(
                            text = "Execução:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                        exercise.instructions.execution.forEachIndexed { index, step ->
                            InstructionStepItem(stepNumber = index + 1, text = step)
                        }
                    }

                    exercise.instructions.breathing?.let { breathing ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ObsidianSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = "Respiração",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = breathing,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ----------------------------------------------------
                    // 5. FASES DO MOVIMENTO
                    // ----------------------------------------------------
                    if (exercise.movementPhases.isNotEmpty()) {
                        SectionHeader(title = "FASES DO MOVIMENTO")
                        exercise.movementPhases.forEach { phase ->
                            MovementPhaseCard(phase = phase)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // ----------------------------------------------------
                    // 6. PONTOS DE ATENÇÃO
                    // ----------------------------------------------------
                    if (exercise.instructions.cuePoints.isNotEmpty() || exercise.safetyNotes.isNotEmpty()) {
                        SectionHeader(title = "PONTOS DE ATENÇÃO")
                        exercise.instructions.cuePoints.forEach { cue ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(text = "• ", color = GoldAccent, fontWeight = FontWeight.Bold)
                                Text(text = cue, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                        exercise.safetyNotes.forEach { note ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Segurança",
                                    tint = StatusWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = note, style = MaterialTheme.typography.bodySmall, color = StatusWarning)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // ----------------------------------------------------
                    // 7. ERROS COMUNS
                    // ----------------------------------------------------
                    if (exercise.commonErrors.isNotEmpty()) {
                        SectionHeader(title = "ERROS COMUNS")
                        exercise.commonErrors.forEach { error ->
                            CommonErrorCard(error = error)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ----------------------------------------------------
                    // 8 & 9. PROGRESSÕES E REGRESSÕES
                    // ----------------------------------------------------
                    val path = state.progressionPath
                    if (path != null && (path.progressions.isNotEmpty() || path.regressions.isNotEmpty())) {
                        if (path.progressions.isNotEmpty()) {
                            SectionHeader(title = "PROGRESSÕES (AVANÇAR COMPLEXIDADE)")
                            path.progressions.forEach { prog ->
                                ExerciseProgressionItem(
                                    exercise = prog,
                                    onClick = { viewModel.loadExercise(prog.exerciseId) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (path.regressions.isNotEmpty()) {
                            SectionHeader(title = "REGRESSÕES (ALTERNATIVAS E FUNDAMENTOS)")
                            path.regressions.forEach { reg ->
                                ExerciseProgressionItem(
                                    exercise = reg,
                                    onClick = { viewModel.loadExercise(reg.exerciseId) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // ----------------------------------------------------
                    // 10. BIOMECÂNICA
                    // ----------------------------------------------------
                    SectionHeader(title = "BIOMECÂNICA")
                    BiomechanicalSection(profile = exercise.biomechanicalProfile)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun VisualDemonstrationViewport(
    state: ExerciseDetailUiState,
    motionAvatarEngine: MotionAvatarEngine? = null,
    onTogglePlay: () -> Unit,
    onSelectAvatar: (AvatarCharacterId) -> Unit,
    onSelectAngle: (CameraPreset) -> Unit,
    onToggleFullscreen: () -> Unit
) {
    val exerciseId = state.exercise?.exerciseId ?: ""
    val hasMotionAvatar = motionAvatarEngine != null && motionAvatarEngine.hasAnimationFor(exerciseId)

    if (hasMotionAvatar) {
        MotionAvatarViewport(
            engine = motionAvatarEngine!!,
            onSelectAvatar = onSelectAvatar
        )
        return
    }

    val resolved = state.resolvedMedia
    val isMediaNone = resolved == null || resolved.resolvedMediaType == ExerciseMediaType.NONE

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .testTag("demonstration_viewport"),
        color = PitchBlack,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background / Demo Layer
            if (isMediaNone) {
                // Modo Visual Técnico / Fallback sem quebrar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "Demonstração Visual",
                        tint = CyanPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DEMONSTRAÇÃO VISUAL OFICIAL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (state.networkStatus == NetworkStatus.OFFLINE)
                            "Modo Offline • Visualização estruturada por instruções técnicas"
                        else
                            "Asset 3D preparado • Aguardando sincronização com avatar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Conteúdo de mídia existente
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Demonstração: ${resolved?.resolvedMediaType?.name}",
                        color = GoldAccent,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Barra Superior de Controles (Avatar + Câmera)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seletor de Avatar Oficial (MALE / FEMALE)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurface.copy(alpha = 0.85f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AvatarChip(
                        label = "M",
                        isSelected = state.selectedAvatar == AvatarCharacterId.MALE_AVATAR_V1,
                        onClick = { onSelectAvatar(AvatarCharacterId.MALE_AVATAR_V1) },
                        testTag = "avatar_male_button"
                    )
                    AvatarChip(
                        label = "F",
                        isSelected = state.selectedAvatar == AvatarCharacterId.FEMALE_AVATAR_V1,
                        onClick = { onSelectAvatar(AvatarCharacterId.FEMALE_AVATAR_V1) },
                        testTag = "avatar_female_button"
                    )
                }

                // Seletor de Ângulo de Câmera
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurface.copy(alpha = 0.85f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.availableAngles.forEach { angle ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (state.selectedAngle == angle) CyanPrimary else Color.Transparent,
                            modifier = Modifier
                                .clickable { onSelectAngle(angle) }
                                .testTag("camera_angle_${angle.name.lowercase()}")
                        ) {
                            Text(
                                text = when (angle) {
                                    CameraPreset.FRONT -> "Frente"
                                    CameraPreset.SIDE -> "Lado"
                                    CameraPreset.THREE_QUARTER -> "3/4"
                                    CameraPreset.BACK -> "Costas"
                                    CameraPreset.TOP -> "Topo"
                                    else -> angle.name
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.selectedAngle == angle) PitchBlack else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Barra Inferior de Controles (Play/Pause + Fullscreen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ObsidianSurfaceElevated)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pausar" else "Reproduzir",
                        tint = GoldAccent
                    )
                }

                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ObsidianSurfaceElevated)
                        .testTag("fullscreen_button")
                ) {
                    Icon(
                        imageVector = if (state.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "Tela Cheia",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) GoldAccent else Color.Transparent,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) PitchBlack else TextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = GoldAccent,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun InstructionStepItem(stepNumber: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun MovementPhaseCard(phase: MovementPhaseDefinition) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ObsidianSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = ObsidianSurfaceElevated,
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    text = phase.phase.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = phase.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                if (phase.jointFocus.isNotEmpty()) {
                    Text(
                        text = "Foco: ${phase.jointFocus.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CommonErrorCard(error: CommonError) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ObsidianSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StatusCritical.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Aviso de Erro",
                    tint = StatusCritical,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = error.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Correção: ${error.correction}",
                style = MaterialTheme.typography.bodySmall,
                color = StatusActive
            )
        }
    }
}

@Composable
private fun ExerciseProgressionItem(
    exercise: ExerciseDefinition,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ObsidianSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = exercise.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${exercise.difficulty.name} • ${exercise.category.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Visualizar",
                tint = GoldAccent
            )
        }
    }
}

@Composable
private fun BiomechanicalSection(profile: BiomechanicalProfile) {
    val hasValidData = profile.motionPattern != null || profile.jointTargets.isNotEmpty() || profile.expectedROM != null || profile.expectedVelocity != null

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ObsidianSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!hasValidData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Análise biomecânica ainda não disponível.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(modifier = Modifier.padding(12.dp)) {
                profile.motionPattern?.let {
                    Text(
                        text = "Padrão Biomecânico: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
                if (profile.jointTargets.isNotEmpty()) {
                    Text(
                        text = "Articulações: ${profile.jointTargets.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                profile.expectedROM?.let {
                    Text(
                        text = "Amplitude Alvo (ROM): $it°",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanPrimary
                    )
                }
                profile.expectedVelocity?.let {
                    Text(
                        text = "Velocidade Alvo: $it m/s",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanPrimary
                    )
                }
            }
        }
    }
}
