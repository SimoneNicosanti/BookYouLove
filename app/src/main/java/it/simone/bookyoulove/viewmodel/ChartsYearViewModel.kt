package it.simone.bookyoulove.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.model.ChartsBookData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import kotlin.collections.ArrayList



class ChartsYearViewModel : ViewModel() {

    private var currentChartsDataArray = arrayOf<ChartsBookData>()

    val currentYearList = MutableLiveData<Array<Int>>()
    val currentChartsYearInfo = MutableLiveData<ChartsYearInfo>()

    fun setChartsDataArray(newChartDataArray: Array<ChartsBookData>) {
        //TODO("Fai il sortinge dell'array prima di passarlo")
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
        var bookPerMonthArray = arrayListOf<Int>()
        var pagesPerMonthArray = arrayListOf<Float>()
        var supportPerYear = arrayListOf<Float>()
        var totalRateArray = arrayListOf<Float>()
        viewModelScope.launch { Dispatchers.Default

            bookPerMonthArray = computeBookPerMonthArray(selectedYear)
            pagesPerMonthArray = computePagesPerMonthArray(selectedYear)
            supportPerYear = computeSupportPerYear(selectedYear)
            totalRateArray = computeTotalRateArray(selectedYear)
            currentChartsYearInfo.value = ChartsYearInfo(bookPerMonthArray, pagesPerMonthArray, supportPerYear, totalRateArray)
        }
    }

    private fun computeTotalRateArray(selectedYear: Int): ArrayList<Float> {
        val totalRateArray = arrayListOf<Float>()
        for (info in currentChartsDataArray) {
            if (info.endDate.endYear == selectedYear) {
                totalRateArray.add(info.rate.totalRate)
            }
        }
        return totalRateArray
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
        if (totalSupport != 0F) {
            return arrayListOf(paperSupport / totalSupport * 100, ebookSupport / totalSupport * 100, audiobookSupport / totalSupport * 100)
        }
        else {
            return arrayListOf(0F, 0F, 0F)
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

            if (info.startDate.startYear <= selectedYear && selectedYear <= info.endDate.endYear) {
                val totalReadDays : Int = computeReadDays(info)
                val pagesPerDay : Float = if (totalReadDays > 0) info.pages / totalReadDays.toFloat() else 0F

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
                            info.startDate.startMonth -> pagesPerMonthArray[monthIndex - 1] += (YearMonth.of(selectedYear, monthIndex).lengthOfMonth() - info.startDate.startDay) * pagesPerDay
                            info.endDate.endMonth -> pagesPerMonthArray[monthIndex - 1] += info.endDate.endDay * pagesPerDay
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
        //TODO("Rivedi per ritornare il numero esatto e non approssimato")
        /*return ((info.endDate.endYear - info.startDate.startYear) * DAYS_IN_YEAR +
                (info.endDate.endMonth - info.startDate.startMonth) * DAYS_IN_MONTH +
                (info.endDate.endDay - info.startDate.startDay + 1))*/

        val totalDays = Period.between(LocalDate.of(info.startDate.startYear, info.startDate.startMonth, info.startDate.startDay), LocalDate.of(info.endDate.endYear, info.endDate.endMonth, info.endDate.endDay)).days
        Log.i("Nicosanti", "$totalDays")
        return totalDays
        // +1 mi considera il giorno di inizio
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
    var totalRateArray : ArrayList<Float>
)