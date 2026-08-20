package com.example.core.exerciseengine.catalog

import com.example.core.exerciseengine.model.*
import com.example.core.exerciseengine.registry.ExerciseRegistryV1

/**
 * EVOLUTION HUMAN AI — EXERCISE CATALOG V1
 *
 * Catálogo canônico de 20 exercícios fundamentais.
 * Contém metadados biomecânicos, instruções, erros comuns, fases e relações de progressão/regressão.
 */
object ExerciseCatalogV1 {

    val BODYWEIGHT_SQUAT = ExerciseDefinition(
        exerciseId = "EX-SQ-BW-001-V1",
        version = "V1",
        canonicalName = "Bodyweight Squat",
        displayName = "Agachamento Livre (Peso Corporal)",
        description = "Padrão fundamental de flexão de joelho e quadril com peso corporal.",
        category = ExerciseCategory.STRENGTH,
        movementPattern = MovementPattern.SQUAT,
        primaryMuscles = listOf(MuscleGroup.QUADRICEPS, MuscleGroup.GLUTES),
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.CORE, MuscleGroup.CALVES),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.MOTOR_CONTROL, TrainingGoal.HYPERTROPHY, TrainingGoal.JOINT_MOBILITY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        movementPhases = listOf(
            MovementPhaseDefinition(MovementPhase.SETUP, "Posicionamento dos pés na largura dos ombros e ativação do core."),
            MovementPhaseDefinition(MovementPhase.ECCENTRIC, "Descida controlada flexionando quadril e joelhos simultaneamente.", listOf("KNEE", "HIP")),
            MovementPhaseDefinition(MovementPhase.TRANSITION, "Inversão de movimento mantendo alinhamento do tronco no ponto mais profundo."),
            MovementPhaseDefinition(MovementPhase.CONCENTRIC, "Extensão potente de joelhos e quadril retornando à posição vertical.", listOf("KNEE", "HIP")),
            MovementPhaseDefinition(MovementPhase.TERMINATION, "Bloqueio articular seguro no topo sem hiperextensão lombar.")
        ),
        instructions = ExerciseInstructions(
            setup = listOf("Pés paralelos ou levemente apontados para fora, alinhados aos ombros.", "Coluna neutra e olhar direcionado ao horizonte."),
            execution = listOf("Inicie a descida empurrando o quadril para trás e dobrando os joelhos.", "Mantenha os calcanhares firmemente apoiados no solo.", "Desça até que as coxas fiquem pelo menos paralelas ao solo."),
            breathing = "Inspire durante a descida (fase excêntrica) e expire durante a subida (fase concêntrica).",
            cuePoints = listOf("Peito aberto", "Joelhos acompanham a ponta dos pés", "Calcanhares colados no chão")
        ),
        commonErrors = listOf(
            CommonError("ERR-SQ-01", "Valgo dinâmico de joelho (joelhos colapsando para dentro).", ErrorSeverity.HIGH, "Foque em abrir os joelhos para fora na direção do terceiro dedo do pé."),
            CommonError("ERR-SQ-02", "Elevação dos calcanhares do solo.", ErrorSeverity.MEDIUM, "Trabalhe dorsiflexão de tornozelo e transfira o peso para o centro do pé.")
        ),
        safetyNotes = listOf("Mantenha a lordose lombar fisiológica durante todo o percurso."),
        progressionIds = listOf("EX-SQ-GOBLET-001-V1", "EX-PLY-JSQ-001-V1"),
        regressionIds = listOf("EX-HG-GB-001-V1"),
        mediaReferences = listOf(
            MediaReference("MED-SQ-BW-01", MediaType.ILLUSTRATION, source = "OFFICIAL_CATALOG")
        ),
        biomechanicalProfile = BiomechanicalProfile(
            motionPattern = "BILATERAL_KNEE_DOMINANT",
            jointTargets = listOf("HIP_FLEXION", "KNEE_FLEXION", "ANKLE_DORSIFLEXION"),
            expectedPhases = listOf(MovementPhase.SETUP, MovementPhase.ECCENTRIC, MovementPhase.CONCENTRIC),
            expectedROM = 110.0
        ),
        status = ExerciseStatus.ACTIVE
    )

    val GOBLET_SQUAT = ExerciseDefinition(
        exerciseId = "EX-SQ-GOBLET-001-V1",
        version = "V1",
        canonicalName = "Goblet Squat",
        displayName = "Agachamento Goblet",
        description = "Agachamento com sobrecarga anterior segurada junto ao tórax, promovendo verticalidade do tronco.",
        category = ExerciseCategory.STRENGTH,
        movementPattern = MovementPattern.SQUAT,
        primaryMuscles = listOf(MuscleGroup.QUADRICEPS, MuscleGroup.GLUTES),
        secondaryMuscles = listOf(MuscleGroup.CORE, MuscleGroup.UPPER_BACK, MuscleGroup.FOREARMS),
        equipment = listOf(EquipmentType.DUMBBELL, EquipmentType.KETTLEBELL),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        trainingGoals = listOf(TrainingGoal.HYPERTROPHY, TrainingGoal.MAX_STRENGTH, TrainingGoal.POSTURAL_STABILITY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        instructions = ExerciseInstructions(
            setup = listOf("Segure um halter ou kettlebell junto ao peito com as duas mãos.", "Cotovelos apontando para baixo."),
            execution = listOf("Agache entre as pernas mantendo os cotovelos passando por dentro dos joelhos no fundo.")
        ),
        progressionIds = listOf(),
        regressionIds = listOf("EX-SQ-BW-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val REVERSE_LUNGE = ExerciseDefinition(
        exerciseId = "EX-LG-REV-001-V1",
        version = "V1",
        canonicalName = "Reverse Lunge",
        displayName = "Avanço Reverso (Afundo)",
        description = "Padrão de lunge unilateral com passada para trás, reduzindo estresse na patela.",
        category = ExerciseCategory.STRENGTH,
        movementPattern = MovementPattern.LUNGE,
        primaryMuscles = listOf(MuscleGroup.QUADRICEPS, MuscleGroup.GLUTES),
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES, MuscleGroup.CORE),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.HYPERTROPHY, TrainingGoal.BALANCE, TrainingGoal.MOTOR_CONTROL),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.UNILATERAL,
        progressionIds = listOf("EX-LG-WLK-001-V1"),
        regressionIds = listOf("EX-SQ-BW-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val WALKING_LUNGE = ExerciseDefinition(
        exerciseId = "EX-LG-WLK-001-V1",
        version = "V1",
        canonicalName = "Walking Lunge",
        displayName = "Passada Caminhando",
        description = "Lunge dinâmico com deslocamento contínuo, exigindo controle postural e desaceleração.",
        category = ExerciseCategory.FUNCTIONAL,
        movementPattern = MovementPattern.LUNGE,
        primaryMuscles = listOf(MuscleGroup.QUADRICEPS, MuscleGroup.GLUTES),
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.CORE, MuscleGroup.CALVES),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        trainingGoals = listOf(TrainingGoal.HYPERTROPHY, TrainingGoal.BALANCE, TrainingGoal.POSTURAL_STABILITY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.ALTERNATING,
        progressionIds = listOf(),
        regressionIds = listOf("EX-LG-REV-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val ROMANIAN_DEADLIFT = ExerciseDefinition(
        exerciseId = "EX-HG-RDL-001-V1",
        version = "V1",
        canonicalName = "Romanian Deadlift",
        displayName = "Levantamento Terra Romeno (RDL)",
        description = "Padrão puro de hip hinge com ênfase na cadeia posterior e controle isométrico da coluna.",
        category = ExerciseCategory.STRENGTH,
        movementPattern = MovementPattern.HINGE,
        primaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES),
        secondaryMuscles = listOf(MuscleGroup.LOWER_BACK, MuscleGroup.CORE, MuscleGroup.UPPER_BACK),
        equipment = listOf(EquipmentType.DUMBBELL, EquipmentType.BARBELL),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        trainingGoals = listOf(TrainingGoal.HYPERTROPHY, TrainingGoal.MAX_STRENGTH, TrainingGoal.POSTURAL_STABILITY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf(),
        regressionIds = listOf("EX-HG-BW-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val HIP_HINGE = ExerciseDefinition(
        exerciseId = "EX-HG-BW-001-V1",
        version = "V1",
        canonicalName = "Bodyweight Hip Hinge",
        displayName = "Hip Hinge (Peso Corporal)",
        description = "Exercício educativo fundamental para dissociação lombo-pélvica e flexão pura de quadril.",
        category = ExerciseCategory.MOBILITY,
        movementPattern = MovementPattern.HINGE,
        primaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES),
        secondaryMuscles = listOf(MuscleGroup.LOWER_BACK, MuscleGroup.CORE),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.MOTOR_CONTROL, TrainingGoal.JOINT_MOBILITY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf("EX-HG-RDL-001-V1"),
        regressionIds = listOf("EX-HG-GB-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val PUSH_UP = ExerciseDefinition(
        exerciseId = "EX-PSH-STD-001-V1",
        version = "V1",
        canonicalName = "Standard Push-Up",
        displayName = "Flexão de Braços Padrão",
        description = "Padrão horizontal de empurrar em cadeia cinética fechada com controle rígido do core.",
        category = ExerciseCategory.CALISTHENICS,
        movementPattern = MovementPattern.PUSH,
        primaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.DELTOIDS),
        secondaryMuscles = listOf(MuscleGroup.CORE, MuscleGroup.ABDOMINALS),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.HYPERTROPHY, TrainingGoal.MAX_STRENGTH, TrainingGoal.POSTURAL_STABILITY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf(),
        regressionIds = listOf("EX-PSH-INC-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val INCLINE_PUSH_UP = ExerciseDefinition(
        exerciseId = "EX-PSH-INC-001-V1",
        version = "V1",
        canonicalName = "Incline Push-Up",
        displayName = "Flexão Inclinada no Banco",
        description = "Regressão da flexão clássica com mãos apoiadas em plano elevado, reduzindo a carga corporal relativa.",
        category = ExerciseCategory.CALISTHENICS,
        movementPattern = MovementPattern.PUSH,
        primaryMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
        secondaryMuscles = listOf(MuscleGroup.DELTOIDS, MuscleGroup.CORE),
        equipment = listOf(EquipmentType.BENCH, EquipmentType.BOX),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.MOTOR_CONTROL, TrainingGoal.HYPERTROPHY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf("EX-PSH-STD-001-V1"),
        regressionIds = listOf(),
        status = ExerciseStatus.ACTIVE
    )

    val PULL_UP = ExerciseDefinition(
        exerciseId = "EX-PLL-STD-001-V1",
        version = "V1",
        canonicalName = "Standard Pull-Up",
        displayName = "Barra Fixa Pronada",
        description = "Padrão vertical de puxar em cadeia fechada exigindo alta força relativa e estabilização escapular.",
        category = ExerciseCategory.CALISTHENICS,
        movementPattern = MovementPattern.PULL,
        primaryMuscles = listOf(MuscleGroup.LATS, MuscleGroup.UPPER_BACK, MuscleGroup.BICEPS),
        secondaryMuscles = listOf(MuscleGroup.FOREARMS, MuscleGroup.CORE),
        equipment = listOf(EquipmentType.PULL_UP_BAR),
        difficulty = ExerciseDifficulty.ADVANCED,
        trainingGoals = listOf(TrainingGoal.MAX_STRENGTH, TrainingGoal.HYPERTROPHY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf(),
        regressionIds = listOf("EX-PLL-AST-001-V1", "EX-PLL-INV-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val ASSISTED_PULL_UP = ExerciseDefinition(
        exerciseId = "EX-PLL-AST-001-V1",
        version = "V1",
        canonicalName = "Band-Assisted Pull-Up",
        displayName = "Barra Fixa com Elástico",
        description = "Variação assistida com elástico reduzindo a carga no ponto de maior desvantagem mecânica.",
        category = ExerciseCategory.CALISTHENICS,
        movementPattern = MovementPattern.PULL,
        primaryMuscles = listOf(MuscleGroup.LATS, MuscleGroup.BICEPS),
        secondaryMuscles = listOf(MuscleGroup.UPPER_BACK, MuscleGroup.FOREARMS),
        equipment = listOf(EquipmentType.PULL_UP_BAR, EquipmentType.RESISTANCE_BAND),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        trainingGoals = listOf(TrainingGoal.HYPERTROPHY, TrainingGoal.MOTOR_CONTROL),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf("EX-PLL-STD-001-V1"),
        regressionIds = listOf("EX-PLL-INV-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val INVERTED_ROW = ExerciseDefinition(
        exerciseId = "EX-PLL-INV-001-V1",
        version = "V1",
        canonicalName = "Inverted Row",
        displayName = "Remada Invertida (Peso Corporal)",
        description = "Padrão horizontal de puxar com suporte nos calcanhares e tração escapular controlada.",
        category = ExerciseCategory.CALISTHENICS,
        movementPattern = MovementPattern.PULL,
        primaryMuscles = listOf(MuscleGroup.UPPER_BACK, MuscleGroup.LATS, MuscleGroup.BICEPS),
        secondaryMuscles = listOf(MuscleGroup.CORE, MuscleGroup.FOREARMS),
        equipment = listOf(EquipmentType.BARBELL, EquipmentType.RINGS),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.POSTURAL_STABILITY, TrainingGoal.HYPERTROPHY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf("EX-PLL-AST-001-V1"),
        regressionIds = listOf(),
        status = ExerciseStatus.ACTIVE
    )

    val PLANK = ExerciseDefinition(
        exerciseId = "EX-COR-PLK-001-V1",
        version = "V1",
        canonicalName = "Prone Forearm Plank",
        displayName = "Prancha Isométrica Ventral",
        description = "Sustentação isométrica em decúbito ventral focada em anti-extensão lombar.",
        category = ExerciseCategory.STABILITY,
        movementPattern = MovementPattern.ISOMETRIC,
        primaryMuscles = listOf(MuscleGroup.ABDOMINALS, MuscleGroup.CORE),
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.DELTOIDS, MuscleGroup.QUADRICEPS),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.POSTURAL_STABILITY, TrainingGoal.MOTOR_CONTROL),
        executionType = ExecutionType.TIME_BASED,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf("EX-COR-HBH-001-V1"),
        regressionIds = listOf(),
        status = ExerciseStatus.ACTIVE
    )

    val SIDE_PLANK = ExerciseDefinition(
        exerciseId = "EX-COR-SPLK-001-V1",
        version = "V1",
        canonicalName = "Side Forearm Plank",
        displayName = "Prancha Lateral Isométrica",
        description = "Sustentação isométrica lateral para fortalecimento da musculatura oblíqua e anti-flexão lateral.",
        category = ExerciseCategory.STABILITY,
        movementPattern = MovementPattern.ISOMETRIC,
        primaryMuscles = listOf(MuscleGroup.OBLIQUES, MuscleGroup.CORE),
        secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.DELTOIDS),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.POSTURAL_STABILITY, TrainingGoal.BALANCE),
        executionType = ExecutionType.TIME_BASED,
        laterality = Laterality.UNILATERAL,
        progressionIds = listOf(),
        regressionIds = listOf("EX-COR-PLK-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val GLUTE_BRIDGE = ExerciseDefinition(
        exerciseId = "EX-HG-GB-001-V1",
        version = "V1",
        canonicalName = "Bodyweight Glute Bridge",
        displayName = "Elevação Pélvica no Solo",
        description = "Extensão isolada de quadril em decúbito dorsal sem sobrecarga axial na coluna.",
        category = ExerciseCategory.STRENGTH,
        movementPattern = MovementPattern.HINGE,
        primaryMuscles = listOf(MuscleGroup.GLUTES),
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.MOTOR_CONTROL, TrainingGoal.HYPERTROPHY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf("EX-HG-BW-001-V1"),
        regressionIds = listOf(),
        status = ExerciseStatus.ACTIVE
    )

    val CALF_RAISE = ExerciseDefinition(
        exerciseId = "EX-CALF-BW-001-V1",
        version = "V1",
        canonicalName = "Standing Calf Raise",
        displayName = "Elevação de Panturrilha em Pé",
        description = "Flexão plantar completa contra o peso corporal para fortalecimento do gastrocnêmio e sóleo.",
        category = ExerciseCategory.STRENGTH,
        movementPattern = MovementPattern.ISOMETRIC,
        primaryMuscles = listOf(MuscleGroup.CALVES),
        secondaryMuscles = listOf(),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.HYPERTROPHY, TrainingGoal.POSTURAL_STABILITY),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf(),
        regressionIds = listOf(),
        status = ExerciseStatus.ACTIVE
    )

    val JUMP_SQUAT = ExerciseDefinition(
        exerciseId = "EX-PLY-JSQ-001-V1",
        version = "V1",
        canonicalName = "Bodyweight Jump Squat",
        displayName = "Agachamento com Salto Pliométrico",
        description = "Exercício pliométrico de tripla extensão explosiva e aterrissagem amortecida.",
        category = ExerciseCategory.POWER,
        movementPattern = MovementPattern.JUMP,
        primaryMuscles = listOf(MuscleGroup.QUADRICEPS, MuscleGroup.GLUTES, MuscleGroup.CALVES),
        secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        trainingGoals = listOf(TrainingGoal.EXPLOSIVE_POWER, TrainingGoal.CARDIO_ENDURANCE),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf(),
        regressionIds = listOf("EX-SQ-BW-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val BURPEE = ExerciseDefinition(
        exerciseId = "EX-CND-BRP-001-V1",
        version = "V1",
        canonicalName = "Standard Burpee",
        displayName = "Burpee Tradicional",
        description = "Padrão de condicionamento de corpo inteiro combinando agachamento, apoio ventral e salto.",
        category = ExerciseCategory.CARDIORESPIRATORY,
        movementPattern = MovementPattern.LOCOMOTION,
        primaryMuscles = listOf(MuscleGroup.FULL_BODY, MuscleGroup.QUADRICEPS, MuscleGroup.CHEST),
        secondaryMuscles = listOf(MuscleGroup.CORE, MuscleGroup.CALVES, MuscleGroup.DELTOIDS),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        trainingGoals = listOf(TrainingGoal.CARDIO_ENDURANCE, TrainingGoal.SPEED_ENDURANCE),
        executionType = ExecutionType.REPETITION,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf(),
        regressionIds = listOf("EX-CND-MTC-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val MOUNTAIN_CLIMBER = ExerciseDefinition(
        exerciseId = "EX-CND-MTC-001-V1",
        version = "V1",
        canonicalName = "Mountain Climber",
        displayName = "Mountain Climber (Escalador)",
        description = "Flexão dinâmica alternada de quadris em posição de prancha alta com cadência rápida.",
        category = ExerciseCategory.CARDIORESPIRATORY,
        movementPattern = MovementPattern.LOCOMOTION,
        primaryMuscles = listOf(MuscleGroup.CORE, MuscleGroup.HIP_FLEXORS),
        secondaryMuscles = listOf(MuscleGroup.DELTOIDS, MuscleGroup.QUADRICEPS),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.CARDIO_ENDURANCE, TrainingGoal.MOTOR_CONTROL),
        executionType = ExecutionType.TIME_BASED,
        laterality = Laterality.ALTERNATING,
        progressionIds = listOf("EX-CND-BRP-001-V1"),
        regressionIds = listOf("EX-COR-PLK-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val DEAD_HANG = ExerciseDefinition(
        exerciseId = "EX-GRP-DH-001-V1",
        version = "V1",
        canonicalName = "Passive Dead Hang",
        displayName = "Dead Hang na Barra Fixa",
        description = "Suspensão passiva na barra para descompressão espinhal, força de preensão manual e mobilidade de ombros.",
        category = ExerciseCategory.MOBILITY,
        movementPattern = MovementPattern.ISOMETRIC,
        primaryMuscles = listOf(MuscleGroup.FOREARMS, MuscleGroup.LATS),
        secondaryMuscles = listOf(MuscleGroup.UPPER_BACK),
        equipment = listOf(EquipmentType.PULL_UP_BAR),
        difficulty = ExerciseDifficulty.BEGINNER,
        trainingGoals = listOf(TrainingGoal.JOINT_MOBILITY, TrainingGoal.POSTURAL_STABILITY),
        executionType = ExecutionType.TIME_BASED,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf("EX-PLL-STD-001-V1"),
        regressionIds = listOf(),
        status = ExerciseStatus.ACTIVE
    )

    val HOLLOW_BODY_HOLD = ExerciseDefinition(
        exerciseId = "EX-COR-HBH-001-V1",
        version = "V1",
        canonicalName = "Hollow Body Hold",
        displayName = "Hollow Body Hold (Canoa Isométrica)",
        description = "Postura isométrica fundamental de ginástica com retroversão pélvica e compressão total do core.",
        category = ExerciseCategory.CALISTHENICS,
        movementPattern = MovementPattern.ISOMETRIC,
        primaryMuscles = listOf(MuscleGroup.ABDOMINALS, MuscleGroup.CORE),
        secondaryMuscles = listOf(MuscleGroup.HIP_FLEXORS, MuscleGroup.QUADRICEPS),
        equipment = listOf(EquipmentType.BODYWEIGHT),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        trainingGoals = listOf(TrainingGoal.POSTURAL_STABILITY, TrainingGoal.MOTOR_CONTROL),
        executionType = ExecutionType.TIME_BASED,
        laterality = Laterality.BILATERAL,
        progressionIds = listOf(),
        regressionIds = listOf("EX-COR-PLK-001-V1"),
        status = ExerciseStatus.ACTIVE
    )

    val CANONICAL_CATALOG = listOf(
        BODYWEIGHT_SQUAT,
        GOBLET_SQUAT,
        REVERSE_LUNGE,
        WALKING_LUNGE,
        ROMANIAN_DEADLIFT,
        HIP_HINGE,
        PUSH_UP,
        INCLINE_PUSH_UP,
        PULL_UP,
        ASSISTED_PULL_UP,
        INVERTED_ROW,
        PLANK,
        SIDE_PLANK,
        GLUTE_BRIDGE,
        CALF_RAISE,
        JUMP_SQUAT,
        BURPEE,
        MOUNTAIN_CLIMBER,
        DEAD_HANG,
        HOLLOW_BODY_HOLD
    )

    fun initializeCanonicalCatalog() {
        for (exercise in CANONICAL_CATALOG) {
            ExerciseRegistryV1.register(exercise)
        }
    }

    fun getExerciseById(exerciseId: String): ExerciseDefinition? {
        return CANONICAL_CATALOG.find { it.exerciseId == exerciseId } ?: ExerciseRegistryV1.getById(exerciseId)
    }
}
