// From https://github.com/c05mic/pause-resume-timer with modify
package top.learningman.hystime.utils

import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 *
 * A abstract timer utility class, that supports pause and resume.
 *
 * Can be used either as a normal timer or a countdown timer.
 *
 */
class Timer constructor(
    private val interval: Long = 1000,
    private val duration: Long = -1,
    onTick: (() -> Unit),
    onFinish: (() -> Unit)
) {

    var onTick: (() -> Unit)
    var onFinish: (() -> Unit)

    init {
        this.onTick = onTick
        this.onFinish = onFinish
    }


    /**
     * @return true if the timer is currently running, and false otherwise.
     */
    @Volatile
    var isRunning = false
        private set

    /**
     * @return the elapsed time (in millis) since the start of the timer.
     */
    var elapsedTime: Long = 0
        private set

    private val execService = Executors.newSingleThreadScheduledExecutor()
    private var future: Future<*>? = null

    /**
     * Starts the timer. If the timer was already running, this call is ignored.
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        future = execService.scheduleWithFixedDelay({
            onTick.invoke()
            elapsedTime += interval
            if (duration > 0) {
                if (elapsedTime >= duration) {
                    onFinish.invoke()
                    future!!.cancel(false)
                }
            }
        }, 0, interval, TimeUnit.MILLISECONDS)
    }

    /**
     * Paused the timer. If the timer is not running, this call is ignored.
     */
    fun pause() {
        if (!isRunning) return
        future!!.cancel(false)
        isRunning = false
    }

    /**
     * Resumes the timer if it was paused, else starts the timer.
     */
    fun resume() {
        start()
    }


    /**
     * Stops the timer. If the timer is not running, then this call does nothing.
     */
    fun cancel() {
        pause()
        elapsedTime = 0
    }

    /**
     * @return the time remaining (in millis) for the timer to stop. If the duration was set to `Timer#DURATION_INFINITY`, then -1 is returned.
     */
    val remainingTime: Long
        get() = if (duration < 0) {
            DURATION_INFINITY.toLong()
        } else {
            duration - elapsedTime
        }

    companion object {
        const val DURATION_INFINITY = -1
    }
}