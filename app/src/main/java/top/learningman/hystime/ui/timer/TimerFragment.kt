package top.learningman.hystime.ui.timer

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
import top.learningman.hystime.databinding.FragmentTimerBinding
import top.learningman.hystime.ui.timer.timing.NormalTimingFragment
import top.learningman.hystime.ui.timer.timing.PomodoroTimingFragment
import top.learningman.hystime.view.TimerView
import kotlin.math.abs

class TimerFragment : Fragment() {

    private lateinit var timerViewModel: TimerViewModel
    private var _binding: FragmentTimerBinding? = null

    private lateinit var viewPager: ViewPager2
    private var tabLayout: TabLayout? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        timerViewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        viewPager = binding.pager
        viewPager.adapter = TimingAdapter(this)
        viewPager.setPageTransformer { view, position ->
            view.apply {
                val pageWidth = width
                when {
                    -1 <= position && position <= 1 -> { // [-1,1]
                        translationX = pageWidth * -position
                    }
                }
            }
            view.findViewById<TimerView>(R.id.timer).apply {
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

        tabLayout?.let {
            TabLayoutMediator(it, viewPager) { tab, position ->
                tab.text = getFragmentName(position)
            }.attach()
        }

        return binding.root
    }

    fun setTabLayout(tabLayout: TabLayout) {
        this.tabLayout = tabLayout
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