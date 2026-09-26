package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserEntity
import com.example.ui.components.ConnectUpBottomBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.screens.create.CreatePostScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.feed.CommentsBottomSheet
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.messages.ChatDetailScreen
import com.example.ui.screens.messages.MessagesListScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.profile.EditProfileScreen
import com.example.ui.screens.profile.FollowersFollowingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reels.CreateReelScreen
import com.example.ui.screens.reels.ReelsScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.BlockedUsersScreen
import com.example.ui.screens.settings.ReportHistoryScreen
import com.example.ui.screens.settings.SavedPostsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.ConnectUpTheme
import com.example.ui.viewmodel.ConnectUpViewModel
import kotlinx.coroutines.launch

@Composable
fun ConnectUpApp(viewModel: ConnectUpViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val appSession by viewModel.appSession.collectAsStateWithLifecycle()

    // Determine theme mode (SYSTEM, LIGHT, DARK)
    val themeMode = appSession?.themeMode ?: "SYSTEM"
    val isDarkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Navigation Stack
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var authScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    val screenBackStack = remember { mutableStateListOf<Screen>() }

    fun navigateTo(screen: Screen) {
        screenBackStack.add(currentScreen)
        currentScreen = screen
    }

    fun navigateBack() {
        if (screenBackStack.isNotEmpty()) {
            currentScreen = screenBackStack.removeAt(screenBackStack.lastIndex)
        } else {
            currentScreen = Screen.Home
        }
    }

    ConnectUpTheme(darkTheme = isDarkTheme) {
        // If not logged in -> Auth flow
        if (currentUser == null) {
            AnimatedContent(
                targetState = authScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "auth_screen_transition"
            ) { targetAuth ->
                when (targetAuth) {
                    Screen.Welcome -> {
                        WelcomeScreen(
                            onCreateAccountClick = { authScreen = Screen.Register },
                            onLoginClick = { authScreen = Screen.Login }
                        )
                    }
                    Screen.Login -> {
                        BackHandler { authScreen = Screen.Welcome }
                        LoginScreen(
                            onNavigateBack = { authScreen = Screen.Welcome },
                            onLoginSubmit = { id, pass, rememberMe ->
                                viewModel.login(id, pass) {
                                    currentScreen = Screen.Home
                                }
                            },
                            onNavigateToRegister = { authScreen = Screen.Register },
                            isLoading = viewModel.isAuthLoading,
                            errorMessage = viewModel.authError
                        )
                    }
                    Screen.Register -> {
                        BackHandler { authScreen = Screen.Welcome }
                        RegisterScreen(
                            onNavigateBack = { authScreen = Screen.Welcome },
                            onRegisterSubmit = { name, username, email, pass, dob ->
                                viewModel.register(name, username, email, pass, dob) {
                                    // Successfully registered, will show onboarding
                                }
                            },
                            onNavigateToLogin = { authScreen = Screen.Login },
                            isLoading = viewModel.isAuthLoading,
                            errorMessage = viewModel.authError
                        )
                    }
                    else -> WelcomeScreen(
                        onCreateAccountClick = { authScreen = Screen.Register },
                        onLoginClick = { authScreen = Screen.Login }
                    )
                }
            }
            return@ConnectUpTheme
        }

        // First-Run Experience after registration
        if (viewModel.isFirstRunOnboarding) {
            val user = currentUser!!
            val suggested by viewModel.suggestedUsers.collectAsStateWithLifecycle()
            OnboardingScreen(
                user = user,
                suggestedUsers = suggested,
                onFollowUser = { viewModel.toggleFollowUser(it) },
                onComplete = { bio, website, avatar ->
                    viewModel.completeOnboarding(bio, website, avatar) {
                        currentScreen = Screen.Home
                    }
                }
            )
            return@ConnectUpTheme
        }

        // Main In-App Flow with Bottom Navigation
        val feedPosts by viewModel.feedPosts.collectAsStateWithLifecycle()
        val isFeedLoading by viewModel.isFeedLoading.collectAsStateWithLifecycle()
        val isFollowingOnly by viewModel.isFollowingOnly.collectAsStateWithLifecycle()
        val reelsList by viewModel.reels.collectAsStateWithLifecycle()
        val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
        val unreadMessagesTotalCount by viewModel.unreadMessagesTotalCount.collectAsStateWithLifecycle()
        val activeComments by viewModel.activePostComments.collectAsStateWithLifecycle()

        val isBottomBarVisible = currentScreen is Screen.Home ||
                currentScreen is Screen.Explore ||
                currentScreen is Screen.Reels ||
                currentScreen is Screen.Notifications ||
                currentScreen is Screen.Profile

        Scaffold(
            bottomBar = {
                if (isBottomBarVisible) {
                    val activeRoute = when (currentScreen) {
                        Screen.Home -> "home"
                        Screen.Explore -> "explore"
                        Screen.Reels -> "reels"
                        Screen.CreatePost -> "create_post"
                        Screen.Notifications -> "notifications"
                        Screen.Profile -> "profile"
                        else -> "home"
                    }

                    ConnectUpBottomBar(
                        currentRoute = activeRoute,
                        unreadNotificationCount = unreadNotificationsCount,
                        unreadMessagesCount = unreadMessagesTotalCount,
                        onNavigateTo = { route ->
                            screenBackStack.clear()
                            when (route) {
                                "home" -> currentScreen = Screen.Home
                                "explore" -> currentScreen = Screen.Explore
                                "reels" -> currentScreen = Screen.Reels
                                "create_post" -> currentScreen = Screen.CreatePost
                                "notifications" -> currentScreen = Screen.Notifications
                                "profile" -> currentScreen = Screen.Profile
                            }
                        }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val screen = currentScreen) {
                    Screen.Home -> {
                        FeedScreen(
                            currentUserId = currentUser!!.id,
                            posts = feedPosts,
                            isLoading = isFeedLoading,
                            isFollowingOnly = isFollowingOnly,
                            unreadMessagesCount = unreadMessagesTotalCount,
                            onTabSelected = { viewModel.setFollowingOnlyTab(it) },
                            onLikeClick = { viewModel.toggleLikePost(it) },
                            onSaveClick = { viewModel.toggleSavePost(it) },
                            onDeletePostClick = { viewModel.deletePost(it) },
                            onReportPostClick = { postId, reason, desc ->
                                viewModel.submitReport("POST", postId, reason, desc)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Thank you. Report submitted for moderation.")
                                }
                            },
                            onBlockUserClick = { userId ->
                                viewModel.blockUser(userId)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("User blocked.")
                                }
                            },
                            onUserClick = { userId ->
                                if (userId == currentUser!!.id) {
                                    currentScreen = Screen.Profile
                                } else {
                                    navigateTo(Screen.UserProfile(userId))
                                }
                            },
                            onSearchClick = { navigateTo(Screen.Search) },
                            onMessagesClick = { navigateTo(Screen.Messages) },
                            onCreatePostClick = { navigateTo(Screen.CreatePost) },
                            activePostComments = activeComments,
                            onLoadCommentsForPost = { viewModel.loadCommentsForPost(it) },
                            onAddComment = { postId, text -> viewModel.addComment(postId, text) },
                            onDeleteComment = { viewModel.deleteComment(it) },
                            onToggleCommentLike = { viewModel.toggleCommentLike(it) }
                        )
                    }

                    Screen.Explore -> {
                        val mediaPosts by viewModel.exploreMediaPosts.collectAsStateWithLifecycle()
                        val suggested by viewModel.suggestedUsers.collectAsStateWithLifecycle()
                        ExploreScreen(
                            mediaPosts = mediaPosts,
                            suggestedUsers = suggested,
                            onSearchClick = { navigateTo(Screen.Search) },
                            onHashtagClick = { tag ->
                                viewModel.searchQuery = "#$tag"
                                viewModel.performSearch(tag)
                                navigateTo(Screen.Search)
                            },
                            onUserClick = { userId ->
                                if (userId == currentUser!!.id) {
                                    currentScreen = Screen.Profile
                                } else {
                                    navigateTo(Screen.UserProfile(userId))
                                }
                            },
                            onFollowUser = { viewModel.toggleFollowUser(it) },
                            onPostClick = { /* Could open post detail */ }
                        )
                    }

                    Screen.Reels -> {
                        ReelsScreen(
                            currentUserId = currentUser!!.id,
                            reels = reelsList,
                            onLikeClick = { viewModel.toggleLikePost(it) },
                            onSaveClick = { viewModel.toggleSavePost(it) },
                            onDeleteReelClick = { viewModel.deletePost(it) },
                            onReportReelClick = { postId, reason, desc ->
                                viewModel.submitReport("POST", postId, reason, desc)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Report submitted for moderation.")
                                }
                            },
                            onUserClick = { userId ->
                                if (userId == currentUser!!.id) {
                                    currentScreen = Screen.Profile
                                } else {
                                    navigateTo(Screen.UserProfile(userId))
                                }
                            },
                            onFollowToggle = { viewModel.toggleFollowUser(it) },
                            onCreateReelClick = { navigateTo(Screen.CreateReel) },
                            onNavigateBack = if (screenBackStack.isNotEmpty()) { { navigateBack() } } else null,
                            onLoadComments = { viewModel.loadCommentsForPost(it) },
                            commentsContent = { postId, onDismiss ->
                                CommentsBottomSheet(
                                    postId = postId,
                                    currentUserId = currentUser!!.id,
                                    comments = activeComments,
                                    onDismiss = onDismiss,
                                    onAddComment = { text -> viewModel.addComment(postId, text) },
                                    onDeleteComment = { commentId -> viewModel.deleteComment(commentId) },
                                    onToggleCommentLike = { commentId -> viewModel.toggleCommentLike(commentId) },
                                    onUserClick = { userId ->
                                        onDismiss()
                                        if (userId == currentUser!!.id) {
                                            currentScreen = Screen.Profile
                                        } else {
                                            navigateTo(Screen.UserProfile(userId))
                                        }
                                    }
                                )
                            },
                            onReelViewed = { viewModel.incrementReelView(it) },
                            onHashtagClick = { tag ->
                                val clean = tag.removePrefix("#")
                                viewModel.searchQuery = "#$clean"
                                viewModel.performSearch(clean)
                                navigateTo(Screen.Search)
                            }
                        )
                    }

                    is Screen.ReelDetail -> {
                        BackHandler { navigateBack() }
                        ReelsScreen(
                            currentUserId = currentUser!!.id,
                            reels = reelsList,
                            initialPostId = screen.startPostId,
                            onLikeClick = { viewModel.toggleLikePost(it) },
                            onSaveClick = { viewModel.toggleSavePost(it) },
                            onDeleteReelClick = { viewModel.deletePost(it) },
                            onReportReelClick = { postId, reason, desc ->
                                viewModel.submitReport("POST", postId, reason, desc)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Report submitted for moderation.")
                                }
                            },
                            onUserClick = { userId ->
                                if (userId == currentUser!!.id) {
                                    currentScreen = Screen.Profile
                                } else {
                                    navigateTo(Screen.UserProfile(userId))
                                }
                            },
                            onFollowToggle = { viewModel.toggleFollowUser(it) },
                            onCreateReelClick = { navigateTo(Screen.CreateReel) },
                            onNavigateBack = { navigateBack() },
                            onLoadComments = { viewModel.loadCommentsForPost(it) },
                            commentsContent = { postId, onDismiss ->
                                CommentsBottomSheet(
                                    postId = postId,
                                    currentUserId = currentUser!!.id,
                                    comments = activeComments,
                                    onDismiss = onDismiss,
                                    onAddComment = { text -> viewModel.addComment(postId, text) },
                                    onDeleteComment = { commentId -> viewModel.deleteComment(commentId) },
                                    onToggleCommentLike = { commentId -> viewModel.toggleCommentLike(commentId) },
                                    onUserClick = { userId ->
                                        onDismiss()
                                        if (userId == currentUser!!.id) {
                                            currentScreen = Screen.Profile
                                        } else {
                                            navigateTo(Screen.UserProfile(userId))
                                        }
                                    }
                                )
                            },
                            onReelViewed = { viewModel.incrementReelView(it) },
                            onHashtagClick = { tag ->
                                val clean = tag.removePrefix("#")
                                viewModel.searchQuery = "#$clean"
                                viewModel.performSearch(clean)
                                navigateTo(Screen.Search)
                            }
                        )
                    }

                    Screen.CreateReel -> {
                        BackHandler { navigateBack() }
                        var isSharingReel by remember { mutableStateOf(false) }
                        CreateReelScreen(
                            currentUser = currentUser,
                            onNavigateBack = { navigateBack() },
                            onSubmitReel = { caption, mediaUrl, soundTitle, hashtags ->
                                isSharingReel = true
                                viewModel.createPost(
                                    caption = caption,
                                    mediaUrl = mediaUrl,
                                    mediaType = "REEL",
                                    hashtags = hashtags,
                                    isReel = true,
                                    soundTitle = soundTitle
                                ) {
                                    isSharingReel = false
                                    currentScreen = Screen.Reels
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Reel posted!")
                                    }
                                }
                            },
                            isSubmitting = isSharingReel
                        )
                    }

                    Screen.CreatePost -> {
                        BackHandler { navigateBack() }
                        var isPosting by remember { mutableStateOf(false) }
                        CreatePostScreen(
                            currentUser = currentUser,
                            onNavigateBack = { navigateBack() },
                            onNavigateToCreateReel = { navigateTo(Screen.CreateReel) },
                            onSubmitPost = { caption, mediaUrl, mediaType, hashtags ->
                                isPosting = true
                                viewModel.createPost(caption, mediaUrl, mediaType, hashtags) {
                                    isPosting = false
                                    currentScreen = Screen.Home
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Post published!")
                                    }
                                }
                            },
                            isSubmitting = isPosting
                        )
                    }

                    Screen.Notifications -> {
                        val notifs by viewModel.notifications.collectAsStateWithLifecycle()
                        NotificationsScreen(
                            notifications = notifs,
                            onMarkAllRead = { viewModel.markAllNotificationsRead() },
                            onNotificationClick = { notifUi ->
                                viewModel.markAllNotificationsRead()
                                notifUi.actor?.let { actor ->
                                    if (actor.id == currentUser!!.id) {
                                        currentScreen = Screen.Profile
                                    } else {
                                        navigateTo(Screen.UserProfile(actor.id))
                                    }
                                }
                            }
                        )
                    }

                    Screen.Profile -> {
                        val profileUi by viewModel.getUserProfileFlow(currentUser!!.id).collectAsStateWithLifecycle(null)
                        val myPosts by viewModel.getUserPostsFlow(currentUser!!.id).collectAsStateWithLifecycle(emptyList())
                        val likedPosts by viewModel.getLikedPostsFlow(currentUser!!.id).collectAsStateWithLifecycle(emptyList())

                        ProfileScreen(
                            currentUserId = currentUser!!.id,
                            profileUi = profileUi,
                            userPosts = myPosts,
                            likedPosts = likedPosts,
                            onFollowToggle = {},
                            onEditProfileClick = { navigateTo(Screen.EditProfile) },
                            onMessageClick = {},
                            onFollowersClick = { navigateTo(Screen.FollowList(currentUser!!.id, 0)) },
                            onFollowingClick = { navigateTo(Screen.FollowList(currentUser!!.id, 1)) },
                            onSettingsClick = { navigateTo(Screen.Settings) },
                            onLikePostClick = { viewModel.toggleLikePost(it) },
                            onSavePostClick = { viewModel.toggleSavePost(it) },
                            onReportUserClick = { _, _ -> },
                            onBlockToggle = {},
                            onNavigateBack = {},
                            canNavigateBack = false,
                            onReelClick = { reelId -> navigateTo(Screen.ReelDetail(reelId)) }
                        )
                    }

                    is Screen.UserProfile -> {
                        BackHandler { navigateBack() }
                        val targetProfileUi by viewModel.getUserProfileFlow(screen.userId).collectAsStateWithLifecycle(null)
                        val userPosts by viewModel.getUserPostsFlow(screen.userId).collectAsStateWithLifecycle(emptyList())
                        val likedPosts by viewModel.getLikedPostsFlow(screen.userId).collectAsStateWithLifecycle(emptyList())

                        ProfileScreen(
                            currentUserId = currentUser!!.id,
                            profileUi = targetProfileUi,
                            userPosts = userPosts,
                            likedPosts = likedPosts,
                            onFollowToggle = { viewModel.toggleFollowUser(screen.userId) },
                            onEditProfileClick = {},
                            onMessageClick = {
                                viewModel.openChat(screen.userId)
                                navigateTo(Screen.Chat(screen.userId))
                            },
                            onFollowersClick = { navigateTo(Screen.FollowList(screen.userId, 0)) },
                            onFollowingClick = { navigateTo(Screen.FollowList(screen.userId, 1)) },
                            onSettingsClick = {},
                            onLikePostClick = { viewModel.toggleLikePost(it) },
                            onSavePostClick = { viewModel.toggleSavePost(it) },
                            onReportUserClick = { reason, desc ->
                                viewModel.submitReport("USER", screen.userId, reason, desc)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("User reported.")
                                }
                            },
                            onBlockToggle = {
                                viewModel.blockUser(screen.userId)
                                navigateBack()
                            },
                            onNavigateBack = { navigateBack() },
                            canNavigateBack = true,
                            onReelClick = { reelId -> navigateTo(Screen.ReelDetail(reelId)) }
                        )
                    }

                    Screen.EditProfile -> {
                        BackHandler { navigateBack() }
                        var isSaving by remember { mutableStateOf(false) }
                        var errorMsg by remember { mutableStateOf<String?>(null) }

                        EditProfileScreen(
                            user = currentUser!!,
                            onNavigateBack = { navigateBack() },
                            onSaveProfile = { name, username, bio, website, avatar, cover ->
                                isSaving = true
                                errorMsg = null
                                viewModel.updateProfile(name, username, bio, website, avatar, cover) { success, err ->
                                    isSaving = false
                                    if (success) {
                                        navigateBack()
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Profile updated!") }
                                    } else {
                                        errorMsg = err
                                    }
                                }
                            },
                            isSaving = isSaving,
                            errorMessage = errorMsg
                        )
                    }

                    is Screen.FollowList -> {
                        BackHandler { navigateBack() }
                        var targetUser by remember { mutableStateOf<UserEntity?>(null) }
                        LaunchedEffect(screen.userId) {
                            targetUser = viewModel.getUserById(screen.userId)
                        }
                        val followers by viewModel.getFollowersFlow(screen.userId).collectAsStateWithLifecycle(emptyList())
                        val following by viewModel.getFollowingFlow(screen.userId).collectAsStateWithLifecycle(emptyList())

                        FollowersFollowingScreen(
                            currentUserId = currentUser!!.id,
                            targetUser = targetUser,
                            followers = followers,
                            following = following,
                            initialTab = screen.initialTab,
                            onFollowToggle = { viewModel.toggleFollowUser(it) },
                            onUserClick = { userId ->
                                if (userId == currentUser!!.id) currentScreen = Screen.Profile
                                else navigateTo(Screen.UserProfile(userId))
                            },
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    Screen.Search -> {
                        BackHandler { navigateBack() }
                        val searchUsers by viewModel.searchResultsUsers.collectAsStateWithLifecycle()
                        val searchPosts by viewModel.searchResultsPosts.collectAsStateWithLifecycle()
                        val history by viewModel.searchHistory.collectAsStateWithLifecycle()

                        SearchScreen(
                            currentUserId = currentUser!!.id,
                            searchQuery = viewModel.searchQuery,
                            onQueryChange = { viewModel.searchQuery = it },
                            onPerformSearch = { viewModel.performSearch(it) },
                            searchHistory = history,
                            onClearHistory = { viewModel.clearSearchHistory() },
                            onDeleteHistoryItem = { viewModel.removeSearchHistoryItem(it) },
                            userResults = searchUsers,
                            postResults = searchPosts,
                            isSearching = viewModel.isSearching,
                            onUserClick = { userId ->
                                if (userId == currentUser!!.id) currentScreen = Screen.Profile
                                else navigateTo(Screen.UserProfile(userId))
                            },
                            onFollowClick = { viewModel.toggleFollowUser(it) },
                            onLikePostClick = { viewModel.toggleLikePost(it) },
                            onSavePostClick = { viewModel.toggleSavePost(it) },
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    Screen.Messages -> {
                        BackHandler { navigateBack() }
                        val convos by viewModel.conversations.collectAsStateWithLifecycle()
                        val users by viewModel.adminAllUsers.collectAsStateWithLifecycle()
                        val otherUsers = users.filter { it.id != currentUser!!.id }

                        MessagesListScreen(
                            conversations = convos,
                            allUsers = otherUsers,
                            onSelectConversation = { otherId ->
                                viewModel.openChat(otherId)
                                navigateTo(Screen.Chat(otherId))
                            },
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    is Screen.Chat -> {
                        BackHandler { navigateBack() }
                        var otherUser by remember { mutableStateOf<UserEntity?>(null) }
                        LaunchedEffect(screen.otherUserId) {
                            otherUser = viewModel.getUserById(screen.otherUserId)
                        }
                        val chatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()

                        ChatDetailScreen(
                            currentUserId = currentUser!!.id,
                            otherUser = otherUser,
                            messages = chatMessages,
                            onSendMessage = { text -> viewModel.sendMessage(screen.otherUserId, text) },
                            onDeleteMessage = { viewModel.deleteMessage(it) },
                            onNavigateBack = { navigateBack() },
                            onViewProfile = { userId -> navigateTo(Screen.UserProfile(userId)) }
                        )
                    }

                    Screen.Settings -> {
                        BackHandler { navigateBack() }
                        SettingsScreen(
                            currentUser = currentUser,
                            session = appSession,
                            onNavigateBack = { navigateBack() },
                            onNavigateToEditProfile = { navigateTo(Screen.EditProfile) },
                            onNavigateToSavedPosts = { navigateTo(Screen.SavedPosts) },
                            onNavigateToBlockedUsers = { navigateTo(Screen.BlockedUsers) },
                            onNavigateToReportHistory = { navigateTo(Screen.ReportHistory) },
                            onNavigateToAdminDashboard = { navigateTo(Screen.AdminDashboard) },
                            onThemeChange = { viewModel.updateTheme(it) },
                            onChangePassword = { oldPass, newPass ->
                                viewModel.changePassword(oldPass, newPass) { success, err ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(if (success) "Password updated!" else (err ?: "Failed"))
                                    }
                                }
                            },
                            onLogout = {
                                viewModel.logout {
                                    authScreen = Screen.Login
                                }
                            },
                            onDeleteAccount = {
                                viewModel.deleteAccount {
                                    authScreen = Screen.Welcome
                                }
                            }
                        )
                    }

                    Screen.SavedPosts -> {
                        BackHandler { navigateBack() }
                        val savedList by viewModel.savedPosts.collectAsStateWithLifecycle()
                        SavedPostsScreen(
                            currentUserId = currentUser!!.id,
                            savedPosts = savedList,
                            onLikeClick = { viewModel.toggleLikePost(it) },
                            onSaveClick = { viewModel.toggleSavePost(it) },
                            onUserClick = { userId ->
                                if (userId == currentUser!!.id) currentScreen = Screen.Profile
                                else navigateTo(Screen.UserProfile(userId))
                            },
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    Screen.BlockedUsers -> {
                        BackHandler { navigateBack() }
                        val blockedList by viewModel.blockedUsers.collectAsStateWithLifecycle()
                        BlockedUsersScreen(
                            blockedUsers = blockedList,
                            onUnblockClick = { viewModel.unblockUser(it) },
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    Screen.ReportHistory -> {
                        BackHandler { navigateBack() }
                        val userReportsList by viewModel.userReports.collectAsStateWithLifecycle()
                        ReportHistoryScreen(
                            reports = userReportsList,
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    Screen.AdminDashboard -> {
                        BackHandler { navigateBack() }
                        val stats by viewModel.platformStats.collectAsStateWithLifecycle()
                        val reports by viewModel.adminReports.collectAsStateWithLifecycle()
                        val allUsers by viewModel.adminAllUsers.collectAsStateWithLifecycle()

                        AdminDashboardScreen(
                            stats = stats,
                            reports = reports,
                            users = allUsers,
                            onUpdateReportStatus = { reportId, status ->
                                viewModel.adminUpdateReport(reportId, status)
                            },
                            onDeletePost = { viewModel.adminDeletePost(it) },
                            onToggleSuspendUser = { userId, suspend ->
                                viewModel.adminToggleSuspend(userId, suspend)
                            },
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}
