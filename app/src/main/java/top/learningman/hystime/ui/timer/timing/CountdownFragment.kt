package top.learningman.hystime.ui.timer.timing

import androidx.fragment.app.Fragment

enum class CountdownType {
    NORMAL,
    POMODORO,
    SHORT_BREAK,
    LONG_BREAK
}

class CountdownFragment(private val type: CountdownType) : Fragment() {
}
