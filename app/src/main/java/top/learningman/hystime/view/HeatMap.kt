package top.learningman.hystime.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import top.learningman.hystime.utils.weekday
import java.util.*
import kotlin.math.ceil

class HeatMap(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val mData: Cal? = null

    private var ceilWidth: Int = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : this(
        context,
        attrs,
        defStyleAttr
    )


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (mData == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val day = mData.start.weekday()
            val dayCount = mData.data.size + (day - 1)
            val weekCount = ceil(dayCount.toDouble() / 7).toInt()
            val ceilCount = weekCount * 5
            // 4 ceil for a time block and 1 ceil for slide
            val ceilEdge = width / ceilCount
            this.ceilWidth = ceilEdge

            val newHeight = ceilEdge * (7 * 4 + 6)
            setMeasuredDimension(width, newHeight)
        }

    }

    companion object {
        data class Cal(
            val start: Date,
            val data: List<Int?>
        )
    }


}