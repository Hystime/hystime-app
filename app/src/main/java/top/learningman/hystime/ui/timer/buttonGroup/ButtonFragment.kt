package top.learningman.hystime.ui.timer.buttonGroup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import top.learningman.hystime.R
import top.learningman.hystime.databinding.ButtonFragmentBinding
import top.learningman.hystime.ui.timer.TimerViewModel

abstract class ButtonFragment : Fragment() {
    lateinit var binding: ButtonFragmentBinding
    val viewModel: TimerViewModel by activityViewModels()

    abstract fun bind(context: Context)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ButtonFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(requireContext())
    }

    fun isBreak() {
        binding.button0.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.relax_color
            )
        )
        listOf(binding.button1, binding.button2).forEach {
            it.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.relax_color)
            it.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.relax_color))
        }
    }
}