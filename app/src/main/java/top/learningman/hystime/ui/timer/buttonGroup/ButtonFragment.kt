package top.learningman.hystime.ui.timer.buttonGroup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.learningman.hystime.databinding.ButtonFragmentBinding
import top.learningman.hystime.ui.timer.TimerViewModel

abstract class ButtonFragment : Fragment() {
    lateinit var binding: ButtonFragmentBinding
    lateinit var viewModel: TimerViewModel

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
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        bind(requireContext())
    }

    fun isBreak() {
        listOf(binding.button0, binding.button1, binding.button2).forEach {

        }
    }
}