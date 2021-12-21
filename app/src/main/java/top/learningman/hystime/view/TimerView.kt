package top.learningman.hystime.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import top.learningman.hystime.R

class TimerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    enum class TimerViewType {
        NORMAL,
        POMODORO
    }

    class ProgressAnimation(val view: TimerView, newAngle: Float) : Animation() {
        private var diffAngle: Float

        init {
            diffAngle = newAngle - view.lastAngle
            view.lastAngle = newAngle
            Log.d("TimerView", "diffAngle: $diffAngle, newAngle $newAngle, angle ${view.angle}")
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            view.angle += diffAngle * interpolatedTime
        }
    }

    private var cx: Float = 0f
    private var cy: Float = 0f
    private var radius: Float = 0f

    private var mType: TimerViewType
    private var mArcRectF: RectF = RectF()
    var lastAngle = 0f
    var angle = 0f
        set(value) {
            field = value
            postInvalidate()
        }

    private var mCurrentPaint: Paint
    private var mCurrentBasePaint: Paint

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

        when (mType) {
            TimerViewType.NORMAL -> {
                mCurrentPaint = mNormalCirclePaint
                mCurrentBasePaint = mNormalBaseCirclePaint
            }
            TimerViewType.POMODORO -> {
                mCurrentPaint = mPomodoroCirclePaint
                mCurrentBasePaint = mPomodoroBaseCirclePaint
            }
        }

    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : this(
        context,
        attrs,
        defStyleAttr
    )

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        cx = width / 2f
        cy = height / 2f
        radius = cx.coerceAtMost(cy) - 20
        mArcRectF.set(cx - radius, cy - radius, cx + radius, cy + radius)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(cx, cy, radius, mCurrentBasePaint)
        if (angle != 0f) {
            canvas.drawArc(mArcRectF, -90f, angle, false, mCurrentPaint)
        }
    }

    // FIXME: correct animation
    fun setAngleWithAnimation(angle: Float) {
        Log.d("Animation", "setAngleWithAnimation angle $angle")
        val progressAnimation = ProgressAnimation(this, angle)
        progressAnimation.duration = 1000
        progressAnimation.interpolator = LinearInterpolator()
        progressAnimation.fillAfter = false
        progressAnimation.fillBefore = false
        startAnimation(progressAnimation)
    }
}