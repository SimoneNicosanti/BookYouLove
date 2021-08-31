package it.simone.bookyoulove.utilsClass

import android.content.Context
import androidx.preference.PreferenceManager
import it.simone.bookyoulove.database.entity.EndDate
import it.simone.bookyoulove.database.entity.StartDate
import java.text.DateFormatSymbols
import java.util.*

class DateFormatClass(private val context : Context) {

    fun computeStartDateString(startDate : StartDate?) : String{
        val preferenceFormat = PreferenceManager.getDefaultSharedPreferences(context).getString("date_format", "dd-mm-yyyy")

        return if (startDate != null) {
            val monthString = DateFormatSymbols(Locale.getDefault()).months[startDate.startMonth - 1]
            when (preferenceFormat) {
                "mm-dd-yyyy" -> "${monthString.capitalize(Locale.ROOT)} ${startDate.startDay} ${startDate.startYear}"
                else -> "${startDate.startDay} ${monthString.capitalize(Locale.ROOT)} ${startDate.startYear}"
            }
        }
        else ""


    }

    fun computeEndDateString(endDate: EndDate?) : String {
        val preferenceFormat = PreferenceManager.getDefaultSharedPreferences(context).getString("date_format", "dd-mm-yyyy")
        return if (endDate != null) {
            val monthString = DateFormatSymbols(Locale.getDefault()).months[endDate.endMonth - 1]
            when (preferenceFormat) {
                "mm-dd-yyyy" -> "${monthString.capitalize(Locale.ROOT)} ${endDate.endDay} ${endDate.endYear}"
                else -> "${endDate.endDay} ${monthString.capitalize(Locale.ROOT)} ${endDate.endYear}"

            }
        }
        else ""
    }
}