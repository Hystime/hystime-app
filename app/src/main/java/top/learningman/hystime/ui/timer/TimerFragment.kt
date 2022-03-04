package top.learningman.hystime.ui.timer

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.learningman.hystime.Constant
import top.learningman.hystime.MainActivity
import top.learningman.hystime.MainViewModel
import top.learningman.hystime.R
import top.learningman.hystime.databinding.FragmentTimerBinding
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.repo.TimePieceRepo
import top.learningman.hystime.ui.timer.TimerViewModel.TimerStatus.*
import top.learningman.hystime.ui.timer.TimerViewModel.TimerType.*
import top.learningman.hystime.ui.timer.timing.CountdownFragment
import top.learningman.hystime.ui.timer.timing.FinishFragment
import top.learningman.hystime.ui.timer.timing.NormalTimerViewFragment
import top.learningman.hystime.ui.timer.timing.PomodoroTimerViewFragment
import top.learningman.hystime.utils.toTimeString
import type.TimePieceType
import java.util.*
import kotlin.math.abs

class TimerFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val timerViewModel: TimerViewModel by activityViewModels()

    private var _binding: FragmentTimerBinding? = null

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constant.TIMER_BROADCAST_CLEAN_ACTION -> {
                    val duration =
                        intent.getLongExtra(Constant.TIMER_BROADCAST_CLEAN_DURATION_EXTRA, 0)
                    val remain = intent.getLongExtra(Constant.TIMER_BROADCAST_CLEAN_REMAIN_EXTRA, 0)
                    val startedAt =
                        intent.getSerializableExtra(Constant.TIMER_BROADCAST_CLEAN_START_EXTRA)!! as Date
                    val type =
                        intent.getSerializableExtra(Constant.TIMER_BROADCAST_CLEAN_TYPE_EXTRA)!! as TimerViewModel.TimerType

                    if (type == BREAK) {
                        Log.d("TimerFragment", "BREAK CLEAN will not be recorded")
                        return
                    }

                    // ignore too short time piece
                    if (duration < 60) {
                        Toast.makeText(
                            context,
                            getString(R.string.too_short_time_piece_toast),
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    if (type == POMODORO) {
                        if (remain > 0) {
                            Log.d("TimerFragment", "Broken pomodoro should not be recorded.")
                            Toast.makeText(
                                context,
                                getString(R.string.too_short_pomodoro_toast),
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }
                    }

                    // TODO： create a queue to handle timepieces
                    lifecycleScope.launch(Dispatchers.IO) {
                        TimePieceRepo.addTimePiece(
                            mainViewModel.currentTarget.value!!.id, // FIXME: make target transaction safe
                            startedAt,
                            duration.toInt(),
                            if (type == NORMAL) {
                                TimePieceType.NORMAL
                            } else {
                                TimePieceType.POMODORO
                            }
                        ).fold({
                            Log.d("TimerFragment", "Add time piece success")
                            // TODO update dashboard
                        }, {
                            Log.e("TimerFragment", "Add time piece failed", it)
                            Toast.makeText(
                                context,
                                getString(R.string.fail_to_create_timepiece_toast),
                                Toast.LENGTH_LONG
                            ).show()
                        })
                    }
                }
            }
        }
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val binding get() = _binding!!

    private val mTransformer =
        ViewPager2.PageTransformer { view, position ->
            view.apply {
                val pageWidth = width
                when {
                    -1 <= position && position <= 1 -> {
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
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                timerViewModel.setType(
                    when (position) {
                        0 -> NORMAL
                        1 -> POMODORO
                        else -> throw Error("Unexpected position")
                    }
                )
            }
        })

        tabLayout = binding.tabLayout
        tabLayout.let {
            TabLayoutMediator(it, viewPager) { tab, position ->
                tab.text = getFragmentName(position)
            }.attach()
        }

        // update time at wait start
        timerViewModel.type.observe(viewLifecycleOwner) {
            when (it) {
                NORMAL -> {
                    binding.time.text = getString(R.string.zero_time)
                }
                POMODORO -> {
                    binding.time.text =
                        (SharedPrefRepo.getPomodoroFocusLength() * 60 * 1000).toLong()
                            .toTimeString()
                }
                else -> {}
            }
        }

        binding.target.setOnClickListener {
            if (timerViewModel.status.value != WAIT_START) {
                return@setOnClickListener
            }
            val targets = mainViewModel.targets.value!!.map {
                it.name
            }.toTypedArray()
            if (targets.isEmpty()) {
                Toast.makeText(
                    context,
                    getString(R.string.no_target_toast),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.select_target_title))
                    .setItems(targets) { _, which ->
                        mainViewModel.setCurrentTarget(targets[which])
                    }.show()
            }
        }

        binding.start.setOnClickListener {
            timerViewModel.setStatus(WORK_RUNNING)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().registerReceiver(timerReceiver, IntentFilter().apply {
            addAction(Constant.TIMER_BROADCAST_CLEAN_ACTION)
        })

        mainViewModel.currentTarget.observe(viewLifecycleOwner) {
            binding.target.text = it?.name ?: getString(R.string.no_target)
        }

        fun switchFragment(fragment: Fragment) {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commitNow()
        }

        timerViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                WORK_RUNNING, BREAK_RUNNING -> switchFragment(CountdownFragment())
                WORK_FINISH, BREAK_FINISH -> switchFragment(FinishFragment())
                else -> {}
            }
        }

        // switch env
        timerViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                WORK_RUNNING -> enterEnv()
                WAIT_START -> leaveEnv()
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(timerReceiver)
        _binding = null
    }

    class TimingAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> NormalTimerViewFragment()
                1 -> PomodoroTimerViewFragment()
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

    private fun enterEnv() {
        (requireActivity() as MainActivity).hideNav()
        binding.tabLayout.apply {
            if (isShown) {
                visibility = View.INVISIBLE
            }
        }
        binding.timerHost.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE


    }

    private fun leaveEnv() {
        (requireActivity() as MainActivity).showNav()
        binding.tabLayout.apply {
            if (!isShown) {
                visibility = View.VISIBLE
            }
        }
        binding.timerHost.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
    }
}