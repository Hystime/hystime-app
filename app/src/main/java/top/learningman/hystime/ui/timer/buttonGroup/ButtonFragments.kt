package top.learningman.hystime.ui.timer.buttonGroup

import android.content.Context
import android.view.View
import com.airbnb.paris.extensions.style
import top.learningman.hystime.R

object ButtonFragments {
    enum class BreakLength{
        SHORT,
        LONG
    }

    val WAIT_START = object : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button1.visibility = View.GONE

            binding.button0.apply {
                text = context.getString(R.string.start)
                // TODO: onClick
            }
        }
    }

    val WORK_RUNNING = object : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button1.visibility = View.GONE

            binding.button0.apply {
                text = context.getString(R.string.pause)
            }
        }
    }

    val WORK_PAUSE = object : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button0.apply {
                text = context.getString(R.string.resume)
            }

            binding.button1.apply {
                text = context.getString(R.string.exit)
                style(R.style.Widget_MaterialComponents_Button_OutlinedButton)
            }
        }
    }

    val WORK_FINISH = object : ButtonFragment() {
        override fun bind(context: Context) {
            binding.button0.apply {
                text = context.getString(R.string.)
            }

            binding.button1.apply {
                text = context.getString(R.string.exit)
                style(R.style.Widget_MaterialComponents_Button_OutlinedButton)
            }
        }
    }
}