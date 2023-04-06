package com.example.mediaplayerapp

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.net.toUri

class Downloader(
    context: Context,
) {

   private val downloadManager = context.getSystemService(DownloadManager::class.java)
    fun downloadVideo(url: String): Long {
        val videoName = URLUtil.guessFileName(url, null, VIDEO_MIME_TYPE)
        val request = DownloadManager.Request(url.toUri())
            .setMimeType(VIDEO_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(videoName)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoName)

        return downloadManager.enqueue(request)
    }

    companion object {
        const val VIDEO_MIME_TYPE = "video/*"
        const val ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE"
        const val DOWNLOAD_DEFAULT_VALUE = -1L

    }

}