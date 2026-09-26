package com.example.data.repository

import com.example.data.dao.ConnectUpDao
import com.example.data.model.*
import com.example.util.PasswordHasher
import com.example.util.VideoUrlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class ConnectUpRepository(private val dao: ConnectUpDao) {

    // === AUTH & SESSION ===
    val appSessionFlow: Flow<AppSessionEntity?> = dao.getAppSessionFlow()

    fun getCurrentUserFlow(): Flow<UserEntity?> {
        return dao.getAppSessionFlow().flatMapLatest { session ->
            val userId = session?.loggedInUserId
            if (userId != null) {
                dao.getUserByIdFlow(userId)
            } else {
                flowOf(null)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun registerUser(
        fullName: String,
        username: String,
        email: String,
        password: String,
        dateOfBirth: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val cleanUsername = username.trim().lowercase()
            val cleanEmail = email.trim().lowercase()

            if (dao.getUserByUsername(cleanUsername) != null) {
                return@withContext Result.failure(Exception("Username @$cleanUsername is already taken."))
            }
            if (dao.getUserByEmail(cleanEmail) != null) {
                return@withContext Result.failure(Exception("An account with email $cleanEmail already exists."))
            }

            val newUser = UserEntity(
                name = fullName.trim(),
                username = cleanUsername,
                email = cleanEmail,
                passwordHash = PasswordHasher.hash(password),
                dateOfBirth = dateOfBirth,
                profileImage = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                coverImage = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800",
                bio = "Hey there! I am using ConnectUp.",
                createdAt = System.currentTimeMillis()
            )

            val newId = dao.insertUser(newUser)
            val createdUser = newUser.copy(id = newId)

            // Auto-login registered user
            dao.setLoggedInUser(newId)

            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(identifier: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val clean = identifier.trim()
            val user = dao.getUserByIdentifier(clean)
                ?: return@withContext Result.failure(Exception("No account found with username or email: $identifier"))

            if (user.isSuspended) {
                return@withContext Result.failure(Exception("This account has been suspended by ConnectUp Trust & Safety."))
            }

            if (!PasswordHasher.verify(password, user.passwordHash)) {
                return@withContext Result.failure(Exception("Incorrect password. Please try again."))
            }

            dao.setLoggedInUser(user.id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        dao.setLoggedInUser(null)
    }

    suspend fun updateProfile(
        userId: Long,
        name: String,
        username: String,
        bio: String,
        website: String,
        profileImage: String,
        coverImage: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
            val cleanUsername = username.trim().lowercase()

            if (cleanUsername != existing.username) {
                val taken = dao.getUserByUsername(cleanUsername)
                if (taken != null && taken.id != userId) {
                    return@withContext Result.failure(Exception("Username @$cleanUsername is already taken."))
                }
            }

            val updated = existing.copy(
                name = name.trim(),
                username = cleanUsername,
                bio = bio.trim(),
                website = website.trim(),
                profileImage = profileImage.ifEmpty { existing.profileImage },
                coverImage = coverImage.ifEmpty { existing.coverImage },
                updatedAt = System.currentTimeMillis()
            )
            dao.updateUser(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(userId: Long, currentPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
            if (!PasswordHasher.verify(currentPass, user.passwordHash)) {
                return@withContext Result.failure(Exception("Current password does not match."))
            }
            val updated = user.copy(passwordHash = PasswordHasher.hash(newPass))
            dao.updateUser(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(userId: Long) = withContext(Dispatchers.IO) {
        dao.deleteUser(userId)
        dao.setLoggedInUser(null)
    }

    suspend fun setThemeMode(mode: String) = withContext(Dispatchers.IO) {
        dao.setThemeMode(mode)
    }

    // === USER STATS & DETAILS ===
    fun getUserProfileUiFlow(targetUserId: Long, currentUserId: Long): Flow<UserProfileUiModel?> {
        val userFlow = dao.getUserByIdFlow(targetUserId)
        val statsFlow = combine(
            dao.getPostsCountFlow(targetUserId),
            dao.getFollowersCountFlow(targetUserId),
            dao.getFollowingCountFlow(targetUserId)
        ) { posts, followers, following ->
            Triple(posts, followers, following)
        }
        val statusFlow = combine(
            dao.isFollowingFlow(currentUserId, targetUserId),
            dao.isUserBlockedFlow(currentUserId, targetUserId)
        ) { isFollowing, isBlocked ->
            Pair(isFollowing, isBlocked)
        }

        return combine(userFlow, statsFlow, statusFlow) { user, (postsCount, followers, following), (isFollowing, isBlocked) ->
            if (user == null) null
            else UserProfileUiModel(
                user = user,
                postsCount = postsCount,
                followersCount = followers,
                followingCount = following,
                isFollowing = isFollowing,
                isBlocked = isBlocked
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getUserById(userId: Long): UserEntity? = withContext(Dispatchers.IO) {
        dao.getUserById(userId)
    }

    // === FEED & POSTS ===
    private fun sanitizePost(post: PostEntity): PostEntity {
        val cleanUrl = VideoUrlUtils.resolvePlayableUrl(post.mediaUrl)
        return if (cleanUrl != post.mediaUrl) post.copy(mediaUrl = cleanUrl) else post
    }

    fun getFeedPostsFlow(currentUserId: Long, followingOnly: Boolean): Flow<List<PostUiModel>> {
        val postsSourceFlow: Flow<List<PostEntity>> = if (followingOnly) {
            // Get posts from followed accounts
            dao.getFollowingFlow(currentUserId).flatMapLatest { followingUsers ->
                val ids = followingUsers.map { it.id } + listOf(currentUserId)
                dao.getPostsFromUsersFlow(ids)
            }
        } else {
            // Public / For You feed
            dao.getAllPostsFlow()
        }

        return combine(postsSourceFlow, dao.getBlockedUserIdsFlow(currentUserId)) { posts, blockedIds ->
            // Filter out posts from blocked users
            posts.filterNot { blockedIds.contains(it.userId) }
        }.map { posts ->
            posts.mapNotNull { rawPost ->
                val post = sanitizePost(rawPost)
                val author = dao.getUserById(post.userId) ?: return@mapNotNull null
                if (author.isSuspended) return@mapNotNull null

                val likeCount = dao.getPostLikeCountFlow(post.id).first()
                val commentCount = dao.getPostCommentCountFlow(post.id).first()
                val isLiked = dao.isPostLiked(currentUserId, post.id)
                val isSaved = dao.isPostSaved(currentUserId, post.id)

                PostUiModel(
                    post = post,
                    author = author,
                    likeCount = likeCount,
                    commentCount = commentCount,
                    isLikedByCurrentUser = isLiked,
                    isSavedByCurrentUser = isSaved
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getUserPostsUiFlow(userId: Long, currentUserId: Long): Flow<List<PostUiModel>> {
        return dao.getPostsByUserIdFlow(userId).map { posts ->
            posts.mapNotNull { rawPost ->
                val post = sanitizePost(rawPost)
                val author = dao.getUserById(post.userId) ?: return@mapNotNull null
                val likeCount = dao.getPostLikeCountFlow(post.id).first()
                val commentCount = dao.getPostCommentCountFlow(post.id).first()
                val isLiked = dao.isPostLiked(currentUserId, post.id)
                val isSaved = dao.isPostSaved(currentUserId, post.id)
                PostUiModel(post, author, likeCount, commentCount, isLiked, isSaved)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getLikedPostsUiFlow(userId: Long, currentUserId: Long): Flow<List<PostUiModel>> {
        return dao.getLikedPostsByUserFlow(userId).map { posts ->
            posts.mapNotNull { rawPost ->
                val post = sanitizePost(rawPost)
                val author = dao.getUserById(post.userId) ?: return@mapNotNull null
                val likeCount = dao.getPostLikeCountFlow(post.id).first()
                val commentCount = dao.getPostCommentCountFlow(post.id).first()
                val isLiked = dao.isPostLiked(currentUserId, post.id)
                val isSaved = dao.isPostSaved(currentUserId, post.id)
                PostUiModel(post, author, likeCount, commentCount, isLiked, isSaved)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getSavedPostsUiFlow(currentUserId: Long): Flow<List<PostUiModel>> {
        return dao.getSavedPostsFlow(currentUserId).map { posts ->
            posts.mapNotNull { rawPost ->
                val post = sanitizePost(rawPost)
                val author = dao.getUserById(post.userId) ?: return@mapNotNull null
                val likeCount = dao.getPostLikeCountFlow(post.id).first()
                val commentCount = dao.getPostCommentCountFlow(post.id).first()
                val isLiked = dao.isPostLiked(currentUserId, post.id)
                PostUiModel(post, author, likeCount, commentCount, isLiked, isSavedByCurrentUser = true)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getExploreMediaPostsUiFlow(currentUserId: Long): Flow<List<PostUiModel>> {
        return dao.getExploreMediaPostsFlow().map { posts ->
            posts.mapNotNull { rawPost ->
                val post = sanitizePost(rawPost)
                val author = dao.getUserById(post.userId) ?: return@mapNotNull null
                val likeCount = dao.getPostLikeCountFlow(post.id).first()
                val commentCount = dao.getPostCommentCountFlow(post.id).first()
                val isLiked = dao.isPostLiked(currentUserId, post.id)
                val isSaved = dao.isPostSaved(currentUserId, post.id)
                PostUiModel(post, author, likeCount, commentCount, isLiked, isSaved)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getReelsUiFlow(currentUserId: Long): Flow<List<PostUiModel>> {
        return combine(dao.getAllReelsFlow(), dao.getBlockedUserIdsFlow(currentUserId)) { reels, blockedIds ->
            reels.filterNot { blockedIds.contains(it.userId) }
        }.map { reels ->
            reels.mapNotNull { rawPost ->
                val post = sanitizePost(rawPost)
                val author = dao.getUserById(post.userId) ?: return@mapNotNull null
                if (author.isSuspended) return@mapNotNull null
                val likeCount = dao.getPostLikeCountFlow(post.id).first()
                val commentCount = dao.getPostCommentCountFlow(post.id).first()
                val isLiked = dao.isPostLiked(currentUserId, post.id)
                val isSaved = dao.isPostSaved(currentUserId, post.id)
                PostUiModel(post, author, likeCount, commentCount, isLiked, isSaved)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getUserReelsUiFlow(userId: Long, currentUserId: Long): Flow<List<PostUiModel>> {
        return dao.getReelsByUserIdFlow(userId).map { reels ->
            reels.mapNotNull { rawPost ->
                val post = sanitizePost(rawPost)
                val author = dao.getUserById(post.userId) ?: return@mapNotNull null
                val likeCount = dao.getPostLikeCountFlow(post.id).first()
                val commentCount = dao.getPostCommentCountFlow(post.id).first()
                val isLiked = dao.isPostLiked(currentUserId, post.id)
                val isSaved = dao.isPostSaved(currentUserId, post.id)
                PostUiModel(post, author, likeCount, commentCount, isLiked, isSaved)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun incrementReelView(postId: Long) = withContext(Dispatchers.IO) {
        dao.incrementPostViews(postId)
    }

    suspend fun createPost(
        userId: Long,
        caption: String,
        mediaUrl: String?,
        mediaType: String?,
        hashtags: String,
        isReel: Boolean = false,
        soundTitle: String = "Original Audio"
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val post = PostEntity(
                userId = userId,
                caption = caption.trim(),
                mediaUrl = mediaUrl?.ifEmpty { null },
                mediaType = mediaType,
                hashtags = hashtags.trim(),
                isReel = isReel,
                soundTitle = soundTitle.ifBlank { "Original Audio" },
                createdAt = System.currentTimeMillis()
            )
            val id = dao.insertPost(post)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: Long, currentUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val post = dao.getPostById(postId) ?: return@withContext Result.failure(Exception("Post not found"))
            val currentUser = dao.getUserById(currentUserId)
            if (post.userId != currentUserId && currentUser?.isAdmin != true) {
                return@withContext Result.failure(Exception("Unauthorized: You can only delete your own posts."))
            }
            dao.deletePost(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === LIKES ===
    suspend fun toggleLike(postId: Long, userId: Long) = withContext(Dispatchers.IO) {
        val isLiked = dao.isPostLiked(userId, postId)
        if (isLiked) {
            dao.deleteLike(userId, postId)
        } else {
            dao.insertLike(LikeEntity(userId = userId, postId = postId))
            // Generate notification for post author
            val post = dao.getPostById(postId)
            if (post != null && post.userId != userId) {
                val user = dao.getUserById(userId)
                dao.insertNotification(
                    NotificationEntity(
                        userId = post.userId,
                        actorId = userId,
                        type = "POST_LIKE",
                        postId = postId,
                        message = "liked your post: \"${post.caption.take(30)}...\""
                    )
                )
            }
        }
    }

    // === SAVED POSTS ===
    suspend fun toggleSave(postId: Long, userId: Long) = withContext(Dispatchers.IO) {
        val isSaved = dao.isPostSaved(userId, postId)
        if (isSaved) {
            dao.deleteSavedPost(userId, postId)
        } else {
            dao.insertSavedPost(SavedPostEntity(userId = userId, postId = postId))
        }
    }

    // === COMMENTS ===
    fun getCommentsForPostFlow(postId: Long, currentUserId: Long): Flow<List<CommentUiModel>> {
        return dao.getCommentsForPostFlow(postId).map { comments ->
            comments.mapNotNull { comment ->
                val author = dao.getUserById(comment.userId) ?: return@mapNotNull null
                val likeCount = dao.getCommentLikeCountFlow(comment.id).first()
                val isLiked = dao.isCommentLikedFlow(currentUserId, comment.id).first()
                CommentUiModel(comment, author, likeCount, isLiked)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addComment(postId: Long, userId: Long, text: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (text.isBlank()) return@withContext Result.failure(Exception("Comment cannot be empty."))
            val comment = CommentEntity(
                userId = userId,
                postId = postId,
                text = text.trim(),
                createdAt = System.currentTimeMillis()
            )
            val id = dao.insertComment(comment)

            val post = dao.getPostById(postId)
            if (post != null && post.userId != userId) {
                dao.insertNotification(
                    NotificationEntity(
                        userId = post.userId,
                        actorId = userId,
                        type = "COMMENT",
                        postId = postId,
                        message = "commented on your post: \"${text.take(30)}\""
                    )
                )
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(commentId: Long, currentUserId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteComment(commentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCommentLike(commentId: Long, currentUserId: Long) = withContext(Dispatchers.IO) {
        val isLiked = dao.isCommentLikedFlow(currentUserId, commentId).first()
        if (isLiked) {
            dao.deleteCommentLike(currentUserId, commentId)
        } else {
            dao.insertCommentLike(CommentLikeEntity(userId = currentUserId, commentId = commentId))
        }
    }

    // === FOLLOW SYSTEM ===
    suspend fun followUser(followerId: Long, followingId: Long) = withContext(Dispatchers.IO) {
        if (followerId == followingId) return@withContext
        dao.insertFollow(FollowEntity(followerId = followerId, followingId = followingId))
        dao.insertNotification(
            NotificationEntity(
                userId = followingId,
                actorId = followerId,
                type = "FOLLOW",
                postId = null,
                message = "started following you."
            )
        )
    }

    suspend fun unfollowUser(followerId: Long, followingId: Long) = withContext(Dispatchers.IO) {
        dao.deleteFollow(followerId, followingId)
    }

    fun getFollowersFlow(userId: Long): Flow<List<UserEntity>> = dao.getFollowersFlow(userId).flowOn(Dispatchers.IO)
    fun getFollowingFlow(userId: Long): Flow<List<UserEntity>> = dao.getFollowingFlow(userId).flowOn(Dispatchers.IO)

    suspend fun getSuggestedUsers(currentUserId: Long, limit: Int = 10): List<UserEntity> = withContext(Dispatchers.IO) {
        val suggested = dao.getSuggestedUsers(currentUserId, limit)
        val followingIds = dao.getFollowingIds(currentUserId)
        suggested.filterNot { followingIds.contains(it.id) }
    }

    // === SEARCH & DISCOVER ===
    suspend fun searchUsers(query: String): List<UserEntity> = withContext(Dispatchers.IO) {
        if (query.isBlank()) emptyList() else dao.searchUsers(query.trim())
    }

    suspend fun searchPosts(query: String, currentUserId: Long): List<PostUiModel> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val posts = dao.searchPosts(query.trim())
        posts.mapNotNull { post ->
            val author = dao.getUserById(post.userId) ?: return@mapNotNull null
            val likeCount = dao.getPostLikeCountFlow(post.id).first()
            val commentCount = dao.getPostCommentCountFlow(post.id).first()
            val isLiked = dao.isPostLiked(currentUserId, post.id)
            val isSaved = dao.isPostSaved(currentUserId, post.id)
            PostUiModel(post, author, likeCount, commentCount, isLiked, isSaved)
        }
    }

    fun getSearchHistoryFlow(userId: Long): Flow<List<SearchHistoryEntity>> = dao.getRecentSearchesFlow(userId)

    suspend fun addSearchHistory(userId: Long, query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            dao.insertSearchHistory(SearchHistoryEntity(userId = userId, query = query.trim()))
        }
    }

    suspend fun clearSearchHistory(userId: Long) = withContext(Dispatchers.IO) {
        dao.clearSearchHistory(userId)
    }

    suspend fun removeSearchHistoryItem(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteSearchItem(id)
    }

    // === DIRECT MESSAGES ===
    fun getConversationsFlow(currentUserId: Long): Flow<List<ConversationSummary>> {
        return dao.getAllMessagesForUserFlow(currentUserId).map { messages ->
            // Group messages by counterpart
            val counterpartIds = messages.map { if (it.senderId == currentUserId) it.receiverId else it.senderId }.distinct()
            counterpartIds.mapNotNull { otherId ->
                val otherUser = dao.getUserById(otherId) ?: return@mapNotNull null
                val userMessages = messages.filter {
                    (it.senderId == currentUserId && it.receiverId == otherId) ||
                    (it.senderId == otherId && it.receiverId == currentUserId)
                }
                val lastMsg = userMessages.maxByOrNull { it.createdAt } ?: return@mapNotNull null
                val unreadCount = userMessages.count { it.receiverId == currentUserId && it.seenAt == null }
                ConversationSummary(otherUser, lastMsg, unreadCount)
            }.sortedByDescending { it.lastMessage.createdAt }
        }.flowOn(Dispatchers.IO)
    }

    fun getMessagesBetweenUsersFlow(user1: Long, user2: Long): Flow<List<MessageEntity>> =
        dao.getMessagesBetweenUsersFlow(user1, user2).flowOn(Dispatchers.IO)

    suspend fun sendMessage(senderId: Long, receiverId: Long, text: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (text.isBlank()) return@withContext Result.failure(Exception("Message cannot be empty."))
            val msg = MessageEntity(
                senderId = senderId,
                receiverId = receiverId,
                text = text.trim(),
                createdAt = System.currentTimeMillis()
            )
            val id = dao.insertMessage(msg)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: Long) = withContext(Dispatchers.IO) {
        dao.deleteMessage(messageId)
    }

    suspend fun markMessagesSeen(currentUserId: Long, otherUserId: Long) = withContext(Dispatchers.IO) {
        dao.markMessagesAsSeen(currentUserId, otherUserId)
    }

    // === NOTIFICATIONS ===
    fun getNotificationsFlow(userId: Long): Flow<List<NotificationUiModel>> {
        return dao.getNotificationsFlow(userId).map { list ->
            list.map { notif ->
                val actor = dao.getUserById(notif.actorId)
                val post = notif.postId?.let { dao.getPostById(it) }
                NotificationUiModel(notif, actor, post)
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getUnreadNotificationsCountFlow(userId: Long): Flow<Int> = dao.getUnreadNotificationCountFlow(userId)

    suspend fun markNotificationRead(id: Long) = withContext(Dispatchers.IO) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead(userId: Long) = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsRead(userId)
    }

    // === SAFETY: REPORT & BLOCK ===
    suspend fun submitReport(
        reporterId: Long,
        targetType: String,
        targetId: Long,
        reason: String,
        description: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val report = ReportEntity(
                reporterId = reporterId,
                targetType = targetType,
                targetId = targetId,
                reason = reason,
                description = description.trim(),
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            val id = dao.insertReport(report)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockUser(blockerId: Long, targetUserId: Long) = withContext(Dispatchers.IO) {
        dao.blockUser(BlockedUserEntity(blockerId = blockerId, blockedUserId = targetUserId))
        // Also unfollow mutually upon block
        dao.deleteFollow(blockerId, targetUserId)
        dao.deleteFollow(targetUserId, blockerId)
    }

    suspend fun unblockUser(blockerId: Long, targetUserId: Long) = withContext(Dispatchers.IO) {
        dao.unblockUser(blockerId, targetUserId)
    }

    fun getBlockedUsersFlow(blockerId: Long): Flow<List<UserEntity>> = dao.getBlockedUsersFlow(blockerId).flowOn(Dispatchers.IO)

    fun getUserReportsFlow(reporterId: Long): Flow<List<ReportEntity>> = dao.getReportsByReporterFlow(reporterId).flowOn(Dispatchers.IO)

    // === ADMIN DASHBOARD ===
    fun getAllReportsAdminFlow(): Flow<List<ReportEntity>> = dao.getAllReportsFlow().flowOn(Dispatchers.IO)

    fun getAllUsersAdminFlow(): Flow<List<UserEntity>> = dao.getAllUsersAdminFlow().flowOn(Dispatchers.IO)

    suspend fun updateReportStatus(reportId: Long, status: String) = withContext(Dispatchers.IO) {
        dao.updateReportStatus(reportId, status)
    }

    suspend fun setSuspendedStatus(userId: Long, suspended: Boolean) = withContext(Dispatchers.IO) {
        dao.setSuspendedStatus(userId, suspended)
    }

    suspend fun getPlatformStats(): PlatformStats = withContext(Dispatchers.IO) {
        val totalUsers = dao.getTotalUsersCount()
        val totalPosts = dao.getTotalPostsCount()
        val totalComments = dao.getTotalCommentsCount()
        val totalReports = dao.getTotalReportsCount()
        PlatformStats(
            totalUsers = totalUsers,
            totalPosts = totalPosts,
            totalComments = totalComments,
            totalReports = totalReports,
            activeUsers = totalUsers
        )
    }
}
