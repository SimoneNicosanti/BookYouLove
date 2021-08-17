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
import androidx.navigation.fragment.findNavController
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
import it.simone.bookyoulove.view.ChartsFragmentDirections
import it.simone.bookyoulove.viewmodel.*
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList


class ChartsYearFragment : Fragment(), AdapterView.OnItemSelectedListener, OnChartValueSelectedListener {

    private lateinit var binding : FragmentChartsYearBinding

    private var booksOfTheYearArray = arrayListOf<ChartsBookData>()

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

            for (rates in it.rateMap) {
                setRatesLineCharts(it.rateMap, rates.key)
            }

            booksOfTheYearArray = it.booksOfTheYear
        }
        chartsYearVM.currentChartsYearInfo.observe(viewLifecycleOwner, currentChartsYearInfoObserver)
    }

    private fun setRatesLineCharts(rateMap: Map<String, ArrayList<Float>>, rateKey : String) {
        val rateLineChart = when(rateKey) {
            STYLE_RATE -> binding.chartsYearStyleRateLineChart
            EMOTIONS_RATE -> binding.chartsYearEmotionsRateLineChart
            PLOT_RATE -> binding.chartsYearPlotRateLineChart
            CHARACTER_RATE -> binding.chartsYearCharacterRateLineChart
            else -> binding.chartsYearTotalRateLineChart
        } as LineChart

        val rateEntries = ArrayList<Entry>()
        for (index in 0 until rateMap[rateKey]!!.size) {
            rateEntries.add(Entry((index).toFloat(), rateMap[rateKey]!![index]))
        }
        val chartLabel : String = when(rateKey) {
            STYLE_RATE -> getString(R.string.style_rate_string)
            EMOTIONS_RATE -> getString(R.string.emotions_rate_string)
            PLOT_RATE -> getString(R.string.plot_rate_string)
            CHARACTER_RATE -> getString(R.string.characters_rate_string)
            else -> getString(R.string.total_rate_string)
        }
        val rateChartSet = LineDataSet(rateEntries, chartLabel)
        val rateChartData = LineData(rateChartSet)
        rateLineChart.data = rateChartData

        rateLineChart.setVisibleXRangeMaximum(10F)
        rateLineChart.invalidate()

        rateLineChart.setOnChartValueSelectedListener(this)
    }

    private fun setSupportYearPieChart(it: ChartsYearInfo) {
        val supportPerYearPieChart = binding.chartsYearSupportPieChart as PieChart
        val supportPerYearEntries = ArrayList<PieEntry>()

        if (it.supportPerYear[0] != 0F)supportPerYearEntries.add(PieEntry(it.supportPerYear[0], getString(R.string.paper_string)))
        if (it.supportPerYear[1] != 0F) supportPerYearEntries.add(PieEntry(it.supportPerYear[1], getString(R.string.ebook_string)))
        if (it.supportPerYear[2] != 0F)supportPerYearEntries.add(PieEntry(it.supportPerYear[2], getString(R.string.audiobook_string)))

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
        val pagesPerMonthDataSet = BarDataSet(pagesPerMonthEntries, getString(R.string.pages_per_month_string))
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
        val bookPerMonthBarChartSet = BarDataSet(bookPerMonthBarChartEntries, getString(R.string.books_per_month_string))
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
                    0 -> { //Books
                        binding.chartsYearBooksBarChartCard.visibility = View.VISIBLE
                        binding.chartsYearPagesBarChartCard.visibility = View.GONE
                        binding.chartsYearPagesPieChartCard.visibility = View.GONE
                        binding.chartsYearSupportPieChartCard.visibility = View.GONE
                        binding.chartsYearTotalRateLineChartCard.visibility = View.GONE
                        binding.chartsYearStyleRateLineChartCard.visibility = View.GONE
                        binding.chartsYearEmotionRateLineChartCard.visibility = View.GONE
                        binding.chartsYearPlotRateLineChartCard.visibility = View.GONE
                        binding.chartsYearCharacterRateLineChartCard.visibility = View.GONE
                    }

                    1 -> { //Pages
                        binding.chartsYearPagesBarChartCard.visibility = View.VISIBLE
                        binding.chartsYearPagesPieChartCard.visibility = View.VISIBLE
                        binding.chartsYearBooksBarChartCard.visibility = View.GONE
                        binding.chartsYearSupportPieChartCard.visibility = View.GONE
                        binding.chartsYearTotalRateLineChartCard.visibility = View.GONE
                        binding.chartsYearStyleRateLineChartCard.visibility = View.GONE
                        binding.chartsYearEmotionRateLineChartCard.visibility = View.GONE
                        binding.chartsYearPlotRateLineChartCard.visibility = View.GONE
                        binding.chartsYearCharacterRateLineChartCard.visibility = View.GONE
                    }

                    2 -> { //Supports
                        binding.chartsYearSupportPieChartCard.visibility = View.VISIBLE
                        binding.chartsYearPagesBarChartCard.visibility = View.GONE
                        binding.chartsYearPagesPieChartCard.visibility = View.GONE
                        binding.chartsYearBooksBarChartCard.visibility = View.GONE
                        binding.chartsYearTotalRateLineChartCard.visibility = View.GONE
                        binding.chartsYearStyleRateLineChartCard.visibility = View.GONE
                        binding.chartsYearEmotionRateLineChartCard.visibility = View.GONE
                        binding.chartsYearPlotRateLineChartCard.visibility = View.GONE
                        binding.chartsYearCharacterRateLineChartCard.visibility = View.GONE
                    }

                    3 -> { //Rates
                        binding.chartsYearTotalRateLineChartCard.visibility = View.VISIBLE
                        binding.chartsYearStyleRateLineChartCard.visibility = View.VISIBLE
                        binding.chartsYearEmotionRateLineChartCard.visibility = View.VISIBLE
                        binding.chartsYearPlotRateLineChartCard.visibility = View.VISIBLE
                        binding.chartsYearCharacterRateLineChartCard.visibility = View.VISIBLE

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
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null) {
            val x = e.x.toInt()
            //Log.d("Nicosanti", "$x ${chartsBookDataArray[x].title}")
            val rateChartSnackbar = Snackbar.make(requireView(), booksOfTheYearArray[x].title, Snackbar.LENGTH_SHORT)
            rateChartSnackbar.setAction(getString(R.string.goto_string)) {
                findNavController().navigate(
                    ChartsFragmentDirections.actionChartsFragmentToEndedDetailFragment(
                        booksOfTheYearArray[x].keyTitle,
                        booksOfTheYearArray[x].keyAuthor,
                        booksOfTheYearArray[x].readTime
                    )
                )
            }
            rateChartSnackbar.show()
        }
    }

    override fun onNothingSelected() {
    }

}