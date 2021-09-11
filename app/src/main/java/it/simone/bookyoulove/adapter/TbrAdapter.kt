package it.simone.bookyoulove.adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.simone.bookyoulove.Constants.SEARCH_BY_TITLE_OR_AUTHOR
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.filters.BookListFilter
import it.simone.bookyoulove.utilsClass.MyPicasso
import java.util.*


class TbrAdapter(private val tbrSetAll : MutableList<ShowedBookInfo>,
                 private val onTbrItemClickedListener: OnTbrItemClickedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    val tbrSet = ArrayList(tbrSetAll).toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val tbrView = LayoutInflater.from(parent.context).inflate(R.layout.tbr_item, parent, false)
        return TbrViewHolder(tbrView, onTbrItemClickedListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as TbrViewHolder
        holder.titleTextView.text = tbrSet[position].title
        holder.authorTextView.text = tbrSet[position].author
        holder.pagesTextView.text = tbrSet[position].pages.toString()

        MyPicasso().putImageIntoView(tbrSet[position].coverName, holder.coverImageView)
    }

    override fun getItemCount(): Int {
        return tbrSet.size
    }

    class TbrViewHolder(tbrView : View, private val onTbrItemClickedListener : OnTbrItemClickedListener) : RecyclerView.ViewHolder(tbrView), androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {

        val titleTextView : TextView = tbrView.findViewById(R.id.tbrItemTitle)
        val authorTextView : TextView = tbrView.findViewById(R.id.tbrItemAuthor)
        val pagesTextView : TextView = tbrView.findViewById(R.id.tbrItemPages)
        val coverImageView : ImageView = tbrView.findViewById(R.id.tbrItemCoverImageView)
        private val toolbar : androidx.appcompat.widget.Toolbar = tbrView.findViewById(R.id.tbrItemToolbar)

        init {
            toolbar.setOnMenuItemClickListener(this)
        }


        override fun onMenuItemClick(item: MenuItem?): Boolean {
            return onTbrItemClickedListener.onTbrListItemToolbarMenuClicked(absoluteAdapterPosition, item)
        }
    }

    interface OnTbrItemClickedListener {
        fun onTbrListItemToolbarMenuClicked(position : Int, item : MenuItem?): Boolean
    }

    override fun getFilter(): Filter {
        return BookListFilter(tbrSetAll, tbrSet, SEARCH_BY_TITLE_OR_AUTHOR, this)
    }
}