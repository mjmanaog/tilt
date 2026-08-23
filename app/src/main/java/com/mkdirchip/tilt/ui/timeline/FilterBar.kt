package com.mkdirchip.tilt.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkdirchip.tilt.data.LabelEntity
import com.mkdirchip.tilt.ui.theme.LocalTiltPalette
import java.time.Instant
import java.time.ZoneId

@Composable
fun FilterBar(
    dateFilter: DateFilter,
    labels: List<LabelEntity>,
    selectedLabelIds: Set<Long>,
    onDateFilter: (DateFilter) -> Unit,
    onToggleLabel: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRangePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 18.dp),
        ) {
            item {
                FilterChip("All", dateFilter is DateFilter.All) { onDateFilter(DateFilter.All) }
            }
            item {
                FilterChip("Today", dateFilter is DateFilter.Today) { onDateFilter(DateFilter.Today) }
            }
            item {
                FilterChip("This week", dateFilter is DateFilter.ThisWeek) {
                    onDateFilter(DateFilter.ThisWeek)
                }
            }
            item {
                FilterChip("This month", dateFilter is DateFilter.ThisMonth) {
                    onDateFilter(DateFilter.ThisMonth)
                }
            }
            item {
                FilterChip(
                    label = customChipLabel(dateFilter),
                    selected = dateFilter is DateFilter.Custom,
                ) { showRangePicker = true }
            }
        }

        if (labels.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
            ) {
                items(labels, key = { it.id }) { label ->
                    FilterChip(
                        label = label.displayName,
                        selected = label.id in selectedLabelIds,
                    ) { onToggleLabel(label.id) }
                }
            }
        }
    }

    if (showRangePicker) {
        RangePickerDialog(
            onDismiss = { showRangePicker = false },
            onPicked = { start, end ->
                onDateFilter(DateFilter.Custom(start, end))
                showRangePicker = false
            },
        )
    }
}

private fun customChipLabel(filter: DateFilter): String =
    if (filter is DateFilter.Custom) "${filter.start} → ${filter.end}" else "Custom range"

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalTiltPalette.current
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) palette.onAccent else palette.muted,
        modifier = Modifier
            .background(
                color = if (selected) palette.accent else palette.surface,
                shape = RoundedCornerShape(50),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangePickerDialog(
    onDismiss: () -> Unit,
    onPicked: (java.time.LocalDate, java.time.LocalDate) -> Unit,
) {
    val state = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null,
                onClick = {
                    val startMillis = state.selectedStartDateMillis ?: return@TextButton
                    // A single tap selects one day: treat the start as the end too.
                    val endMillis = state.selectedEndDateMillis ?: startMillis
                    onPicked(startMillis.toLocalDate(), endMillis.toLocalDate())
                },
            ) { Text("Apply") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DateRangePicker(state = state, showModeToggle = false)
    }
}

/** The picker reports UTC midnight, so read the date in UTC rather than shifting a day. */
private fun Long.toLocalDate(): java.time.LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()
