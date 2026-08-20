package com.example.data.local

import androidx.room.TypeConverter
import com.example.core.datacore.model.ActorType
import com.example.core.datacore.model.IntegrityStatus
import com.example.core.datacore.model.UserStatus
import com.example.core.datacore.model.ValidationStatus
import com.example.core.model.AssessmentStatus
import com.example.core.model.AssessmentType
import com.example.core.model.AuditSeverity
import com.example.core.model.EvidenceType
import com.example.core.model.GoalCategory
import com.example.core.model.GoalStatus
import com.example.core.model.MissionStatus
import com.example.core.model.ProfileStatus
import com.example.core.model.TrialStatus
import com.example.core.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.USER)

    @TypeConverter
    fun fromProfileStatus(status: ProfileStatus): String = status.name

    @TypeConverter
    fun toProfileStatus(value: String): ProfileStatus = runCatching { ProfileStatus.valueOf(value) }.getOrDefault(ProfileStatus.INCOMPLETE)

    @TypeConverter
    fun fromGoalCategory(category: GoalCategory): String = category.name

    @TypeConverter
    fun toGoalCategory(value: String): GoalCategory = runCatching { GoalCategory.valueOf(value) }.getOrDefault(GoalCategory.BIOMECHANICAL)

    @TypeConverter
    fun fromGoalStatus(status: GoalStatus): String = status.name

    @TypeConverter
    fun toGoalStatus(value: String): GoalStatus = runCatching { GoalStatus.valueOf(value) }.getOrDefault(GoalStatus.DRAFT)

    @TypeConverter
    fun fromAssessmentType(type: AssessmentType): String = type.name

    @TypeConverter
    fun toAssessmentType(value: String): AssessmentType = runCatching { AssessmentType.valueOf(value) }.getOrDefault(AssessmentType.INITIAL_FOUNDATION)

    @TypeConverter
    fun fromAssessmentStatus(status: AssessmentStatus): String = status.name

    @TypeConverter
    fun toAssessmentStatus(value: String): AssessmentStatus = runCatching { AssessmentStatus.valueOf(value) }.getOrDefault(AssessmentStatus.NOT_STARTED)

    @TypeConverter
    fun fromEvidenceType(type: EvidenceType): String = type.name

    @TypeConverter
    fun toEvidenceType(value: String): EvidenceType = runCatching { EvidenceType.valueOf(value) }.getOrDefault(EvidenceType.BIOMETRIC_DATA)

    @TypeConverter
    fun fromMissionStatus(status: MissionStatus): String = status.name

    @TypeConverter
    fun toMissionStatus(value: String): MissionStatus = runCatching { MissionStatus.valueOf(value) }.getOrDefault(MissionStatus.LOCKED)

    @TypeConverter
    fun fromTrialStatus(status: TrialStatus): String = status.name

    @TypeConverter
    fun toTrialStatus(value: String): TrialStatus = runCatching { TrialStatus.valueOf(value) }.getOrDefault(TrialStatus.LOCKED)

    @TypeConverter
    fun fromAuditSeverity(severity: AuditSeverity): String = severity.name

    @TypeConverter
    fun toAuditSeverity(value: String): AuditSeverity = runCatching { AuditSeverity.valueOf(value) }.getOrDefault(AuditSeverity.INFO)

    // Data Core V1 Converters
    @TypeConverter
    fun fromValidationStatus(status: ValidationStatus): String = status.name

    @TypeConverter
    fun toValidationStatus(value: String): ValidationStatus = runCatching { ValidationStatus.valueOf(value) }.getOrDefault(ValidationStatus.PENDING)

    @TypeConverter
    fun fromIntegrityStatus(status: IntegrityStatus): String = status.name

    @TypeConverter
    fun toIntegrityStatus(value: String): IntegrityStatus = runCatching { IntegrityStatus.valueOf(value) }.getOrDefault(IntegrityStatus.UNKNOWN)

    @TypeConverter
    fun fromActorType(actorType: ActorType): String = actorType.name

    @TypeConverter
    fun toActorType(value: String): ActorType = runCatching { ActorType.valueOf(value) }.getOrDefault(ActorType.SYSTEM)

    @TypeConverter
    fun fromUserStatus(status: UserStatus): String = status.name

    @TypeConverter
    fun toUserStatus(value: String): UserStatus = runCatching { UserStatus.valueOf(value) }.getOrDefault(UserStatus.ACTIVE)

    // Score Engine V1 Converters
    @TypeConverter
    fun fromCalculationStatus(status: com.example.core.scoreengine.model.CalculationStatus): String = status.name

    @TypeConverter
    fun toCalculationStatus(value: String): com.example.core.scoreengine.model.CalculationStatus =
        runCatching { com.example.core.scoreengine.model.CalculationStatus.valueOf(value) }.getOrDefault(com.example.core.scoreengine.model.CalculationStatus.PENDING_VALIDATION)

    // Evidence & Consistency Engine V1 Converters
    @TypeConverter
    fun fromValidityStatus(status: com.example.core.evidenceconsistency.model.ValidityStatus): String = status.name

    @TypeConverter
    fun toValidityStatus(value: String): com.example.core.evidenceconsistency.model.ValidityStatus =
        runCatching { com.example.core.evidenceconsistency.model.ValidityStatus.valueOf(value) }.getOrDefault(com.example.core.evidenceconsistency.model.ValidityStatus.PENDING_VALIDATION)

    @TypeConverter
    fun fromConsistencyStatus(status: com.example.core.evidenceconsistency.model.ConsistencyStatus): String = status.name

    @TypeConverter
    fun toConsistencyStatus(value: String): com.example.core.evidenceconsistency.model.ConsistencyStatus =
        runCatching { com.example.core.evidenceconsistency.model.ConsistencyStatus.valueOf(value) }.getOrDefault(com.example.core.evidenceconsistency.model.ConsistencyStatus.PENDING_VALIDATION)

    @TypeConverter
    fun fromMaturityStatus(status: com.example.core.evidenceconsistency.model.MaturityStatus): String = status.name

    @TypeConverter
    fun toMaturityStatus(value: String): com.example.core.evidenceconsistency.model.MaturityStatus =
        runCatching { com.example.core.evidenceconsistency.model.MaturityStatus.valueOf(value) }.getOrDefault(com.example.core.evidenceconsistency.model.MaturityStatus.PENDING_VALIDATION)

    // Evolution Engine V1 Converters
    @TypeConverter
    fun fromClassEligibilityStatus(status: com.example.core.evolutionengine.model.ClassEligibilityStatus): String = status.name

    @TypeConverter
    fun toClassEligibilityStatus(value: String): com.example.core.evolutionengine.model.ClassEligibilityStatus =
        runCatching { com.example.core.evolutionengine.model.ClassEligibilityStatus.valueOf(value) }.getOrDefault(com.example.core.evolutionengine.model.ClassEligibilityStatus.PENDING_VALIDATION)

    // Trial Engine V1 Converters
    @TypeConverter
    fun fromTrialSessionStatus(status: com.example.core.trialengine.model.TrialSessionStatus): String = status.name

    @TypeConverter
    fun toTrialSessionStatus(value: String): com.example.core.trialengine.model.TrialSessionStatus =
        runCatching { com.example.core.trialengine.model.TrialSessionStatus.valueOf(value) }.getOrDefault(com.example.core.trialengine.model.TrialSessionStatus.CREATED)

    @TypeConverter
    fun fromTrialAttemptValidationStatus(status: com.example.core.trialengine.model.TrialAttemptValidationStatus): String = status.name

    @TypeConverter
    fun toTrialAttemptValidationStatus(value: String): com.example.core.trialengine.model.TrialAttemptValidationStatus =
        runCatching { com.example.core.trialengine.model.TrialAttemptValidationStatus.valueOf(value) }.getOrDefault(com.example.core.trialengine.model.TrialAttemptValidationStatus.PENDING)

    @TypeConverter
    fun fromTrialResultStatus(status: com.example.core.trialengine.model.TrialResultStatus): String = status.name

    @TypeConverter
    fun toTrialResultStatus(value: String): com.example.core.trialengine.model.TrialResultStatus =
        runCatching { com.example.core.trialengine.model.TrialResultStatus.valueOf(value) }.getOrDefault(com.example.core.trialengine.model.TrialResultStatus.PENDING_VALIDATION)

    // Progression Engine V1 Converters
    @TypeConverter
    fun fromEvolutionProgressionStatus(status: com.example.core.progressionengine.model.EvolutionProgressionStatus): String = status.name

    @TypeConverter
    fun toEvolutionProgressionStatus(value: String): com.example.core.progressionengine.model.EvolutionProgressionStatus =
        runCatching { com.example.core.progressionengine.model.EvolutionProgressionStatus.valueOf(value) }.getOrDefault(com.example.core.progressionengine.model.EvolutionProgressionStatus.PENDING_VALIDATION)

    @TypeConverter
    fun fromPromotionCandidateStatus(status: com.example.core.progressionengine.model.PromotionCandidateStatus): String = status.name

    @TypeConverter
    fun toPromotionCandidateStatus(value: String): com.example.core.progressionengine.model.PromotionCandidateStatus =
        runCatching { com.example.core.progressionengine.model.PromotionCandidateStatus.valueOf(value) }.getOrDefault(com.example.core.progressionengine.model.PromotionCandidateStatus.PENDING_VALIDATION)

    @TypeConverter
    fun fromProgressionAnomalyType(type: com.example.core.progressionengine.model.ProgressionAnomalyType): String = type.name

    @TypeConverter
    fun toProgressionAnomalyType(value: String): com.example.core.progressionengine.model.ProgressionAnomalyType =
        runCatching { com.example.core.progressionengine.model.ProgressionAnomalyType.valueOf(value) }.getOrDefault(com.example.core.progressionengine.model.ProgressionAnomalyType.INSUFFICIENT_LONGITUDINAL_DATA)

    @TypeConverter
    fun fromAnomalySeverity(severity: com.example.core.progressionengine.model.AnomalySeverity): String = severity.name

    @TypeConverter
    fun toAnomalySeverity(value: String): com.example.core.progressionengine.model.AnomalySeverity =
        runCatching { com.example.core.progressionengine.model.AnomalySeverity.valueOf(value) }.getOrDefault(com.example.core.progressionengine.model.AnomalySeverity.LOW)

    // Scientific Methodology & Protocol Registry V1 Converters
    @TypeConverter
    fun fromMethodologyValidationStatus(status: com.example.core.scientific.model.MethodologyValidationStatus): String = status.name

    @TypeConverter
    fun toMethodologyValidationStatus(value: String): com.example.core.scientific.model.MethodologyValidationStatus =
        runCatching { com.example.core.scientific.model.MethodologyValidationStatus.valueOf(value) }.getOrDefault(com.example.core.scientific.model.MethodologyValidationStatus.DRAFT)

    @TypeConverter
    fun fromProtocolValidationStatus(status: com.example.core.scientific.model.ProtocolValidationStatus): String = status.name

    @TypeConverter
    fun toProtocolValidationStatus(value: String): com.example.core.scientific.model.ProtocolValidationStatus =
        runCatching { com.example.core.scientific.model.ProtocolValidationStatus.valueOf(value) }.getOrDefault(com.example.core.scientific.model.ProtocolValidationStatus.DRAFT)

    @TypeConverter
    fun fromInstrumentValidationStatus(status: com.example.core.scientific.model.InstrumentValidationStatus): String = status.name

    @TypeConverter
    fun toInstrumentValidationStatus(value: String): com.example.core.scientific.model.InstrumentValidationStatus =
        runCatching { com.example.core.scientific.model.InstrumentValidationStatus.valueOf(value) }.getOrDefault(com.example.core.scientific.model.InstrumentValidationStatus.PENDING_VALIDATION)

    @TypeConverter
    fun fromEvidenceLevel(level: com.example.core.scientific.model.EvidenceLevel): String = level.name

    @TypeConverter
    fun toEvidenceLevel(value: String): com.example.core.scientific.model.EvidenceLevel =
        runCatching { com.example.core.scientific.model.EvidenceLevel.valueOf(value) }.getOrDefault(com.example.core.scientific.model.EvidenceLevel.EVIDENCE_LEVEL_UNSPECIFIED)

    @TypeConverter
    fun fromDeviationSeverity(severity: com.example.core.scientific.model.DeviationSeverity): String = severity.name

    @TypeConverter
    fun toDeviationSeverity(value: String): com.example.core.scientific.model.DeviationSeverity =
        runCatching { com.example.core.scientific.model.DeviationSeverity.valueOf(value) }.getOrDefault(com.example.core.scientific.model.DeviationSeverity.NONE)

    @TypeConverter
    fun fromQualityGateStatus(status: com.example.core.scientific.model.QualityGateStatus): String = status.name

    @TypeConverter
    fun toQualityGateStatus(value: String): com.example.core.scientific.model.QualityGateStatus =
        runCatching { com.example.core.scientific.model.QualityGateStatus.valueOf(value) }.getOrDefault(com.example.core.scientific.model.QualityGateStatus.PENDING_VALIDATION)

    @TypeConverter
    fun fromScientificCallerTier(tier: com.example.core.scientific.model.ScientificCallerTier): String = tier.name

    @TypeConverter
    fun toScientificCallerTier(value: String): com.example.core.scientific.model.ScientificCallerTier =
        runCatching { com.example.core.scientific.model.ScientificCallerTier.valueOf(value) }.getOrDefault(com.example.core.scientific.model.ScientificCallerTier.CLIENT)

    @TypeConverter
    fun fromStringList(list: List<String>?): String = list?.joinToString("|||") ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split("|||").filter { it.isNotEmpty() }

    // Exercise Engine V1 Converters
    @TypeConverter
    fun fromExerciseCategory(category: com.example.core.exerciseengine.model.ExerciseCategory): String = category.name

    @TypeConverter
    fun toExerciseCategory(value: String): com.example.core.exerciseengine.model.ExerciseCategory =
        runCatching { com.example.core.exerciseengine.model.ExerciseCategory.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.model.ExerciseCategory.STRENGTH)

    @TypeConverter
    fun fromMovementPattern(pattern: com.example.core.exerciseengine.model.MovementPattern): String = pattern.name

    @TypeConverter
    fun toMovementPattern(value: String): com.example.core.exerciseengine.model.MovementPattern =
        runCatching { com.example.core.exerciseengine.model.MovementPattern.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.model.MovementPattern.OTHER)

    @TypeConverter
    fun fromExerciseDifficulty(difficulty: com.example.core.exerciseengine.model.ExerciseDifficulty): String = difficulty.name

    @TypeConverter
    fun toExerciseDifficulty(value: String): com.example.core.exerciseengine.model.ExerciseDifficulty =
        runCatching { com.example.core.exerciseengine.model.ExerciseDifficulty.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.model.ExerciseDifficulty.BEGINNER)

    @TypeConverter
    fun fromExecutionType(type: com.example.core.exerciseengine.model.ExecutionType): String = type.name

    @TypeConverter
    fun toExecutionType(value: String): com.example.core.exerciseengine.model.ExecutionType =
        runCatching { com.example.core.exerciseengine.model.ExecutionType.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.model.ExecutionType.REPETITION)

    @TypeConverter
    fun fromLaterality(laterality: com.example.core.exerciseengine.model.Laterality): String = laterality.name

    @TypeConverter
    fun toLaterality(value: String): com.example.core.exerciseengine.model.Laterality =
        runCatching { com.example.core.exerciseengine.model.Laterality.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.model.Laterality.BILATERAL)

    @TypeConverter
    fun fromExerciseStatus(status: com.example.core.exerciseengine.model.ExerciseStatus): String = status.name

    @TypeConverter
    fun toExerciseStatus(value: String): com.example.core.exerciseengine.model.ExerciseStatus =
        runCatching { com.example.core.exerciseengine.model.ExerciseStatus.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.model.ExerciseStatus.ACTIVE)

    @TypeConverter
    fun fromMediaType(type: com.example.core.exerciseengine.model.MediaType): String = type.name

    @TypeConverter
    fun toMediaType(value: String): com.example.core.exerciseengine.model.MediaType =
        runCatching { com.example.core.exerciseengine.model.MediaType.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.model.MediaType.NONE)

    @TypeConverter
    fun fromMediaStatus(status: com.example.core.exerciseengine.model.MediaStatus): String = status.name

    @TypeConverter
    fun toMediaStatus(value: String): com.example.core.exerciseengine.model.MediaStatus =
        runCatching { com.example.core.exerciseengine.model.MediaStatus.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.model.MediaStatus.AVAILABLE)

    @TypeConverter
    fun fromLoadUnit(unit: com.example.core.exerciseengine.prescription.LoadUnit): String = unit.name

    @TypeConverter
    fun toLoadUnit(value: String): com.example.core.exerciseengine.prescription.LoadUnit =
        runCatching { com.example.core.exerciseengine.prescription.LoadUnit.valueOf(value) }.getOrDefault(com.example.core.exerciseengine.prescription.LoadUnit.NOT_SPECIFIED)

    // Training Domain TypeConverters
    @TypeConverter
    fun fromSessionStatus(status: com.example.core.trainingengine.model.SessionStatus): String = status.name

    @TypeConverter
    fun toSessionStatus(value: String): com.example.core.trainingengine.model.SessionStatus =
        runCatching { com.example.core.trainingengine.model.SessionStatus.valueOf(value) }.getOrDefault(com.example.core.trainingengine.model.SessionStatus.NOT_STARTED)

    @TypeConverter
    fun fromExerciseExecutionStatus(status: com.example.core.trainingengine.model.ExerciseExecutionStatus): String = status.name

    @TypeConverter
    fun toExerciseExecutionStatus(value: String): com.example.core.trainingengine.model.ExerciseExecutionStatus =
        runCatching { com.example.core.trainingengine.model.ExerciseExecutionStatus.valueOf(value) }.getOrDefault(com.example.core.trainingengine.model.ExerciseExecutionStatus.PENDING)

    @TypeConverter
    fun fromSyncStatus(status: com.example.core.trainingengine.model.SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): com.example.core.trainingengine.model.SyncStatus =
        runCatching { com.example.core.trainingengine.model.SyncStatus.valueOf(value) }.getOrDefault(com.example.core.trainingengine.model.SyncStatus.LOCAL_ONLY)

    @TypeConverter
    fun fromExperienceLevel(level: com.example.core.trainingengine.baseline.ExperienceLevel): String = level.name

    @TypeConverter
    fun toExperienceLevel(value: String): com.example.core.trainingengine.baseline.ExperienceLevel =
        runCatching { com.example.core.trainingengine.baseline.ExperienceLevel.valueOf(value) }.getOrDefault(com.example.core.trainingengine.baseline.ExperienceLevel.BEGINNER)

    @TypeConverter
    fun fromBaselineVerificationStatus(status: com.example.core.trainingengine.baseline.BaselineVerificationStatus): String = status.name

    @TypeConverter
    fun toBaselineVerificationStatus(value: String): com.example.core.trainingengine.baseline.BaselineVerificationStatus =
        runCatching { com.example.core.trainingengine.baseline.BaselineVerificationStatus.valueOf(value) }.getOrDefault(com.example.core.trainingengine.baseline.BaselineVerificationStatus.SELF_REPORTED)

    @TypeConverter
    fun fromAthleteGoalType(type: com.example.core.trainingengine.baseline.AthleteGoalType): String = type.name

    @TypeConverter
    fun toAthleteGoalType(value: String): com.example.core.trainingengine.baseline.AthleteGoalType =
        runCatching { com.example.core.trainingengine.baseline.AthleteGoalType.valueOf(value) }.getOrDefault(com.example.core.trainingengine.baseline.AthleteGoalType.GENERAL_FITNESS)

    @TypeConverter
    fun fromSyncEntityType(type: com.example.core.sync.SyncEntityType): String = type.name

    @TypeConverter
    fun toSyncEntityType(value: String): com.example.core.sync.SyncEntityType =
        runCatching { com.example.core.sync.SyncEntityType.valueOf(value) }.getOrDefault(com.example.core.sync.SyncEntityType.TRAINING_SESSION)
}
