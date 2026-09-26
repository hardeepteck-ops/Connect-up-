package com.example.data.seed

import com.example.data.dao.ConnectUpDao
import com.example.data.model.*
import com.example.util.PasswordHasher

object SeedData {

    suspend fun populateInitialData(dao: ConnectUpDao) {
        // Only seed if empty
        val existingCount = dao.getTotalUsersCount()
        if (existingCount > 0) return

        val now = System.currentTimeMillis()
        val hour = 3600_000L
        val day = 86400_000L

        // Default password hashes
        val defaultHash = PasswordHasher.hash("Password123!")
        val adminHash = PasswordHasher.hash("AdminPassword123!")

        // 1. Users
        val adminUser = UserEntity(
            name = "ConnectUp Admin",
            username = "connectup_admin",
            email = "admin@connectup.com",
            passwordHash = adminHash,
            profileImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
            coverImage = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
            bio = "Official ConnectUp community lead & moderation desk. Connect. Share. Discover.",
            website = "https://connectup.social",
            dateOfBirth = "1995-04-12",
            isAdmin = true,
            createdAt = now - 30 * day
        )
        val adminId = dao.insertUser(adminUser)

        val demoUser = UserEntity(
            name = "Jordan Lee",
            username = "jordanlee",
            email = "jordan@example.com",
            passwordHash = defaultHash,
            profileImage = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400",
            coverImage = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800",
            bio = "Building experiences with Compose & Kotlin. Coffee lover ☕ Tech explorer 🚀",
            website = "https://jordanlee.dev",
            dateOfBirth = "1998-08-20",
            isAdmin = false,
            createdAt = now - 20 * day
        )
        val demoUserId = dao.insertUser(demoUser)

        val user1 = UserEntity(
            name = "Alex Rivera",
            username = "alexrivera",
            email = "alex@example.com",
            passwordHash = defaultHash,
            profileImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
            coverImage = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            bio = "Architectural photographer based in San Francisco. Chasing clean lines & golden hours 📷",
            website = "https://alexrivera.photo",
            dateOfBirth = "1994-03-15",
            createdAt = now - 15 * day
        )
        val user1Id = dao.insertUser(user1)

        val user2 = UserEntity(
            name = "Elena Chen",
            username = "elenachen",
            email = "elena@example.com",
            passwordHash = defaultHash,
            profileImage = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
            coverImage = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=800",
            bio = "Product designer & typography nerd. Simplicity is the ultimate sophistication 🎨",
            website = "https://elenachen.design",
            dateOfBirth = "1997-11-04",
            createdAt = now - 12 * day
        )
        val user2Id = dao.insertUser(user2)

        val user3 = UserEntity(
            name = "Marcus Sterling",
            username = "marcus_s",
            email = "marcus@example.com",
            passwordHash = defaultHash,
            profileImage = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
            coverImage = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800",
            bio = "Sound design, modular synths & vinyl collector 🎧",
            website = "https://sterlingwaves.fm",
            dateOfBirth = "1992-06-25",
            createdAt = now - 10 * day
        )
        val user3Id = dao.insertUser(user3)

        val user4 = UserEntity(
            name = "Sophia Patel",
            username = "sophiatech",
            email = "sophia@example.com",
            passwordHash = defaultHash,
            profileImage = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
            coverImage = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800",
            bio = "Open-source contributor & AI engineer. Exploring multimodal computing ✨",
            website = "https://github.com/sophiap",
            dateOfBirth = "1999-01-19",
            createdAt = now - 8 * day
        )
        val user4Id = dao.insertUser(user4)

        // 2. Follows
        // Jordan follows Alex, Elena, Sophia
        dao.insertFollow(FollowEntity(followerId = demoUserId, followingId = user1Id, createdAt = now - 5 * day))
        dao.insertFollow(FollowEntity(followerId = demoUserId, followingId = user2Id, createdAt = now - 4 * day))
        dao.insertFollow(FollowEntity(followerId = demoUserId, followingId = user4Id, createdAt = now - 3 * day))

        // Elena and Alex follow Jordan back
        dao.insertFollow(FollowEntity(followerId = user1Id, followingId = demoUserId, createdAt = now - 4 * day))
        dao.insertFollow(FollowEntity(followerId = user2Id, followingId = demoUserId, createdAt = now - 3 * day))
        dao.insertFollow(FollowEntity(followerId = user3Id, followingId = demoUserId, createdAt = now - 2 * day))

        // Other follows
        dao.insertFollow(FollowEntity(followerId = user1Id, followingId = user2Id, createdAt = now - 6 * day))
        dao.insertFollow(FollowEntity(followerId = user2Id, followingId = user3Id, createdAt = now - 6 * day))
        dao.insertFollow(FollowEntity(followerId = user4Id, followingId = user1Id, createdAt = now - 5 * day))

        // 3. Posts
        val post1 = PostEntity(
            userId = user1Id,
            caption = "Early morning fog rolling through the redwood canopy. The light shafts here never fail to inspire. #photography #nature #morningwalk #sanfrancisco",
            mediaUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?w=900",
            mediaType = "IMAGE",
            hashtags = "photography,nature,morningwalk,sanfrancisco",
            createdAt = now - 2 * hour
        )
        val post1Id = dao.insertPost(post1)

        val post2 = PostEntity(
            userId = user2Id,
            caption = "Fresh prototype drops today! Focusing on high-contrast accessibility and rhythmic typography grids. What do you think of this layout? 📐✨ #design #uiux #mobile #minimalism",
            mediaUrl = "https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=900",
            mediaType = "IMAGE",
            hashtags = "design,uiux,mobile,minimalism",
            createdAt = now - 5 * hour
        )
        val post2Id = dao.insertPost(post2)

        val post3 = PostEntity(
            userId = user4Id,
            caption = "Just published our open-source benchmark for on-device neural model inference. Seeing 35% latency drops on modern mobile chipsets! 🚀 #android #tech #ai #opensource",
            mediaUrl = null,
            mediaType = null,
            hashtags = "android,tech,ai,opensource",
            createdAt = now - 12 * hour
        )
        val post3Id = dao.insertPost(post3)

        val post4 = PostEntity(
            userId = user3Id,
            caption = "Late night studio sessions with the Prophet-6 analog synth. Captured some warm ambient textures for the next EP. 🎹🔊 #musicproduction #synths #ambient",
            mediaUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=900",
            mediaType = "IMAGE",
            hashtags = "musicproduction,synths,ambient",
            createdAt = now - 24 * hour
        )
        val post4Id = dao.insertPost(post4)

        val post5 = PostEntity(
            userId = demoUserId,
            caption = "Welcome to ConnectUp! Excited to share work, connect with incredible creators, and discover new ideas. Let's build something awesome together! 🌟 #connectup #welcome #community",
            mediaUrl = "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=900",
            mediaType = "IMAGE",
            hashtags = "connectup,welcome,community",
            createdAt = now - 36 * hour
        )
        val post5Id = dao.insertPost(post5)

        // Reels (Short-form portrait video feed like Instagram)
        val reel1 = PostEntity(
            userId = user1Id,
            caption = "Sunset hyperlapse over the Pacific coastline. Still processing the colors from tonight! 🌉✨ Follow for more travel captures. #reels #cinematic #sunset #nature",
            mediaUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4",
            mediaType = "REEL",
            hashtags = "reels,cinematic,sunset,nature",
            isReel = true,
            soundTitle = "Golden Hour Chill • Alex Rivera",
            viewsCount = 14850,
            createdAt = now - 1 * hour
        )
        val reel1Id = dao.insertPost(reel1)

        val reel2 = PostEntity(
            userId = user2Id,
            caption = "Quick UI micro-interaction breakdown! Notice how spring damping creates tangible physical feedback. What do you think? 📱✨ #design #uiux #mobile #reels",
            mediaUrl = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4",
            mediaType = "REEL",
            hashtags = "design,uiux,mobile,reels",
            isReel = true,
            soundTitle = "Lofi Study Session • Elena Chen",
            viewsCount = 38920,
            createdAt = now - 3 * hour
        )
        val reel2Id = dao.insertPost(reel2)

        val reel3 = PostEntity(
            userId = user3Id,
            caption = "Building a polyrhythmic patch on modular synthesizers. Headphones recommended for sub-bass textures! 🎧🔊 #synthwave #reels #modular #sounddesign",
            mediaUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4",
            mediaType = "REEL",
            hashtags = "synthwave,reels,modular,sounddesign",
            isReel = true,
            soundTitle = "Analog Pulse in D Minor • Marcus",
            viewsCount = 22400,
            createdAt = now - 7 * hour
        )
        val reel3Id = dao.insertPost(reel3)

        val reel4 = PostEntity(
            userId = user4Id,
            caption = "Real-time edge neural inference running at 60fps on mobile hardware! Code repo is live on GitHub 🚀💻 #ai #techtok #coding #reels",
            mediaUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/person-bicycle-car-detection.mp4",
            mediaType = "REEL",
            hashtags = "ai,techtok,coding,reels",
            isReel = true,
            soundTitle = "Cybernetic Flow • Sophia Patel",
            viewsCount = 57300,
            createdAt = now - 14 * hour
        )
        val reel4Id = dao.insertPost(reel4)

        // 4. Likes
        dao.insertLike(LikeEntity(userId = demoUserId, postId = post1Id, createdAt = now - 1 * hour))
        dao.insertLike(LikeEntity(userId = user2Id, postId = post1Id, createdAt = now - 1 * hour))
        dao.insertLike(LikeEntity(userId = user3Id, postId = post1Id, createdAt = now - 30 * 60_000L))

        // Likes on reels
        dao.insertLike(LikeEntity(userId = demoUserId, postId = reel1Id, createdAt = now - 45 * 60_000L))
        dao.insertLike(LikeEntity(userId = user2Id, postId = reel1Id, createdAt = now - 40 * 60_000L))
        dao.insertLike(LikeEntity(userId = user3Id, postId = reel1Id, createdAt = now - 35 * 60_000L))
        dao.insertLike(LikeEntity(userId = user4Id, postId = reel1Id, createdAt = now - 30 * 60_000L))

        dao.insertLike(LikeEntity(userId = demoUserId, postId = reel2Id, createdAt = now - 2 * hour))
        dao.insertLike(LikeEntity(userId = user1Id, postId = reel2Id, createdAt = now - 2 * hour))
        dao.insertLike(LikeEntity(userId = user4Id, postId = reel2Id, createdAt = now - 1 * hour))

        dao.insertLike(LikeEntity(userId = user1Id, postId = reel3Id, createdAt = now - 5 * hour))
        dao.insertLike(LikeEntity(userId = user2Id, postId = reel3Id, createdAt = now - 4 * hour))
        dao.insertLike(LikeEntity(userId = demoUserId, postId = reel4Id, createdAt = now - 10 * hour))

        dao.insertLike(LikeEntity(userId = demoUserId, postId = post2Id, createdAt = now - 3 * hour))
        dao.insertLike(LikeEntity(userId = user1Id, postId = post2Id, createdAt = now - 2 * hour))
        dao.insertLike(LikeEntity(userId = user4Id, postId = post2Id, createdAt = now - 2 * hour))

        dao.insertLike(LikeEntity(userId = user1Id, postId = post3Id, createdAt = now - 8 * hour))
        dao.insertLike(LikeEntity(userId = user2Id, postId = post3Id, createdAt = now - 6 * hour))

        dao.insertLike(LikeEntity(userId = user1Id, postId = post5Id, createdAt = now - 20 * hour))
        dao.insertLike(LikeEntity(userId = user2Id, postId = post5Id, createdAt = now - 18 * hour))
        dao.insertLike(LikeEntity(userId = user3Id, postId = post5Id, createdAt = now - 16 * hour))
        dao.insertLike(LikeEntity(userId = user4Id, postId = post5Id, createdAt = now - 14 * hour))

        // 5. Comments
        val comment1 = CommentEntity(
            userId = user2Id,
            postId = post1Id,
            text = "That lighting is unreal Alex! What focal length did you shoot this on?",
            createdAt = now - 90 * 60_000L
        )
        val comment1Id = dao.insertComment(comment1)

        val comment2 = CommentEntity(
            userId = user1Id,
            postId = post1Id,
            text = "Thanks Elena! Shot on a 35mm prime at f/2.8 right as the sun hit the mist.",
            createdAt = now - 60 * 60_000L
        )
        dao.insertComment(comment2)

        val comment3 = CommentEntity(
            userId = demoUserId,
            postId = post2Id,
            text = "Love the clean whitespace and hierarchy! The active pill colors are spot-on.",
            createdAt = now - 4 * hour
        )
        dao.insertComment(comment3)

        val comment4 = CommentEntity(
            userId = user2Id,
            postId = post2Id,
            text = "Thank you Jordan! Appreciate the feedback. Will share the design system components next.",
            createdAt = now - 3 * hour
        )
        dao.insertComment(comment4)

        // Comments on reels
        dao.insertComment(
            CommentEntity(
                userId = demoUserId,
                postId = reel1Id,
                text = "Incredible capture Alex! Those colors look unreal 🌅",
                createdAt = now - 30 * 60_000L
            )
        )
        dao.insertComment(
            CommentEntity(
                userId = user3Id,
                postId = reel1Id,
                text = "The audio track pairs with this so well 👌",
                createdAt = now - 20 * 60_000L
            )
        )
        dao.insertComment(
            CommentEntity(
                userId = demoUserId,
                postId = reel2Id,
                text = "That spring curve is super smooth! Jetpack Compose animateFloatAsState?",
                createdAt = now - 1 * hour
            )
        )

        // Comment Likes
        dao.insertCommentLike(CommentLikeEntity(userId = user1Id, commentId = comment1Id, createdAt = now - 40 * 60_000L))

        // 6. Saved Posts (Jordan saved post1 and post2)
        dao.insertSavedPost(SavedPostEntity(userId = demoUserId, postId = post1Id, createdAt = now - 1 * hour))
        dao.insertSavedPost(SavedPostEntity(userId = demoUserId, postId = post2Id, createdAt = now - 3 * hour))

        // 7. Messages between Jordan and Elena Chen
        dao.insertMessage(
            MessageEntity(
                senderId = user2Id,
                receiverId = demoUserId,
                text = "Hey Jordan! Checked out your latest post, welcome to ConnectUp!",
                createdAt = now - 2 * hour,
                seenAt = now - 1 * hour
            )
        )
        dao.insertMessage(
            MessageEntity(
                senderId = demoUserId,
                receiverId = user2Id,
                text = "Hey Elena! Thanks so much, loving the vibe here. Your UI design work looks incredible.",
                createdAt = now - 1 * hour,
                seenAt = now - 45 * 60_000L
            )
        )
        dao.insertMessage(
            MessageEntity(
                senderId = user2Id,
                receiverId = demoUserId,
                text = "Appreciate it! Let me know if you want to collaborate on any mobile prototypes.",
                createdAt = now - 30 * 60_000L,
                seenAt = null
            )
        )

        // Message between Jordan and Alex
        dao.insertMessage(
            MessageEntity(
                senderId = user1Id,
                receiverId = demoUserId,
                text = "Hey! Let me know if you visit SF soon, we do photo walks on weekends.",
                createdAt = now - 4 * hour,
                seenAt = now - 3 * hour
            )
        )

        // 8. Notifications for Jordan
        dao.insertNotification(
            NotificationEntity(
                userId = demoUserId,
                actorId = user1Id,
                type = "POST_LIKE",
                postId = post5Id,
                message = "liked your post: \"Welcome to ConnectUp!\"",
                isRead = false,
                createdAt = now - 20 * hour
            )
        )
        dao.insertNotification(
            NotificationEntity(
                userId = demoUserId,
                actorId = user2Id,
                type = "FOLLOW",
                postId = null,
                message = "started following you.",
                isRead = false,
                createdAt = now - 3 * day
            )
        )
        dao.insertNotification(
            NotificationEntity(
                userId = demoUserId,
                actorId = user3Id,
                type = "POST_LIKE",
                postId = post5Id,
                message = "liked your post.",
                isRead = true,
                createdAt = now - 16 * hour
            )
        )

        // 9. Initial App Session - Start with Jordan logged in for smooth first-time experience,
        // while also providing instantaneous Logout / Switch / Create Account / Login functionality.
        dao.setAppSession(
            AppSessionEntity(
                id = 1,
                loggedInUserId = demoUserId,
                rememberMe = true,
                themeMode = "SYSTEM",
                pushLikes = true,
                pushComments = true,
                pushFollows = true,
                pushMessages = true
            )
        )
    }
}
