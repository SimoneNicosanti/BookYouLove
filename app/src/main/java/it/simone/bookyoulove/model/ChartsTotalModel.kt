package it.simone.bookyoulove.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChartsTotalModel(private val infoSet : Array<ChartsBookData>) {

    private lateinit var totalChartData : TotalChartData

    fun getValue(): TotalChartData {
        return totalChartData
    }

    suspend fun totalChartComputeInfo() {
        var totalPages = 0L
        var totalPaperSupport = 0
        var totalEbookSupport = 0
        var totalAudiobookSupport = 0
        var totalFinalRate = 0F
        withContext(Dispatchers.Default) {
            for (info in infoSet) {
                totalPages += info.pages.toLong()
                if (info.support.paperSupport) totalPaperSupport += 1
                if (info.support.ebookSupport) totalEbookSupport += 1
                if (info.support.audiobookSupport) totalAudiobookSupport += 1
                totalFinalRate += info.rate.totalRate
            }
        }
        val totalSupport = totalPaperSupport + totalEbookSupport + totalAudiobookSupport

        totalChartData = TotalChartData(
                totalBooks = infoSet.size,
                totalPages,
                (if (totalSupport != 0) (totalPaperSupport / totalSupport.toFloat()) * 100 else 0).toFloat(),
                (if (totalSupport != 0) (totalEbookSupport / totalSupport.toFloat()) * 100 else 0).toFloat(),
                (if (totalSupport != 0) (totalAudiobookSupport / totalSupport.toFloat()) * 100 else 0).toFloat(),
                (if (infoSet.isNotEmpty()) totalFinalRate / infoSet.size else 0F)
        )

    }
}

data class TotalChartData(
        var totalBooks : Int,
        var totalPages : Long,
        var totalPaperSupport : Float,
        var totalEbookSupport : Float,
        var totalAudiobookSupport : Float,
        var totalFinalRateAverage : Float
)