package com.example.ui.screens.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommentUiModel
import com.example.data.model.PostUiModel
import com.example.ui.components.*
import com.example.ui.theme.PrimaryIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    currentUserId: Long,
    posts: List<PostUiModel>,
    isLoading: Boolean,
    isFollowingOnly: Boolean,
    unreadMessagesCount: Int,
    onTabSelected: (followingOnly: Boolean) -> Unit,
    onLikeClick: (postId: Long) -> Unit,
    onSaveClick: (postId: Long) -> Unit,
    onDeletePostClick: (postId: Long) -> Unit,
    onReportPostClick: (postId: Long, reason: String, desc: String) -> Unit,
    onBlockUserClick: (userId: Long) -> Unit,
    onUserClick: (userId: Long) -> Unit,
    onSearchClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onCreatePostClick: () -> Unit,
    // Comments
    activePostComments: List<CommentUiModel>,
    onLoadCommentsForPost: (postId: Long) -> Unit,
    onAddComment: (postId: Long, text: String) -> Unit,
    onDeleteComment: (commentId: Long) -> Unit,
    onToggleCommentLike: (commentId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPostForComments by remember { mutableStateOf<Long?>(null) }
    var reportingPostId by remember { mutableStateOf<Long?>(null) }
    var deleteConfirmPostId by remember { mutableStateOf<Long?>(null) }
    var blockConfirmUserId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ConnectUpLogo(size = 30.dp)
                },
                actions = {
                    IconButton(
                        onClick = onCreatePostClick,
                        modifier = Modifier.testTag("feed_create_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddBox,
                            contentDescription = "Create Post",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("feed_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(
                        onClick = onMessagesClick,
                        modifier = Modifier.testTag("feed_messages_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadMessagesCount > 0) {
                                    Badge { Text("$unreadMessagesCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = "Messages",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Feed Feed Tab Selector: "For You" vs "Following"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isFollowingOnly,
                    onClick = { onTabSelected(false) },
                    label = { Text("For You", fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryIndigo,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("feed_tab_for_you")
                )

                FilterChip(
                    selected = isFollowingOnly,
                    onClick = { onTabSelected(true) },
                    label = { Text("Following", fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryIndigo,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("feed_tab_following")
                )
            }

            if (isLoading && posts.isEmpty()) {
                // Skeleton Loader
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(3) {
                        SkeletonPostCard()
                    }
                }
            } else if (posts.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFollowingOnly) {
                        EmptyStateView(
                            icon = Icons.Outlined.People,
                            title = "No posts from following",
                            description = "Follow more creators or switch to 'For You' to discover what's happening!",
                            actionButtonText = "Explore Creators",
                            onActionClick = { onTabSelected(false) }
                        )
                    } else {
                        EmptyStateView(
                            icon = Icons.Outlined.PostAdd,
                            title = "Feed is quiet",
                            description = "Be the first to share an update, photo, or thought with the community!",
                            actionButtonText = "Create a Post",
                            onActionClick = onCreatePostClick
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("feed_posts_list"),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(posts, key = { it.post.id }) { postUi ->
                        PostCard(
                            postUi = postUi,
                            currentUserId = currentUserId,
                            onLikeClick = { onLikeClick(postUi.post.id) },
                            onCommentClick = {
                                selectedPostForComments = postUi.post.id
                                onLoadCommentsForPost(postUi.post.id)
                            },
                            onSaveClick = { onSaveClick(postUi.post.id) },
                            onUserClick = onUserClick,
                            onDeleteClick = { deleteConfirmPostId = postUi.post.id },
                            onReportClick = { reportingPostId = postUi.post.id },
                            onBlockUserClick = { blockConfirmUserId = postUi.author.id }
                        )
                    }
                }
            }
        }
    }

    // Comments Bottom Sheet
    selectedPostForComments?.let { postId ->
        CommentsBottomSheet(
            postId = postId,
            currentUserId = currentUserId,
            comments = activePostComments,
            onDismiss = { selectedPostForComments = null },
            onAddComment = { text -> onAddComment(postId, text) },
            onDeleteComment = onDeleteComment,
            onToggleCommentLike = onToggleCommentLike,
            onUserClick = { userId ->
                selectedPostForComments = null
                onUserClick(userId)
            }
        )
    }

    // Report Dialog
    reportingPostId?.let { postId ->
        ReportDialog(
            targetName = "Post",
            onDismiss = { reportingPostId = null },
            onSubmitReport = { reason, desc ->
                onReportPostClick(postId, reason, desc)
                reportingPostId = null
            }
        )
    }

    // Delete Confirmation Dialog
    deleteConfirmPostId?.let { postId ->
        AlertDialog(
            onDismissRequest = { deleteConfirmPostId = null },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to permanently delete this post? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePostClick(postId)
                        deleteConfirmPostId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmPostId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Block User Confirmation Dialog
    blockConfirmUserId?.let { userId ->
        AlertDialog(
            onDismissRequest = { blockConfirmUserId = null },
            title = { Text("Block User") },
            text = { Text("Are you sure you want to block this user? You will no longer see each other's posts or messages.") },
            confirmButton = {
                Button(
                    onClick = {
                        onBlockUserClick(userId)
                        blockConfirmUserId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { blockConfirmUserId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
