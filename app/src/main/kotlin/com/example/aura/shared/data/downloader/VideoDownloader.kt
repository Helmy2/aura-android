package com.example.aura.shared.data.downloader

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

class VideoDownloader(private val context: Context) {

    fun downloadVideo(url: String, fileName: String): Long {
        val request = DownloadManager.Request(url.toUri())
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
