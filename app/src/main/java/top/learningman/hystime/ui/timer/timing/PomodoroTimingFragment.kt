package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import top.learningman.hystime.databinding.FragmentPomodoroTimingBinding
import top.learningman.hystime.ui.timer.TimerViewModel

class PomodoroTimingFragment : TimingFragment() {

    private lateinit var binding: FragmentPomodoroTimingBinding
    private val viewModel: TimerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPomodoroTimingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}