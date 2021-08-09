package it.simone.bookyoulove.view.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentChartsTotalBinding
import it.simone.bookyoulove.model.ChartsBookData
import it.simone.bookyoulove.viewmodel.ChartsTotalViewModel
import it.simone.bookyoulove.viewmodel.ChartsViewModel
import it.simone.bookyoulove.viewmodel.TotalChartData
import java.util.*


class ChartsTotalFragment : Fragment() {

    private lateinit var binding : FragmentChartsTotalBinding

    private val chartsVM : ChartsViewModel by activityViewModels()
    private val chartsTotalVM : ChartsTotalViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentChartsTotalBinding.inflate(inflater, container, false)

        setObservers()

        return binding.root
    }


    private fun setObservers() {
        val currentBookDataArrayObserver = Observer<Array<ChartsBookData>> { newArray ->
            chartsTotalVM.setDataArray(newArray)
        }
        chartsVM.currentChartsDataArray.observe(viewLifecycleOwner, currentBookDataArrayObserver)

        val chartsTotalInfoObserver = Observer<TotalChartData> {
            binding.chartsTotalTotalBooksTextView.text = it.totalBooks.toString()
            binding.chartsTotalTotalPagesTextView.text = it.totalPages.toString()

            val supportPieEntries = ArrayList<PieEntry>()
            supportPieEntries.add(PieEntry(it.totalPaperSupport, getString(R.string.paper_string)))
            supportPieEntries.add(PieEntry(it.totalEbookSupport, getString(R.string.ebook_string)))
            supportPieEntries.add(PieEntry(it.totalAudiobookSupport, getString(R.string.audiobook_string)))

            val set = PieDataSet(supportPieEntries, getString(R.string.total_support_string))
            set.colors = ColorTemplate.MATERIAL_COLORS.toList()
            binding.chartsTotalSupportPieChart.setData(PieData(set))
            binding.chartsTotalSupportPieChart.invalidate()
        }
        chartsTotalVM.totalChartData.observe(viewLifecycleOwner, chartsTotalInfoObserver)
    }


}