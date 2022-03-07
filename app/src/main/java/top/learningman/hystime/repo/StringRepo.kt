package top.learningman.hystime.repo

import androidx.annotation.StringRes

object StringRepo {
    fun getString(@StringRes id: Int): String {
        return AppRepo.context.getString(id)
    }
}