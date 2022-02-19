package top.learningman.hystime.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import top.learningman.hystime.utils.weekday
import java.util.*
import kotlin.math.ceil

class HeatMapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var mData: Cal? = null

    private var ceilWidth: Int = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : this(
        context,
        attrs,
        defStyleAttr
    )

    fun setData(data: Cal) {
        mData = data
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mData == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val day = mData!!.start.weekday()
            val dayCount = mData!!.data.size + (day - 1)
            val weekCount = ceil(dayCount.toDouble() / 7).toInt()
            val ceilCount = weekCount * 5
            // 4 ceil for a time block and 1 ceil for slide
            val ceilEdge = width / ceilCount
            this.ceilWidth = ceilEdge

            val height = ceilEdge * (7 * 4 + 6)
            setMeasuredDimension(width, height)
        }
    }

    override fun onDraw(canvas: Canvas) {

    }

    companion object {
        data class Cal(
            val start: Date,
            val data: List<Int?>
        )
    }

}