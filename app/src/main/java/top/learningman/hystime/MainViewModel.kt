package top.learningman.hystime

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.data.UserBean
import top.learningman.hystime.repo.UserRepository
import top.learningman.hystime.sdk.errorString

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
            viewModelScope.launch {
                UserRepository.getUser(username).fold(
                    {
                        _user.value = it
                    }, {
                        _user.value = null
                        AlertDialog.Builder(context)
                            .setTitle("Oooooops!")
                            .setMessage(it.errorString())
                            .setNegativeButton("Close") { _, _ -> }
                            .show()
                    }
                )
            }
        }
    }
//    private val _currentTarget = MutableLiveData<TargetBean>()
//    val currentTarget: LiveData<TargetBean> = _currentTarget

}