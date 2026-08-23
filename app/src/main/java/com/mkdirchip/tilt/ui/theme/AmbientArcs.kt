package com.mkdirchip.tilt.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Sweeping gradient arcs, used behind the sparse screens — statistics and empty states — and
 * never behind the timeline feed, where they would compete with card text.
 *
 * Decoration only: no layout role, one draw pass, and kept at a low alpha so text sitting over
 * it keeps its contrast against the ground.
 */
@Composable
fun AmbientArcs(
    modifier: Modifier = Modifier,
    alpha: Float = 0.22f,
) {
    val sweep = TiltGradients.arcSweep()

    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 44.dp.toPx())
        val brush = Brush.linearGradient(
            colors = sweep,
            start = Offset(size.width * 0.1f, 0f),
            end = Offset(size.width, size.height * 0.8f),
        )

        // Two concentric arcs anchored off the top-right, echoing the reference's rings.
        val outer = size.minDimension * 1.15f
        val inner = size.minDimension * 0.72f

        drawArc(
            brush = brush,
            startAngle = 130f,
            sweepAngle = 165f,
            useCenter = false,
            topLeft = Offset(size.width - outer * 0.55f, -outer * 0.45f),
            size = Size(outer, outer),
            style = stroke,
            alpha = alpha,
        )
        drawArc(
            brush = brush,
            startAngle = 155f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width - inner * 0.9f, -inner * 0.15f),
            size = Size(inner, inner),
            style = stroke,
            alpha = alpha * 0.7f,
        )
        // A third, low arc anchored bottom-left to balance the composition.
        val low = size.minDimension * 0.9f
        drawArc(
            brush = brush,
            startAngle = 300f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(-low * 0.5f, size.height - low * 0.5f),
            size = Size(low, low),
            style = stroke,
            alpha = alpha * 0.55f,
        )
    }
}
