package top.learningman.hystime.ui.timer

import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    enum class TimerStatus {
        WAIT_START, // wait_start
        WORK_RUNNING, // work_running
        WORK_PAUSE, // work_pause
        WORK_FINISH_SHORT, // work_finish(short)
        WORK_FINISH_LONG, // work_finish(long)
        BREAK_RUNNING, // break_running
        BREAK_FINISH_LONG, // break_finish(long)
        BREAK_FINISH_SHORT // break_finish(short)
    }
}