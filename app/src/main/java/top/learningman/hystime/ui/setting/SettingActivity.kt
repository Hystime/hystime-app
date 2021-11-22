package top.learningman.hystime.ui.setting

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import top.learningman.hystime.Constant
import top.learningman.hystime.R
import top.learningman.hystime.databinding.SettingsActivityBinding

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        intent.getIntExtra(Constant.TIMER_SETTING_INTENT_KEY, 0).let {
            if (it == 0) {
                throw Error("Not found a valid xml ID in Intent")
            }
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment(it))
                .commit()

            supportActionBar?.apply {
                title = when (it) {
                    R.xml.normal_timer_pref ->getString(R.string.setting_pref_normal_title)
                    R.xml.pomodoro_timer_pref -> getString(R.string.setting_pref_pomodoro_title)
                    else -> throw Error("Not found a valid xml ID in Intent")
                }
            }?: Log.d("Setting","Support" +
                    "bar not found")
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

    class SettingsFragment(@XmlRes val type: Int) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(type, rootKey)
        }
    }
}