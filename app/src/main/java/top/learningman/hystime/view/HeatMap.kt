package top.learningman.hystime.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import top.learningman.hystime.R
import top.learningman.hystime.repo.AppRepo
import top.learningman.hystime.utils.*
import java.io.Serializable
import java.util.*
import kotlin.math.ceil
import kotlin.math.ln

class HeatMapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var mData: Cal? = null

    private var cellLength: Float = 0.0f

    private val fontSizeBase = 3 * 4
    private val fontSizeBasePx = fontSizeBase.spToPx()

    private val labelPaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
        isAntiAlias = true
        textSize = fontSizeBasePx
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }

    private val fontHeight =
        ceil(labelPaint.fontMetrics.descent - labelPaint.fontMetrics.ascent).toInt()
    private val fontOffsetX = labelPaint.measureText("XXX") * 1.5f
    private val fontOffsetY = (this.cellLength * 4 + fontHeight)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : this(
        context, attrs, defStyleAttr
    )

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeatMapView)
        this.cellLength = typedArray.getDimension(R.styleable.HeatMapView_ceilSize, 2f)
        typedArray.recycle()
    }

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
            val day = mData!!.start.weekday()
            val dayCount = mData!!.data.size + (day - 1)
            val weekCount = ceil(dayCount.toDouble() / 7).toInt()
            // 4 cell for a time block and 1 cell for divider
            val height = (this.cellLength * (7 * 4 + 6) + fontOffsetY).toInt()
            val width = (this.cellLength * (weekCount * 4 + (weekCount - 1)) + fontOffsetX).toInt()
            setMeasuredDimension(width, height)
        }
    }


    override fun onDraw(canvas: Canvas) {
        if (mData == null) {
            super.onDraw(canvas)
            return
        }
        // draw label

        canvas.drawText("Mon", fontOffsetX / 2, fontOffsetY + cellLength * (4), labelPaint)
        canvas.drawText("Wed", fontOffsetX / 2, fontOffsetY + cellLength * (4 * 3 + 2), labelPaint)
        canvas.drawText("Fri", fontOffsetX / 2, fontOffsetY + cellLength * (4 * 5 + 4), labelPaint)
        canvas.drawText("Sun", fontOffsetX / 2, fontOffsetY + cellLength * (4 * 7 + 6), labelPaint)

        // draw heatmap
        val dayPrefix = mData!!.start.weekday() - 1
        val max = mData!!.data.maxOrNull() ?: 0
        val maxLog = if (max == 0) 0.0.toFloat() else ln(max.toFloat())

        fun normalize(i: Int): Float {
            if (i == 0) {
                return 0.0.toFloat()
            }
            return ln(i.toFloat()) / maxLog
        }

        val monthXAxis = mutableListOf<Pair<Float, String>>()
        var currentMonth: Int = Calendar.getInstance().apply {
            time = mData!!.start
        }.get(Calendar.MONTH)

        for (day in 0 until mData!!.data.size) {
            val date = mData!!.start.plusDays(day)
            val loc = dayPrefix + day
            val week = loc / 7
            val weekDay = loc % 7

            if (monthXAxis.isEmpty()) {
                val xAxis = week * cellLength * 5 + cellLength * 2 + fontOffsetX
                monthXAxis.add(pairOf(xAxis, date.monthStr()))
            } else {
                val month = Calendar.getInstance().apply {
                    time = date
                }.get(Calendar.MONTH)
                if (month != currentMonth) {
                    currentMonth = month
                    val xAxis = week * cellLength * 5 + cellLength * 2 + fontOffsetX
                    monthXAxis.add(pairOf(xAxis, date.monthStr()))
                }
            }




            val leftTopX = week * cellLength * 5 + fontOffsetX
            val leftTopY = weekDay * cellLength * 5 + fontOffsetY
            val rightBottomX = leftTopX + cellLength * 4
            val rightBottomY = leftTopY + cellLength * 4
            val paint = getPaint(blendColors(normalize(mData!!.data[day])))
            canvas.drawRoundRect(
                leftTopX, leftTopY, rightBottomX, rightBottomY, cellLength, cellLength, paint
            )
        }

        // Prevent overlap
        if (monthXAxis[1].first - monthXAxis[0].first < cellLength * 10) {
            monthXAxis.removeAt(0)
        }

        for (x in monthXAxis) {
            canvas.drawText(x.second, x.first, cellLength + fontHeight / 2, labelPaint)
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
            val a = Color.alpha(whiteColor).toFloat() * inverseRatio + Color.alpha(primaryColor)
                .toFloat() * ratio
            val r = Color.red(whiteColor).toFloat() * inverseRatio + Color.red(primaryColor)
                .toFloat() * ratio
            val g = Color.green(whiteColor).toFloat() * inverseRatio + Color.green(primaryColor)
                .toFloat() * ratio
            val b = Color.blue(whiteColor).toFloat() * inverseRatio + Color.blue(primaryColor)
                .toFloat() * ratio
            return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
        }


    }

}