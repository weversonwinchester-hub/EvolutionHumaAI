package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.AppResult
import com.example.core.evidenceconsistency.model.EvolutionEvidencePackage
import com.example.core.evolutionengine.model.EvolutionSnapshot
import com.example.core.model.Assessment
import com.example.core.model.AssessmentStatus
import com.example.core.model.AuditLog
import com.example.core.model.EvolutionState
import com.example.core.model.INITIAL_EVOLUTION_CLASS
import com.example.core.model.Profile
import com.example.core.model.ProfileStatus
import com.example.core.model.User
import com.example.core.progressionengine.model.EvolutionProgressionState
import com.example.core.progressionengine.model.PromotionCandidate
import com.example.core.security.SecurityContext
import com.example.core.trialengine.model.TrialSnapshot
import com.example.data.repository.PerformAIRepository
import com.example.service.CoreServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val user: User? = null,
    val profile: Profile? = null,
    val evolutionState: EvolutionState? = null,
    val evolutionSnapshot: EvolutionSnapshot? = null,
    val evidencePackage: EvolutionEvidencePackage? = null,
    val progressionState: EvolutionProgressionState? = null,
    val promotionCandidate: PromotionCandidate? = null,
    val trialSnapshot: TrialSnapshot? = null,
    val initialAssessment: Assessment? = null,
    val latestScoreSnapshot: com.example.core.scoreengine.model.ScoreSnapshot? = null,
    val isOfficialScore: Boolean = false,
    val recentAuditLogs: List<AuditLog> = emptyList(),
    val isInitiatingAssessment: Boolean = false,
    val assessmentSuccessMessage: String? = null,
    val errorMessage: String? = null,
    val activeTab: Int = 0 // 0: My Evolution, 1: Evolution Ladder (22 Classes), 2: Biomechanics & Motion, 3: Hub & Core
)

class DashboardViewModel(
    private val coreServices: CoreServices,
    private val repository: PerformAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(activeTab = index)
    }

    fun loadData() {
        val user = SecurityContext.currentUser.value
        _uiState.value = _uiState.value.copy(user = user)

        if (user != null) {
            viewModelScope.launch {
                // Observe Profile
                repository.getProfileFlow(user.id).collect { profile ->
                    _uiState.value = _uiState.value.copy(profile = profile)
                }
            }

            viewModelScope.launch {
                // Observe Evolution State (MANDATE: Strictly initial "Corpo Adormecido")
                repository.getEvolutionStateFlow(user.id).collect { evolutionState ->
                    _uiState.value = _uiState.value.copy(evolutionState = evolutionState)
                }
            }

            viewModelScope.launch {
                // Observe Initial Assessment
                repository.getInitialAssessmentFlow(user.id).collect { assessment ->
                    _uiState.value = _uiState.value.copy(initialAssessment = assessment)
                }
            }

            viewModelScope.launch {
                // Observe Recent Score Snapshots
                repository.getLatestSnapshotFlow(user.id).collect { snapshotEntity ->
                    if (snapshotEntity != null) {
                        _uiState.value = _uiState.value.copy(
                            isOfficialScore = !snapshotEntity.isMock && snapshotEntity.calculationStatus != com.example.core.scoreengine.model.CalculationStatus.MOCK_DEMO
                        )
                    }
                }
            }

            viewModelScope.launch {
                // Observe Audit Logs
                repository.getRecentAuditLogsFlow(30).collect { logs ->
                    _uiState.value = _uiState.value.copy(recentAuditLogs = logs)
                }
            }

            viewModelScope.launch {
                // Observe Progression State
                repository.observeLatestProgressionState(user.id).collect { stateEntity ->
                    if (stateEntity != null) {
                        _uiState.value = _uiState.value.copy(
                            progressionState = EvolutionProgressionState(
                                id = stateEntity.id,
                                userId = stateEntity.userId,
                                currentClassId = stateEntity.currentClassId,
                                currentClassSince = stateEntity.currentClassSince,
                                highestEligibleClassId = stateEntity.highestEligibleClassId,
                                nextTargetClassId = stateEntity.nextTargetClassId,
                                progressionStatus = stateEntity.progressionStatus,
                                progressionPhase = stateEntity.progressionPhase,
                                lastAssessmentAt = stateEntity.lastAssessmentAt,
                                methodologyVersion = stateEntity.methodologyVersion,
                                coreVersion = stateEntity.coreVersion,
                                isMock = stateEntity.isMock,
                                simulationMode = stateEntity.simulationMode
                            )
                        )
                    }
                }
            }
        }
    }

    fun startInitialAssessment() {
        _uiState.value = _uiState.value.copy(isInitiatingAssessment = true, errorMessage = null, assessmentSuccessMessage = null)
        viewModelScope.launch {
            when (val result = coreServices.initiateInitialAssessment()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isInitiatingAssessment = false,
                        assessmentSuccessMessage = "Protocolo de Avaliação Inicial registrado no Core com sucesso. Baseline em processo."
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isInitiatingAssessment = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun askAiConsultation(query: String) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            coreServices.aiGateway.analyzePerformanceContext(user.id, query)
        }
    }

    fun testUnauthorizedMutationAttempt() {
        viewModelScope.launch {
            // Demonstra a regra: Frontend não pode determinar promoção ou classe evolutiva
            when (val result = coreServices.validateSecurityStateMutationAttempt("Mestre Transcendente")) {
                is AppResult.Success -> {}
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun logout() {
        coreServices.logout()
    }
}
