package top.learningman.hystime.ui.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.ui.timer.TimerViewModel.TimerType.*

class TimerViewModel : ViewModel() {
    enum class TimerStatus {
        WAIT_START, // wait_start
        WORK_RUNNING, // work_running
        WORK_FINISH, // work_finish
        BREAK_RUNNING, // break_running
        BREAK_FINISH, // break_finish
    }

    enum class TimerType {
        NORMAL,
        POMODORO,
        NORMAL_BREAK,
        POMODORO_BREAK;

        fun isBreak(): Boolean {
            return this == NORMAL_BREAK || this == POMODORO_BREAK
        }
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

    private var breakCount = 0 // TODO: persistent store

    private fun isLongBreak() = breakCount > 3

    fun updateBreakCount() {
        if (breakCount < 4) {
            breakCount++
        } else {
            breakCount = 0
        }
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
        else -> throw Error("Unexpected type ${type.value.toString()}")
    } * 60L


    private fun getBreakTime(): Long {
        return when (type.value) {
            NORMAL_BREAK -> SharedPrefRepo.getNormalBreakLength()
            POMODORO_BREAK -> if (isLongBreak()) {
                SharedPrefRepo.getPomodoroLongBreakLength()
            } else {
                SharedPrefRepo.getPomodoroShortBreakLength()
            }
            else -> throw Error("Unexpected type")
        } * 60L
    }
}