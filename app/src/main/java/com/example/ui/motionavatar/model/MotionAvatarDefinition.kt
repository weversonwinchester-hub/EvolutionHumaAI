package com.example.ui.motionavatar.model

import androidx.compose.ui.graphics.Color
import com.example.core.exerciseengine.media.model.AvatarCharacterId

/**
 * EVOLUTION HUMAN AI — MOTION AVATAR V1
 *
 * Definition of the 2D/2.5D visual avatar dimensions, proportions, and styling.
 * Supports both MALE_AVATAR_V1 and FEMALE_AVATAR_V1 on the exact same skeletal architecture.
 */
data class MotionAvatarDefinition(
    val avatarId: AvatarCharacterId,
    val name: String,
    val genderLabel: String,
    // Proportional dimensions (normalized base units)
    val headRadius: Float,
    val neckLength: Float,
    val torsoLength: Float,
    val shoulderWidth: Float,
    val pelvisWidth: Float,
    val upperArmLength: Float,
    val forearmLength: Float,
    val thighLength: Float,
    val shankLength: Float,
    val footLength: Float,
    // Stylistic aesthetics
    val jointRadius: Float,
    val limbStrokeWidth: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val jointColor: Color
) {
    companion object {
        /**
         * MALE AVATAR V1 — Broader shoulders, standard pelvic ratio, athletic dark teal theme.
         */
        val MALE_AVATAR_V1 = MotionAvatarDefinition(
            avatarId = AvatarCharacterId.MALE_AVATAR_V1,
            name = "Atlas V1 (Masculino)",
            genderLabel = "Masculino",
            headRadius = 18f,
            neckLength = 12f,
            torsoLength = 70f,
            shoulderWidth = 42f,
            pelvisWidth = 32f,
            upperArmLength = 40f,
            forearmLength = 36f,
            thighLength = 54f,
            shankLength = 52f,
            footLength = 24f,
            jointRadius = 5f,
            limbStrokeWidth = 8f,
            primaryColor = Color(0xFF00E5FF),     // Vibrant Cyber Cyan
            secondaryColor = Color(0xFF00838F),   // Deep Cyan
            accentColor = Color(0xFF76FF03),      // Energy Lime
            jointColor = Color(0xFFFFFFFF)        // Pure White Pivot Point
        )

        /**
         * FEMALE AVATAR V1 — Slightly narrower shoulders, wider pelvic ratio, athletic electric violet theme.
         */
        val FEMALE_AVATAR_V1 = MotionAvatarDefinition(
            avatarId = AvatarCharacterId.FEMALE_AVATAR_V1,
            name = "Astra V1 (Feminino)",
            genderLabel = "Feminino",
            headRadius = 17f,
            neckLength = 12f,
            torsoLength = 68f,
            shoulderWidth = 36f,
            pelvisWidth = 36f,
            upperArmLength = 38f,
            forearmLength = 34f,
            thighLength = 53f,
            shankLength = 50f,
            footLength = 22f,
            jointRadius = 4.5f,
            limbStrokeWidth = 7.5f,
            primaryColor = Color(0xFFE040FB),     // Electric Orchid
            secondaryColor = Color(0xFF8E24AA),   // Deep Violet
            accentColor = Color(0xFFFFD700),      // Golden Flare
            jointColor = Color(0xFFFFFFFF)        // Pure White Pivot Point
        )

        fun forAvatarId(avatarId: AvatarCharacterId): MotionAvatarDefinition {
            return when (avatarId) {
                AvatarCharacterId.FEMALE_AVATAR_V1 -> FEMALE_AVATAR_V1
                AvatarCharacterId.MALE_AVATAR_V1 -> MALE_AVATAR_V1
                else -> MALE_AVATAR_V1
            }
        }
    }
}
