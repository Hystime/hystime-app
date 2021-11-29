package top.learningman.hystime.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import top.learningman.hystime.data.Target
import top.learningman.hystime.databinding.FragmentDashboardBinding
import top.learningman.hystime.databinding.ItemDashboardTargetBinding
import top.learningman.hystime.utils.Interface

class DashboardFragment : Fragment(),Interface.RefreshableFragment {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null

    private lateinit var mRecyclerView: RecyclerView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel =
            ViewModelProvider(this)[DashboardViewModel::class.java]

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mRecyclerView = binding.recyclerView

        dashboardViewModel.targetList.observe(viewLifecycleOwner) {
            mRecyclerView.adapter = TargetRecyclerAdapter(dashboardViewModel, it, requireContext())
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TargetRecyclerAdapter(
        val viewModel: DashboardViewModel,
        private val list: List<Target>,
        val context: Context
    ) : RecyclerView.Adapter<TargetRecyclerAdapter.TargetViewHolder>() {
        inner class TargetViewHolder(private val binding: View) : RecyclerView.ViewHolder(binding) {
            fun bind(target: Target) {
                binding
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TargetViewHolder {
            val root = ItemDashboardTargetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return TargetViewHolder(root.root)
        }

        override fun onBindViewHolder(holder: TargetViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    override fun refresh() {
        Toast.makeText(requireContext(), "refresh", Toast.LENGTH_LONG).show()
    }
}