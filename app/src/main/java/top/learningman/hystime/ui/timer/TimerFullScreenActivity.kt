package top.learningman.hystime.ui.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import top.learningman.hystime.Constant
import top.learningman.hystime.databinding.ActivityTimerFullScreenBinding
import top.learningman.hystime.utils.toTimeString

class TimerFullScreenActivity : AppCompatActivity() {
    enum class Type {
        NORMAL,
        POMODORO
    }

    lateinit var type: Type

    val binding by lazy { ActivityTimerFullScreenBinding.inflate(layoutInflater) }

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constant.TIMER_BROADCAST_TIME_ACTION -> {
                    val time = intent.getLongExtra(Constant.TIMER_BROADCAST_PAST_TIME_EXTRA, 0)
                        .toTimeString()
                    val remain = intent.getLongExtra(Constant.TIMER_BROADCAST_REMAIN_TIME_EXTRA, 0)
                        .toTimeString()
                    when (type) {
                        Type.NORMAL -> {
                            binding.time.text = time
                        }
                        Type.POMODORO -> {
                            binding.time.text = remain
                        }
                    }
                }
                Constant.TIMER_BROADCAST_CLEAN_ACTION -> {
                    finish()
                }
            }
        }

    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 30) {
            binding.layout.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            binding.layout.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        intent.action?.let {
            if (it == Constant.TIMER_FULLSCREEN_ACTION) {
                val time = intent.getLongExtra(Constant.TIMER_FULLSCREEN_INTENT_TIME_KEY, 0)
                binding.time.text = time.toTimeString()
                type =
                    intent.getSerializableExtra(Constant.TIMER_FULLSCREEN_INTENT_TYPE_KEY) as Type
            } else {
                finish()
            }
        } ?: finish()

        registerReceiver(timerReceiver, IntentFilter().apply {
            addAction(Constant.TIMER_BROADCAST_TIME_ACTION)
            addAction(Constant.TIMER_BROADCAST_CLEAN_ACTION)
        })


    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(timerReceiver)
    }
}