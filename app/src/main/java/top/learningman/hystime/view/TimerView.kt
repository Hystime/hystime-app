package top.learningman.hystime.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import top.learningman.hystime.R
import kotlin.math.min

enum class TimerViewType {
    NORMAL,
    POMODORO
}

class TimerView(context: Context) : View(context) {
    private lateinit var mType: TimerViewType

    private val mPomodoroBaseCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.md_theme_light_primaryContainer)
        strokeWidth = 40F
        pathEffect = DashPathEffect(floatArrayOf(5f, 15f), 1f)
    }

    private val mPomodoroCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.md_theme_light_primary)
        strokeWidth = 40F
        pathEffect = DashPathEffect(floatArrayOf(5f, 15f), 1f)
    }

    private val mNormalBaseCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.md_theme_light_inverseOnSurface)
        strokeWidth = 20F
    }

    private val mNormalCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.md_theme_light_primary)
        strokeWidth = 20F
    }

    constructor(context: Context, attrs: AttributeSet) : this(context) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimerView)
        mType = typedArray.getInt(R.styleable.TimerView_type, 0).let {
            when (it) {
                0 -> TimerViewType.NORMAL
                1 -> TimerViewType.POMODORO
                else -> TimerViewType.NORMAL
            }
        }
        typedArray.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : this(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f;
        val cy = height / 2f;
        val radius = cx.coerceAtMost(cy) / 1.3f;

        when (mType) {
            TimerViewType.NORMAL -> {
                canvas.drawCircle(cx, cy, radius, mNormalBaseCirclePaint)
            }
            TimerViewType.POMODORO -> {
                canvas.drawCircle(cx, cy, radius, mPomodoroBaseCirclePaint)
            }
        }

    }
}