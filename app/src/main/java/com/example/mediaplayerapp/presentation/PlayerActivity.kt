package com.example.mediaplayerapp.presentation

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.cronet.CronetDataSource
import com.google.android.exoplayer2.ext.cronet.CronetUtil
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.DefaultMediaDescriptionAdapter
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.EventLogger
import org.chromium.net.CronetEngine
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.Executors


class PlayerActivity : AppCompatActivity() {
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var link: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        parseIntent()
        binding.playerView.player = exoPlayer
        loadMediaToPlayer()
    }


//    override fun onPause() {
//        super.onPause()
//        if (Build.VERSION.SDK_INT <= 23) {
//
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (Build.VERSION.SDK_INT > 23) {
//            finish()
//
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        finish()
    }


    private fun loadMediaToPlayer() {
        val mediaItem = MediaItem.fromUri(link)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }


    private fun initializeVideoPlayer() {
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

    private fun initializeAudioPlayer() {
        val renderersFactory = buildRenderersFactory(applicationContext, true)
        val mediaSourceFactory =
            DefaultMediaSourceFactory(
                getDataSourceFactory(applicationContext),
                DefaultExtractorsFactory()
            )
        val trackSelector = DefaultTrackSelector(applicationContext)

        binding.playerView.defaultArtwork =
            AppCompatResources.getDrawable(this, R.drawable.player_background)
        exoPlayer = ExoPlayer.Builder(applicationContext, renderersFactory)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                addAnalyticsListener(EventLogger())
                trackSelectionParameters =
                    DefaultTrackSelector.Parameters.getDefaults(applicationContext)
                addListener(exoPlayerListener)
                playWhenReady = false
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

    private fun getDataSourceFactory(context: Context): DataSource.Factory =
        DefaultDataSource.Factory(context, getHttpDataSourceFactory(context))

    private fun getHttpDataSourceFactory(context: Context): HttpDataSource.Factory {
        val cronetEngine: CronetEngine? = CronetUtil.buildCronetEngine(context)
        var httpDataSourceFactory: HttpDataSource.Factory? = null

        if (cronetEngine != null) httpDataSourceFactory =
            CronetDataSource.Factory(cronetEngine, Executors.newSingleThreadExecutor())

        if (httpDataSourceFactory == null) {
            // We don't want to use Cronet, or we failed to instantiate a CronetEngine.
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
            CookieHandler.setDefault(cookieManager)
            httpDataSourceFactory = DefaultHttpDataSource.Factory()
        }
        return httpDataSourceFactory
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
        if (!intent.hasExtra(PLAYER_MODE)) {
            throw RuntimeException("Parameter PLAYER_MODE is absent")
        }
        if (intent.hasExtra(LINK)) {
            link = intent.getStringExtra(LINK) ?: EMPTY_LINK
            Log.d(TAG, link)
        } else throw RuntimeException("Parameter LINK is absent")
        if (intent.getStringExtra(PLAYER_MODE) == VIDEO_PLAYER_MODE) {
            initializeVideoPlayer()
        }
        if (intent.getStringExtra(PLAYER_MODE) == AUDIO_PLAYER_MODE) {
            initializeAudioPlayer()
        }
    }


    companion object {
        private const val TAG = "PLAYER_TAG"
        private const val EMPTY_LINK = ""
        private const val LINK = "Link"
        private const val VIDEO_PLAYER_MODE = "VIDEO_MODE"
        private const val AUDIO_PLAYER_MODE = "AUDIO_MODE"
        private const val PLAYER_MODE = "PLAYER_MODE"

        fun newVideoIntent(context: Context, link: String): Intent {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(LINK, link)
                putExtra(PLAYER_MODE, VIDEO_PLAYER_MODE)
            }
            return intent
        }

        fun newAudioIntent(context: Context, link: String): Intent {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(LINK, link)
                putExtra(PLAYER_MODE, AUDIO_PLAYER_MODE)
            }
            return intent
        }
    }
}