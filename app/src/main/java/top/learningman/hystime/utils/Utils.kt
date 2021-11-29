package top.learningman.hystime.utils

import android.content.Context
import android.content.res.Resources
import androidx.fragment.app.Fragment
import top.learningman.hystime.MainActivity
import top.learningman.hystime.MainApplication
import top.learningman.hystime.sdk.HystimeClient
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception


fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun requireMainApplication(context: Fragment): MainApplication {
    return context.requireActivity().application as MainApplication
}

fun requireClient(context: Fragment): HystimeClient? {
    return requireMainApplication(context).client
}

fun setClient(context: Fragment, client: HystimeClient) {
    requireMainApplication(context).client = client
}