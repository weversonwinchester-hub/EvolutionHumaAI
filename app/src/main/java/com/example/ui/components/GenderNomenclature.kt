package com.example.ui.components

import com.example.core.evolutionengine.catalog.ClassCatalog

/**
 * Formatação e Nomenclatura Inclusiva de Gênero para as Classes da EvolutionHumanAI (01 a 22).
 *
 * Mapeia adequações gramaticais femininas com base no perfil do atleta sem alterar os identificadores
 * de classe oficiais do Core.
 */
object GenderNomenclature {

    private val FEMALE_CLASS_NAMES = mapOf(
        ClassCatalog.CLASS_01 to "01 Corpo Adormecido",
        ClassCatalog.CLASS_02 to "02 Sobrevivente",
        ClassCatalog.CLASS_03 to "03 Desperta",
        ClassCatalog.CLASS_04 to "04 Iniciada",
        ClassCatalog.CLASS_05 to "05 Exploradora",
        ClassCatalog.CLASS_06 to "06 Aprendiz",
        ClassCatalog.CLASS_07 to "07 Discípula",
        ClassCatalog.CLASS_08 to "08 Atleta Emergente",
        ClassCatalog.CLASS_09 to "09 Competidora",
        ClassCatalog.CLASS_10 to "10 Atleta",
        ClassCatalog.CLASS_11 to "11 Especialista",
        ClassCatalog.CLASS_12 to "12 Predadora Atlética",
        ClassCatalog.CLASS_13 to "13 Guerreira",
        ClassCatalog.CLASS_14 to "14 Gladiadora",
        ClassCatalog.CLASS_15 to "15 Campeã",
        ClassCatalog.CLASS_16 to "16 Titã",
        ClassCatalog.CLASS_17 to "17 Colosso",
        ClassCatalog.CLASS_18 to "18 Heroína",
        ClassCatalog.CLASS_19 to "19 Heroína Ascendente",
        ClassCatalog.CLASS_20 to "20 Lenda",
        ClassCatalog.CLASS_21 to "21 Ascendente",
        ClassCatalog.CLASS_22 to "22 Semideusa"
    )

    fun formatClassName(classId: String, standardName: String, gender: String?): String {
        val isFemale = isFemaleGender(gender)
        return if (isFemale) {
            FEMALE_CLASS_NAMES[classId] ?: standardName
        } else {
            standardName
        }
    }

    fun formatClassTitleOnly(className: String, gender: String?): String {
        val isFemale = isFemaleGender(gender)
        if (!isFemale) return className

        return when {
            className.contains("Desperto", ignoreCase = true) -> className.replace("Desperto", "Desperta", ignoreCase = true)
            className.contains("Iniciado", ignoreCase = true) -> className.replace("Iniciado", "Iniciada", ignoreCase = true)
            className.contains("Explorador", ignoreCase = true) -> className.replace("Explorador", "Exploradora", ignoreCase = true)
            className.contains("Discípulo", ignoreCase = true) -> className.replace("Discípulo", "Discípula", ignoreCase = true)
            className.contains("Competidor", ignoreCase = true) -> className.replace("Competidor", "Competidora", ignoreCase = true)
            className.contains("Predador Atlético", ignoreCase = true) -> className.replace("Predador Atlético", "Predadora Atlética", ignoreCase = true)
            className.contains("Guerreiro", ignoreCase = true) -> className.replace("Guerreiro", "Guerreira", ignoreCase = true)
            className.contains("Gladiador", ignoreCase = true) -> className.replace("Gladiador", "Gladiadora", ignoreCase = true)
            className.contains("Campeão", ignoreCase = true) -> className.replace("Campeão", "Campeã", ignoreCase = true)
            className.contains("Herói Ascendente", ignoreCase = true) -> className.replace("Herói Ascendente", "Heroína Ascendente", ignoreCase = true)
            className.contains("Herói", ignoreCase = true) -> className.replace("Herói", "Heroína", ignoreCase = true)
            className.contains("Semideus", ignoreCase = true) -> className.replace("Semideus", "Semideusa", ignoreCase = true)
            else -> className
        }
    }

    fun isFemaleGender(gender: String?): Boolean {
        if (gender == null) return false
        val normalized = gender.trim().lowercase()
        return normalized == "feminino" || normalized == "mulher" || normalized == "female" || normalized == "f"
    }
}
