package top.learningman.hystime.ui.dashboard.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.learningman.hystime.databinding.FragmentDashboardBinding
import top.learningman.hystime.ui.dashboard.DashboardActivity

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DashboardFragment", "onViewCreated")
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        val data =
            requireArguments().getSerializable(FRAGMENT_DATA_KEY) as DashboardActivity.Statistic
        binding.todayPomodoro.text = data.todayPomodoroCount.toString()
        binding.totalPomodoro.text = data.pomodoroCount.toString()
        binding.todayFocusLength.text = data.todayTimeSpent.toTimeStr()
        binding.totalFocusLength.text = data.timeSpent.toTimeStr()

    }

    companion object {
        const val FRAGMENT_DATA_KEY = "fragment_data_key"

        fun Int.toTimeStr(): String {
            val hour = this / 60
            val minute = this % 60
            return "%02d:%02d".format(hour, minute)
        }
    }

}