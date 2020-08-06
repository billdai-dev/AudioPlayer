package com.app.audioplayer

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainViewModel(private val state: SavedStateHandle) : ViewModel() {
    val audioUri = state.getLiveData<Uri>(STATE_AUDIO_URI)

    companion object {
        private const val STATE_AUDIO_URI = "audioUri"
    }
}