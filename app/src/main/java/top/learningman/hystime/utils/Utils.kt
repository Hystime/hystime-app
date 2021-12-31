package top.learningman.hystime.utils

import android.content.res.Resources
import android.util.Log


fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

// Applied to milliseconds
fun Long.toTimeString(): String {
    val secs = this / 1000
    val sec = secs % 60
    val min = secs / 60
    return "%02d:%02d".format(min, sec)
}

fun String.toSafeInt(): Int {
    return if (this.isEmpty()) {
        0
    } else {
        this.toInt()
    }
}

enum class Status {
    SUCCESS,
    FAILED,
    PENDING
}

