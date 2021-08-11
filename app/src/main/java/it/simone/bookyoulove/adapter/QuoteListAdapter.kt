package it.simone.bookyoulove.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo

class QuoteListAdapter(private val quotesArray: Array<ShowQuoteInfo>, private val onQuoteListHolderClick: OnQuoteListHolderClick) : RecyclerView.Adapter<QuoteListAdapter.QuotesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotesViewHolder {

        val view =  LayoutInflater.from(parent.context).inflate(R.layout.quote_list_item, parent, false)
        return QuotesViewHolder(view, onQuoteListHolderClick)
    }

    override fun onBindViewHolder(holder: QuotesViewHolder, position: Int) {
        holder.quoteText.text = quotesArray[position].quoteText
        holder.quoteBookTitle.text = quotesArray[position].bookTitle
        holder.quoteBookAuthor.text = quotesArray[position].bookAuthor

        if (quotesArray[position].favourite) holder.quoteBookFavoriteButton.setImageResource(R.drawable.ic_round_modify_quote_favorite_on)
        else holder.quoteBookFavoriteButton.setImageResource(R.drawable.ic_round_modify_quote_favorite_off)
    }

    override fun getItemCount(): Int {
        return quotesArray.size
    }

    class QuotesViewHolder(quoteView: View, private val onQuoteListHolderClick: OnQuoteListHolderClick) : RecyclerView.ViewHolder(quoteView) , View.OnClickListener{

        var quoteText: TextView = quoteView.findViewById(R.id.quoteListItemQuoteText)
        var quoteBookTitle : TextView  = quoteView.findViewById(R.id.quoteListItemBookTitle)
        var quoteBookAuthor : TextView = quoteView.findViewById(R.id.quoteListItemBookAuthor)
        var quoteBookFavoriteButton : ImageButton = quoteView.findViewById(R.id.quoteListItemFavoriteImageButton)

        init {
            quoteView.setOnClickListener(this)
            quoteBookFavoriteButton.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onQuoteListHolderClick.onQuoteListHolderClickedListener(v, absoluteAdapterPosition)
        }

    }

    interface OnQuoteListHolderClick {
        fun onQuoteListHolderClickedListener(view : View, position : Int)
    }
}