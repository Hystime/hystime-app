package top.learningman.hystime.ui.timer.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import top.learningman.hystime.databinding.FragmentCountdownBinding

enum class CountdownType {
    NORMAL,
    POMODORO,
    SHORT_BREAK,
    LONG_BREAK
}

class CountdownFragment(private val type: CountdownType) : Fragment() {
    lateinit var binding: FragmentCountdownBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCountdownBinding.inflate(inflater, container, false)



        return binding.root
    }

}
