package com.example.mediaplayerapp.domain.repository

import androidx.lifecycle.LiveData
import com.example.mediaplayerapp.domain.entity.Media
import com.example.mediaplayerapp.utils.DownloadMediaState

interface MediaRepository {

    suspend fun addMedia(media: Media)

    suspend fun removeMedia(mediaId: Long)

    suspend fun updateMediaUri(mediaId: Long, uri: String?, state: DownloadMediaState)

    fun getMediaHistory(): LiveData<List<Media>>
}