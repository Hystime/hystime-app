package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import top.learningman.hystime.MainViewModel
import top.learningman.hystime.R
import top.learningman.hystime.databinding.FragmentNormalTimingBinding
import top.learningman.hystime.ui.timer.TimerViewModel

class NormalTimingFragment : TimingFragment() {

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
        mainViewModel.currentTarget.observe(viewLifecycleOwner){
            binding.target.text = it?.name?:getString(R.string.no_target)
        }
    }

}