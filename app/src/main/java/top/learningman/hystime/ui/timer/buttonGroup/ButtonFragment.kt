package top.learningman.hystime.ui.timer.buttonGroup

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import top.learningman.hystime.Constant
import top.learningman.hystime.databinding.ButtonFragmentBinding
import top.learningman.hystime.ui.timer.TimerService
import top.learningman.hystime.ui.timer.TimerViewModel
import top.learningman.hystime.ui.timer.TimerViewModel.TimerStatus.*

abstract class ButtonFragment : Fragment() {
    lateinit var binding: ButtonFragmentBinding
    val viewModel: TimerViewModel by activityViewModels()

    abstract fun bind(context: Context)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ButtonFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instance = this
        bind(requireContext())
    }

    fun isBreak() {
        listOf(binding.button0, binding.button1, binding.button2).forEach {

        }
    }

    companion object {
        lateinit var instance: ButtonFragment

        var binder: TimerService.TimerBinder? = null
        private val connection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                binder = null
                with(instance.viewModel) {
                    setTime(0L)
                    when (status.value) {
                        WORK_RUNNING, WORK_PAUSE -> {
                            setStatus(WORK_FINISH)
                        }
                        BREAK_RUNNING -> {
                            setStatus(BREAK_FINISH)
                        }
                        else -> {
                            throw Error("Service died unexpected.")
                        }
                    }
                }
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service as TimerService.TimerBinder
                binder!!.start()
            }
        }

        fun startService(context: Context, duration: Long, name: String? = null) {
            val intent = Intent(context, TimerService::class.java)
            intent.putExtra(Constant.TIMER_DURATION_INTENT_KEY, duration)
            name?.let {
                intent.putExtra(Constant.TIMER_NAME_INTENT_KEY, name)
            }
            context.bindService(
                intent,
                connection,
                Context.BIND_AUTO_CREATE
            )
        }

        fun stopService(context: Context) {
            context.unbindService(connection)
        }
    }
}