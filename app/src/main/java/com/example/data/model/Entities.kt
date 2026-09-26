package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val profileImage: String = "",
    val coverImage: String = "",
    val bio: String = "",
    val website: String = "",
    val dateOfBirth: String = "",
    val accountType: String = "PUBLIC", // "PUBLIC" or "PRIVATE"
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "posts",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"]),
        Index(value = ["isReel"])
    ]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val caption: String,
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "IMAGE", "VIDEO", "REEL", null
    val hashtags: String = "", // comma-separated, e.g. "tech,connect,design"
    val isReel: Boolean = false,
    val soundTitle: String = "Original Audio",
    val viewsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "likes",
    indices = [
        Index(value = ["userId", "postId"], unique = true),
        Index(value = ["postId"])
    ]
)
data class LikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val postId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "comments",
    indices = [
        Index(value = ["postId"]),
        Index(value = ["userId"])
    ]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val postId: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "comment_likes",
    indices = [
        Index(value = ["userId", "commentId"], unique = true),
        Index(value = ["commentId"])
    ]
)
data class CommentLikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val commentId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "follows",
    indices = [
        Index(value = ["followerId", "followingId"], unique = true),
        Index(value = ["followingId"])
    ]
)
data class FollowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val followerId: Long,
    val followingId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["senderId"]),
        Index(value = ["receiverId"]),
        Index(value = ["createdAt"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long,
    val receiverId: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val seenAt: Long? = null
)

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["isRead"]),
        Index(value = ["createdAt"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, // recipient
    val actorId: Long, // trigger user
    val type: String, // "FOLLOW", "POST_LIKE", "COMMENT", "COMMENT_LIKE", "MENTION"
    val postId: Long? = null,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "saved_posts",
    indices = [
        Index(value = ["userId", "postId"], unique = true)
    ]
)
data class SavedPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val postId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reports",
    indices = [
        Index(value = ["targetType", "targetId"]),
        Index(value = ["status"])
    ]
)
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reporterId: Long,
    val targetType: String, // "POST", "COMMENT", "USER"
    val targetId: Long,
    val reason: String, // "Spam", "Harassment", "Hate/abusive content", "Fake account", "Inappropriate content", "Other"
    val description: String = "",
    val status: String = "PENDING", // "PENDING", "RESOLVED", "DISMISSED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "blocked_users",
    indices = [
        Index(value = ["blockerId", "blockedUserId"], unique = true)
    ]
)
data class BlockedUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockerId: Long,
    val blockedUserId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_session")
data class AppSessionEntity(
    @PrimaryKey val id: Int = 1,
    val loggedInUserId: Long? = null,
    val rememberMe: Boolean = true,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val pushLikes: Boolean = true,
    val pushComments: Boolean = true,
    val pushFollows: Boolean = true,
    val pushMessages: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val allowMessages: Boolean = true
)

// UI Aggregate models
data class PostUiModel(
    val post: PostEntity,
    val author: UserEntity,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean,
    val isSavedByCurrentUser: Boolean
)

data class CommentUiModel(
    val comment: CommentEntity,
    val author: UserEntity,
    val likeCount: Int,
    val isLikedByCurrentUser: Boolean
)

data class NotificationUiModel(
    val notification: NotificationEntity,
    val actor: UserEntity?,
    val postPreview: PostEntity?
)

data class ConversationSummary(
    val otherUser: UserEntity,
    val lastMessage: MessageEntity,
    val unreadCount: Int
)

data class UserProfileUiModel(
    val user: UserEntity,
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean,
    val isBlocked: Boolean
)

data class PlatformStats(
    val totalUsers: Int,
    val totalPosts: Int,
    val totalComments: Int,
    val totalReports: Int,
    val activeUsers: Int
)
