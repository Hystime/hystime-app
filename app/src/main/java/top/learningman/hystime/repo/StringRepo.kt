package top.learningman.hystime.repo

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes

@SuppressLint("StaticFieldLeak")
object StringRepo {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }
}