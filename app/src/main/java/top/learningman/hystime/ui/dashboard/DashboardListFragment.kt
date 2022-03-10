package top.learningman.hystime.ui.dashboard

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import top.learningman.hystime.MainActivity
import top.learningman.hystime.MainViewModel
import top.learningman.hystime.R
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.databinding.DialogAddTargetBinding
import top.learningman.hystime.databinding.FragmentDashboardListBinding
import top.learningman.hystime.databinding.ItemDashboardTargetBinding
import top.learningman.hystime.repo.SharedPrefRepo
import top.learningman.hystime.utils.Interface
import top.learningman.hystime.utils.Status
import top.learningman.hystime.utils.toSafeInt
import type.TargetType


class DashboardListFragment : Fragment(), Interface.RefreshableFragment {

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentDashboardListBinding? = null

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardListBinding.inflate(inflater, container, false)

        val root = binding.root

        toolbar = binding.toolbar
        toolbar.setTitle(R.string.title_dashboard)
        toolbar.inflateMenu(R.menu.dashboard_toolbar_menu)
        toolbar.setOnMenuItemClickListener { item ->
            if (viewModel.userStatus.value != Status.SUCCESS) {
               viewModel.showSnackBarMessage(getString(R.string.msg_login_first))
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.refresh -> {
                    refresh()
                    true
                }
                R.id.add -> {
                    add()
                    true
                }
                R.id.user_info -> {
                    userInfo()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        mRecyclerView = binding.targets
        mRecyclerView.setHasFixedSize(true)

        viewModel.targets.observe(viewLifecycleOwner) {
            viewModel.setCurrentTarget(null)
            if (it.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.targets.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.targets.visibility = View.VISIBLE
                mRecyclerView.swapAdapter(
                    TargetRecyclerAdapter(
                        viewModel,
                        it,
                        requireActivity() as MainActivity
                    ), false
                )
            }
            // TODO: design a diff algorithm
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TargetRecyclerAdapter(
        private val viewModel: MainViewModel,
        private val list: List<TargetBean>,
        val activity: MainActivity
    ) : RecyclerView.Adapter<TargetRecyclerAdapter.TargetViewHolder>() {
        fun Int.toLocalTimeString(): String {
            val hour = this / 3600
            val minute = (this % 3600) / 60
            return "$hour ${activity.getString(R.string.hour)} $minute ${activity.getString(R.string.minute)}"
        }

        inner class TargetViewHolder(private val binding: ItemDashboardTargetBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(targetBean: TargetBean) {
                Log.d("TargetViewHolder", "bind: ${targetBean.name}")
                binding.title.text = targetBean.name
                binding.timeSpent.text = targetBean.timeSpent.toLocalTimeString()
                binding.startTimer.setOnClickListener {
                    viewModel.setCurrentTarget(targetBean)
                    activity.getPager().currentItem = 1
                }
                binding.card.setOnClickListener {
                    val user = SharedPrefRepo.getUser()
                    val intent = DashboardActivity.getTargetIntent(
                        it.context,
                        user,
                        targetBean.id,
                        targetBean.name
                    )
                    it.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TargetViewHolder {
            Log.d("TargetRecyclerAdapter", "onCreateViewHolder")
            val root = ItemDashboardTargetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return TargetViewHolder(root)
        }

        override fun onBindViewHolder(holder: TargetViewHolder, position: Int) {
            Log.d("BindViewHolder", "True")
            holder.bind(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    override fun refresh() {
        viewModel.fetchTarget(null)
        viewModel.showSnackBarMessage(getString(R.string.refresh_target_hint))
    }

    private fun userInfo() {
        val user = SharedPrefRepo.getUser()
        val intent = DashboardActivity.getUserIntent(requireActivity(), user)
        startActivity(intent)
    }

    private fun add() {
        val binding = DialogAddTargetBinding.inflate(layoutInflater, null, false).apply {
            hour.text = Editable.Factory.getInstance().newEditable("0")
            minute.text = Editable.Factory.getInstance().newEditable("0")

            val array = resources.getStringArray(R.array.target_type)
            Log.d("TargetType", array.toString())
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                array
            )
            type.text = Editable.Factory.getInstance().newEditable(array[0])
            type.setAdapter(adapter)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_target)
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.create) { dialog, _ ->
                try {
                    val name = binding.target.text.toString()
                    if (binding.target.text == null || name.isEmpty()) {
                        throw IllegalArgumentException(getString(R.string.target_name_empty))
                    }
                    val hour = binding.hour.text.toString().toSafeInt()
                    val minute = binding.minute.text.toString().toSafeInt()
                    val timeSpent = (60 * hour + minute) * 60
                    val types = resources.getStringArray(R.array.target_type)
                    val type: TargetType = when (binding.type.text.toString()) {
                        types[0] -> TargetType.NORMAL
                        types[1] -> TargetType.LONGTERM
                        else -> TargetType.NORMAL
                    }
                    viewModel.addTarget(name, timeSpent, type)
                    dialog.dismiss()
                } catch (e: Throwable) {
                    viewModel.setError(e)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
            .show()
    }
}

