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

        fun resume() {
            timer?.resume()
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
                .setSmallIcon(R.drawable.ic_clock_outline_24dp)
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

    private var cleanTrigger = false
    private fun sendCleanBroadcast() {
        if (cleanTrigger) return
        cleanTrigger = true
        Log.d("broadcast", "sendCleanBroadcast")
        Intent(Constant.TIMER_BROADCAST_CLEAN_ACTION).apply {
            val dur = (Date().time - startedAt!!.time) / 1000
            putExtra(Constant.TIMER_BROADCAST_CLEAN_DURATION_EXTRA, dur)
            putExtra(Constant.TIMER_BROADCAST_CLEAN_START_EXTRA, startedAt)
        }.also {
            sendBroadcast(it)
        }
    }


    override fun onBind(intent: Intent): IBinder {
        intent.let {
            duration = it.getLongExtra(Constant.TIMER_DURATION_INTENT_KEY, 0) * 1000
            startedAt = Date()
            val name =
                it.getStringExtra(Constant.TIMER_NAME_INTENT_KEY) ?: StringRepo.getString(
                    R.string.timer
                )
            createNotificationChannel()

            val notificationBuilder = getNotificationBuilder()

            startForeground(Constant.FOREGROUND_NOTIFICATION_ID, notificationBuilder(name, 0))
            timer = Timer({ time ->
                // FIXME: buggy implement, depends on string resource.
                val notifyTime =
                    if (name.startsWith(StringRepo.getString(R.string.tab_pomodoro_timing))) {
                        duration - time
                    } else {
                        time
                    }
                val notification = notificationBuilder(name, notifyTime)
                NotificationManagerCompat.from(applicationContext).notify(
                    Constant.FOREGROUND_NOTIFICATION_ID,
                    notification
                )
                sendTimeBroadcast()
            }, {
                sendCleanBroadcast()
                stopForeground(true)
//                stopSelf()
            }, duration)
            timer?.start()
        }
        return binder
    }
}