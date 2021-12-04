package top.learningman.hystime

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.data.UserBean

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val _user = MutableLiveData<UserBean?>()
    val user: LiveData<UserBean?> = _user

    private val _targets = MutableLiveData<List<TargetBean>>()
    val targets: LiveData<List<TargetBean>> = _targets

    fun refreshUser() {
        val context = getApplication<Application>().applicationContext
        val sp = context.getSharedPreferences(
            context.getString(R.string.setting_filename),
            Context.MODE_PRIVATE
        )
        val username = sp.getString(context.getString(R.string.setting_username_key), "")
        if (username.isNullOrEmpty()) {
            _user.value = null
        } else {

        }
    }
//    private val _currentTarget = MutableLiveData<TargetBean>()
//    val currentTarget: LiveData<TargetBean> = _currentTarget

}