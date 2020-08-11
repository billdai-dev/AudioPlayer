package com.app.audioplayer

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.app.audioplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initViews()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    private fun initViews() {
        viewModel.viewEvent.observe(this) { viewEvent ->
            when (viewEvent) {
                is MainViewEvent.Loading -> {
                    TransitionManager.beginDelayedTransition(binding.root)
                    binding.btnMainPickSong.isVisible = false
                    binding.piMainLoading.isVisible = true
                }
                is MainViewEvent.Done -> {
                    binding.piMainLoading.isVisible = false
                }
            }
        }
        viewModel.audioUri.observe(this) {
            mediaPlayer = MediaPlayer.create(this@MainActivity, it)
            viewModel.decodeAudioFile(this@MainActivity, it)
        }
        viewModel.decodedAudioData.observe(this) {
            binding.run {
                TransitionManager.beginDelayedTransition(root)
                btnMainPickSong.isVisible = false
                grpMainPlayerUi.isVisible = true
                vMainWaveform.setDataSource(it)
            }
        }

        binding.btnMainPickSong.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/mpeg"
            }
            startActivityForResult(intent, REQ_PICK_AUDIO_FILE)
        }

        binding.ibMainPlay.setOnClickListener {
            mediaPlayer?.start()
        }
        binding.ibMainPause.setOnClickListener {
            mediaPlayer?.pause()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQ_PICK_AUDIO_FILE) {
            val uri = data?.data ?: return
            viewModel.audioUri.value = uri
        }
    }

    companion object {
        private const val REQ_PICK_AUDIO_FILE = 0
    }
}