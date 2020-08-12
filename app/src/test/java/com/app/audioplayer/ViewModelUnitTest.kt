package com.app.audioplayer

import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(MockitoJUnitRunner::class)
class ViewModelUnitTest {
    @Rule
    @JvmField
    var instantExecutor: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var mockMediaDecoder: MediaDecoder

    @Mock
    lateinit var mockContext: Context

    @Mock
    lateinit var mockUri: Uri

    @Spy
    lateinit var handle: SavedStateHandle

    lateinit var viewModel: MainViewModel

    @Before
    fun init() {
        viewModel = MainViewModel(handle, mockMediaDecoder)
    }

    @Test
    fun avoidDecodingSameAudioFile() {
        viewModel.decodedAudioData.value = mockUri to byteArrayOf()
        viewModel.decodeAudioFile(mockContext, mockUri)
        assertNull(viewModel.viewEvent.value)
        verify(mockMediaDecoder, never()).init(any(), any())
        verify(mockMediaDecoder, never()).readAudioData()
    }

    @Test
    fun decodingAudioFileSuccess() {
        val fakeRawAudioData = arrayOf(
            byteArrayOf(0),
            byteArrayOf(1),
            byteArrayOf(2)
        )
        whenever(mockMediaDecoder.readAudioData()).thenReturn(
            fakeRawAudioData[0],
            fakeRawAudioData[1],
            fakeRawAudioData[2],
            null
        )
        viewModel.decodeAudioFile(mockContext, mockUri)
        val inOrder = inOrder(mockMediaDecoder)

        inOrder.verify(mockMediaDecoder, times(1)).init(any(), any())
        inOrder.verify(mockMediaDecoder, times(fakeRawAudioData.size + 1)).readAudioData()

        assertEquals(viewModel.viewEvent.value, MainViewEvent.Done)

        val resultUri = viewModel.decodedAudioData.value?.first
        assertEquals(resultUri, mockUri)

        val resultRawAudioData = viewModel.decodedAudioData.value?.second
        assertNotNull(resultRawAudioData)
        assertEquals(resultRawAudioData!!.size, fakeRawAudioData.size)
        for (i in 0..resultRawAudioData.lastIndex) {
            assertEquals(resultRawAudioData[i], fakeRawAudioData[i].first())
        }
    }
}