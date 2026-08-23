package com.mkdirchip.tilt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.mkdirchip.tilt.R

/**
 * The app's colours. Read from `colors.xml` rather than duplicated as Kotlin literals, so the
 * widget — which can only use resources — and the app cannot drift apart.
 */
@Immutable
data class TiltPalette(
    val ground: Color,
    val surface: Color,
    val surfaceLift: Color,
    val accent: Color,
    val accentPartner: Color,
    val onGround: Color,
    val muted: Color,
    val onAccent: Color,
    val onAccentMuted: Color,
)

@Composable
fun tiltPalette(): TiltPalette = TiltPalette(
    ground = colorResource(R.color.tilt_ground),
    surface = colorResource(R.color.tilt_surface),
    surfaceLift = colorResource(R.color.tilt_surface_lift),
    accent = colorResource(R.color.tilt_accent),
    accentPartner = colorResource(R.color.tilt_accent_partner),
    onGround = colorResource(R.color.tilt_on_ground),
    muted = colorResource(R.color.tilt_muted),
    onAccent = colorResource(R.color.tilt_on_accent),
    onAccentMuted = colorResource(R.color.tilt_on_accent_muted),
)

val LocalTiltPalette = staticCompositionLocalOf<TiltPalette> {
    error("No TiltPalette; wrap the content in TiltTheme")
}
