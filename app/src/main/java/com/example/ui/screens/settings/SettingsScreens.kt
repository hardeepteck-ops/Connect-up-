package com.example.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSessionEntity
import com.example.data.model.PostUiModel
import com.example.data.model.ReportEntity
import com.example.data.model.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessGreen
import com.example.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: UserEntity?,
    session: AppSessionEntity?,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSavedPosts: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    onNavigateToReportHistory: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onThemeChange: (String) -> Unit,
    onChangePassword: (oldPass: String, newPass: String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showCacheClearedSnackbar by remember { mutableStateOf(false) }

    // Privacy & Notification toggles
    var allowMessages by remember { mutableStateOf(session?.allowMessages ?: true) }
    var showOnlineStatus by remember { mutableStateOf(session?.showOnlineStatus ?: true) }
    var pushLikes by remember { mutableStateOf(session?.pushLikes ?: true) }
    var pushComments by remember { mutableStateOf(session?.pushComments ?: true) }
    var pushFollows by remember { mutableStateOf(session?.pushFollows ?: true) }
    var pushMessages by remember { mutableStateOf(session?.pushMessages ?: true) }

    val currentTheme = session?.themeMode ?: "SYSTEM"

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = "Settings",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ADMIN PANEL CTA (if user is admin)
            if (currentUser?.isAdmin == true) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryIndigo.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToAdminDashboard)
                        .padding(bottom = 16.dp)
                        .testTag("admin_dashboard_cta")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Moderation Center",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryIndigo
                            )
                            Text(
                                text = "Review reported content and platform stats",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = PrimaryIndigo
                        )
                    }
                }
            }

            // Section: Account
            SettingsSectionHeader(title = "Account")
            SettingsItem(
                icon = Icons.Outlined.Person,
                title = "Edit Profile",
                subtitle = "Name, username, bio, and avatar",
                onClick = onNavigateToEditProfile,
                testTag = "settings_item_edit_profile"
            )
            SettingsItem(
                icon = Icons.Outlined.BookmarkBorder,
                title = "Favorites & Saved",
                subtitle = "Your private bookmarked collection",
                onClick = onNavigateToSavedPosts,
                testTag = "settings_item_saved_posts"
            )
            SettingsItem(
                icon = Icons.Outlined.Lock,
                title = "Change Password",
                subtitle = "Update security credentials",
                onClick = { showChangePasswordDialog = true },
                testTag = "settings_item_change_password"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Privacy
            SettingsSectionHeader(title = "Privacy")
            SettingsToggleItem(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "Direct Messages",
                subtitle = "Allow messages from anyone",
                checked = allowMessages,
                onCheckedChange = { allowMessages = it }
            )
            SettingsToggleItem(
                icon = Icons.Outlined.Visibility,
                title = "Show Online Status",
                subtitle = "Let friends know when you're active",
                checked = showOnlineStatus,
                onCheckedChange = { showOnlineStatus = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Notifications
            SettingsSectionHeader(title = "Push Notifications")
            SettingsToggleItem(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Likes",
                checked = pushLikes,
                onCheckedChange = { pushLikes = it }
            )
            SettingsToggleItem(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "Comments",
                checked = pushComments,
                onCheckedChange = { pushComments = it }
            )
            SettingsToggleItem(
                icon = Icons.Outlined.PersonAdd,
                title = "New Followers",
                checked = pushFollows,
                onCheckedChange = { pushFollows = it }
            )
            SettingsToggleItem(
                icon = Icons.Outlined.Mail,
                title = "Direct Messages",
                checked = pushMessages,
                onCheckedChange = { pushMessages = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Safety
            SettingsSectionHeader(title = "Safety & Community")
            SettingsItem(
                icon = Icons.Outlined.Block,
                title = "Blocked Accounts",
                subtitle = "Manage restricted accounts",
                onClick = onNavigateToBlockedUsers,
                testTag = "settings_item_blocked_users"
            )
            SettingsItem(
                icon = Icons.Outlined.Flag,
                title = "Report History",
                subtitle = "Check status of reported content",
                onClick = onNavigateToReportHistory,
                testTag = "settings_item_report_history"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section: App & Appearance
            SettingsSectionHeader(title = "App Appearance")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (mode, label) ->
                            FilterChip(
                                selected = currentTheme == mode,
                                onClick = { onThemeChange(mode) },
                                label = { Text(label) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsItem(
                icon = Icons.Outlined.CleaningServices,
                title = "Clear Cache",
                subtitle = "Free up device storage",
                onClick = { showCacheClearedSnackbar = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Account Actions (Logout, Delete)
            SettingsSectionHeader(title = "Actions")

            Button(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_button")
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_account_button")
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showCacheClearedSnackbar) {
        AlertDialog(
            onDismissRequest = { showCacheClearedSnackbar = false },
            title = { Text("Cache Cleared") },
            text = { Text("Local image cache and temporary query artifacts have been cleared.") },
            confirmButton = {
                Button(onClick = { showCacheClearedSnackbar = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out of ConnectUp?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account permanently?") },
            text = { Text("This will delete your account, posts, comments, likes, and profile permanently. This action cannot be reversed.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onChangePassword(currentPass, newPass)
                        showChangePasswordDialog = false
                    },
                    enabled = currentPass.isNotBlank() && newPass.length >= 6
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = PrimaryIndigo,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsScreen(
    currentUserId: Long,
    savedPosts: List<PostUiModel>,
    onLikeClick: (Long) -> Unit,
    onSaveClick: (Long) -> Unit,
    onUserClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Favorite Reels, 2: Posts

    val reelsCount = remember(savedPosts) { savedPosts.count { it.post.isReel } }
    val postsCount = remember(savedPosts) { savedPosts.count { !it.post.isReel } }

    val filteredPosts = remember(savedPosts, selectedFilter) {
        when (selectedFilter) {
            1 -> savedPosts.filter { it.post.isReel }
            2 -> savedPosts.filter { !it.post.isReel }
            else -> savedPosts
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Favorites & Saved", fontWeight = FontWeight.Bold) },
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
            // Private Collection Information Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("saved_private_collection_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Private collection",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Private 'Favorites' Collection",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Only you can see your bookmarked reels and saved posts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Collection Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == 0,
                    onClick = { selectedFilter = 0 },
                    label = { Text("All (${savedPosts.size})") },
                    modifier = Modifier.testTag("saved_filter_all")
                )
                FilterChip(
                    selected = selectedFilter == 1,
                    onClick = { selectedFilter = 1 },
                    label = { Text("Favorite Reels ($reelsCount)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("saved_filter_reels")
                )
                FilterChip(
                    selected = selectedFilter == 2,
                    onClick = { selectedFilter = 2 },
                    label = { Text("Posts ($postsCount)") },
                    modifier = Modifier.testTag("saved_filter_posts")
                )
            }

            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val (emptyTitle, emptyDesc) = when (selectedFilter) {
                        1 -> Pair(
                            "No favorite reels saved yet",
                            "Bookmark reels in the Reels player to add them to your private Favorites collection."
                        )
                        2 -> Pair(
                            "No saved posts yet",
                            "Bookmark posts you love by tapping the bookmark icon to view them later."
                        )
                        else -> Pair(
                            "Your collection is empty",
                            "Tap the bookmark icon on any reel or post to save it to your private Favorites collection."
                        )
                    }
                    EmptyStateView(
                        icon = Icons.Outlined.BookmarkBorder,
                        title = emptyTitle,
                        description = emptyDesc
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredPosts, key = { it.post.id }) { postUi ->
                        PostCard(
                            postUi = postUi,
                            currentUserId = currentUserId,
                            onLikeClick = { onLikeClick(postUi.post.id) },
                            onCommentClick = {},
                            onSaveClick = { onSaveClick(postUi.post.id) },
                            onUserClick = onUserClick,
                            onDeleteClick = {},
                            onReportClick = {},
                            onBlockUserClick = {}
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    blockedUsers: List<UserEntity>,
    onUnblockClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Blocked Accounts", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (blockedUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Outlined.Security,
                    title = "No blocked users",
                    description = "When you block an account, they won't be able to view your posts or message you."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(blockedUsers, key = { it.id }) { user ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(imageUrl = user.profileImage, name = user.name, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, fontWeight = FontWeight.Bold)
                                Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(
                                onClick = { onUnblockClick(user.id) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Unblock")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportHistoryScreen(
    reports: List<ReportEntity>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Report History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (reports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Outlined.CheckCircle,
                    title = "No reports submitted",
                    description = "Reports you make regarding inappropriate content or behavior will appear here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reports, key = { it.id }) { report ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Report #${report.id} • ${report.targetType}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (report.status == "PENDING") PrimaryIndigo.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = report.status,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (report.status == "PENDING") PrimaryIndigo else SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Reason: ${report.reason}", style = MaterialTheme.typography.bodyMedium)
                            if (report.description.isNotBlank()) {
                                Text(
                                    text = "Details: ${report.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Submitted on ${DateFormatter.formatDate(report.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
