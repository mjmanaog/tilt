package com.mkdirchip.tilt.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mkdirchip.tilt.MainActivity
import com.mkdirchip.tilt.R
import com.mkdirchip.tilt.data.TilEntryEntity
import com.mkdirchip.tilt.tiltContainer
import com.mkdirchip.tilt.ui.capture.QuickCaptureActivity
import com.mkdirchip.tilt.ui.formatStripStamp
import com.mkdirchip.tilt.ui.formatWidgetStamp

/** Extra carrying the entry the widget was showing, so the app can open it. */
const val EXTRA_ENTRY_ID = "com.mkdirchip.tilt.EXTRA_ENTRY_ID"

/**
 * Two jobs on one surface: a tap target that opens capture over the home screen, and a rotating
 * glimpse of something already learned.
 */
class TiltWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.tiltContainer.repository
        // Read once up front so the very first frame is already correct.
        val initial = repository.randomEntry()

        provideContent {
            // Collected inside the composition on purpose: update() recomposes an existing
            // session without re-running provideGlance, so an entry captured out here would be
            // frozen for the life of the session and a save or delete would never show up.
            val entries by repository.observeEntries().collectAsState(initial = null)
            val loaded = entries

            // Which entry is shown is remembered, so the widget cannot flicker between entries
            // across recompositions. Its content is then looked up fresh every pass, so editing
            // the displayed entry updates the widget instead of showing the old text.
            val pickedId = remember(loaded?.size) { loaded?.randomOrNull()?.entry?.id }
            val entry = if (loaded == null) {
                initial
            } else {
                loaded.firstOrNull { it.entry.id == pickedId }?.entry
                    ?: loaded.randomOrNull()?.entry
            }

            WidgetBody(entry)
        }
    }
}

@Composable
private fun WidgetBody(entry: TilEntryEntity?) {
    // A 4x1 strip has no room to stack anything; above that the taller arrangement fits.
    if (LocalSize.current.height < 90.dp) StripBody(entry) else StackedBody(entry)
}

/** The single-row layout: entry, its date, and a compact capture target, all on one line. */
@Composable
private fun StripBody(entry: TilEntryEntity?) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (entry == null) {
            Text(
                text = "What did you learn today?",
                style = TextStyle(
                    color = ColorProvider(R.color.tilt_on_ground),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
                    .clickable(actionStartActivity(quickCaptureIntent())),
            )
        } else {
            val openEntry = Intent(LocalContext.current, MainActivity::class.java)
                .putExtra(EXTRA_ENTRY_ID, entry.id)
            // The entry text absorbs the truncation; the date is a fixed-width sibling so it
            // cannot be the thing that gets ellipsised away.
            Text(
                text = entry.text,
                style = TextStyle(
                    color = ColorProvider(R.color.tilt_on_ground),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight()
                    .clickable(actionStartActivity(openEntry)),
            )
            Text(
                text = "  ${formatStripStamp(entry.createdAtEpochMillis)}",
                style = TextStyle(
                    color = ColorProvider(R.color.tilt_muted),
                    fontSize = 11.sp,
                ),
                maxLines = 1,
            )
        }
        Spacer(GlanceModifier.width(10.dp))
        CompactCaptureTarget()
    }
}

/** The taller layout, once the widget has height for a stacked arrangement. */
@Composable
private fun StackedBody(entry: TilEntryEntity?) {
    val height = LocalSize.current.height
    // Widget space is fixed, so this is the one place entry text may be truncated. The budget is
    // deliberately conservative: asking for more lines than the space fits makes Glance clip the
    // last line mid-glyph instead of ellipsising it.
    val bodyLines = when {
        height < 120.dp -> 2
        height < 170.dp -> 3
        height < 220.dp -> 4
        else -> 6
    }
    val compact = height < 120.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(14.dp),
    ) {
        if (entry == null) {
            EmptyPrompt()
        } else {
            // defaultWeight() is a ColumnScope member, so it is applied here and passed down.
            ResurfacedEntry(
                entry = entry,
                maxLines = bodyLines,
                compact = compact,
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            )
            Spacer(GlanceModifier.height(8.dp))
            CaptureTarget()
        }
    }
}

/** The strip's capture affordance: small, but a real tap target at the row's full height. */
@Composable
private fun CompactCaptureTarget() {
    Text(
        text = "+",
        style = TextStyle(
            color = ColorProvider(R.color.tilt_on_accent),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        ),
        maxLines = 1,
        modifier = GlanceModifier
            .cornerRadius(10.dp)
            .background(ColorProvider(R.color.tilt_accent))
            .clickable(actionStartActivity(quickCaptureIntent()))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Shown when nothing has been captured yet. Tapping still opens capture. */
@Composable
private fun EmptyPrompt() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(quickCaptureIntent())),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = "What did you learn today?",
            style = TextStyle(
                color = ColorProvider(R.color.tilt_on_ground),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 3,
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = "Tap to capture",
            style = TextStyle(
                color = ColorProvider(R.color.tilt_accent_partner),
                fontSize = 12.sp,
            ),
            maxLines = 1,
        )
    }
}

/** Tapping the resurfaced entry opens that entry in the app. */
@Composable
private fun ResurfacedEntry(
    entry: TilEntryEntity,
    maxLines: Int,
    compact: Boolean,
    modifier: GlanceModifier,
) {
    val context = LocalContext.current
    val openEntry = Intent(context, MainActivity::class.java)
        .putExtra(EXTRA_ENTRY_ID, entry.id)

    Column(modifier = modifier.clickable(actionStartActivity(openEntry))) {
        // The text takes the leftover space and truncates; without this weight a long entry
        // pushes the date stamp out from under it and the stamp gets clipped instead.
        Text(
            text = entry.text,
            style = TextStyle(
                color = ColorProvider(R.color.tilt_on_ground),
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = maxLines,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = formatWidgetStamp(entry.createdAtEpochMillis),
            style = TextStyle(
                color = ColorProvider(R.color.tilt_muted),
                fontSize = 11.sp,
            ),
            maxLines = 1,
        )
    }
}

/** Builds the intent for the translucent capture surface. */
@Composable
private fun quickCaptureIntent(): Intent =
    Intent(LocalContext.current, QuickCaptureActivity::class.java)

@Composable
private fun CaptureTarget() {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(14.dp)
            .background(ColorProvider(R.color.tilt_accent))
            .clickable(actionStartActivity(quickCaptureIntent()))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = "+  Today I learned…",
            style = TextStyle(
                color = ColorProvider(R.color.tilt_on_accent),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
        )
    }
}
