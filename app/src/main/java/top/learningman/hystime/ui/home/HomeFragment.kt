package top.learningman.hystime.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import top.learningman.hystime.R
import top.learningman.hystime.databinding.FragmentHomeBinding
import top.learningman.hystime.ui.home.timing.NormalTimingFragment
import top.learningman.hystime.ui.home.timing.PomodoroTimingFragment
import top.learningman.hystime.utils.FadePageTransformer

class HomeFragment(private val tabLayout: TabLayout) : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var viewPager: ViewPager2

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        viewPager = binding.pager
        viewPager.adapter = TimingAdapter(this)
        viewPager.setPageTransformer(FadePageTransformer())

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

    private fun getFragmentName(position: Int): String {
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