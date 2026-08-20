package com.example.ui.evolution

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
import com.example.core.evolutionengine.catalog.ClassCatalog
import com.example.core.evolutionengine.explanation.ClassExplanation
import com.example.core.evolutionengine.explanation.ClassExplanationRegistryV1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassExplanationScreen(
    classId: String = ClassCatalog.CLASS_01,
    onNavigateBack: () -> Unit
) {
    var selectedClassId by remember { mutableStateOf(classId) }
    val explanation = remember(selectedClassId) {
        ClassExplanationRegistryV1.getExplanation(selectedClassId)
            ?: ClassExplanationRegistryV1.getExplanation(ClassCatalog.CLASS_01)!!
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Guia de Classes de Evolução",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "GOVERNANÇA CIENTÍFICA FORMAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00F5FF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_class_explanation")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
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
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E172A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge(
                        containerColor = Color(0xFF00F5FF).copy(alpha = 0.2f),
                        contentColor = Color(0xFF00F5FF)
                    ) {
                        Text(
                            text = "ORDEM ${explanation.order} DE 22",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = explanation.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = explanation.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // 12 Structured Explanation Fields
            ExplanationSection(
                title = "Por que o atleta está nesta classe?",
                content = explanation.whyInThisClass,
                icon = Icons.Default.Info
            )

            ExplanationSection(
                title = "Evidências Científicas Consideradas",
                items = explanation.evidencesConsidered,
                icon = Icons.Default.Science
            )

            ExplanationSection(
                title = "Critérios Cumpridos",
                items = explanation.defaultCriteriaSatisfied,
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF10B981)
            )

            ExplanationSection(
                title = "Critérios Pendentes para Próxima Classe",
                items = explanation.defaultCriteriaPending,
                icon = Icons.Default.HourglassEmpty,
                accentColor = Color(0xFFF59E0B)
            )

            ExplanationSection(
                title = "O que NÃO significa estar nesta classe",
                content = explanation.whatItDoesNotMean,
                icon = Icons.Default.Warning,
                accentColor = Color(0xFFEF4444)
            )

            ExplanationSection(
                title = "Próxima Etapa de Evolução",
                content = "${explanation.nextClassName ?: "Nenhum (Nível Máximo)"}\nRequisitos: ${explanation.progressionRequirements.joinToString("; ")}",
                icon = Icons.Default.TrendingUp,
                accentColor = Color(0xFF00F5FF)
            )

            // Evidence & Minimum Tenure Card
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
                    Column {
                        Text("Estado da Evidência", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
                        Text(explanation.evidenceState, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Tempo Mínimo", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
                        Text(
                            if (explanation.minimumTenureWeeks > 0) "${explanation.minimumTenureWeeks} semanas" else "Sem carência",
                            color = Color(0xFF00F5FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExplanationSection(
    title: String,
    content: String? = null,
    items: List<String> = emptyList(),
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color = Color(0xFF00F5FF)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (!content.isNullOrBlank()) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1)
                )
            }

            if (items.isNotEmpty()) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(accentColor)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}
