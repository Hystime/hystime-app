package top.learningman.hystime.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import top.learningman.hystime.R
import top.learningman.hystime.databinding.FragmentHomeBinding
import top.learningman.hystime.ui.dashboard.DashboardFragment
import top.learningman.hystime.ui.home.timing.NormalTimingFragment
import top.learningman.hystime.ui.home.timing.PomodoroTimingFragment
import top.learningman.hystime.ui.setting.SettingFragment
import kotlin.coroutines.coroutineContext

class HomeFragment(val tabLayout: TabLayout) : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var timingAdapter: TimingAdapter
    private lateinit var viewPager: ViewPager2

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        timingAdapter = TimingAdapter(this)
        viewPager = binding.pager
        viewPager.adapter = timingAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getFragmentName(position)
        }.attach()

        return binding.root
    }

    class TimingAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> NormalTimingFragment()
                1 -> PomodoroTimingFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }

    fun getFragmentName(position: Int): String {
        return when (position) {
            0 -> context?.getString(R.string.tab_normal_timing) ?: ""
            1 -> context?.getString(R.string.tab_pomodoro_timing) ?: ""
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}