package it.simone.bookyoulove.view.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import it.simone.bookyoulove.Constants.CHARACTER_RATE
import it.simone.bookyoulove.Constants.EMOTIONS_RATE
import it.simone.bookyoulove.Constants.ENDED_DETAIL_ENTRY_CODE_FROM_CHARTS
import it.simone.bookyoulove.Constants.PLOT_RATE
import it.simone.bookyoulove.Constants.STYLE_RATE
import it.simone.bookyoulove.R
import it.simone.bookyoulove.databinding.FragmentChartsYearBinding
import it.simone.bookyoulove.model.ChartsBookData
import it.simone.bookyoulove.model.ChartsYearInfo
import it.simone.bookyoulove.view.setViewEnable
import it.simone.bookyoulove.viewmodel.charts.*
import java.text.DateFormatSymbols
import java.util.*
import kotlin.collections.ArrayList





//Non posso inserire animazione sul constraintlayout perché entra in conflitto con l'animazione del ViewPager

class ChartsYearFragment : Fragment(), AdapterView.OnItemSelectedListener, OnChartValueSelectedListener {

    companion object {
        const val TEXT_DATA_SIZE = 10F
    }

    private lateinit var binding : FragmentChartsYearBinding

    private var booksOfTheYearArray = arrayListOf<ChartsBookData>()

    private val chartsVM : ChartsViewModel by activityViewModels()




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentChartsYearBinding.inflate(inflater, container, false)

        setViewEnable(true, requireActivity())

        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.charts_year_chart_type_array))
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //Quando è impostato l'adapter viene chiamata la funzione onItemSelected che invoca l'aggiornamento delle statistiche
        binding.chartsYearChartTypeSpinner.adapter = arrayAdapter

        binding.chartsYearChartTypeSpinner.onItemSelectedListener = this
        binding.chartsYearSpinner.onItemSelectedListener = this

        setObservers()
        return binding.root
    }


    private fun setObservers() {

        val readyDataObserver = Observer<Boolean> {
            if (it) chartsVM.getYearList()
        }
        chartsVM.readyArray.observe(viewLifecycleOwner, readyDataObserver)

        val currentYearListObserver = Observer<Array<Int>> {
            val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            //Quando è impostato l'adapter viene chiamata la funzione onItemSelected che invoca l'aggiornamento delle statistiche
            binding.chartsYearSpinner.adapter = arrayAdapter
        }
        chartsVM.currentYearList.observe(viewLifecycleOwner, currentYearListObserver)

        val yearChartDataObserver = Observer<ChartsYearInfo> {

            setBookPerMonthBarChart(it)
            setPagesPerMonthBarChart(it)
            setPagesPerMonthPieChart(it)
            setSupportYearPieChart(it)

            for (rates in it.rateMap) {
                setRatesLineCharts(it.rateMap, rates.key)
            }

            setBooksYearTotal(it)
            setPagesYearTotal(it)

            booksOfTheYearArray = it.booksOfTheYear
        }
        chartsVM.yearChartData.observe(viewLifecycleOwner, yearChartDataObserver)
    }

    private fun setPagesYearTotal(it: ChartsYearInfo) {
        var totalYearPages = 0F
        for (monthIndex in 0 until 12) totalYearPages += it.pagesPerMonth[monthIndex]
        binding.chartsYearPagesInclude.chartsYearPagesTotalTextView.text = totalYearPages.toLong().toString()
    }

    private fun setBooksYearTotal(it: ChartsYearInfo) {
        val totalYearBooks = it.booksOfTheYear.size
        binding.chartsYearBooksInclude.chartsYearBooksTotalTextView.text = totalYearBooks.toString()
    }

    private fun setRatesLineCharts(rateMap: Map<String, ArrayList<Float>>, rateKey : String) {
        val rateLineChart = when(rateKey) {
            STYLE_RATE ->  requireView().findViewById<LineChart>(R.id.chartsYearStyleRateLineChart)!!
            EMOTIONS_RATE -> requireView().findViewById(R.id.chartsYearEmotionsRateLineChart)
            PLOT_RATE -> requireView().findViewById(R.id.chartsYearPlotRateLineChart)
            CHARACTER_RATE -> requireView().findViewById(R.id.chartsYearCharacterRateLineChart)
            else -> requireView().findViewById(R.id.chartsYearTotalRateLineChart)
        }

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
        rateLineChart?.run {
            val editTextColor = EditText(requireContext()).currentTextColor
            data = rateChartData
            legend.textColor = editTextColor
            setVisibleXRangeMaximum(10F)

            animateXY(1000, 1000)
            setScaleEnabled(false)

            description?.isEnabled = false

            axisLeft?.run {
                textColor = editTextColor
                //setDrawGridLines(false)
            }
            xAxis?.run {
                textColor = editTextColor
                setDrawGridLines(false)
            }
            data.setValueTextColor(editTextColor)

            axisRight?.isEnabled = false
            invalidate()
        }
        rateLineChart?.setOnChartValueSelectedListener(this)
    }

    private fun setSupportYearPieChart(it: ChartsYearInfo) {
        val supportPerYearPieChart = requireView().findViewById<PieChart>(R.id.chartsYearSupportPieChart)
        val supportPerYearEntries = ArrayList<PieEntry>()

        if (it.supportPerYear[0] != 0F)supportPerYearEntries.add(PieEntry(it.supportPerYear[0], getString(R.string.paper_string)))
        if (it.supportPerYear[1] != 0F) supportPerYearEntries.add(PieEntry(it.supportPerYear[1], getString(R.string.ebook_string)))
        if (it.supportPerYear[2] != 0F)supportPerYearEntries.add(PieEntry(it.supportPerYear[2], getString(R.string.audiobook_string)))

        val supportPerYearSet = PieDataSet(supportPerYearEntries, "")
        supportPerYearSet.colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.COLORFUL_COLORS.toList()
        val supportPerYearData = PieData(supportPerYearSet)
        supportPerYearPieChart?.run {
            data = supportPerYearData
            description?.isEnabled = false
            //pagesPerMonthPieChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthArrayLabels.toTypedArray())
            //supportPerYearPieChart.setBackgroundColor(resources.getColor(R.color.white))
            legend.textColor = EditText(requireContext()).currentTextColor
            animateXY(3000, 3000)
            invalidate()
        }
    }

    private fun setPagesPerMonthPieChart(it: ChartsYearInfo) {
        val pagesPerMonthPieChart = requireView().findViewById<PieChart>(R.id.chartsYearPagesPieChart)
        val pagesPerMonthEntries = ArrayList<PieEntry>()
        val monthArrayLabels = ArrayList<String>()
        for (monthIndex in 0..11) {
            val monthString = DateFormatSymbols(Locale.getDefault()).shortMonths[monthIndex]
            if (it.pagesPerMonth[monthIndex] != 0F) pagesPerMonthEntries.add(PieEntry(it.pagesPerMonth[monthIndex], monthString.capitalize(Locale.getDefault())))
            monthArrayLabels.add(monthString.capitalize(Locale.getDefault()))
        }
        val pagesPerMonthDataSet = PieDataSet(pagesPerMonthEntries, "")
        pagesPerMonthDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.COLORFUL_COLORS.toList()
        val pagesPerMonthData = PieData(pagesPerMonthDataSet)
        pagesPerMonthPieChart?.run {
            data = pagesPerMonthData

            //pagesPerMonthPieChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthArrayLabels.toTypedArray())
            //pagesPerMonthPieChart.setBackgroundColor(resources.getColor(R.color.white))
            description?.isEnabled = false
            legend.textColor = EditText(requireContext()).currentTextColor
            animateXY(3000, 3000)
            invalidate()
        }
    }

    private fun setPagesPerMonthBarChart(it: ChartsYearInfo) {
        val pagesPerMonthBarChart = requireView().findViewById<BarChart>(R.id.chartsYearPagesBarChart)
        val pagesPerMonthEntries = ArrayList<BarEntry>()
        val monthArrayLabels = ArrayList<String>()
        for (monthIndex in 0..11) {
            val monthString = DateFormatSymbols(Locale.getDefault()).shortMonths[monthIndex]
            pagesPerMonthEntries.add(BarEntry(monthIndex.toFloat(), it.pagesPerMonth[monthIndex]))
            monthArrayLabels.add(monthString.capitalize(Locale.getDefault()))
        }
        val pagesPerMonthDataSet = BarDataSet(pagesPerMonthEntries, "")
        pagesPerMonthDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val pagesPerMonthData = BarData(pagesPerMonthDataSet)
        pagesPerMonthBarChart?.run {
            val color = EditText(requireContext()).currentTextColor
            data = pagesPerMonthData
            xAxis?.valueFormatter = IndexAxisValueFormatter(monthArrayLabels.toTypedArray())
            setVisibleXRangeMaximum(6F)

            axisRight.isEnabled = false
            axisLeft.textColor = color
            xAxis.textColor = color
            xAxis.setDrawGridLines(false)
            data.setValueTextColor(color)
            data.setValueTextSize(TEXT_DATA_SIZE)
            //pagesPerMonthBarChart.setBackgroundColor(resources.getColor(R.color.white))
            setScaleEnabled(false)
            description?.isEnabled = false
            animateXY(1000, 1000)
            invalidate()
        }
    }


    private fun setBookPerMonthBarChart(it: ChartsYearInfo) {
        val barChart = requireView().findViewById<BarChart>(R.id.chartsYearBooksBarChart)
        val bookPerMonthBarChartEntries = ArrayList<BarEntry>()
        val monthArrayLabels = ArrayList<String>()
        for (monthIndex in 0..11) {
            val monthString = DateFormatSymbols(Locale.getDefault()).shortMonths[monthIndex]
            bookPerMonthBarChartEntries.add(BarEntry(monthIndex.toFloat(), it.bookPerMonth[monthIndex].toFloat()))
            monthArrayLabels.add(monthString.capitalize(Locale.getDefault()))
        }
        val bookPerMonthBarChartSet = BarDataSet(bookPerMonthBarChartEntries, "")
        bookPerMonthBarChartSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val bookPerMonthBarChartData = BarData(bookPerMonthBarChartSet)
        barChart?.run {
            data = bookPerMonthBarChartData
            xAxis?.valueFormatter = IndexAxisValueFormatter(monthArrayLabels.toTypedArray())
            setVisibleXRangeMaximum(6F)
            setVisibleXRangeMinimum(3F)
            //barChart.setBackgroundColor(resources.getColor(R.color.white))
            setScaleEnabled(false)
            description?.isEnabled = false

            val color = EditText(requireContext()).currentTextColor
            legend.textColor = color
            xAxis.textColor = color
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            axisLeft.textColor = color
            data.setValueTextColor(color)
            data.setValueTextSize(TEXT_DATA_SIZE)
            animateXY(1000,1000)
            invalidate()
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when(parent) {
            binding.chartsYearSpinner -> {
                val selectedYear = parent.getItemAtPosition(position) as Int
                chartsVM.changeSelectedYear(selectedYear)
            }

            binding.chartsYearChartTypeSpinner -> {
                binding.run {
                    chartsYearBooksInclude.root.visibility = View.GONE
                    chartsYearPagesInclude.root.visibility = View.GONE
                    chartsYearSupportInclude.root.visibility = View.GONE
                    chartsYearRatesInclude.root.visibility = View.GONE
                }
                when (position) {
                    0 -> binding.chartsYearBooksInclude.root.visibility = View.VISIBLE //Books
                    1 -> binding.chartsYearPagesInclude.root.visibility = View.VISIBLE //Pages
                    2 -> binding.chartsYearSupportInclude.root.visibility = View.VISIBLE //Supports
                    3 -> binding.chartsYearRatesInclude.root.visibility = View.VISIBLE //Rates
                }
            }
        }
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null) {
            val x = e.x.toInt()
            val rateChartSnackbar = Snackbar.make(requireView(), booksOfTheYearArray[x].title, Snackbar.LENGTH_SHORT)
            rateChartSnackbar.setAction(R.string.goto_string) {
                val action = ChartsFragmentDirections.actionChartsFragmentToEndedDetailFragment(booksOfTheYearArray[x].bookId)
                action.endedDetailEntryPoint = ENDED_DETAIL_ENTRY_CODE_FROM_CHARTS
                findNavController().navigate(action)
            }
            rateChartSnackbar.anchorView = requireActivity().findViewById(R.id.bottomNavigationView)
            rateChartSnackbar.show()
        }
    }

    override fun onNothingSelected() {
    }

}