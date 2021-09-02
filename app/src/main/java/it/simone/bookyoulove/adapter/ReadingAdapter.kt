package it.simone.bookyoulove.adapter


import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.github.islamkhsh.CardSliderAdapter
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowedBookInfo


class ReadingAdapter(private val showedReadingBookInfoArray : Array<ShowedBookInfo>, private val onReadingItemMenuItemClickListener: OnReadingItemMenuItemClickListener) : CardSliderAdapter<ReadingAdapter.BookViewHolder>() {

    override fun bindVH(holder: BookViewHolder, position: Int) {

        if (showedReadingBookInfoArray[position].coverName != "") Picasso.get().load(showedReadingBookInfoArray[position].coverName)
                .placeholder(R.drawable.book_cover_place_holder).error(R.drawable.cover_not_found).into(holder.bookCoverImageView)
        else Picasso.get().load(R.drawable.book_cover_place_holder).into(holder.bookCoverImageView)
        holder.toolbar.title = showedReadingBookInfoArray[position].title
        holder.toolbar.subtitle = showedReadingBookInfoArray[position].author
    }

    override fun getItemCount(): Int {
        return showedReadingBookInfoArray.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reading_item_layout, parent, false)
        return BookViewHolder(view, onReadingItemMenuItemClickListener)
    }


    class BookViewHolder(view : View, private val onReadingItemMenuItemClickListener: OnReadingItemMenuItemClickListener) : RecyclerView.ViewHolder(view), Toolbar.OnMenuItemClickListener {
        val toolbar : Toolbar = view.findViewById(R.id.readingCardToolbar)
        val bookCoverImageView : ImageView = view.findViewById(R.id.readingCardCoverImageView)

        init {
            toolbar.setOnMenuItemClickListener(this)
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            return onReadingItemMenuItemClickListener.onReadingItemMenuItemClickListener(absoluteAdapterPosition, item)
        }
    }

    interface OnReadingItemMenuItemClickListener {
        fun onReadingItemMenuItemClickListener(position : Int, item : MenuItem?) : Boolean
    }
}

