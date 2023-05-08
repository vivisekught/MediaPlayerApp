package com.example.mediaplayerapp.presentation.download_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.databinding.LayoutMediaHistoryBinding
import com.example.mediaplayerapp.domain.entity.Media
import com.example.mediaplayerapp.utils.Constants
import com.example.mediaplayerapp.utils.DownloadMediaState


class DownloadHistoryAdapter :
    ListAdapter<Media, DownloadHistoryAdapter.DownloadHistoryViewHolder>(
        DownloadHistoryDiffCallback()
    ) {

    var onPlayClickListener: ((Media) -> Unit)? = null

    class DownloadHistoryViewHolder(
        val binding: LayoutMediaHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadHistoryViewHolder {
        val binding =
            LayoutMediaHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DownloadHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadHistoryViewHolder, position: Int) {
        val media = getItem(position)
        val binding = holder.binding
        with(binding) {
            mediaTitle.text = media.title
            mediaLink.text = media.url
            mediaDate.text = media.date
            mediaTime.text = media.time
            when (media.state) {
                DownloadMediaState.Loading -> {
                    mediaOpenProgressBar.isVisible = true
                    mediaOpenButton.isVisible = false
                }
                DownloadMediaState.Success -> {
                    mediaOpenProgressBar.isVisible = false
                    mediaOpenButton.setImageResource(R.drawable.ic_baseline_play_arrow)
                    mediaOpenButton.isVisible = true
                    mediaOpenButton.setOnClickListener {
                        onPlayClickListener?.invoke(media)
                    }
                }
                DownloadMediaState.Failed -> {
                    mediaOpenProgressBar.isVisible = false
                    mediaOpenButton.setImageResource(R.drawable.ic_baseline_cancel_24)
                    mediaOpenButton.isVisible = true
                }
            }
            when (media.type) {
                Constants.VIDEO_MIME_TYPE -> mediaTypeImv.setImageResource(R.drawable.ic_video)
                Constants.AUDIO_MIME_TYPE -> mediaTypeImv.setImageResource(R.drawable.ic_audio)
            }
        }
    }
}