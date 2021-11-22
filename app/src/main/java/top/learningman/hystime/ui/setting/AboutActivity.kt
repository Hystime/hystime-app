package top.learningman.hystime.ui.setting

import android.annotation.SuppressLint
import android.widget.ImageView

import android.widget.TextView
import com.drakeet.about.*

import top.learningman.hystime.BuildConfig
import top.learningman.hystime.R


class AboutActivity : AbsAboutActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        icon.setImageResource(R.mipmap.ic_launcher)
        slogan.text = "Hystime by Zxilly"
        version.text = "v" + BuildConfig.VERSION_NAME
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.add(Category("Open Source Licenses"))
        items.apply {
            add(
                License(
                    "kotlin",
                    "JetBrains",
                    License.Apache_2_0,
                    "https://github.com/JetBrains/kotlin"
                )
            )
            add(
                License(
                    "MultiType",
                    "drakeet",
                    License.Apache_2_0,
                    "https://github.com/drakeet/MultiType"
                )
            )
            add(
                License(
                    "about-page",
                    "drakeet",
                    License.Apache_2_0,
                    "https://github.com/drakeet/about-page"
                )
            )
            add(
                License(
                    "AndroidX",
                    "Google",
                    License.Apache_2_0,
                    "https://source.google.com"
                )
            )
            add(
                License(
                    "Android Jetpack",
                    "Google",
                    License.Apache_2_0,
                    "https://source.google.com"
                )
            )
            add(
                License(
                    "material-components-android",
                    "Google",
                    License.Apache_2_0,
                    "https://github.com/material-components/material-components-android"
                )
            )
            add(
                License(
                    "NumberPicker",
                    "ShawnLin013",
                    License.MIT,
                    "https://github.com/ShawnLin013/NumberPicker"
                )
            )
        }
    }
}