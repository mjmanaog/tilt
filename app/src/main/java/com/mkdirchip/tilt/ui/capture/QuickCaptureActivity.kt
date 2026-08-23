package com.mkdirchip.tilt.ui.capture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkdirchip.tilt.tiltContainer
import com.mkdirchip.tilt.ui.theme.LocalReducedMotion
import com.mkdirchip.tilt.ui.theme.TiltMotion
import com.mkdirchip.tilt.ui.theme.LocalTiltPalette
import com.mkdirchip.tilt.ui.theme.TiltTheme

/**
 * Capture floating over the home screen. The window is translucent and never drawn opaque, so
 * the launcher keeps rendering behind it — tapping the widget must not feel like launching an
 * app. Tapping outside or going back finishes without writing anything.
 */
class QuickCaptureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = tiltContainer.repository

        setContent {
            TiltTheme {
                val viewModel: CaptureViewModel = viewModel(
                    factory = CaptureViewModel.factory(repository, entryId = null),
                )
                val state by viewModel.state.collectAsState()
                val reduced = LocalReducedMotion.current
                var visible by remember { mutableStateOf(false) }

                // Drives the entrance animation on first composition.
                androidx.compose.runtime.LaunchedEffect(Unit) { visible = true }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Tap anywhere outside the sheet to dismiss without saving.
                        .pointerInput(Unit) { detectTapGestures(onTap = { finish() }) },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = if (reduced) {
                            fadeIn(tween(0))
                        } else {
                            slideInVertically(
                                animationSpec = tween(TiltMotion.STANDARD),
                                initialOffsetY = { it / 3 },
                            ) + fadeIn(tween(TiltMotion.STANDARD))
                        },
                    ) {
                        QuickCaptureSheet(
                            state = state,
                            onTextChange = viewModel::onTextChange,
                            onLabelDraftChange = viewModel::onLabelDraftChange,
                            onAddLabel = viewModel::addLabel,
                            onRemoveLabel = viewModel::removeLabel,
                            onSave = { viewModel.save(onSaved = ::finish) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCaptureSheet(
    state: CaptureUiState,
    onTextChange: (String) -> Unit,
    onLabelDraftChange: (String) -> Unit,
    onAddLabel: (String) -> Unit,
    onRemoveLabel: (String) -> Unit,
    onSave: () -> Unit,
) {
    val palette = LocalTiltPalette.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(palette.surface, RoundedCornerShape(26.dp))
            // Swallow taps on the sheet so they do not reach the dismiss handler behind it.
            .pointerInput(Unit) { detectTapGestures(onTap = { }) }
            .padding(18.dp)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        Text(
            text = if (state.isEditing) "Edit" else "Today I learned",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.onGround,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        CaptureContent(
            state = state,
            onTextChange = onTextChange,
            onLabelDraftChange = onLabelDraftChange,
            onAddLabel = onAddLabel,
            onRemoveLabel = onRemoveLabel,
            onSave = onSave,
            autoFocus = true,
        )
    }
}
