package com.example.mediaplayerapp.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.mediaplayerapp.data.db.AppDatabase
import com.example.mediaplayerapp.data.db.mapper.Mapper
import com.example.mediaplayerapp.domain.entity.Media
import com.example.mediaplayerapp.domain.repository.MediaRepository
import com.example.mediaplayerapp.utils.DownloadMediaState

class MediaRepositoryImpl(application: Application): MediaRepository {

    private val mediaDao = AppDatabase.getInstance(application).mediaDao()
    private val mapper = Mapper()

    override suspend fun addMedia(media: Media) {
        mediaDao.addMedia(mapper.mapEntityToDbModel(media))
    }

    override suspend fun removeMedia(mediaId: Long) {
        mediaDao.deleteMedia(mediaId)
    }

    override suspend fun updateMediaUri(mediaId: Long, uri: String?, state: DownloadMediaState) {
        mediaDao.updateMediaUri(mediaId, uri, state)
    }

    override fun getMediaHistory(): LiveData<List<Media>> = MediatorLiveData<List<Media>>().apply {
        addSource(mediaDao.getMediaHistory()){
            value = mapper.mapDbModelListToEntity(it)
        }
    }
}