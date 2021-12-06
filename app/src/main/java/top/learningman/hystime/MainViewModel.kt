package top.learningman.hystime

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.data.UserBean
import top.learningman.hystime.repo.TargetRepository
import top.learningman.hystime.repo.UserRepository
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.utils.Status

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val context by lazy { getApplication<Application>().applicationContext }
    private val client by HystimeClient.Companion.Client()

    private val _user = MutableLiveData<UserBean?>(null)
    val user: LiveData<UserBean?> = _user

    private val _targets = MutableLiveData<List<TargetBean>>(emptyList())
    val targets: LiveData<List<TargetBean>> = _targets

    private val _error = MutableLiveData<Throwable?>()
    val error: LiveData<Throwable?> = _error

    fun resetError() {
        _error.postValue(null)
    }

    private val _serverStatus = MutableLiveData(Status.PENDING)
    val serverStatus: LiveData<Status> = _serverStatus
    private val _userStatus = MutableLiveData(Status.PENDING)
    val userStatus: LiveData<Status> = _userStatus

    private val _snackBarMessage = MutableLiveData<String?>(null)
    val snackBarMessage: LiveData<String?> = _snackBarMessage

    fun showSnackBarMessage(message: String) {
        _snackBarMessage.postValue(message)
    }

    fun resetSnackBarMessage() {
        _snackBarMessage.postValue(null)
    }

    fun fetchTarget(newUser: UserBean?) {
        if (user.value == null && newUser == null) {
            _error.postValue(Error(context.getString(R.string.user_not_valid)))
            return
        }
        val realUser = newUser ?: user.value!!
        viewModelScope.launch(Dispatchers.IO) {
            TargetRepository.getUserTargets(realUser.username).fold({
                _targets.postValue(it)
            }, {
                _targets.postValue(emptyList())
                _error.postValue(it)
            })

        }
    }


    fun refreshUser(newUser: String?) {
        val sp = context.getSharedPreferences(
            context.getString(R.string.setting_filename),
            Context.MODE_PRIVATE
        )
        val username = newUser ?: sp.getString(context.getString(R.string.setting_username_key), "")
        if (username.isNullOrEmpty()) {
            _user.postValue(null)
            _userStatus.postValue(Status.FAILED)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                UserRepository.getUser(username).fold(
                    {
                        _user.postValue(it)
                        _userStatus.postValue(Status.SUCCESS)
                        fetchTarget(it)
                    }
                ) {
                    _user.postValue(null)
                    _userStatus.postValue(Status.FAILED)
                    _error.postValue(it)
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
                _serverStatus.postValue(Status.SUCCESS)
            }, {
                _serverStatus.postValue(Status.FAILED)
                _error.postValue(it)
            })
            refreshUser(null)
        }
    }

}