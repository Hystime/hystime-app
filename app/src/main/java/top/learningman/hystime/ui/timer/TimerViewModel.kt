package top.learningman.hystime.ui.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import top.learningman.hystime.Constant
import top.learningman.hystime.R
import top.learningman.hystime.repo.AppRepo
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.repo.StringRepo
import top.learningman.hystime.ui.timer.TimerViewModel.TimerType.NORMAL
import top.learningman.hystime.ui.timer.TimerViewModel.TimerType.POMODORO

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
        POMODORO
    }

    private val _status = MutableLiveData(TimerStatus.WAIT_START)
    val status: LiveData<TimerStatus> = _status

    private val _type = MutableLiveData(NORMAL)
    val type: LiveData<TimerType> = _type

    fun setType(type: TimerType) {
        _type.value = type
    }

    private fun setStatus(status: TimerStatus) {
        _status.postValue(status)
    }

    private val _time = MutableLiveData(0L)
    val time: LiveData<Long> = _time

    fun setTime(time: Long) {
        _time.postValue(time)
    }

    private fun getServiceName(isBreak: Boolean = false) = when (type.value) {
        NORMAL -> {
            StringRepo.getString(R.string.tab_normal_timing)
        }
        POMODORO -> {
            StringRepo.getString(R.string.tab_pomodoro_timing)
        }
        else -> throw Error("Unexpected type")
    } + if (isBreak) " ${StringRepo.getString(R.string.timer_break)}" else ""

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

    fun exitAll() {
        setStatus(TimerStatus.WAIT_START)
        if (binder != null) {
            binder?.cancel()
            binder = null
            stopService()
        }
    }

    fun startFocus() {
        setStatus(TimerStatus.WORK_RUNNING)
        startService(getFocusTime(), getServiceName())
    }

    fun pauseFocus() {
        setStatus(TimerStatus.WORK_PAUSE)
        binder?.pause()
    }

    fun resumeFocus() {
        status.value?.let {
            if (it == TimerStatus.WORK_PAUSE) {
                setStatus(TimerStatus.WORK_RUNNING)
                binder?.resume()
            }
        }
    }

    fun cancelFocus() {
        binder?.cancel()
    }

    fun startBreak() {
        setStatus(TimerStatus.BREAK_RUNNING)
        startService(getBreakTime(), getServiceName())
    }

    fun skipBreak() {
        if (binder != null) {
            resetTimer()
        } else {
            setStatus(TimerStatus.BREAK_FINISH)
        }
    }

    fun resetTimer() {
        stopService()
        binder = null
        setTime(0L)
        when (status.value) {
            TimerStatus.WORK_RUNNING -> {
                setStatus(TimerStatus.WORK_FINISH)
            }
            TimerStatus.WORK_PAUSE -> {
                setStatus(TimerStatus.WAIT_START)
            }
            TimerStatus.BREAK_RUNNING -> {
                setStatus(TimerStatus.BREAK_FINISH)
            }
            else -> {
                throw Error("Service died unexpected.")
            }
        }
    }

    var binder: TimerService.TimerBinder? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as TimerService.TimerBinder
            binder!!.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            resetTimer()
        }
    }

    private fun startService(duration: Long, name: String? = null) {
        val intent = Intent(AppRepo.context, TimerService::class.java)
        intent.putExtra(Constant.TIMER_DURATION_INTENT_KEY, duration)
        name?.let {
            intent.putExtra(Constant.TIMER_NAME_INTENT_KEY, name)
        }
        AppRepo.context.bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun stopService() {
        AppRepo.context.unbindService(connection)
    }
}