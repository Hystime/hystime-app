package top.learningman.hystime.ui.dashboard.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import top.learningman.hystime.R
import top.learningman.hystime.data.TimePieceBean.TimePieceType.NORMAL
import top.learningman.hystime.data.TimePieceBean.TimePieceType.POMODORO
import top.learningman.hystime.databinding.FragmentDashboardBinding
import top.learningman.hystime.databinding.WidgetDashboardHourminTextBinding
import top.learningman.hystime.repo.StringRepo
import top.learningman.hystime.ui.dashboard.DashboardActivity
import top.learningman.hystime.utils.autoCleared
import top.learningman.hystime.utils.plus
import top.learningman.hystime.utils.shortFormat

class DashboardFragment : Fragment() {

    private var binding: FragmentDashboardBinding by autoCleared()
    private var todayFocusBinding: WidgetDashboardHourminTextBinding by autoCleared()
    private var totalFocusBinding: WidgetDashboardHourminTextBinding by autoCleared()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.todayFocusLength.setOnInflateListener { _, inflated ->
            todayFocusBinding = WidgetDashboardHourminTextBinding.bind(inflated)
        }
        binding.totalFocusLength.setOnInflateListener { _, inflated ->
            totalFocusBinding = WidgetDashboardHourminTextBinding.bind(inflated)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DashboardFragment", "onViewCreated")

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

        // Render timepiece
        if (data.hasTimepiece()) {
            binding.start.text = data.tpStart?.shortFormat()
            val end = data.tpStart!! + data.tpDuration!!
            binding.end.text = end.shortFormat()
            binding.duration.text = data.tpDuration.toTime().toString()
            binding.type.text = when (data.tpType) {
                NORMAL -> StringRepo.getString(R.string.normal)
                POMODORO -> StringRepo.getString(R.string.pomodoro)
                else -> throw IllegalArgumentException("Unknown timepiece type")
            }

            if (data.type == DashboardActivity.Type.USER) {
                binding.target.visibility = View.VISIBLE
                binding.target.text = data.tpTargetName
            }

        } else {
            binding.timepiece.visibility = View.GONE
            binding.placeholderTimepiece.visibility = View.VISIBLE
        }

        // Render heatmap
        binding.heatmap.setData(data.heatMap)

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

            override fun toString(): String {
                return if (minuteOnly()) {
                    "$minute ${StringRepo.getString(R.string.minute)}"
                } else {
                    "$hour ${StringRepo.getString(R.string.hour)} $minute ${StringRepo.getString(R.string.minute)}"
                }
            }
        }

        fun Int.toTime(): Time {
            val hour = this / 3600
            val minute = this % 3600 / 60
            return Time(hour, minute)
        }
    }

}