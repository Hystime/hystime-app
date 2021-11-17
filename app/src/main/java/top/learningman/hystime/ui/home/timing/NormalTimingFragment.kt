package top.learningman.hystime.ui.home.timing

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import top.learningman.hystime.R
import top.learningman.hystime.databinding.FragmentNormalTimingBinding
import top.learningman.hystime.ui.home.HomeViewModel

class NormalTimingFragment : Fragment() {

    private val model: HomeViewModel by lazy {
        ViewModelProvider(requireActivity())[HomeViewModel::class.java]
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

        model.target.observe(viewLifecycleOwner) { target ->
            binding.target.text = target
        }
    }

}