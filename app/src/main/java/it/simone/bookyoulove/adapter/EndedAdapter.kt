package it.simone.bookyoulove.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book

//https://www.youtube.com/watch?v=69C1ljfDvl0
/*
    Rivedi Video youtube per capire bene quello che succede!!
 */

class EndedAdapter(private val bookSet: Array<Book>, private val onRecyclerViewItemSelectedListener : OnRecyclerViewItemSelectedListener): RecyclerView.Adapter<EndedAdapter.ViewHolder>() {

    class ViewHolder(view: View, private val onRecyclerViewItemSelectedListener: OnRecyclerViewItemSelectedListener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val coverImageView : ImageView
        val titleTextView : TextView

        init {
            coverImageView = view.findViewById(R.id.readCoverImageView)
            titleTextView = view.findViewById(R.id.readTitleTextView)

            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onRecyclerViewItemSelectedListener.onRecyclerViewItemSelected(absoluteAdapterPosition)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.read_list_grid_item, parent, false)

        return ViewHolder(view, onRecyclerViewItemSelectedListener)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (bookSet[position].coverName != "") Picasso.get().load(bookSet[position].coverName).into(holder.coverImageView)
        else Picasso.get().load(R.mipmap.book_cover_placeholder).into(holder.coverImageView)

        holder.titleTextView.text = bookSet[position].title
    }


    override fun getItemCount(): Int {
        return bookSet.size
    }

    interface OnRecyclerViewItemSelectedListener {
        fun onRecyclerViewItemSelected(position: Int)
    }
}