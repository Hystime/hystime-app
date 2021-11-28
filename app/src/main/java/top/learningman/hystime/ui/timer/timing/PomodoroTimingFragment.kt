package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.learningman.hystime.databinding.FragmentPomodoroTimingBinding
import top.learningman.hystime.ui.timer.TimerViewModel

class PomodoroTimingFragment : Fragment() {

    private val model: TimerViewModel by lazy {
        ViewModelProvider(requireActivity())[TimerViewModel::class.java]
    }

    private lateinit var binding: FragmentPomodoroTimingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPomodoroTimingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.target.observe(viewLifecycleOwner) { target ->
            binding.target.text = target
        }
    }


}