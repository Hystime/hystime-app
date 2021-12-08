package top.learningman.hystime.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import top.learningman.hystime.MainActivity
import top.learningman.hystime.MainViewModel
import top.learningman.hystime.R
import top.learningman.hystime.data.TargetBean
import top.learningman.hystime.databinding.FragmentDashboardBinding
import top.learningman.hystime.databinding.ItemDashboardTargetBinding
import top.learningman.hystime.utils.Interface

class DashboardFragment : Fragment(), Interface.RefreshableFragment {

    private lateinit var viewModel: MainViewModel
    private var _binding: FragmentDashboardBinding? = null

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val root = binding.root

        toolbar = root.findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.title_dashboard)
        setHasOptionsMenu(true)

        mRecyclerView = binding.targets
        mRecyclerView.setHasFixedSize(true)

        viewModel.targets.observe(viewLifecycleOwner) {
            viewModel.setCurrentTarget(null)
            mRecyclerView.swapAdapter(
                TargetRecyclerAdapter(
                    viewModel,
                    it,
                    requireActivity() as MainActivity
                ), false
            )
        }

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_toolbar_menu, menu)
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

    class TargetRecyclerAdapter(
        private val viewModel: MainViewModel,
        private val list: List<TargetBean>,
        val context: MainActivity
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
                binding.startTimer.setOnClickListener {
                    viewModel.setCurrentTarget(targetBean)
                    context.getPager().currentItem = 1
                }
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
        viewModel.fetchTarget(null)
        viewModel.showSnackBarMessage(getString(R.string.refresh_target_hint))
    }
}

