package top.learningman.hystime

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.andrognito.flashbar.Flashbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import top.learningman.hystime.databinding.ActivityMainBinding
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.ui.dashboard.DashboardFragment
import top.learningman.hystime.ui.setting.SettingFragment
import top.learningman.hystime.ui.timer.TimerFragment
import top.learningman.hystime.utils.getAuthCode
import top.learningman.hystime.utils.getEndpoint
import kotlin.math.abs

private const val NUM_PAGES = 3

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var viewPager: ViewPager2

    fun getPager(): ViewPager2 = viewPager

    private val mOnSelectItemListener = object : NavigationBarView.OnItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            fun goItem(item: Int) {
                if (item == viewPager.currentItem) return
                if (abs(item - viewPager.currentItem) > 1) {
                    viewPager.setCurrentItem(item, false)
                } else {
                    viewPager.setCurrentItem(item, true)
                }
            }

            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    goItem(0)
                    return true
                }
                R.id.navigation_home -> {
                    goItem(1)
                    return true
                }
                R.id.navigation_setting -> {
                    goItem(2)
                    return true
                }
            }
            return false
        }
    }

    private val mOnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            binding.navView.selectedItemId = when (position) {
                0 -> R.id.navigation_dashboard
                1 -> R.id.navigation_home
                2 -> R.id.navigation_setting
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }

    private val mTransformer =
        ViewPager2.PageTransformer { view, position ->
            val pageWidth = view.width
            fun stay(view: View?) {
                view?.let {
                    when {
                        -1 <= position && position <= 1 -> {
                            view.translationX = pageWidth * -position
                        }
                    }
                }
            }

            fun fade(view: View?) {
                view?.let {
                    it.alpha = when {
                        position < -1 -> {
                            0f
                        }
                        position <= 1 -> {
                            1 - abs(position)
                        }
                        else -> {
                            0f
                        }
                    }
                }
            }

            val tabLayout: View? = view.findViewById(R.id.tabLayout)
            val appbar: View? = view.findViewById(R.id.appbar)

            stay(appbar)
            stay(tabLayout)
            fade(appbar)
            fade(tabLayout)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =
            ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory(application)
            )[MainViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val toolbar = binding.toolbar
//        setSupportActionBar(toolbar)

        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener(mOnSelectItemListener)
        viewPager = binding.pager

        val pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(mOnPageChangeCallback)
        viewPager.setPageTransformer(mTransformer)

        HystimeClient(getEndpoint(this), getAuthCode(this))

        viewModel.error.observe(this) {
            it?.let { it1 ->
                showErrorDialog(it1)
                viewModel.resetError()
            }
        }

        viewModel.snackBarMessage.observe(this) {
            it?.let { it1 ->
//                Snackbar.make(binding.content, it1, Snackbar.LENGTH_SHORT).show()
                Flashbar.Builder(this)
                    .gravity(Flashbar.Gravity.BOTTOM)
                    .message(it1)
                    .backgroundColor(getColor(R.color.primaryLightColor))
                    .duration(Flashbar.DURATION_LONG)
                    .show()
                viewModel.resetSnackBarMessage()
            }
        }

        viewModel.refreshServer(null,null)
    }

    private fun showErrorDialog(error: Throwable) {
        AlertDialog.Builder(this)
            .setTitle("Oooooops!")
            .setMessage(error.localizedMessage)
            .setNegativeButton("Close") { _, _ -> }
            .show()
            .findViewById<TextView>(android.R.id.message).apply {
                typeface = Typeface.MONOSPACE
            }
    }

    private inner class MainPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount() = NUM_PAGES

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> DashboardFragment()
                1 -> TimerFragment()
                2 -> SettingFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
    }
}