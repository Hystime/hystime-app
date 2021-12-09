package top.learningman.hystime.ui.timer.buttonGroup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import top.learningman.hystime.R
import top.learningman.hystime.databinding.ButtonFragmentPauseBinding

class PauseFragment : Fragment() {
    lateinit var binding: ButtonFragmentPauseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ButtonFragmentPauseBinding.inflate(inflater, container, false)
        return binding.root
    }
}