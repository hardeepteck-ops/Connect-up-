package com.example.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PostUiModel
import com.example.data.model.SearchHistoryEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.PrimaryIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    currentUserId: Long,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onPerformSearch: (String) -> Unit,
    searchHistory: List<SearchHistoryEntity>,
    onClearHistory: () -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    userResults: List<UserEntity>,
    postResults: List<PostUiModel>,
    isSearching: Boolean,
    onUserClick: (Long) -> Unit,
    onFollowClick: (Long) -> Unit,
    onLikePostClick: (Long) -> Unit,
    onSavePostClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(0) } // 0: All, 1: Accounts, 2: Posts

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("search_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            onQueryChange(it)
                            if (it.isNotBlank()) onPerformSearch(it)
                        },
                        placeholder = { Text("Search users, posts, #tags...", fontSize = 14.sp) },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("search_text_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = PrimaryIndigo
                        )
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
            // Filter tabs
            if (searchQuery.isNotBlank()) {
                TabRow(
                    selectedTabIndex = selectedFilter,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = PrimaryIndigo
                ) {
                    Tab(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        text = { Text("All") }
                    )
                    Tab(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        text = { Text("Accounts (${userResults.size})") }
                    )
                    Tab(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        text = { Text("Posts (${postResults.size})") }
                    )
                }
            }

            if (isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // If query is empty -> show search history
            if (searchQuery.isBlank()) {
                if (searchHistory.isNotEmpty()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(
                                onClick = onClearHistory,
                                modifier = Modifier.testTag("clear_search_history_button")
                            ) {
                                Text("Clear all", color = PrimaryIndigo, fontSize = 12.sp)
                            }
                        }

                        LazyColumn {
                            items(searchHistory, key = { it.id }) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onQueryChange(item.query)
                                            onPerformSearch(item.query)
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.query,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onDeleteHistoryItem(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove item",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateView(
                            icon = Icons.Outlined.Search,
                            title = "Search ConnectUp",
                            description = "Find creators, posts, hashtags, and ideas from around the platform."
                        )
                    }
                }
            } else {
                // Search Results
                val hasNoResults = userResults.isEmpty() && postResults.isEmpty()

                if (hasNoResults && !isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateView(
                            icon = Icons.Outlined.Search,
                            title = "No results found",
                            description = "We couldn't find anything matching \"$searchQuery\". Try checking the spelling or searching a different term."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_results_list"),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // User accounts results
                        if (selectedFilter == 0 || selectedFilter == 1) {
                            if (userResults.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "People",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                items(userResults, key = { "user_${it.id}" }) { user ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onUserClick(user.id) }
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(14.dp),
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
                                                if (user.bio.isNotBlank()) {
                                                    Text(
                                                        text = user.bio,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                            if (user.id != currentUserId) {
                                                Button(
                                                    onClick = { onFollowClick(user.id) },
                                                    shape = RoundedCornerShape(10.dp),
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

                        // Posts results
                        if (selectedFilter == 0 || selectedFilter == 2) {
                            if (postResults.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Posts",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                    )
                                }
                                items(postResults, key = { "post_${it.post.id}" }) { postUi ->
                                    PostCard(
                                        postUi = postUi,
                                        currentUserId = currentUserId,
                                        onLikeClick = { onLikePostClick(postUi.post.id) },
                                        onCommentClick = { /* No-op or navigate */ },
                                        onSaveClick = { onSavePostClick(postUi.post.id) },
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
        }
    }
}
