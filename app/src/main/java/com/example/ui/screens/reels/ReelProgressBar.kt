package com.example.ui.screens.reels

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan

/**
 * A thin, horizontal progress bar positioned at the bottom of the Reels player that indicates
 * the current playback progress of the video.
 * Supports smooth visual tracking, gradient highlights, and optional tap/drag scrubber seeking.
 */
@Composable
fun ReelProgressBar(
    progress: Float,
    onSeek: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (isDragging) dragProgress else progress.coerceIn(0f, 1f)
    val barHeight by animateDpAsState(
        targetValue = if (isDragging) 4.5.dp else 2.5.dp,
        animationSpec = tween(durationMillis = 150),
        label = "progress_bar_height"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp) // Accessible touch target height
            .testTag("reel_progress_bar")
            .pointerInput(onSeek) {
                if (onSeek != null) {
                    detectTapGestures { offset ->
                        val newFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(newFraction)
                    }
                }
            }
            .pointerInput(onSeek) {
                if (onSeek != null) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek(dragProgress)
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Horizontal Track Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .background(Color.White.copy(alpha = 0.22f))
                .testTag("reel_progress_track"),
            contentAlignment = Alignment.CenterStart
        ) {
            // Active Progress Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth(currentProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                PrimaryIndigo,
                                SecondaryCyan,
                                Color.White
                            )
                        )
                    )
                    .testTag("reel_progress_indicator")
            )
        }
    }
}
