package top.learningman.hystime.utils

import android.content.Context
import android.content.res.Resources
import top.learningman.hystime.R


fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun getSettingSharedPreferences(context: Context) =
    context.getSharedPreferences(
        context.getString(R.string.setting_filename),
        Context.MODE_PRIVATE
    )!!

fun getUser(context: Context): String = getSettingSharedPreferences(context).getString(
    context.getString(R.string.setting_username_key),
    ""
)!!

fun getEndpoint(context: Context): String = getSettingSharedPreferences(context).getString(
    context.getString(R.string.setting_backend_key),
    ""
)!!

fun getAuthCode(context: Context): String = getSettingSharedPreferences(context).getString(
    context.getString(R.string.setting_auth_key),
    ""
)!!


