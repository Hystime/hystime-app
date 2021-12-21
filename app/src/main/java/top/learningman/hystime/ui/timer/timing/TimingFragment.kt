package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import top.learningman.hystime.ui.timer.TimerViewModel
import top.learningman.hystime.R
import top.learningman.hystime.view.TimerView


open class TimingFragment : Fragment() {
    private val viewModel: TimerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.time.observe(viewLifecycleOwner) {
            val allTime = viewModel.getTime()
            if (it != 0L) {
                val angle = it.toFloat() / allTime * 360
                view.findViewById<TimerView>(R.id.timer).setAngleWithAnimation(angle)
            }
        }
    }
}