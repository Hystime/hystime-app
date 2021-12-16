package top.learningman.hystime.ui.timer.buttonGroup

import android.content.Context
import android.view.View
import top.learningman.hystime.R
import top.learningman.hystime.repo.SharedPreferenceRepository
import top.learningman.hystime.ui.timer.TimerViewModel
import top.learningman.hystime.ui.timer.TimerViewModel.TimerStatus

object ButtonFragments {

    class WaitStartFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button1.visibility = View.GONE

            binding.button0.apply {
                text = context.getString(R.string.start)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.WORK_RUNNING)
                    val duration: Long
                    val name: String
                    when (viewModel.type.value) {
                        TimerViewModel.TimerType.NORMAL -> {
                            duration = SharedPreferenceRepository.getNormalFocusLength().toLong()
                            name = getString(R.string.tab_normal_timing)
                        }
                        TimerViewModel.TimerType.POMODORO -> {
                            duration = SharedPreferenceRepository.getPomodoroFocusLength().toLong()
                            name = getString(R.string.tab_pomodoro_timing)
                        }
                        else -> throw Error("Unknown type")
                    }
                    startService(
                        requireContext(),
                        duration,
                        name
                    )
                }
            }
        }
    }

    class WorkRunningFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button1.visibility = View.GONE

            binding.button0.apply {
                text = context.getString(R.string.pause)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.WORK_PAUSE)
                    binder?.pause()
                }
            }
        }
    }

    class WorkPauseFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button0.apply {
                text = context.getString(R.string.resume)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.WORK_RUNNING)
                    binder?.start()
                }
            }

            binding.button1.apply {
                text = context.getString(R.string.exit)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.WAIT_START)
                    binder?.cancel()
                    stopService(requireContext())
                }
            }
        }
    }

    class WorkFinishFragment : ButtonFragment() {
        override fun bind(context: Context) {
            isBreak()
            binding.button0.apply {
                text = context.getString(R.string.break_start)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.BREAK_RUNNING)
                    val duration: Long
                    val name: String

                }
            }

            binding.button1.apply {
                text = context.getString(R.string.break_skip)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.BREAK_FINISH)
                }
            }

            binding.button2.apply {
                visibility = View.VISIBLE
                text = context.getString(R.string.exit)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.WAIT_START) // If pomodoro, reset counter
                }
            }
        }
    }

    class BreakRunningFragment : ButtonFragment() {
        override fun bind(context: Context) {
            isBreak()
            binding.button0.apply {
                text = context.getString(R.string.skip)
                viewModel.setStatus(TimerStatus.BREAK_FINISH)
            }

            binding.button1.apply {
                text = context.getString(R.string.exit)
                viewModel.setStatus(TimerStatus.WAIT_START) // If pomodoro
            }
        }
    }

    class BreakFinishFragment : ButtonFragment() {
        override fun bind(context: Context) {
            isBreak()
            binding.button0.apply {
                text = context.getString(R.string.work_continue)
            }
        }
    }
}