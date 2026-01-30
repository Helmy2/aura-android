package com.example.aura.shared.data.downloader

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoDownloaderTest {

    @Test
    fun downloadVideo_validUrl_enqueuesDownload() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val downloader = VideoDownloader(appContext)

        // Use a dummy URL that satisfies URI parsing
        val downloadId = downloader.downloadVideo("https://example.com/video.mp4", "test_video")

        assertNotEquals("Download ID should not be 0 or -1", 0L, downloadId)
        assertNotEquals("Download ID should not be -1", -1L, downloadId)
    }
}
