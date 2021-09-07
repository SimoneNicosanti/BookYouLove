package it.simone.bookyoulove.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.Constants.SEARCH_BY_TITLE
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.filters.BookListFilter


//https://www.youtube.com/watch?v=69C1ljfDvl0
class EndedAdapter(private val bookSetAll: MutableList<ShowedBookInfo>,
                   private val onRecyclerViewItemSelectedListener: OnRecyclerViewItemSelectedListener,
                   private val linearLayoutIndicator: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() , Filterable {

    var filterType : Int = SEARCH_BY_TITLE

    val bookSet : MutableList<ShowedBookInfo> = ArrayList(bookSetAll).toMutableList()


    class GridViewHolder(view: View, private val onRecyclerViewItemSelectedListener: OnRecyclerViewItemSelectedListener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val coverImageView : ImageView = view.findViewById(R.id.readCoverImageView)
        val titleTextView : TextView = view.findViewById(R.id.readTitleTextView)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onRecyclerViewItemSelectedListener.onRecyclerViewItemSelected(absoluteAdapterPosition)
        }

    }


    class LinearViewHolder(view: View, private val onRecyclerViewItemSelectedListener: OnRecyclerViewItemSelectedListener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val coverImageView : ImageView = view.findViewById(R.id.linearEndedCoverImageView)
        val titleTextView : TextView = view.findViewById(R.id.linearEndedTitle)
        val authorTextView : TextView = view.findViewById(R.id.linearEndedAuthor)
        val ratingBar : RatingBar = view.findViewById(R.id.linearEndedRate)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onRecyclerViewItemSelectedListener.onRecyclerViewItemSelected(absoluteAdapterPosition)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view : View

        return if (linearLayoutIndicator) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.ended_list_row_item, parent, false)
            LinearViewHolder(view, onRecyclerViewItemSelectedListener)
        }
        else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.ended_list_grid_item, parent, false)
            GridViewHolder(view, onRecyclerViewItemSelectedListener)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder , position: Int) {

        if (holder is GridViewHolder) {
            if (bookSet[position].coverName != "") Picasso.get().load(bookSet[position].coverName)
                .placeholder(R.drawable.book_cover_place_holder).error(R.drawable.cover_not_found)
                .into(holder.coverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(holder.coverImageView)

            holder.titleTextView.text = bookSet[position].title
        }

        else if (holder is LinearViewHolder){
            if (bookSet[position].coverName != "") Picasso.get().load(bookSet[position].coverName)
                .placeholder(R.drawable.book_cover_place_holder).error(R.drawable.cover_not_found)
                .into(holder.coverImageView)
            else Picasso.get().load(R.drawable.book_cover_place_holder).into(holder.coverImageView)

            holder.titleTextView.text = bookSet[position].title
            holder.ratingBar.rating = bookSet[position].totalRate!!
            holder.authorTextView.text = bookSet[position].author

        }

    }


    override fun getItemCount(): Int {
        return bookSet.size
    }

    interface OnRecyclerViewItemSelectedListener {
        fun onRecyclerViewItemSelected(position: Int)
    }

    override fun getFilter(): Filter {
        return BookListFilter(bookSetAll, bookSet, filterType, this)
    }
}