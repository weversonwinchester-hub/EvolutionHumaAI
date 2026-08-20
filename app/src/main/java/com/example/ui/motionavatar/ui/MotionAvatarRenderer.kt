package com.example.ui.motionavatar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.exerciseengine.media.model.AvatarCharacterId
import com.example.ui.motionavatar.engine.MotionAvatarEngine
import com.example.ui.motionavatar.engine.MotionAvatarState
import com.example.ui.motionavatar.model.MotionAvatarDefinition
import com.example.ui.motionavatar.model.MotionAvatarPhase
import com.example.ui.motionavatar.model.MotionAvatarPose
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * EVOLUTION HUMAN AI — MOTION AVATAR V1 RENDERER
 *
 * Lightweight 2D/2.5D articulated skeletal viewport with keyframe interpolation,
 * visual angle overlays, and interactive playback controls.
 */
@Composable
fun MotionAvatarViewport(
    engine: MotionAvatarEngine,
    modifier: Modifier = Modifier,
    onSelectAvatar: (AvatarCharacterId) -> Unit = {}
) {
    val state by engine.state.collectAsState()

    // Real-time animation loop driver
    LaunchedEffect(state.isPlaying, state.playbackSpeed) {
        var lastTime = withInfiniteAnimationFrameMillis { it }
        while (state.isPlaying) {
            val currentTime = withInfiniteAnimationFrameMillis { it }
            val delta = currentTime - lastTime
            lastTime = currentTime
            engine.advanceTime(delta)
        }
    }

    val avatarDef = MotionAvatarDefinition.forAvatarId(state.selectedAvatar)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PitchBlack,
                        ObsidianSurfaceElevated,
                        PitchBlack
                    )
                )
            )
            .border(1.dp, ObsidianBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .testTag("motion_avatar_viewport")
    ) {
        // Grid / Technical Background Plane
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMotionGrid(this)
        }

        // Avatar Skeletal Model Drawing
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 44.dp)
                .testTag("motion_avatar_canvas")
        ) {
            drawArticulatedAvatar(
                pose = state.currentPose,
                def = avatarDef,
                isMirrored = state.isMirrored
            )
        }

        // Display-Only Top HUD (Phase, Angles, Repetitions)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Repetition Counter & Phase Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = GoldAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                ) {
                    Text(
                        text = "REP #${state.repetitionCount}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = when (state.currentPose.phase) {
                        MotionAvatarPhase.START, MotionAvatarPhase.END -> CyanPrimaryVariant.copy(alpha = 0.2f)
                        MotionAvatarPhase.DESCENT, MotionAvatarPhase.ASCENT -> CyanPrimary.copy(alpha = 0.2f)
                        MotionAvatarPhase.BOTTOM -> StatusActive.copy(alpha = 0.25f)
                    },
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (state.currentPose.phase) {
                            MotionAvatarPhase.START, MotionAvatarPhase.END -> CyanPrimaryVariant
                            MotionAvatarPhase.DESCENT, MotionAvatarPhase.ASCENT -> CyanPrimary
                            MotionAvatarPhase.BOTTOM -> StatusActive
                        }
                    )
                ) {
                    Text(
                        text = state.currentPose.phase.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Biomechanical Angles HUD (Display-Only)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AnglePill(label = "JOELHO", angle = "${state.currentPose.displayKneeAngle}°", color = avatarDef.primaryColor)
                AnglePill(label = "QUADRIL", angle = "${state.currentPose.displayHipAngle}°", color = avatarDef.accentColor)
                AnglePill(label = "TORNOZELO", angle = "${state.currentPose.displayAnkleAngle}°", color = TextSecondary)
            }
        }

        // Bottom Technical Cue Banner
        if (state.activeCue.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                color = PitchBlack.copy(alpha = 0.75f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
            ) {
                Text(
                    text = state.activeCue,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1
                )
            }
        }

        // Bottom Control Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(42.dp),
            color = PitchBlack.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause / Restart
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { engine.togglePlay() },
                        modifier = Modifier.size(32.dp).testTag("motion_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pausar" else "Reproduzir",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { engine.restart() },
                        modifier = Modifier.size(32.dp).testTag("motion_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reiniciar",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { engine.setMirrored(!state.isMirrored) },
                        modifier = Modifier.size(32.dp).testTag("motion_mirror_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Espelhar",
                            tint = if (state.isMirrored) CyanPrimary else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Speed Selectors
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.availableSpeeds.forEach { speed ->
                        val isSelected = state.playbackSpeed == speed
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { engine.setSpeed(speed) }
                                .testTag("motion_speed_${speed}x"),
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.3f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CyanPrimary else ObsidianBorder
                            )
                        ) {
                            Text(
                                text = "${speed}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) CyanPrimary else TextMuted,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Avatar Switcher (Male/Female)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AvatarMiniBadge(
                        avatarId = AvatarCharacterId.MALE_AVATAR_V1,
                        label = "M",
                        isSelected = state.selectedAvatar == AvatarCharacterId.MALE_AVATAR_V1,
                        color = Color(0xFF00E5FF),
                        onClick = {
                            engine.setAvatar(AvatarCharacterId.MALE_AVATAR_V1)
                            onSelectAvatar(AvatarCharacterId.MALE_AVATAR_V1)
                        }
                    )

                    AvatarMiniBadge(
                        avatarId = AvatarCharacterId.FEMALE_AVATAR_V1,
                        label = "F",
                        isSelected = state.selectedAvatar == AvatarCharacterId.FEMALE_AVATAR_V1,
                        color = Color(0xFFE040FB),
                        onClick = {
                            engine.setAvatar(AvatarCharacterId.FEMALE_AVATAR_V1)
                            onSelectAvatar(AvatarCharacterId.FEMALE_AVATAR_V1)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnglePill(label: String, angle: String, color: Color) {
    Surface(
        color = PitchBlack.copy(alpha = 0.8f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            )
            Text(
                text = angle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
private fun AvatarMiniBadge(
    avatarId: AvatarCharacterId,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag("motion_avatar_badge_${avatarId.name.lowercase()}"),
        color = if (isSelected) color.copy(alpha = 0.25f) else PitchBlack,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) color else ObsidianBorder
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) color else TextMuted
            )
        }
    }
}

/**
 * Draws the technical grid ground and vertical biomechanical axes.
 */
private fun drawMotionGrid(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height
    val groundY = h * 0.78f

    // Ground plane
    scope.drawLine(
        color = ObsidianBorder,
        start = Offset(0f, groundY),
        end = Offset(w, groundY),
        strokeWidth = 2f
    )

    // Floor hash marks
    val step = 30f
    var x = 0f
    while (x < w) {
        scope.drawLine(
            color = TextMuted.copy(alpha = 0.2f),
            start = Offset(x, groundY),
            end = Offset(x - 15f, groundY + 15f),
            strokeWidth = 1f
        )
        x += step
    }

    // Vertical plumb line indicator at center
    scope.drawLine(
        color = GoldAccent.copy(alpha = 0.15f),
        start = Offset(w / 2f, 20f),
        end = Offset(w / 2f, groundY),
        strokeWidth = 1f
    )
}

/**
 * Extension on DrawScope to perform actual visual rendering of segments.
 */
private fun DrawScope.drawArticulatedAvatar(
    pose: MotionAvatarPose,
    def: MotionAvatarDefinition,
    isMirrored: Boolean
) {
    val scale = size.height / 280f
    val centerX = size.width / 2f
    val groundY = size.height * 0.76f

    val mirrorFactor = if (isMirrored) -1f else 1f

    // 1. Foot / Ankle Anchor
    val ankleX = centerX
    val ankleY = groundY - (6f * scale)

    // 2. Ankle Dorsiflexion -> Knee calculation
    val shankProgress = (pose.ankleDorsiDeg / 20f).coerceIn(0f, 1f)
    val kneeForwardShift = (22f * shankProgress * scale * mirrorFactor)
    val kneeVerticalDrop = (10f * shankProgress * scale)
    val kneeX = ankleX + kneeForwardShift
    val kneeY = ankleY - (def.shankLength * scale) + kneeVerticalDrop

    // 3. Hip Flexion -> Pelvis calculation (backward shift + vertical drop)
    val hipProgress = (pose.hipFlexionDeg / 92f).coerceIn(0f, 1f)
    val pelvisBackwardShift = (32f * hipProgress * scale * mirrorFactor)
    val pelvisVerticalDrop = (pose.pelvisOffsetY * 110f * scale)
    val pelvisX = ankleX - pelvisBackwardShift
    val pelvisY = ankleY - (def.thighLength * scale) - (def.shankLength * scale) + pelvisVerticalDrop

    // 4. Torso Lean -> Shoulder calculation
    val trunkLeanRatio = sin(Math.toRadians(pose.trunkAngleDeg.toDouble())).toFloat()
    val trunkCosRatio = cos(Math.toRadians(pose.trunkAngleDeg.toDouble())).toFloat()
    val shoulderX = pelvisX + (def.torsoLength * scale * trunkLeanRatio * mirrorFactor)
    val shoulderY = pelvisY - (def.torsoLength * scale * trunkCosRatio)

    // 5. Neck & Head
    val neckX = shoulderX + (def.neckLength * scale * trunkLeanRatio * mirrorFactor)
    val neckY = shoulderY - (def.neckLength * scale * trunkCosRatio)
    val headX = neckX + (def.headRadius * 1.1f * scale * trunkLeanRatio * mirrorFactor)
    val headY = neckY - (def.headRadius * 1.1f * scale * trunkCosRatio)

    // 6. Arms (Counter-balance forward reach)
    val armAngleRad = Math.toRadians((pose.shoulderFlexionDeg.toDouble()))
    val elbowX = shoulderX + (sin(armAngleRad).toFloat() * def.upperArmLength * scale * mirrorFactor)
    val elbowY = shoulderY + (cos(armAngleRad).toFloat() * def.upperArmLength * scale * 0.6f)

    val forearmAngleRad = Math.toRadians(((pose.shoulderFlexionDeg + 25f).toDouble()))
    val handX = elbowX + (sin(forearmAngleRad).toFloat() * def.forearmLength * scale * mirrorFactor)
    val handY = elbowY - (cos(forearmAngleRad).toFloat() * def.forearmLength * scale * 0.4f)

    // 7. Foot geometry
    val toeX = ankleX + (def.footLength * scale * mirrorFactor)
    val toeY = groundY
    val heelX = ankleX - (def.footLength * 0.35f * scale * mirrorFactor)
    val heelY = groundY

    val limbStroke = def.limbStrokeWidth * scale
    val jointR = def.jointRadius * scale

    // --- DRAW SKELETAL SEGMENTS ---

    // A. Foot
    val footPath = Path().apply {
        moveTo(heelX, heelY)
        lineTo(toeX, toeY)
        lineTo(ankleX, ankleY)
        close()
    }
    drawPath(
        path = footPath,
        color = def.secondaryColor,
        style = Stroke(width = limbStroke * 0.8f, cap = StrokeCap.Round)
    )

    // B. Shank (Lower Leg: Ankle to Knee)
    drawLine(
        color = def.primaryColor,
        start = Offset(ankleX, ankleY),
        end = Offset(kneeX, kneeY),
        strokeWidth = limbStroke,
        cap = StrokeCap.Round
    )

    // C. Thigh (Knee to Pelvis)
    drawLine(
        color = def.primaryColor,
        start = Offset(kneeX, kneeY),
        end = Offset(pelvisX, pelvisY),
        strokeWidth = limbStroke * 1.15f,
        cap = StrokeCap.Round
    )

    // D. Pelvis Segment / Hip Bar
    drawCircle(
        color = def.secondaryColor,
        radius = jointR * 1.4f,
        center = Offset(pelvisX, pelvisY)
    )

    // E. Torso / Spine (Pelvis to Shoulder)
    drawLine(
        color = def.primaryColor,
        start = Offset(pelvisX, pelvisY),
        end = Offset(shoulderX, shoulderY),
        strokeWidth = limbStroke * 1.3f,
        cap = StrokeCap.Round
    )

    // Ribcage / Chest styling accent
    val chestCenterX = (pelvisX + shoulderX * 2f) / 3f
    val chestCenterY = (pelvisY + shoulderY * 2f) / 3f
    drawLine(
        color = def.secondaryColor,
        start = Offset(chestCenterX - (6f * scale * mirrorFactor), chestCenterY),
        end = Offset(chestCenterX + (10f * scale * mirrorFactor), chestCenterY - (4f * scale)),
        strokeWidth = limbStroke * 0.7f,
        cap = StrokeCap.Round
    )

    // F. Neck
    drawLine(
        color = def.secondaryColor,
        start = Offset(shoulderX, shoulderY),
        end = Offset(neckX, neckY),
        strokeWidth = limbStroke * 0.75f,
        cap = StrokeCap.Round
    )

    // G. Head (Stylized Cyber Visor)
    drawCircle(
        color = def.primaryColor,
        radius = def.headRadius * scale,
        center = Offset(headX, headY)
    )
    // Inner Head Core
    drawCircle(
        color = ObsidianSurfaceElevated,
        radius = def.headRadius * scale * 0.75f,
        center = Offset(headX, headY)
    )
    // Gaze Directional Visor
    val visorX = headX + (def.headRadius * scale * 0.5f * mirrorFactor)
    val visorY = headY - (2f * scale)
    drawCircle(
        color = def.accentColor,
        radius = 3.5f * scale,
        center = Offset(visorX, visorY)
    )

    // H. Arm (Shoulder -> Elbow -> Hand)
    drawLine(
        color = def.secondaryColor,
        start = Offset(shoulderX, shoulderY),
        end = Offset(elbowX, elbowY),
        strokeWidth = limbStroke * 0.85f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = def.primaryColor,
        start = Offset(elbowX, elbowY),
        end = Offset(handX, handY),
        strokeWidth = limbStroke * 0.75f,
        cap = StrokeCap.Round
    )
    // Hand
    drawCircle(
        color = def.accentColor,
        radius = jointR * 0.8f,
        center = Offset(handX, handY)
    )

    // --- ARTICULATION JOINTS (Glowing White/Cyan Pivot Nodes) ---
    val jointColor = def.jointColor
    drawCircle(color = jointColor, radius = jointR, center = Offset(ankleX, ankleY))
    drawCircle(color = jointColor, radius = jointR * 1.1f, center = Offset(kneeX, kneeY))
    drawCircle(color = jointColor, radius = jointR * 1.1f, center = Offset(pelvisX, pelvisY))
    drawCircle(color = jointColor, radius = jointR, center = Offset(shoulderX, shoulderY))
    drawCircle(color = jointColor, radius = jointR * 0.9f, center = Offset(elbowX, elbowY))

    // --- BIOMECHANICAL ANGLE ARC (Knee Flexion Visualization) ---
    if (pose.kneeFlexionDeg > 15f) {
        val arcRadius = 24f * scale
        drawCircle(
            color = def.accentColor.copy(alpha = 0.25f),
            radius = arcRadius,
            center = Offset(kneeX, kneeY),
            style = Stroke(width = 1.5f * scale)
        )
    }
}
