package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import top.learningman.hystime.databinding.FragmentFinishBinding
import top.learningman.hystime.ui.timer.TimerViewModel
import top.learningman.hystime.ui.timer.TimerViewModel.TimerStatus.*


class FinishFragment : Fragment() {

    lateinit var binding: FragmentFinishBinding

    private val timerViewModel: TimerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFinishBinding.inflate(inflater, container, false)

        when (timerViewModel.status.value) {
            WORK_RUNNING -> {
                listOf(binding.workFinish, binding.workFinishHint).forEach {
                    it.visibility = View.VISIBLE
                }
            }
            BREAK_RUNNING -> {
                listOf(binding.breakFinish, binding.breakFinishHint).forEach {
                    it.visibility = View.VISIBLE
                }
            }
            else -> {
                Log.e("FinishFinishFragment", "Unexpected status: ${timerViewModel.status.value}")
            }
        }

        return binding.root
    }

}