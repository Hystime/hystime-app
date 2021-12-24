package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import top.learningman.hystime.databinding.FragmentPomodoroTimingBinding

class PomodoroTimerViewFragment : TimerViewFragment() {

    private lateinit var binding: FragmentPomodoroTimingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPomodoroTimingBinding.inflate(inflater, container, false)
        return binding.root
    }
}