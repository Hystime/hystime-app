package top.learningman.hystime.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _target = MutableLiveData<String>().apply {
        value = "Example Target"
    }
    val target: LiveData<String> = _target
}