package it.simone.bookyoulove.viewmodel.charts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.model.charts.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*



class ChartsViewModel(application : Application) : AndroidViewModel(application) {

    private val myAppDatabase = AppDatabase.getDatabaseInstance(application.applicationContext)
    private val chartsModel = ChartsModel(myAppDatabase)

    val currentYearList = MutableLiveData(arrayOf<Int>())
    val isAccessing = MutableLiveData(false)
    val totalChartData = MutableLiveData<TotalChartData>()
    val yearChartData = MutableLiveData<ChartsYearInfo>()
    val readyArray = MutableLiveData(false)

    private var loadedOnce = false
    private var loadedYearOnce = false

    private var currentChartsDataArray = arrayOf<ChartsBookData>()

    private lateinit var chartsTotalModel : ChartsTotalModel
    private lateinit var chartsYearModel : ChartsYearInfoModel


    fun getAllChartsData() {
        if (!loadedOnce) {
            isAccessing.value = true
            viewModelScope.launch {
                currentChartsDataArray = chartsModel.getAllChartsDataFromDatabase()
                loadedOnce = true
                isAccessing.value = false
                readyArray.value = true
            }
        }
    }

    fun changeLoadedStatus() {
        loadedOnce = false
        loadedYearOnce = false
    }


    fun getChartsTotalInfo() {
        isAccessing.value = true
        viewModelScope.launch {
            chartsTotalModel = ChartsTotalModel(currentChartsDataArray)
            chartsTotalModel.totalChartComputeInfo()
            totalChartData.value = chartsTotalModel.getValue()
            isAccessing.value = false
        }
    }

    fun getYearList() {
        if (!loadedYearOnce) {
            val yearList = arrayListOf<Int>()
            isAccessing.value = true
            viewModelScope.launch {
                Dispatchers.Default
                for (info in currentChartsDataArray) {
                    val cal = Calendar.getInstance(Locale.getDefault())
                    cal.timeInMillis = info.endDate
                    val year = cal.get(Calendar.YEAR)
                    if (year !in yearList) yearList.add(year)
                }
                if (yearList.isEmpty()) {
                    currentYearList.value = arrayOf(0)
                }
                else {
                    yearList.sort()
                    yearList.reverse()
                    currentYearList.value = yearList.toTypedArray()
                    chartsYearModel = ChartsYearInfoModel(currentChartsDataArray, yearList.toTypedArray())
                }
                isAccessing.value = false
                loadedYearOnce = true
            }
        }
    }


    fun changeSelectedYear(selectedYear: Int) {
        if (selectedYear != 0) {
            chartsYearModel.onChangeSelectedYear(selectedYear)
            computeChartsYearInfo()
        }
    }


    private fun computeChartsYearInfo() {
        isAccessing.value = true
        viewModelScope.launch {
            chartsYearModel.computeChartsYearInfo()
            yearChartData.value = chartsYearModel.getValue()
            isAccessing.value = false
        }
    }

}

