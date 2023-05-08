package com.example.mediaplayerapp.presentation.download_history

import androidx.recyclerview.widget.DiffUtil
import com.example.mediaplayerapp.domain.entity.Media

class DownloadHistoryDiffCallback: DiffUtil.ItemCallback<Media>() {
    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem == newItem
    }

}