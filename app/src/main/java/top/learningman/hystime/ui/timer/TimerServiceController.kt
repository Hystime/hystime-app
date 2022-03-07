package top.learningman.hystime.ui.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import top.learningman.hystime.Constant

class TimerServiceController(private val context: Context) {
    var binder: TimerService.TimerBinder? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("onServiceConnected", "Connected $name")
            binder = service as TimerService.TimerBinder
            binder!!.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("onServiceDisconnected", "Disconnected")
            binder = null // FIXME: handle service crash
        }
    }

    fun pause() {
        Log.i("binderPause", "pause called")
        binder?.pause() ?: Log.e("binderPause", "binder is null, connect status: $isConnected")
    }

    fun resume() {
        Log.i("binderResume", "resume called")
        binder?.start() ?: Log.e("bidnerResume", "binder is null, connect status: $isConnected")
    }

    fun cancel() {
        Log.i("binderCancel", "cancel called")
        binder?.cancel() ?: Log.e("binderCancel", "binder is null, connect status: $isConnected")
    }

    var isConnected: Boolean = false
    fun startTimerService(duration: Long, type: TimerViewModel.TimerType, name: String) {
        val intent = Intent(context, TimerService::class.java).apply {
            putExtra(Constant.TIMER_DURATION_INTENT_KEY, duration * 1000)
            putExtra(Constant.TIMER_NAME_INTENT_KEY, name)
            putExtra(Constant.TIMER_TYPE_INTENT_KEY, type)
        }

        isConnected = context.bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE
        )

        Log.d("startService", "bindService for $name, Connect = $isConnected")
        if (!isConnected) {
            Log.e("startService", "bindService failed")
            throw RuntimeException("bindService failed")
        }
    }

    fun unbindTimerService() {
        if (isConnected) {
            context.unbindService(connection)
            isConnected = false
            binder = null
        } else {
            Log.e("unbindTimerService", "not connected")
        }
    }

    companion object {
        object TimerController {
            private fun getActionIntent(action: String): Intent {
                Log.i("TimerFragment", "getActionIntent: $action")
                return Intent(action)
            }

            fun pauseTimer(context: Context) {
                getActionIntent(Constant.TIMER_FRAGMENT_PAUSE_ACTION).let {
                    context.sendBroadcast(it)
                    Log.i("TimerFragment", "send broadcast for pause")
                }
            }

            fun resumeTimer(context: Context) {
                getActionIntent(Constant.TIMER_FRAGMENT_RESUME_ACTION).let {
                    context.sendBroadcast(it)
                    Log.i("TimerFragment", "send broadcast for resume")
                }
            }

            fun killTimer(context: Context) {
                getActionIntent(Constant.TIMER_FRAGMENT_CANCEL_ACTION).let {
                    context.sendBroadcast(it)
                    Log.i("TimerFragment", "send broadcast for kill")
                }
            }
        }
    }
}