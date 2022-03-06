package top.learningman.hystime.ui.timer.timing

import android.content.*
import android.os.Bundle
import android.os.IBinder
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
import top.learningman.hystime.repo.AppRepo
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.repo.StringRepo
import top.learningman.hystime.ui.timer.TimerFullScreenActivity
import top.learningman.hystime.ui.timer.TimerService
import top.learningman.hystime.ui.timer.TimerViewModel
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
            TimerViewModel.TimerStatus.WORK_RUNNING -> {
                binding.workRunning.visibility = View.VISIBLE
                binding.target.visibility = View.VISIBLE
                binding.target.text = mainViewModel.currentTarget.value!!.name

                // work running buttons
                binding.resume.setOnClickListener {
                    binding.timer.resume()
                    binder!!.start()

                    binding.workPause.visibility = View.GONE
                    binding.workRunning.visibility = View.VISIBLE
                }

                binding.pause.setOnClickListener {
                    binding.timer.pause()
                    binder!!.pause()

                    binding.workRunning.visibility = View.INVISIBLE
                    binding.workPause.visibility = View.VISIBLE
                }

                binding.exit.setOnClickListener {
                    killTimerService() // redirect to WAIT_START internal
                }
            }
            TimerViewModel.TimerStatus.BREAK_RUNNING -> {
                binding.breakRunning.visibility = View.VISIBLE

                // break running buttons
                binding.skip.setOnClickListener {
                    timerViewModel.setStatus(TimerViewModel.TimerStatus.BREAK_FINISH)
                }

                binding.exit2.setOnClickListener {
                    killTimerService() // redirect to WAIT_START internal
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

        startTimerService(timerViewModel.getTime(), getServiceName())
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
            addAction(Constant.TIMER_BROADCAST_CLEAN_ACTION)
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
                Constant.TIMER_BROADCAST_CLEAN_ACTION -> { // TODO: use timer fragment to handle service
                    unbindTimerService()
                    val remain = intent.getLongExtra(Constant.TIMER_BROADCAST_CLEAN_REMAIN_EXTRA, 0)

                    if (remain > 0) {
                        timerViewModel.setStatus(TimerViewModel.TimerStatus.WAIT_START)
                    } else {
                        when (intent.getSerializableExtra(Constant.TIMER_BROADCAST_CLEAN_TYPE_EXTRA)!! as TimerViewModel.TimerType) {
                            NORMAL, POMODORO -> {
                                Log.d("status", "NORMAL or POMODORO")
                                timerViewModel.setStatus(TimerViewModel.TimerStatus.WORK_FINISH)
                            }
                            NORMAL_BREAK, POMODORO_BREAK -> {
                                Log.d("status", "NORMAL_BREAK or POMODORO_BREAK")
                                timerViewModel.setStatus(TimerViewModel.TimerStatus.BREAK_FINISH)
                            }
                        }
                    }
                }
            }
        }
    }


    // Service control
    var binder: TimerService.TimerBinder? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("onServiceConnected", "Connected $name")
            binder = service as TimerService.TimerBinder
            binder!!.start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("onServiceDisconnected", "Disconnected")
            binder = null // FIXME: handle service crash
        }
    }

    private var isConnected: Boolean = false
    private fun startTimerService(duration: Long, name: String? = null) {
        val intent = Intent(AppRepo.context, TimerService::class.java).apply {
            putExtra(Constant.TIMER_DURATION_INTENT_KEY, duration * 1000)
            putExtra(Constant.TIMER_NAME_INTENT_KEY, name)
            putExtra(Constant.TIMER_TYPE_INTENT_KEY, timerViewModel.type.value)
        }

        isConnected = AppRepo.context.bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE
        )

        Log.d("startService", "bindService for $name, Connect = $isConnected")
        if (!isConnected) {
            Log.e("startService", "bindService failed")
        }
    }

    private fun killTimerService() {
        Log.d("stopService", "unbindService")
        binder!!.cancel()
    }

    private fun unbindTimerService() {
        Log.d("unbindService", "unbindService")
        if (isConnected) {
            AppRepo.context.unbindService(connection)
            isConnected = false
        }
        binder = null
    }

    private fun getServiceName() = when (timerViewModel.type.value) {
        NORMAL -> {
            StringRepo.getString(R.string.tab_normal_timing)
        }
        POMODORO -> {
            StringRepo.getString(R.string.tab_pomodoro_timing)
        }
        NORMAL_BREAK, POMODORO_BREAK -> {
            StringRepo.getString(R.string.timer_break)
        }
        else -> throw Error("Unexpected type")
    }


}
