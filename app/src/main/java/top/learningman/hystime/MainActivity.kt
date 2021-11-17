package top.learningman.hystime

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import top.learningman.hystime.databinding.ActivityMainBinding
import top.learningman.hystime.ui.dashboard.DashboardFragment
import top.learningman.hystime.ui.home.HomeFragment
import top.learningman.hystime.ui.setting.SettingFragment

private const val NUM_PAGES = 3

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

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
            when (position) {
                0 -> {
                    showActionBar()
                    supportActionBar?.title = getString(R.string.title_dashboard)
                }
                1 -> hideActionBar()
                2 -> {
                    showActionBar()
                    supportActionBar?.title = getString(R.string.title_setting)
                }
                else -> throw IllegalArgumentException("Invalid position")
            }
        }

        fun showActionBar() {
            tabLayout.visibility = TabLayout.GONE
            supportActionBar?.show()

        }

        fun hideActionBar() {
            tabLayout.visibility = TabLayout.VISIBLE
            supportActionBar?.hide()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        tabLayout = binding.tabLayout

        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener(mOnSelectItemListener)
        viewPager = binding.pager

        val pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(mOnPageChangeCallback)

    }

    private inner class MainPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount() = NUM_PAGES

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> DashboardFragment()
                1 -> HomeFragment(tabLayout)
                2 -> SettingFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
    }
}