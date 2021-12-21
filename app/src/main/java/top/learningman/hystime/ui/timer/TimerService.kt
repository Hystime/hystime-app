package top.learningman.hystime.ui.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import top.learningman.hystime.Constant
import top.learningman.hystime.R
import top.learningman.hystime.repo.StringRepo
import top.learningman.hystime.utils.Timer
import top.learningman.hystime.utils.format


class TimerService : Service() {
    private var timer: Timer? = null
    private val binder = TimerBinder()

    inner class TimerBinder : Binder() {
        fun pause() {
            timer?.pause()
        }

        fun cancel() {
            timer?.cancel()
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
            builder!!.setContentText(time.format()).build()
        }
    }


    private fun sendTimeBroadcast() {
        Intent(Constant.TIMER_BROADCAST_TIME_ACTION).apply {
            timer?.let {
                putExtra(Constant.TIMER_BROADCAST_TIME_EXTRA, it.elapsedTime)
                putExtra(Constant.TIMER_BROADCAST_REMAIN_TIME_EXTRA, it.remainingTime)
            }
        }.also {
            sendBroadcast(it)
        }
    }

    private fun sendCleanBroadcast() {
        Intent(Constant.TIMER_BROADCAST_CLEAN_ACTION).also {
            sendBroadcast(it)
        }
    }


    override fun onBind(intent: Intent): IBinder {
        intent.let {
            val duration = it.getLongExtra(Constant.TIMER_DURATION_INTENT_KEY, 0) * 1000
            val name =
                it.getStringExtra(Constant.TIMER_NAME_INTENT_KEY) ?: StringRepo.getString(
                    R.string.timer
                )
            createNotificationChannel()

            val notificationBuilder = getNotificationBuilder()

            startForeground(Constant.FOREGROUND_NOTIFICATION_ID, notificationBuilder(name, 0))
            timer = Timer(duration, { time ->
                val notification = notificationBuilder(name, time)
                NotificationManagerCompat.from(applicationContext).notify(
                    Constant.FOREGROUND_NOTIFICATION_ID,
                    notification
                )
                sendTimeBroadcast()
            }, {
                sendCleanBroadcast()
                stopForeground(true)
                stopSelf()
            })
            timer?.start()
        }
        return binder
    }
}