package top.learningman.hystime.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.launch
import top.learningman.hystime.*
import top.learningman.hystime.sdk.HystimeClient
import top.learningman.hystime.sdk.errorString
import top.learningman.hystime.utils.Interface
import top.learningman.hystime.utils.getUser

class SettingFragment : PreferenceFragmentCompat(), Interface.RefreshableFragment {
    private val sp by lazy {
        requireContext().getSharedPreferences(
            getString(R.string.setting_filename),
            Context.MODE_PRIVATE
        )
    }

    private lateinit var toolbar: Toolbar
    private val viewModel = ViewModelProvider(this)[MainViewModel::class.java]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        toolbar = requireNotNull(root).findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.title_setting)
        setHasOptionsMenu(true)
        return root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
            val result = HystimeClient.getInstance().refreshValid()
            if (result.isSuccess) {
                serverTitle.title =
                    getString(R.string.setting_category_server_title_valid)
            } else {
                Toast.makeText(
                    context,
                    result.exceptionOrNull()?.errorString() ?: "Unknown error",
                    Toast.LENGTH_LONG
                ).show()
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
            if (HystimeClient.getInstance().isValid()) {
                val usernameQuery =
                    username ?: sp.getString(getString(R.string.setting_username_key), "")!!
                val result = viewModel.refreshUser()
                    if (result.isSuccess) {
                        userTitle.title = getString(R.string.setting_category_user_title_valid)
                    } else {
                        Log.e(
                            "UserCheck",
                            result.exceptionOrNull()?.errorString() ?: "Unknown error"
                        )
                        userTitle.title =
                            getString(R.string.setting_category_user_title_invalid)
                    }
            } else {
                // Apollo client is not valid.
                userTitle.title =
                    getString(R.string.setting_category_user_title_failed)
            }

        }
    }

    override fun refresh() {
        serverCheck(null, null)
    }
}