package top.learningman.hystime.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import top.learningman.hystime.data.Target

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _targetList = MutableLiveData<List<Target>>().apply {
        value = emptyList()
    }

    val targetList: LiveData<List<Target>> = _targetList
}