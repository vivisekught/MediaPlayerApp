package com.example.mediaplayerapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.mediaplayerapp.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector


class PlayerActivity : AppCompatActivity() {
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var link: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        parseIntent()
        initializePlayer()
        binding.playerView.player = exoPlayer
        loadMediaToPlayer()

    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            exoPlayer.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) {
            exoPlayer.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }


    private fun loadMediaToPlayer() {
        val mediaItem = MediaItem.fromUri(link)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }


    private fun initializePlayer() {
        val renderersFactory = buildRenderersFactory(applicationContext, true)
        val trackSelector = DefaultTrackSelector(applicationContext)
        exoPlayer = ExoPlayer.Builder(applicationContext, renderersFactory)
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .setTrackSelector(trackSelector)
            .build().apply {
                trackSelectionParameters =
                    DefaultTrackSelector.Parameters.Builder(applicationContext).build()
                addListener(exoPlayerListener)
                playWhenReady = true
            }
    }

    private fun buildRenderersFactory(
        context: Context, preferExtensionRenderer: Boolean
    ): RenderersFactory {
        val extensionRendererMode = if (preferExtensionRenderer)
            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
        else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON

        return DefaultRenderersFactory(context.applicationContext)
            .setExtensionRendererMode(extensionRendererMode)
            .setEnableDecoderFallback(true)
    }


    private val exoPlayerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    Log.d(TAG, "Loading")
                    binding.playerProgressBar.isVisible = true
                }
                Player.STATE_READY -> {
                    binding.playerProgressBar.isVisible = false

                    Log.d(TAG, "READY")
                }
                Player.STATE_ENDED -> {
                    exoPlayer.seekTo(0)
                    exoPlayer.playWhenReady = false
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            binding.playerProgressBar.isVisible = false
            Toast.makeText(this@PlayerActivity, error.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun parseIntent() {
        if (intent.hasExtra(LINK)) {
            link = intent.getStringExtra(LINK) ?: EMPTY_LINK
            Log.d(TAG, link)
        } else throw RuntimeException("Parameter LINK is absent")
    }


    companion object {
        private const val TAG = "PLAYER_TAG"
        private const val EMPTY_LINK = ""
        private const val LINK = "Link"

        fun newIntent(context: Context, link: String): Intent {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(LINK, link)
            return intent
        }
    }
}