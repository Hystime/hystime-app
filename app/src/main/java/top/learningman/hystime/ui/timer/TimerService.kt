package top.learningman.hystime.ui.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import top.learningman.hystime.Constant
import top.learningman.hystime.R
import top.learningman.hystime.repo.StringRepo
import top.learningman.hystime.utils.Timer
import top.learningman.hystime.utils.toTimeString
import java.util.*


class TimerService : Service() {
    private var timer: Timer? = null
    private val binder = TimerBinder()

    private var duration: Long = 0
    private var startedAt: Date? = null
    private var type: TimerViewModel.TimerType = TimerViewModel.TimerType.NORMAL

    inner class TimerBinder : Binder() {
        fun pause() {
            timer?.pause()
        }

        fun cancel() {
            timer?.cancel() // stopTimer and invoke onFinish
        }

        fun start() {
            timer?.start()
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.timer_service_name)
        val descriptionText = getString(R.string.timer_service_notification)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(Constant.TIMER_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getNotificationBuilder(): (String, Long) -> Notification {
        var builder: NotificationCompat.Builder? = null
        var builderName: String? = null
        fun createNotificationBuilder(name: String): NotificationCompat.Builder {
            return NotificationCompat.Builder(
                applicationContext,
                Constant.TIMER_NOTIFICATION_CHANNEL_ID
            )
                .setContentTitle(name)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        return { name: String, time: Long ->
            if ((builder == null) or (name != builderName)) {
                builder = createNotificationBuilder(name)
                builderName = name // maybe unnecessary
            }
            builder!!.setContentText(time.toTimeString()).build()
        }
    }

    private fun sendTimeBroadcast() {
        Intent(Constant.TIMER_BROADCAST_TIME_ACTION).apply {
            timer?.let {
                putExtra(Constant.TIMER_BROADCAST_PAST_TIME_EXTRA, it.elapsedTime)
                putExtra(Constant.TIMER_BROADCAST_REMAIN_TIME_EXTRA, it.remainingTime)
            }
        }.also {
            sendBroadcast(it)
        }
    }

    private fun sendCleanBroadcast(remain: Long) {
        Log.d("broadcast", "sendCleanBroadcast")
        Intent(Constant.TIMER_BROADCAST_CLEAN_ACTION).apply {
            val dur = (Date().time - startedAt!!.time) / 1000
            putExtra(Constant.TIMER_BROADCAST_CLEAN_DURATION_EXTRA, dur)
            putExtra(Constant.TIMER_BROADCAST_CLEAN_REMAIN_EXTRA, remain / 1000)
            putExtra(Constant.TIMER_BROADCAST_CLEAN_START_EXTRA, startedAt)
            putExtra(Constant.TIMER_BROADCAST_CLEAN_TYPE_EXTRA, this@TimerService.type)
        }.also {
            sendBroadcast(it)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("timer", "onBind")
        intent.let { iet ->
            duration = iet.getLongExtra(Constant.TIMER_DURATION_INTENT_KEY, 0)
            startedAt = Date()
            val name =
                iet.getStringExtra(Constant.TIMER_NAME_INTENT_KEY) ?: StringRepo.getString(
                    R.string.timer
                )
            type =
                iet.getSerializableExtra(Constant.TIMER_TYPE_INTENT_KEY) as TimerViewModel.TimerType
            createNotificationChannel()
            val notificationBuilder = getNotificationBuilder()

            startForeground(Constant.FOREGROUND_NOTIFICATION_ID, notificationBuilder(name, 0))
            timer = Timer({ time ->
                val notifyTime =
                    if (type == TimerViewModel.TimerType.NORMAL
                    ) {
                        time
                    } else {
                        duration - time
                    }
                val notification = notificationBuilder(name, notifyTime)
                NotificationManagerCompat.from(applicationContext).notify(
                    Constant.FOREGROUND_NOTIFICATION_ID,
                    notification
                )
                sendTimeBroadcast()
            }, {
                sendCleanBroadcast(it)
                stopForeground(true)
            }, duration)
            timer?.start()
        }
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("timer", "onUnbind")
        stopSelf()
        return false
    }

    override fun onDestroy() {
        Log.d("timerService", "service killed")
        super.onDestroy()
    }
}