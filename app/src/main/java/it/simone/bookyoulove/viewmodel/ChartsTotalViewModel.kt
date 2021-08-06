package it.simone.bookyoulove.viewmodel

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
import java.util.function.ToDoubleBiFunction

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
                totalInfo.getInt("totalPages"),
                totalInfo.getInt(PAPER_SUPPORT),
                totalInfo.getInt(EBOOK_SUPPORT),
                totalInfo.getInt(AUDIOBOOK_SUPPORT),
                totalInfo.getFloat("averageFinalRate")
            )
        }
    }

    private suspend fun totalChartComputeInfo(): Bundle {
        var totalPages = 0
        var totalPaperSupport = 0
        var totalEbookSupport = 0
        var totalAudiobookSupport = 0
        var totalFinalRate = 0F
        withContext(Dispatchers.Default) {
            for (info in currentDataArray) {
                totalPages += info.pages
                if (info.support.paperSupport) totalPaperSupport += 1
                if (info.support.ebookSupport) totalEbookSupport += 1
                if (info.support.audiobookSupport) totalAudiobookSupport += 1
                totalFinalRate += info.rate.totalRate
            }
        }
        return bundleOf("totalPages" to totalPages,
            PAPER_SUPPORT to totalPaperSupport,
            EBOOK_SUPPORT to totalEbookSupport,
            AUDIOBOOK_SUPPORT to totalAudiobookSupport,
            "averageFinalRate" to (if (currentDataArray.isNotEmpty()) totalFinalRate / currentDataArray.size else 0F))
    }

}

data class TotalChartData(
    var totalBooks : Int,
    var totalPages : Int,
    var totalPaperSupport : Int,
    var totalEbookSupport : Int,
    var totalAudiobookSupport : Int,
    var totalFinalRateAverage : Float,
    //Non considero gli altri perché non è detto che siano campi compilati : un saggio non ha trama o personaggi
)