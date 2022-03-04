// From https://github.com/c05mic/pause-resume-timer with modification
package top.learningman.hystime.utils

import android.util.Log
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
    private val onTick: ((Long) -> Unit),
    private val onFinish: ((Long) -> Unit),
    private val duration: Long = -1,
) {

    private var interval = 1000L // 1 second

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
            try {
                elapsedTime += interval
                Log.d("Timer", "onTick $elapsedTime")
                onTick.invoke(elapsedTime)
                if (duration > 0) {
                    if (elapsedTime >= duration) {
                        onFinish.invoke(duration - elapsedTime)
                        future!!.cancel(true)
                    }
                }
            } catch (e: Throwable) {
                Log.e("Timer", "onTick error", e)
            }
        }, interval, interval, TimeUnit.MILLISECONDS)
    }

    /**
     * Paused the timer. If the timer is not running, this call is ignored.
     */
    fun pause() {
        if (!isRunning) return
        future?.cancel(false)
        isRunning = false
    }


    /**
     * Stops the timer. If the timer is not running, then this call does nothing.
     */
    fun cancel() {
        pause()
        onFinish.invoke(duration - elapsedTime)
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