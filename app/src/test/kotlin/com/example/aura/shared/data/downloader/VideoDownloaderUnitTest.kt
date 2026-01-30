package com.example.aura.shared.data.downloader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class VideoDownloaderUnitTest {

    private lateinit var context: Context
    private lateinit var downloadManager: DownloadManager
    private lateinit var requestFactory: DownloadRequestFactory
    private lateinit var request: DownloadManager.Request
    private lateinit var downloader: VideoDownloader
    private lateinit var uri: Uri

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        downloadManager = mockk(relaxed = true)
        requestFactory = mockk()
        request = mockk(relaxed = true)
        uri = mockk()

        // Mock static Uri.parse
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns uri

        every { context.getSystemService(Context.DOWNLOAD_SERVICE) } returns downloadManager
        every { requestFactory.createRequest(any()) } returns request
        
        // Mock request chaining
        every { request.setTitle(any()) } returns request
        every { request.setDescription(any()) } returns request
        every { request.setNotificationVisibility(any()) } returns request
        every { request.setDestinationInExternalPublicDir(any(), any()) } returns request
        every { request.setAllowedOverMetered(any()) } returns request
        every { request.setAllowedOverRoaming(any()) } returns request

        downloader = VideoDownloader(context, requestFactory)
    }

    @Test
    fun `downloadVideo should enqueue request with correct parameters`() {
        val url = "http://example.com/video.mp4"
        val fileName = "test_video"
        val expectedId = 123L
        
        every { downloadManager.enqueue(request) } returns expectedId

        val result = downloader.downloadVideo(url, fileName)

        // Verify result
        assertEquals(expectedId, result)

        // Verify request configuration
        verify { requestFactory.createRequest(uri) }
        verify { request.setTitle("Downloading Video") }
        verify { request.setDestinationInExternalPublicDir(any(), "Aura/$fileName.mp4") }
        
        // Verify enqueue
        verify { downloadManager.enqueue(request) }
    }
}
