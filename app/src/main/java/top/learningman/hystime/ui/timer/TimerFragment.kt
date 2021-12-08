package top.learningman.hystime.ui.timer

import android.app.AlertDialog
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
import top.learningman.hystime.MainViewModel
import top.learningman.hystime.R
import top.learningman.hystime.databinding.FragmentTimerBinding
import top.learningman.hystime.ui.timer.timing.NormalTimingFragment
import top.learningman.hystime.ui.timer.timing.PomodoroTimingFragment
import kotlin.math.abs

class TimerFragment : Fragment() {

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    private var _binding: FragmentTimerBinding? = null

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val binding get() = _binding!!

    private val mTransformer =
        ViewPager2.PageTransformer { view, position ->
            view.apply {
                val pageWidth = width
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        viewPager = binding.pager
        viewPager.adapter = TimingAdapter(this)
        viewPager.setPageTransformer(mTransformer)

        tabLayout = binding.tabLayout
        tabLayout.let {
            TabLayoutMediator(it, viewPager) { tab, position ->
                tab.text = getFragmentName(position)
            }.attach()
        }

        binding.target.setOnClickListener {
            val targets = mainViewModel.targets.value!!.map {
                it.name
            }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_target_title))
                .setItems(targets) { _, which ->
                    mainViewModel.setCurrentTarget(targets[which])
                }.show()
        }

        mainViewModel.currentTarget.observe(viewLifecycleOwner) {
            binding.target.text = it?.name ?: getString(R.string.no_target)
        }

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
            0 -> requireContext().getString(R.string.tab_normal_timing)
            1 -> requireContext().getString(R.string.tab_pomodoro_timing)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}