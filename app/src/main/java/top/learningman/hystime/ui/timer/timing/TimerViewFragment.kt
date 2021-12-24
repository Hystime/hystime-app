package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import top.learningman.hystime.R
import top.learningman.hystime.ui.timer.TimerViewModel
import top.learningman.hystime.ui.timer.TimerViewModel.TimerStatus.*
import top.learningman.hystime.view.TimerView


open class TimerViewFragment : Fragment() {
    private val viewModel: TimerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timerView = view.findViewById<TimerView>(R.id.timer)
        timerView.viewModel = viewModel

        viewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                WORK_RUNNING -> {
                    if (timerView.isPause()) {
                        timerView.resume()
                    } else {
                        timerView.start(viewModel.getTime())
                    }
                }
                BREAK_RUNNING -> {
                    timerView.start(viewModel.getTime())
                }
                WORK_PAUSE -> {
                    timerView.pause()
                }
                WORK_FINISH -> {
                    timerView.cancel()
                }
                BREAK_FINISH -> {
                    timerView.cancel()
                    timerView.isFocus()
                }
                WAIT_START -> {
                    if (timerView.isStarted()) {
                        timerView.cancel()
                    }
                    timerView.isFocus()
                }
                null -> throw Error("TimerViewModel.status is null")
            }
        }
    }
}