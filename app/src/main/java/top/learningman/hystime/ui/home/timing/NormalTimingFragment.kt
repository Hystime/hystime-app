package top.learningman.hystime.ui.home.timing

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import top.learningman.hystime.R

class NormalTimingFragment : Fragment() {

    companion object {
        fun newInstance() = NormalTimingFragment()
    }

    private lateinit var viewModel: NormalTimingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_normal_timing, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NormalTimingViewModel::class.java)
        // TODO: Use the ViewModel
    }

}