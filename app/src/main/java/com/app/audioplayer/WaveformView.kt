package com.app.audioplayer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt

class WaveformView : View {
    private var rawData: ByteArray? = null
    private var normalizedSamples: FloatArray? = null
    private var playedPercentage: Float = 0f

    private val playedPartPaint: Paint = Paint()
    private val notPlayedPartPaint: Paint = Paint()

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    private fun init() {
        playedPartPaint.run {
            strokeWidth = 1f
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.colorAccent)
        }

        notPlayedPartPaint.run {
            strokeWidth = 1f
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.material_on_surface_disabled)
        }
    }

    fun setDataSource(bytes: ByteArray?) {
        rawData = bytes ?: return
        invalidate()
    }

    @MainThread
    fun updatePlayedPercentage(percentage: Float) {
        playedPercentage = percentage
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val barCount = calculateBarCount(width)
        if (normalizedSamples == null) {
            normalizedSamples = calculateSample(barCount, rawData)
        }

        //Draw bar
        var left = 0f
        var right: Float
        for (i in 0 until barCount) {
            //Determine the bar color by checking playback progress
            val isPlayed = i <= (barCount * playedPercentage).roundToInt()
            val paint = if (isPlayed) playedPartPaint else notPlayedPartPaint

            right = left + toPx(BAR_WIDTH_DP)
            val ratio = (normalizedSamples!![i]).absoluteValue
            val top = height - (ratio * height)
            canvas.drawRect(left, top, right, height, paint)

            left = right + toPx(BAR_GAP_DP)
        }
    }

    private fun calculateBarCount(width: Float): Int {
        return (width / (toPx(BAR_WIDTH_DP) + toPx(BAR_GAP_DP))).toInt()
    }

    private fun calculateSample(barCount: Int, rawData: ByteArray?): FloatArray? {
        if (rawData == null) {
            return null
        }
        val shortArr = ShortArray(rawData.size / 2)
        ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArr)

        //Calculate samples for each bar
        val samplesPerBar = shortArr.size / barCount
        val samples = FloatArray(barCount)

        //Down sampling raw audio data as chunks
        for (i in 0 until barCount) {
            //Calculate average of the chunk as sampling algorithm
            val sampleStartIndex = samplesPerBar * i
            var sum = 0f
            for (j in 0 until samplesPerBar) {
                sum += abs(shortArr[sampleStartIndex + j].toFloat())
            }
            samples[i] = sum / samplesPerBar
        }

        //Find the sample chunk with highest value for normalization
        val multiplier = samples.max()?.toDouble()?.pow(-1)?.toFloat() ?: 1f
        for (i in samples.indices) {
            samples[i] = samples[i] * multiplier
        }
        return samples
    }

    private fun toPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics!!
        )
    }

    companion object {
        private const val BAR_WIDTH_DP = 3f
        private const val BAR_GAP_DP = 1f
    }
}