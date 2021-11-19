package top.learningman.hystime.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class HomeViewModel : ViewModel() {

    enum class TimerStatus {
        STOP,
        PAUSE,
        RUNNING
    }

    private val _target = MutableLiveData<String>().apply {
        value = "Example Target"
    }
    val target: LiveData<String> = _target

    private val _status = MutableLiveData<TimerStatus>().apply {
        value = TimerStatus.STOP
    }
    val status: LiveData<TimerStatus> = _status

    private var _targetStart: Long? = null
    private var _pauseStart: Long? = null
    private var _pauseLength: Long = 0

    fun start() {
        _status.value = TimerStatus.RUNNING
        _targetStart = Date().time
    }

    fun pause() {
        _status.value = TimerStatus.PAUSE
        _pauseStart = Date().time
    }

    fun resume() {
        _status.value = TimerStatus.RUNNING
        _pauseLength += Date().time - _pauseStart!!
    }

    fun stop() {
        _status.value = TimerStatus.STOP
        _targetStart = null
        _pauseLength = 0
    }

    fun getTimeString(): String {
        val time = (Date().time - _targetStart!! - _pauseLength) / 1000
        val seconds = time % 60
        val minutes = time / 60
        return "$minutes:$seconds"
    }

}