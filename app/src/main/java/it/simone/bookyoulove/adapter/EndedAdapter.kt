package it.simone.bookyoulove.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowedBookInfo

//https://www.youtube.com/watch?v=69C1ljfDvl0
/*
    Rivedi Video youtube per capire bene quello che succede!!
 */

class EndedAdapter(private val bookSet: Array<ShowedBookInfo>,
                   private val onRecyclerViewItemSelectedListener : OnRecyclerViewItemSelectedListener,
                   private val linearLayoutIndicator : Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        //val ratingBar : RatingBar = view.findViewById(R.id.linearEndedRate)

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
            //holder.ratingBar.rating = bookSet[position].totalRate!!
        }

    }


    override fun getItemCount(): Int {
        return bookSet.size
    }

    interface OnRecyclerViewItemSelectedListener {
        fun onRecyclerViewItemSelected(position: Int)
    }
}