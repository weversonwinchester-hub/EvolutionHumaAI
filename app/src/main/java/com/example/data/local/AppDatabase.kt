package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AIGatewayDao
import com.example.data.local.dao.AssessmentDao
import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.DataCoreAuditDao
import com.example.data.local.dao.DataCoreEvidenceDao
import com.example.data.local.dao.DataCoreMeasurementDao
import com.example.data.local.dao.EvolutionDao
import com.example.data.local.dao.EvolutionEvidencePackageDao
import com.example.data.local.dao.EvolutionSnapshotDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.MetricDao
import com.example.data.local.dao.MissionTrialDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.EvolutionHistoryDao
import com.example.data.local.dao.ProgressionAnomalyDao
import com.example.data.local.dao.ProgressionSnapshotDao
import com.example.data.local.dao.ProgressionStateDao
import com.example.data.local.dao.PromotionCandidateDao
import com.example.data.local.dao.ProtocolDao
import com.example.data.local.dao.ProvenanceDao
import com.example.data.local.dao.RawDataDao
import com.example.data.local.dao.ScoreSnapshotDao
import com.example.data.local.dao.ScientificAuditLogDao
import com.example.data.local.dao.ScientificMethodologyDao
import com.example.data.local.dao.ScientificProtocolDao
import com.example.data.local.dao.MeasurementInstrumentDao
import com.example.data.local.dao.QualityGateEvaluationDao
import com.example.data.local.dao.ProtocolDeviationDao
import com.example.data.local.dao.PopulationReferenceDao
import com.example.data.local.dao.TrialAttemptDao
import com.example.data.local.dao.TrialSessionDao
import com.example.data.local.dao.TrialSnapshotDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.ExerciseDao
import com.example.data.local.dao.ExerciseAuditDao
import com.example.data.local.entity.AIInteractionEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CoreAuditLogEntity
import com.example.data.local.entity.CoreEvidenceEntity
import com.example.data.local.entity.CoreMeasurementEntity
import com.example.data.local.entity.EvidenceEntity
import com.example.data.local.entity.EvolutionEvidencePackageEntity
import com.example.data.local.entity.EvolutionHistoryEntity
import com.example.data.local.entity.EvolutionProgressionStateEntity
import com.example.data.local.entity.EvolutionSnapshotEntity
import com.example.data.local.entity.EvolutionStateEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.MeasurementEntity
import com.example.data.local.entity.MeasurementInstrumentEntity
import com.example.data.local.entity.MetricEntity
import com.example.data.local.entity.MissionEntity
import com.example.data.local.entity.PerformanceStateEntity
import com.example.data.local.entity.PopulationReferenceEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ProgressionAnomalyEntity
import com.example.data.local.entity.ProgressionAssessmentSnapshotEntity
import com.example.data.local.entity.PromotionCandidateEntity
import com.example.data.local.entity.ProtocolDeviationEntity
import com.example.data.local.entity.ProtocolEntity
import com.example.data.local.entity.ProvenanceEntity
import com.example.data.local.entity.QualityGateEvaluationEntity
import com.example.data.local.entity.RawDataInputEntity
import com.example.data.local.entity.ScientificAuditLogEntity
import com.example.data.local.entity.ScientificMethodologyEntity
import com.example.data.local.entity.ScientificProtocolEntity
import com.example.data.local.entity.ScoreSnapshotEntity
import com.example.data.local.entity.TrialAttemptEntity
import com.example.data.local.entity.TrialEntity
import com.example.data.local.entity.TrialSessionEntity
import com.example.data.local.entity.TrialSnapshotEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.ExerciseVersionEntity
import com.example.data.local.entity.ExerciseAuditLogEntity
import com.example.data.local.entity.ExerciseMediaEntity
import com.example.data.local.entity.ExercisePrescriptionEntity

@Database(
    entities = [
        UserEntity::class,
        ProfileEntity::class,
        GoalEntity::class,
        AssessmentEntity::class,
        MeasurementEntity::class,
        EvidenceEntity::class,
        PerformanceStateEntity::class,
        EvolutionStateEntity::class,
        MissionEntity::class,
        TrialEntity::class,
        AuditLogEntity::class,
        AIInteractionEntity::class,
        // Data Core V1 Entities
        RawDataInputEntity::class,
        ProvenanceEntity::class,
        ProtocolEntity::class,
        MetricEntity::class,
        CoreMeasurementEntity::class,
        CoreEvidenceEntity::class,
        CoreAuditLogEntity::class,
        // Score Engine V1 Entities
        ScoreSnapshotEntity::class,
        // Evidence & Consistency Engine V1 Entities
        EvolutionEvidencePackageEntity::class,
        // Evolution Engine V1 Entities
        EvolutionSnapshotEntity::class,
        // Trial Engine V1 Entities
        TrialSessionEntity::class,
        TrialAttemptEntity::class,
        TrialSnapshotEntity::class,
        // Progression Engine V1 Entities
        EvolutionProgressionStateEntity::class,
        PromotionCandidateEntity::class,
        ProgressionAssessmentSnapshotEntity::class,
        EvolutionHistoryEntity::class,
        ProgressionAnomalyEntity::class,
        // Scientific Methodology & Protocol Registry V1 Entities
        ScientificMethodologyEntity::class,
        ScientificProtocolEntity::class,
        MeasurementInstrumentEntity::class,
        QualityGateEvaluationEntity::class,
        ProtocolDeviationEntity::class,
        PopulationReferenceEntity::class,
        ScientificAuditLogEntity::class,
        // Exercise Engine V1 Entities
        ExerciseEntity::class,
        ExerciseVersionEntity::class,
        ExerciseAuditLogEntity::class,
        ExerciseMediaEntity::class,
        ExercisePrescriptionEntity::class,
        // Training Domain V1 Entities
        com.example.data.local.entity.WorkoutEntity::class,
        com.example.data.local.entity.WorkoutItemEntity::class,
        com.example.data.local.entity.TrainingSessionEntity::class,
        com.example.data.local.entity.SessionExerciseLogEntity::class,
        com.example.data.local.entity.AthleteBaselineEntity::class,
        com.example.data.local.entity.AthleteGoalEntity::class,
        com.example.data.local.entity.TrainingSyncQueueEntity::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun goalDao(): GoalDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun evolutionDao(): EvolutionDao
    abstract fun missionTrialDao(): MissionTrialDao
    abstract fun auditDao(): AuditDao
    abstract fun aiGatewayDao(): AIGatewayDao

    // Training Domain V1 DAOs
    abstract fun workoutDao(): com.example.data.local.dao.WorkoutDao
    abstract fun trainingSessionDao(): com.example.data.local.dao.TrainingSessionDao
    abstract fun athleteBaselineDao(): com.example.data.local.dao.AthleteBaselineDao
    abstract fun athleteGoalDao(): com.example.data.local.dao.AthleteGoalDao
    abstract fun trainingSyncQueueDao(): com.example.data.local.dao.TrainingSyncQueueDao

    // Data Core V1 DAOs
    abstract fun rawDataDao(): RawDataDao
    abstract fun provenanceDao(): ProvenanceDao
    abstract fun dataCoreMeasurementDao(): DataCoreMeasurementDao
    abstract fun dataCoreEvidenceDao(): DataCoreEvidenceDao
    abstract fun dataCoreAuditDao(): DataCoreAuditDao
    abstract fun protocolDao(): ProtocolDao
    abstract fun metricDao(): MetricDao

    // Score Engine V1 DAOs
    abstract fun scoreSnapshotDao(): ScoreSnapshotDao

    // Evidence & Consistency Engine V1 DAOs
    abstract fun evolutionEvidencePackageDao(): EvolutionEvidencePackageDao

    // Evolution Engine V1 DAOs
    abstract fun evolutionSnapshotDao(): EvolutionSnapshotDao

    // Trial Engine V1 DAOs
    abstract fun trialSessionDao(): TrialSessionDao
    abstract fun trialAttemptDao(): TrialAttemptDao
    abstract fun trialSnapshotDao(): TrialSnapshotDao

    // Progression Engine V1 DAOs
    abstract fun progressionStateDao(): ProgressionStateDao
    abstract fun promotionCandidateDao(): PromotionCandidateDao
    abstract fun progressionSnapshotDao(): ProgressionSnapshotDao
    abstract fun evolutionHistoryDao(): EvolutionHistoryDao
    abstract fun progressionAnomalyDao(): ProgressionAnomalyDao

    // Scientific Methodology & Protocol Registry V1 DAOs
    abstract fun scientificMethodologyDao(): ScientificMethodologyDao
    abstract fun scientificProtocolDao(): ScientificProtocolDao
    abstract fun measurementInstrumentDao(): MeasurementInstrumentDao
    abstract fun qualityGateEvaluationDao(): QualityGateEvaluationDao
    abstract fun protocolDeviationDao(): ProtocolDeviationDao
    abstract fun populationReferenceDao(): PopulationReferenceDao
    abstract fun scientificAuditLogDao(): ScientificAuditLogDao

    // Exercise Engine V1 DAOs
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseAuditDao(): ExerciseAuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "performai_evolution_foundation.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
