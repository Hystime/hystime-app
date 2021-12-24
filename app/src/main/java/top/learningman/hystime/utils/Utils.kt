package top.learningman.hystime.utils

import android.content.res.Resources
import android.util.Log


fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Long.format(): String {
    val secs = this / 1000
    val sec = secs % 60
    val min = secs / 60
    return "%02d:%02d".format(min, sec)
}

fun LGD(log: String) {
    val info = log.split(";")
    Log.d(info[0], info[1]);
}

enum class Status {
    SUCCESS,
    FAILED,
    PENDING
}

