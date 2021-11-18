package top.learningman.hystime.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import top.learningman.hystime.R

enum class TimerViewType {
    NORMAL,
    POMODORO
}

class TimerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private lateinit var mType: TimerViewType
    private var mArcRectF: RectF? = null
    private var angle = 0f

    private val mPomodoroBaseCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.timing_circle_base)
        strokeWidth = 40F
        pathEffect = DashPathEffect(floatArrayOf(5f, 15f), 1f)
    }

    private val mPomodoroCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.timing_circle)
        strokeWidth = 40F
        pathEffect = DashPathEffect(floatArrayOf(5f, 15f), 1f)
    }

    private val mNormalBaseCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.timing_circle_base)
        strokeWidth = 20F
    }

    private val mNormalCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.timing_circle)
        strokeWidth = 20F
    }

    init {
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

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : this(
        context,
        attrs,
        defStyleAttr
    )

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = cx.coerceAtMost(cy) - 20

        if (mArcRectF == null) {
            mArcRectF = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        }

        when (mType) {
            TimerViewType.NORMAL -> {
                canvas.drawCircle(cx, cy, radius, mNormalBaseCirclePaint)
                canvas.drawArc(mArcRectF!!, -90f, angle, false, mNormalCirclePaint)
            }
            TimerViewType.POMODORO -> {
                canvas.drawCircle(cx, cy, radius, mPomodoroBaseCirclePaint)
                canvas.drawArc(mArcRectF!!, -90f, angle, false, mPomodoroCirclePaint)
            }
        }
    }

    fun update(angle: Float) {
        this.angle = angle
        postInvalidate()
    }

    fun update(percent: Int) {
        this.angle = percent * 3.6f
        postInvalidate()
    }
}