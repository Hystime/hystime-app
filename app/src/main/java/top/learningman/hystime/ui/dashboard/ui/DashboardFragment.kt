package top.learningman.hystime.ui.dashboard.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.learningman.hystime.databinding.FragmentDashboardBinding
import top.learningman.hystime.databinding.ItemDashboardHourminTextBinding
import top.learningman.hystime.ui.dashboard.DashboardActivity
import top.learningman.hystime.utils.autoCleared

class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private var binding: FragmentDashboardBinding by autoCleared()
    private var todayFocusBinding: ItemDashboardHourminTextBinding by autoCleared()
    private var totalFocusBinding: ItemDashboardHourminTextBinding by autoCleared()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.todayFocusLength.setOnInflateListener { _, inflated ->
            todayFocusBinding = ItemDashboardHourminTextBinding.bind(inflated)
        }
        binding.totalFocusLength.setOnInflateListener { _, inflated ->
            totalFocusBinding = ItemDashboardHourminTextBinding.bind(inflated)
        }
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
            vs.inflate()
            val stubBinding = when (vs) {
                binding.totalFocusLength -> totalFocusBinding
                binding.todayFocusLength -> todayFocusBinding
                else -> throw IllegalArgumentException("Unknown viewStub")
            }
            if (time.minuteOnly()) {
                stubBinding.hourGroup.visibility = View.GONE
            } else {
                stubBinding.hour.text = time.hour.toString()
            }
            stubBinding.minute.text = time.minute.toString()
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