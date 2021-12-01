package top.learningman.hystime

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import top.learningman.hystime.databinding.ActivityMainBinding
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.ui.dashboard.DashboardFragment
import top.learningman.hystime.ui.timer.TimerFragment
import top.learningman.hystime.ui.setting.SettingFragment
import top.learningman.hystime.utils.getAuthCode
import top.learningman.hystime.utils.getEndpoint
import kotlin.math.abs

private const val NUM_PAGES = 3

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private val mOnSelectItemListener = object : NavigationBarView.OnItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    viewPager.currentItem = 0
                    return true
                }
                R.id.navigation_home -> {
                    viewPager.currentItem = 1
                    return true
                }
                R.id.navigation_setting -> {
                    viewPager.currentItem = 2
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
            view.findViewById<Toolbar>(R.id.topPanel)?.apply {
                when {
                    -1 <= position && position <= 1 -> { // [-1,1]
                        translationX = pageWidth * -position
                    }
                }
                alpha = when {
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
            view.findViewById<TabLayout>(R.id.tabLayout)?.apply {
                when {
                    -1 <= position && position <= 1 -> { // [-1,1]
                        translationX = pageWidth * -position
                    }
                }
                alpha = when {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

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
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.toolbar_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.refresh -> {
//                // Associate with https://stackoverflow.com/questions/55728719/get-current-fragment-with-viewpager2
//                currentFragment()?.let {
//                    if (it is Interface.RefreshableFragment) {
//                        it.refresh()
//                    }
//                }
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    private fun currentFragment(): Fragment? =
//        supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")


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

    companion object {
        private lateinit var viewPager: ViewPager2
        fun getPager(): ViewPager2 = viewPager
    }
}