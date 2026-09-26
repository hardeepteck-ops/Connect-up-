package com.example.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.PostUiModel
import com.example.data.model.UserEntity
import com.example.data.model.UserProfileUiModel
import com.example.ui.components.*
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUserId: Long,
    profileUi: UserProfileUiModel?,
    userPosts: List<PostUiModel>,
    likedPosts: List<PostUiModel>,
    onFollowToggle: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLikePostClick: (Long) -> Unit,
    onSavePostClick: (Long) -> Unit,
    onReportUserClick: (reason: String, desc: String) -> Unit,
    onBlockToggle: () -> Unit,
    onNavigateBack: () -> Unit,
    canNavigateBack: Boolean,
    onReelClick: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Posts, 1: Reels, 2: Media, 3: Liked
    var showReportDialog by remember { mutableStateOf(false) }

    if (profileUi == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryIndigo)
        }
        return
    }

    val user = profileUi.user
    val isCurrentUser = user.id == currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                title = {
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    if (isCurrentUser) {
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.testTag("profile_settings_button")
                        ) {
                            Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings")
                        }
                    } else {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(imageVector = Icons.Outlined.ReportProblem, contentDescription = "Report")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header: Cover Image + Avatar
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Cover photo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryIndigo, SecondaryCyan)
                                )
                            )
                    ) {
                        if (user.coverImage.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(user.coverImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Cover Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Avatar positioned partially over cover
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 80.dp)
                    ) {
                        UserAvatar(
                            imageUrl = user.profileImage,
                            name = user.name,
                            size = 80.dp
                        )
                    }
                }
            }

            // User Info & Bio
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                if (user.isAdmin) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Admin badge",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Profile CTA buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isCurrentUser) {
                                OutlinedButton(
                                    onClick = onEditProfileClick,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("edit_profile_button")
                                ) {
                                    Text("Edit Profile")
                                }
                            } else {
                                Button(
                                    onClick = onFollowToggle,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (profileUi.isFollowing) MaterialTheme.colorScheme.surfaceVariant else PrimaryIndigo,
                                        contentColor = if (profileUi.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                    ),
                                    modifier = Modifier.testTag("follow_user_button")
                                ) {
                                    Text(if (profileUi.isFollowing) "Following" else "Follow")
                                }

                                IconButton(
                                    onClick = onMessageClick,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .testTag("message_user_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Mail,
                                        contentDescription = "Message",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bio
                    if (user.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Website link
                    if (user.website.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(user.website))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.website.removePrefix("https://").removePrefix("http://"),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = PrimaryIndigo
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats: Posts, Followers, Following
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${profileUi.postsCount}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Posts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(onClick = onFollowersClick)
                                .testTag("profile_followers_stat")
                        ) {
                            Text(
                                text = "${profileUi.followersCount}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Followers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(onClick = onFollowingClick)
                                .testTag("profile_following_stat")
                        ) {
                            Text(
                                text = "${profileUi.followingCount}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Following",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Profile Tabs: Posts, Reels, Media, Liked
            val userReels = userPosts.filter { it.post.isReel }
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = PrimaryIndigo
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Posts (${userPosts.count { !it.post.isReel }})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Reels (${userReels.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Media (${userPosts.count { !it.post.mediaUrl.isNullOrBlank() }})") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Liked (${likedPosts.size})") }
                    )
                }
            }

            // Tab Content
            if (selectedTab == 1) {
                // Reels Tab - Instagram-style vertical video grid
                if (userReels.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateView(
                                icon = Icons.Outlined.PlayCircle,
                                title = "No Reels Yet",
                                description = "Short video reels shared by this user will appear here."
                            )
                        }
                    }
                } else {
                    item {
                        // 3-column Reels Grid like Instagram
                        val chunkedReels = userReels.chunked(3)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            chunkedReels.forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEach { reel ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(9f / 16f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Black)
                                                .clickable { onReelClick?.invoke(reel.post.id) }
                                        ) {
                                            if (!reel.post.mediaUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(reel.post.mediaUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }

                                            // Gradient vignette
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                        )
                                                    )
                                            )

                                            // View Count badge at bottom
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                val formattedViews = when {
                                                    reel.post.viewsCount >= 1000 -> String.format("%.1fk", reel.post.viewsCount / 1000.0)
                                                    reel.post.viewsCount > 0 -> reel.post.viewsCount.toString()
                                                    else -> "${reel.likeCount * 3 + 12}"
                                                }
                                                Text(
                                                    text = formattedViews,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    for (i in 0 until (3 - rowItems.size)) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val displayedPosts = when (selectedTab) {
                    0 -> userPosts.filter { !it.post.isReel }
                    2 -> userPosts.filter { !it.post.mediaUrl.isNullOrBlank() }
                    3 -> likedPosts
                    else -> emptyList()
                }

                if (displayedPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateView(
                                icon = Icons.Outlined.DynamicFeed,
                                title = "No posts in this section",
                                description = "Items will show up here once created or liked."
                            )
                        }
                    }
                } else {
                    items(displayedPosts, key = { it.post.id }) { postUi ->
                        PostCard(
                            postUi = postUi,
                            currentUserId = currentUserId,
                            onLikeClick = { onLikePostClick(postUi.post.id) },
                            onCommentClick = {},
                            onSaveClick = { onSavePostClick(postUi.post.id) },
                            onUserClick = {},
                            onDeleteClick = {},
                            onReportClick = {},
                            onBlockUserClick = {}
                        )
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        ReportDialog(
            targetName = "@${user.username}",
            onDismiss = { showReportDialog = false },
            onSubmitReport = { reason, desc ->
                onReportUserClick(reason, desc)
                showReportDialog = false
            }
        )
    }
}

@Composable
fun EditProfileScreen(
    user: UserEntity,
    onNavigateBack: () -> Unit,
    onSaveProfile: (name: String, username: String, bio: String, website: String, avatar: String, cover: String) -> Unit,
    isSaving: Boolean,
    errorMessage: String?
) {
    var name by remember { mutableStateOf(user.name) }
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio) }
    var website by remember { mutableStateOf(user.website) }
    var profileImage by remember { mutableStateOf(user.profileImage) }
    var coverImage by remember { mutableStateOf(user.coverImage) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImage = uri.toString()
        }
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coverImage = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            ConnectUpTopBar(
                title = "Edit Profile",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = {
                            onSaveProfile(name, username, bio, website, profileImage, coverImage)
                        },
                        enabled = !isSaving && name.isNotBlank() && username.isNotBlank(),
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Save", fontWeight = FontWeight.Bold, color = PrimaryIndigo)
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Avatar Change
            Box(contentAlignment = Alignment.BottomEnd) {
                UserAvatar(
                    imageUrl = profileImage,
                    name = name,
                    size = 90.dp
                )
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo)
                        .testTag("change_avatar_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Change photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            TextButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Text("Change Profile Photo", fontSize = 13.sp, color = PrimaryIndigo)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ConnectUpTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                testTag = "edit_name_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConnectUpTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                testTag = "edit_username_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConnectUpTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "Bio",
                singleLine = false,
                maxLines = 4,
                testTag = "edit_bio_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ConnectUpTextField(
                value = website,
                onValueChange = { website = it },
                label = "Website",
                leadingIcon = Icons.Outlined.Link,
                testTag = "edit_website_input"
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersFollowingScreen(
    currentUserId: Long,
    targetUser: UserEntity?,
    followers: List<UserEntity>,
    following: List<UserEntity>,
    initialTab: Int = 0,
    onFollowToggle: (userId: Long) -> Unit,
    onUserClick: (userId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = targetUser?.name ?: "Connections",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = PrimaryIndigo
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Followers (${followers.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Following (${following.size})") }
                )
            }

            val list = if (selectedTab == 0) followers else following

            if (list.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Outlined.People,
                        title = if (selectedTab == 0) "No followers yet" else "Not following anyone yet",
                        description = "When connections are made, they will appear here."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(list, key = { it.id }) { user ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserClick(user.id) }
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    imageUrl = user.profileImage,
                                    name = user.name,
                                    size = 46.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "@${user.username}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (user.id != currentUserId) {
                                    Button(
                                        onClick = { onFollowToggle(user.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text("Follow", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
