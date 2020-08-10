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
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private var mediaDecoder: MediaDecoder? = null

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
        viewModel.audioUri.observe(this) {
            TransitionManager.beginDelayedTransition(binding.root)
            binding.btnMainPickSong.isVisible = false
            binding.grpMainPlayerUi.isVisible = true
            mediaPlayer = MediaPlayer.create(this@MainActivity, it)
            mediaDecoder = MediaDecoder(this@MainActivity, it).apply {
                val outputStream = ByteArrayOutputStream()
                var data: ByteArray?
                var size = 0
                while (readAudioData().apply { data = this } != null) {
                    outputStream.write(data!!)
                    size += data?.size ?: 0
                }
                val result = outputStream.toByteArray()
                binding.waveform.setDataSource(result)
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