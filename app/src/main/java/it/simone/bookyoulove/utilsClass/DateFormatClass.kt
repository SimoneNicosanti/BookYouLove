package it.simone.bookyoulove.utilsClass

import android.content.Context
import androidx.preference.PreferenceManager
import java.text.DateFormatSymbols
import java.util.*

class DateFormatClass(private val context : Context) {

    fun computeDateString(dateMillis : Long?) : String {
        val preferenceFormat = PreferenceManager.getDefaultSharedPreferences(context).getString("date_format", "mm-dd-yyyy")
        return if (dateMillis != null) {
            val cal = Calendar.getInstance(Locale.getDefault())
            cal.timeInMillis = dateMillis
            val monthString = DateFormatSymbols(Locale.getDefault()).months[cal.get(Calendar.MONTH)]
            when (preferenceFormat) {
                "mm-dd-yyyy" -> "${monthString.capitalize(Locale.ROOT)} ${cal.get(Calendar.DAY_OF_MONTH)} ${cal.get(Calendar.YEAR)}"
                else -> "${cal.get(Calendar.DAY_OF_MONTH)} ${monthString.capitalize(Locale.ROOT)} ${cal.get(Calendar.YEAR)}"
            }
        }
        else ""
    }
}

class DateUtils {

    fun getYear(dateMillis: Long): Int {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = dateMillis
        return cal.get(Calendar.YEAR)
    }

    fun getMonth(dateMillis: Long) : Int {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = dateMillis
        return cal.get(Calendar.MONTH)
    }

    fun getDay(dateMillis: Long) : Int {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = dateMillis
        return cal.get(Calendar.DAY_OF_MONTH)
    }
}