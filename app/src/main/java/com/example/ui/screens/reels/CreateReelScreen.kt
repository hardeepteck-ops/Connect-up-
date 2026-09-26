package com.example.ui.screens.reels

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserEntity
import com.example.ui.components.ConnectUpTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan

@Composable
fun CreateReelScreen(
    currentUser: UserEntity?,
    onNavigateBack: () -> Unit,
    onSubmitReel: (caption: String, mediaUrl: String, soundTitle: String, hashtags: String) -> Unit,
    isSubmitting: Boolean
) {
    var caption by remember { mutableStateOf("") }
    var selectedMediaUri by remember {
        mutableStateOf<String?>("https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4")
    }
    var selectedSound by remember { mutableStateOf("Trending: Golden Hour Chill") }
    var selectedHashtags by remember { mutableStateOf("reels,viral,connectup") }

    val presetSounds = listOf(
        "Trending: Golden Hour Chill",
        "Lofi Study Beats",
        "Synthwave Pulse in D Minor",
        "Cybernetic Flow",
        "ConnectUp Original Sound"
    )

    val sampleClips = listOf(
        Pair("Tech UI Demo", "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4"),
        Pair("Music Wave", "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4"),
        Pair("Bunny Animation", "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4"),
        Pair("City Streets", "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/person-bicycle-car-detection.mp4"),
        Pair("Walking & Motion", "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/face-demographics-walking-and-pause.mp4")
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
        }
    }

    val sampleTags = listOf("reels", "viral", "creative", "tech", "design", "explore", "music", "lifestyle")

    Scaffold(
        topBar = {
            ConnectUpTopBar(
                title = "Create Reel",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    Button(
                        onClick = {
                            val url = selectedMediaUri ?: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
                            onSubmitReel(caption, url, selectedSound, selectedHashtags)
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("submit_reel_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Share Reel", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isSubmitting) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Portrait 9:16 Reel Preview Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Reel Card Preview
                Box(
                    modifier = Modifier
                        .width(135.dp)
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.5.dp, PrimaryIndigo.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    if (selectedMediaUri != null) {
                        ReelVideoPlayer(
                            mediaUrl = selectedMediaUri,
                            isActive = true,
                            isPlaying = true,
                            isMuted = true,
                            onProgressUpdate = {},
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Simulated live Reel overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )

                    // Reel indicator badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Reel", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Caption preview at bottom of card
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (caption.isNotBlank()) caption else "Your caption here...",
                            color = Color.White,
                            fontSize = 10.sp,
                            maxLines = 2
                        )
                    }
                }

                // Video selection actions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(240.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Reel Video",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Pick a video from your gallery or choose a preset clip",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Video")
                        }
                    }

                    // Preset Clips Selector
                    Column {
                        Text(
                            text = "Preset Video Clips:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            sampleClips.forEach { (title, url) ->
                                val isSelected = selectedMediaUri == url
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedMediaUri = url }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Sound Selector
            Text(
                text = "Audio Track",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                presetSounds.forEach { sound ->
                    val isSelected = selectedSound == sound
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) PrimaryIndigo.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedSound = sound }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sound,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reel Caption Field
            Text(
                text = "Caption & Hashtags",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = caption,
                onValueChange = { if (it.length <= 400) caption = it },
                placeholder = { Text("Write a catchy caption for your Reel...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .testTag("create_reel_caption_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Suggested Hashtags
            Text(
                text = "Tap to add trending hashtags:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                sampleTags.forEach { tag ->
                    val isIncluded = selectedHashtags.contains(tag)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isIncluded) PrimaryIndigo.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                selectedHashtags = if (isIncluded) {
                                    selectedHashtags.split(",")
                                        .filterNot { it.trim() == tag }
                                        .joinToString(",")
                                } else {
                                    if (selectedHashtags.isBlank()) tag else "$selectedHashtags,$tag"
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isIncluded) FontWeight.Bold else FontWeight.Normal,
                                color = if (isIncluded) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
