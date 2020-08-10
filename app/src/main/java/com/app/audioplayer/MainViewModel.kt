package com.app.audioplayer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class MainViewModel(private val state: SavedStateHandle) : ViewModel() {
    val audioUri = state.getLiveData<Uri>(STATE_AUDIO_URI)
    val decodedAudioData = MutableLiveData<ByteArray>()
    private var mediaDecoder: MediaDecoder? = null
    private val compositeDisposable = CompositeDisposable()

    fun decodeAudioFile(context: Context, uri: Uri) {
        mediaDecoder = MediaDecoder(context, uri)
        Single.fromCallable {
            val outputStream = ByteArrayOutputStream()
            var data: ByteArray?
            var size = 0
            while (mediaDecoder?.readAudioData().apply { data = this } != null) {
                outputStream.write(data!!)
                size += data?.size ?: 0
            }
            outputStream.toByteArray()
        }
            .subscribeOn(Schedulers.io())
            .subscribeBy { bytes -> decodedAudioData.postValue(bytes) }
    }

    override fun onCleared() {
        mediaDecoder = null
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onCleared()
    }

    companion object {
        private const val STATE_AUDIO_URI = "audioUri"
    }
}