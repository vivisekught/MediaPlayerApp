package com.example.mediaplayerapp.presentation

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.URLUtil
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mediaplayerapp.data.repository.MediaRepositoryImpl
import com.example.mediaplayerapp.domain.entity.Media
import com.example.mediaplayerapp.utils.Constants
import com.example.mediaplayerapp.utils.DownloadMediaState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepositoryImpl = MediaRepositoryImpl(application)

    private var _videoUrl = MutableLiveData<String>()
    val videoUrl: LiveData<String>
        get() = _videoUrl

    private var _audioUrl = MutableLiveData<String>()
    val audioUrl: LiveData<String>
        get() = _audioUrl

    fun parseURL(url: String?) = !url.isNullOrBlank()

    fun getPath(context: Context, uri: Uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    _videoUrl.value = getDataColumn(
                        context, contentUri, selection,
                        selectionArgs
                    )
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    _audioUrl.value = getDataColumn(
                        context, contentUri, selection,
                        selectionArgs
                    )
                }

            } else null
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            uri.path
        } else null

    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri!!,
                projection,
                selection,
                selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex: Int = cursor
                    .getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private val downloadManager = application.getSystemService(DownloadManager::class.java)
    fun downloadVideo(url: String) {
        val videoName = URLUtil.guessFileName(url, null, Constants.VIDEO_MIME_TYPE)
        val request = DownloadManager.Request(url.toUri())
            .setMimeType(Constants.VIDEO_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(videoName)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, videoName)
        val mediaId = downloadManager.enqueue(request)
        viewModelScope.launch {
            mediaRepositoryImpl.addMedia(
                Media(
                    media_id = mediaId,
                    title = videoName,
                    url = url,
                    uri = null,
                    type = Constants.VIDEO_MIME_TYPE,
                    time = getCurrentTime(),
                    date = getCurrentDate(),
                    state = DownloadMediaState.Loading
                )
            )
        }
    }

    fun downloadAudio(url: String) {
        val audioName = URLUtil.guessFileName(url, null, Constants.AUDIO_MIME_TYPE)
        val request = DownloadManager.Request(url.toUri())
            .setMimeType(Constants.AUDIO_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(audioName)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, audioName)

        val mediaId = downloadManager.enqueue(request)
        viewModelScope.launch {
            mediaRepositoryImpl.addMedia(
                Media(
                    media_id = mediaId,
                    title = audioName,
                    url = url,
                    uri = null,
                    type = Constants.AUDIO_MIME_TYPE,
                    time = getCurrentTime(),
                    date = getCurrentDate(),
                    state = DownloadMediaState.Loading
                )
            )
        }
    }


    fun updateMediaUri(mediaId: Long, uri: String?, state: DownloadMediaState) {
        viewModelScope.launch {
            mediaRepositoryImpl.updateMediaUri(mediaId, uri, state)
        }
    }

    private fun getCurrentTime(): String {
        val time = SimpleDateFormat("hh:mm:ss")
        return time.format(Date())
    }


    private fun getCurrentDate(): String {
        val date = SimpleDateFormat("dd/M/yyyy")
        return date.format(Date())
    }
}