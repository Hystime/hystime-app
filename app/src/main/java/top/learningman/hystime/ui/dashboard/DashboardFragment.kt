package top.learningman.hystime.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import top.learningman.hystime.MainActivity
import top.learningman.hystime.R
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.databinding.FragmentDashboardBinding
import top.learningman.hystime.databinding.ItemDashboardTargetBinding
import top.learningman.hystime.utils.Interface
import top.learningman.hystime.utils.getUser

class DashboardFragment : Fragment(), Interface.RefreshableFragment {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null

    private lateinit var mRecyclerView: RecyclerView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val root = binding.root

        val toolbar = root.findViewById<Toolbar>(R.id.toolbar)
        val activity = activity as MainActivity
        activity.setSupportActionBar(toolbar)
        val actionbar = activity.supportActionBar
        requireNotNull(actionbar).setTitle(R.string.title_dashboard)
        setHasOptionsMenu(true)

        mRecyclerView = binding.recyclerView

        dashboardViewModel.targetBeanList.observe(viewLifecycleOwner) {
            mRecyclerView.adapter = TargetRecyclerAdapter(dashboardViewModel, it, requireActivity())
        }

        refresh()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TargetRecyclerAdapter(
        val viewModel: DashboardViewModel,
        private val list: List<TargetBean>,
        val context: FragmentActivity
    ) : RecyclerView.Adapter<TargetRecyclerAdapter.TargetViewHolder>() {
        fun Int.toLocalTimeString(): String {
            val hour = this / 3600
            val minute = (this % 3600) / 60
            return "$hour ${context.getString(R.string.hour)} $minute ${context.getString(R.string.minute)}"
        }

        inner class TargetViewHolder(private val binding: ItemDashboardTargetBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(targetBean: TargetBean) {
                binding.title.text = targetBean.name
                binding.timeSpent.text = targetBean.timeSpent.toLocalTimeString()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TargetViewHolder {
            val root = ItemDashboardTargetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return TargetViewHolder(root)
        }

        override fun onBindViewHolder(holder: TargetViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    override fun refresh() {
        dashboardViewModel.refreshTarget(getUser(requireContext()))
    }
}

