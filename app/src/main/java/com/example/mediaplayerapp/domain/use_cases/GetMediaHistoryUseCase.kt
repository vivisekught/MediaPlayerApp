package com.example.mediaplayerapp.domain.use_cases

import com.example.mediaplayerapp.domain.repository.MediaRepository

class GetMediaHistoryUseCase(private val repository: MediaRepository) {

    operator fun invoke() = repository.getMediaHistory()
}