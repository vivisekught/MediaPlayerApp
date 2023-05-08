package com.example.mediaplayerapp.domain.use_cases

import com.example.mediaplayerapp.domain.entity.Media
import com.example.mediaplayerapp.domain.repository.MediaRepository

class AddMediaUseCase(private val repository: MediaRepository) {

    suspend operator fun invoke(media: Media) = repository.addMedia(media)
}