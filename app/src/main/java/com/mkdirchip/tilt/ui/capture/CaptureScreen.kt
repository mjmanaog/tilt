package com.mkdirchip.tilt.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkdirchip.tilt.ui.theme.LocalTiltPalette

/**
 * The in-app editor, for a new entry or an existing one. Unlike the feed's card, this shows the
 * entry's text in full — no clamp, no ellipsis.
 */
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val palette = LocalTiltPalette.current
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.ground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = palette.onGround)
            }
            Text(
                text = if (state.isEditing) "Edit entry" else "New entry",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.onGround,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Fields take the space above; the save action is pinned to the bottom edge so it stays
        // within thumb reach instead of floating in the middle of a tall screen.
        CaptureContent(
            state = state,
            onTextChange = viewModel::onTextChange,
            onLabelDraftChange = viewModel::onLabelDraftChange,
            onAddLabel = viewModel::addLabel,
            onRemoveLabel = viewModel::removeLabel,
            onSave = { viewModel.save(onSaved = onDone) },
            autoFocus = !state.isEditing,
            inlineSave = false,
        )

        Spacer(Modifier.weight(1f))

        CaptureSaveButton(
            state = state,
            onSave = { viewModel.save(onSaved = onDone) },
            modifier = Modifier.padding(bottom = 20.dp),
        )
    }
}
