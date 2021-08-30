package it.simone.bookyoulove.viewmodel.charts

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.model.ChartsBookData
import it.simone.bookyoulove.view.AUDIOBOOK_SUPPORT
import it.simone.bookyoulove.view.EBOOK_SUPPORT
import it.simone.bookyoulove.view.PAPER_SUPPORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChartsTotalViewModel : ViewModel() {

    private var currentDataArray = arrayOf<ChartsBookData>()

    val totalChartData = MutableLiveData<TotalChartData>()

    fun setDataArray(newArray: Array<ChartsBookData>) {
        currentDataArray = newArray
        computeChartsTotalInfo()
    }

    private fun computeChartsTotalInfo() {
        viewModelScope.launch {
            val totalInfo = totalChartComputeInfo()
            totalChartData.value = TotalChartData(
                currentDataArray.size,
                totalInfo.getLong("totalPages"),
                totalInfo.getFloat(PAPER_SUPPORT),
                totalInfo.getFloat(EBOOK_SUPPORT),
                totalInfo.getFloat(AUDIOBOOK_SUPPORT),
                totalInfo.getFloat("averageFinalRate")
            )
        }
    }

    private suspend fun totalChartComputeInfo(): Bundle {
        var totalPages = 0L
        var totalPaperSupport = 0
        var totalEbookSupport = 0
        var totalAudiobookSupport = 0
        var totalFinalRate = 0F
        withContext(Dispatchers.Default) {
            for (info in currentDataArray) {
                totalPages += info.pages.toLong()
                if (info.support.paperSupport) totalPaperSupport += 1
                if (info.support.ebookSupport) totalEbookSupport += 1
                if (info.support.audiobookSupport) totalAudiobookSupport += 1
                totalFinalRate += info.rate.totalRate
            }
        }
        val totalSupport = totalPaperSupport + totalEbookSupport + totalAudiobookSupport
        return bundleOf("totalPages" to totalPages,
            PAPER_SUPPORT to if (totalSupport != 0) (totalPaperSupport / totalSupport.toFloat()) * 100 else 0,
            EBOOK_SUPPORT to if (totalSupport != 0) (totalEbookSupport / totalSupport.toFloat()) * 100 else 0,
            AUDIOBOOK_SUPPORT to if (totalSupport != 0) (totalAudiobookSupport / totalSupport.toFloat()) * 100 else 0,
            "averageFinalRate" to (if (currentDataArray.isNotEmpty()) totalFinalRate / currentDataArray.size else 0F))
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