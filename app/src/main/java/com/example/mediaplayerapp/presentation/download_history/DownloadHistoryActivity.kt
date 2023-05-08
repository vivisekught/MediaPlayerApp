package com.example.mediaplayerapp.presentation.download_history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.mediaplayerapp.databinding.ActivityMediaDownloadHistoryBinding
import com.example.mediaplayerapp.presentation.PlayerActivity
import com.example.mediaplayerapp.utils.Constants

class DownloadHistoryActivity : AppCompatActivity() {

    private lateinit var downloadHistoryAdapter: DownloadHistoryAdapter
    private lateinit var binding: ActivityMediaDownloadHistoryBinding
    private lateinit var viewModel: DownloadHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaDownloadHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[DownloadHistoryViewModel::class.java]
        setupRecyclerView()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.mediaHistory.observe(this) {
            binding.emptyHistory.isVisible = it.isEmpty()
            downloadHistoryAdapter.submitList(it)
        }
    }

    private fun setupRecyclerView() {
        downloadHistoryAdapter = DownloadHistoryAdapter()
        binding.downloadMediaRv.adapter = downloadHistoryAdapter
        setupOnPlayClickListener()
    }

    private fun setupOnPlayClickListener() {
        downloadHistoryAdapter.onPlayClickListener = {
            when (it.type) {
                Constants.VIDEO_MIME_TYPE -> startActivity(
                    PlayerActivity.newVideoIntent(
                        this,
                        it.uri.toString()
                    )
                )
                Constants.AUDIO_MIME_TYPE -> startActivity(
                    PlayerActivity.newAudioIntent(
                        this,
                        it.uri.toString()
                    )
                )
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, DownloadHistoryActivity::class.java)
        }
    }
}