package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.learningman.hystime.databinding.FragmentNormalTimingBinding
import top.learningman.hystime.ui.timer.TimerViewModel

class NormalTimingFragment : Fragment() {

    private val viewModel: TimerViewModel by lazy {
        ViewModelProvider(requireActivity())[TimerViewModel::class.java]
    }

    private lateinit var binding: FragmentNormalTimingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNormalTimingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}