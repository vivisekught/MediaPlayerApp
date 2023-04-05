package com.example.mediaplayerapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mediaplayerapp.databinding.ActivityMainBinding
import com.google.android.exoplayer2.Player


class MainActivity : AppCompatActivity(), Player.Listener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

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
        setObservers()
        setOnClickListeners()
    }

    private fun setObservers() {
        with(viewModel) {
            uri.observe(this@MainActivity) { uri ->
                if (uri != null) {
                    startActivity(PlayerActivity.newIntent(this@MainActivity, uri))
                }
            }
        }
    }

    private fun setOnClickListeners() {
        with(binding) {
            watchVideoByUrlButton.setOnClickListener {
                val url = watchVideoByUrlEt.text.toString()
                if (viewModel.parseURL(url)) {
                    startActivity(PlayerActivity.newIntent(this@MainActivity, url))
                } else {
                    Toast.makeText(this@MainActivity, "Incorrect URL", Toast.LENGTH_SHORT).show()
                }
            }
            chooseVideoStorage.setOnClickListener {
                getContent.launch("video/*")
            }
        }
    }

    private companion object {
        private const val TAG = "MAIN_ACTIVITY_TAG"
    }
}