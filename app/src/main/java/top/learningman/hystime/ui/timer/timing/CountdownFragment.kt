package top.learningman.hystime.ui.timer.timing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import top.learningman.hystime.Constant
import top.learningman.hystime.MainViewModel
import top.learningman.hystime.R
import top.learningman.hystime.databinding.FragmentCountdownBinding
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.ui.timer.TimerFullScreenActivity
import top.learningman.hystime.ui.timer.TimerServiceController
import top.learningman.hystime.ui.timer.TimerViewModel
import top.learningman.hystime.ui.timer.TimerViewModel.TimerStatus.*
import top.learningman.hystime.ui.timer.TimerViewModel.TimerType.*
import top.learningman.hystime.utils.toTimeString

class CountdownFragment : Fragment() {
    lateinit var binding: FragmentCountdownBinding

    private val mainViewModel: MainViewModel by activityViewModels()
    private val timerViewModel: TimerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCountdownBinding.inflate(inflater, container, false)

        // update visibility and configure buttons
        when (timerViewModel.status.value) {
            WORK_RUNNING -> {
                binding.workRunning.visibility = View.VISIBLE
                binding.target.visibility = View.VISIBLE
                binding.target.text = mainViewModel.currentTarget.value!!.name

                // work running buttons
                binding.resume.setOnClickListener {
                    binding.timer.resume()
                    timerViewModel.setStatus(WORK_RUNNING)
                    TimerServiceController.Companion.TimerController.resumeTimer(requireContext())

                    binding.workPause.visibility = View.GONE
                    binding.workRunning.visibility = View.VISIBLE
                }

                binding.pause.setOnClickListener {
                    binding.timer.pause()
                    timerViewModel.setStatus(WORK_PAUSE)
                    TimerServiceController.Companion.TimerController.pauseTimer(requireContext())

                    binding.workRunning.visibility = View.INVISIBLE
                    binding.workPause.visibility = View.VISIBLE
                }

                binding.exit.setOnClickListener {
                    timerViewModel.setStatus(WAIT_START)
                    TimerServiceController.Companion.TimerController.killTimer(requireContext())
                }
            }
            BREAK_RUNNING -> {
                binding.breakRunning.visibility = View.VISIBLE

                // break running buttons
                binding.skip.setOnClickListener {
                    timerViewModel.setStatus(BREAK_FINISH)
                    TimerServiceController.Companion.TimerController.killTimer(requireContext())
                }

                binding.exit2.setOnClickListener {
                    timerViewModel.setStatus(WAIT_START)
                }
            }
            else -> {}
        }

        binding.container.setOnClickListener { _ ->
            Intent(requireContext(), TimerFullScreenActivity::class.java).apply {
                action = Constant.TIMER_FULLSCREEN_ACTION

                putExtra(Constant.TIMER_FULLSCREEN_INTENT_TIME_KEY, binding.time.text)
                putExtra(Constant.TIMER_FULLSCREEN_INTENT_TYPE_KEY, timerViewModel.type.value)
            }.also {
                startActivity(it)
                requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

        Log.d("CountdownFragment", "Configured view")

        binding.timer.setType(timerViewModel.type.value!!)

        if (timerViewModel.type.value == POMODORO_BREAK) {
            timerViewModel.updateBreakCount()
        }

        if (timerViewModel.type.value == POMODORO) {
            binding.time.text =
                (SharedPrefRepo.getPomodoroFocusLength() * 60 * 1000L).toTimeString()
        }

        binding.timer.start(timerViewModel.getTime())

        requireActivity().registerReceiver(timerReceiver, IntentFilter().apply {
            addAction(Constant.TIMER_BROADCAST_TIME_ACTION)
            Log.d("Countdown", "registerReceiver")
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("CountdownFragment", "onDestroyView")
        requireActivity().unregisterReceiver(timerReceiver)
    }

    // receiver
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                Constant.TIMER_BROADCAST_TIME_ACTION -> {
                    val time = intent.getLongExtra(Constant.TIMER_BROADCAST_PAST_TIME_EXTRA, 0)
                    val remain = intent.getLongExtra(Constant.TIMER_BROADCAST_REMAIN_TIME_EXTRA, 0)
                    val timeStr = when (timerViewModel.type.value) {
                        NORMAL -> time.toTimeString()
                        else -> remain.toTimeString()
                    }
                    binding.time.text = timeStr
                }
            }
        }
    }
}
