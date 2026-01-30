package com.example.aura.shared.data.downloader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri

// Factory interface to allow mocking Request creation
interface DownloadRequestFactory {
    fun createRequest(uri: Uri): DownloadManager.Request
}

class DefaultDownloadRequestFactory : DownloadRequestFactory {
    override fun createRequest(uri: Uri): DownloadManager.Request {
        return DownloadManager.Request(uri)
    }
}

class VideoDownloader(
    private val context: Context,
    private val requestFactory: DownloadRequestFactory = DefaultDownloadRequestFactory() 
) {

    fun downloadVideo(url: String, fileName: String): Long {
        val request = requestFactory.createRequest(url.toUri())
            .setTitle("Downloading Video")
            .setDescription("Downloading video from Aura")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "Aura/$fileName.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }
}
