package com.app.audioplayer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.absoluteValue
import kotlin.math.pow

class WaveformView : View {
    private var sampledData: FloatArray? = null
    private var playedPercentage: Float = 0f
    private var barCount = 1
    private val playedPartPaint = Paint()
    private val notPlayedPartPaint = Paint()

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
        bytes ?: return
        val shortArr = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArr)

        //Calculate samples for each bar
        val samplesPerBar = shortArr.size / barCount
        val sampledChunks = shortArr.toList()
            //Down sampling raw audio data as chunks
            .chunked(samplesPerBar) { samples ->
                //Calculate average of the chunk as sampling algorithm
                var squareSum = 0
                samples.forEach { squareSum += it * it }
                (squareSum / samples.size).toFloat()
            }
        //Find the sample chunk with highest value for normalization
        val multiplier = sampledChunks.max()?.toDouble()?.pow(-1)?.toFloat() ?: 1f
        this.sampledData = FloatArray(sampledChunks.size) {
            //Do normalization
            sampledChunks[it] * multiplier
        }

        invalidate()
    }

    fun updatePlayedPercentage(percentage: Float) {
        playedPercentage = percentage
        invalidate()
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        barCount = (measuredWidth / (toPx(BAR_WIDTH_DP) + toPx(BAR_GAP_DP))).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        if (sampledData == null || width == 0f) {
            return
        }
        var left = 0f
        var right: Float
        for (i in 0 until barCount) {
            right = left + toPx(BAR_WIDTH_DP)
            val ratio = (sampledData!![i]).absoluteValue
            val top = height - (ratio * height)
            canvas.drawRect(left, top, right, height, notPlayedPartPaint)
            left = right + toPx(BAR_GAP_DP)
        }
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