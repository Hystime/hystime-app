package top.learningman.hystime.ui.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    enum class TimerStatus {
        WAIT_START, // wait_start
        WORK_RUNNING, // work_running
        WORK_PAUSE, // work_pause
        WORK_FINISH, // work_finish
        BREAK_RUNNING, // break_running
        BREAK_FINISH, // break_finish
    }

    private val _status = MutableLiveData(TimerStatus.WAIT_START)
    val status: LiveData<TimerStatus> = _status

    fun setStatus(status: TimerStatus) {
        _status.postValue(status)
    }

    private val _time = MutableLiveData(0L)
    val time: LiveData<Long> = _time

    fun setTime(time: Long) {
        _time.postValue(time)
    }
}