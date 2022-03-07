package top.learningman.hystime

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import top.learningman.hystime.databinding.ActivityMainBinding
import top.learningman.hystime.repo.AppRepo
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.ui.dashboard.DashboardListFragment
import top.learningman.hystime.ui.setting.SettingFragment
import top.learningman.hystime.ui.timer.TimerFragment
import kotlin.math.abs

private const val NUM_PAGES = 3

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
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
                R.id.navigation_timer -> {
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
                1 -> R.id.navigation_timer
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
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Load App Center
        AppCenter.start(
            application, "1a1d6bb8-95ca-43c5-a2b4-56a5c9042bad",
            Analytics::class.java, Crashes::class.java
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init
        AppRepo.init(applicationContext)

        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener(mOnSelectItemListener)
        viewPager = binding.pager

        val pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(mOnPageChangeCallback)
        viewPager.setPageTransformer(mTransformer)
        viewPager.offscreenPageLimit = NUM_PAGES - 1

        HystimeClient(
            SharedPrefRepo.getEndpoint(),
            SharedPrefRepo.getAuthCode()
        )

        viewModel.error.observe(this) {
            it?.let { it1 ->
                showErrorDialog(it1)
                viewModel.resetError()
            }
        }

        viewModel.snackBarMessage.observe(this) {
            it?.let { it1 ->
                Snackbar.make(binding.content, it1, Snackbar.LENGTH_SHORT).show()
                viewModel.resetSnackBarMessage()
            }
        }

        viewModel.refreshServer(null, null)
    }

    private fun showErrorDialog(error: Throwable) {
        AlertDialog.Builder(this)
            .setTitle("Oooooops!")
            .setMessage(error.localizedMessage)
            .setNegativeButton("Close") { _, _ -> }
            .show()
            .apply {
                findViewById<TextView>(android.R.id.message).apply {
                    typeface = Typeface.MONOSPACE
                }
            }
    }

    // TODO: add custom animation
    fun showNav() {
        binding.navView.apply {
            if (!isShown) {
                visibility = View.VISIBLE
            }
        }
    }

    fun hideNav() {
        binding.navView.apply {
            if (isShown) {
                visibility = View.INVISIBLE
            }
        }
    }

    private inner class MainPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount() = NUM_PAGES

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> DashboardListFragment()
                1 -> TimerFragment()
                2 -> SettingFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
    }
}