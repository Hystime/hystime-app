package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import top.learningman.hystime.databinding.FragmentNormalTimingBinding

class NormalTimerViewFragment : TimerViewFragment() {
    private lateinit var binding: FragmentNormalTimingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNormalTimingBinding.inflate(inflater, container, false)
        return binding.root
    }
}