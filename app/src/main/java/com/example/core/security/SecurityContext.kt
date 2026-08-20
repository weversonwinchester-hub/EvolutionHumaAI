package com.example.core.security

import com.example.core.error.AppError
import com.example.core.error.AppResult
import com.example.core.model.Permission
import com.example.core.model.RolePermissions
import com.example.core.model.User
import com.example.core.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SecurityContext {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    fun getAuthenticatedUserId(): String? = _currentUser.value?.id

    fun getCurrentRole(): UserRole = _currentUser.value?.role ?: UserRole.USER

    fun requirePermission(permission: Permission): AppResult<Unit> {
        val user = _currentUser.value ?: return AppResult.Failure(
            AppError.AuthenticationError("Operação bloqueada: Sessão não autenticada.")
        )
        if (!RolePermissions.hasPermission(user.role, permission)) {
            return AppResult.Failure(
                AppError.AuthorizationError("Acesso negado: Perfil ${user.role} não possui a permissão $permission.")
            )
        }
        return AppResult.Success(Unit)
    }

    fun isAuthorized(permission: Permission): Boolean {
        val role = _currentUser.value?.role ?: return false
        return RolePermissions.hasPermission(role, permission)
    }
}
