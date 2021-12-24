package top.learningman.hystime.ui.timer.buttonGroup

import android.content.Context
import android.view.View
import top.learningman.hystime.R

object ButtonFragments {
    class WaitStartFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button1.visibility = View.GONE

            binding.button0.apply {
                text = context.getString(R.string.start)
                setOnClickListener {
                    if (mainViewModel.currentTarget.value == null) {
                        mainViewModel.showSnackBarMessage(context.getString(R.string.no_target_hint))
                        return@setOnClickListener
                    }
                    viewModel.startFocus()
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
                    viewModel.pauseFocus()
                }
            }
        }
    }

    class WorkPauseFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button0.apply {
                text = context.getString(R.string.resume)
                setOnClickListener {
                    viewModel.resumeFocus()
                }
            }

            binding.button1.apply {
                text = context.getString(R.string.exit)
                setOnClickListener {
                    viewModel.exitAll()
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
                    viewModel.startBreak()
                }
            }

            binding.button1.apply {
                text = context.getString(R.string.break_skip)
                setOnClickListener {
                    viewModel.skipBreak()
                }
            }

            binding.button2.apply {
                visibility = View.VISIBLE
                text = context.getString(R.string.exit)
                setOnClickListener {
                    viewModel.exitAll()
                }
            }
        }
    }

    class BreakRunningFragment : ButtonFragment() {
        override fun bind(context: Context) {
            isBreak()
            binding.button0.apply {
                text = context.getString(R.string.skip)
                setOnClickListener {
                    viewModel.skipBreak()
                }
            }

            binding.button1.apply {
                text = context.getString(R.string.exit)
                setOnClickListener {
                    viewModel.exitAll()
                }
            }
        }
    }

    class BreakFinishFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button0.apply {
                text = context.getString(R.string.work_continue)
                setOnClickListener {
                    viewModel.startFocus()
                }
            }

            binding.button1.apply {
                text = context.getString(R.string.exit)
                setOnClickListener {
                    viewModel.exitAll()
                }
            }
        }
    }
}