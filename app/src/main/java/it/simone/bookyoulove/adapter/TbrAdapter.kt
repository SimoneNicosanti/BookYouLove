package it.simone.bookyoulove.adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowedBookInfo

class TbrAdapter(private val tbrArray : Array<ShowedBookInfo>, private val onTbrItemClickedListener: OnTbrItemClickedListener) : RecyclerView.Adapter<TbrAdapter.TbrViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TbrAdapter.TbrViewHolder {
        val tbrView = LayoutInflater.from(parent.context).inflate(R.layout.tbr_item, parent, false)
        return TbrViewHolder(tbrView, onTbrItemClickedListener)
    }

    override fun onBindViewHolder(holder: TbrViewHolder, position: Int) {
        holder.titleTextView.text = tbrArray[position].title
        holder.authorTextView.text = tbrArray[position].author

        if (tbrArray[position].coverName != "") Picasso.get().load(tbrArray[position].coverName)
                                                        .placeholder(R.drawable.book_cover_place_holder).error(R.drawable.cover_not_found)
                                                        .into(holder.coverImageView)

        else Picasso.get().load(R.drawable.book_cover_place_holder).into(holder.coverImageView)
    }

    override fun getItemCount(): Int {
        return tbrArray.size
    }

    class TbrViewHolder(tbrView : View, private val onTbrItemClickedListener : OnTbrItemClickedListener) : RecyclerView.ViewHolder(tbrView), androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {

        val titleTextView : TextView = tbrView.findViewById(R.id.tbrItemTitle)
        val authorTextView : TextView = tbrView.findViewById(R.id.tbrItemAuthor)
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
}