package com.example.mediaplayerapp

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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.mediaplayerapp.databinding.ActivityMainBinding
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

    private val videoReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Downloader.ACTION_DOWNLOAD_COMPLETE) {
                try {
                    val id = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID,
                        Downloader.DOWNLOAD_DEFAULT_VALUE
                    )
                    val query = DownloadManager.Query().setFilterById(id)
                    downloadManager.query(query).use { cursor ->
                        if (cursor.moveToFirst()) {
                            viewModel.downloadState.value =
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        } else {
                            viewModel.downloadState.value = null
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
        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
                writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: writePermissionGranted

                if (readPermissionGranted) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Can't read files without permission.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        updateOrRequestPermissions()
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
        val downloader = Downloader(this)
        downloader.downloadVideo(uri)
    }

    private fun registerReceivers() {
        val intentFilter = IntentFilter(Downloader.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(videoReceiver, intentFilter)
    }

    private fun setObservers() {
        with(viewModel) {
            uri.observe(this@MainActivity) { uri ->
                if (uri != null) {
                    startActivity(PlayerActivity.newIntent(this@MainActivity, uri))
                }
            }
            downloadState.observe(this@MainActivity) { state ->
                when (state) {
                    DownloadManager.STATUS_FAILED -> {
                        binding.downloadVideoByUrlPb.isVisible = false
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.download_failure),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        binding.downloadVideoByUrlPb.isVisible = false
                    }
                }
            }
        }
    }

    private fun setOnClickListeners() {
        with(binding) {
            watchVideoByUrlButton.setOnClickListener {
                if (readPermissionGranted) {
                    val url = watchVideoByUrlEt.text.toString()
                    if (viewModel.parseURL(url)) {
                        startActivity(PlayerActivity.newIntent(this@MainActivity, url))
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
                if (writePermissionGranted){
                    val url = binding.downloadVideoByUrlEt.text.toString()
                    binding.downloadVideoByUrlPb.isVisible = true
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
                    getContent.launch("video/*")
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
        unregisterReceiver(videoReceiver)
    }

    private companion object {
        private const val TAG = "MAIN_ACTIVITY_TAG"
    }
}