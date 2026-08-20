package com.example.ui.training.execution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.motionavatar.ui.MotionAvatarViewport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseExecutionScreen(
    viewModel: ExerciseExecutionViewModel,
    exerciseId: String,
    onNavigateBack: () -> Unit,
    onSessionCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(exerciseId) {
        viewModel.initializeExercise(exerciseId)
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onSessionCompleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "MODALIDADE: ${uiState.exerciseCategory}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00F5FF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_execution")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.togglePauseSession() },
                        modifier = Modifier.testTag("btn_pause_session")
                    ) {
                        Icon(
                            imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (uiState.isPaused) "Retomar Sessão" else "Pausar Sessão",
                            tint = Color(0xFF00F5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B101D)
                )
            )
        },
        containerColor = Color(0xFF070A13)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. MOTION AVATAR TIER 1 VIEWPORT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("motion_avatar_execution_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424))
            ) {
                MotionAvatarViewport(
                    engine = viewModel.motionAvatarEngine,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. REST TIMER BANNER (IF RESTING)
            if (uiState.isResting) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Descanso",
                                tint = Color(0xFF00F5FF)
                            )
                            Text(
                                text = "Descanso: ${uiState.restRemainingSeconds}s",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.skipRest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.testTag("btn_skip_rest")
                        ) {
                            Text("Pular", color = Color.White)
                        }
                    }
                }
            }

            // 3. SET EXECUTION & LOGGING PANEL
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E172A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SÉRIE ${uiState.currentSetNumber} DE ${uiState.targetSets}",
                            color = Color(0xFF00F5FF),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Volume Total: ${uiState.totalVolumeKg} kg",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HorizontalDivider(color = Color(0xFF1E293B))

                    // Reps & Load Adjusters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Reps
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Repetições", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { viewModel.updateReps(uiState.loggedReps - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuir repetições", tint = Color.White)
                                }
                                Text(
                                    text = "${uiState.loggedReps}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                IconButton(onClick = { viewModel.updateReps(uiState.loggedReps + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar repetições", tint = Color.White)
                                }
                            }
                        }

                        // Load (kg)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Carga (kg)", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { viewModel.updateLoad(uiState.loggedLoadKg - 2.5) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuir carga", tint = Color.White)
                                }
                                Text(
                                    text = "${uiState.loggedLoadKg}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                IconButton(onClick = { viewModel.updateLoad(uiState.loggedLoadKg + 2.5) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar carga", tint = Color.White)
                                }
                            }
                        }
                    }

                    // RPE Rating
                    Column {
                        Text("Percepção de Esforço (RPE): ${uiState.selectedRpe}", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = uiState.selectedRpe.toFloat(),
                            onValueChange = { viewModel.updateRpe(it.toDouble()) },
                            valueRange = 5f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00F5FF),
                                activeTrackColor = Color(0xFF00F5FF),
                                inactiveTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.testTag("slider_rpe")
                        )
                    }

                    // Log Set Button
                    Button(
                        onClick = { viewModel.completeCurrentSet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_log_set"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF070A13))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REGISTRAR SÉRIE ${uiState.currentSetNumber}",
                            color = Color(0xFF070A13),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. SESSION FINALIZE / ABANDON CONTROLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.abandonSession() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_abandon_session"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Abandonar")
                }

                Button(
                    onClick = { viewModel.finishSession() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_finish_session"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Finalizar Treino", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
