package top.learningman.hystime.repo

import android.annotation.SuppressLint
import androidx.annotation.StringRes

object StringRepo {
    fun getString(@StringRes id: Int): String {
        return AppRepo.context.getString(id)
    }
}