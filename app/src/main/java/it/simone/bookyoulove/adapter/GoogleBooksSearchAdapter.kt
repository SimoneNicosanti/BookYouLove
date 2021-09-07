package it.simone.bookyoulove.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.model.GoogleBooksApi


class GoogleBooksSearchAdapter(val networkBookList : MutableList<GoogleBooksApi.NetworkBook>,
                               private val networkBookListItemClickListener : OnNetworkBookListItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.google_books_search_item_layout, parent, false)
        return NetworkBookListViewHolder(view, networkBookListItemClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as NetworkBookListViewHolder
        if (networkBookList[position].thumbnail != "") Picasso.get()
            .load(networkBookList[position].thumbnail)
            .placeholder(R.drawable.book_cover_place_holder)
            .error(R.drawable.cover_not_found)
            .into(holder.coverImageView)

        else Picasso.get().load(R.drawable.book_cover_place_holder).into(holder.coverImageView)

        holder.titleTextView.text = networkBookList[position].title
        holder.authorTextView.text = networkBookList[position].authors
    }

    override fun getItemCount(): Int {
        return networkBookList.size
    }

    class NetworkBookListViewHolder(
        view: View,
        private val networkBookListItemClickListener: OnNetworkBookListItemClickListener
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val coverImageView: ImageView = view.findViewById(R.id.googleBooksSearchItemCoverImageView)
        val titleTextView : TextView = view.findViewById(R.id.googleBooksSearchItemTitleTextView)
        val authorTextView : TextView = view.findViewById(R.id.googleBooksSearchItemAuthorTextView)
        //val cardView : CardView = view.findViewById(R.id.googleBooksSearchItemCard)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            networkBookListItemClickListener.onNetworkBookListItemClick(absoluteAdapterPosition)
        }

    }


    interface OnNetworkBookListItemClickListener {
        fun onNetworkBookListItemClick(position : Int)
    }
}