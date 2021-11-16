package top.learningman.hystime.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import top.learningman.hystime.R

class TimerView(context: Context) : View(context) {

    constructor(context: Context, attrs: AttributeSet) : this(context)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs)


    private val mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.md_theme_light_primary)
        strokeWidth = 40F
        pathEffect = DashPathEffect(floatArrayOf(5f, 15f), 1f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f;
        val cy = height / 2f;
        val radius = cx.coerceAtMost(cy) / 1.5f;

        canvas.drawCircle(cx, cy, radius, mCirclePaint)
    }
}