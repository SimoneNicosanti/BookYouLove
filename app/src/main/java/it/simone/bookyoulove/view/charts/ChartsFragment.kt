package it.simone.bookyoulove.view

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentChartsBinding
import it.simone.bookyoulove.view.charts.*
import it.simone.bookyoulove.view.dialog.LoadingDialogFragment
import it.simone.bookyoulove.viewmodel.ChartsViewModel

private const val CHARTS_FRAGMENTS_COUNT = 2


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
                1 -> tab.text = getString(R.string.year_string)

            }
        }.attach()

        /*
            Disabilita lo Swiping del ViewPager. Il Motivo per cui lo faccio è che questo può andare ad interferire
            quando l'utente fa lo scroll dei grafici nella pagina successiva
         */
        binding.chartsViewPager.isUserInputEnabled = false

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setObservers() {
        val isAccessingDatabaseObserver = Observer<Boolean> {isAccessing ->
            if (isAccessing) {
                requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.chartsLoading.root.visibility = View.VISIBLE
            }

            else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.chartsLoading.root.visibility = View.GONE
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
            1 -> ChartsYearFragment()
            else -> ChartsTotalFragment()
        }
    }

}