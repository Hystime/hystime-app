package top.learningman.hystime.utils

import android.content.res.Resources


fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

enum class Status{
    SUCCESS,
    FAILED,
    PENDING
}

