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
            }
        }
    }

    class WorkRunningFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button1.visibility = View.GONE

            binding.button0.apply {
                text = context.getString(R.string.pause)
            }
        }
    }

    class WorkPauseFragment : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button0.apply {
                text = context.getString(R.string.resume)
            }

            binding.button1.apply {
                visibility = View.VISIBLE
                text = context.getString(R.string.exit)
            }
        }
    }

    class WorkFinishFragment : ButtonFragment() {
        override fun bind(context: Context) {
            isBreak()
            binding.button0.apply {
                text = context.getString(R.string.break_start)
            }

            binding.button1.apply {
                visibility = View.VISIBLE
                text = context.getString(R.string.break_skip)
            }

            binding.button2.apply {
                visibility = View.VISIBLE
                text = context.getString(R.string.exit)
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