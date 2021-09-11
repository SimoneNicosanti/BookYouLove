package it.simone.bookyoulove.adapter


import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.github.islamkhsh.CardSliderAdapter
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.utilsClass.MyPicasso


class ReadingAdapter(val readingBookSetAll: MutableList<ShowedBookInfo>,
                     private val onReadingItemMenuItemClickListener: OnReadingItemMenuItemClickListener) : CardSliderAdapter<ReadingAdapter.BookViewHolder>() {

    override fun bindVH(holder: BookViewHolder, position: Int) {
        MyPicasso().putImageIntoView(readingBookSetAll[position].coverName, holder.bookCoverImageView)

        holder.toolbar.title = readingBookSetAll[position].title
        holder.toolbar.subtitle = readingBookSetAll[position].author
    }

    override fun getItemCount(): Int {
        return readingBookSetAll.size
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

