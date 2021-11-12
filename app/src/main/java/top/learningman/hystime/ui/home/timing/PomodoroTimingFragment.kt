package top.learningman.hystime.ui.home.timing

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import top.learningman.hystime.R

class PomodoroTimingFragment : Fragment() {

    companion object {
        fun newInstance() = PomodoroTimingFragment()
    }

    private lateinit var viewModel: PomodoroTimingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pomodoro_timing, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PomodoroTimingViewModel::class.java)
        // TODO: Use the ViewModel
    }

}