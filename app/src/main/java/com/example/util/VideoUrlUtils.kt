package com.example.util

/**
 * Utility for resolving video URLs, ensuring resilient playback across devices and networks.
 * Automatically migrates deprecated or forbidden sample video URLs to verified working streams.
 */
object VideoUrlUtils {
    const val DEFAULT_FALLBACK_VIDEO_URL =
        "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4"

    val LEGACY_URL_MAPPINGS = mapOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4" to
                "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4" to
                "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4" to
                "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4" to
                "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/person-bicycle-car-detection.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4" to
                "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/face-demographics-walking-and-pause.mp4"
    )

    fun resolvePlayableUrl(url: String?): String? {
        if (url.isNullOrBlank()) return url
        val exactMatch = LEGACY_URL_MAPPINGS[url]
        if (exactMatch != null) return exactMatch

        if (url.contains("commondatastorage.googleapis.com/gtv-videos-bucket")) {
            for ((legacy, replacement) in LEGACY_URL_MAPPINGS) {
                val filename = legacy.substringAfterLast("/")
                if (url.contains(filename)) {
                    return replacement
                }
            }
            return DEFAULT_FALLBACK_VIDEO_URL
        }
        return url
    }
}
