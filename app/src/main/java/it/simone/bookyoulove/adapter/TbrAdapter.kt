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

class TbrAdapter(private val tbrArray : Array<ShowedBookInfo>) : RecyclerView.Adapter<TbrAdapter.TbrViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TbrAdapter.TbrViewHolder {
        val tbrView = LayoutInflater.from(parent.context).inflate(R.layout.tbr_item, parent, false)
        return TbrViewHolder(tbrView)
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

    class TbrViewHolder(tbrView : View) : RecyclerView.ViewHolder(tbrView) {

        val titleTextView : TextView = tbrView.findViewById(R.id.tbrItemTitle)
        val authorTextView : TextView = tbrView.findViewById(R.id.tbrItemAuthor)
        val coverImageView : ImageView = tbrView.findViewById(R.id.tbrItemCoverImageView)
    }
}