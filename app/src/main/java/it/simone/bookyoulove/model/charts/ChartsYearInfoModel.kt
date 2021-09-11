package it.simone.bookyoulove.model.charts

import android.util.Log
import it.simone.bookyoulove.Constants.CHARACTER_RATE
import it.simone.bookyoulove.Constants.EMOTIONS_RATE
import it.simone.bookyoulove.Constants.PLOT_RATE
import it.simone.bookyoulove.Constants.STYLE_RATE
import it.simone.bookyoulove.Constants.TAG
import it.simone.bookyoulove.Constants.TOTAL_RATE
import it.simone.bookyoulove.utilsClass.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.util.*


class ChartsYearInfoModel(private val infoSet : Array<ChartsBookData>, yearSet : Array<Int>) {

    private var selectedYear : Int = 0

    private var yearMap = mutableMapOf<Int, ChartsYearInfo?>()

    init {
        for (year in yearSet) yearMap[year] = null
    }

    fun getValue(): ChartsYearInfo {
        return yearMap[selectedYear]!!
    }

    fun onChangeSelectedYear(newYear : Int) {
        selectedYear = newYear
    }


    suspend fun computeChartsYearInfo() {
        Log.d(TAG, "$selectedYear")
        if (yearMap[selectedYear] == null) {
            /*
                Se i precedenti calcoli sono nulli, significa che l'anno non è mai stato richiesto!! --> Calcolo per l'anno chiesto
                Se è non nullo ho già chiesto le info per quell'anno --> non ho bisogno di ricalcolare
             */
            withContext(Dispatchers.Default) {
                val bookPerMonthArray = computeBookPerMonthArray(selectedYear)
                val pagesPerMonthArray = computePagesPerMonthArray(selectedYear)
                val supportPerYear = computeSupportPerYear(selectedYear)
                val rateMap = computeRateMap(selectedYear)
                val booksOfTheYearArray = computeBooksOfTheYear(selectedYear)

                yearMap[selectedYear] = ChartsYearInfo(
                        bookPerMonthArray,
                        pagesPerMonthArray,
                        supportPerYear,
                        rateMap,
                        booksOfTheYearArray)
            }
        }
    }

    private fun computeBooksOfTheYear(selectedYear: Int): ArrayList<ChartsBookData> {
        val booksOfTheYearArray = arrayListOf<ChartsBookData>()
        for (info in infoSet) {
            val cal = Calendar.getInstance(Locale.getDefault())
            cal.timeInMillis = info.endDate
            val infoYear = cal.get(Calendar.YEAR)
            if (infoYear == selectedYear) booksOfTheYearArray.add(info)
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

        for (info in infoSet) {
            if (DateUtils().getYear(info.endDate) == selectedYear) {
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
        for (info in infoSet) {
            if (DateUtils().getYear(info.endDate) == selectedYear) {
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

        for (info in infoSet) {
            if (DateUtils().getYear(info.endDate) == selectedYear) bookPerMonthArray[DateUtils().getMonth(info.endDate)] += 1
        }

        return bookPerMonthArray
    }

    private fun computePagesPerMonthArray(selectedYear: Int): ArrayList<Float> {
        val pagesPerMonthArray = arrayListOf<Float>()
        for (monthIndex in 0 until 12) pagesPerMonthArray.add(0F)
        for (info in infoSet) {
            val startYear = DateUtils().getYear(info.startDate)
            val startMonth = DateUtils().getMonth(info.startDate)
            val startDay = DateUtils().getDay(info.startDate)

            val endYear = DateUtils().getYear(info.endDate)
            val endMonth = DateUtils().getMonth(info.endDate)
            val endDay = DateUtils().getDay(info.endDate)

            if (selectedYear in startYear..endYear) {
                val totalReadDays : Int = computeReadDays(info)
                val pagesPerDay : Float = if (totalReadDays > 0) info.pages / totalReadDays.toFloat() else 0F
                Log.d("Nicosanti", "readDays $totalReadDays - pagesPerDay $pagesPerDay")

                // Se il libro è stato letto TUTTO nell'anno selezionato
                if (selectedYear == startYear && selectedYear == endYear) {
                    if (startMonth == endMonth) {
                        //Se stato letto tutto nello stesso mese, sommo solo a quel mese tutte le pagine
                        pagesPerMonthArray[startMonth] += info.pages.toFloat()
                    }
                    else {
                        //Se è stato letto in mesi diversi ripartisco le pagine tra i mesi
                        for (monthIndex in startMonth..endMonth) {
                            when (monthIndex) {
                                startMonth -> pagesPerMonthArray[monthIndex] += (YearMonth.of(selectedYear, monthIndex + 1).lengthOfMonth() - startDay) * pagesPerDay
                                endMonth -> pagesPerMonthArray[monthIndex] += endDay * pagesPerDay
                                else -> pagesPerMonthArray[monthIndex] += YearMonth.of(selectedYear, monthIndex + 1).lengthOfMonth() * pagesPerDay
                            }
                        }
                    }
                }

                //Se il libro è stato letto in anni diversi e il selezionato è quello di inizio o di fine
                else if (selectedYear == startYear || selectedYear == endYear) {
                    val startIndex : Int
                    val endIndex : Int
                    if (selectedYear == startYear) {
                        startIndex = startMonth
                        endIndex = 11
                    }
                    else {
                        startIndex = 0
                        endIndex = endMonth
                    }
                    for (monthIndex in startIndex..endIndex) {
                        when (monthIndex) {
                            startMonth -> {
                                if (selectedYear == startYear) pagesPerMonthArray[monthIndex] += (YearMonth.of(selectedYear, monthIndex + 1).lengthOfMonth() - startDay) * pagesPerDay
                                else pagesPerMonthArray[monthIndex] += YearMonth.of(selectedYear, monthIndex + 1).lengthOfMonth() * pagesPerDay
                            }
                            endMonth -> {
                                if (selectedYear == endYear) pagesPerMonthArray[monthIndex] += endDay * pagesPerDay
                                else pagesPerMonthArray[monthIndex] += YearMonth.of(selectedYear, monthIndex + 1).lengthOfMonth() * pagesPerDay
                            }
                            else -> pagesPerMonthArray[monthIndex] += YearMonth.of(selectedYear, monthIndex + 1).lengthOfMonth() * pagesPerDay
                        }
                    }
                }

                //Se il libro è stato letto in anni diversi e quello selezionato è uno intermedio
                else {
                    for (monthIndex in 0..11) {
                        pagesPerMonthArray[monthIndex] += YearMonth.of(selectedYear, monthIndex + 1).lengthOfMonth() * pagesPerDay
                    }
                }
            }
        }
        return pagesPerMonthArray
    }

    private fun computeReadDays(info: ChartsBookData): Int {

        val diffInMillis = info.endDate - info.startDate
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}

data class ChartsYearInfo(
        var bookPerMonth: ArrayList<Int> ,
        var pagesPerMonth : ArrayList<Float>,
        var supportPerYear : ArrayList<Float>,
        var rateMap : Map<String, ArrayList<Float>>,
        var booksOfTheYear : ArrayList<ChartsBookData>
)
