package top.learningman.hystime.ui.timer

class TimerViewModel {
    enum class TimerStatus{
        WAIT_START,
        WORK_RUNNING,
        WORK_PAUSE,
        WORK_FINISH_NORMAL,
        WORK_FINISH_SHORT,
        WORK_FINISH_LONG,
        BREAK_SKIP,
        BREAK_FINISH
    }
}