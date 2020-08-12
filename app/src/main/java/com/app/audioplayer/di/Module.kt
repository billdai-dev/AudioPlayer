package com.app.audioplayer.di

import com.app.audioplayer.MediaDecoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object Module {

    @Provides
    fun provideMediaDecoder(): MediaDecoder {
        return MediaDecoder()
    }
}