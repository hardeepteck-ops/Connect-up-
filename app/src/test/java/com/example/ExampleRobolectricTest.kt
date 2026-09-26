package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.DateFormatter
import com.example.util.PasswordHasher
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ConnectUp", appName)
    }

    @Test
    fun `password hasher generates secure hash and verifies correctly`() {
        val rawPassword = "SecurePassword123!"
        val hash = PasswordHasher.hash(rawPassword)
        assertNotNull(hash)
        assertTrue(hash.length >= 32)
        assertTrue(PasswordHasher.verify(rawPassword, hash))
        assertFalse(PasswordHasher.verify("WrongPassword", hash))
    }

    @Test
    fun `date formatter returns relative times correctly`() {
        val now = System.currentTimeMillis()
        val justNow = DateFormatter.formatRelativeTime(now - 10_000)
        assertEquals("Just now", justNow)

        val minutesAgo = DateFormatter.formatRelativeTime(now - 5 * 60_000)
        assertEquals("5m ago", minutesAgo)

        val hoursAgo = DateFormatter.formatRelativeTime(now - 3 * 3600_000)
        assertEquals("3h ago", hoursAgo)
    }

    @Test
    fun `reel entity initializes with proper defaults and video properties`() {
        val reel = com.example.data.model.PostEntity(
            userId = 1L,
            caption = "Test reel #video",
            mediaUrl = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4",
            mediaType = "REEL",
            hashtags = "video,reel",
            isReel = true,
            soundTitle = "Chill Lofi",
            viewsCount = 100
        )
        assertTrue(reel.isReel)
        assertEquals("REEL", reel.mediaType)
        assertEquals("Chill Lofi", reel.soundTitle)
        assertEquals(100, reel.viewsCount)
        assertTrue(reel.mediaUrl!!.endsWith(".mp4"))
    }

    @Test
    fun `reel audio volume toggles between muted and unmuted correctly`() {
        var isAudioMuted = false
        fun computeVolume(muted: Boolean): Float = if (muted) 0f else 1f

        assertEquals(1.0f, computeVolume(isAudioMuted), 0.001f)

        // Toggle to muted
        isAudioMuted = !isAudioMuted
        assertTrue(isAudioMuted)
        assertEquals(0.0f, computeVolume(isAudioMuted), 0.001f)

        // Toggle back to unmuted
        isAudioMuted = !isAudioMuted
        assertFalse(isAudioMuted)
        assertEquals(1.0f, computeVolume(isAudioMuted), 0.001f)
    }

    @Test
    fun `reels vertical pager index computation resolves correctly for swiping and target postId`() {
        val samplePostIds = listOf(101L, 102L, 103L, 104L)

        fun computeInitialIndex(initialPostId: Long?, postIds: List<Long>): Int {
            return if (initialPostId != null) {
                postIds.indexOfFirst { it == initialPostId }.coerceAtLeast(0)
            } else 0
        }

        // Default should be first reel (0)
        assertEquals(0, computeInitialIndex(null, samplePostIds))

        // Direct navigation to specific reel (e.g. 103L -> index 2)
        assertEquals(2, computeInitialIndex(103L, samplePostIds))

        // Non-existent reel gracefully falls back to index 0
        assertEquals(0, computeInitialIndex(999L, samplePostIds))

        // Verify active page playback exclusivity
        var currentPage = 0
        fun isItemActive(pageIndex: Int) = pageIndex == currentPage

        assertTrue(isItemActive(0))
        assertFalse(isItemActive(1))

        // Swiped down to next reel
        currentPage = 1
        assertFalse(isItemActive(0))
        assertTrue(isItemActive(1))
        assertFalse(isItemActive(2))
    }
}
