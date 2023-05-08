package com.example.mediaplayerapp.domain.use_cases

import com.example.mediaplayerapp.domain.repository.MediaRepository
import com.example.mediaplayerapp.utils.DownloadMediaState

class UpdateMediaUriUseCase (private val repository: MediaRepository) {

    suspend operator fun invoke(mediaId: Long, uri: String?, state: DownloadMediaState) = repository.updateMediaUri(mediaId, uri, state)
}