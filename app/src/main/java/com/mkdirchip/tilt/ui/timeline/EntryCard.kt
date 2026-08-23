package com.mkdirchip.tilt.ui.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkdirchip.tilt.data.TilEntryWithLabels
import com.mkdirchip.tilt.ui.formatCardStamp
import com.mkdirchip.tilt.ui.theme.LocalTiltPalette
import com.mkdirchip.tilt.ui.theme.TiltGradients

/** The feed clamps a preview at six lines; the full text lives in the entry view. */
const val CARD_TEXT_MAX_LINES = 6

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun EntryCard(
    item: TilEntryWithLabels,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalTiltPalette.current
    val accented = isAccentCard(item.entry.id)

    val textColor = if (accented) palette.onAccent else palette.onGround
    val metaColor = if (accented) palette.onAccentMuted else palette.muted
    val chipColor = if (accented) palette.onAccentMuted else palette.surfaceLift

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (accented) TiltGradients.accentCard() else TiltGradients.card())
            .combinedClickable(onClick = onOpen, onLongClick = onDelete)
            .padding(16.dp),
    ) {
        Text(
            text = item.entry.text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            maxLines = CARD_TEXT_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(12.dp))

        // Metadata sits at the card's foot, below the text.
        if (item.labels.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                item.labels.forEach { label ->
                    Text(
                        text = label.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (accented) palette.accent else palette.onGround,
                        modifier = Modifier
                            .background(chipColor, RoundedCornerShape(50))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                }
            }
        }

        Text(
            text = formatCardStamp(item.entry.createdAtEpochMillis),
            style = MaterialTheme.typography.labelMedium,
            color = metaColor,
        )
    }
}
