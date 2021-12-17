package top.learningman.hystime.repo

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AppRepo {
    lateinit var context: Context

    fun init(context: Context){
        this.context = context
    }
}