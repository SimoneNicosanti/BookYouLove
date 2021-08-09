package it.simone.bookyoulove.view.charts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.snackbar.Snackbar
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentChartsYearBinding
import it.simone.bookyoulove.model.ChartsBookData
import it.simone.bookyoulove.viewmodel.ChartsViewModel
import it.simone.bookyoulove.viewmodel.ChartsYearInfo
import it.simone.bookyoulove.viewmodel.ChartsYearViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList


class ChartsYearFragment : Fragment(), AdapterView.OnItemSelectedListener, OnChartValueSelectedListener {

    private lateinit var binding : FragmentChartsYearBinding

    private lateinit var chartsBookDataArray : Array<ChartsBookData>

    private val chartsVM : ChartsViewModel by activityViewModels()
    private val chartsYearVM : ChartsYearViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentChartsYearBinding.inflate(inflater, container, false)

        binding.chartsYearSpinner.onItemSelectedListener = this
        binding.chartsYearChartTypeSpinner.onItemSelectedListener = this

        val arrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.charts_year_chart_type_array))
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //Quando è impostato l'adapter viene chiamata la funzione onItemSelected che invoca l'aggiornamento delle statistiche
        binding.chartsYearChartTypeSpinner.adapter = arrayAdapter

        setObservers()
        return binding.root
    }

    private fun setObservers() {

        val currentChartsDataArrayObserver = Observer<Array<ChartsBookData>> {
            chartsYearVM.setChartsDataArray(it)
            chartsBookDataArray = it
        }
        chartsVM.currentChartsDataArray.observe(viewLifecycleOwner, currentChartsDataArrayObserver)

        val currentYearListObserver = Observer<Array<Int>> {
            val arrayAdapter = ArrayAdapter<Int>(requireContext(), android.R.layout.simple_spinner_item, it)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            //Quando è impostato l'adapter viene chiamata la funzione onItemSelected che invoca l'aggiornamento delle statistiche
            binding.chartsYearSpinner.adapter = arrayAdapter
        }
        chartsYearVM.currentYearList.observe(viewLifecycleOwner, currentYearListObserver)

        val currentChartsYearInfoObserver = Observer<ChartsYearInfo> {
            setBookPerMonthBarChart(it)
            setPagesPerMonthBarChart(it)
            setPagesPerMonthPieChart(it)
            setSupportYearPieChart(it)
            setTotalRateLineChart(it)
        }
        chartsYearVM.currentChartsYearInfo.observe(viewLifecycleOwner, currentChartsYearInfoObserver)
    }

    private fun setTotalRateLineChart(it: ChartsYearInfo) {
        val totalRateLineChart = binding.chartsYearTotalRateLineChart as LineChart
        val totalRateEntries = ArrayList<Entry>()
        for (index in 0 until it.totalRateArray.size) {
            totalRateEntries.add(Entry((index + 2).toFloat(), it.totalRateArray[index]))
        }
        val totalRateSet = LineDataSet(totalRateEntries, "Total Rate")
        val totalRateData = LineData(totalRateSet)
        totalRateLineChart.data = totalRateData
        totalRateLineChart.invalidate()

        totalRateLineChart.setOnChartValueSelectedListener(this)
    }

    private fun setSupportYearPieChart(it: ChartsYearInfo) {
        val supportPerYearPieChart = binding.chartsYearSupportPieChart as PieChart
        val supportPerYearEntries = ArrayList<PieEntry>()

        if (it.supportPerYear[0] != 0F)supportPerYearEntries.add(PieEntry(it.supportPerYear[0], "Paper"))
        if (it.supportPerYear[1] != 0F) supportPerYearEntries.add(PieEntry(it.supportPerYear[1], "eBook"))
        if (it.supportPerYear[2] != 0F)supportPerYearEntries.add(PieEntry(it.supportPerYear[2], "AudioBook"))

        val supportPerYearSet = PieDataSet(supportPerYearEntries, "")
        supportPerYearSet.colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.COLORFUL_COLORS.toList()
        val supportPerYearData = PieData(supportPerYearSet)
        supportPerYearPieChart.data = supportPerYearData
        //pagesPerMonthPieChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthArrayLabels.toTypedArray())
        //supportPerYearPieChart.setBackgroundColor(resources.getColor(R.color.white))
        supportPerYearPieChart.animateXY(3000, 3000)
        supportPerYearPieChart.invalidate()
    }

    private fun setPagesPerMonthPieChart(it: ChartsYearInfo) {
        val pagesPerMonthPieChart = binding.chartsYearPagesPieChart as PieChart
        val pagesPerMonthEntries = ArrayList<PieEntry>()
        val monthArrayLabels = ArrayList<String>()
        for (monthIndex in 0..11) {
            if (it.pagesPerMonth[monthIndex] != 0F) pagesPerMonthEntries.add(PieEntry(it.pagesPerMonth[monthIndex], Month.of(monthIndex + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault()).capitalize(Locale.getDefault())))
            monthArrayLabels.add(Month.of(monthIndex + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault()).capitalize(Locale.getDefault()))
        }
        val pagesPerMonthDataSet = PieDataSet(pagesPerMonthEntries, "")
        pagesPerMonthDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.COLORFUL_COLORS.toList()
        val pagesPerMonthData = PieData(pagesPerMonthDataSet)
        pagesPerMonthPieChart.data = pagesPerMonthData
        //pagesPerMonthPieChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthArrayLabels.toTypedArray())
        //pagesPerMonthPieChart.setBackgroundColor(resources.getColor(R.color.white))
        pagesPerMonthPieChart.animateXY(3000, 3000)
        pagesPerMonthPieChart.invalidate()
    }

    private fun setPagesPerMonthBarChart(it: ChartsYearInfo) {
        val pagesPerMonthBarChart = binding.chartsYearPagesBarChart as BarChart
        val pagesPerMonthEntries = ArrayList<BarEntry>()
        val monthArrayLabels = ArrayList<String>()
        for (monthIndex in 0..11) {
            pagesPerMonthEntries.add(BarEntry(monthIndex.toFloat(), it.pagesPerMonth[monthIndex]))
            monthArrayLabels.add(Month.of(monthIndex + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault()).capitalize(Locale.getDefault()))
        }
        val pagesPerMonthDataSet = BarDataSet(pagesPerMonthEntries, "Pages Per Month")
        pagesPerMonthDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val pagesPerMonthData = BarData(pagesPerMonthDataSet)
        pagesPerMonthBarChart.data = pagesPerMonthData
        pagesPerMonthBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthArrayLabels.toTypedArray())
        pagesPerMonthBarChart.setVisibleXRangeMaximum(6F)
        //pagesPerMonthBarChart.setBackgroundColor(resources.getColor(R.color.white))

        pagesPerMonthBarChart.invalidate()
    }


    private fun setBookPerMonthBarChart(it: ChartsYearInfo) {
        val barChart = binding.chartsYearBooksBarChart as BarChart
        val bookPerMonthBarChartEntries = ArrayList<BarEntry>()
        val monthArrayLabels = ArrayList<String>()
        for (monthIndex in 0..11) {
            bookPerMonthBarChartEntries.add(BarEntry(monthIndex.toFloat(), it.bookPerMonth[monthIndex].toFloat()))
            monthArrayLabels.add(Month.of(monthIndex + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault()).capitalize(Locale.getDefault()))
        }
        val bookPerMonthBarChartSet = BarDataSet(bookPerMonthBarChartEntries, "Book Per Month")
        bookPerMonthBarChartSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val bookPerMonthBarChartData = BarData(bookPerMonthBarChartSet)
        barChart.data = bookPerMonthBarChartData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthArrayLabels.toTypedArray())
        barChart.setVisibleXRangeMaximum(6F)
        barChart.setVisibleXRangeMinimum(3F)
        //barChart.setBackgroundColor(resources.getColor(R.color.white))
        barChart.invalidate()
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when(parent) {
            binding.chartsYearSpinner -> {
                val selectedYear = parent.getItemAtPosition(position) as Int
                chartsYearVM.changeSelectedYear(selectedYear)
            }

            binding.chartsYearChartTypeSpinner -> {
                when (position) {
                    0 -> {
                        binding.chartsYearBooksBarChartCard.visibility = View.VISIBLE
                        binding.chartsYearPagesBarChartCard.visibility = View.GONE
                        binding.chartsYearPagesPieChartCard.visibility = View.GONE
                        binding.chartsYearSupportPieChartCard.visibility = View.GONE
                        binding.chartsYearTotalRateLineChartCard.visibility = View.GONE
                    }

                    1 -> {
                        binding.chartsYearPagesBarChartCard.visibility = View.VISIBLE
                        binding.chartsYearPagesPieChartCard.visibility = View.VISIBLE
                        binding.chartsYearBooksBarChartCard.visibility = View.GONE
                        binding.chartsYearSupportPieChartCard.visibility = View.GONE
                        binding.chartsYearTotalRateLineChartCard.visibility = View.GONE

                    }

                    2 -> {
                        binding.chartsYearSupportPieChartCard.visibility = View.VISIBLE
                        binding.chartsYearPagesBarChartCard.visibility = View.GONE
                        binding.chartsYearPagesPieChartCard.visibility = View.GONE
                        binding.chartsYearBooksBarChartCard.visibility = View.GONE
                        binding.chartsYearTotalRateLineChartCard.visibility = View.GONE
                    }

                    3 -> {
                        binding.chartsYearTotalRateLineChartCard.visibility = View.VISIBLE
                        binding.chartsYearBooksBarChartCard.visibility = View.GONE
                        binding.chartsYearPagesBarChartCard.visibility = View.GONE
                        binding.chartsYearPagesPieChartCard.visibility = View.GONE
                        binding.chartsYearSupportPieChartCard.visibility = View.GONE
                    }
                }
            }
        }
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        //TODO("Devo poter passare il titolo (ed eventualmente il resto della chiave se voglio implementare la ridirezione) nella lista delle entry che vengono inserite, potrei usare un array di array")
    }

    override fun onNothingSelected() {
    }

}