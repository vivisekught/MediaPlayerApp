package com.example.mediaplayerapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediaplayerapp.data.db.entity.MediaDbModel
import com.example.mediaplayerapp.utils.DownloadMediaState

@Dao
interface MediaDao {

    @Query("SELECT * FROM media_download_history ORDER BY time DESC")
    fun getMediaHistory(): LiveData<List<MediaDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMedia(mediaDbModel: MediaDbModel)

    @Query("DELETE FROM media_download_history WHERE id=:mediaId")
    suspend fun deleteMedia(mediaId: Long)

    @Query("UPDATE media_download_history SET uri=:uri, state=:state WHERE media_id=:mediaId")
    suspend fun updateMediaUri(mediaId: Long, uri: String?, state: DownloadMediaState)
}