package com.example.data.repository

import com.example.core.model.AIInteraction
import com.example.core.model.Assessment
import com.example.core.model.AssessmentStatus
import com.example.core.model.AssessmentType
import com.example.core.model.AuditLog
import com.example.core.model.AuditSeverity
import com.example.core.model.Evidence
import com.example.core.model.EvolutionState
import com.example.core.model.Goal
import com.example.core.model.INITIAL_EVOLUTION_CLASS
import com.example.core.model.Measurement
import com.example.core.model.Mission
import com.example.core.model.PerformanceState
import com.example.core.model.Profile
import com.example.core.model.ProfileStatus
import com.example.core.model.Trial
import com.example.core.model.User
import com.example.core.model.UserRole
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AIInteractionEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.EvidenceEntity
import com.example.data.local.entity.EvolutionStateEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MeasurementEntity
import com.example.data.local.entity.MissionEntity
import com.example.data.local.entity.PerformanceStateEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.TrialEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PerformAIRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val profileDao = database.profileDao()
    private val goalDao = database.goalDao()
    private val assessmentDao = database.assessmentDao()
    private val evolutionDao = database.evolutionDao()
    private val missionTrialDao = database.missionTrialDao()
    private val auditDao = database.auditDao()
    private val aiGatewayDao = database.aiGatewayDao()
    private val evolutionEvidencePackageDao = database.evolutionEvidencePackageDao()
    private val evolutionSnapshotDao = database.evolutionSnapshotDao()
    private val trialSessionDao = database.trialSessionDao()
    private val trialAttemptDao = database.trialAttemptDao()
    private val trialSnapshotDao = database.trialSnapshotDao()
    private val progressionStateDao = database.progressionStateDao()
    private val promotionCandidateDao = database.promotionCandidateDao()
    private val progressionSnapshotDao = database.progressionSnapshotDao()
    private val evolutionHistoryDao = database.evolutionHistoryDao()
    private val progressionAnomalyDao = database.progressionAnomalyDao()
    private val scientificMethodologyDao = database.scientificMethodologyDao()
    private val scientificProtocolDao = database.scientificProtocolDao()
    private val measurementInstrumentDao = database.measurementInstrumentDao()
    private val qualityGateEvaluationDao = database.qualityGateEvaluationDao()
    private val protocolDeviationDao = database.protocolDeviationDao()
    private val populationReferenceDao = database.populationReferenceDao()
    private val scientificAuditLogDao = database.scientificAuditLogDao()
    private val workoutDao = database.workoutDao()
    private val trainingSessionDao = database.trainingSessionDao()
    private val athleteBaselineDao = database.athleteBaselineDao()
    private val athleteGoalDao = database.athleteGoalDao()
    private val trainingSyncQueueDao = database.trainingSyncQueueDao()

    // --- SCIENTIFIC METHODOLOGY & PROTOCOL REGISTRY V1 REPOSITORY ---
    suspend fun insertScientificMethodology(methodology: com.example.data.local.entity.ScientificMethodologyEntity) =
        scientificMethodologyDao.insertMethodology(methodology)

    suspend fun getScientificMethodologyById(id: String) =
        scientificMethodologyDao.getMethodologyById(id)

    suspend fun getAllScientificMethodologies() =
        scientificMethodologyDao.getAllMethodologies()

    suspend fun insertScientificProtocol(protocol: com.example.data.local.entity.ScientificProtocolEntity) =
        scientificProtocolDao.insertProtocol(protocol)

    suspend fun getScientificProtocolById(id: String) =
        scientificProtocolDao.getProtocolById(id)

    suspend fun getAllScientificProtocols() =
        scientificProtocolDao.getAllProtocols()

    suspend fun insertMeasurementInstrument(instrument: com.example.data.local.entity.MeasurementInstrumentEntity) =
        measurementInstrumentDao.insertInstrument(instrument)

    suspend fun getMeasurementInstrumentById(id: String) =
        measurementInstrumentDao.getInstrumentById(id)

    suspend fun insertQualityGateEvaluation(evaluation: com.example.data.local.entity.QualityGateEvaluationEntity) =
        qualityGateEvaluationDao.insertEvaluation(evaluation)

    suspend fun getQualityGateEvaluationsForMeasurement(measurementId: String) =
        qualityGateEvaluationDao.getEvaluationsForMeasurement(measurementId)

    fun getAllQualityGateEvaluationsFlow() =
        qualityGateEvaluationDao.getAllEvaluationsFlow()

    suspend fun insertProtocolDeviation(deviation: com.example.data.local.entity.ProtocolDeviationEntity) =
        protocolDeviationDao.insertDeviation(deviation)

    suspend fun getProtocolDeviationsForMeasurement(measurementId: String) =
        protocolDeviationDao.getDeviationsForMeasurement(measurementId)

    suspend fun insertScientificAudit(audit: com.example.data.local.entity.ScientificAuditLogEntity) =
        scientificAuditLogDao.insertAudit(audit)

    suspend fun getAllScientificAudits() =
        scientificAuditLogDao.getAllAudits()

    suspend fun getScientificSecurityViolations() =
        scientificAuditLogDao.getSecurityViolations()

    // --- PROGRESSION ENGINE V1 REPOSITORY ---
    suspend fun insertProgressionState(state: com.example.data.local.entity.EvolutionProgressionStateEntity) =
        progressionStateDao.insertState(state)

    suspend fun getLatestProgressionState(userId: String) =
        progressionStateDao.getLatestState(userId)

    fun observeLatestProgressionState(userId: String) =
        progressionStateDao.observeLatestState(userId)

    suspend fun insertPromotionCandidate(candidate: com.example.data.local.entity.PromotionCandidateEntity) =
        promotionCandidateDao.insertCandidate(candidate)

    suspend fun getPromotionCandidatesForUser(userId: String) =
        promotionCandidateDao.getCandidatesForUser(userId)

    suspend fun getLatestPromotionCandidate(userId: String) =
        promotionCandidateDao.getLatestCandidate(userId)

    suspend fun insertProgressionSnapshot(snapshot: com.example.data.local.entity.ProgressionAssessmentSnapshotEntity) =
        progressionSnapshotDao.insertSnapshot(snapshot)

    suspend fun getProgressionSnapshotsForUser(userId: String) =
        progressionSnapshotDao.getSnapshotsForUser(userId)

    suspend fun getProgressionSnapshotById(id: String) =
        progressionSnapshotDao.getSnapshotById(id)

    suspend fun insertEvolutionHistoryEntry(entry: com.example.data.local.entity.EvolutionHistoryEntity) =
        evolutionHistoryDao.insertHistoryEntry(entry)

    suspend fun getEvolutionHistoryForUser(userId: String) =
        evolutionHistoryDao.getHistoryForUser(userId)

    fun observeEvolutionHistoryForUser(userId: String) =
        evolutionHistoryDao.observeHistoryForUser(userId)

    suspend fun insertProgressionAnomaly(anomaly: com.example.data.local.entity.ProgressionAnomalyEntity) =
        progressionAnomalyDao.insertAnomaly(anomaly)

    suspend fun getProgressionAnomaliesForUser(userId: String) =
        progressionAnomalyDao.getAnomaliesForUser(userId)

    // --- TRIAL ENGINE V1 REPOSITORY ---
    suspend fun insertTrialSession(session: com.example.data.local.entity.TrialSessionEntity) =
        trialSessionDao.insertTrialSession(session)

    suspend fun updateTrialSession(session: com.example.data.local.entity.TrialSessionEntity) =
        trialSessionDao.updateTrialSession(session)

    suspend fun getTrialSessionById(id: String) =
        trialSessionDao.getSessionById(id)

    fun getTrialSessionsByUserFlow(userId: String) =
        trialSessionDao.getSessionsByUserFlow(userId)

    suspend fun getActiveTrialSession(userId: String) =
        trialSessionDao.getActiveSession(userId)

    suspend fun insertTrialAttempt(attempt: com.example.data.local.entity.TrialAttemptEntity) =
        trialAttemptDao.insertTrialAttempt(attempt)

    suspend fun getTrialAttemptsBySessionId(sessionId: String) =
        trialAttemptDao.getAttemptsBySessionId(sessionId)

    fun getTrialAttemptsBySessionIdFlow(sessionId: String) =
        trialAttemptDao.getAttemptsBySessionIdFlow(sessionId)

    suspend fun insertTrialSnapshot(snapshot: com.example.data.local.entity.TrialSnapshotEntity) =
        trialSnapshotDao.insertTrialSnapshot(snapshot)

    fun getTrialSnapshotsByUserFlow(userId: String) =
        trialSnapshotDao.getSnapshotsByUserFlow(userId)

    suspend fun getTrialSnapshotBySessionId(sessionId: String) =
        trialSnapshotDao.getSnapshotBySessionId(sessionId)

    suspend fun getQualifiedOfficialTrialSnapshots(userId: String) =
        trialSnapshotDao.getQualifiedOfficialSnapshots(userId)

    // --- EVOLUTION SNAPSHOT (V1) ---
    suspend fun insertEvolutionSnapshot(snapshot: com.example.data.local.entity.EvolutionSnapshotEntity) =
        evolutionSnapshotDao.insertEvolutionSnapshot(snapshot)

    fun getEvolutionSnapshotsFlow(userId: String) =
        evolutionSnapshotDao.getEvolutionSnapshotsFlow(userId)

    fun getLatestOfficialEvolutionSnapshotFlow(userId: String) =
        evolutionSnapshotDao.getLatestOfficialEvolutionSnapshotFlow(userId)

    suspend fun getLatestOfficialEvolutionSnapshot(userId: String) =
        evolutionSnapshotDao.getLatestOfficialEvolutionSnapshot(userId)

    suspend fun getLatestEvolutionSnapshot(userId: String) =
        evolutionSnapshotDao.getLatestEvolutionSnapshot(userId)

    suspend fun getLatestSnapshotForClass(userId: String, classId: String) =
        evolutionSnapshotDao.getLatestSnapshotForClass(userId, classId)

    suspend fun getEvolutionSnapshotById(id: String) =
        evolutionSnapshotDao.getEvolutionSnapshotById(id)

    // --- EVOLUTION EVIDENCE PACKAGE (V1) ---
    suspend fun insertEvolutionEvidencePackage(pkg: com.example.data.local.entity.EvolutionEvidencePackageEntity) =
        evolutionEvidencePackageDao.insertPackage(pkg)

    fun getEvidencePackagesFlow(userId: String): Flow<List<com.example.data.local.entity.EvolutionEvidencePackageEntity>> =
        evolutionEvidencePackageDao.getPackagesByUserIdFlow(userId)

    suspend fun getEvidencePackages(userId: String): List<com.example.data.local.entity.EvolutionEvidencePackageEntity> =
        evolutionEvidencePackageDao.getPackagesByUserId(userId)

    fun getLatestOfficialPackageFlow(userId: String): Flow<com.example.data.local.entity.EvolutionEvidencePackageEntity?> =
        evolutionEvidencePackageDao.getLatestOfficialPackageFlow(userId)

    suspend fun getLatestOfficialPackage(userId: String): com.example.data.local.entity.EvolutionEvidencePackageEntity? =
        evolutionEvidencePackageDao.getLatestOfficialPackage(userId)

    suspend fun getEvidencePackageById(id: String): com.example.data.local.entity.EvolutionEvidencePackageEntity? =
        evolutionEvidencePackageDao.getPackageById(id)

    // --- USER ---
    suspend fun getUserById(id: String): User? = userDao.getUserById(id)?.toDomain()
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)?.toDomain()
    suspend fun insertUser(user: User) = userDao.insertUser(user.toEntity())
    suspend fun updateUser(user: User) = userDao.updateUser(user.toEntity())
    suspend fun getUserCount(): Int = userDao.getUserCount()

    // --- PROFILE ---
    fun getProfileFlow(userId: String): Flow<Profile?> =
        profileDao.getProfileFlow(userId).map { it?.toDomain() }

    suspend fun getProfileByUserId(userId: String): Profile? =
        profileDao.getProfileByUserId(userId)?.toDomain()

    suspend fun saveProfile(profile: Profile) =
        profileDao.insertOrUpdateProfile(profile.toEntity())

    // --- GOALS ---
    fun getGoalsFlow(userId: String): Flow<List<Goal>> =
        goalDao.getGoalsFlow(userId).map { list -> list.map { it.toDomain() } }

    suspend fun insertGoal(goal: Goal) =
        goalDao.insertGoal(goal.toEntity())

    // --- EVOLUTION STATE ---
    fun getEvolutionStateFlow(userId: String): Flow<EvolutionState?> =
        evolutionDao.getEvolutionStateFlow(userId).map { it?.toDomain() }

    suspend fun getEvolutionState(userId: String): EvolutionState? =
        evolutionDao.getEvolutionState(userId)?.toDomain()

    suspend fun saveEvolutionState(state: EvolutionState) =
        evolutionDao.insertOrUpdateEvolutionState(state.toEntity())

    // --- PERFORMANCE STATE ---
    fun getPerformanceStateFlow(userId: String): Flow<PerformanceState?> =
        evolutionDao.getPerformanceStateFlow(userId).map { it?.toDomain() }

    suspend fun savePerformanceState(state: PerformanceState) =
        evolutionDao.insertOrUpdatePerformanceState(state.toEntity())

    // --- ASSESSMENTS ---
    fun getInitialAssessmentFlow(userId: String): Flow<Assessment?> =
        assessmentDao.getInitialAssessmentFlow(userId).map { it?.toDomain() }

    suspend fun getInitialAssessment(userId: String): Assessment? =
        assessmentDao.getInitialAssessment(userId)?.toDomain()

    suspend fun saveAssessment(assessment: Assessment) =
        assessmentDao.insertAssessment(assessment.toEntity())

    suspend fun updateAssessment(assessment: Assessment) =
        assessmentDao.updateAssessment(assessment.toEntity())

    // --- MEASUREMENTS & EVIDENCES ---
    fun getMeasurementsFlow(userId: String): Flow<List<Measurement>> =
        assessmentDao.getMeasurementsFlow(userId).map { list -> list.map { it.toDomain() } }

    suspend fun insertMeasurement(measurement: Measurement) =
        assessmentDao.insertMeasurement(measurement.toEntity())

    fun getEvidencesFlow(userId: String): Flow<List<Evidence>> =
        assessmentDao.getEvidencesFlow(userId).map { list -> list.map { it.toDomain() } }

    suspend fun insertEvidence(evidence: Evidence) =
        assessmentDao.insertEvidence(evidence.toEntity())

    // --- MISSIONS & TRIALS ---
    fun getMissionsFlow(userId: String): Flow<List<Mission>> =
        missionTrialDao.getMissionsFlow(userId).map { list -> list.map { it.toDomain() } }

    suspend fun insertMission(mission: Mission) =
        missionTrialDao.insertMission(mission.toEntity())

    fun getTrialsFlow(userId: String): Flow<List<Trial>> =
        missionTrialDao.getTrialsFlow(userId).map { list -> list.map { it.toDomain() } }

    suspend fun insertTrial(trial: Trial) =
        missionTrialDao.insertTrial(trial.toEntity())

    // --- AUDIT LOGS (Immutable) ---
    fun getRecentAuditLogsFlow(limit: Int = 100): Flow<List<AuditLog>> =
        auditDao.getRecentAuditLogsFlow(limit).map { list -> list.map { it.toDomain() } }

    fun getUserAuditLogsFlow(userId: String): Flow<List<AuditLog>> =
        auditDao.getUserAuditLogsFlow(userId).map { list -> list.map { it.toDomain() } }

    suspend fun insertAuditLog(log: AuditLog) =
        auditDao.insertAuditLog(log.toEntity())

    // --- AI GATEWAY INTERACTIONS ---
    fun getAIInteractionsFlow(userId: String): Flow<List<AIInteraction>> =
        aiGatewayDao.getAIInteractionsFlow(userId).map { list -> list.map { it.toDomain() } }

    suspend fun insertAIInteraction(interaction: AIInteraction) =
        aiGatewayDao.insertAIInteraction(interaction.toEntity())

    // --- DATA CORE V1 PIPELINE REPOSITORY ---
    private val rawDataDao = database.rawDataDao()
    private val provenanceDao = database.provenanceDao()
    private val coreMeasurementDao = database.dataCoreMeasurementDao()
    private val coreEvidenceDao = database.dataCoreEvidenceDao()
    private val coreAuditDao = database.dataCoreAuditDao()
    private val protocolDao = database.protocolDao()
    private val metricDao = database.metricDao()
    private val scoreSnapshotDao = database.scoreSnapshotDao()

    suspend fun insertRawData(rawInput: com.example.data.local.entity.RawDataInputEntity) =
        rawDataDao.insertRawData(rawInput)

    fun getRawDataInputsFlow(userId: String) = rawDataDao.getRawDataInputsFlow(userId)

    suspend fun insertProvenance(provenance: com.example.data.local.entity.ProvenanceEntity) =
        provenanceDao.insertProvenance(provenance)

    suspend fun getProvenanceById(id: String) = provenanceDao.getProvenanceById(id)

    suspend fun insertCoreMeasurement(measurement: com.example.data.local.entity.CoreMeasurementEntity) =
        coreMeasurementDao.insertCoreMeasurement(measurement)

    fun getCoreMeasurementsFlow(userId: String) = coreMeasurementDao.getMeasurementsFlow(userId)

    suspend fun getCoreMeasurementsByUserId(userId: String) = coreMeasurementDao.getMeasurementsByUserId(userId)

    suspend fun insertCoreEvidence(evidence: com.example.data.local.entity.CoreEvidenceEntity) =
        coreEvidenceDao.insertCoreEvidence(evidence)

    fun getCoreEvidencesFlow(userId: String) = coreEvidenceDao.getEvidencesFlow(userId)

    suspend fun getCoreEvidencesByUserId(userId: String) = coreEvidenceDao.getEvidencesByUserId(userId)

    suspend fun insertCoreAuditLog(log: com.example.data.local.entity.CoreAuditLogEntity) =
        coreAuditDao.insertAuditLog(log)

    fun getCoreAuditTrailFlow(limit: Int = 100) = coreAuditDao.getRecentAuditLogsFlow(limit)

    // --- SCORE ENGINE V1 REPOSITORY ---
    suspend fun insertScoreSnapshot(snapshot: com.example.data.local.entity.ScoreSnapshotEntity) =
        scoreSnapshotDao.insertScoreSnapshot(snapshot)

    fun getScoreSnapshotsFlow(userId: String) = scoreSnapshotDao.getScoreSnapshotsFlow(userId)

    fun getLatestOfficialSnapshotFlow(userId: String) = scoreSnapshotDao.getLatestOfficialSnapshotFlow(userId)

    suspend fun getLatestOfficialSnapshot(userId: String) = scoreSnapshotDao.getLatestOfficialSnapshot(userId)

    fun getLatestSnapshotFlow(userId: String) = scoreSnapshotDao.getLatestSnapshotFlow(userId)

    suspend fun getLatestSnapshot(userId: String) = scoreSnapshotDao.getLatestSnapshot(userId)

    suspend fun getSnapshotById(id: String) = scoreSnapshotDao.getSnapshotById(id)

    // --- TRAINING DOMAIN V1 REPOSITORY ---
    suspend fun insertTrainingSession(session: com.example.data.local.entity.TrainingSessionEntity) =
        trainingSessionDao.insertSession(session)

    suspend fun insertSessionExerciseLogs(logs: List<com.example.data.local.entity.SessionExerciseLogEntity>) =
        trainingSessionDao.insertExerciseLogs(logs)

    suspend fun getTrainingSessionById(sessionId: String) =
        trainingSessionDao.getSessionById(sessionId)

    suspend fun getExerciseLogsForSession(sessionId: String) =
        trainingSessionDao.getExerciseLogsForSession(sessionId)

    fun getTrainingSessionsForUserFlow(userId: String) =
        trainingSessionDao.getSessionsForUserFlow(userId)

    suspend fun getTrainingSessionsForUser(userId: String) =
        trainingSessionDao.getSessionsForUser(userId)

    suspend fun insertWorkout(workout: com.example.data.local.entity.WorkoutEntity) =
        workoutDao.insertWorkout(workout)

    suspend fun insertWorkoutItems(items: List<com.example.data.local.entity.WorkoutItemEntity>) =
        workoutDao.insertWorkoutItems(items)

    fun getAllActiveWorkoutsFlow() =
        workoutDao.getAllActiveWorkouts()

    suspend fun getAthleteLatestBaseline(userId: String) =
        athleteBaselineDao.getLatestBaseline(userId)

    suspend fun insertAthleteBaseline(baseline: com.example.data.local.entity.AthleteBaselineEntity) =
        athleteBaselineDao.insertBaseline(baseline)

    // Mapper extensions
    private fun UserEntity.toDomain() = User(id, email, passwordHash, role, createdAt, updatedAt, isActive)
    private fun User.toEntity() = UserEntity(id, email, passwordHash, role, createdAt, updatedAt, isActive)

    private fun ProfileEntity.toDomain() = Profile(id, userId, fullName, nickname, dateOfBirth, gender, heightCm, weightKg, status, createdAt, updatedAt)
    private fun Profile.toEntity() = ProfileEntity(id, userId, fullName, nickname, dateOfBirth, gender, heightCm, weightKg, status, createdAt, updatedAt)

    private fun GoalEntity.toDomain() = Goal(id, userId, title, description, category, targetValue, targetDate, status, createdAt)
    private fun Goal.toEntity() = GoalEntity(id, userId, title, description, category, targetValue, targetDate, status, createdAt)

    private fun AssessmentEntity.toDomain() = Assessment(id, userId, assessmentType, status, summary, requestedAt, conductedAt, engineVersion)
    private fun Assessment.toEntity() = AssessmentEntity(id, userId, assessmentType, status, summary, requestedAt, conductedAt, engineVersion)

    private fun MeasurementEntity.toDomain() = Measurement(id, userId, assessmentId, metricType, rawValue, unit, source, recordedAt, signature)
    private fun Measurement.toEntity() = MeasurementEntity(id, userId, assessmentId, metricType, rawValue, unit, source, recordedAt, signature)

    private fun EvidenceEntity.toDomain() = Evidence(id, userId, referenceType, referenceId, evidenceType, dataHash, uriOrLocation, verified, submittedAt)
    private fun Evidence.toEntity() = EvidenceEntity(id, userId, referenceType, referenceId, evidenceType, dataHash, uriOrLocation, verified, submittedAt)

    private fun PerformanceStateEntity.toDomain() = PerformanceState(id, userId, readinessScore, staminaIndex, cognitiveLoad, recoveryIndex, calculatedAt, engineVersion)
    private fun PerformanceState.toEntity() = PerformanceStateEntity(id, userId, readinessScore, staminaIndex, cognitiveLoad, recoveryIndex, calculatedAt, engineVersion)

    private fun EvolutionStateEntity.toDomain() = EvolutionState(id, userId, currentClass, currentLevel, currentXp, requiredXpForNextLevel, rankStatus, updatedAt, engineVersion)
    private fun EvolutionState.toEntity() = EvolutionStateEntity(id, userId, currentClass, currentLevel, currentXp, requiredXpForNextLevel, rankStatus, updatedAt, engineVersion)

    private fun MissionEntity.toDomain() = Mission(id, userId, code, title, description, difficulty, status, tentativeXpReward, createdAt)
    private fun Mission.toEntity() = MissionEntity(id, userId, code, title, description, difficulty, status, tentativeXpReward, createdAt)

    private fun TrialEntity.toDomain() = Trial(id, userId, title, description, requirements, status, evaluatedAt)
    private fun Trial.toEntity() = TrialEntity(id, userId, title, description, requirements, status, evaluatedAt)

    private fun AuditLogEntity.toDomain() = AuditLog(id, userId, action, resource, detailsJson, severity, source, timestamp)
    private fun AuditLog.toEntity() = AuditLogEntity(id, userId, action, resource, detailsJson, severity, source, timestamp)

    private fun AIInteractionEntity.toDomain() = AIInteraction(id, userId, promptContext, suggestedAction, confidence, processedByCore, appliedStateChange, timestamp)
    private fun AIInteraction.toEntity() = AIInteractionEntity(id, userId, promptContext, suggestedAction, confidence, processedByCore, appliedStateChange, timestamp)
}
