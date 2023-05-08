package com.example.mediaplayerapp.presentation

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.mediaplayerapp.R
import com.example.mediaplayerapp.databinding.ActivityMainBinding
import com.example.mediaplayerapp.presentation.download_history.DownloadHistoryActivity
import com.example.mediaplayerapp.utils.Constants
import com.example.mediaplayerapp.utils.Constants.AUDIO_MIME_TYPE
import com.example.mediaplayerapp.utils.Constants.VIDEO_MIME_TYPE
import com.example.mediaplayerapp.utils.DownloadMediaState
import com.google.android.exoplayer2.Player


class MainActivity : AppCompatActivity(), Player.Listener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    val downloadManager: DownloadManager by lazy {
        getSystemService(DownloadManager::class.java)
    }

    private val downloadReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                try {
                    val id = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID,
                        Constants.DOWNLOAD_DEFAULT_VALUE
                    )
                    val query = DownloadManager.Query().setFilterById(id)
                    downloadManager.query(query).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val state =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            if (state == DownloadManager.STATUS_SUCCESSFUL) {
                                viewModel.updateMediaUri(
                                    id,
                                    downloadManager.getUriForDownloadedFile(id).toString(),
                                    DownloadMediaState.Success
                                )
                            }
                            if (state == DownloadManager.STATUS_FAILED) {
                                viewModel.updateMediaUri(
                                    id,
                                    null,
                                    DownloadMediaState.Failed
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                }
            }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Log.d(TAG, uri.toString())
            if (uri != null) {
                viewModel.getPath(this@MainActivity, uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        registerReceivers()
        setObservers()
        setOnClickListeners()
        setupActionBar()

        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: readPermissionGranted
                writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: writePermissionGranted

                if (readPermissionGranted) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        this,
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        updateOrRequestPermissions()
    }

    private fun setupActionBar() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.main_activity_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_bar_downloads -> {
                        startActivity(DownloadHistoryActivity.newIntent(this@MainActivity))
                        true
                    }
                    R.id.action_bar_history -> {
                        true
                    }
                    else -> false
                }
            }
        }, this, Lifecycle.State.RESUMED)
    }

    private fun updateOrRequestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startDownloadVideo(uri: String) {
        viewModel.downloadVideo(uri)
    }

    private fun startDownloadAudio(uri: String) {
        viewModel.downloadAudio(uri)
    }

    private fun registerReceivers() {
        val intentFilter = IntentFilter().apply {
            addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        }
        registerReceiver(downloadReceiver, intentFilter)
    }

    private fun setObservers() {
        with(viewModel){
            audioUrl.observe(this@MainActivity){
                startActivity(PlayerActivity.newAudioIntent(this@MainActivity, it))
            }
            videoUrl.observe(this@MainActivity){
                startActivity(PlayerActivity.newVideoIntent(this@MainActivity, it))
            }
        }
    }

    private fun setOnClickListeners() {
        with(binding) {
            watchVideoByUrlButton.setOnClickListener {
                if (readPermissionGranted) {
                    val url = watchVideoByUrlEt.text.toString()
                    if (viewModel.parseURL(url)) {
                        startActivity(PlayerActivity.newVideoIntent(this@MainActivity, url))
                    } else {
                        Toast.makeText(this@MainActivity, "Incorrect URL", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                    updateOrRequestPermissions()
                }

            }
            downloadVideoByUrlButton.setOnClickListener {
                if (writePermissionGranted) {
                    val url = binding.downloadVideoByUrlEt.text.toString()
                    startDownloadVideo(url)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                    updateOrRequestPermissions()
                }
            }
            chooseVideoStorage.setOnClickListener {
                if (readPermissionGranted) {
                    getContent.launch(VIDEO_MIME_TYPE)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                    updateOrRequestPermissions()
                }
            }
            audioByUrlButton.setOnClickListener {
                if (readPermissionGranted) {
                    val url = audioByUrlEt.text.toString()
                    if (viewModel.parseURL(url)) {
                        startActivity(PlayerActivity.newAudioIntent(this@MainActivity, url))
                    } else {
                        Toast.makeText(this@MainActivity, "Incorrect URL", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                    updateOrRequestPermissions()
                }

            }
            downloadAudioByUrlButton.setOnClickListener {
                if (writePermissionGranted) {
                    val url = binding.downloadAudioByUrlEt.text.toString()
                    startDownloadAudio(url)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                    updateOrRequestPermissions()
                }
            }
            chooseAudioStorage.setOnClickListener {
                if (readPermissionGranted) {
                    getContent.launch(AUDIO_MIME_TYPE)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Can't read files without permission.",
                        Toast.LENGTH_LONG
                    ).show()
                    updateOrRequestPermissions()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }

    private companion object {
        private const val TAG = "MAIN_ACTIVITY_TAG"
    }
}