package top.learningman.hystime.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import top.learningman.hystime.BuildConfig
import top.learningman.hystime.Constant
import top.learningman.hystime.R

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)

        setOf(
            getString(R.string.setting_auth_key),
            getString(R.string.setting_user_key)
        ).forEach { key ->
            preferenceScreen.findPreference<EditTextPreference>(key)
                ?.setOnBindEditTextListener { it.setSingleLine(); }
        }
        getString(R.string.setting_backend_key).let { key ->
            preferenceScreen.findPreference<EditTextPreference>(key)?.setOnBindEditTextListener {
                it.setSingleLine()
                it.inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            }
        }
        getString(R.string.setting_about_key).let { key ->
            preferenceScreen.findPreference<Preference>(key)?.apply {
                summary = "v${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
                setOnPreferenceClickListener {
                    val intent = Intent(context, AboutActivity::class.java)
                    context.startActivity(intent)
                    true
                }
            }
        }

        setOf(
            getString(R.string.setting_pref_normal_key),
            getString(R.string.setting_pref_pomodoro_key)
        ).forEach { key ->
            preferenceScreen.findPreference<Preference>(key)?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(context, SettingActivity::class.java)
                    when (key) {
                        getString(R.string.setting_pref_normal_key) -> R.xml.normal_timer_pref
                        getString(R.string.setting_pref_pomodoro_key) -> R.xml.pomodoro_timer_pref
                        else -> throw IllegalArgumentException("Unknown key: $key")
                    }.let {
                        intent.putExtra(Constant.TIMER_SETTING_INTENT_KEY, it)
                    }
                    context?.startActivity(intent)
                    true
                }
        }

    }
}