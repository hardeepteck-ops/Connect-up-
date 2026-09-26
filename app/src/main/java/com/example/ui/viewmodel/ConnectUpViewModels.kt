package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ConnectUpRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectUpViewModel(private val repository: ConnectUpRepository) : ViewModel() {

    // === CURRENT USER & SESSION ===
    val currentUser: StateFlow<UserEntity?> = repository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSession: StateFlow<AppSessionEntity?> = repository.appSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Auth operation states
    var isAuthLoading by androidx.compose.runtime.mutableStateOf(false)
        private set
    var authError by androidx.compose.runtime.mutableStateOf<String?>(null)
        private set

    // Onboarding flag
    var isFirstRunOnboarding by androidx.compose.runtime.mutableStateOf(false)

    // === FEED ===
    private val _isFollowingOnly = MutableStateFlow(false)
    val isFollowingOnly: StateFlow<Boolean> = _isFollowingOnly.asStateFlow()

    private val _isFeedLoading = MutableStateFlow(false)
    val isFeedLoading: StateFlow<Boolean> = _isFeedLoading.asStateFlow()

    val feedPosts: StateFlow<List<PostUiModel>> = combine(
        currentUser.filterNotNull(),
        _isFollowingOnly
    ) { user, followingOnly ->
        Pair(user.id, followingOnly)
    }.flatMapLatest { (userId, followingOnly) ->
        repository.getFeedPostsFlow(userId, followingOnly)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === COMMENTS ===
    private val _activePostId = MutableStateFlow<Long?>(null)
    val activePostComments: StateFlow<List<CommentUiModel>> = combine(
        _activePostId,
        currentUser
    ) { postId, user ->
        Pair(postId, user?.id ?: 0L)
    }.flatMapLatest { (postId, userId) ->
        if (postId == null) flowOf(emptyList())
        else repository.getCommentsForPostFlow(postId, userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === NOTIFICATIONS ===
    val notifications: StateFlow<List<NotificationUiModel>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getNotificationsFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(0)
        else repository.getUnreadNotificationsCountFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // === MESSAGES ===
    val conversations: StateFlow<List<ConversationSummary>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getConversationsFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadMessagesTotalCount: StateFlow<Int> = conversations.map { list ->
        list.sumOf { it.unreadCount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _activeChatOtherUserId = MutableStateFlow<Long?>(null)
    val activeChatMessages: StateFlow<List<MessageEntity>> = combine(
        currentUser,
        _activeChatOtherUserId
    ) { user, otherId ->
        Pair(user?.id, otherId)
    }.flatMapLatest { (myId, otherId) ->
        if (myId == null || otherId == null) flowOf(emptyList())
        else repository.getMessagesBetweenUsersFlow(myId, otherId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === EXPLORE & SUGGESTIONS ===
    val exploreMediaPosts: StateFlow<List<PostUiModel>> = currentUser.flatMapLatest { user ->
        repository.getExploreMediaPostsUiFlow(user?.id ?: 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === REELS ===
    val reels: StateFlow<List<PostUiModel>> = currentUser.flatMapLatest { user ->
        repository.getReelsUiFlow(user?.id ?: 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _suggestedUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val suggestedUsers: StateFlow<List<UserEntity>> = _suggestedUsers.asStateFlow()

    // === SEARCH ===
    var searchQuery by androidx.compose.runtime.mutableStateOf("")
    private val _searchResultsUsers = MutableStateFlow<List<UserEntity>>(emptyList())
    val searchResultsUsers: StateFlow<List<UserEntity>> = _searchResultsUsers.asStateFlow()

    private val _searchResultsPosts = MutableStateFlow<List<PostUiModel>>(emptyList())
    val searchResultsPosts: StateFlow<List<PostUiModel>> = _searchResultsPosts.asStateFlow()

    var isSearching by androidx.compose.runtime.mutableStateOf(false)

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getSearchHistoryFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === SAVED POSTS ===
    val savedPosts: StateFlow<List<PostUiModel>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getSavedPostsUiFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === BLOCKED USERS & REPORTS ===
    val blockedUsers: StateFlow<List<UserEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getBlockedUsersFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userReports: StateFlow<List<ReportEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getUserReportsFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === ADMIN ===
    val adminReports: StateFlow<List<ReportEntity>> = repository.getAllReportsAdminFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllUsers: StateFlow<List<UserEntity>> = repository.getAllUsersAdminFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _platformStats = MutableStateFlow(PlatformStats(0, 0, 0, 0, 0))
    val platformStats: StateFlow<PlatformStats> = _platformStats.asStateFlow()

    init {
        refreshSuggestedUsers()
        refreshAdminStats()
    }

    fun refreshSuggestedUsers() {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: 0L
            _suggestedUsers.value = repository.getSuggestedUsers(userId)
        }
    }

    fun refreshAdminStats() {
        viewModelScope.launch {
            _platformStats.value = repository.getPlatformStats()
        }
    }

    // === AUTH ACTIONS ===
    fun login(identifier: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isAuthLoading = true
            authError = null
            val result = repository.loginUser(identifier, pass)
            isAuthLoading = false
            result.onSuccess {
                refreshSuggestedUsers()
                onSuccess()
            }.onFailure {
                authError = it.message ?: "Login failed. Please check your credentials."
            }
        }
    }

    fun register(
        fullName: String,
        username: String,
        email: String,
        pass: String,
        dob: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isAuthLoading = true
            authError = null
            val result = repository.registerUser(fullName, username, email, pass, dob)
            isAuthLoading = false
            result.onSuccess {
                isFirstRunOnboarding = true
                refreshSuggestedUsers()
                onSuccess()
            }.onFailure {
                authError = it.message ?: "Registration failed."
            }
        }
    }

    fun completeOnboarding(bio: String, website: String, avatar: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.updateProfile(
                userId = user.id,
                name = user.name,
                username = user.username,
                bio = bio,
                website = website,
                profileImage = avatar,
                coverImage = user.coverImage
            )
            isFirstRunOnboarding = false
            onSuccess()
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }

    fun changePassword(currentPass: String, newPass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val res = repository.changePassword(user.id, currentPass, newPass)
            res.onSuccess { onResult(true, null) }
                .onFailure { onResult(false, it.message) }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.deleteAccount(user.id)
            onSuccess()
        }
    }

    fun updateTheme(themeMode: String) {
        viewModelScope.launch {
            repository.setThemeMode(themeMode)
        }
    }

    // === FEED ACTIONS ===
    fun setFollowingOnlyTab(followingOnly: Boolean) {
        _isFollowingOnly.value = followingOnly
    }

    fun toggleLikePost(postId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.toggleLike(postId, user.id)
        }
    }

    fun toggleSavePost(postId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.toggleSave(postId, user.id)
        }
    }

    fun createPost(
        caption: String,
        mediaUrl: String?,
        mediaType: String?,
        hashtags: String,
        isReel: Boolean = false,
        soundTitle: String = "Original Audio",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val res = repository.createPost(user.id, caption, mediaUrl, mediaType, hashtags, isReel, soundTitle)
            if (res.isSuccess) {
                refreshAdminStats()
                onSuccess()
            }
        }
    }

    fun incrementReelView(postId: Long) {
        viewModelScope.launch {
            repository.incrementReelView(postId)
        }
    }

    fun getUserReelsFlow(userId: Long): Flow<List<PostUiModel>> {
        val currentId = currentUser.value?.id ?: 0L
        return repository.getUserReelsUiFlow(userId, currentId)
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.deletePost(postId, user.id)
            refreshAdminStats()
        }
    }

    // === COMMENTS ACTIONS ===
    fun loadCommentsForPost(postId: Long) {
        _activePostId.value = postId
    }

    fun addComment(postId: Long, text: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.addComment(postId, user.id, text)
            refreshAdminStats()
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.deleteComment(commentId, user.id)
            refreshAdminStats()
        }
    }

    fun toggleCommentLike(commentId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.toggleCommentLike(commentId, user.id)
        }
    }

    // === FOLLOW ACTIONS ===
    fun toggleFollowUser(targetUserId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val isFollowing = repository.getUserProfileUiFlow(targetUserId, user.id).first()?.isFollowing ?: false
            if (isFollowing) {
                repository.unfollowUser(user.id, targetUserId)
            } else {
                repository.followUser(user.id, targetUserId)
            }
            refreshSuggestedUsers()
        }
    }

    // === SEARCH ACTIONS ===
    fun performSearch(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResultsUsers.value = emptyList()
                _searchResultsPosts.value = emptyList()
                return@launch
            }
            isSearching = true
            val user = currentUser.value
            _searchResultsUsers.value = repository.searchUsers(query)
            _searchResultsPosts.value = repository.searchPosts(query, user?.id ?: 0L)
            if (user != null) {
                repository.addSearchHistory(user.id, query)
            }
            isSearching = false
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.clearSearchHistory(user.id)
        }
    }

    fun removeSearchHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.removeSearchHistoryItem(id)
        }
    }

    // === MESSAGES ACTIONS ===
    fun openChat(otherUserId: Long) {
        _activeChatOtherUserId.value = otherUserId
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.markMessagesSeen(user.id, otherUserId)
        }
    }

    fun sendMessage(otherUserId: Long, text: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.sendMessage(user.id, otherUserId, text)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    // === NOTIFICATIONS ACTIONS ===
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.markAllNotificationsRead(user.id)
        }
    }

    // === SAFETY (REPORT & BLOCK) ===
    fun submitReport(targetType: String, targetId: Long, reason: String, description: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.submitReport(user.id, targetType, targetId, reason, description)
            refreshAdminStats()
        }
    }

    fun blockUser(targetUserId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.blockUser(user.id, targetUserId)
        }
    }

    fun unblockUser(targetUserId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.unblockUser(user.id, targetUserId)
        }
    }

    // === PROFILE MANAGEMENT ===
    fun getUserProfileFlow(userId: Long): Flow<UserProfileUiModel?> {
        val currentId = currentUser.value?.id ?: 0L
        return repository.getUserProfileUiFlow(userId, currentId)
    }

    fun getUserPostsFlow(userId: Long): Flow<List<PostUiModel>> {
        val currentId = currentUser.value?.id ?: 0L
        return repository.getUserPostsUiFlow(userId, currentId)
    }

    fun getLikedPostsFlow(userId: Long): Flow<List<PostUiModel>> {
        val currentId = currentUser.value?.id ?: 0L
        return repository.getLikedPostsUiFlow(userId, currentId)
    }

    fun getFollowersFlow(userId: Long): Flow<List<UserEntity>> = repository.getFollowersFlow(userId)
    fun getFollowingFlow(userId: Long): Flow<List<UserEntity>> = repository.getFollowingFlow(userId)

    suspend fun getUserById(userId: Long): UserEntity? = repository.getUserById(userId)

    fun updateProfile(
        name: String,
        username: String,
        bio: String,
        website: String,
        avatar: String,
        cover: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val res = repository.updateProfile(user.id, name, username, bio, website, avatar, cover)
            res.onSuccess { onResult(true, null) }
                .onFailure { onResult(false, it.message) }
        }
    }

    // === ADMIN ACTIONS ===
    fun adminUpdateReport(reportId: Long, status: String) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status)
            refreshAdminStats()
        }
    }

    fun adminToggleSuspend(userId: Long, suspend: Boolean) {
        viewModelScope.launch {
            repository.setSuspendedStatus(userId, suspend)
        }
    }

    fun adminDeletePost(postId: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.deletePost(postId, user.id)
            refreshAdminStats()
        }
    }
}

class ConnectUpViewModelFactory(private val repository: ConnectUpRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectUpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
