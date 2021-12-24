package top.learningman.hystime.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import top.learningman.hystime.R
import top.learningman.hystime.repo.AppRepo
import top.learningman.hystime.ui.timer.TimerViewModel

class TimerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    lateinit var viewModel: TimerViewModel

    enum class TimerViewType {
        NORMAL,
        POMODORO
    }

    private var cx: Float = 0f
    private var cy: Float = 0f
    private var radius: Float = 0f

    private var mType: TimerViewType
    private var mArcRectF: RectF = RectF()

    var angle = 0f

    private var mCurrentPaint: Paint
    private var mCurrentBasePaint: Paint

    private var isBreak = false

    fun isBreak() {
        isBreak = true
        updatePaintColor()
    }

    fun isFocus() {
        isBreak = false
        updatePaintColor()
    }

    private fun getColor(): Int {
        return if (isBreak) {
            AppRepo.context.getColor(R.color.relax_color)
        } else {
            AppRepo.context.getColor(R.color.timing_circle)
        }
    }

    private fun getBaseColor(): Int {
        return if (isBreak) {
            AppRepo.context.getColor(R.color.relax_color_base)
        } else {
            AppRepo.context.getColor(R.color.timing_circle_base)
        }
    }

    private fun updatePaintColor() {
        listOf(mPomodoroCirclePaint, mNormalCirclePaint).forEach {
            it.color = getColor()
        }
        listOf(mPomodoroBaseCirclePaint, mNormalBaseCirclePaint).forEach {
            it.color = getBaseColor()
        }
        postInvalidate()
    }

    private var mPomodoroBaseCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.timing_circle_base)
        strokeWidth = 40F
        pathEffect = DashPathEffect(floatArrayOf(5f, 15f), 0f)
    }

    private var mPomodoroCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.timing_circle)
        strokeWidth = 40F
        pathEffect = DashPathEffect(floatArrayOf(5f, 15f), -10f)
    }

    private var mNormalBaseCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = context.getColor(R.color.timing_circle_base)
        strokeWidth = 20F
    }

    private var mNormalCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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

    var animation: ObjectAnimator? = null

    fun start(time: Long) {
        if (animation != null) {
            animation?.cancel()
        }
        animation = ObjectAnimator().apply {
            setObjectValues(0f, 360f)
            duration = time * 1000L
            interpolator = LinearInterpolator()
            addUpdateListener {
                angle = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                private fun reset(update: Boolean = false) {
                    angle = 0f
                    if (update) {
                        if (isBreak) {
                            isFocus()
                        } else {
                            isBreak()
                        }
                    }
                    invalidate()
                    animation = null
                }

                override fun onAnimationEnd(animator: Animator?) {
                    super.onAnimationEnd(animator)
                    reset(true)
                }

                override fun onAnimationCancel(animator: Animator?) {
                    super.onAnimationCancel(animator)
                    reset()
                }
            })
            start()
        }
    }

    fun pause() {
        animation?.pause()
    }

    fun resume() {
        animation?.resume()
    }

    fun cancel() {
        animation?.cancel()
    }

    fun isStarted(): Boolean {
        return animation?.isStarted ?: false
    }

    fun isPause(): Boolean {
        return animation?.isPaused ?: false
    }

}