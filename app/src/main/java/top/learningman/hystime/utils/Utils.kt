package top.learningman.hystime.utils

import android.content.res.Resources


fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Long.format(): String {
    val secs = this / 1000
    val sec = secs % 60
    val min = secs / 60
    return "%02d:%02d".format(min, sec)
}

enum class Status{
    SUCCESS,
    FAILED,
    PENDING
}

