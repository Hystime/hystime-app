package top.learningman.hystime.ui.dashboard

import TargetStatisticQuery
import UserStatisticQuery
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import top.learningman.hystime.R
import top.learningman.hystime.data.TimePieceBean
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.ui.dashboard.ui.DashboardFragment
import top.learningman.hystime.utils.LoadingFragment
import top.learningman.hystime.view.HeatMapView
import java.io.Serializable
import java.util.*

class DashboardActivity : AppCompatActivity() {
    enum class Type {
        USER,
        TARGET
    }

    data class Statistic(
        val pomodoroCount: Int,
        val todayPomodoroCount: Int,
        val timeSpent: Int,
        val todayTimeSpent: Int,
        val tpStart: Date?,
        val tpDuration: Int?,
        val tpType: TimePieceBean.TimePieceType?,
        val type: Type,
        val tpTargetId: String?,
        val tpTargetName: String?,
        val username: String,
        val heatMap: HeatMapView.Companion.Cal,
    ) : Serializable {
        fun hasTimepiece(): Boolean {
            return tpStart != null && tpDuration != null && tpType != null
        }

        companion object {
            fun fromUser(username: String, input: UserStatisticQuery.User): Statistic {
                return Statistic(
                    input.pomodoroCount,
                    input.todayPomodoroCount,
                    input.timeSpent,
                    input.todayTimeSpent,
                    input.timePieces.edges.firstOrNull()?.node?.start,
                    input.timePieces.edges.firstOrNull()?.node?.duration,
                    TimePieceBean.TimePieceType.valueOf(input.timePieces.edges.firstOrNull()?.node?.type.toString()),
                    Type.USER,
                    input.timePieces.edges.firstOrNull()?.node?.target?.id,
                    input.timePieces.edges.firstOrNull()?.node?.target?.name,
                    username,
                    HeatMapView.Companion.Cal(input.heatMap.start, input.heatMap.data)
                )
            }

            fun fromTarget(
                username: String,
                targetId: String,
                input: TargetStatisticQuery.Target
            ): Statistic {
                return Statistic(
                    input.pomodoroCount,
                    input.todayPomodoroCount,
                    input.timeSpent,
                    input.todayTimeSpent,
                    input.timePieces.edges.firstOrNull()?.node?.start,
                    input.timePieces.edges.firstOrNull()?.node?.duration,
                    TimePieceBean.TimePieceType.valueOf(input.timePieces.edges.firstOrNull()?.node?.type.toString()),
                    Type.TARGET,
                    targetId,
                    null,
                    username,
                    HeatMapView.Companion.Cal(input.heatMap.start, input.heatMap.data)
                )
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            loadFragment()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment() {
        fun switchFragment(data: Statistic) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DashboardFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(
                            DashboardFragment.FRAGMENT_DATA_KEY,
                            data
                        )
                    }
                })
                .commitNow()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, LoadingFragment {
                lifecycleScope.launch {
                    val client = HystimeClient.getInstance()
                    var data: Statistic? = null
                    val type = intent.getSerializableExtra(TYPE_KEY) as Type

                    when (type) {
                        Type.USER -> {
                            val username = intent.getStringExtra(USER_NAME_KEY)
                            supportActionBar?.title = username
                            client.getUserStatistic(username!!)
                        }
                        Type.TARGET -> {
                            val targetId = intent.getStringExtra(TARGET_ID_KEY)
                            val username = intent.getStringExtra(USER_NAME_KEY)
                            val targetName = intent.getStringExtra(TARGET_NAME_KEY)
                            supportActionBar?.title = targetName
                            client.getTargetStatistic(username!!, targetId!!)
                        }
                    }.fold({
                        data = when (type) {
                            Type.USER -> {
                                val username = intent.getStringExtra(USER_NAME_KEY)!!
                                Statistic.fromUser(username, it as UserStatisticQuery.User)
                            }
                            Type.TARGET -> {
                                val targetId = intent.getStringExtra(TARGET_ID_KEY)!!
                                val username = intent.getStringExtra(USER_NAME_KEY)!!
                                Statistic.fromTarget(
                                    username,
                                    targetId,
                                    it as TargetStatisticQuery.Target
                                )
                            }
                        }
                    }, { throwable ->
                        throwable.message?.let {
                            Toast.makeText(this@DashboardActivity, it, Toast.LENGTH_LONG).show()
                        }
                        finish()
                        return@launch
                    })
                    switchFragment(data!!)
                }
            })
            .commitNow()
    }

    companion object {
        private const val TYPE_KEY = "type_key"
        private const val USER_NAME_KEY = "user_name_key"
        private const val TARGET_ID_KEY = "target_id_key"
        private const val TARGET_NAME_KEY = "target_name_key"

        fun getUserIntent(context: Context, username: String): Intent {
            return Intent(context, DashboardActivity::class.java).apply {
                putExtra(TYPE_KEY, Type.USER)
                putExtra(USER_NAME_KEY, username)
            }
        }

        fun getTargetIntent(
            context: Context,
            username: String,
            targetId: String,
            targetName: String
        ): Intent {
            return Intent(context, DashboardActivity::class.java).apply {
                putExtra(TYPE_KEY, Type.TARGET)
                putExtra(USER_NAME_KEY, username)
                putExtra(TARGET_ID_KEY, targetId)
                putExtra(TARGET_NAME_KEY, targetName)
            }
        }
    }
}