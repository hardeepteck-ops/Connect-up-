package com.example.ui.screens.reels

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PostUiModel
import com.example.data.model.UserEntity
import com.example.ui.components.ReportDialog
import com.example.util.VideoUrlUtils
import com.example.ui.components.UserAvatar
import com.example.ui.screens.feed.CommentsBottomSheet
import com.example.ui.theme.HeartRed
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelsScreen(
    currentUserId: Long,
    reels: List<PostUiModel>,
    initialPostId: Long? = null,
    onLikeClick: (Long) -> Unit,
    onSaveClick: (Long) -> Unit,
    onDeleteReelClick: (Long) -> Unit,
    onReportReelClick: (Long, String, String) -> Unit,
    onUserClick: (Long) -> Unit,
    onFollowToggle: (Long) -> Unit,
    onCreateReelClick: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onLoadComments: (Long) -> Unit,
    commentsContent: @Composable (postId: Long, onDismiss: () -> Unit) -> Unit,
    onReelViewed: (Long) -> Unit,
    onHashtagClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var activeTopicFilter by remember { mutableStateOf<String?>(null) }

    val displayedReels = remember(reels, activeTopicFilter) {
        if (activeTopicFilter.isNullOrBlank()) {
            reels
        } else {
            val filter = activeTopicFilter!!.lowercase().removePrefix("#")
            val filtered = reels.filter { item ->
                item.post.caption.lowercase().contains("#$filter") ||
                item.post.hashtags.lowercase().contains(filter)
            }
            if (filtered.isNotEmpty()) filtered else reels
        }
    }

    if (displayedReels.isEmpty()) {
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
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (activeTopicFilter != null) "No Reels for #$activeTopicFilter" else "No Reels Yet",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (activeTopicFilter != null) "Try exploring other topics or clear the filter." else "Be the first to share a short video reel with the ConnectUp community!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (activeTopicFilter != null) {
                    Button(
                        onClick = { activeTopicFilter = null },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("Show All Reels", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onCreateReelClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.testTag("create_first_reel_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Reel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val initialIndex = remember(initialPostId, displayedReels) {
        if (initialPostId != null) {
            displayedReels.indexOfFirst { it.post.id == initialPostId }.coerceAtLeast(0)
        } else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { displayedReels.size }
    )

    // Ensure we scroll to the initial target reel if index changes or loads
    LaunchedEffect(initialIndex, displayedReels.size) {
        if (initialIndex in displayedReels.indices && pagerState.currentPage != initialIndex) {
            pagerState.scrollToPage(initialIndex)
        }
    }

    // Trigger view increment when page changes
    LaunchedEffect(pagerState.currentPage) {
        val currentReel = displayedReels.getOrNull(pagerState.currentPage)
        if (currentReel != null) {
            onReelViewed(currentReel.post.id)
        }
    }

    var isAudioMuted by remember { mutableStateOf(false) }
    var commentSheetPostId by remember { mutableStateOf<Long?>(null) }
    var reportReelPostId by remember { mutableStateOf<Long?>(null) }

    val handleHashtagClick: (String) -> Unit = { tag ->
        val clean = tag.removePrefix("#")
        if (onHashtagClick != null) {
            onHashtagClick(clean)
        } else {
            activeTopicFilter = clean
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("reels_vertical_pager"),
            beyondViewportPageCount = 1,
            key = { displayedReels[it].post.id },
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
        ) { page ->
            val reel = displayedReels[page]
            val isCurrentActive = page == pagerState.currentPage
            ReelItemPage(
                reel = reel,
                isActive = isCurrentActive,
                isMuted = isAudioMuted,
                onToggleMute = { isAudioMuted = !isAudioMuted },
                currentUserId = currentUserId,
                onLikeClick = { onLikeClick(reel.post.id) },
                onSaveClick = { onSaveClick(reel.post.id) },
                onCommentClick = {
                    onLoadComments(reel.post.id)
                    commentSheetPostId = reel.post.id
                },
                onUserClick = { onUserClick(reel.author.id) },
                onFollowToggle = { onFollowToggle(reel.author.id) },
                onReportClick = { reportReelPostId = reel.post.id },
                onDeleteClick = { onDeleteReelClick(reel.post.id) },
                onHashtagClick = handleHashtagClick
            )
        }

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .testTag("reels_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = "Reels",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White
                )

                if (displayedReels.size > 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${displayedReels.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Right actions: Mute toggle & Create Reel button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isAudioMuted = !isAudioMuted },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("reels_top_mute_toggle")
                ) {
                    Icon(
                        imageVector = if (isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isAudioMuted) "Unmute video audio" else "Mute video audio",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = onCreateReelClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag("create_reel_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Create Reel",
                        tint = Color.White
                    )
                }
            }
        }

        // Active Topic Filter Banner
        activeTopicFilter?.let { topic ->
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.8f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 56.dp)
                    .testTag("active_topic_filter_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = SecondaryCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "#$topic (${displayedReels.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { activeTopicFilter = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear topic filter",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Comments Bottom Sheet overlay
        commentSheetPostId?.let { postId ->
            commentsContent(postId) {
                commentSheetPostId = null
            }
        }

        // Report Dialog overlay
        reportReelPostId?.let { postId ->
            ReportDialog(
                targetName = "Reel",
                onDismiss = { reportReelPostId = null },
                onSubmitReport = { reason, desc ->
                    onReportReelClick(postId, reason, desc)
                    reportReelPostId = null
                }
            )
        }

        // Swipe up cue on first reel to indicate vertical swiping
        val showSwipeCue by remember {
            derivedStateOf { pagerState.currentPage == 0 && !pagerState.isScrollInProgress && reels.size > 1 }
        }
        AnimatedVisibility(
            visible = showSwipeCue,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Swipe up for next reel",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Swipe up",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ReelItemPage(
    reel: PostUiModel,
    isActive: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    currentUserId: Long,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onUserClick: () -> Unit,
    onFollowToggle: () -> Unit,
    onReportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onHashtagClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val videoLink = remember(reel.post.id, reel.post.mediaUrl) {
        val url = reel.post.mediaUrl
        if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
            url
        } else {
            "https://connectup.app/reel/${reel.post.id}"
        }
    }

    val triggerShareIntent: () -> Unit = {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this reel by @${reel.author.username}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Watch @${reel.author.username}'s reel on ConnectUp:\n$videoLink\n\n\"${reel.post.caption}\""
            )
        }
        val chooserIntent = Intent.createChooser(sendIntent, "Share video link via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }

    var isPlaying by remember(isActive) { mutableStateOf(isActive) }
    var hasToggledMute by remember { mutableStateOf(false) }
    var showMuteToast by remember { mutableStateOf(false) }
    var showPauseOverlay by remember { mutableStateOf(false) }
    var showHeartPop by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableStateOf<Float?>(null) }
    var showBookmarkFeedback by remember { mutableStateOf(false) }
    var bookmarkFeedbackMessage by remember { mutableStateOf("") }

    val handleBookmarkClick: () -> Unit = {
        val willBeSaved = !reel.isSavedByCurrentUser
        onSaveClick()
        bookmarkFeedbackMessage = if (willBeSaved) {
            "Saved to private 'Favorites' collection"
        } else {
            "Removed from Favorites"
        }
        showBookmarkFeedback = true
        Toast.makeText(context, bookmarkFeedbackMessage, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(showBookmarkFeedback) {
        if (showBookmarkFeedback) {
            delay(2200)
            showBookmarkFeedback = false
        }
    }

    fun handleToggleMute() {
        hasToggledMute = true
        onToggleMute()
    }

    LaunchedEffect(isMuted) {
        if (hasToggledMute) {
            showMuteToast = true
            delay(1000)
            showMuteToast = false
        }
    }

    // Reel simulated playback progress (updated directly by ExoPlayer, or fallback timer for static media)
    var progress by remember(isActive) { mutableFloatStateOf(0f) }

    val isVideo = remember(reel.post.mediaUrl) {
        val resolved = VideoUrlUtils.resolvePlayableUrl(reel.post.mediaUrl)
        resolved?.let {
            val l = it.lowercase()
            l.endsWith(".mp4") || l.endsWith(".webm") || l.endsWith(".mkv") ||
                    l.contains("video") || l.contains("googlevideo") ||
                    it.startsWith("content://") || it.startsWith("file://")
        } ?: false
    }

    LaunchedEffect(isActive, isPlaying, isVideo) {
        if (!isVideo && isActive && isPlaying) {
            val duration = 15000L
            val step = 50L
            while (isActive && isPlaying) {
                delay(step)
                progress = (progress + (step.toFloat() / duration)) % 1f
            }
        } else if (!isActive) {
            progress = 0f
        }
    }

    // Audio disc rotation
    val infiniteTransition = rememberInfiniteTransition(label = "audio_disc")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_spin"
    )

    // Instagram double-tap heart pop animation
    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

    fun triggerHeartPop() {
        if (!reel.isLikedByCurrentUser) {
            onLikeClick()
        }
        showHeartPop = true
        coroutineScope.launch {
            heartScale.snapTo(0.2f)
            heartAlpha.snapTo(1f)
            heartScale.animateTo(
                targetValue = 1.3f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            delay(150)
            heartAlpha.animateTo(0f, animationSpec = tween(250))
            showHeartPop = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        triggerHeartPop()
                    },
                    onTap = {
                        isPlaying = !isPlaying
                        showPauseOverlay = true
                        coroutineScope.launch {
                            delay(700)
                            showPauseOverlay = false
                        }
                    }
                )
            }
    ) {
        // 1. Reel Media Background (ExoPlayer full-bleed portrait video with auto-play & loop)
        if (!reel.post.mediaUrl.isNullOrBlank()) {
            ReelVideoPlayer(
                mediaUrl = reel.post.mediaUrl,
                isActive = isActive,
                isPlaying = isPlaying,
                isMuted = isMuted,
                onProgressUpdate = { progress = it },
                seekToFraction = seekFraction,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Stylized background if media is text-based
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PrimaryIndigo.copy(alpha = 0.8f),
                                Color(0xFF1E1B4B),
                                Color.Black
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reel.post.caption,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        // Dark Vignette & Gradient Overlays for readable text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Center Pause / Play indicator overlay
        AnimatedVisibility(
            visible = showPauseOverlay,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPlaying) "Playing" else "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Center Double-Tap Popping Heart (Instagram signature feedback)
        if (showHeartPop) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(heartScale.value)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = HeartRed.copy(alpha = heartAlpha.value),
                    modifier = Modifier.size(110.dp)
                )
            }
        }

        // Center Bookmark to Private Favorites Feedback Pill
        AnimatedVisibility(
            visible = showBookmarkFeedback,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .testTag("reel_bookmark_feedback_toast")
        ) {
            Surface(
                color = Color(0xEE0F172A),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.7f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = if (reel.isSavedByCurrentUser) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = SecondaryCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = bookmarkFeedbackMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Private Favorites Collection",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Dedicated Audio Mute Toggle Button on Player
        IconButton(
            onClick = { handleToggleMute() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 56.dp, end = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .testTag("reel_player_mute_toggle")
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute video" else "Mute video",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Center Mute / Unmute HUD Toast Feedback
        AnimatedVisibility(
            visible = showMuteToast,
            enter = fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.85f),
            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.85f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isMuted) "Audio Muted" else "Audio On",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // 2. Right-side Action Rail (Instagram layout)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 36.dp)
        ) {
            // Author Avatar with Follow (+) badge
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                UserAvatar(
                    imageUrl = reel.author.profileImage,
                    name = reel.author.name,
                    size = 46.dp,
                    onClick = onUserClick,
                    modifier = Modifier.border(1.5.dp, Color.White, CircleShape)
                )

                if (reel.author.id != currentUserId) {
                    Box(
                        modifier = Modifier
                            .offset(y = 8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(HeartRed)
                            .clickable(onClick = onFollowToggle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Follow",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Like Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onLikeClick)
                    .testTag("reel_like_button")
            ) {
                val likeScale by animateFloatAsState(
                    targetValue = if (reel.isLikedByCurrentUser) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "like_scale"
                )

                Icon(
                    imageVector = if (reel.isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (reel.isLikedByCurrentUser) HeartRed else Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(likeScale)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCount(reel.likeCount),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Comment Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onCommentClick)
                    .testTag("reel_comment_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCount(reel.commentCount),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Share Action (triggers system intent picker)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = triggerShareIntent)
                    .testTag("reel_share_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share video link",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Share",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Bookmark Action (saves reel to private 'Favorites' collection)
            val bookmarkScale by animateFloatAsState(
                targetValue = if (reel.isSavedByCurrentUser) 1.25f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "bookmark_scale"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = handleBookmarkClick)
                    .testTag("reel_bookmark_button")
            ) {
                Icon(
                    imageVector = if (reel.isSavedByCurrentUser) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (reel.isSavedByCurrentUser) "Remove reel from private Favorites" else "Save reel to private Favorites collection",
                    tint = if (reel.isSavedByCurrentUser) SecondaryCyan else Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .scale(bookmarkScale)
                        .testTag("reel_bookmark_icon")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (reel.isSavedByCurrentUser) "Saved" else "Favorite",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (reel.isSavedByCurrentUser) SecondaryCyan else Color.White,
                    modifier = Modifier.testTag("reel_bookmark_label")
                )
            }

            // More Options (Three dots)
            Box {
                IconButton(
                    onClick = { showMoreMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (reel.isSavedByCurrentUser) "Remove from Favorites" else "Save to Favorites"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (reel.isSavedByCurrentUser) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                tint = if (reel.isSavedByCurrentUser) SecondaryCyan else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            showMoreMenu = false
                            handleBookmarkClick()
                        },
                        modifier = Modifier.testTag("menu_bookmark_reel")
                    )
                    DropdownMenuItem(
                        text = { Text("Share Video Link") },
                        leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                        onClick = {
                            showMoreMenu = false
                            triggerShareIntent()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy Link") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        onClick = {
                            showMoreMenu = false
                            clipboardManager.setText(AnnotatedString(videoLink))
                            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                    if (reel.author.id == currentUserId) {
                        DropdownMenuItem(
                            text = { Text("Delete Reel", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                showMoreMenu = false
                                onDeleteClick()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Report Reel") },
                            leadingIcon = { Icon(Icons.Default.ReportProblem, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                onReportClick()
                            }
                        )
                    }
                }
            }

            // Rotating Audio Vinyl Disc
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .rotate(if (isPlaying) discRotation else 0f)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo)
                )
            }
        }

        // 3. Bottom Left Text Overlay: Displays creator name and brief description with clickable hashtags
        ReelTextOverlay(
            reel = reel,
            currentUserId = currentUserId,
            isMuted = isMuted,
            onUserClick = onUserClick,
            onFollowToggle = onFollowToggle,
            onToggleMute = { handleToggleMute() },
            onHashtagClick = onHashtagClick,
            modifier = Modifier.align(Alignment.BottomStart)
        )

        // 4. Thin Horizontal Progress Bar at the bottom indicating video playback progress
        ReelProgressBar(
            progress = progress,
            onSeek = { fraction ->
                progress = fraction
                seekFraction = fraction
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}
