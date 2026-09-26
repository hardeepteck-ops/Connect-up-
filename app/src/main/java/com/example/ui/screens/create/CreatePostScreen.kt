package com.example.ui.screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.example.ui.components.ConnectUpButton
import com.example.ui.components.ConnectUpTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.PrimaryIndigo

@Composable
fun CreatePostScreen(
    currentUser: UserEntity?,
    onNavigateBack: () -> Unit,
    onSubmitPost: (caption: String, mediaUrl: String?, mediaType: String?, hashtags: String) -> Unit,
    onNavigateToCreateReel: (() -> Unit)? = null,
    isSubmitting: Boolean
) {
    var caption by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var selectedMediaType by remember { mutableStateOf<String?>("IMAGE") }
    var selectedHashtags by remember { mutableStateOf("") }
    var showUrlInputDialog by remember { mutableStateOf(false) }

    val maxChars = 500
    val charsRemaining = maxChars - caption.length

    // Android Photo Picker - compliant with Google Play policy
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri.toString()
            selectedMediaType = "IMAGE"
        }
    }

    val sampleTags = listOf("connectup", "tech", "design", "photography", "lifestyle", "nature", "music", "ai")

    Scaffold(
        topBar = {
            ConnectUpTopBar(
                title = "Create Post",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    Button(
                        onClick = {
                            if (caption.isNotBlank() || selectedMediaUri != null) {
                                onSubmitPost(caption, selectedMediaUri, selectedMediaType, selectedHashtags)
                            }
                        },
                        enabled = !isSubmitting && (caption.isNotBlank() || selectedMediaUri != null),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("submit_post_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Post", fontWeight = FontWeight.Bold)
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

            // User Info Header
            if (currentUser != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        imageUrl = currentUser.profileImage,
                        name = currentUser.name,
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Posting to Public Feed",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryIndigo
                        )
                    }
                }
            }

            // Post vs Reel format selector
            if (onNavigateToCreateReel != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryIndigo)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Post", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onNavigateToCreateReel)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reel 🎬", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Caption Text Input
            OutlinedTextField(
                value = caption,
                onValueChange = { if (it.length <= maxChars) caption = it },
                placeholder = {
                    Text(
                        "What's on your mind? Share a thought, idea, or update...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp)
                    .testTag("create_post_caption_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Character Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "$charsRemaining characters left",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (charsRemaining < 50) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Media Preview (if selected)
            AnimatedVisibility(visible = selectedMediaUri != null) {
                selectedMediaUri?.let { mediaUri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(mediaUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected media preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Remove button
                        IconButton(
                            onClick = {
                                selectedMediaUri = null
                                selectedMediaType = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .testTag("remove_media_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove media",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hashtag quick selector
            Text(
                text = "Trending Hashtags",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sampleTags.forEach { tag ->
                    val isIncluded = selectedHashtags.contains(tag)
                    FilterChip(
                        selected = isIncluded,
                        onClick = {
                            val currentTags = selectedHashtags.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
                            if (isIncluded) {
                                currentTags.remove(tag)
                            } else {
                                currentTags.add(tag)
                            }
                            selectedHashtags = currentTags.joinToString(",")
                        },
                        label = { Text("#$tag") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Media attachment options card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Add to your post",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Gallery photo
                        AssistChip(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            label = { Text("Photo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = null,
                                    tint = PrimaryIndigo
                                )
                            },
                            modifier = Modifier.testTag("pick_photo_button")
                        )

                        // Video upload
                        AssistChip(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            label = { Text("Video") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Videocam,
                                    contentDescription = null,
                                    tint = PrimaryIndigo
                                )
                            },
                            modifier = Modifier.testTag("pick_video_button")
                        )

                        // Web Image / Preset URL
                        AssistChip(
                            onClick = { showUrlInputDialog = true },
                            label = { Text("Web URL") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Link,
                                    contentDescription = null,
                                    tint = PrimaryIndigo
                                )
                            },
                            modifier = Modifier.testTag("enter_url_button")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showUrlInputDialog) {
        var inputUrl by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showUrlInputDialog = false },
            title = { Text("Attach Media URL") },
            text = {
                Column {
                    Text("Enter image or video URL:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            selectedMediaUri = inputUrl
                            selectedMediaType = if (inputUrl.endsWith(".mp4") || inputUrl.contains("video")) "VIDEO" else "IMAGE"
                            showUrlInputDialog = false
                        }
                    }
                ) {
                    Text("Attach")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
