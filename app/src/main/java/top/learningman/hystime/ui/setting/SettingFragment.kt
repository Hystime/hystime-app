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
import top.learningman.hystime.utils.Interface.RefreshableFragment
import top.learningman.hystime.utils.getUser

class SettingFragment : PreferenceFragmentCompat(), RefreshableFragment {
    private val sp by lazy {
        requireContext().getSharedPreferences(
            getString(R.string.setting_filename),
            Context.MODE_PRIVATE
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)

        serverCheck(null, null)

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

    private fun serverCheck(pref: Preference?, newValue: Any?): Boolean {
        val serverTitle =
            preferenceScreen.findPreference<PreferenceCategory>(getString(R.string.setting_category_server_key))!!
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
        HystimeClient(endpoint, authCode)

        lifecycleScope.launch {
            if (HystimeClient.getInstance().checkValid()) {
                serverTitle.title =
                    getString(R.string.setting_category_server_title_valid)
            } else {
                serverTitle.title =
                    getString(R.string.setting_category_server_title_invalid)
            }
            userCheck(getUser(requireContext()))
        }


        return true
    }

    private fun userCheck(username: String?) {
        val userTitle =
            preferenceScreen.findPreference<PreferenceCategory>(getString(R.string.setting_category_user_key))!!
        lifecycleScope.launch {

                userTitle.title = getString(R.string.setting_category_user_title_pending)
                if (HystimeClient.getInstance().checkValid()) {
                    val usernameQuery =
                        username ?: sp.getString(getString(R.string.setting_username_key), "")!!
                    val user = HystimeClient.getInstance().getUserInfo(usernameQuery)
                    if (user != null) {
                        userTitle.title = getString(R.string.setting_category_user_title_valid)
                    } else {
                        userTitle.title =
                            getString(R.string.setting_category_user_title_invalid)
                    }
                } else {
                    userTitle.title =
                        getString(R.string.setting_category_user_title_failed)
                }

        }
    }

    override fun refresh() {
        serverCheck(null, null)
        userCheck(null)
    }
}