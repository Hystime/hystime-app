package top.learningman.hystime.ui.timer.timing

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.learningman.hystime.MainViewModel


open class TimingFragment : Fragment() {
    val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }
}