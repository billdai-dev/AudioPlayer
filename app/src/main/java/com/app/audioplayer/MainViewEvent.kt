package com.app.audioplayer

sealed class MainViewEvent {
    object Loading : MainViewEvent()
    object Done : MainViewEvent()
}