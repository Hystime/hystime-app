@file:Suppress("DEPRECATION")

package top.learningman.hystime.ui.setting

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import top.learningman.hystime.Constant
import top.learningman.hystime.R
import top.learningman.hystime.databinding.ActivitySettingsBinding
import top.learningman.hystime.utils.numberPicker.NumberPickerPreferenceCompat
import top.learningman.hystime.utils.numberPicker.NumberPickerPreferenceDialogFragmentCompat


class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
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
                    R.xml.normal_timer_pref -> getString(R.string.setting_pref_normal_title)
                    R.xml.pomodoro_timer_pref -> getString(R.string.setting_pref_pomodoro_title)
                    else -> throw Error("Not found a valid xml ID in Intent")
                }
            } ?: Log.d(
                "Setting", "Support" +
                        "bar not found"
            )
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
        private val dialogFragmentTag = "androidx.preference.PreferenceFragment.DIALOG"

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(type, rootKey)
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            // check if dialog is already showing
            if (parentFragmentManager.findFragmentByTag(dialogFragmentTag) != null) {
                return
            }
            val f = if (preference is NumberPickerPreferenceCompat) {
                NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.key)
            } else {
                null
            }
            f?.let {
                it.setTargetFragment(this, 0)
                it.show(parentFragmentManager, dialogFragmentTag)
            } ?: super.onDisplayPreferenceDialog(preference)
        }


    }
}
