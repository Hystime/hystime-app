package top.learningman.hystime.ui.timer.timing

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import top.learningman.hystime.MainViewModel


open class TimingFragment : Fragment() {
    val mainViewModel: MainViewModel by activityViewModels()
}