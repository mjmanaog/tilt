package com.mkdirchip.tilt.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mkdirchip.tilt.ui.theme.LocalTiltPalette

/**
 * The capture form. Used by the in-app editor and by the widget's overlay, so validation and
 * label behaviour cannot diverge between them.
 */
@Composable
fun CaptureContent(
    state: CaptureUiState,
    onTextChange: (String) -> Unit,
    onLabelDraftChange: (String) -> Unit,
    onAddLabel: (String) -> Unit,
    onRemoveLabel: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
) {
    val palette = LocalTiltPalette.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // Requesting focus does not raise the IME on its own, and capture is meant to be ready to
    // type into the moment it opens.
    LaunchedEffect(autoFocus, state.loaded) {
        if (autoFocus && state.loaded) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {

        OutlinedTextField(
            value = state.text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = "What did you learn?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.muted,
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = fieldColors(),
            shape = RoundedCornerShape(18.dp),
        )

        // Attached labels.
        if (state.labels.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.labels, key = { it }) { label ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(palette.accent, RoundedCornerShape(50))
                            .clickable { onRemoveLabel(label) }
                            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.onAccent,
                        )
                        Spacer(Modifier.size(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove label $label",
                            tint = palette.onAccent,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.labelDraft,
            onValueChange = onLabelDraftChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Add a label — science, music, general…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted,
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAddLabel(state.labelDraft) }),
            colors = fieldColors(),
            shape = RoundedCornerShape(14.dp),
        )

        // Suggestions drawn from labels already in use.
        if (state.suggestions.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.suggestions, key = { it }) { suggestion ->
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.onGround,
                        modifier = Modifier
                            .background(palette.surfaceLift, RoundedCornerShape(50))
                            .clickable { onAddLabel(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Button(
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.accent,
                contentColor = palette.onAccent,
                disabledContainerColor = palette.surfaceLift,
                disabledContentColor = palette.muted,
            ),
        ) {
            Text(
                text = if (state.isEditing) "Save changes" else "Save",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = LocalTiltPalette.current.onGround,
    unfocusedTextColor = LocalTiltPalette.current.onGround,
    focusedContainerColor = LocalTiltPalette.current.surface,
    unfocusedContainerColor = LocalTiltPalette.current.surface,
    cursorColor = LocalTiltPalette.current.accentPartner,
    focusedBorderColor = LocalTiltPalette.current.accent,
    unfocusedBorderColor = Color.Transparent,
)
