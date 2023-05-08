package com.example.mediaplayerapp.data.db.mapper

import com.example.mediaplayerapp.data.db.entity.MediaDbModel
import com.example.mediaplayerapp.domain.entity.Media

class Mapper {

    fun mapEntityToDbModel(media: Media) = MediaDbModel(
        id = media.id,
        media_id = media.media_id,
        title = media.title,
        url = media.url,
        uri = media.uri,
        type = media.type,
        time = media.time,
        date = media.date,
        state = media.state
    )

    fun mapDbModelToEntity(media: MediaDbModel) = Media(
        id = media.id,
        media_id = media.media_id,
        title = media.title,
        url = media.url,
        uri = media.uri,
        type = media.type,
        time = media.time,
        date = media.date,
        state = media.state

    )

    fun mapDbModelListToEntity(list: List<MediaDbModel>) = list.map {
        mapDbModelToEntity(it)
    }
}