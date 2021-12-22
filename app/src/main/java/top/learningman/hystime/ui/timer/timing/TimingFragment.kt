package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import top.learningman.hystime.R
import top.learningman.hystime.ui.timer.TimerViewModel
import top.learningman.hystime.ui.timer.TimerViewModel.TimerStatus.*
import top.learningman.hystime.view.TimerView


open class TimingFragment : Fragment() {
    private val viewModel: TimerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timer = view.findViewById<TimerView>(R.id.timer)
        timer.viewModel = viewModel

        viewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                WORK_RUNNING -> {
                    if (timer.isPause()) {
                        timer.resume()
                    } else {
                        timer.start(viewModel.getTime())
                    }
                }
                BREAK_RUNNING -> {
                    timer.start(viewModel.getTime())
                }
                WORK_PAUSE -> {
                    timer.pause()
                }
                WORK_FINISH -> {
                    timer.cancel()
                }
                BREAK_FINISH -> {
                    timer.cancel()
                    timer.isFocus()
                }
                WAIT_START -> {
                    if (timer.isStarted()) {
                        timer.cancel()
                    }
                    timer.isFocus()
                }
                null -> throw Error("TimerViewModel.status is null")
            }
        }
    }
}