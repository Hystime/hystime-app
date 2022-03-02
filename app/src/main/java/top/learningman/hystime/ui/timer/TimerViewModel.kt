package top.learningman.hystime.ui.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import top.learningman.hystime.Constant
import top.learningman.hystime.R
import top.learningman.hystime.repo.AppRepo
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.repo.StringRepo
import top.learningman.hystime.ui.timer.TimerViewModel.TimerType.*

class TimerViewModel : ViewModel() {
    enum class TimerStatus {
        WAIT_START, // wait_start
        WORK_RUNNING, // work_running
        WORK_PAUSE, // work_pause
        WORK_FINISH, // work_finish
        BREAK_RUNNING, // break_running
        BREAK_FINISH, // break_finish
    }

    enum class TimerType {
        NORMAL,
        POMODORO,
        BREAK
    }

    private val _status = MutableLiveData(TimerStatus.WAIT_START)
    val status: LiveData<TimerStatus> = _status

    private val _type = MutableLiveData(NORMAL)
    val type: LiveData<TimerType> = _type

    fun setType(type: TimerType) {
        _type.value = type
    }

    fun setStatus(status: TimerStatus) {
        _status.postValue(status)
    }

    fun isBreak() = status.value == TimerStatus.BREAK_RUNNING

    private val _time = MutableLiveData(0L)
    val time: LiveData<Long> = _time
    private val _remainTime = MutableLiveData(0L)
    val remainTime: LiveData<Long> = _remainTime

    fun setRemainTime(remainTime: Long) {
        _remainTime.postValue(remainTime)
    }

    fun setTime(time: Long) {
        _time.postValue(time)
    }

    private fun getServiceName() = when (type.value) {
        NORMAL -> {
            StringRepo.getString(R.string.tab_normal_timing)
        }
        POMODORO -> {
            StringRepo.getString(R.string.tab_pomodoro_timing)
        }
        BREAK -> {
            StringRepo.getString(R.string.timer_break)
        }
        else -> throw Error("Unexpected type")
    }

    fun getTime() =
        when (status.value) {
            TimerStatus.WORK_RUNNING -> getFocusTime()
            TimerStatus.BREAK_RUNNING -> getBreakTime()
            else -> 0L
        }


    private fun getFocusTime() = when (type.value) {
        NORMAL -> SharedPrefRepo.getNormalFocusLength()
        POMODORO -> SharedPrefRepo.getPomodoroFocusLength()
        else -> throw Error("Unexpected type")
    } * 60L

    private var breakCount = 0 // TODO: persistent store

    private fun getBreakTime(): Long {
        return when (type.value) {
            NORMAL -> SharedPrefRepo.getNormalBreakLength()
            POMODORO -> if (breakCount <= 3) {
                breakCount++;
                SharedPrefRepo.getPomodoroShortBreakLength()
            } else {
                SharedPrefRepo.getPomodoroLongBreakLength()
            }
            else -> throw Error("Unexpected type")
        } * 60L
    }

    // Timer Actions

    private fun killTimer() {
        Log.d("TimerViewModel", "resetTimer")
        stopService()
        resetTimer()
    }

    fun resetTimer() {
        setTime(0L)
        setRemainTime(0L)
    }

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

    private fun startTimerService(duration: Long, name: String? = null) {
        val intent = Intent(AppRepo.context, TimerService::class.java)
        intent.putExtra(Constant.TIMER_DURATION_INTENT_KEY, duration)
        name?.let {
            intent.putExtra(Constant.TIMER_NAME_INTENT_KEY, name)
        }
        Log.d("startService", "bindService")
        AppRepo.context.bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun stopService() {
        Log.d("stopService", "unbindService")
        binder?.cancel()
        binder = null
    }

    fun unbind() {
        AppRepo.context.unbindService(connection)
    }
}