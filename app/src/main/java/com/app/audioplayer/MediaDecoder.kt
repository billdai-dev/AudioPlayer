package com.app.audioplayer

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.IOException
import java.nio.ByteBuffer

class MediaDecoder(context: Context, uri: Uri) {
    private val extractor = MediaExtractor()
    private var decoder: MediaCodec? = null
    private var inputFormat: MediaFormat? = null
    private var eof: Boolean = false
    private var outputBufferIndex: Int? = -1

    init {
        try {
            extractor.setDataSource(context, uri, null)
            // Select the first audio track we find.
            val numTracks = extractor.trackCount
            for (i in 0 until numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    extractor.selectTrack(i)
                    decoder = MediaCodec.createDecoderByType(mime)
                    decoder?.configure(format, null, null, 0)
                    inputFormat = format
                    break
                }
            }
            decoder?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Read the raw audio data in 16-bit format
    // Returns null on EOF
    fun readAudioData(): ByteArray? {
        val info = MediaCodec.BufferInfo()
        val data = readData(info) ?: return null
        val samplesRead = info.size
        if (samplesRead == 0) {
            return ByteArray(0)
        }
        val bytes = ByteArray(samplesRead)
        data.get(bytes)
        if (outputBufferIndex != null && outputBufferIndex!! >= 0) {
            decoder?.releaseOutputBuffer(outputBufferIndex!!, false)
        }
        return bytes
    }

    @SuppressLint("WrongConstant")
    private fun readData(info: MediaCodec.BufferInfo): ByteBuffer? {
        if (decoder == null) {
            return null
        }
        while (true) {
            // Read data from the uri into the codec.
            if (!eof) {
                val inputBufferIndex = decoder?.dequeueInputBuffer(-1)
                if (inputBufferIndex != null && inputBufferIndex >= 0) {
                    val inputBuffer = decoder?.getInputBuffer(inputBufferIndex) ?: continue
                    val size = extractor.readSampleData(inputBuffer, 0)
                    if (size < 0) {
                        // End Of File
                        decoder?.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        eof = true
                    } else {
                        decoder?.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            size,
                            extractor.sampleTime,
                            0
                        )
                        extractor.advance()
                    }
                }
            }

            // Read the output from the codec.
            outputBufferIndex = decoder?.dequeueOutputBuffer(info, 10000)
            if (outputBufferIndex != null && outputBufferIndex!! >= 0) {
                // Handle EOF
                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    decoder?.stop()
                    decoder?.release()
                    decoder = null
                    return null
                }
                return decoder?.getOutputBuffer(outputBufferIndex!!)
            }
        }
    }
}