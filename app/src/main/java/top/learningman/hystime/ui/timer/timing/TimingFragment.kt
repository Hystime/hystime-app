package top.learningman.hystime.ui.timer.timing

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import top.learningman.hystime.MainViewModel
import top.learningman.hystime.R


open class TimingFragment : Fragment() {
    val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialButton>(R.id.target).setOnClickListener {
            val targets = mainViewModel.targets.value!!.map {
                it.name
            }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_target_title))
                .setItems(targets) { _, which ->
                    mainViewModel.setCurrentTarget(targets[which])
                }.show()
        }
    }
}