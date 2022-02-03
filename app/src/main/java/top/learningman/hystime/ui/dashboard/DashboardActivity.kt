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
        val type: Type
    ) : Serializable {
        companion object {
            fun fromUser(type: Type, input: UserStatisticQuery.User): Statistic {
                return Statistic(
                    input.pomodoroCount,
                    input.todayPomodoroCount,
                    input.timeSpent,
                    input.todayTimeSpent,
                    input.timePieces.edges.firstOrNull()?.node?.start,
                    input.timePieces.edges.firstOrNull()?.node?.duration,
                    TimePieceBean.TimePieceType.valueOf(input.timePieces.edges.firstOrNull()?.node?.type.toString()),
                    type
                )
            }

            fun fromTarget(type: Type, input: TargetStatisticQuery.Target): Statistic {
                return Statistic(
                    input.pomodoroCount,
                    input.todayPomodoroCount,
                    input.timeSpent,
                    input.todayTimeSpent,
                    input.timePieces.edges.firstOrNull()?.node?.start,
                    input.timePieces.edges.firstOrNull()?.node?.duration,
                    TimePieceBean.TimePieceType.valueOf(input.timePieces.edges.firstOrNull()?.node?.type.toString()),
                    type
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
                            val userId = intent.getStringExtra(USER_ID_KEY)
                            supportActionBar?.title = userId
                            client.getUserStatistic(userId!!)
                        }
                        Type.TARGET -> {
                            val targetId = intent.getStringExtra(TARGET_ID_KEY)
                            val username = intent.getStringExtra(USER_ID_KEY)
                            val targetName = intent.getStringExtra(TARGET_NAME_KEY)
                            supportActionBar?.title = targetName
                            client.getTargetStatistic(username!!, targetId!!)
                        }
                    }.fold({
                        data = when (type) {
                            Type.USER -> Statistic.fromUser(type, it as UserStatisticQuery.User)
                            Type.TARGET -> Statistic.fromTarget(
                                type,
                                it as TargetStatisticQuery.Target
                            )
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

    override fun finish() {
        super.finish()
    }

    companion object {
        private const val TYPE_KEY = "type_key"
        private const val USER_ID_KEY = "user_id_key"
        private const val TARGET_ID_KEY = "target_id_key"
        private const val TARGET_NAME_KEY = "target_name_key"

        fun getUserIntent(context: Context, username: String): Intent {
            return Intent(context, DashboardActivity::class.java).apply {
                putExtra(TYPE_KEY, Type.USER)
                putExtra(USER_ID_KEY, username)
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
                putExtra(USER_ID_KEY, username)
                putExtra(TARGET_ID_KEY, targetId)
                putExtra(TARGET_NAME_KEY, targetName)
            }
        }
    }
}