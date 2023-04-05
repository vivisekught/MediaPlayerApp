package com.example.mediaplayerapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        Log.d(TAG, "onCreate: $link")
        binding.playerView.player = exoPlayer
        if (checkPermission()) {
            loadMediaToPlayer()
        } else {
            requestPermissions()
        }
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

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "request started")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                Log.d(TAG, "request failed")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSIONS_CODE
            )
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d(TAG, "registerForActivityResult")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    loadMediaToPlayer()
                } else {
                    Log.d(TAG, "Access Denied: ")
                }
            }
        }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write and read) {
                    loadMediaToPlayer()
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: Denied permissions")
                }
            }
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


    private val exoPlayerListener = object : Player.Listener {  // 1
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {   // 2
            if (playWhenReady) Log.d("Nikita", "Start")
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {   // 3
                    Log.d(TAG, "Loading")
                    binding.playerProgressBar.isVisible = true
                }
                Player.STATE_READY -> {   // 4
                    binding.playerProgressBar.isVisible = false

                    Log.d(TAG, "READY")
                }
                Player.STATE_ENDED -> { // 5
                    exoPlayer.seekTo(0)
                    exoPlayer.playWhenReady = false
                }
                Player.STATE_IDLE -> {

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
        private const val STORAGE_PERMISSIONS_CODE = 100
        private const val EMPTY_LINK = ""


        private const val LINK = "Link"

        fun newIntent(context: Context, link: String): Intent {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(LINK, link)
            return intent
        }
    }
}