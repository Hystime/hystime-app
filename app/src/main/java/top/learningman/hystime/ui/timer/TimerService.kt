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

    private fun getNotification(): (String, Long) -> Notification {
        fun Long.format(): String {
            val secs = (this / 1000).toInt()
            val sec = secs % 60
            val min = secs / 60
            return "%.2d:%.2d".format(min, sec)
        }

        var notification: Notification? = null
        fun createNotification(name: String): Notification {
            return NotificationCompat.Builder(
                applicationContext,
                Constant.TIMER_NOTIFICATION_CHANNEL_ID
            )
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
                .apply {
                    flags = Notification.FLAG_FOREGROUND_SERVICE
                }
        }

        fun Notification.update(time: Long) {
            fun limitCharSequenceLength(cs: CharSequence?): CharSequence? {
                if (cs == null) return cs
                if (cs.length > 5 * 1024) {
                    return cs.subSequence(0, 5 * 1024)
                }
                return cs
            }

            val mContentText = this.javaClass.getDeclaredField("mContentText")
            mContentText.isAccessible = true
            mContentText.set(
                notification,
                limitCharSequenceLength(time.format())
            )
        }

        return { name: String, time: Long ->
            if (notification == null) {
                notification = createNotification(name)
            }
            notification!!.apply {
                update(time)
            }
        }
    }


    private fun sendBroadcast() {
        Intent(Constant.TIMER_BROADCAST_TIME_ACTION).apply {
            timer?.let {
                putExtra(Constant.TIMER_BROADCAST_TIME_EXTRA, it.elapsedTime)
                putExtra(Constant.TIMER_BROADCAST_REMAIN_TIME_EXTRA, it.remainingTime)
            }
        }.also {
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

            startForeground(Constant.FOREGROUND_NOTIFICATION_ID, getNotification()(name, 0))
            timer = Timer(duration, { time ->
                val notification = getNotification()(name, time)
                NotificationManagerCompat.from(applicationContext).notify(
                    Constant.FOREGROUND_NOTIFICATION_ID,
                    notification
                )
                sendBroadcast()
            }, {
                stopSelf()
            })
            timer?.start()
        }
        return binder
    }
}