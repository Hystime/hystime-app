package top.learningman.hystime.ui.dashboard.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import top.learningman.hystime.R
import top.learningman.hystime.data.TimePieceBean.TimePieceType.NORMAL
import top.learningman.hystime.data.TimePieceBean.TimePieceType.POMODORO
import top.learningman.hystime.databinding.FragmentDashboardBinding
import top.learningman.hystime.databinding.WidgetDashboardHourminTextBinding
import top.learningman.hystime.repo.StringRepo
import top.learningman.hystime.ui.dashboard.DashboardActivity
import top.learningman.hystime.ui.dashboard.TimePieceActivity
import top.learningman.hystime.utils.autoCleared
import top.learningman.hystime.utils.dateShortFormat
import top.learningman.hystime.utils.plusSec
import top.learningman.hystime.utils.timeShortFormat

class DashboardFragment : Fragment() {

    private lateinit var data: DashboardActivity.Statistic

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

        data =
            requireArguments().getSerializable(FRAGMENT_DATA_KEY) as DashboardActivity.Statistic
        binding.todayPomodoro.text = data.todayPomodoroCount.toString()
        binding.totalPomodoro.text = data.pomodoroCount.toString()

        val viewStubs = listOf(
            data.timeSpent to binding.totalFocusLength,
            data.todayTimeSpent to binding.todayFocusLength
        )
        // Inflate ViewStub
        viewStubs.forEach { (key, value) ->
            val time = key.toTime()
            value.inflate()
            val stubBinding = when (value) {
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
            binding.start.text = data.tpStart!!.timeShortFormat()
            binding.date.text = data.tpStart!!.dateShortFormat()
            val end = data.tpStart!!.plusSec(data.tpDuration!!)
            binding.end.text = end.timeShortFormat()
            binding.duration.text = data.tpDuration!!.toTime().toString()
            binding.type.text = when (data.tpType) {
                NORMAL -> StringRepo.getString(R.string.normal)
                POMODORO -> StringRepo.getString(R.string.pomodoro)
                else -> throw IllegalArgumentException("Unknown timepiece type")
            }

            if (data.type == DashboardActivity.Type.USER) {
                binding.target.visibility = View.VISIBLE
                binding.target.text = data.tpTargetName
            }

            binding.timepieceButton.setOnClickListener {
                val intent = Intent(requireContext(), TimePieceActivity::class.java)
                val bundle = Bundle().apply {
                    putSerializable(TimePieceActivity.BUNDLE_TYPE_KEY, data.type)
                    putString(TimePieceActivity.BUNDLE_USERNAME_KEY, data.username)
                    putString(TimePieceActivity.BUNDLE_TARGET_ID_KEY, data.tpTargetId)
                }
                intent.putExtra(TimePieceActivity.INTENT_BUNDLE_KEY, bundle)
                startActivity(intent)
            }


        } else {
            binding.timepieceButton.visibility = View.GONE
            binding.timepiece.visibility = View.GONE
            binding.placeholderTimepiece.visibility = View.VISIBLE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DashboardFragment", "onViewCreated")

        // Render heatmap
        binding.heatmap.setData(data.heatMap)

        binding.heatmap.doOnLayout {
            binding.scroll.isSmoothScrollingEnabled = false
            binding.scroll.fullScroll(ScrollView.FOCUS_RIGHT)
            binding.scroll.isSmoothScrollingEnabled = true
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

            override fun toString(): String {
                return if (minuteOnly()) {
                    "$minute${StringRepo.getString(R.string.minute)}"
                } else {
                    "$hour${StringRepo.getString(R.string.hour)}$minute${StringRepo.getString(R.string.minute)}"
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