package top.learningman.hystime

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.data.UserBean
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.repo.TargetRepo
import top.learningman.hystime.repo.UserRepo
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.utils.Status
import type.TargetType

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val context by lazy { getApplication<Application>().applicationContext }
    private val client by HystimeClient.Companion.Client()

    private val _user = MutableLiveData<UserBean?>(null)
    val user: LiveData<UserBean?> = _user

    private val _targets = MutableLiveData<List<TargetBean>>(emptyList())
    val targets: LiveData<List<TargetBean>> = _targets

    private val _currentTarget = MutableLiveData<TargetBean?>(null)
    var currentTarget: LiveData<TargetBean?> = _currentTarget

    fun setCurrentTarget(target: TargetBean?) {
        _currentTarget.postValue(target)
    }

    fun setCurrentTarget(targetName: String) {
        _targets.value!!.find {
            it.name == targetName
        }?.let {
            _currentTarget.postValue(it)
        }
    }

    fun resetCurrentTarget() {
        _currentTarget.postValue(null)
    }

    fun currentTargetAddTime(duration: Int) {
        _currentTarget.value?.let {
            it.timeSpent += duration
            _currentTarget.postValue(it)
        }
    }

    private val _error = MutableLiveData<Throwable?>()
    val error: LiveData<Throwable?> = _error

    fun setError(e: Throwable) {
        _error.postValue(e)
    }

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
            TargetRepo.getUserTargets(realUser.username).fold({
                _targets.postValue(it)
            }, {
                _targets.postValue(emptyList())
                _error.postValue(it)
            })

        }
    }

    fun addTarget(name: String, timeSpent: Int, type: TargetType) {
        viewModelScope.launch(Dispatchers.IO) {
            TargetRepo.addTarget(_user.value!!.id, name, timeSpent, type).fold({
                showSnackBarMessage(it.name + context.getString(R.string.target_add_success_postfix))
                _targets.postValue(_targets.value!!.plus(it))
            }, {
                _error.postValue(it)
            })
        }
    }

    fun refreshUser(newUser: String?) {
        val username = newUser ?: SharedPrefRepo.getUser()
        if (username.isEmpty()) {
            _user.postValue(null)
            _userStatus.postValue(Status.FAILED)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                UserRepo.getUser(username).fold(
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
        val uri = newUri ?: SharedPrefRepo.getEndpoint()
        val authCode = newAuthCode ?: SharedPrefRepo.getAuthCode()
        HystimeClient(uri, authCode)
        Log.d("refreshClient", "$uri with $authCode")
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