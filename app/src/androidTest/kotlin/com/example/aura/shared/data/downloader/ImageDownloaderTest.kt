package com.example.aura.shared.data.downloader

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageDownloaderTest {

    @Test
    fun downloadImage_invalidUrl_returnsFalse() = runTest {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val downloader = ImageDownloader(appContext)

        val result = downloader.downloadImage("http://invalid.url/image.jpg", "test_image")
        
        assertFalse("Download should fail for invalid URL", result)
    }
}
