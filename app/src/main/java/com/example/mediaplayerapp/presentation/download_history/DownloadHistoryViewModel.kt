package com.example.mediaplayerapp.presentation.download_history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mediaplayerapp.data.repository.MediaRepositoryImpl

class DownloadHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepositoryImpl = MediaRepositoryImpl(application)

    val mediaHistory = mediaRepositoryImpl.getMediaHistory()
}