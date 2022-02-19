package top.learningman.hystime.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import top.learningman.hystime.R
import top.learningman.hystime.repo.AppRepo
import top.learningman.hystime.utils.weekday
import java.io.Serializable
import java.util.*
import kotlin.math.ceil
import kotlin.math.ln

class HeatMapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var mData: Cal? = null

    private var cellLength: Float = 0.0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : this(
        context, attrs, defStyleAttr
    )

    fun setData(data: Cal) {
        mData = data
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mData == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val width = MeasureSpec.getSize(widthMeasureSpec)
            Log.d("HeatMapViewDefault", "width: $width")
        } else {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val day = mData!!.start.weekday()
            val dayCount = mData!!.data.size + (day - 1)
            val weekCount = ceil(dayCount.toDouble() / 7).toInt()
            val cellCount = weekCount * 5
            Log.d(
                "HeatMapViewMeasure",
                "dayCount: $dayCount, weekCount: $weekCount, cellCount: $cellCount width: $width"
            )
            // 4 cell for a time block and 1 cell for slide
            val cellEdge = width.toFloat() / cellCount
            this.cellLength = cellEdge

            val height = (cellEdge * (7 * 4 + 6)).toInt()
            setMeasuredDimension(width, height)
        }
    }


    override fun onDraw(canvas: Canvas) {
        if (mData == null) {
            super.onDraw(canvas)
            return
        }

        val dayPrefix = mData!!.start.weekday() - 1
        val max = mData!!.data.maxOrNull() ?: 0
        val maxLog = if (max == 0) 0.0.toFloat() else ln(max.toFloat())

        fun normalize(i: Int): Float {
            if (i == 0) {
                return 0.0.toFloat()
            }
            return ln(i.toFloat()) / maxLog
        }

        for (day in 0 until mData!!.data.size) {
            val loc = dayPrefix + day
            val week = loc / 7
            val weekDay = loc % 7
            Log.d(
                "HeatMapView",
                "day: $day week: $week, weekDay: $weekDay loc: $loc cellLength: $cellLength"
            )
            val leftTopX = week * cellLength * 5
            val leftTopY = weekDay * cellLength * 5
            val rightBottomX = leftTopX + cellLength * 4
            val rightBottomY = leftTopY + cellLength * 4
            val paint = getPaint(blendColors(normalize(mData!!.data[day])))
            canvas.drawRect(
                leftTopX,
                leftTopY,
                rightBottomX,
                rightBottomY,
                paint
            )
        }
    }

    companion object {
        data class Cal(
            val start: Date, val data: List<Int>
        ) : Serializable

        private val paintCache = HashMap<Int, Paint>()
        fun getPaint(color: Int): Paint {
            var paint = paintCache[color]
            if (paint == null) {
                paint = Paint().let {
                    it.color = color
                    it.isAntiAlias = true
                    it.style = Paint.Style.FILL
                    it.strokeWidth = 1f
                    it
                }
                paintCache[color] = paint
            }
            return paint
        }

        private fun blendColors(ratio: Float): Int {
            if (ratio <= 0.01f) {
                return Color.parseColor("#d0d7de")
            }
            val primaryColor = AppRepo.context.getColor(R.color.primaryColor)
            val whiteColor = Color.WHITE
            val inverseRatio = 1.0f - ratio
            val a =
                Color.alpha(whiteColor).toFloat() * inverseRatio + Color.alpha(primaryColor)
                    .toFloat() * ratio
            val r = Color.red(whiteColor).toFloat() * inverseRatio + Color.red(primaryColor)
                .toFloat() * ratio
            val g =
                Color.green(whiteColor).toFloat() * inverseRatio + Color.green(primaryColor)
                    .toFloat() * ratio
            val b =
                Color.blue(whiteColor).toFloat() * inverseRatio + Color.blue(primaryColor)
                    .toFloat() * ratio
            return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
        }


    }

}