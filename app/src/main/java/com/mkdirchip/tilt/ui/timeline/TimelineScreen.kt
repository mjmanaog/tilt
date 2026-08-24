package com.mkdirchip.tilt.ui.timeline

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mkdirchip.tilt.ui.theme.LocalReducedMotion
import com.mkdirchip.tilt.ui.theme.LocalTiltPalette
import com.mkdirchip.tilt.ui.theme.TiltGradients
import com.mkdirchip.tilt.ui.theme.TiltMotion
import com.mkdirchip.tilt.ui.theme.tiltDelayedSpec

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    onCapture: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    onOpenStats: () -> Unit,
) {
    val palette = LocalTiltPalette.current
    val state by viewModel.state.collectAsState()
    val hasAnyEntries by viewModel.hasAnyEntries.collectAsState()
    val lastDeleted by viewModel.lastDeleted.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    // Undo affordance for a deletion.
    LaunchedEffect(lastDeleted) {
        val deleted = lastDeleted ?: return@LaunchedEffect
        val result = snackbarHost.showSnackbar(
            message = "Entry deleted",
            actionLabel = "Undo",
            // Explicit: with an action label the default is Indefinite, which would never lapse,
            // and the deletion is specified to become permanent once the chance passes.
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        } else {
            viewModel.forgetDeleted()
        }
    }

    Scaffold(
        containerColor = palette.ground,
        snackbarHost = { SnackbarHost(snackbarHost) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCapture,
                containerColor = palette.accent,
                contentColor = palette.onAccent,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add what you learned")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "TILT",
                        style = MaterialTheme.typography.displayMedium,
                        color = palette.onGround,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onOpenStats) {
                        Icon(
                            imageVector = Icons.Filled.Insights,
                            contentDescription = "Statistics",
                            tint = palette.onGround,
                        )
                    }
                }

                FilterBar(
                    dateFilter = state.dateFilter,
                    labels = state.labels,
                    selectedLabelIds = state.selectedLabelIds,
                    onDateFilter = viewModel::setDateFilter,
                    onToggleLabel = viewModel::toggleLabel,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                when {
                    state.loading -> Spacer(Modifier.fillMaxSize())

                    state.entries.isEmpty() && !hasAnyEntries -> FirstRunEmptyState()

                    state.entries.isEmpty() ->
                        NoMatchesEmptyState(onClear = viewModel::clearFilters)

                    else -> Feed(
                        state = state,
                        onOpenEntry = onOpenEntry,
                        onDelete = viewModel::delete,
                    )
                }
            }

            // Fades the feed into the ground at the scrolling edge.
            if (state.entries.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(TiltGradients.scrollEdgeScrim()),
                )
            }
        }
    }
}

@Composable
private fun Feed(
    state: TimelineUiState,
    onOpenEntry: (Long) -> Unit,
    onDelete: (com.mkdirchip.tilt.data.TilEntryWithLabels) -> Unit,
) {
    val reduced = LocalReducedMotion.current

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(state.entries, key = { _, item -> item.entry.id }) { index, item ->
            // Cards animate into place rather than appearing fully formed.
            val appeared by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tiltDelayedSpec(
                    durationMillis = if (reduced) 0 else TiltMotion.ENTRANCE,
                    delayMillis = TiltMotion.stagger(index, reduced),
                ),
                label = "cardAppear",
            )

            EntryCard(
                item = item,
                onOpen = { onOpenEntry(item.entry.id) },
                onDelete = { onDelete(item) },
                modifier = Modifier
                    .alpha(appeared)
                    .animateItem(),
            )
        }
    }
}

/**
 * Nothing has ever been captured. Dimmed placeholders trace the shape the feed will take, so the
 * first screen shows what the app becomes rather than an empty field.
 */
@Composable
private fun FirstRunEmptyState() {
    val palette = LocalTiltPalette.current

    Box(modifier = Modifier.fillMaxSize()) {
        GhostFeed(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 14.dp, bottom = 96.dp)
        )

        // Keeps the prompt clear of the placeholders behind it.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    // Not fully opaque: the placeholders stay faintly visible through it, so the
                    // feed reads as one continuous shape rather than two disconnected bands.
                    Brush.verticalGradient(
                        0.20f to Color.Transparent,
                        0.40f to palette.ground.copy(alpha = 0.90f),
                        0.60f to palette.ground.copy(alpha = 0.90f),
                        0.80f to Color.Transparent,
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "What did you learn today?",
                style = MaterialTheme.typography.headlineLarge,
                color = palette.onGround,
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = "Capture the first thing. The widget makes it a single tap.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
        }
    }
}

/**
 * Entries exist, the filters just exclude them all. Deliberately undecorated: this is a dead end
 * to back out of, not an invitation, and placeholders here would suggest content that is not there.
 */
@Composable
private fun NoMatchesEmptyState(onClear: () -> Unit) {
    val palette = LocalTiltPalette.current

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Nothing matches",
                style = MaterialTheme.typography.headlineLarge,
                color = palette.onGround,
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = "No entries fall inside these filters.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            Spacer(Modifier.size(18.dp))
            Text(
                text = "Clear filters",
                style = MaterialTheme.typography.titleMedium,
                color = palette.accentPartner,
                modifier = Modifier
                    .background(palette.surface, RoundedCornerShape(50))
                    .clickable(onClick = onClear)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

/** Non-interactive silhouettes of cards, in the same staggered two-column rhythm as the feed. */
@Composable
private fun GhostFeed(modifier: Modifier = Modifier) {
    val columns = listOf(
        listOf(156.dp, 92.dp, 132.dp, 108.dp),
        listOf(104.dp, 172.dp, 88.dp, 148.dp),
    )

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        columns.forEach { heights ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                heights.forEach { height ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                            .clip(RoundedCornerShape(22.dp))
                            .alpha(0.45f)
                            .background(TiltGradients.card())
                    )
                }
            }
        }
    }
}
