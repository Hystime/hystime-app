package top.learningman.hystime

import android.app.Application
import top.learningman.hystime.sdk.HystimeClient

class MainApplication: Application() {
    var client: HystimeClient? = null
}