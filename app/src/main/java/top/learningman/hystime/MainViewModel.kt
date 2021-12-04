package top.learningman.hystime

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.data.UserBean
import top.learningman.hystime.repo.UserRepository
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.sdk.errorString

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val client by HystimeClient.Companion.Client()

    private val _user = MutableLiveData<UserBean?>()
    val user: LiveData<UserBean?> = _user

    private val _targets = MutableLiveData<List<TargetBean>>()
    val targets: LiveData<List<TargetBean>> = _targets

    private val _error = MutableLiveData<Throwable?>()
    val error: LiveData<Throwable?> = _error

    fun resetError() {
        _error.value = null
    }

    private val _serverConnection = MutableLiveData(false)
    val serverConnection: LiveData<Boolean> = _serverConnection

    fun refreshUser(newUser: String?) {
        val context = getApplication<Application>().applicationContext
        val sp = context.getSharedPreferences(
            context.getString(R.string.setting_filename),
            Context.MODE_PRIVATE
        )
        val username = newUser ?: sp.getString(context.getString(R.string.setting_username_key), "")
        if (username.isNullOrEmpty()) {
            _user.value = null
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                UserRepository.getUser(username).fold(
                    {
                        _user.value = it
                    }
                ) {
                    _user.value = null
                }
            }
        }
    }

    fun refreshServer(newUri: String?, newAuthCode: String?) {
        val context = getApplication<Application>().applicationContext
        val sp = context.getSharedPreferences(
            context.getString(R.string.setting_filename),
            Context.MODE_PRIVATE
        )
        val uri = newUri ?: sp.getString(context.getString(R.string.setting_backend_key), "")!!
        val authCode =
            newAuthCode ?: sp.getString(context.getString(R.string.setting_auth_key), "")!!
        HystimeClient(uri, authCode)
        viewModelScope.launch(Dispatchers.IO) {
            client.refreshValid().fold({
                _serverConnection.postValue(true)
            }, {
                _serverConnection.postValue(false)
                _error.postValue(it)
            })
        }
    }
//    private val _currentTarget = MutableLiveData<TargetBean>()
//    val currentTarget: LiveData<TargetBean> = _currentTarget

}