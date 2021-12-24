package top.learningman.hystime.repo

import android.content.Context
import android.content.SharedPreferences
import top.learningman.hystime.R

object SharedPrefRepo {
    private var sp: SharedPreferences? = null

    private fun getSettingSharedPreferences(): SharedPreferences {
        if (sp == null) {
            val context = AppRepo.context
            sp = context.getSharedPreferences(
                context.getString(R.string.setting_filename),
                Context.MODE_PRIVATE
            )!!
        }
        return sp!!
    }


    fun getUser() = getSettingSharedPreferences().getString(
        StringRepo.getString(R.string.setting_username_key),
        ""
    )!!

    fun getEndpoint() = getSettingSharedPreferences().getString(
        StringRepo.getString(R.string.setting_backend_key),
        ""
    )!!

    fun getAuthCode() = getSettingSharedPreferences().getString(
        StringRepo.getString(R.string.setting_auth_key),
        ""
    )!!

    fun getNormalFocusLength() = getSettingSharedPreferences().getInt(
        StringRepo.getString(R.string.setting_normal_length_key),
        45
    )

    fun getNormalBreakLength() = getSettingSharedPreferences().getInt(
        StringRepo.getString(R.string.setting_normal_break_length_key),
        15
    )

    fun getPomodoroFocusLength() = getSettingSharedPreferences().getInt(
        StringRepo.getString(R.string.setting_pomodoro_length_key),
        25
    )

    fun getPomodoroShortBreakLength() = getSettingSharedPreferences().getInt(
        StringRepo.getString(R.string.setting_pomodoro_length_key),
        5
    )

    fun getPomodoroLongBreakLength() = getSettingSharedPreferences().getInt(
        StringRepo.getString(R.string.setting_pomodoro_long_break_key),
        15
    )

    fun getVibrationStatus() = getSettingSharedPreferences().getBoolean(
        StringRepo.getString(R.string.setting_vibration_key),
        false
    )

}