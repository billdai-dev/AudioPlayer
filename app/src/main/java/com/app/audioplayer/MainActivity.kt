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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private var trackProgressDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initViews()
    }

    override fun onDestroy() {
        if (trackProgressDisposable?.isDisposed == false) {
            trackProgressDisposable?.dispose()
        }
        trackProgressDisposable = null
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
            mediaPlayer = MediaPlayer.create(this@MainActivity, it).apply {
                setOnCompletionListener { mp ->
                    if (trackProgressDisposable?.isDisposed == false) {
                        trackProgressDisposable?.dispose()
                    }
                    binding.vMainWaveform.updatePlayedPercentage(1f)
                }
            }
            viewModel.decodeAudioFile(this@MainActivity, it)
        }
        viewModel.decodedAudioData.observe(this) {
            binding.run {
                TransitionManager.beginDelayedTransition(root)
                btnMainPickSong.isVisible = false
                grpMainPlayerUi.isVisible = true
                vMainWaveform.setDataSource(it.second)
            }
        }

        binding.btnMainPickSong.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/mpeg"
            }
            startActivityForResult(intent, REQ_PICK_AUDIO_FILE)
        }

        binding.ibMainPlay.setOnClickListener {
            mediaPlayer?.run {
                if (!isPlaying) {
                    trackProgressDisposable = createProgressTracker()
                }
                start()
            }
        }
        binding.ibMainPause.setOnClickListener {
            mediaPlayer?.run {
                if (isPlaying) {
                    if (trackProgressDisposable?.isDisposed == false) {
                        trackProgressDisposable?.dispose()
                    }
                }
                pause()
            }
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

    private fun createProgressTracker(): Disposable {
        return Observable.interval(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                val audioDuration = mediaPlayer?.duration?.toFloat() ?: return@subscribeBy
                val currentPosition = mediaPlayer?.currentPosition?.toFloat() ?: return@subscribeBy
                val progressPercent = currentPosition / audioDuration
                binding.vMainWaveform.updatePlayedPercentage(progressPercent)
            }
    }

    companion object {
        private const val REQ_PICK_AUDIO_FILE = 0
    }
}