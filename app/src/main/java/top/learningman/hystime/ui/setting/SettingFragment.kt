package top.learningman.hystime.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import top.learningman.hystime.BuildConfig
import top.learningman.hystime.Constant
import top.learningman.hystime.MainViewModel
import top.learningman.hystime.R
import top.learningman.hystime.utils.Interface
import top.learningman.hystime.utils.Status

class SettingFragment : PreferenceFragmentCompat(), Interface.RefreshableFragment {
    private lateinit var toolbar: Toolbar
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        toolbar = root.findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.title_setting)
        toolbar.inflateMenu(R.menu.setting_toolbar_menu)
        toolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.refresh -> {
                    refresh()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        return root
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)

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
                    viewModel.refreshUser(newValue as String)
                    true
                }
        }

        getString(R.string.setting_backend_key).let { key ->
            preferenceScreen.findPreference<EditTextPreference>(key)?.setOnBindEditTextListener {
                it.setSingleLine()
                it.inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            }
        }


        preferenceScreen.findPreference<EditTextPreference>(getString(R.string.setting_backend_key))?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                viewModel.refreshServer(newValue as String, null)
                true
            }

        preferenceScreen.findPreference<EditTextPreference>(getString(R.string.setting_auth_key))?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                viewModel.refreshServer(null, newValue as String)
                true
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
                    }.let { pref_key ->
                        intent.putExtra(Constant.TIMER_SETTING_INTENT_KEY, pref_key)
                    }
                    it.context.startActivity(intent)
                    true
                }
        }

        // ViewModel binding
        viewModel.userStatus.observe(this) {
            preferenceScreen.findPreference<PreferenceCategory>(getString(R.string.setting_category_user_key))
                ?.let { cg ->
                    when (it!!) {
                        Status.SUCCESS -> {
                            cg.title = getString(R.string.setting_category_user_title_valid)
                        }
                        Status.FAILED -> {
                            cg.title = getString(R.string.setting_category_user_title_invalid)
                        }
                        Status.PENDING -> {
                            cg.title = getString(R.string.setting_category_user_title_pending)
                        }
                    }
                }
        }

        viewModel.serverStatus.observe(this) {
            preferenceScreen.findPreference<PreferenceCategory>(getString(R.string.setting_category_server_key))
                ?.let { cg ->
                    when (it!!) {
                        Status.SUCCESS -> {
                            cg.title = getString(R.string.setting_category_server_title_valid)
                        }
                        Status.FAILED -> {
                            cg.title = getString(R.string.setting_category_server_title_invalid)
                        }
                        Status.PENDING -> {
                            cg.title = getString(R.string.setting_category_server_title_pending)
                        }
                    }
                }
        }
    }

    override fun refresh() {
        viewModel.refreshServer(null, null)
        viewModel.showSnackBarMessage("Refreshed Server Status")
    }
}