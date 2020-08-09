package com.app.audioplayer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class WaveformView : View {
    /**
     * bytes array converted from file.
     */
    private var sampledBytes: FloatArray? = null

    //    /**
//     * Percentage of audio sample scale
//     * Should updated dynamically while audioPlayer is played
//     */
//    private var denseness = 0f
    private var playedPercentage: Float = 0f
    private var barCount = 0

    /**
     * Canvas painting for sample scale, filling played part of audio sample
     */
    private val playedPartPaint = Paint()

    /**
     * Canvas painting for sample scale, filling not played part of audio sample
     */
    private val notPlayedPartPaint = Paint()
    //private var width = 0
    //private var height = 0

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
        playedPartPaint.strokeWidth = 1f
        playedPartPaint.isAntiAlias = true
        playedPartPaint.color = ContextCompat.getColor(
            context,
            R.color.material_on_surface_disabled
        )
        notPlayedPartPaint.strokeWidth = 1f
        notPlayedPartPaint.isAntiAlias = true
        notPlayedPartPaint.color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    /**
     * update and redraw Visualizer view
     */
    fun setSource(bytes: ByteArray?) {
        bytes ?: return
        val samplesPerBar = (bytes.size / barCount)
        val sampledBytes = bytes.toList()
            .chunked(samplesPerBar) { samples ->
                var squareSum = 0
                samples.forEach { squareSum += it * it }
                sqrt((squareSum / samples.size).toFloat())
                //samples.maxBy { it } ?: 0
            }
        this.sampledBytes = FloatArray(sampledBytes.size) { sampledBytes[it] }
        invalidate()
    }

    /**
     * Update player percent. 0 - file not played, 1 - full played
     *
     * @param percentage
     */
    fun updatePlayerPercent(percentage: Float) {
        playedPercentage = percentage
//        denseness = Math.ceil(width * percent.toDouble()) as Int.toFloat()
//        if (denseness < 0) {
//            denseness = 0f
//        } else if (denseness > width) {
//            denseness = width.toFloat()
//        }
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
        barCount = (measuredWidth / toPx(BAR_WIDTH_DP)).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        if (sampledBytes == null || width == 0f) {
            return
        }
        var left = 0f
        var right: Float
        for (i in 0 until barCount) {
            right = left + (i+1) * toPx(BAR_WIDTH_DP)
            val ratio = (sampledBytes!![i] / 128f).absoluteValue
            val top = height - (ratio * height)
            canvas.drawRect(left, top, right, height, notPlayedPartPaint)
//            if (x < denseness && x + toPx(2f) < denseness) {
//                canvas.drawRect(left, top, right, bottom, notPlayedPartPaint)
//            } else {
//                canvas.drawRect(left, top, right, bottom, playedPartPaint)
//                if (x < denseness) {
//                    canvas.drawRect(left, top, right, bottom, notPlayedPartPaint)
//                }
//            }
            left = right
        }

        //val totalBarsCount = width / toPx(3f)
//        if (totalBarsCount <= 0.1f) {
//            return
//        }
        //var value: Byte
//        val samplesCount = sampledBytes!!.size
//        var barCounter = 0
//        var nextBarNum = 0
//        val y = height / 2
//        var barNum = 0
//        var lastBarNum: Int
//        var drawBarCount: Int
//        for (a in 0 until samplesCount) {
//            if (a != nextBarNum) {
//                continue
//            }
//            drawBarCount = 0
//            lastBarNum = nextBarNum
//            while (lastBarNum == nextBarNum) {
//                barCounter += (samplesCount / totalBarsCount)
//                nextBarNum = barCounter
//                drawBarCount++
//            }
//            val bitPointer = a * 5
//            val byteNum = bitPointer / java.lang.Byte.SIZE
//            val byteBitOffset = bitPointer - byteNum * java.lang.Byte.SIZE
//            val currentByteCount = java.lang.Byte.SIZE - byteBitOffset
//            val nextByteRest = 5 - currentByteCount
//            value = (sampledBytes!![byteNum] shr byteBitOffset and (2 shl Math.min(
//                5,
//                currentByteCount
//            ) - 1) - 1)
//            if (nextByteRest > 0) {
//                value = value shl nextByteRest
//                value = value or (sampledBytes!![byteNum + 1] and (2 shl nextByteRest - 1) - 1)
//            }
//            for (b in 0 until drawBarCount) {
//                val x = barNum * toPx(3f)
//                val left = x.toFloat()
//                val top = y + toPx(
//                    VISUALIZER_HEIGHT - Math.max(
//                        1,
//                        VISUALIZER_HEIGHT * value / 31.0f
//                    )
//                ).toFloat()
//                val right = x + toPx(2f).toFloat()
//                val bottom = y + toPx(VISUALIZER_HEIGHT).toFloat()
//                if (x < denseness && x + toPx(2f) < denseness) {
//                    canvas.drawRect(left, top, right, bottom, notPlayedPartPaint)
//                } else {
//                    canvas.drawRect(left, top, right, bottom, playedPartPaint)
//                    if (x < denseness) {
//                        canvas.drawRect(left, top, right, bottom, notPlayedPartPaint)
//                    }
//                }
//                barNum++
//            }
//        }
    }

    private fun toPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics!!
        )
    }

    companion object {
        private const val BAR_WIDTH_DP = 5f
    }
}