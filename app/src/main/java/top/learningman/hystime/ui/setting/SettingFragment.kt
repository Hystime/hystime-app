package top.learningman.hystime.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.launch
import top.learningman.hystime.BuildConfig
import top.learningman.hystime.Constant
import top.learningman.hystime.R
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.utils.requireClient
import top.learningman.hystime.utils.setClient

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)

        val sp = context?.getSharedPreferences(
            getString(R.string.setting_filename),
            Context.MODE_PRIVATE
        )!!
        val serverTitle =
            preferenceScreen.findPreference<PreferenceCategory>(getString(R.string.setting_category_server_key))!!

        fun serverCheck(pref: Preference?, newValue: Any?): Boolean {
            serverTitle.title = getString(R.string.setting_category_server_title_pending)

            var endpoint = sp.getString(getString(R.string.setting_backend_key), "")!!
            var authCode = sp.getString(getString(R.string.setting_auth_key), "")!!
            pref?.let {
                when (pref.key) {
                    getString(R.string.setting_backend_key) -> {
                        endpoint = newValue as String
                    }
                    getString(R.string.setting_auth_key) -> {
                        authCode = newValue as String
                    }
                }
            }
            setClient(
                this, HystimeClient(
                    endpoint, authCode
                )
            )
            lifecycleScope.launch {
                if (requireClient(this@SettingFragment)!!.isValid()) {
                    serverTitle.title =
                        getString(R.string.setting_category_server_title_valid)
                } else {
                    serverTitle.title =
                        getString(R.string.setting_category_server_title_invalid)
                }
            }


            return true
        }

        fun userCheck(username:String?) {
            val userTitle =
                preferenceScreen.findPreference<PreferenceCategory>(getString(R.string.setting_category_user_key))!!
            lifecycleScope.launch {
                requireClient(this@SettingFragment)?.let {
                    if (it.isValid()) {
                        val usernameQuery =
                            username ?: sp.getString(getString(R.string.setting_username_key), "")!!
                        val user = requireClient(this@SettingFragment)!!.getUserInfo(usernameQuery)
                        if (user != null) {
                            userTitle.title = getString(R.string.setting_category_user_key)
                        } else {
                            userTitle.title =
                                getString(R.string.setting_category_user_title_invalid)
                        }
                    }
                } ?: run {
                    userTitle.title = getString(R.string.setting_category_user)
                }
            }
        }

        serverCheck(null, null)
        userCheck(null)


        setOf(
            getString(R.string.setting_auth_key),
            getString(R.string.setting_username_key)
        ).forEach { key ->
            preferenceScreen.findPreference<EditTextPreference>(key)
                ?.setOnBindEditTextListener { it.setSingleLine(); }
        }

        getString(R.string.setting_username_key).let {
            preferenceScreen.findPreference<EditTextPreference>(it)
                ?.setOnPreferenceChangeListener { _, newValue ->
                    userCheck(newValue as String)
                    true
                }
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
            preferenceScreen.findPreference<EditTextPreference>(key)?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { pref, newValue ->
                    serverCheck(pref, newValue)
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