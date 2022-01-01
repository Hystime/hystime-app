package top.learningman.hystime.ui.timer

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
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
import top.learningman.hystime.ui.timer.buttonGroup.ButtonFragments
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
                Constant.TIMER_BROADCAST_TIME_ACTION -> {
                    val time = intent.getLongExtra(Constant.TIMER_BROADCAST_PAST_TIME_EXTRA, 0)
                    val remain = intent.getLongExtra(Constant.TIMER_BROADCAST_REMAIN_TIME_EXTRA, 0)
                    timerViewModel.setTime(time)
                    timerViewModel.setRemainTime(remain)
                }
                Constant.TIMER_BROADCAST_CLEAN_ACTION -> {
                    var needSubmit = true
                    when (timerViewModel.status.value) {
                        WORK_RUNNING -> {
                            timerViewModel.setStatus(WORK_FINISH)
                        }
                        WORK_PAUSE -> {
                            timerViewModel.setStatus(WAIT_START)
                        }
                        BREAK_RUNNING -> {
                            needSubmit = false
                            Log.d("TimerFragment", "Break should not be recorded.")
                            timerViewModel.setStatus(WAIT_START)
                        }
                        BREAK_FINISH -> {
                            needSubmit = false
                        }
                        else -> {
                            Log.d(
                                "Clean Broadcast",
                                "No need to set with status ${timerViewModel.status.value!!.name}"
                            )
                        }
                    }
                    timerViewModel.unbind()

                    if (!needSubmit) {
                        return
                    }

                    val duration =
                        intent.getLongExtra(Constant.TIMER_BROADCAST_CLEAN_DURATION_EXTRA, 0)
                    val startedAt =
                        intent.getSerializableExtra(Constant.TIMER_BROADCAST_CLEAN_START_EXTRA)!! as Date
                    if (duration < 60) {
                        Toast.makeText(
                            context,
                            getString(R.string.too_short_time_piece_toast),
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                    if (timerViewModel.type.value == TimerViewModel.TimerType.POMODORO) {
                        if (duration < SharedPrefRepo.getPomodoroFocusLength() * 60 - 10) {
                            Log.d("TimerFragment", "Failed pomodoro should not be recorded.")
                            Toast.makeText(
                                context,
                                getString(R.string.too_short_pomodoro_toast),
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        TimePieceRepo.addTimePiece(
                            mainViewModel.currentTarget.value!!.id,
                            startedAt,
                            duration.toInt(),
                            if (timerViewModel.type.value == TimerViewModel.TimerType.NORMAL) {
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

    @SuppressLint("ClickableViewAccessibility")
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
                        0 -> TimerViewModel.TimerType.NORMAL
                        1 -> TimerViewModel.TimerType.POMODORO
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

        binding.target.setOnClickListener {
            if (timerViewModel.status.value != WAIT_START) {
                return@setOnClickListener
            }
            val targets = mainViewModel.targets.value!!.map {
                it.name
            }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_target_title))
                .setItems(targets) { _, which ->
                    mainViewModel.setCurrentTarget(targets[which])
                }.show()
        }

        binding.container.setOnTouchListener { _, _ ->
            if (timerViewModel.status.value != WORK_RUNNING) {
                return@setOnTouchListener false
            }
            Intent(requireContext(), TimerFullScreenActivity::class.java).apply {
                action = Constant.TIMER_FULLSCREEN_ACTION
                if (timerViewModel.type.value == TimerViewModel.TimerType.NORMAL) {
                    putExtra(Constant.TIMER_FULLSCREEN_INTENT_TIME_KEY, timerViewModel.time.value)
                } else {
                    putExtra(
                        Constant.TIMER_FULLSCREEN_INTENT_TIME_KEY,
                        timerViewModel.remainTime.value
                    )
                }
                putExtra(Constant.TIMER_FULLSCREEN_INTENT_TYPE_KEY, timerViewModel.type.value)
            }.also {
                startActivity(it)
                requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            true
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filter = IntentFilter().apply {
            addAction(Constant.TIMER_BROADCAST_TIME_ACTION)
            addAction(Constant.TIMER_BROADCAST_CLEAN_ACTION)
        }
        requireActivity().registerReceiver(timerReceiver, filter)

        mainViewModel.currentTarget.observe(viewLifecycleOwner) {
            binding.target.text = it?.name ?: getString(R.string.no_target)
        }

        timerViewModel.status.observe(viewLifecycleOwner) {
            setButtonFragment(
                when (it) {
                    WAIT_START -> ButtonFragments.WaitStartFragment()
                    WORK_RUNNING -> ButtonFragments.WorkRunningFragment()
                    WORK_PAUSE -> ButtonFragments.WorkPauseFragment()
                    WORK_FINISH -> ButtonFragments.WorkFinishFragment()
                    BREAK_RUNNING -> ButtonFragments.BreakRunningFragment()
                    BREAK_FINISH -> ButtonFragments.BreakFinishFragment()
                    else -> throw Error("Unexpected status")
                }
            )
        }

        timerViewModel.time.observe(viewLifecycleOwner) {
            if (timerViewModel.type.value == TimerViewModel.TimerType.NORMAL && !timerViewModel.isBreak()) {
                binding.time.text = it.toTimeString()
            }
        }

        timerViewModel.remainTime.observe(viewLifecycleOwner) {
            if (timerViewModel.type.value == TimerViewModel.TimerType.POMODORO || timerViewModel.isBreak()) {
                binding.time.text = it.toTimeString()
            }
        }

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

    private fun setButtonFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.buttonGroup, fragment)
            .commit()
    }

    private fun enterEnv() {
        (requireActivity() as MainActivity).hideNav()
        binding.tabLayout.apply {
            if (isShown) {
                visibility = View.INVISIBLE
            }
        }
        binding.timerHost.forbidScroll()
        binding.target.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_null)
        binding.target.isClickable = false
    }

    private fun leaveEnv() {
        (requireActivity() as MainActivity).showNav()
        binding.tabLayout.apply {
            if (!isShown) {
                visibility = View.VISIBLE
            }
        }
        binding.timerHost.allowScroll()
        binding.target.icon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right_white_24dp)
        binding.target.isClickable = true
    }
}