package com.mkdirchip.tilt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Always dark, always navy. [isSystemInDarkTheme][androidx.compose.foundation.isSystemInDarkTheme]
 * is deliberately never consulted: there is no light variant of this app.
 *
 * Material3's scheme is populated so stock components inherit sane colours, but anything
 * expressive reads [LocalTiltPalette] instead.
 */
@Composable
fun TiltTheme(content: @Composable () -> Unit) {
    val palette = tiltPalette()

    val scheme = darkColorScheme(
        primary = palette.accent,
        onPrimary = palette.onAccent,
        primaryContainer = palette.accent,
        onPrimaryContainer = palette.onAccent,
        secondary = palette.accentPartner,
        onSecondary = palette.ground,
        background = palette.ground,
        onBackground = palette.onGround,
        surface = palette.surface,
        onSurface = palette.onGround,
        surfaceVariant = palette.surfaceLift,
        onSurfaceVariant = palette.muted,
        outline = palette.muted,
        error = palette.accentPartner,
        onError = palette.ground,
    )

    CompositionLocalProvider(
        LocalTiltPalette provides palette,
        LocalReducedMotion provides rememberReducedMotion(),
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = TiltTypography,
            content = content,
        )
    }
}
