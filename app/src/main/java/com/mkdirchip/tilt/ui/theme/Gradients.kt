package com.mkdirchip.tilt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Every gradient in the app. Stops come only from [TiltPalette]; the card gradient's two stops
 * sit inside a narrow luminance band so the 4.5:1 text check holds at the lightest stop rather
 * than merely on average.
 */
object TiltGradients {

    @Composable
    fun card(): Brush {
        val p = LocalTiltPalette.current
        return Brush.linearGradient(listOf(p.surfaceLift, p.surface))
    }

    /** The accent-filled card treatment that punctuates the feed. */
    @Composable
    fun accentCard(): Brush {
        val p = LocalTiltPalette.current
        return Brush.linearGradient(listOf(p.accent, blend(p.accent, p.ground, 0.28f)))
    }

    @Composable
    fun header(): Brush {
        val p = LocalTiltPalette.current
        return Brush.verticalGradient(listOf(p.surface, p.ground))
    }

    /** Fades the feed into the ground at its scrolling edge instead of cutting it off. */
    @Composable
    fun scrollEdgeScrim(): Brush {
        val p = LocalTiltPalette.current
        return Brush.verticalGradient(listOf(Color.Transparent, p.ground))
    }

    /** Accent-to-cyan sweep, used by the ambient arcs and the streak ring. */
    @Composable
    fun arcSweep(): List<Color> {
        val p = LocalTiltPalette.current
        return listOf(p.accent, p.accentPartner)
    }
}

/** Mixes [b] into [a] by [amount]. Used to derive a second gradient stop from one token. */
fun blend(a: Color, b: Color, amount: Float): Color = Color(
    red = a.red + (b.red - a.red) * amount,
    green = a.green + (b.green - a.green) * amount,
    blue = a.blue + (b.blue - a.blue) * amount,
    alpha = 1f,
)
