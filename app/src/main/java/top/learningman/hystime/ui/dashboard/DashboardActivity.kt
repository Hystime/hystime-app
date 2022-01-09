package top.learningman.hystime.ui.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import top.learningman.hystime.R
import top.learningman.hystime.ui.dashboard.ui.DashboardFragment
import top.learningman.hystime.utils.LoadingFragment

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        if (savedInstanceState == null) {
            loadFragment()
        }
    }

    fun loadFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, LoadingFragment {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, DashboardFragment().apply {
                        arguments = Bundle().apply {
                            putSerializable(DashboardFragment.FRAGMENT_DATA_KEY, null) // TODO: add real data
                        }
                    })
                    .commitNow()
            })
            .commitNow()
    }
}