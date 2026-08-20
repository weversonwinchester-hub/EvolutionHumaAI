package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.AppResult
import com.example.core.model.ProfileStatus
import com.example.core.model.User
import com.example.service.CoreServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val emailInput: String = "atleta@evolutionhuman.ai",
    val passwordInput: String = "evolution123",
    val fullNameInput: String = "Alex Vance",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authenticatedUser: User? = null,
    val nextDestination: String? = null
)

class AuthViewModel(
    private val coreServices: CoreServices
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isLoginMode = !_uiState.value.isLoginMode,
            errorMessage = null
        )
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(emailInput = email, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(passwordInput = password, errorMessage = null)
    }

    fun onFullNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(fullNameInput = name, errorMessage = null)
    }

    fun submit() {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            if (state.isLoginMode) {
                when (val result = coreServices.login(state.emailInput, state.passwordInput)) {
                    is AppResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            authenticatedUser = result.data,
                            nextDestination = "dashboard"
                        )
                    }
                    is AppResult.Failure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            } else {
                when (val result = coreServices.registerUser(
                    email = state.emailInput,
                    passwordRaw = state.passwordInput,
                    fullName = state.fullNameInput
                )) {
                    is AppResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            authenticatedUser = result.data,
                            nextDestination = "onboarding"
                        )
                    }
                    is AppResult.Failure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    fun clearNavigation() {
        _uiState.value = _uiState.value.copy(nextDestination = null)
    }
}
