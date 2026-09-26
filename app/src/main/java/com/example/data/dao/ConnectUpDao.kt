package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectUpDao {

    // === USERS ===
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE (username = :identifier COLLATE NOCASE OR email = :identifier COLLATE NOCASE) LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE isSuspended = 0 ORDER BY createdAt DESC")
    fun getAllActiveUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersAdminFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE (name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%') AND isSuspended = 0")
    suspend fun searchUsers(query: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE id != :currentUserId AND isSuspended = 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getSuggestedUsers(currentUserId: Long, limit: Int = 10): List<UserEntity>

    @Query("UPDATE users SET isSuspended = :suspended WHERE id = :userId")
    suspend fun setSuspendedStatus(userId: Long, suspended: Boolean)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)

    // === FOLLOWS ===
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFollow(follow: FollowEntity): Long

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteFollow(followerId: Long, followingId: Long)

    @Query("SELECT COUNT(*) > 0 FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun isFollowing(followerId: Long, followingId: Long): Boolean

    @Query("SELECT COUNT(*) > 0 FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    fun isFollowingFlow(followerId: Long, followingId: Long): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM follows WHERE followingId = :userId")
    fun getFollowersCountFlow(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    fun getFollowingCountFlow(userId: Long): Flow<Int>

    @Query("SELECT u.* FROM users u INNER JOIN follows f ON u.id = f.followerId WHERE f.followingId = :userId")
    fun getFollowersFlow(userId: Long): Flow<List<UserEntity>>

    @Query("SELECT u.* FROM users u INNER JOIN follows f ON u.id = f.followingId WHERE f.followerId = :userId")
    fun getFollowingFlow(userId: Long): Flow<List<UserEntity>>

    @Query("SELECT followingId FROM follows WHERE followerId = :userId")
    suspend fun getFollowingIds(userId: Long): List<Long>

    // === POSTS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Long)

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: Long): PostEntity?

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    fun getPostByIdFlow(postId: Long): Flow<PostEntity?>

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserIdFlow(userId: Long): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM posts WHERE userId = :userId")
    fun getPostsCountFlow(userId: Long): Flow<Int>

    @Query("SELECT * FROM posts WHERE userId IN (:userIds) ORDER BY createdAt DESC")
    fun getPostsFromUsersFlow(userIds: List<Long>): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE caption LIKE '%' || :query || '%' OR hashtags LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchPosts(query: String): List<PostEntity>

    @Query("SELECT * FROM posts WHERE mediaUrl IS NOT NULL AND mediaUrl != '' ORDER BY createdAt DESC")
    fun getExploreMediaPostsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isReel = 1 ORDER BY createdAt DESC")
    fun getAllReelsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId AND isReel = 1 ORDER BY createdAt DESC")
    fun getReelsByUserIdFlow(userId: Long): Flow<List<PostEntity>>

    @Query("UPDATE posts SET viewsCount = viewsCount + 1 WHERE id = :postId")
    suspend fun incrementPostViews(postId: Long)

    @Query("UPDATE posts SET mediaUrl = :newUrl WHERE mediaUrl = :oldUrl")
    suspend fun updateLegacyMediaUrl(oldUrl: String, newUrl: String)

    @Query("UPDATE posts SET mediaUrl = :fallbackUrl WHERE mediaUrl LIKE '%commondatastorage.googleapis.com%'")
    suspend fun updateAllLegacyCommondatastorageUrls(fallbackUrl: String)

    // === LIKES ===
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLike(like: LikeEntity): Long

    @Query("DELETE FROM likes WHERE userId = :userId AND postId = :postId")
    suspend fun deleteLike(userId: Long, postId: Long)

    @Query("SELECT COUNT(*) > 0 FROM likes WHERE userId = :userId AND postId = :postId")
    suspend fun isPostLiked(userId: Long, postId: Long): Boolean

    @Query("SELECT COUNT(*) > 0 FROM likes WHERE userId = :userId AND postId = :postId")
    fun isPostLikedFlow(userId: Long, postId: Long): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    fun getPostLikeCountFlow(postId: Long): Flow<Int>

    @Query("SELECT p.* FROM posts p INNER JOIN likes l ON p.id = l.postId WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    fun getLikedPostsByUserFlow(userId: Long): Flow<List<PostEntity>>

    // === COMMENTS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Long)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPostFlow(postId: Long): Flow<List<CommentEntity>>

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    fun getPostCommentCountFlow(postId: Long): Flow<Int>

    // === COMMENT LIKES ===
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommentLike(commentLike: CommentLikeEntity): Long

    @Query("DELETE FROM comment_likes WHERE userId = :userId AND commentId = :commentId")
    suspend fun deleteCommentLike(userId: Long, commentId: Long)

    @Query("SELECT COUNT(*) > 0 FROM comment_likes WHERE userId = :userId AND commentId = :commentId")
    fun isCommentLikedFlow(userId: Long, commentId: Long): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM comment_likes WHERE commentId = :commentId")
    fun getCommentLikeCountFlow(commentId: Long): Flow<Int>

    // === SAVED POSTS ===
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedPost(savedPost: SavedPostEntity): Long

    @Query("DELETE FROM saved_posts WHERE userId = :userId AND postId = :postId")
    suspend fun deleteSavedPost(userId: Long, postId: Long)

    @Query("SELECT COUNT(*) > 0 FROM saved_posts WHERE userId = :userId AND postId = :postId")
    suspend fun isPostSaved(userId: Long, postId: Long): Boolean

    @Query("SELECT COUNT(*) > 0 FROM saved_posts WHERE userId = :userId AND postId = :postId")
    fun isPostSavedFlow(userId: Long, postId: Long): Flow<Boolean>

    @Query("SELECT p.* FROM posts p INNER JOIN saved_posts s ON p.id = s.postId WHERE s.userId = :userId ORDER BY s.createdAt DESC")
    fun getSavedPostsFlow(userId: Long): Flow<List<PostEntity>>

    // === MESSAGES ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("SELECT * FROM messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY createdAt ASC")
    fun getMessagesBetweenUsersFlow(user1: Long, user2: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY createdAt DESC")
    fun getAllMessagesForUserFlow(userId: Long): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET seenAt = :seenAt WHERE senderId = :otherUserId AND receiverId = :currentUserId AND seenAt IS NULL")
    suspend fun markMessagesAsSeen(currentUserId: Long, otherUserId: Long, seenAt: Long = System.currentTimeMillis())

    // === NOTIFICATIONS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsFlow(userId: Long): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadNotificationCountFlow(userId: Long): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: Long)

    // === REPORTS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE reporterId = :reporterId ORDER BY createdAt DESC")
    fun getReportsByReporterFlow(reporterId: Long): Flow<List<ReportEntity>>

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: Long, status: String)

    // === BLOCKED USERS ===
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun blockUser(blocked: BlockedUserEntity): Long

    @Query("DELETE FROM blocked_users WHERE blockerId = :blockerId AND blockedUserId = :blockedUserId")
    suspend fun unblockUser(blockerId: Long, blockedUserId: Long)

    @Query("SELECT COUNT(*) > 0 FROM blocked_users WHERE blockerId = :blockerId AND blockedUserId = :blockedUserId")
    suspend fun isUserBlocked(blockerId: Long, blockedUserId: Long): Boolean

    @Query("SELECT COUNT(*) > 0 FROM blocked_users WHERE blockerId = :blockerId AND blockedUserId = :blockedUserId")
    fun isUserBlockedFlow(blockerId: Long, blockedUserId: Long): Flow<Boolean>

    @Query("SELECT blockedUserId FROM blocked_users WHERE blockerId = :blockerId")
    suspend fun getBlockedUserIds(blockerId: Long): List<Long>

    @Query("SELECT blockedUserId FROM blocked_users WHERE blockerId = :blockerId")
    fun getBlockedUserIdsFlow(blockerId: Long): Flow<List<Long>>

    @Query("SELECT u.* FROM users u INNER JOIN blocked_users b ON u.id = b.blockedUserId WHERE b.blockerId = :blockerId")
    fun getBlockedUsersFlow(blockerId: Long): Flow<List<UserEntity>>

    // === SEARCH HISTORY ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(search: SearchHistoryEntity): Long

    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearchesFlow(userId: Long, limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun clearSearchHistory(userId: Long)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchItem(id: Long)

    // === APP SESSION ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAppSession(session: AppSessionEntity)

    @Query("SELECT * FROM app_session WHERE id = 1 LIMIT 1")
    fun getAppSessionFlow(): Flow<AppSessionEntity?>

    @Query("SELECT * FROM app_session WHERE id = 1 LIMIT 1")
    suspend fun getAppSession(): AppSessionEntity?

    @Query("UPDATE app_session SET loggedInUserId = :userId WHERE id = 1")
    suspend fun setLoggedInUser(userId: Long?)

    @Query("UPDATE app_session SET themeMode = :mode WHERE id = 1")
    suspend fun setThemeMode(mode: String)

    // === PLATFORM STATS ===
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUsersCount(): Int

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getTotalPostsCount(): Int

    @Query("SELECT COUNT(*) FROM comments")
    suspend fun getTotalCommentsCount(): Int

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun getTotalReportsCount(): Int
}
