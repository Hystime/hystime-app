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

                    binding.workPause.visibility = View.INVISIBLE
                    binding.workRunning.visibility = View.VISIBLE
                }

                binding.pause.setOnClickListener {
                    binding.timer.pause()
                    binder!!.pause()

                    binding.workRunning.visibility = View.INVISIBLE
                    binding.workPause.visibility = View.VISIBLE
                }

                binding.exit.setOnClickListener {
                    stopTimerService() // redirect to WAIT_START internal
                }
            }
            TimerViewModel.TimerStatus.BREAK_RUNNING -> {
                binding.breakRunning.visibility = View.VISIBLE

                // break running buttons
                binding.skip.setOnClickListener {
                    timerViewModel.setStatus(TimerViewModel.TimerStatus.BREAK_FINISH)
                }

                binding.exit2.setOnClickListener {
                    stopTimerService() // redirect to WAIT_START internal
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

        startTimerService(timerViewModel.getTime(), getServiceName())
        binding.timer.setType(timerViewModel.type.value!!)
        binding.timer.start(timerViewModel.getTime())

        requireActivity().registerReceiver(receiver, IntentFilter().apply {
            addAction(Constant.TIMER_BROADCAST_TIME_ACTION)
            addAction(Constant.TIMER_BROADCAST_CLEAN_ACTION)
        })

        return binding.root
    }

    // receiver
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                Constant.TIMER_BROADCAST_TIME_ACTION -> {
                    val time = intent.getLongExtra(Constant.TIMER_BROADCAST_PAST_TIME_EXTRA, 0)
                    val remain = intent.getLongExtra(Constant.TIMER_BROADCAST_REMAIN_TIME_EXTRA, 0)
                    val timeStr = when (timerViewModel.type.value) {
                        BREAK, POMODORO -> remain.toTimeString()
                        NORMAL -> time.toTimeString()
                        else -> "ERR"
                    }
                    binding.time.text = timeStr
                }
                Constant.TIMER_BROADCAST_CLEAN_ACTION -> {
                    val remain = intent.getLongExtra(Constant.TIMER_BROADCAST_CLEAN_REMAIN_EXTRA, 0)

                    if (remain > 0) {
                        timerViewModel.setStatus(TimerViewModel.TimerStatus.WAIT_START)
                    } else {
                        when (intent.getSerializableExtra(Constant.TIMER_BROADCAST_CLEAN_TYPE_EXTRA)!! as TimerViewModel.TimerType) {
                            NORMAL, POMODORO -> {
                                timerViewModel.setStatus(TimerViewModel.TimerStatus.WORK_FINISH)
                            }
                            BREAK -> {
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
        val intent = Intent(AppRepo.context, TimerService::class.java)
        intent.putExtra(Constant.TIMER_DURATION_INTENT_KEY, duration * 1000)
        intent.putExtra(Constant.TIMER_NAME_INTENT_KEY, name)
        intent.putExtra(Constant.TIMER_TYPE_INTENT_KEY, timerViewModel.type.value)

        Log.d("startService", "bindService")
        isConnected = AppRepo.context.bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun stopTimerService() {
        Log.d("stopService", "unbindService")
        binder?.cancel()
        unbind()
        binder = null
    }

    private fun unbind() {
        if (isConnected) {
            AppRepo.context.unbindService(connection)
            isConnected = false
        }
    }

    private fun getServiceName() = when (timerViewModel.type.value) {
        NORMAL -> {
            StringRepo.getString(R.string.tab_normal_timing)
        }
        POMODORO -> {
            StringRepo.getString(R.string.tab_pomodoro_timing)
        }
        BREAK -> {
            StringRepo.getString(R.string.timer_break)
        }
        else -> throw Error("Unexpected type")
    }


}
