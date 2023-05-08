package com.example.mediaplayerapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mediaplayerapp.domain.entity.Media
import com.example.mediaplayerapp.utils.DownloadMediaState

@Entity(tableName = "media_download_history")
data class MediaDbModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val media_id: Long,
    val title: String,
    val url: String,
    val uri: String?,
    val type: String,
    val time: String,
    val date: String,
    val state: DownloadMediaState,
    )
