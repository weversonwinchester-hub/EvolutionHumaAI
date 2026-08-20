package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Onboarding : Screen("onboarding")
    data object ProfileSetup : Screen("profile_setup")
    data object Dashboard : Screen("dashboard")
    data object AuditLogs : Screen("audit_logs")
    data object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_detail/$exerciseId"
    }
    data object ExerciseExecution : Screen("exercise_execution/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_execution/$exerciseId"
    }
    data object TrainingHistory : Screen("training_history")
    data object ClassExplanation : Screen("class_explanation/{classId}") {
        fun createRoute(classId: String) = "class_explanation/$classId"
    }
}
