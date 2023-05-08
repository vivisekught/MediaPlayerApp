package com.example.mediaplayerapp.domain.use_cases

import com.example.mediaplayerapp.domain.entity.Media
import com.example.mediaplayerapp.domain.repository.MediaRepository

class RemoveMediaUseCase (private val repository: MediaRepository) {

    suspend operator fun invoke(mediaId: Long) = repository.removeMedia(mediaId)
}