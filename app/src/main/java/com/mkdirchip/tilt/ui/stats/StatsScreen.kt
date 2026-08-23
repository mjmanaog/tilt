package com.mkdirchip.tilt.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mkdirchip.tilt.ui.theme.AmbientArcs
import com.mkdirchip.tilt.ui.theme.LocalTiltPalette
import com.mkdirchip.tilt.ui.theme.TiltGradients
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(
    state: StatsUiState,
    weekStart: DayOfWeek,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val palette = LocalTiltPalette.current

    Box(modifier = Modifier.fillMaxSize().background(palette.ground)) {
        // Statistics is sparse enough to carry the ambient arcs.
        AmbientArcs(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                // No Scaffold here, so window insets have to be applied explicitly or the
                // heading sits under the status bar and the calendar under the navigation bar.
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "SUMMARIES",
                style = MaterialTheme.typography.labelSmall,
                color = palette.muted,
            )
            Spacer(Modifier.height(6.dp))

            if (!state.hasAnyEntries) {
                Text(
                    text = "Nothing yet",
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.onGround,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Capture something and this fills in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            }

            Spacer(Modifier.height(18.dp))
            StreakRing(
                progress = state.monthProgress,
                currentStreak = state.currentStreak,
            )

            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryTile(
                    modifier = Modifier.weight(1f),
                    caption = "This week",
                    value = state.week.entries.toString(),
                    detail = daysLabel(state.week.activeDays),
                )
                SummaryTile(
                    modifier = Modifier.weight(1f),
                    caption = "This month",
                    value = state.month.entries.toString(),
                    detail = daysLabel(state.month.activeDays),
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryTile(
                    modifier = Modifier.weight(1f),
                    caption = "Current streak",
                    value = state.currentStreak.toString(),
                    detail = if (state.currentStreak == 1) "day" else "days",
                )
                SummaryTile(
                    modifier = Modifier.weight(1f),
                    caption = "Longest streak",
                    value = state.longestStreak.toString(),
                    detail = if (state.longestStreak == 1) "day" else "days",
                )
            }

            Spacer(Modifier.height(26.dp))
            MonthCalendar(
                month = state.visibleMonth,
                countsByDate = state.countsByDate,
                today = state.today,
                weekStart = weekStart,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

private fun daysLabel(days: Int): String = if (days == 1) "on 1 day" else "on $days days"

/** The reference's gradient ring: an arc stroke with a white cap at its leading end. */
@Composable
private fun StreakRing(progress: Float, currentStreak: Int) {
    val palette = LocalTiltPalette.current
    val sweepColors = TiltGradients.arcSweep()

    Box(
        modifier = Modifier.fillMaxWidth().height(210.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(190.dp)) {
            val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(inset, inset)

            // Track.
            drawArc(
                color = palette.surfaceLift,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )

            val sweep = 360f * progress.coerceIn(0f, 1f)
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.linearGradient(sweepColors),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )

                // White dot terminator at the leading end.
                val angle = Math.toRadians((-90f + sweep).toDouble())
                val radius = arcSize.minDimension / 2
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = Offset(
                        x = center.x + (radius * kotlin.math.cos(angle)).toFloat(),
                        y = center.y + (radius * kotlin.math.sin(angle)).toFloat(),
                    ),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                color = palette.onGround,
            )
            Text(
                text = if (currentStreak == 1) "1 day streak" else "$currentStreak day streak",
                style = MaterialTheme.typography.labelMedium,
                color = palette.muted,
            )
        }
    }
}

@Composable
private fun SummaryTile(
    caption: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalTiltPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TiltGradients.card())
            .padding(16.dp),
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelMedium,
            color = palette.muted,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            color = palette.onGround,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelMedium,
            color = palette.muted,
        )
    }
}

private val monthTitleFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

@Composable
private fun MonthCalendar(
    month: YearMonth,
    countsByDate: Map<LocalDate, Int>,
    today: LocalDate,
    weekStart: DayOfWeek,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val palette = LocalTiltPalette.current

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = month.atDay(1).format(monthTitleFormatter),
                style = MaterialTheme.typography.headlineMedium,
                color = palette.onGround,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.ChevronLeft, "Previous month", tint = palette.onGround)
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.ChevronRight, "Next month", tint = palette.onGround)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Weekday headings, starting on the locale's first day of week.
        val headings = (0..6).map { weekStart.plus(it.toLong()) }
        Row(Modifier.fillMaxWidth()) {
            headings.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        val firstOfMonth = month.atDay(1)
        val leadingBlanks = (firstOfMonth.dayOfWeek.value - weekStart.value + 7) % 7
        val cells = leadingBlanks + month.lengthOfMonth()
        val rows = (cells + 6) / 7
        val busiest = countsByDate.values.maxOrNull() ?: 1

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (column in 0 until 7) {
                    val cellIndex = row * 7 + column
                    val dayOfMonth = cellIndex - leadingBlanks + 1
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).padding(3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayOfMonth in 1..month.lengthOfMonth()) {
                            DayCell(
                                date = month.atDay(dayOfMonth),
                                count = countsByDate[month.atDay(dayOfMonth)] ?: 0,
                                busiest = busiest,
                                today = today,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, count: Int, busiest: Int, today: LocalDate) {
    val palette = LocalTiltPalette.current
    val isFuture = date.isAfter(today)

    // Busier days carry more weight; future days are inert and never marked.
    val weight = if (count == 0) 0f else (count.toFloat() / busiest).coerceIn(0.35f, 1f)
    val background = when {
        isFuture -> Color.Transparent
        count == 0 -> palette.surface
        else -> palette.accent.copy(alpha = weight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = when {
                isFuture -> palette.muted.copy(alpha = 0.4f)
                count > 0 -> palette.onAccent
                else -> palette.muted
            },
        )
    }
}
