package top.learningman.hystime.ui.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import top.learningman.hystime.R
import top.learningman.hystime.ui.dashboard.ui.DashboardFragment

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DashboardFragment())
                .commitNow()
        }
    }
}