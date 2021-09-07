package it.simone.bookyoulove.filters

import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import it.simone.bookyoulove.Constants.SEARCH_BY_AUTHOR
import it.simone.bookyoulove.Constants.SEARCH_BY_RATE
import it.simone.bookyoulove.Constants.SEARCH_BY_TITLE
import it.simone.bookyoulove.Constants.SEARCH_BY_YEAR
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.utilsClass.DateUtils
import java.util.*


class BookListFilter(private val bookSetAll: MutableList<ShowedBookInfo>, private val bookSet: MutableList<ShowedBookInfo>,
                     private val filterType: Int, private val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) : Filter() {

    //Runs on background thread
    override fun performFiltering(filterField: CharSequence?): FilterResults {
        val filteredArray : MutableList<ShowedBookInfo>
        if (filterField == null || filterField == "") {
            filteredArray = bookSetAll
        } else {
            filteredArray = when (filterType) {
                SEARCH_BY_TITLE -> bookSetAll.filter { it.title.toLowerCase(Locale.getDefault()).contains(filterField.toString().toLowerCase(Locale.ROOT)) } as MutableList<ShowedBookInfo>

                SEARCH_BY_AUTHOR -> bookSetAll.filter { it.author.toLowerCase(Locale.getDefault()).contains(filterField.toString().toLowerCase(Locale.ROOT)) } as MutableList<ShowedBookInfo>
                SEARCH_BY_RATE -> {
                    val searchRate = filterField.toString().toFloat()
                    bookSetAll.filter { it.totalRate == searchRate || it.totalRate == searchRate + 0.5F } as MutableList<ShowedBookInfo>
                }
                SEARCH_BY_YEAR -> {
                    val searchYear = filterField.toString().toInt()

                    bookSetAll.filter { findYearByMillis(it.startDate) == searchYear || findYearByMillis(it.endDate) == searchYear } as MutableList<ShowedBookInfo>
                }
                else -> { //Titolo e autore
                    bookSetAll.filter { it.title.toLowerCase(Locale.getDefault()).contains(filterField.toString().toLowerCase(Locale.getDefault())) ||
                            it.author.toLowerCase(Locale.getDefault()).contains(filterField.toString().toLowerCase(Locale.getDefault()))} as MutableList<ShowedBookInfo>
                }
            }
        }

        val filterResult = FilterResults()
        filterResult.values = filteredArray
        return filterResult
    }

    private fun findYearByMillis(dateMillis: Long?): Any {
        return if (dateMillis != null) {
            DateUtils().getYear(dateMillis)
        }
        else 1970
    }

    //Runs on UI Thread
    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        bookSet.clear()
        bookSet.addAll(results.values as Collection<ShowedBookInfo>)
        adapter.notifyDataSetChanged()
    }
}