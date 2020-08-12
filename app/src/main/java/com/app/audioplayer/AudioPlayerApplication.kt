package com.app.audioplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AudioPlayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

    }
}