package top.learningman.hystime.ui.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import top.learningman.hystime.R
import top.learningman.hystime.data.TimePieceBean
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.sdk.HystimeClient.Companion.getInput
import top.learningman.hystime.utils.dateShortFormat

class TimePieceActivity : AppCompatActivity() {
    private val client by HystimeClient.Companion.Client()

    private lateinit var type: DashboardActivity.Type
    private lateinit var username: String
    private lateinit var targetId: String

    private var after: String? = null
    private var hasNext = true

    data class Item(val tp: TimePieceBean?, val isDate: Boolean, val date: String?)

    private val data = object {
        val itemList = mutableListOf<Item>()
        private var currentDate: String? = null

        fun extend(tps: List<TimePieceBean>) {
            for (tp in tps) {
                val date = tp.start.dateShortFormat()
                if (currentDate != date) {
                    currentDate = date
                    itemList.add(Item(null, true, date))
                }
                itemList.add(Item(tp, false, null))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_piece)

        with(intent.getBundleExtra(INTENT_BUNDLE_KEY)!!) {
            type = getSerializable(BUNDLE_TYPE_KEY) as DashboardActivity.Type
            getString(BUNDLE_USERNAME_KEY)?.let { username = it }
            getString(BUNDLE_TARGET_ID_KEY)?.let { targetId = it }
        }

        supportActionBar?.apply {
            title = getString(R.string.timepieces)
            setDisplayHomeAsUpEnabled(true)
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

    suspend fun userLoadMore() {
        if (!hasNext) return
        client.getUserTimePieces(username, 30, getInput(after)).fold(
            {
                hasNext = it.pageInfo.hasNextPage
                after = it.pageInfo.endCursor
                val value = it.edges.map { edge ->
                    with(edge.node) {
                        return@with TimePieceBean(
                            id,
                            start,
                            duration,
                            TimePieceBean.TimePieceType.fromString(type.toString())
                        )
                    }
                }
                data.extend(value)
                //TODO: notify data changed
            },
            {
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show()
                Log.e("TimePieceActivity", "UserLoadMore", it)
            }
        )
    }

    suspend fun targetLoadMore() {
        if (!hasNext) return
        client.getTargetTimePieces(username, targetId, 30, getInput(after)).fold(
            {
                hasNext = it.pageInfo.hasNextPage
                after = it.pageInfo.endCursor
                val value = it.edges.map { edge ->
                    with(edge.node) {
                        return@with TimePieceBean(
                            id,
                            start,
                            duration,
                            TimePieceBean.TimePieceType.fromString(type.toString())
                        )
                    }
                }
                data.extend(value)
                //TODO: notify data changed
            },
            {
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show()
                Log.e("TimePieceActivity", "TargetLoadMore", it)
            }
        )
    }

    companion object {
        const val INTENT_BUNDLE_KEY = "bundle_key"

        const val BUNDLE_TYPE_KEY = "type"
        const val BUNDLE_USERNAME_KEY = "work"
        const val BUNDLE_TARGET_ID_KEY = "target_id"
    }
}