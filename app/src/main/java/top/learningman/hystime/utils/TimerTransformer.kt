package top.learningman.hystime.utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class TimerTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 1 -> { // [-1,1]
                    translationX = pageWidth * -position
                    // Fade the page relative to its size.
                    alpha = 1 - abs(position)
                }
                else -> { // (1,+Infinity]
                    alpha = 0f
                }
            }
        }
    }
}