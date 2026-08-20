package com.example.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.AppResult
import com.example.core.model.Profile
import com.example.core.security.SecurityContext
import com.example.service.CoreServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val fullName: String = "",
    val nickname: String = "",
    val dateOfBirth: String = "1995-06-15",
    val gender: String = "Masculino",
    val heightCmInput: String = "178.0",
    val weightKgInput: String = "75.5",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false
)

class ProfileViewModel(
    private val coreServices: CoreServices
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val user = SecurityContext.currentUser.value
        if (user != null) {
            _uiState.value = _uiState.value.copy(fullName = user.email.substringBefore("@").replace(".", " ").capitalize())
        }
    }

    fun onFullNameChange(value: String) { _uiState.value = _uiState.value.copy(fullName = value, errorMessage = null) }
    fun onNicknameChange(value: String) { _uiState.value = _uiState.value.copy(nickname = value, errorMessage = null) }
    fun onDateOfBirthChange(value: String) { _uiState.value = _uiState.value.copy(dateOfBirth = value, errorMessage = null) }
    fun onGenderChange(value: String) { _uiState.value = _uiState.value.copy(gender = value, errorMessage = null) }
    fun onHeightChange(value: String) { _uiState.value = _uiState.value.copy(heightCmInput = value, errorMessage = null) }
    fun onWeightChange(value: String) { _uiState.value = _uiState.value.copy(weightKgInput = value, errorMessage = null) }

    fun saveProfile() {
        val state = _uiState.value
        val height = state.heightCmInput.toDoubleOrNull()
        val weight = state.weightKgInput.toDoubleOrNull()

        if (height == null) {
            _uiState.value = state.copy(errorMessage = "Informe uma altura válida em centímetros.")
            return
        }
        if (weight == null) {
            _uiState.value = state.copy(errorMessage = "Informe um peso válido em quilogramas.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = coreServices.updateProfileBiometrics(
                fullName = state.fullName,
                nickname = state.nickname,
                dateOfBirth = state.dateOfBirth,
                gender = state.gender,
                heightCm = height,
                weightKg = weight
            )

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSavedSuccess = true
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
