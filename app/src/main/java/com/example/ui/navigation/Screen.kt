package com.example.ui.navigation

sealed class Screen(val route: String) {
    // Auth flow
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")

    // Main bottom tabs
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Reels : Screen("reels")
    object CreatePost : Screen("create_post")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")

    // Sub screens
    object CreateReel : Screen("create_reel")
    data class ReelDetail(val startPostId: Long) : Screen("reel_detail/$startPostId")
    object Search : Screen("search")
    object Messages : Screen("messages")
    data class Chat(val otherUserId: Long) : Screen("chat/$otherUserId") {
        companion object {
            const val ROUTE_PATTERN = "chat/{userId}"
        }
    }
    data class UserProfile(val userId: Long) : Screen("user_profile/$userId") {
        companion object {
            const val ROUTE_PATTERN = "user_profile/{userId}"
        }
    }
    object EditProfile : Screen("edit_profile")
    data class FollowList(val userId: Long, val initialTab: Int = 0) : Screen("follow_list/$userId/$initialTab") {
        companion object {
            const val ROUTE_PATTERN = "follow_list/{userId}/{tab}"
        }
    }
    object Settings : Screen("settings")
    object SavedPosts : Screen("saved_posts")
    object BlockedUsers : Screen("blocked_users")
    object ReportHistory : Screen("report_history")
    object AdminDashboard : Screen("admin_dashboard")
}
