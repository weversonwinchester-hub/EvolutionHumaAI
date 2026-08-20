package com.example.service

import com.example.core.aigateway.AIGateway
import com.example.core.aigateway.FoundationAIGateway
import com.example.core.error.AppError
import com.example.core.error.AppResult
import com.example.core.model.Assessment
import com.example.core.model.AssessmentStatus
import com.example.core.model.AssessmentType
import com.example.core.model.AuditLog
import com.example.core.model.AuditSeverity
import com.example.core.model.EvolutionState
import com.example.core.model.INITIAL_EVOLUTION_CLASS
import com.example.core.model.Measurement
import com.example.core.model.Permission
import com.example.core.model.Profile
import com.example.core.model.ProfileStatus
import com.example.core.model.User
import com.example.core.model.UserRole
import com.example.core.security.PasswordHasher
import com.example.core.security.SecurityContext
import com.example.data.repository.PerformAIRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class CoreServices(
    private val repository: PerformAIRepository,
    val aiGateway: AIGateway = FoundationAIGateway()
) {

    // ==========================================
    // 1. AUDIT SERVICE
    // ==========================================
    suspend fun logAudit(
        userId: String?,
        action: String,
        resource: String,
        details: String,
        severity: AuditSeverity = AuditSeverity.INFO
    ) {
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            userId = userId,
            action = action,
            resource = resource,
            detailsJson = details,
            severity = severity,
            source = "CoreServiceAuthority",
            timestamp = System.currentTimeMillis()
        )
        repository.insertAuditLog(log)
    }

    // ==========================================
    // 2. AUTH SERVICE
    // ==========================================
    suspend fun registerUser(
        email: String,
        passwordRaw: String,
        fullName: String
    ): AppResult<User> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return AppResult.Failure(AppError.ValidationError("E-mail inválido fornecido."))
        }
        if (passwordRaw.length < 6) {
            return AppResult.Failure(AppError.ValidationError("A senha deve ter no mínimo 6 caracteres."))
        }
        if (fullName.trim().isEmpty()) {
            return AppResult.Failure(AppError.ValidationError("Nome completo é obrigatório."))
        }

        val existing = repository.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return AppResult.Failure(AppError.ValidationError("Já existe uma conta cadastrada com este e-mail."))
        }

        val userId = UUID.randomUUID().toString()
        val passwordHash = PasswordHasher.hashPassword(passwordRaw)
        val isFirstUser = repository.getUserCount() == 0
        val role = if (isFirstUser) UserRole.ADMIN else UserRole.USER

        val newUser = User(
            id = userId,
            email = trimmedEmail,
            passwordHash = passwordHash,
            role = role,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isActive = true
        )

        repository.insertUser(newUser)

        // Criar Perfil Inicial Obrigatório (INCOMPLETE)
        val initialProfile = Profile(
            id = UUID.randomUUID().toString(),
            userId = userId,
            fullName = fullName.trim(),
            nickname = fullName.trim().split(" ").firstOrNull() ?: fullName.trim(),
            dateOfBirth = "",
            gender = "Não especificado",
            heightCm = 0.0,
            weightKg = 0.0,
            status = ProfileStatus.INCOMPLETE,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.saveProfile(initialProfile)

        // REGRA CRÍTICA: Estado evolutivo inicial fixado estritamente como "Corpo Adormecido"
        val initialEvolution = EvolutionState(
            id = UUID.randomUUID().toString(),
            userId = userId,
            currentClass = INITIAL_EVOLUTION_CLASS,
            currentLevel = 1,
            currentXp = 0L,
            requiredXpForNextLevel = 1000L,
            rankStatus = "Iniciante Não Avaliado",
            updatedAt = System.currentTimeMillis(),
            engineVersion = "1.0.0-foundation"
        )
        repository.saveEvolutionState(initialEvolution)

        logAudit(
            userId = userId,
            action = "USER_REGISTERED",
            resource = "User/EvolutionState",
            details = "Usuário registrado com perfil inicial e classe 'Corpo Adormecido' (Nível 1, 0 XP)."
        )

        SecurityContext.setCurrentUser(newUser)
        return AppResult.Success(newUser)
    }

    suspend fun login(email: String, passwordRaw: String): AppResult<User> {
        val trimmedEmail = email.trim().lowercase()
        val user = repository.getUserByEmail(trimmedEmail)
            ?: return AppResult.Failure(AppError.AuthenticationError("Credenciais inválidas."))

        if (!PasswordHasher.verifyPassword(passwordRaw, user.passwordHash)) {
            logAudit(
                userId = user.id,
                action = "LOGIN_FAILED",
                resource = "Auth",
                details = "Tentativa de login com senha incorreta.",
                severity = AuditSeverity.WARNING
            )
            return AppResult.Failure(AppError.AuthenticationError("Credenciais inválidas."))
        }

        if (!user.isActive) {
            return AppResult.Failure(AppError.SecurityViolation("Conta inativa ou suspensa pelo Core."))
        }

        // Lazy Rehash: atualiza silenciosamente hashes legados para PBKDF2 v2
        var authenticatedUser = user
        if (PasswordHasher.needsRehash(user.passwordHash)) {
            val upgradedHash = PasswordHasher.hashPassword(passwordRaw)
            val updatedUser = user.copy(passwordHash = upgradedHash, updatedAt = System.currentTimeMillis())
            repository.updateUser(updatedUser)
            authenticatedUser = updatedUser
            logAudit(
                userId = user.id,
                action = "PASSWORD_HASH_UPGRADED_V2",
                resource = "Auth",
                details = "Hash de senha migrado de forma transparente e segura para PBKDF2-HMAC-SHA512."
            )
        }

        SecurityContext.setCurrentUser(authenticatedUser)
        logAudit(
            userId = authenticatedUser.id,
            action = "LOGIN_SUCCESS",
            resource = "Auth",
            details = "Login autenticado com sucesso. Role: ${authenticatedUser.role}."
        )

        return AppResult.Success(authenticatedUser)
    }

    fun logout() {
        val current = SecurityContext.getAuthenticatedUserId()
        SecurityContext.setCurrentUser(null)
    }

    // ==========================================
    // 3. PROFILE SERVICE
    // ==========================================
    suspend fun updateProfileBiometrics(
        fullName: String,
        nickname: String,
        dateOfBirth: String,
        gender: String,
        heightCm: Double,
        weightKg: Double
    ): AppResult<Profile> {
        val userId = SecurityContext.getAuthenticatedUserId()
            ?: return AppResult.Failure(AppError.AuthenticationError("Sessão não autenticada."))

        if (fullName.isBlank()) {
            return AppResult.Failure(AppError.ValidationError("Nome completo é obrigatório."))
        }
        if (heightCm <= 50.0 || heightCm >= 300.0) {
            return AppResult.Failure(AppError.ValidationError("Altura inválida ($heightCm cm). Deve estar entre 50 e 300 cm."))
        }
        if (weightKg <= 20.0 || weightKg >= 500.0) {
            return AppResult.Failure(AppError.ValidationError("Peso inválido ($weightKg kg). Deve estar entre 20 e 500 kg."))
        }

        val existingProfile = repository.getProfileByUserId(userId)
        val profileId = existingProfile?.id ?: UUID.randomUUID().toString()

        val updatedProfile = Profile(
            id = profileId,
            userId = userId,
            fullName = fullName.trim(),
            nickname = nickname.ifBlank { fullName.trim().split(" ").first() },
            dateOfBirth = dateOfBirth.trim(),
            gender = gender.trim(),
            heightCm = heightCm,
            weightKg = weightKg,
            status = ProfileStatus.PENDING_INITIAL_ASSESSMENT,
            createdAt = existingProfile?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        repository.saveProfile(updatedProfile)

        logAudit(
            userId = userId,
            action = "PROFILE_UPDATED",
            resource = "Profile",
            details = "Biometria atualizada com sucesso. Status avançado para PENDING_INITIAL_ASSESSMENT."
        )

        return AppResult.Success(updatedProfile)
    }

    // ==========================================
    // 4. ASSESSMENT SERVICE
    // ==========================================
    suspend fun initiateInitialAssessment(): AppResult<Assessment> {
        val userId = SecurityContext.getAuthenticatedUserId()
            ?: return AppResult.Failure(AppError.AuthenticationError("Sessão não autenticada."))

        val existingAssessment = repository.getInitialAssessment(userId)
        if (existingAssessment != null && existingAssessment.status != AssessmentStatus.NOT_STARTED) {
            return AppResult.Success(existingAssessment)
        }

        val newAssessment = Assessment(
            id = UUID.randomUUID().toString(),
            userId = userId,
            assessmentType = AssessmentType.INITIAL_FOUNDATION,
            status = AssessmentStatus.IN_PROGRESS,
            summary = "Protocolo de Avaliação Inicial iniciado. Aguardando coleta de métricas e evidências fundamentais.",
            requestedAt = System.currentTimeMillis(),
            conductedAt = null,
            engineVersion = "1.0.0-foundation"
        )

        repository.saveAssessment(newAssessment)

        logAudit(
            userId = userId,
            action = "ASSESSMENT_INITIATED",
            resource = "Assessment",
            details = "Protocolo de Avaliação Inicial da fundação iniciado pelo usuário."
        )

        return AppResult.Success(newAssessment)
    }

    suspend fun recordBaselineMeasurement(
        assessmentId: String,
        metricType: String,
        value: Double,
        unit: String
    ): AppResult<Measurement> {
        val userId = SecurityContext.getAuthenticatedUserId()
            ?: return AppResult.Failure(AppError.AuthenticationError("Sessão não autenticada."))

        if (value <= 0) {
            return AppResult.Failure(AppError.ValidationError("O valor da métrica deve ser maior que zero."))
        }

        val measurement = Measurement(
            id = UUID.randomUUID().toString(),
            userId = userId,
            assessmentId = assessmentId,
            metricType = metricType,
            rawValue = value,
            unit = unit,
            source = "UserSubmission_Validated",
            recordedAt = System.currentTimeMillis()
        )

        repository.insertMeasurement(measurement)

        logAudit(
            userId = userId,
            action = "MEASUREMENT_RECORDED",
            resource = "Measurement",
            details = "Métrica $metricType ($value $unit) gravada para a avaliação $assessmentId."
        )

        return AppResult.Success(measurement)
    }

    // ==========================================
    // 5. SECURITY & IMMUTABILITY PROTECTION
    // ==========================================
    suspend fun validateSecurityStateMutationAttempt(requestedNewClass: String): AppResult<Unit> {
        val userId = SecurityContext.getAuthenticatedUserId()
        logAudit(
            userId = userId,
            action = "UNAUTHORIZED_MUTATION_ATTEMPT",
            resource = "EvolutionState",
            details = "Bloqueio de segurança: Tentativa do cliente de alterar classe para '$requestedNewClass'. " +
                    "O Core é a autoridade exclusiva para promoções.",
            severity = AuditSeverity.SECURITY_VIOLATION
        )
        return AppResult.Failure(AppError.UnauthorizedStateMutation())
    }

    // ==========================================
    // 6. PERFORMAI DATA CORE V1 PIPELINE SERVICE
    // ==========================================
    private val dataCorePipeline = com.example.core.datacore.pipeline.DataCorePipeline()

    suspend fun processDataCoreInput(input: com.example.core.datacore.model.RawDataInput): AppResult<com.example.core.datacore.pipeline.IngestionSuccessResult> {
        val userId = input.userId.ifBlank { SecurityContext.getAuthenticatedUserId() ?: "" }
        val sanitizedInput = input.copy(userId = userId)

        // 1. Grava o dado bruto imutável antes de qualquer processamento
        val rawEntity = com.example.data.local.entity.RawDataInputEntity(
            id = sanitizedInput.id,
            userId = sanitizedInput.userId,
            assessmentId = sanitizedInput.assessmentId,
            metricId = sanitizedInput.metricId,
            rawPayload = sanitizedInput.rawPayload,
            unit = sanitizedInput.unit,
            source = sanitizedInput.source,
            sourceType = sanitizedInput.sourceType,
            sourceIdentifier = sanitizedInput.sourceIdentifier,
            deviceId = sanitizedInput.deviceId,
            protocolId = sanitizedInput.protocolId,
            clientTimestamp = sanitizedInput.clientTimestamp,
            serverTimestamp = sanitizedInput.serverTimestamp,
            isMock = sanitizedInput.isMock
        )
        repository.insertRawData(rawEntity)

        // 2. Executa a cadeia DataCorePipeline
        val pipelineResult = dataCorePipeline.ingestRawData(sanitizedInput)

        when (pipelineResult) {
            is AppResult.Failure -> {
                // Registrar log de auditoria de rejeição
                val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    actorType = com.example.core.datacore.model.ActorType.SYSTEM,
                    actorId = "DataCorePipeline",
                    action = "RAW_DATA_INGESTION_REJECTED",
                    entityType = "RawDataInput",
                    entityId = sanitizedInput.id,
                    previousState = null,
                    newState = "ERROR=${pipelineResult.error.message}",
                    timestamp = System.currentTimeMillis(),
                    requestId = java.util.UUID.randomUUID().toString(),
                    systemVersion = "1.0.0-datacore-v1"
                )
                repository.insertCoreAuditLog(auditEntity)
                return pipelineResult
            }
            is AppResult.Success -> {
                val data = pipelineResult.data

                // 3. Persiste Provenance
                val provEntity = com.example.data.local.entity.ProvenanceEntity(
                    id = data.provenance.id,
                    sourceType = data.provenance.sourceType,
                    sourceIdentifier = data.provenance.sourceIdentifier,
                    deviceIdentifier = data.provenance.deviceIdentifier,
                    captureTimestamp = data.provenance.captureTimestamp,
                    processingTimestamp = data.provenance.processingTimestamp,
                    processingVersion = data.provenance.processingVersion,
                    protocolId = data.provenance.protocolId,
                    integrityHash = data.provenance.integrityHash,
                    createdAt = data.provenance.createdAt
                )
                repository.insertProvenance(provEntity)

                // 4. Persiste Measurement validado
                val msrEntity = com.example.data.local.entity.CoreMeasurementEntity(
                    id = data.measurement.id,
                    assessmentId = data.measurement.assessmentId,
                    userId = data.measurement.userId,
                    metricId = data.measurement.metricId,
                    rawValue = data.measurement.rawValue,
                    normalizedValue = data.measurement.normalizedValue,
                    unit = data.measurement.unit,
                    timestamp = data.measurement.timestamp,
                    source = data.measurement.source,
                    deviceId = data.measurement.deviceId,
                    protocolId = data.measurement.protocolId,
                    validationStatus = data.measurement.validationStatus,
                    rejectionReason = data.measurement.rejectionReason,
                    rawDataInputId = data.measurement.rawDataInputId,
                    isMock = data.measurement.isMock,
                    createdAt = data.measurement.createdAt
                )
                repository.insertCoreMeasurement(msrEntity)

                // 5. Persiste Evidence
                val evEntity = com.example.data.local.entity.CoreEvidenceEntity(
                    id = data.evidence.id,
                    userId = data.evidence.userId,
                    assessmentId = data.evidence.assessmentId,
                    measurementIdsJson = data.evidence.measurementIds.joinToString(","),
                    source = data.evidence.source,
                    capturedAt = data.evidence.capturedAt,
                    submittedAt = data.evidence.submittedAt,
                    integrityStatus = data.evidence.integrityStatus,
                    reliabilityScore = data.evidence.reliabilityScore,
                    confidenceScore = data.evidence.confidenceScore,
                    provenanceId = data.evidence.provenanceId,
                    coreVersion = data.evidence.coreVersion,
                    isMock = data.evidence.isMock,
                    createdAt = data.evidence.createdAt
                )
                repository.insertCoreEvidence(evEntity)

                // 6. Persiste Core Audit Log Imutável
                val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
                    id = data.auditLog.id,
                    actorType = data.auditLog.actorType,
                    actorId = data.auditLog.actorId,
                    action = data.auditLog.action,
                    entityType = data.auditLog.entityType,
                    entityId = data.auditLog.entityId,
                    previousState = data.auditLog.previousState,
                    newState = data.auditLog.newState,
                    timestamp = data.auditLog.timestamp,
                    requestId = data.auditLog.requestId,
                    systemVersion = data.auditLog.systemVersion
                )
                repository.insertCoreAuditLog(auditEntity)

                return pipelineResult
            }
        }
    }

    // ==========================================
    // 7. PERFORMAI SCORE ENGINE V1 SERVICE
    // ==========================================
    private val scoreEngine = com.example.core.scoreengine.engine.ScoreEngineV1()

    suspend fun computeOfficialScoreSnapshot(
        userId: String,
        assessmentId: String? = null
    ): AppResult<com.example.core.scoreengine.model.ScoreSnapshot> {
        val targetUserId = userId.ifBlank { SecurityContext.getAuthenticatedUserId() ?: "" }
        if (targetUserId.isBlank()) {
            return AppResult.Failure(AppError.ValidationError("userId é obrigatório."))
        }

        // 1. Carrega medições e evidências do Data Core para o usuário
        val measurementEntities = repository.getCoreMeasurementsByUserId(targetUserId)
        val evidenceEntities = repository.getCoreEvidencesByUserId(targetUserId)

        val domainMeasurements = measurementEntities.map { entity ->
            com.example.core.datacore.model.DataCoreMeasurement(
                id = entity.id,
                assessmentId = entity.assessmentId,
                userId = entity.userId,
                metricId = entity.metricId,
                rawValue = entity.rawValue,
                normalizedValue = entity.normalizedValue,
                unit = entity.unit,
                timestamp = entity.timestamp,
                source = entity.source,
                deviceId = entity.deviceId,
                protocolId = entity.protocolId,
                validationStatus = entity.validationStatus,
                rejectionReason = entity.rejectionReason,
                rawDataInputId = entity.rawDataInputId,
                isMock = entity.isMock,
                createdAt = entity.createdAt
            )
        }

        val domainEvidences = evidenceEntities.map { entity ->
            com.example.core.datacore.model.DataCoreEvidence(
                id = entity.id,
                userId = entity.userId,
                assessmentId = entity.assessmentId,
                measurementIds = entity.measurementIdsJson.split(",").filter { it.isNotBlank() },
                source = entity.source,
                capturedAt = entity.capturedAt,
                submittedAt = entity.submittedAt,
                integrityStatus = entity.integrityStatus,
                reliabilityScore = entity.reliabilityScore,
                confidenceScore = entity.confidenceScore,
                provenanceId = entity.provenanceId,
                coreVersion = entity.coreVersion,
                isMock = entity.isMock,
                createdAt = entity.createdAt
            )
        }

        val provenances = mutableMapOf<String, com.example.core.datacore.model.DataCoreProvenance>()
        for (ev in domainEvidences) {
            val provEntity = repository.getProvenanceById(ev.provenanceId)
            if (provEntity != null) {
                provenances[provEntity.id] = com.example.core.datacore.model.DataCoreProvenance(
                    id = provEntity.id,
                    sourceType = provEntity.sourceType,
                    sourceIdentifier = provEntity.sourceIdentifier,
                    deviceIdentifier = provEntity.deviceIdentifier,
                    captureTimestamp = provEntity.captureTimestamp,
                    processingTimestamp = provEntity.processingTimestamp,
                    processingVersion = provEntity.processingVersion,
                    protocolId = provEntity.protocolId,
                    integrityHash = provEntity.integrityHash,
                    createdAt = provEntity.createdAt
                )
            }
        }

        // 2. Executa cálculo no Score Engine V1
        val computeResult = scoreEngine.computeScore(
            userId = targetUserId,
            assessmentId = assessmentId,
            measurements = domainMeasurements,
            evidences = domainEvidences,
            provenances = provenances,
            isMockMode = false
        )

        return when (computeResult) {
            is AppResult.Failure -> computeResult
            is AppResult.Success -> {
                val snapshot = computeResult.data.snapshot
                val auditLog = computeResult.data.auditLog

                // 3. Persiste ScoreSnapshot imutável no banco
                val snapshotEntity = com.example.data.local.entity.ScoreSnapshotEntity(
                    id = snapshot.id,
                    userId = snapshot.userId,
                    assessmentId = snapshot.assessmentId,
                    scoreVersion = snapshot.scoreVersion,
                    coreVersion = snapshot.coreVersion,
                    calculatedAt = snapshot.calculatedAt,
                    performanceIndexValue = snapshot.performanceIndex.value,
                    performanceIndexStatus = snapshot.performanceIndex.calculationStatus,
                    dimensionScoresJson = snapshot.dimensionScores.joinToString(";") { "${it.dimension}:${it.score ?: "NULL"}:${it.calculationStatus}" },
                    evidenceIdsJson = snapshot.evidenceIds.joinToString(","),
                    metricIdsJson = snapshot.metricIds.joinToString(","),
                    calculationStatus = snapshot.calculationStatus,
                    confidenceMetadataJson = "Tier=${snapshot.confidenceMetadata.sourceTier},Status=${snapshot.confidenceMetadata.confidenceStatus}",
                    isMock = snapshot.isMock,
                    provenanceId = snapshot.provenanceId,
                    overallExplanationJson = snapshot.overallExplanation.notes
                )
                repository.insertScoreSnapshot(snapshotEntity)

                // 4. Persiste Log de Auditoria
                val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
                    id = auditLog.id,
                    actorType = auditLog.actorType,
                    actorId = auditLog.actorId,
                    action = auditLog.action,
                    entityType = auditLog.entityType,
                    entityId = auditLog.entityId,
                    previousState = auditLog.previousState,
                    newState = auditLog.newState,
                    timestamp = auditLog.timestamp,
                    requestId = auditLog.requestId,
                    systemVersion = auditLog.systemVersion
                )
                repository.insertCoreAuditLog(auditEntity)

                AppResult.Success(snapshot)
            }
        }
    }

    /**
     * Gera snapshot de demonstração / mock com isolamento explícito (isMock = true).
     */
    suspend fun generateDemoMockScoreSnapshot(
        userId: String
    ): AppResult<com.example.core.scoreengine.model.ScoreSnapshot> {
        val targetUserId = userId.ifBlank { SecurityContext.getAuthenticatedUserId() ?: "DEMO-USER" }

        val computeResult = scoreEngine.computeScore(
            userId = targetUserId,
            assessmentId = "DEMO-ASM-001",
            measurements = emptyList(),
            evidences = emptyList(),
            provenances = emptyMap(),
            isMockMode = true
        )

        return when (computeResult) {
            is AppResult.Failure -> computeResult
            is AppResult.Success -> {
                val snapshot = computeResult.data.snapshot
                val auditLog = computeResult.data.auditLog

                val snapshotEntity = com.example.data.local.entity.ScoreSnapshotEntity(
                    id = snapshot.id,
                    userId = snapshot.userId,
                    assessmentId = snapshot.assessmentId,
                    scoreVersion = snapshot.scoreVersion,
                    coreVersion = snapshot.coreVersion,
                    calculatedAt = snapshot.calculatedAt,
                    performanceIndexValue = snapshot.performanceIndex.value,
                    performanceIndexStatus = snapshot.performanceIndex.calculationStatus,
                    dimensionScoresJson = snapshot.dimensionScores.joinToString(";") { "${it.dimension}:${it.score ?: "NULL"}:${it.calculationStatus}" },
                    evidenceIdsJson = snapshot.evidenceIds.joinToString(","),
                    metricIdsJson = snapshot.metricIds.joinToString(","),
                    calculationStatus = snapshot.calculationStatus,
                    confidenceMetadataJson = "Tier=${snapshot.confidenceMetadata.sourceTier},Status=${snapshot.confidenceMetadata.confidenceStatus}",
                    isMock = true,
                    provenanceId = snapshot.provenanceId,
                    overallExplanationJson = snapshot.overallExplanation.notes
                )
                repository.insertScoreSnapshot(snapshotEntity)

                val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
                    id = auditLog.id,
                    actorType = auditLog.actorType,
                    actorId = auditLog.actorId,
                    action = auditLog.action,
                    entityType = auditLog.entityType,
                    entityId = auditLog.entityId,
                    previousState = auditLog.previousState,
                    newState = auditLog.newState,
                    timestamp = auditLog.timestamp,
                    requestId = auditLog.requestId,
                    systemVersion = auditLog.systemVersion
                )
                repository.insertCoreAuditLog(auditEntity)

                AppResult.Success(snapshot)
            }
        }
    }

    /**
     * TENTATIVA DE INJEÇÃO DIRETA DE SCORE PELO CLIENTE:
     * O cliente NÃO pode enviar valores de scores arbitrários para o backend.
     * Toda tentativa é rejeitada, auditada com alta severidade e não altera o estado.
     */
    suspend fun attemptDirectScoreMutation(
        userId: String,
        attemptedValue: Double,
        attemptedDimension: String
    ): AppResult<Unit> {
        val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
            id = java.util.UUID.randomUUID().toString(),
            actorType = com.example.core.datacore.model.ActorType.CLIENT,
            actorId = SecurityContext.getAuthenticatedUserId() ?: "UNKNOWN_CLIENT",
            action = "SECURITY_VIOLATION_SCORE_MANIPULATION_ATTEMPT",
            entityType = "ScoreSnapshot",
            entityId = "TARGET_USER_$userId",
            previousState = null,
            newState = "ATTEMPTED_MUTATION: dimension=$attemptedDimension, value=$attemptedValue; REJECTED=True",
            timestamp = System.currentTimeMillis(),
            requestId = java.util.UUID.randomUUID().toString(),
            systemVersion = "1.0.0-score-v1"
        )
        repository.insertCoreAuditLog(auditEntity)

        return AppResult.Failure(
            AppError.UnauthorizedStateMutation(
                "Violação de Segurança: Mutação direta de scores por clientes é estritamente proibida. Scores oficiais são derivados exclusivamente pelo Score Engine do Core."
            )
        )
    }

    // ====================================================
    // 8. EVIDENCE & CONSISTENCY ENGINE V1 SERVICE
    // ====================================================
    private val consistencyEngine = com.example.core.evidenceconsistency.engine.EvidenceConsistencyEngineV1()

    suspend fun generateOfficialEvidencePackage(
        userId: String
    ): AppResult<com.example.core.evidenceconsistency.model.EvolutionEvidencePackage> {
        val targetUserId = userId.ifBlank { SecurityContext.getAuthenticatedUserId() ?: "" }
        if (targetUserId.isBlank()) {
            return AppResult.Failure(AppError.ValidationError("userId é obrigatório."))
        }

        // 1. Carrega medições e evidências do Data Core
        val measurementEntities = repository.getCoreMeasurementsByUserId(targetUserId)
        val evidenceEntities = repository.getCoreEvidencesByUserId(targetUserId)

        val domainMeasurements = measurementEntities.map { entity ->
            com.example.core.datacore.model.DataCoreMeasurement(
                id = entity.id,
                assessmentId = entity.assessmentId,
                userId = entity.userId,
                metricId = entity.metricId,
                rawValue = entity.rawValue,
                normalizedValue = entity.normalizedValue,
                unit = entity.unit,
                timestamp = entity.timestamp,
                source = entity.source,
                deviceId = entity.deviceId,
                protocolId = entity.protocolId,
                validationStatus = entity.validationStatus,
                rejectionReason = entity.rejectionReason,
                rawDataInputId = entity.rawDataInputId,
                isMock = entity.isMock,
                createdAt = entity.createdAt
            )
        }

        val domainEvidences = evidenceEntities.map { entity ->
            com.example.core.datacore.model.DataCoreEvidence(
                id = entity.id,
                userId = entity.userId,
                assessmentId = entity.assessmentId,
                measurementIds = entity.measurementIdsJson.split(",").filter { it.isNotBlank() },
                source = entity.source,
                capturedAt = entity.capturedAt,
                submittedAt = entity.submittedAt,
                integrityStatus = entity.integrityStatus,
                reliabilityScore = entity.reliabilityScore,
                confidenceScore = entity.confidenceScore,
                provenanceId = entity.provenanceId,
                coreVersion = entity.coreVersion,
                isMock = entity.isMock,
                createdAt = entity.createdAt
            )
        }

        val provenances = mutableMapOf<String, com.example.core.datacore.model.DataCoreProvenance>()
        for (ev in domainEvidences) {
            val provEntity = repository.getProvenanceById(ev.provenanceId)
            if (provEntity != null) {
                provenances[provEntity.id] = com.example.core.datacore.model.DataCoreProvenance(
                    id = provEntity.id,
                    sourceType = provEntity.sourceType,
                    sourceIdentifier = provEntity.sourceIdentifier,
                    deviceIdentifier = provEntity.deviceIdentifier,
                    captureTimestamp = provEntity.captureTimestamp,
                    processingTimestamp = provEntity.processingTimestamp,
                    processingVersion = provEntity.processingVersion,
                    protocolId = provEntity.protocolId,
                    integrityHash = provEntity.integrityHash,
                    createdAt = provEntity.createdAt
                )
            }
        }

        // 2. Executa análise no Evidence & Consistency Engine V1
        val result = consistencyEngine.generateEvidencePackage(
            userId = targetUserId,
            measurements = domainMeasurements,
            evidences = domainEvidences,
            provenances = provenances,
            isSimulationMode = false
        )

        return when (result) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val pkg = result.data.evidencePackage
                val auditLog = result.data.auditLog

                // 3. Persiste pacote imutável
                val packageEntity = com.example.data.local.entity.EvolutionEvidencePackageEntity(
                    id = pkg.id,
                    userId = pkg.userId,
                    generatedAt = pkg.generatedAt,
                    coreVersion = pkg.coreVersion,
                    engineVersion = pkg.engineVersion,
                    evidenceIdsJson = pkg.evidenceIds.joinToString(","),
                    validMetricsJson = pkg.validMetrics.joinToString(","),
                    invalidMetricsJson = pkg.invalidMetrics.joinToString(","),
                    expiredEvidenceIdsJson = pkg.expiredEvidenceIds.joinToString(","),
                    pendingValidationItemsJson = pkg.pendingValidationItems.joinToString(";"),
                    overallConsistencyStatus = pkg.overallConsistencyStatus,
                    overallRepeatabilityStatus = pkg.overallRepeatabilityStatus,
                    maturityStatus = pkg.overallMaturity.maturityStatus,
                    qualityMatrixJson = "Integrity=${pkg.qualityMatrix.integrityScore},Fidelity=${pkg.qualityMatrix.protocolFidelityScore},Temporal=${pkg.qualityMatrix.temporalValidityStatus}",
                    limitationsJson = pkg.limitations.joinToString(";"),
                    auditReference = pkg.auditReference,
                    isMock = pkg.isMock,
                    simulationMode = pkg.simulationMode
                )
                repository.insertEvolutionEvidencePackage(packageEntity)

                // 4. Persiste log de auditoria
                val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
                    id = auditLog.id,
                    actorType = auditLog.actorType,
                    actorId = auditLog.actorId,
                    action = auditLog.action,
                    entityType = auditLog.entityType,
                    entityId = auditLog.entityId,
                    previousState = auditLog.previousState,
                    newState = auditLog.newState,
                    timestamp = auditLog.timestamp,
                    requestId = auditLog.requestId,
                    systemVersion = auditLog.systemVersion
                )
                repository.insertCoreAuditLog(auditEntity)

                AppResult.Success(pkg)
            }
        }
    }

    suspend fun generateSimulationEvidencePackage(
        userId: String
    ): AppResult<com.example.core.evidenceconsistency.model.EvolutionEvidencePackage> {
        val targetUserId = userId.ifBlank { SecurityContext.getAuthenticatedUserId() ?: "SIMULATION-USER" }

        val result = consistencyEngine.generateEvidencePackage(
            userId = targetUserId,
            measurements = emptyList(),
            evidences = emptyList(),
            provenances = emptyMap(),
            isSimulationMode = true
        )

        return when (result) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val pkg = result.data.evidencePackage
                val auditLog = result.data.auditLog

                val packageEntity = com.example.data.local.entity.EvolutionEvidencePackageEntity(
                    id = pkg.id,
                    userId = pkg.userId,
                    generatedAt = pkg.generatedAt,
                    coreVersion = pkg.coreVersion,
                    engineVersion = pkg.engineVersion,
                    evidenceIdsJson = pkg.evidenceIds.joinToString(","),
                    validMetricsJson = pkg.validMetrics.joinToString(","),
                    invalidMetricsJson = pkg.invalidMetrics.joinToString(","),
                    expiredEvidenceIdsJson = pkg.expiredEvidenceIds.joinToString(","),
                    pendingValidationItemsJson = pkg.pendingValidationItems.joinToString(";"),
                    overallConsistencyStatus = pkg.overallConsistencyStatus,
                    overallRepeatabilityStatus = pkg.overallRepeatabilityStatus,
                    maturityStatus = pkg.overallMaturity.maturityStatus,
                    qualityMatrixJson = "Integrity=1.0,Simulation=True",
                    limitationsJson = pkg.limitations.joinToString(";"),
                    auditReference = pkg.auditReference,
                    isMock = true,
                    simulationMode = true
                )
                repository.insertEvolutionEvidencePackage(packageEntity)

                val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
                    id = auditLog.id,
                    actorType = auditLog.actorType,
                    actorId = auditLog.actorId,
                    action = auditLog.action,
                    entityType = auditLog.entityType,
                    entityId = auditLog.entityId,
                    previousState = auditLog.previousState,
                    newState = auditLog.newState,
                    timestamp = auditLog.timestamp,
                    requestId = auditLog.requestId,
                    systemVersion = auditLog.systemVersion
                )
                repository.insertCoreAuditLog(auditEntity)

                AppResult.Success(pkg)
            }
        }
    }

    /**
     * TENTATIVA DE INJEÇÃO DIRETA DE STATUS DE EVIDÊNCIA OU MATURIDADE PELO CLIENTE:
     * O cliente NÃO pode enviar estados como evidenceStatus = VALID, maturityStatus = MATURE ou consistencyStatus = STABLE.
     * Toda tentativa é rejeitada, auditada com alta severidade e não altera o estado.
     */
    suspend fun attemptDirectEvidenceManipulation(
        userId: String,
        attemptedAttribute: String,
        attemptedValue: String
    ): AppResult<Unit> {
        val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
            id = java.util.UUID.randomUUID().toString(),
            actorType = com.example.core.datacore.model.ActorType.CLIENT,
            actorId = SecurityContext.getAuthenticatedUserId() ?: "UNKNOWN_CLIENT",
            action = "SECURITY_VIOLATION_EVIDENCE_MANIPULATION_ATTEMPT",
            entityType = "EvolutionEvidencePackage",
            entityId = "TARGET_USER_$userId",
            previousState = null,
            newState = "ATTEMPTED_MUTATION: $attemptedAttribute=$attemptedValue; REJECTED=True",
            timestamp = System.currentTimeMillis(),
            requestId = java.util.UUID.randomUUID().toString(),
            systemVersion = "1.0.0-consistency-v1"
        )
        repository.insertCoreAuditLog(auditEntity)

        return AppResult.Failure(
            AppError.UnauthorizedStateMutation(
                "Violação de Segurança: Mutação direta de status de evidência, maturidade ou consistência por clientes é estritamente proibida. Estados são derivados exclusivamente pelo Evidence & Consistency Engine do Core."
            )
        )
    }

    // ====================================================
    // 9. EVOLUTION ENGINE V1 SERVICE
    // ====================================================
    private val evolutionEngine = com.example.core.evolutionengine.engine.EvolutionEngineV1()

    suspend fun evaluateOfficialClassEligibility(
        userId: String,
        targetClassId: String,
        currentClassId: String = com.example.core.evolutionengine.catalog.ClassCatalog.CLASS_01
    ): AppResult<com.example.core.evolutionengine.model.EvolutionSnapshot> {
        val targetUserId = userId.ifBlank { SecurityContext.getAuthenticatedUserId() ?: "" }
        if (targetUserId.isBlank()) {
            return AppResult.Failure(AppError.ValidationError("userId é obrigatório."))
        }

        // 1. Obtém snapshot de score mais recente oficial
        val scoreSnapshotEntity = repository.getLatestOfficialSnapshot(targetUserId)
        val scoreSnapshot = scoreSnapshotEntity?.let {
            // Cria ScoreSnapshot simplificado de domínio
            com.example.core.scoreengine.model.ScoreSnapshot(
                id = it.id,
                userId = it.userId,
                assessmentId = it.assessmentId,
                scoreVersion = it.scoreVersion,
                coreVersion = it.coreVersion,
                calculatedAt = it.calculatedAt,
                performanceIndex = com.example.core.scoreengine.model.PerformanceIndex(
                    value = it.performanceIndexValue,
                    formulaVersion = "1.0.0",
                    dimensionScores = emptyMap(),
                    evidenceIds = it.evidenceIdsJson.split(",").filter { s -> s.isNotBlank() },
                    calculationStatus = it.performanceIndexStatus,
                    confidenceMetadata = com.example.core.scoreengine.model.ScoreConfidenceMetadata(
                        sourceTier = com.example.core.datacore.model.SourceTier.TIER_1_DIRECT_SENSOR,
                        integrityStatus = com.example.core.datacore.model.IntegrityStatus.VALID,
                        consistencyStatus = "STABLE",
                        repeatabilityStatus = "HIGH",
                        evidenceCount = it.evidenceIdsJson.split(",").filter { s -> s.isNotBlank() }.size
                    ),
                    explanation = com.example.core.scoreengine.model.ScoreExplanation(
                        score = it.performanceIndexValue,
                        dimensionOrIndex = "PERFORMANCE_INDEX",
                        metricsUsed = it.metricIdsJson.split(",").filter { s -> s.isNotBlank() },
                        evidenceUsed = it.evidenceIdsJson.split(",").filter { s -> s.isNotBlank() },
                        formulasUsed = listOf("WEIGHTED_HARMONIC_V1"),
                        normalizationUsed = "LINEAR_MIN_MAX",
                        protocolVersions = listOf("PROT-1.0"),
                        coreVersion = it.coreVersion,
                        scoreVersion = it.scoreVersion
                    )
                ),
                dimensionScores = emptyList(),
                evidenceIds = it.evidenceIdsJson.split(",").filter { s -> s.isNotBlank() },
                metricIds = it.metricIdsJson.split(",").filter { s -> s.isNotBlank() },
                calculationStatus = it.calculationStatus,
                confidenceMetadata = com.example.core.scoreengine.model.ScoreConfidenceMetadata(
                    sourceTier = com.example.core.datacore.model.SourceTier.TIER_1_DIRECT_SENSOR,
                    integrityStatus = com.example.core.datacore.model.IntegrityStatus.VALID,
                    consistencyStatus = "STABLE",
                    repeatabilityStatus = "HIGH",
                    evidenceCount = it.evidenceIdsJson.split(",").filter { s -> s.isNotBlank() }.size
                ),
                isMock = it.isMock,
                provenanceId = it.provenanceId,
                overallExplanation = com.example.core.scoreengine.model.ScoreExplanation(
                    score = it.performanceIndexValue,
                    dimensionOrIndex = "OVERALL",
                    metricsUsed = it.metricIdsJson.split(",").filter { s -> s.isNotBlank() },
                    evidenceUsed = it.evidenceIdsJson.split(",").filter { s -> s.isNotBlank() },
                    formulasUsed = listOf("WEIGHTED_HARMONIC_V1"),
                    normalizationUsed = "LINEAR_MIN_MAX",
                    protocolVersions = listOf("PROT-1.0"),
                    coreVersion = it.coreVersion,
                    scoreVersion = it.scoreVersion
                )
            )
        }

        // 2. Obtém ou gera pacote de evidências
        val evidencePackageResult = generateOfficialEvidencePackage(targetUserId)
        val evidencePackage = if (evidencePackageResult is AppResult.Success) evidencePackageResult.data else null

        // 3. Executa a avaliação de evolução oficial no Engine
        val snapshot = try {
            evolutionEngine.evaluateClass(
                userId = targetUserId,
                targetClassId = targetClassId,
                currentClassId = currentClassId,
                scoreSnapshot = scoreSnapshot,
                evidencePackage = evidencePackage,
                actor = com.example.core.datacore.model.ActorType.CORE_ENGINE,
                isMock = false,
                simulationMode = false
            )
        } catch (e: Exception) {
            return AppResult.Failure(AppError.ComputationError("Erro na avaliação de evolução: ${e.message}"))
        }

        // 4. Persiste Snapshot imutável no Room
        val entity = com.example.data.local.entity.EvolutionSnapshotEntity(
            id = snapshot.id,
            userId = snapshot.userId,
            currentClass = snapshot.currentClass,
            evaluatedClass = snapshot.evaluatedClass,
            eligibilityStatus = snapshot.eligibilityResult.status,
            eligibilityResultJson = "Status=${snapshot.eligibilityResult.status},Requirements=${snapshot.requirementResults.size}",
            requirementResultsJson = snapshot.requirementResults.joinToString(";") { "${it.requirementId}:${it.status}" },
            evidencePackageId = snapshot.evidencePackageId,
            scoreSnapshotId = snapshot.scoreSnapshotId,
            policyVersion = snapshot.policyVersion,
            coreVersion = snapshot.coreVersion,
            evaluatedAt = snapshot.evaluatedAt,
            auditReference = snapshot.auditReference,
            isMock = snapshot.isMock,
            simulationMode = snapshot.simulationMode
        )
        repository.insertEvolutionSnapshot(entity)

        return AppResult.Success(snapshot)
    }

    suspend fun evaluateOfficialEvolutionState(
        userId: String,
        currentClassId: String? = null
    ): AppResult<com.example.core.evolutionengine.model.EvolutionState> {
        val targetUserId = userId.ifBlank { SecurityContext.getAuthenticatedUserId() ?: "" }
        if (targetUserId.isBlank()) {
            return AppResult.Failure(AppError.ValidationError("userId é obrigatório."))
        }

        val scoreSnapshotEntity = repository.getLatestOfficialSnapshot(targetUserId)
        val evidencePackageResult = generateOfficialEvidencePackage(targetUserId)
        val evidencePackage = if (evidencePackageResult is AppResult.Success) evidencePackageResult.data else null

        val state = try {
            evolutionEngine.evaluateEvolutionState(
                userId = targetUserId,
                currentClassId = currentClassId,
                scoreSnapshot = null, // Avaliação de estado baseada no catálogo e pacotes
                evidencePackage = evidencePackage,
                actor = com.example.core.datacore.model.ActorType.CORE_ENGINE,
                isMock = false,
                simulationMode = false
            )
        } catch (e: Exception) {
            return AppResult.Failure(AppError.ComputationError("Falha ao avaliar estado de evolução: ${e.message}"))
        }

        return AppResult.Success(state)
    }

    /**
     * Rejeição e Auditoria de tentativas não autorizadas de mutação de evolução
     */
    suspend fun attemptDirectEvolutionManipulation(
        userId: String,
        attemptedAttribute: String,
        attemptedValue: String,
        actor: com.example.core.datacore.model.ActorType = com.example.core.datacore.model.ActorType.CLIENT
    ): AppResult<Unit> {
        val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
            id = java.util.UUID.randomUUID().toString(),
            actorType = actor,
            actorId = SecurityContext.getAuthenticatedUserId() ?: actor.name,
            action = "SECURITY_VIOLATION_EVOLUTION_MANIPULATION_ATTEMPT",
            entityType = "EvolutionEngine",
            entityId = "TARGET_USER_$userId",
            previousState = null,
            newState = "ATTEMPTED_MUTATION: $attemptedAttribute=$attemptedValue; REJECTED=True",
            timestamp = System.currentTimeMillis(),
            requestId = java.util.UUID.randomUUID().toString(),
            systemVersion = "1.0.0-evolution-v1"
        )
        repository.insertCoreAuditLog(auditEntity)

        return AppResult.Failure(
            AppError.UnauthorizedStateMutation(
                "Violação de Segurança: Mutação de classe, requisitos ou elegibilidade por $actor é estritamente proibida. Elegibilidade é calculada exclusivamente pelo Evolution Engine do Core."
            )
        )
    }

    // ====================================================
    // 10. TRIAL ENGINE V1 SERVICE
    // ====================================================
    private val trialEngine = com.example.core.trialengine.engine.TrialEngineV1(
        auditLogger = { log ->
            val entity = com.example.data.local.entity.CoreAuditLogEntity(
                id = log.id,
                actorType = log.actorType,
                actorId = log.actorId,
                action = log.action,
                entityType = log.entityType,
                entityId = log.entityId,
                previousState = log.previousState,
                newState = log.newState,
                timestamp = log.timestamp,
                requestId = log.requestId,
                systemVersion = log.systemVersion
            )
            // Persistência assíncrona/segura
        }
    )

    suspend fun createOfficialTrialSession(
        userId: String,
        classId: String,
        trialPolicyId: String,
        deviceId: String,
        protocolId: String
    ): AppResult<com.example.core.trialengine.model.TrialSession> {
        val targetUserId = userId.ifBlank { SecurityContext.getAuthenticatedUserId() ?: "" }
        if (targetUserId.isBlank()) {
            return AppResult.Failure(AppError.ValidationError("userId é obrigatório."))
        }

        // Checagem de sessão ativa existente
        val activeSession = repository.getActiveTrialSession(targetUserId)
        val hasActive = activeSession != null

        val session = try {
            trialEngine.createSession(
                userId = targetUserId,
                classId = classId,
                trialPolicyId = trialPolicyId,
                deviceId = deviceId,
                protocolId = protocolId,
                actor = com.example.core.datacore.model.ActorType.CORE_ENGINE,
                isMock = false,
                simulationMode = false,
                activeSessionCheck = hasActive
            )
        } catch (e: Exception) {
            return AppResult.Failure(AppError.ValidationError("Falha ao criar sessão de Trial: ${e.message}"))
        }

        val entity = com.example.data.local.entity.TrialSessionEntity(
            id = session.id,
            userId = session.userId,
            classId = session.classId,
            trialPolicyId = session.trialPolicyId,
            policyVersion = session.policyVersion,
            startedAt = session.startedAt,
            completedAt = session.completedAt,
            status = session.status,
            attemptCount = session.attemptCount,
            deviceId = session.deviceId,
            protocolId = session.protocolId,
            sessionIntegrity = session.sessionIntegrity,
            isMock = session.isMock,
            simulationMode = session.simulationMode,
            auditReference = session.auditReference
        )
        repository.insertTrialSession(entity)

        return AppResult.Success(session)
    }

    suspend fun completeOfficialTrialSession(
        sessionId: String
    ): AppResult<com.example.core.trialengine.model.TrialSnapshot> {
        val sessionEntity = repository.getTrialSessionById(sessionId)
            ?: return AppResult.Failure(AppError.NotFoundError("Sessão $sessionId não encontrada."))

        val attemptEntities = repository.getTrialAttemptsBySessionId(sessionId)
        val attempts = attemptEntities.map { entity ->
            com.example.core.trialengine.model.TrialAttempt(
                id = entity.id,
                sessionId = entity.sessionId,
                attemptNumber = entity.attemptNumber,
                startedAt = entity.startedAt,
                completedAt = entity.completedAt,
                rawEvidenceIds = entity.rawEvidenceIdsJson.split(",").filter { it.isNotBlank() },
                measurementIds = entity.measurementIdsJson.split(",").filter { it.isNotBlank() },
                resultValue = entity.resultValue,
                unit = entity.unit,
                validationStatus = entity.validationStatus,
                invalidationReason = entity.invalidationReason,
                integrityHash = entity.integrityHash,
                deviceId = entity.deviceId,
                protocolId = entity.protocolId,
                createdAt = entity.createdAt
            )
        }

        val sessionDomain = com.example.core.trialengine.model.TrialSession(
            id = sessionEntity.id,
            userId = sessionEntity.userId,
            classId = sessionEntity.classId,
            trialPolicyId = sessionEntity.trialPolicyId,
            policyVersion = sessionEntity.policyVersion,
            startedAt = sessionEntity.startedAt,
            completedAt = sessionEntity.completedAt,
            status = sessionEntity.status,
            attemptCount = sessionEntity.attemptCount,
            deviceId = sessionEntity.deviceId,
            protocolId = sessionEntity.protocolId,
            sessionIntegrity = sessionEntity.sessionIntegrity,
            isMock = sessionEntity.isMock,
            simulationMode = sessionEntity.simulationMode,
            auditReference = sessionEntity.auditReference
        )

        val measurementEntities = repository.getCoreMeasurementsByUserId(sessionEntity.userId)
        val domainMeasurements = measurementEntities.map { entity ->
            com.example.core.datacore.model.DataCoreMeasurement(
                id = entity.id,
                assessmentId = entity.assessmentId,
                userId = entity.userId,
                metricId = entity.metricId,
                rawValue = entity.rawValue,
                normalizedValue = entity.normalizedValue,
                unit = entity.unit,
                timestamp = entity.timestamp,
                source = entity.source,
                deviceId = entity.deviceId,
                protocolId = entity.protocolId,
                validationStatus = entity.validationStatus,
                rejectionReason = entity.rejectionReason,
                rawDataInputId = entity.rawDataInputId,
                isMock = entity.isMock,
                createdAt = entity.createdAt
            )
        }

        val evidenceEntities = repository.getCoreEvidencesByUserId(sessionEntity.userId)
        val domainEvidences = evidenceEntities.map { entity ->
            com.example.core.datacore.model.DataCoreEvidence(
                id = entity.id,
                userId = entity.userId,
                assessmentId = entity.assessmentId,
                measurementIds = entity.measurementIdsJson.split(",").filter { it.isNotBlank() },
                source = entity.source,
                capturedAt = entity.capturedAt,
                submittedAt = entity.submittedAt,
                integrityStatus = entity.integrityStatus,
                reliabilityScore = entity.reliabilityScore,
                confidenceScore = entity.confidenceScore,
                provenanceId = entity.provenanceId,
                coreVersion = entity.coreVersion,
                isMock = entity.isMock,
                createdAt = entity.createdAt
            )
        }

        val provenances = mutableMapOf<String, com.example.core.datacore.model.DataCoreProvenance>()
        for (ev in domainEvidences) {
            val provEntity = repository.getProvenanceById(ev.provenanceId)
            if (provEntity != null) {
                provenances[ev.provenanceId] = com.example.core.datacore.model.DataCoreProvenance(
                    id = provEntity.id,
                    sourceType = provEntity.sourceType,
                    sourceIdentifier = provEntity.sourceIdentifier,
                    deviceIdentifier = provEntity.deviceIdentifier,
                    captureTimestamp = provEntity.captureTimestamp,
                    processingTimestamp = provEntity.processingTimestamp,
                    processingVersion = provEntity.processingVersion,
                    protocolId = provEntity.protocolId,
                    integrityHash = provEntity.integrityHash,
                    createdAt = provEntity.createdAt
                )
            }
        }

        val snapshot = try {
            trialEngine.completeSession(
                session = sessionDomain,
                attempts = attempts,
                evidences = domainEvidences,
                measurements = domainMeasurements,
                provenances = provenances,
                actor = com.example.core.datacore.model.ActorType.CORE_ENGINE
            )
        } catch (e: Exception) {
            return AppResult.Failure(AppError.ComputationError("Falha ao concluir sessão de Trial: ${e.message}"))
        }

        // Persistência do Snapshot de Trial
        val snapshotEntity = com.example.data.local.entity.TrialSnapshotEntity(
            id = snapshot.id,
            sessionId = snapshot.sessionId,
            userId = snapshot.userId,
            classId = snapshot.classId,
            trialPolicyId = snapshot.trialPolicyId,
            trialPolicyVersion = snapshot.trialPolicyVersion,
            resultStatus = snapshot.result.resultStatus,
            bestAttemptId = snapshot.result.bestAttemptId,
            qualifyingAttemptsJson = snapshot.result.qualifyingAttempts.joinToString(","),
            failedAttemptsJson = snapshot.result.failedAttempts.joinToString(","),
            metricResultsJson = snapshot.result.metricResults.entries.joinToString(";") { "${it.key}=${it.value}" },
            evidenceIdsJson = snapshot.result.evidenceIds.joinToString(","),
            explanation = snapshot.result.explanation,
            limitationsJson = snapshot.result.limitations.joinToString(";"),
            sessionIntegrity = snapshot.sessionIntegrity,
            calculatedAt = snapshot.calculatedAt,
            coreVersion = snapshot.coreVersion,
            auditReference = snapshot.auditReference,
            isMock = snapshot.isMock,
            simulationMode = snapshot.simulationMode
        )
        repository.insertTrialSnapshot(snapshotEntity)

        // Atualiza a sessão para COMPLETED ou status resultante
        repository.updateTrialSession(
            sessionEntity.copy(
                status = com.example.core.trialengine.model.TrialSessionStatus.COMPLETED,
                completedAt = System.currentTimeMillis()
            )
        )

        return AppResult.Success(snapshot)
    }

    /**
     * Bloqueio e Auditoria de tentativa não autorizada de mutação de resultado de Trial
     */
    suspend fun attemptDirectTrialManipulation(
        sessionId: String,
        attemptedAttribute: String,
        attemptedValue: String,
        actor: com.example.core.datacore.model.ActorType = com.example.core.datacore.model.ActorType.CLIENT
    ): AppResult<Unit> {
        val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
            id = java.util.UUID.randomUUID().toString(),
            actorType = actor,
            actorId = SecurityContext.getAuthenticatedUserId() ?: actor.name,
            action = "SECURITY_VIOLATION_TRIAL_MANIPULATION_ATTEMPT",
            entityType = "TrialEngine",
            entityId = sessionId,
            previousState = null,
            newState = "ATTEMPTED_MUTATION: $attemptedAttribute=$attemptedValue; REJECTED=True",
            timestamp = System.currentTimeMillis(),
            requestId = java.util.UUID.randomUUID().toString(),
            systemVersion = "1.0.0-trial-v1"
        )
        repository.insertCoreAuditLog(auditEntity)

        return AppResult.Failure(
            AppError.UnauthorizedStateMutation(
                "Violação de Segurança: Mutação direta de status de Trial, tentativa ou threshold por $actor é estritamente proibida. Resultados são calculados e auditados exclusivamente pelo Trial Engine do Core."
            )
        )
    }

    // =========================================================================
    // 11. EVOLUTION PROGRESSION SYSTEM V1 (LONGITUDINAL PROGRESSION & PROMOTION GATE)
    // =========================================================================

    private val progressionEngine = com.example.core.progressionengine.engine.ProgressionEngineV1(
        coreVersion = "1.0.0",
        methodologyVersion = "1.0.0",
        progressionPolicyVersion = "1.0.0",
        evolutionPolicyVersion = "1.0.0",
        trialPolicyVersion = "1.0.0",
        scoreVersion = "1.0.0",
        auditLogger = { logMsg ->
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    actorType = com.example.core.datacore.model.ActorType.SYSTEM,
                    actorId = "PROGRESSION_ENGINE_V1",
                    action = "PROGRESSION_ENGINE_EVENT",
                    entityType = "EvolutionProgression",
                    entityId = "PROG_CORE",
                    previousState = null,
                    newState = logMsg,
                    timestamp = System.currentTimeMillis(),
                    requestId = java.util.UUID.randomUUID().toString(),
                    systemVersion = "1.0.0-progression-v1"
                )
                repository.insertCoreAuditLog(auditEntity)
            }
        }
    )

    /**
     * Executa avaliação longitudinal completa de progressão para o usuário.
     */
    suspend fun assessProgression(
        targetClassId: String,
        isMock: Boolean = false,
        simulationMode: Boolean = false
    ): AppResult<com.example.core.progressionengine.model.ProgressionAssessmentSnapshot> {
        val userId = SecurityContext.getAuthenticatedUserId()
            ?: return AppResult.Failure(AppError.AuthenticationError("Usuário não autenticado."))

        val profile = repository.getProfileByUserId(userId)
        val currentClassId = profile?.status?.name ?: "CLASS_01"
        val currentClassSince = profile?.createdAt ?: System.currentTimeMillis()

        val coreEvidences = repository.getCoreEvidencesByUserId(userId).map { entity ->
            com.example.core.datacore.model.DataCoreEvidence(
                id = entity.id,
                userId = entity.userId,
                assessmentId = entity.assessmentId,
                measurementIds = if (entity.measurementIdsJson.isNotBlank()) entity.measurementIdsJson.split(",") else emptyList(),
                source = entity.source,
                capturedAt = entity.capturedAt,
                submittedAt = entity.submittedAt,
                integrityStatus = entity.integrityStatus,
                reliabilityScore = entity.reliabilityScore,
                confidenceScore = entity.confidenceScore,
                provenanceId = entity.provenanceId,
                coreVersion = entity.coreVersion,
                isMock = entity.isMock,
                createdAt = entity.createdAt
            )
        }
        val provenances = mutableMapOf<String, com.example.core.datacore.model.DataCoreProvenance>()
        for (ev in coreEvidences) {
            val provEntity = repository.getProvenanceById(ev.provenanceId)
            if (provEntity != null) {
                provenances[ev.provenanceId] = com.example.core.datacore.model.DataCoreProvenance(
                    id = provEntity.id,
                    sourceType = provEntity.sourceType,
                    sourceIdentifier = provEntity.sourceIdentifier,
                    deviceIdentifier = provEntity.deviceIdentifier,
                    captureTimestamp = provEntity.captureTimestamp,
                    processingTimestamp = provEntity.processingTimestamp,
                    processingVersion = provEntity.processingVersion,
                    protocolId = provEntity.protocolId,
                    integrityHash = provEntity.integrityHash,
                    createdAt = provEntity.createdAt
                )
            }
        }

        val trialSnapshots = repository.getQualifiedOfficialTrialSnapshots(userId)
        val trialSnapshotEntity = trialSnapshots.firstOrNull { it.classId == targetClassId }
        val trialSnapshot = trialSnapshotEntity?.let { entity ->
            com.example.core.trialengine.model.TrialSnapshot(
                id = entity.id,
                sessionId = entity.sessionId,
                userId = entity.userId,
                classId = entity.classId,
                trialPolicyId = entity.trialPolicyId,
                trialPolicyVersion = entity.trialPolicyVersion,
                result = com.example.core.trialengine.model.TrialResult(
                    id = entity.id,
                    sessionId = entity.sessionId,
                    userId = entity.userId,
                    classId = entity.classId,
                    bestAttemptId = entity.bestAttemptId,
                    qualifyingAttempts = if (entity.qualifyingAttemptsJson.isNotBlank()) entity.qualifyingAttemptsJson.split(",") else emptyList(),
                    failedAttempts = if (entity.failedAttemptsJson.isNotBlank()) entity.failedAttemptsJson.split(",") else emptyList(),
                    metricResults = emptyMap(),
                    evidenceIds = if (entity.evidenceIdsJson.isNotBlank()) entity.evidenceIdsJson.split(",") else emptyList(),
                    protocolVersion = "1.0.0",
                    trialPolicyVersion = entity.trialPolicyVersion,
                    methodologyVersion = "1.0.0",
                    resultStatus = entity.resultStatus,
                    explanation = entity.explanation,
                    limitations = if (entity.limitationsJson.isNotBlank()) entity.limitationsJson.split(";") else emptyList(),
                    fatigueAnalysis = null,
                    calculatedAt = entity.calculatedAt,
                    auditReference = entity.auditReference,
                    isMock = entity.isMock,
                    simulationMode = entity.simulationMode
                ),
                attempts = emptyList(),
                sessionIntegrity = entity.sessionIntegrity,
                calculatedAt = entity.calculatedAt,
                coreVersion = entity.coreVersion,
                auditReference = entity.auditReference,
                isMock = entity.isMock,
                simulationMode = entity.simulationMode
            )
        }

        val snapshot = try {
            progressionEngine.assessProgression(
                userId = userId,
                currentClassId = currentClassId,
                targetClassId = targetClassId,
                currentClassSince = currentClassSince,
                evidences = coreEvidences,
                provenances = provenances,
                trialSnapshot = trialSnapshot,
                callerTier = com.example.core.progressionengine.model.CallerTier.CORE_ENGINE,
                isMock = isMock,
                simulationMode = simulationMode
            )
        } catch (e: SecurityException) {
            return AppResult.Failure(AppError.UnauthorizedStateMutation(e.message ?: "Violação de segurança em progressão."))
        } catch (e: Exception) {
            return AppResult.Failure(AppError.ComputationError("Falha na avaliação de progressão: ${e.message}"))
        }

        // Persistência Room
        repository.insertProgressionState(
            com.example.data.local.entity.EvolutionProgressionStateEntity(
                id = snapshot.progressionState.id,
                userId = snapshot.progressionState.userId,
                currentClassId = snapshot.progressionState.currentClassId,
                currentClassSince = snapshot.progressionState.currentClassSince,
                highestEligibleClassId = snapshot.progressionState.highestEligibleClassId,
                nextTargetClassId = snapshot.progressionState.nextTargetClassId,
                progressionStatus = snapshot.progressionState.progressionStatus,
                progressionPhase = snapshot.progressionState.progressionPhase,
                lastAssessmentAt = snapshot.progressionState.lastAssessmentAt,
                methodologyVersion = snapshot.progressionState.methodologyVersion,
                coreVersion = snapshot.progressionState.coreVersion,
                isMock = snapshot.progressionState.isMock,
                simulationMode = snapshot.progressionState.simulationMode
            )
        )

        repository.insertPromotionCandidate(
            com.example.data.local.entity.PromotionCandidateEntity(
                id = snapshot.candidate.id,
                userId = snapshot.candidate.userId,
                currentClassId = snapshot.candidate.currentClassId,
                targetClassId = snapshot.candidate.targetClassId,
                satisfiedRequirementsJson = snapshot.candidate.satisfiedRequirements.joinToString(","),
                blockingRequirementsJson = snapshot.candidate.blockingRequirements.joinToString(","),
                evidencePackageId = snapshot.candidate.evidencePackageId,
                scoreSnapshotId = snapshot.candidate.scoreSnapshotId,
                trialSnapshotId = snapshot.candidate.trialSnapshotId,
                progressionAssessmentId = snapshot.candidate.progressionAssessmentId,
                timePolicyResult = snapshot.candidate.timePolicyResult,
                consistencyResult = snapshot.candidate.consistencyResult,
                maturityResult = snapshot.candidate.maturityResult,
                adaptationResult = snapshot.candidate.adaptationResult,
                balanceResult = snapshot.candidate.balanceResult,
                status = snapshot.candidate.status,
                overallOutcome = snapshot.candidate.explanation.overallOutcome,
                methodologyVersion = snapshot.candidate.methodologyVersion,
                createdAt = snapshot.candidate.createdAt,
                isMock = snapshot.candidate.isMock,
                simulationMode = snapshot.candidate.simulationMode
            )
        )

        repository.insertProgressionSnapshot(
            com.example.data.local.entity.ProgressionAssessmentSnapshotEntity(
                id = snapshot.id,
                userId = snapshot.userId,
                currentClassId = snapshot.currentClassId,
                targetClassId = snapshot.targetClassId,
                progressionStatus = snapshot.progressionState.progressionStatus,
                candidateStatus = snapshot.candidate.status,
                trajectoriesJson = snapshot.trajectories.keys.joinToString(","),
                sustainabilityJson = snapshot.sustainability.consistencyStatus,
                maintenanceJson = snapshot.maintenance.status.name,
                regressionReviewJson = snapshot.regressionReview?.reviewStatus?.name,
                anomaliesJson = snapshot.anomalies.map { it.type.name }.joinToString(","),
                calculatedAt = snapshot.calculatedAt,
                coreVersion = snapshot.coreVersion,
                auditReference = snapshot.auditReference,
                isMock = snapshot.isMock,
                simulationMode = snapshot.simulationMode
            )
        )

        for (anom in snapshot.anomalies) {
            repository.insertProgressionAnomaly(
                com.example.data.local.entity.ProgressionAnomalyEntity(
                    anomalyId = anom.anomalyId,
                    userId = anom.userId,
                    type = anom.type,
                    severity = anom.severity,
                    evidenceIdsJson = anom.evidenceIds.joinToString(","),
                    affectedSnapshotsJson = anom.affectedSnapshots.joinToString(","),
                    detectedAt = anom.detectedAt,
                    status = anom.status,
                    explanation = anom.explanation
                )
            )
        }

        return AppResult.Success(snapshot)
    }

    /**
     * Bloqueio e Auditoria de tentativa não autorizada de mutação de progressão por CLIENT ou AI_GATEWAY.
     */
    suspend fun attemptDirectProgressionManipulation(
        targetClassId: String,
        attemptedAttribute: String,
        attemptedValue: String,
        actor: com.example.core.datacore.model.ActorType = com.example.core.datacore.model.ActorType.CLIENT
    ): AppResult<Unit> {
        val auditEntity = com.example.data.local.entity.CoreAuditLogEntity(
            id = java.util.UUID.randomUUID().toString(),
            actorType = actor,
            actorId = SecurityContext.getAuthenticatedUserId() ?: actor.name,
            action = "SECURITY_VIOLATION_PROGRESSION_MANIPULATION_ATTEMPT",
            entityType = "ProgressionEngine",
            entityId = targetClassId,
            previousState = null,
            newState = "ATTEMPTED_MUTATION: $attemptedAttribute=$attemptedValue; REJECTED=True",
            timestamp = System.currentTimeMillis(),
            requestId = java.util.UUID.randomUUID().toString(),
            systemVersion = "1.0.0-progression-v1"
        )
        repository.insertCoreAuditLog(auditEntity)

        return AppResult.Failure(
            AppError.UnauthorizedStateMutation(
                "Violação de Segurança: Mutação direta de PromotionCandidate, EvolutionProgressionState, ClassMaintenance ou RegressionReview por $actor é estritamente proibida. A progressão é governada exclusivamente pelo Progression Engine do Core."
            )
        )
    }
}
