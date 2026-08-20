package com.example.core.engine

import com.example.core.error.AppResult
import com.example.core.model.Assessment
import com.example.core.model.Evidence
import com.example.core.model.EvolutionState
import com.example.core.model.Measurement
import com.example.core.model.Mission
import com.example.core.model.PerformanceState
import com.example.core.model.Trial

object CoreEngineVersion {
    const val CURRENT_ENGINE_VERSION = "1.0.0-foundation"
    const val MINIMUM_SUPPORTED_VERSION = "1.0.0"

    fun isCompatible(version: String): Boolean {
        return version.startsWith("1.")
    }
}

/**
 * Score Engine: Responsável futuro pelo cálculo de métricas agregadas e scores de prontidão.
 * Na fase FOUNDATION, não há cálculos fictícios.
 */
interface ScoreEngineContract {
    fun calculateScore(measurements: List<Measurement>): AppResult<PerformanceState>
}

/**
 * Evidence Engine: Responsável futuro pela verificação de integridade e evidências de testes.
 */
interface EvidenceEngineContract {
    fun verifyEvidence(evidence: Evidence): AppResult<Boolean>
}

/**
 * Evolution Engine: Responsável futuro pelas promoções oficiais de classe e evolução de nível.
 * O Frontend e a IA NUNCA têm permissão para promover classes.
 */
interface EvolutionEngineContract {
    fun evaluateProgression(
        currentState: EvolutionState,
        approvedEvidence: List<Evidence>,
        completedAssessments: List<Assessment>
    ): AppResult<EvolutionState>
}

/**
 * Mission Engine: Responsável futuro pela geração e validação de missões.
 */
interface MissionEngineContract {
    fun getAvailableMissions(userId: String, evolutionState: EvolutionState): AppResult<List<Mission>>
}

/**
 * Trial Engine: Responsável futuro pelos rituais e testes probatórios de ascensão de classe.
 */
interface TrialEngineContract {
    fun evaluateTrial(trial: Trial, evidenceList: List<Evidence>): AppResult<Trial>
}
