package top.learningman.hystime.repo

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import top.learningman.hystime.R

@SuppressLint("StaticFieldLeak")
object SharedPrefRepo {
    private lateinit var context: Context
    private var sp: SharedPreferences? = null

    fun init(context: Context) {
        this.context = context
    }

    private fun getSettingSharedPreferences(): SharedPreferences {
        if (sp == null) {
            sp = context.getSharedPreferences(
                context.getString(R.string.setting_filename),
                Context.MODE_PRIVATE
            )!!
        }
        return sp!!
    }


    fun getUser() = getSettingSharedPreferences().getString(
        context.getString(R.string.setting_username_key),
        ""
    )!!

    fun getEndpoint() = getSettingSharedPreferences().getString(
        context.getString(R.string.setting_backend_key),
        ""
    )!!

    fun getAuthCode() = getSettingSharedPreferences().getString(
        context.getString(R.string.setting_auth_key),
        ""
    )!!

    fun getNormalFocusLength() = getSettingSharedPreferences().getInt(
        context.getString(R.string.setting_normal_length_key),
        45
    )

    fun getNormalBreakLength() = getSettingSharedPreferences().getInt(
        context.getString(R.string.setting_normal_break_length_key),
        15
    )

    fun getPomodoroFocusLength() = getSettingSharedPreferences().getInt(
        context.getString(R.string.setting_pomodoro_length_key),
        25
    )

    fun getPomodoroShortBreakLength() = getSettingSharedPreferences().getInt(
        context.getString(R.string.setting_pomodoro_length_key),
        5
    )

    fun getPomodoroLongBreakLength() = getSettingSharedPreferences().getInt(
        context.getString(R.string.setting_pomodoro_long_break_key),
        15
    )

}