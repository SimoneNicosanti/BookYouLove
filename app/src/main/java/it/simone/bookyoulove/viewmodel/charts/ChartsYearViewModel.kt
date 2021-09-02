package it.simone.bookyoulove.viewmodel.charts

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.model.ChartsBookData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.util.*
import kotlin.collections.ArrayList

const val TOTAL_RATE = "total"
const val STYLE_RATE = "style"
const val EMOTIONS_RATE = "emotions"
const val PLOT_RATE = "plot"
const val CHARACTER_RATE = "character"

class ChartsYearViewModel : ViewModel() {

    private var currentChartsDataArray = arrayOf<ChartsBookData>()

    val currentYearList = MutableLiveData<Array<Int>>()
    val currentChartsYearInfo = MutableLiveData<ChartsYearInfo>()

    fun setChartsDataArray(newChartDataArray: Array<ChartsBookData>) {
        currentChartsDataArray = newChartDataArray
        computeYearList()
    }

    private fun computeYearList() {
        val yearList = arrayListOf<Int>()
        viewModelScope.launch { Dispatchers.Default
            for (info in currentChartsDataArray) {
                if (info.endDate.endYear !in yearList) yearList.add(info.endDate.endYear)
            }
            if (yearList.isEmpty()) {
                currentYearList.value = arrayOf(0)
            }
            else {
                yearList.sort()
                yearList.reverse()
                currentYearList.value = yearList.toTypedArray()
            }
        }
    }

    private fun computeChartsYearInfo(selectedYear : Int) {

        viewModelScope.launch { Dispatchers.Default

            val bookPerMonthArray = computeBookPerMonthArray(selectedYear)
            val pagesPerMonthArray = computePagesPerMonthArray(selectedYear)
            val supportPerYear = computeSupportPerYear(selectedYear)
            val rateMap = computeRateMap(selectedYear)
            val booksOfTheYearArray = computeBooksOfTheYear(selectedYear)
            withContext(Dispatchers.Main) {
                currentChartsYearInfo.value = ChartsYearInfo(bookPerMonthArray, pagesPerMonthArray, supportPerYear, rateMap, booksOfTheYearArray)
            }
        }
    }

    private fun computeBooksOfTheYear(selectedYear: Int): ArrayList<ChartsBookData> {
        val booksOfTheYearArray = arrayListOf<ChartsBookData>()
        for (info in currentChartsDataArray) {
            if (info.endDate.endYear == selectedYear) booksOfTheYearArray.add(info)
        }

        return booksOfTheYearArray
    }

    private fun computeRateMap(selectedYear: Int): Map<String, ArrayList<Float>> {
        val rateMap = mapOf(
            TOTAL_RATE to arrayListOf<Float>(),
            STYLE_RATE to arrayListOf(),
            EMOTIONS_RATE to arrayListOf(),
            PLOT_RATE to arrayListOf(),
            CHARACTER_RATE to arrayListOf())

        for (info in currentChartsDataArray) {
            if (info.endDate.endYear == selectedYear) {
                rateMap[TOTAL_RATE]!!.add(info.rate.totalRate)
                rateMap[STYLE_RATE]!!.add(info.rate.styleRate)
                rateMap[EMOTIONS_RATE]!!.add(info.rate.emotionRate)
                rateMap[PLOT_RATE]!!.add(info.rate.plotRate)
                rateMap[CHARACTER_RATE]!!.add(info.rate.characterRate)
            }
        }
        return rateMap
    }

    private fun computeSupportPerYear(selectedYear: Int): ArrayList<Float> {
        var paperSupport = 0
        var ebookSupport = 0
        var audiobookSupport = 0
        for (info in currentChartsDataArray) {
            if (info.endDate.endYear == selectedYear) {
                if (info.support.paperSupport) paperSupport += 1
                if (info.support.ebookSupport) ebookSupport += 1
                if (info.support.audiobookSupport) audiobookSupport += 1
            }
        }
        val totalSupport = (paperSupport + ebookSupport + audiobookSupport).toFloat()
        return if (totalSupport != 0F) {
            arrayListOf(paperSupport / totalSupport * 100, ebookSupport / totalSupport * 100, audiobookSupport / totalSupport * 100)
        }
        else {
            arrayListOf(0F, 0F, 0F)
        }
    }

    private fun computeBookPerMonthArray(selectedYear : Int): ArrayList<Int> {
        val bookPerMonthArray = arrayListOf<Int>()
        for (monthIndex in 0..11) { bookPerMonthArray.add(0) }

        for (info in currentChartsDataArray) {
            if (info.endDate.endYear == selectedYear) bookPerMonthArray[info.endDate.endMonth - 1] += 1
        }

        return bookPerMonthArray
    }

    private fun computePagesPerMonthArray(selectedYear: Int): ArrayList<Float> {
        val pagesPerMonthArray = arrayListOf<Float>()
        for (monthIndex in 0..11) pagesPerMonthArray.add(0F)
        for (info in currentChartsDataArray) {
            Log.d("Nicosanti", "${info.title} ${info.startDate.startYear} ${info.endDate.endYear}")
            if (info.startDate.startYear <= selectedYear && selectedYear <= info.endDate.endYear) {
                val totalReadDays : Int = computeReadDays(info)
                val pagesPerDay : Float = if (totalReadDays > 0) info.pages / totalReadDays.toFloat() else 0F
                Log.d("Nicosanti", "readDays $totalReadDays - pagesPerDay $pagesPerDay")

                // Se il libro è stato letto TUTTO nell'anno selezionato
                if (selectedYear == info.startDate.startYear && selectedYear == info.endDate.endYear) {
                    if (info.startDate.startMonth == info.endDate.endMonth) {
                        //Se stato letto tutto nello stesso mese, sommo solo a quel mese tutte le pagine
                        pagesPerMonthArray[info.startDate.startMonth - 1] += info.pages.toFloat()
                    }
                    else {
                        //Se è stato letto in mesi diversi ripartisco le pagine tra i mesi
                        for (monthIndex in info.startDate.startMonth..info.endDate.endMonth) {
                            when (monthIndex) {
                                info.startDate.startMonth -> pagesPerMonthArray[monthIndex - 1] += (YearMonth.of(selectedYear, monthIndex).lengthOfMonth() - info.startDate.startDay) * pagesPerDay
                                info.endDate.endMonth -> pagesPerMonthArray[monthIndex - 1] += info.endDate.endDay * pagesPerDay
                                else -> pagesPerMonthArray[monthIndex - 1] += YearMonth.of(selectedYear, monthIndex).lengthOfMonth() * pagesPerDay
                            }
                        }
                    }
                }

                //Se il libro è stato letto in anni diversi e il selezionato è quello di inizio o di fine
                else if (selectedYear == info.startDate.startYear || selectedYear == info.endDate.endYear) {
                    val startIndex : Int
                    val endIndex : Int
                    if (selectedYear == info.startDate.startYear) {
                        startIndex = info.startDate.startMonth
                        endIndex = 12
                    }
                    else {
                        startIndex = 1
                        endIndex = info.endDate.endMonth
                    }
                    for (monthIndex in startIndex..endIndex) {
                        when (monthIndex) {
                            info.startDate.startMonth -> {
                                if (selectedYear == info.startDate.startYear) pagesPerMonthArray[monthIndex - 1] += (YearMonth.of(selectedYear, monthIndex).lengthOfMonth() - info.startDate.startDay) * pagesPerDay
                                else pagesPerMonthArray[monthIndex - 1] += YearMonth.of(selectedYear, monthIndex).lengthOfMonth() * pagesPerDay
                            }
                            info.endDate.endMonth -> {
                                if (selectedYear == info.endDate.endYear) pagesPerMonthArray[monthIndex - 1] += info.endDate.endDay * pagesPerDay
                                else pagesPerMonthArray[monthIndex - 1] += YearMonth.of(selectedYear, monthIndex).lengthOfMonth() * pagesPerDay
                            }
                            else -> pagesPerMonthArray[monthIndex - 1] += YearMonth.of(selectedYear, monthIndex).lengthOfMonth() * pagesPerDay
                        }
                    }
                }

                //Se il libro è stato letto in anni diversi e quello selezionato è uno intermedio
                else {
                    for (monthIndex in 1..12) {
                        pagesPerMonthArray[monthIndex - 1] += YearMonth.of(selectedYear, monthIndex).lengthOfMonth() * pagesPerDay
                    }
                }
            }
        }
        return pagesPerMonthArray
    }


    private fun computeReadDays(info: ChartsBookData): Int {

        val startDateCal = Calendar.getInstance()
        startDateCal.set(info.startDate.startYear, info.startDate.startMonth, info.startDate.startDay)

        val endDateCal = Calendar.getInstance()
        endDateCal.set(info.endDate.endYear, info.endDate.endMonth, info.endDate.endDay)

        val diffInMillis = endDateCal.timeInMillis - startDateCal.timeInMillis
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    fun changeSelectedYear(selectedYear: Int) {
        //viewModelScope.cancel()
        computeChartsYearInfo(selectedYear)
    }
}


data class ChartsYearInfo(
    var bookPerMonth: ArrayList<Int> ,
    var pagesPerMonth : ArrayList<Float>,
    var supportPerYear : ArrayList<Float>,
    var rateMap : Map<String, ArrayList<Float>>,
    var booksOfTheYear : ArrayList<ChartsBookData>
)