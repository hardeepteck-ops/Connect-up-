package com.example.ui.screens.reels

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostUiModel
import com.example.ui.theme.SecondaryCyan

/**
 * Text overlay component for the Reels player positioned at the bottom-left of the screen.
 * Displays the video creator's name, handle, verification status, follow button,
 * a brief description/caption with expandable toggle and clickable inline hashtags,
 * a row of interactive topic chips that trigger search or feed filtering,
 * and the audio/sound attribution ticker.
 */
@Composable
fun ReelTextOverlay(
    reel: PostUiModel,
    currentUserId: Long,
    isMuted: Boolean,
    onUserClick: () -> Unit,
    onFollowToggle: () -> Unit,
    onToggleMute: () -> Unit,
    onHashtagClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val textShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.85f),
            offset = Offset(x = 1.5f, y = 1.5f),
            blurRadius = 4f
        )
    }

    // Extract all unique hashtags from both metadata hashtags and caption
    val allHashtags = remember(reel.post.hashtags, reel.post.caption) {
        val list = mutableListOf<String>()
        if (reel.post.hashtags.isNotBlank()) {
            reel.post.hashtags.split(",", " ").forEach {
                val clean = it.trim().removePrefix("#")
                if (clean.isNotBlank() && !list.contains(clean)) {
                    list.add(clean)
                }
            }
        }
        val regex = Regex("#([a-zA-Z0-9_]+)")
        regex.findAll(reel.post.caption).forEach { matchResult ->
            val clean = matchResult.groupValues[1]
            if (clean.isNotBlank() && !list.contains(clean)) {
                list.add(clean)
            }
        }
        list
    }

    // Build annotated string for caption with clickable, highlighted hashtags
    val annotatedCaption = remember(reel.post.caption) {
        buildAnnotatedString {
            val caption = reel.post.caption
            val regex = Regex("#([a-zA-Z0-9_]+)")
            var lastIndex = 0

            regex.findAll(caption).forEach { matchResult ->
                val start = matchResult.range.first
                val end = matchResult.range.last + 1
                val tag = matchResult.groupValues[1]

                if (start > lastIndex) {
                    append(caption.substring(lastIndex, start))
                }

                pushStringAnnotation(tag = "HASHTAG", annotation = tag)
                withStyle(
                    SpanStyle(
                        color = SecondaryCyan,
                        fontWeight = FontWeight.Bold,
                        shadow = textShadow
                    )
                ) {
                    append(matchResult.value)
                }
                pop()

                lastIndex = end
            }

            if (lastIndex < caption.length) {
                append(caption.substring(lastIndex))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth(0.80f)
            .padding(start = 16.dp, end = 8.dp, bottom = 28.dp)
            .testTag("reel_text_overlay")
            .animateContentSize()
    ) {
        // Creator Info Row: Display Name, Username, Verification Badge, and Follow Action
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onUserClick
                )
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reel.author.name.ifBlank { reel.author.username },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = textShadow
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("reel_creator_name")
                    )

                    if (reel.author.isAdmin) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified Creator",
                            tint = SecondaryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (reel.author.name.isNotBlank() && !reel.author.name.equals(reel.author.username, ignoreCase = true)) {
                    Text(
                        text = "@${reel.author.username}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                            shadow = textShadow
                        ),
                        modifier = Modifier.testTag("reel_creator_handle")
                    )
                }
            }

            if (reel.author.id != currentUserId) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onFollowToggle)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                        .testTag("reel_creator_follow_button")
                ) {
                    Text(
                        text = "Follow",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Brief Description / Caption with Clickable Hashtags
        val description = reel.post.caption
        if (description.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                ClickableText(
                    text = annotatedCaption,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        lineHeight = 20.sp,
                        shadow = textShadow
                    ),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("reel_description_text"),
                    onClick = { offset ->
                        val annotation = annotatedCaption.getStringAnnotations(
                            tag = "HASHTAG",
                            start = offset,
                            end = offset
                        ).firstOrNull()

                        if (annotation != null) {
                            onHashtagClick(annotation.item)
                        } else {
                            isExpanded = !isExpanded
                        }
                    }
                )

                if (description.length > 70) {
                    Text(
                        text = if (isExpanded) "show less" else "...more",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Bold,
                            shadow = textShadow
                        ),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .testTag("reel_description_expand_button")
                    )
                }
            }
        }

        // Interactive Clickable Hashtag Chips Row (Triggers Topic Search / Feed Filter)
        if (allHashtags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("reel_hashtags_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                allHashtags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onHashtagClick(tag) }
                            .testTag("reel_hashtag_$tag")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = SecondaryCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryCyan,
                                    letterSpacing = 0.3.sp,
                                    shadow = textShadow
                                )
                            )
                        }
                    }
                }
            }
        }

        // Audio / Sound Ticker Pill (tap to toggle mute)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onToggleMute)
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("reel_audio_ticker_mute_toggle")
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.MusicNote,
                contentDescription = if (isMuted) "Unmute" else "Audio",
                tint = if (isMuted) Color.White.copy(alpha = 0.7f) else Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${reel.post.soundTitle} • ${if (isMuted) "Audio Muted" else "Original Audio"}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
