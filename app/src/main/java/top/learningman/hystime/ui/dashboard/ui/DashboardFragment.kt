package top.learningman.hystime.ui.dashboard.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textview.MaterialTextView
import top.learningman.hystime.R
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

        // Inflate ViewStub
        mapOf(
            data.timeSpent to binding.totalFocusLength,
            data.todayTimeSpent to binding.todayFocusLength
        ).forEach {
            val time = it.key.toTime()
            val vs = it.value
            if (time.minuteOnly()) {
                vs.layoutResource = R.layout.item_dashboard_min_text
                val v = vs.inflate()
                val min: MaterialTextView = v.findViewById(R.id.minute)
                min.text = time.minute.toString()
            } else {
                val v = vs.inflate()
                val min: MaterialTextView = v.findViewById(R.id.minute)
                val hour: MaterialTextView = v.findViewById(R.id.hour)
                min.text = time.minute.toString()
                hour.text = time.hour.toString()
            }
        }
    }

    companion object {
        const val FRAGMENT_DATA_KEY = "fragment_data_key"

        data class Time(
            val hour: Int = 0,
            val minute: Int
        ) {
            fun minuteOnly(): Boolean {
                return hour == 0
            }
        }

        fun Int.toTime(): Time {
            val hour = this / 3600
            val minute = this % 3600 / 60
            return Time(hour, minute)
        }
    }

}