package top.learningman.hystime.utils

import android.app.Activity
import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import top.learningman.hystime.R
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


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

fun Activity.slideEnterAnimation() {
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun Activity.slideExitAnimation() {
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

operator fun Date.plus(time: Int): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.add(Calendar.SECOND, time)
    return cal.time
}

fun Date.weekday(): Int {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal.get(Calendar.DAY_OF_WEEK)
}

fun Date.shortFormat(): String {
    val cal = Calendar.getInstance()
    cal.time = this
    return "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}

class AutoClearedValue<T : Any>(val fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            _value = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: throw IllegalStateException(
            "should never call auto-cleared-value get when it might not be available"
        )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }
}

/**
 * Creates an [AutoClearedValue] associated with this fragment.
 */
fun <T : Any> Fragment.autoCleared() = AutoClearedValue<T>(this)
