package top.learningman.hystime.ui.timer.buttonGroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import top.learningman.hystime.databinding.ButtonFragmentStartBinding

class StartFragment:Fragment() {
    private lateinit var binding: ButtonFragmentStartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ButtonFragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }
}