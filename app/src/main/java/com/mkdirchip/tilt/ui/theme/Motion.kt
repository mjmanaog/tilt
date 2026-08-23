package com.mkdirchip.tilt.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Whether the user has asked the system to reduce or disable animation. Compose exposes no such
 * flag, so it is read once from the platform setting and published here — centralized so an
 * individual screen cannot forget to honour it.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    val scale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    return scale == 0f
}

/** Standard durations, collapsing to instant when motion is reduced. */
object TiltMotion {
    const val QUICK = 180
    const val STANDARD = 320
    const val ENTRANCE = 420

    /** Per-card entrance stagger; zero when motion is reduced. */
    fun stagger(index: Int, reduced: Boolean): Int =
        if (reduced) 0 else (index % 12) * 40
}

@Composable
fun <T> tiltSpec(durationMillis: Int = TiltMotion.STANDARD): FiniteAnimationSpec<T> =
    if (LocalReducedMotion.current) {
        snap()
    } else {
        tween(durationMillis = durationMillis, easing = LinearOutSlowInEasing)
    }

@Composable
fun <T> tiltDelayedSpec(durationMillis: Int, delayMillis: Int): FiniteAnimationSpec<T> =
    if (LocalReducedMotion.current) {
        snap()
    } else {
        tween(durationMillis = durationMillis, delayMillis = delayMillis, easing = LinearOutSlowInEasing)
    }
