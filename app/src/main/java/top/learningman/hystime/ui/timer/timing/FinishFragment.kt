package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import top.learningman.hystime.databinding.FragmentFinishBinding


class FinishFragment : Fragment() {

    lateinit var binding: FragmentFinishBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFinishBinding.inflate(inflater, container, false)

        return binding.root
    }

}