package top.learningman.hystime.ui.timer.buttonGroup

import android.content.Context
import android.view.View
import top.learningman.hystime.R
import top.learningman.hystime.ui.timer.TimerViewModel.TimerStatus
object ButtonFragments {

    class WaitStartFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button1.visibility = View.GONE

            binding.button0.apply {
                text = context.getString(R.string.start)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.WORK_RUNNING)
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
                }
            }

            binding.button1.apply {
                text = context.getString(R.string.exit)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.WAIT_START)
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
                }
            }

            binding.button1.apply {
                text = context.getString(R.string.break_skip)
                setOnClickListener {
                    viewModel.setStatus(TimerStatus.WAIT_START)
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
            }

            binding.button1.apply {
                visibility = View.VISIBLE
                text = context.getString(R.string.exit)
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