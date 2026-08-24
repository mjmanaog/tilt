package com.mkdirchip.tilt.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Gradient arcs drawn **concentrically around the centre of whatever box they are placed in**, so
 * when they sit behind the streak ring they read as a halo echoing it rather than as bands
 * crossing the screen at an arbitrary angle.
 *
 * Every radius starts outside the ring itself, which keeps the arcs clear of the figure at the
 * centre. They are decoration only: no layout role, one draw pass, low alpha.
 */
@Composable
fun AmbientArcs(
    modifier: Modifier = Modifier,
    alpha: Float = 0.30f,
) {
    val sweepColors = TiltGradients.arcSweep()

    Canvas(modifier = modifier) {
        val centre = Offset(size.width / 2f, size.height / 2f)
        val stroke = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
        // Largest radius that still fits inside the box once the stroke is accounted for, so the
        // arcs always end in a round cap rather than a hard edge where the canvas clips them.
        val unit = (size.minDimension / 2f) - stroke.width
        val brush = Brush.linearGradient(
            colors = sweepColors,
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f),
        )

        // radius factor, start angle, sweep, relative alpha — staggered so the arcs read as one
        // family rather than a set of concentric circles.
        val rings = listOf(
            Quad(0.72f, -150f, 130f, 1.00f),
            Quad(0.86f, 25f, 105f, 0.70f),
            Quad(1.00f, 160f, 78f, 0.45f),
        )

        rings.forEach { (factor, start, sweep, relAlpha) ->
            val r = unit * factor
            drawArc(
                brush = brush,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(centre.x - r, centre.y - r),
                size = Size(r * 2f, r * 2f),
                style = stroke,
                alpha = alpha * relAlpha,
            )
        }
    }
}

private data class Quad(
    val factor: Float,
    val start: Float,
    val sweep: Float,
    val relAlpha: Float,
)
