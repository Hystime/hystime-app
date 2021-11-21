package top.learningman.hystime.ui.setting

import android.os.Bundle
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
        setContentView(R.layout.settings_activity)
    }

    override fun onResume() {
        super.onResume()
        intent.getIntExtra(Constant.TIMER_SETTING_INTENT_KEY, 0).let {
            if (it == 0) {
                throw Error("Not found a valid xml ID in Intent")
            }
            supportFragmentManager.beginTransaction().replace(
                R.id.settings,
                SettingsFragment(it)
            ).commit()

            //FIXME: not display title
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.title = when (it) {
                R.xml.normal_timer_pref ->getString(R.string.setting_pref_normal_title)
                R.xml.pomodoro_timer_pref -> getString(R.string.setting_pref_pomodoro_title)
                else -> throw Error("Not found a valid xml ID in Intent")
            }
        }
    }

    class SettingsFragment(@XmlRes val type: Int) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(type, rootKey)
        }
    }
}