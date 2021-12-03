package top.learningman.hystime.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.sdk.HystimeClient

class DashboardViewModel : ViewModel() {
    private val client: HystimeClient by lazy {
        HystimeClient.getInstance()
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _targetBeanList = MutableLiveData<List<TargetBean>>().apply {
        value = emptyList()
    }
    val targetBeanList: LiveData<List<TargetBean>> = _targetBeanList

    fun refreshTarget(username: String) {

    }
}