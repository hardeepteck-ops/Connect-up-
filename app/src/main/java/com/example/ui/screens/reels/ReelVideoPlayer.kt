package com.example.ui.screens.reels

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.PrimaryIndigo
import com.example.util.VideoUrlUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * High-performance ExoPlayer-backed video player designed specifically for full-screen Reels.
 * Supports auto-play, looping, mute toggling, progress tracking, automatic legacy URL migration,
 * and robust fallback to working streaming sources.
 */
@OptIn(UnstableApi::class)
@Composable
fun ReelVideoPlayer(
    mediaUrl: String?,
    isActive: Boolean,
    isPlaying: Boolean,
    isMuted: Boolean,
    onProgressUpdate: (Float) -> Unit,
    seekToFraction: Float? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Dynamically resolve URL to guarantee valid streaming endpoint (fixing legacy 403 links)
    val initialResolvedUrl = remember(mediaUrl) {
        VideoUrlUtils.resolvePlayableUrl(mediaUrl)
    }
    var activeUrl by remember(initialResolvedUrl) {
        mutableStateOf(initialResolvedUrl)
    }

    var isBuffering by remember { mutableStateOf(false) }
    var isVideoReady by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }

    // Check if the URL is likely a playable video format
    val isLikelyVideo = remember(activeUrl) {
        if (activeUrl.isNullOrBlank()) false
        else {
            val lower = activeUrl!!.lowercase()
            lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mkv") ||
                    lower.contains("video") || lower.contains("googlevideo") ||
                    activeUrl!!.startsWith("content://") || activeUrl!!.startsWith("file://")
        }
    }

    if (!isLikelyVideo) {
        // Fallback to high-quality image rendering if media is a static photo
        Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            if (!activeUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(activeUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Reel media",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        return
    }

    if (hasPlaybackError) {
        // Clean retry overlay if playback permanently failed
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Video temporarily unavailable",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        hasPlaybackError = false
                        activeUrl = VideoUrlUtils.DEFAULT_FALLBACK_VIDEO_URL
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retry")
                }
            }
        }
        return
    }

    // Configure resilient HTTP media source factory with standard user-agent & cross-protocol redirects
    val mediaSourceFactory = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("ConnectUp/1.0 (Android; ExoPlayer)")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
        DefaultMediaSourceFactory(httpDataSourceFactory)
    }

    // Initialize ExoPlayer instance for this reel item
    val exoPlayer = remember(activeUrl) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE // Continuous seamless looping
                videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                if (!activeUrl.isNullOrBlank()) {
                    val uri = Uri.parse(activeUrl)
                    setMediaItem(MediaItem.fromUri(uri))
                    prepare()
                }
            }
    }

    // Handle lifecycle pause/resume
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isActive && isPlaying) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Handle active and playing state changes (Auto-play when active, pause when swiped away)
    LaunchedEffect(isActive, isPlaying, exoPlayer) {
        if (isActive && isPlaying) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
            if (!isActive) {
                exoPlayer.seekTo(0)
            }
        }
    }

    // Handle mute state changes
    LaunchedEffect(isMuted, exoPlayer) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // Handle scrubber seek fraction
    LaunchedEffect(seekToFraction) {
        seekToFraction?.let { fraction ->
            val duration = exoPlayer.duration
            if (duration > 0) {
                exoPlayer.seekTo((duration * fraction).toLong())
            }
        }
    }

    // Player state listener with automatic fallback on playback error
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                isVideoReady = playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED
                if (isVideoReady) {
                    hasPlaybackError = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.w("ReelVideoPlayer", "Playback error on $activeUrl: ${error.message}")
                if (activeUrl != VideoUrlUtils.DEFAULT_FALLBACK_VIDEO_URL) {
                    // Automatically fallback to guaranteed reliable video stream
                    activeUrl = VideoUrlUtils.DEFAULT_FALLBACK_VIDEO_URL
                } else {
                    hasPlaybackError = true
                    isBuffering = false
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Real-time progress updater loop
    LaunchedEffect(isActive, isPlaying, exoPlayer) {
        while (isActive && isPlaying && kotlinx.coroutines.coroutineScope { this.isActive }) {
            val duration = exoPlayer.duration
            val position = exoPlayer.currentPosition
            if (duration > 0) {
                val progressFraction = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                onProgressUpdate(progressFraction)
            }
            delay(50L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Video View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Custom Reel UI handles interactions
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Crop to fill 9:16 screen
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player != exoPlayer) {
                    playerView.player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Spinner
        AnimatedVisibility(
            visible = isBuffering && !isVideoReady,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo,
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}
