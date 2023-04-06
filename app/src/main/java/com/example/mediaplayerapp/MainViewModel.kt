package com.example.mediaplayerapp

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private var _uri = MutableLiveData<String?>()
    val uri: MutableLiveData<String?>
        get() = _uri

    private var _downloadState = MutableLiveData<Int?>()
    val downloadState: MutableLiveData<Int?>
        get() = _downloadState


    fun parseURL(url: String?) = !url.isNullOrBlank()


    fun getPath(context: Context, uri: Uri) {
        // DocumentProvider
        _uri.value = if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                getDataColumn(
                    context, contentUri, selection,
                    selectionArgs
                )
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
}