package top.learningman.hystime.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.launch
import top.learningman.hystime.BuildConfig
import top.learningman.hystime.Constant
import top.learningman.hystime.MainApplication
import top.learningman.hystime.R
import top.learningman.hystime.sdk.HystimeClient

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = getString(R.string.setting_filename)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)

        val sp = context?.getSharedPreferences(
            getString(R.string.setting_filename),
            Context.MODE_PRIVATE
        )

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

        setOf(
            getString(R.string.setting_backend_key),
            getString(R.string.setting_auth_key)
        ).forEach { key ->
            val serverTitle =
                preferenceScreen.findPreference<PreferenceCategory>(getString(R.string.setting_category_server_key))!!
            preferenceScreen.findPreference<EditTextPreference>(key)?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { oldValue, newValue ->
                    sp?.let {
                        if (oldValue != newValue) {
                            (requireActivity().application as MainApplication).client =
                                HystimeClient(
                                    sp.getString(getString(R.string.setting_backend_key), "")!!,
                                    sp.getString(getString(R.string.setting_auth_key), "")!!
                                )
                            lifecycleScope.launch {
                                if ((requireActivity().application as MainApplication).client!!.isValid()) {
                                    serverTitle.title =
                                        getString(R.string.setting_category_server_title_valid)
                                } else {
                                    serverTitle.title =
                                        getString(R.string.setting_category_server_title_invalid)
                                }
                            }
                        }
                    } ?: Toast.makeText(context, "SharedPreferences not found", Toast.LENGTH_SHORT)
                        .show()
                    true
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