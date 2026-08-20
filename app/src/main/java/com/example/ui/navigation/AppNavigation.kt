package com.example.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.core.trainingengine.model.TrainingSession
import com.example.core.trainingengine.model.ValueState
import com.example.data.local.AppDatabase
import com.example.data.repository.PerformAIRepository
import com.example.service.CoreServices
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.exercise.ExerciseDetailScreen
import com.example.ui.exercise.ExerciseDetailViewModel
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.ProfileViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val appDatabase = remember { AppDatabase.getInstance(context) }
    val repository = remember { PerformAIRepository(appDatabase) }
    val coreServices = remember { CoreServices(repository) }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel {
                AuthViewModel(coreServices)
            }
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onContinueToProfile = {
                    navController.navigate(Screen.ProfileSetup.route)
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            val profileViewModel: ProfileViewModel = viewModel {
                ProfileViewModel(coreServices)
            }
            ProfileScreen(
                viewModel = profileViewModel,
                onProfileConfigured = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = viewModel {
                DashboardViewModel(coreServices, repository)
            }
            DashboardScreen(
                viewModel = dashboardViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("exercise_detail/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "EX-SQ-BW-001-V1"
            val exerciseViewModel: ExerciseDetailViewModel = viewModel {
                ExerciseDetailViewModel(exerciseId)
            }
            ExerciseDetailScreen(
                viewModel = exerciseViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartExecution = { id ->
                    navController.navigate("exercise_execution/$id")
                }
            )
        }

        composable("exercise_execution/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "EX-SQ-BW-001-V1"
            val userId = com.example.core.security.SecurityContext.getAuthenticatedUserId() ?: "ATHLETE-ANONYMOUS"
            val executionViewModel: com.example.ui.training.execution.ExerciseExecutionViewModel = viewModel {
                com.example.ui.training.execution.ExerciseExecutionViewModel(
                    repository = repository,
                    athleteId = userId
                )
            }
            com.example.ui.training.execution.ExerciseExecutionScreen(
                viewModel = executionViewModel,
                exerciseId = exerciseId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSessionCompleted = {
                    navController.navigate(Screen.TrainingHistory.route) {
                        popUpTo("exercise_execution/$exerciseId") { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TrainingHistory.route) {
            val userId = com.example.core.security.SecurityContext.getAuthenticatedUserId() ?: "ATHLETE-ANONYMOUS"
            val sessionEntities by repository.getTrainingSessionsForUserFlow(userId)
                .collectAsState(initial = emptyList())

            val domainSessions = remember(sessionEntities) {
                sessionEntities.map { entity ->
                    TrainingSession(
                        sessionId = entity.sessionId,
                        userId = entity.userId,
                        workoutId = entity.workoutId,
                        sessionName = entity.sessionName,
                        status = entity.status,
                        startedAt = entity.startedAt,
                        endedAt = entity.endedAt,
                        totalDurationSeconds = entity.totalDurationSeconds,
                        activeDurationSeconds = entity.activeDurationSeconds,
                        pausedDurationSeconds = entity.pausedDurationSeconds,
                        perceivedExertion = entity.perceivedExertionValue?.let { ValueState.Recorded(it) } ?: ValueState.NotSpecified,
                        notes = entity.notes,
                        completionRate = entity.completionRate,
                        totalVolumeKg = entity.totalVolumeKg,
                        totalReps = entity.totalReps,
                        exerciseLogs = emptyList(),
                        evidencePackageId = entity.evidencePackageId,
                        syncStatus = entity.syncStatus,
                        version = entity.version
                    )
                }
            }

            com.example.ui.training.history.TrainingHistoryScreen(
                sessions = domainSessions,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("class_explanation/{classId}") { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: com.example.core.evolutionengine.catalog.ClassCatalog.CLASS_01
            com.example.ui.evolution.ClassExplanationScreen(
                classId = classId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
