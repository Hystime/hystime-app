package top.learningman.hystime.ui.timer

import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowInsetsControllerCompat
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
import top.learningman.hystime.repo.AppRepo
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.repo.StringRepo
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

    private lateinit var parentPager: ViewPager2

    private var _binding: FragmentTimerBinding? = null

    private lateinit var serviceController: TimerServiceController

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constant.TIMER_BROADCAST_CLEAN_ACTION -> {
                    serviceController.unbindTimerService()

                    val duration =
                        intent.getLongExtra(Constant.TIMER_BROADCAST_CLEAN_DURATION_EXTRA, 0)
                    val remain = intent.getLongExtra(Constant.TIMER_BROADCAST_CLEAN_REMAIN_EXTRA, 0)
                    val startedAt =
                        intent.getSerializableExtra(Constant.TIMER_BROADCAST_CLEAN_START_EXTRA)!! as Date
                    val type =
                        intent.getSerializableExtra(Constant.TIMER_BROADCAST_CLEAN_TYPE_EXTRA)!! as TimerViewModel.TimerType

                    if (remain > 500) { // FIXME: break running has two status with remain > 500
                        timerViewModel.setStatus(WAIT_START)
                    } else {
                        when (intent.getSerializableExtra(Constant.TIMER_BROADCAST_CLEAN_TYPE_EXTRA)!! as TimerViewModel.TimerType) {
                            NORMAL, POMODORO -> {
                                Log.d("status", "NORMAL or POMODORO")
                                timerViewModel.setStatus(WORK_FINISH)
                            }
                            NORMAL_BREAK, POMODORO_BREAK -> {
                                Log.d("status", "NORMAL_BREAK or POMODORO_BREAK")
                                timerViewModel.setStatus(BREAK_FINISH)
                            }
                        }
                    }

                    if (type.isBreak()) {
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
                            startedAt, duration.toInt(), if (type == NORMAL) {
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

    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("TimerFragmentServiceReceiver", "Receive broadcast")
            when (intent?.action) {
                Constant.TIMER_FRAGMENT_PAUSE_ACTION -> {
                    Log.d("TimerFragment", "Pause action received")
                    serviceController.pause()
                }
                Constant.TIMER_FRAGMENT_RESUME_ACTION -> {
                    Log.d("TimerFragment", "Resume action received")
                    serviceController.resume()
                }
                Constant.TIMER_FRAGMENT_CANCEL_ACTION -> {
                    Log.d("TimerFragment", "Stop action received")
                    serviceController.cancel()
                }
            }
        }
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val binding get() = _binding!!

    private val mTransformer = ViewPager2.PageTransformer { view, position ->
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        serviceController = TimerServiceController(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        binding.target.setOnClickListener {
            val targets = mainViewModel.targets.value!!.map {
                it.name
            }.toTypedArray()
            if (targets.isEmpty()) {
                mainViewModel.showSnackBarMessage(getString(R.string.no_target_toast))
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
            if (mainViewModel.currentTarget.value == null) {
                mainViewModel.showSnackBarMessage(StringRepo.getString(R.string.no_target_hint))
            } else {
                timerViewModel.setStatus(WORK_RUNNING)
            }
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

        mainViewModel.currentTarget.observe(viewLifecycleOwner) {
            binding.target.text = it?.name ?: getString(R.string.no_target)
        }

        fun switchFragment(fragment: Fragment, cb: Runnable? = null) {
            childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).also {
                if (cb != null) {
                    it.runOnCommit(cb)
                }
            }.commit()
        }

        fun workTypeSync() {
            when (timerViewModel.type.value) { // sync timer type
                NORMAL_BREAK -> {
                    timerViewModel.setType(NORMAL)
                }
                POMODORO_BREAK -> {
                    timerViewModel.setType(POMODORO)
                }
                else -> {}
            }
        }

        fun breakTypeSync() {
            when (timerViewModel.type.value) { // sync timer type
                NORMAL -> {
                    timerViewModel.setType(NORMAL_BREAK)
                }
                POMODORO -> {
                    timerViewModel.setType(POMODORO_BREAK)
                }
                else -> {}
            }
        }

        timerViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                WORK_RUNNING -> {
                    if (!serviceController.isConnected) {
                        workTypeSync()
                        switchFragment(CountdownFragment()) { enterEnv() }
                    }
                }
                BREAK_RUNNING -> {
                    breakTypeSync()
                    switchFragment(CountdownFragment())
                }
                WORK_FINISH, BREAK_FINISH -> switchFragment(FinishFragment())
                WAIT_START -> {
                    leaveEnv()
                    workTypeSync()
                }
                else -> {}
            }
        }

        timerViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                WORK_RUNNING, BREAK_RUNNING -> {
                    if (!serviceController.isConnected) {
                        serviceController.startTimerService(
                            timerViewModel.getTime(),
                            timerViewModel.type.value!!,
                            timerViewModel.getServiceName()
                        )
                    }
                }
                else -> {}
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().registerReceiver(timerReceiver, IntentFilter().apply {
            addAction(Constant.TIMER_BROADCAST_CLEAN_ACTION)
        })

        requireActivity().registerReceiver(serviceReceiver, IntentFilter().apply {
            addAction(Constant.TIMER_FRAGMENT_PAUSE_ACTION)
            addAction(Constant.TIMER_FRAGMENT_RESUME_ACTION)
            addAction(Constant.TIMER_FRAGMENT_CANCEL_ACTION)
        })

        parentPager = (requireActivity() as MainActivity).getPager()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(timerReceiver)
        requireActivity().unregisterReceiver(serviceReceiver)
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

    private fun lightStatusBar(status: Boolean) {
        if (status) {
            requireActivity().window.let {
                it.statusBarColor = Color.WHITE
                WindowInsetsControllerCompat(it, it.decorView).isAppearanceLightStatusBars = true
            }
        } else {
            requireActivity().window.let {
                it.statusBarColor =
                    requireContext().getColor(R.color.primaryColor)
                WindowInsetsControllerCompat(it, it.decorView).isAppearanceLightStatusBars = false
            }
        }
    }

    private fun enterEnv() {
        lightStatusBar(true)

        (requireActivity() as MainActivity).hideNav()
        binding.tabLayout.apply {
            if (isShown) {
                visibility = View.INVISIBLE
            }
        }
        binding.timerHost.visibility = View.INVISIBLE
        binding.fragmentContainer.visibility = View.VISIBLE
        parentPager.isUserInputEnabled = false
    }

    private fun leaveEnv() {
        lightStatusBar(false)

        (requireActivity() as MainActivity).showNav()
        binding.tabLayout.apply {
            if (!isShown) {
                visibility = View.VISIBLE
            }
        }
        binding.timerHost.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.INVISIBLE
        parentPager.isUserInputEnabled = true
    }
}