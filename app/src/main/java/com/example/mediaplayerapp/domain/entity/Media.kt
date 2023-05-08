package com.example.mediaplayerapp.domain.entity

import com.example.mediaplayerapp.utils.DownloadMediaState

data class Media(
    val media_id: Long,
    val title: String,
    val url: String,
    val uri: String?,
    val type: String,
    val time: String,
    val date: String,
    val state: DownloadMediaState,
    var id: Int = UNDEFINED_ID
) {

    companion object {

        const val UNDEFINED_ID = 0
    }
}
