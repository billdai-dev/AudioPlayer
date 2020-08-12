package com.app.audioplayer

import android.content.Context
import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class MainViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val mediaDecoder: MediaDecoder
) : ViewModel() {
    val audioUri = state.getLiveData<Uri>(STATE_AUDIO_URI)

    //Avoid saving massive data to SavedStateHandle
    val decodedAudioData = MutableLiveData<Pair<Uri, ByteArray>>()
    val viewEvent = MutableLiveData<MainViewEvent>()

    private val compositeDisposable = CompositeDisposable()

    fun decodeAudioFile(context: Context, uri: Uri) {
        //Avoid decoding duplicate audio data
        if (decodedAudioData.value?.first == uri) {
            return
        }
        viewEvent.value = MainViewEvent.Loading
        mediaDecoder.init(context, uri)
        Single.fromCallable {
            val outputStream = ByteArrayOutputStream()
            var data: ByteArray?
            var size = 0
            while (mediaDecoder.readAudioData().apply { data = this } != null) {
                outputStream.write(data!!)
                size += data?.size ?: 0
            }
            outputStream.toByteArray()
        }
            .subscribeOn(Schedulers.io())
            .subscribeBy { bytes ->
                viewEvent.postValue(MainViewEvent.Done)
                decodedAudioData.postValue(uri to bytes)
            }
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onCleared()
    }

    companion object {
        private const val STATE_AUDIO_URI = "audioUri"
    }
}