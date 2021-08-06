package it.simone.bookyoulove.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentChartsBinding
import it.simone.bookyoulove.view.charts.*
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.ChartsViewModel

private const val CHARTS_FRAGMENTS_COUNT = 5


class ChartsFragment : Fragment() {

    private lateinit var binding : FragmentChartsBinding

    private val chartsVM : ChartsViewModel by activityViewModels()

    private var loadingFragment = LoadingDialogFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChartsBinding.inflate(inflater, container, false)
        setObservers()
        chartsVM.getAllChartsData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chartsCollectionAdapter = ChartsCollectionAdapter(this)
        binding.chartsViewPager.adapter = chartsCollectionAdapter

        val chartsTabLayout = binding.chartsTabLayout
        TabLayoutMediator(chartsTabLayout, binding.chartsViewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.total_string)
                1 -> tab.text = getString(R.string.books_string)
                2 -> tab.text = getString(R.string.pages_string)
                3 -> tab.text = getString(R.string.support_string)
                4 -> tab.text = getString(R.string.rates_string)
            }
        }.attach()
    }

    private fun setObservers() {
        val isAccessingDatabaseObserver = Observer<Boolean> {isAccessing ->
            if (isAccessing) {
                loadingFragment.show(childFragmentManager, "Loading Dialog")
            }
            else {
                if (loadingFragment.isAdded) {
                    loadingFragment.dismiss()
                    loadingFragment = LoadingDialogFragment()
                }
            }
        }
        chartsVM.isAccessingDatabase.observe(viewLifecycleOwner, isAccessingDatabaseObserver)
    }
}

class ChartsCollectionAdapter(chartsFragment: Fragment) : FragmentStateAdapter(chartsFragment) {

    override fun getItemCount(): Int {
        return CHARTS_FRAGMENTS_COUNT
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            1 -> ChartsBooksFragment()
            2 -> ChartsPagesFragment()
            3 -> ChartsSupportFragment()
            4 -> ChartsRatesFragment()
            else -> ChartsTotalFragment()
        }
    }

}