package it.simone.bookyoulove.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.simone.bookyoulove.filters.QuoteListFilter
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.DAO.ShowQuoteInfo
import java.util.*

class QuoteListAdapter(private val quoteSetAll: MutableList<ShowQuoteInfo>,
                       private val onQuoteListHolderClick: OnQuoteListHolderClick) : RecyclerView.Adapter<QuoteListAdapter.QuotesViewHolder>(), Filterable {

    val quoteSet : MutableList<ShowQuoteInfo> = ArrayList(quoteSetAll).toMutableList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotesViewHolder {

        val view =  LayoutInflater.from(parent.context).inflate(R.layout.quote_list_item, parent, false)
        return QuotesViewHolder(view, onQuoteListHolderClick)
    }

    override fun onBindViewHolder(holder: QuotesViewHolder, position: Int) {
        holder.quoteText.text = quoteSet[position].quoteText
        holder.quoteBookTitle.text = quoteSet[position].bookTitle
        holder.quoteBookAuthor.text = quoteSet[position].bookAuthor

        if (quoteSet[position].favourite) holder.quoteBookFavoriteButton.setImageResource(R.drawable.ic_round_modify_quote_favorite_on)
        else holder.quoteBookFavoriteButton.setImageResource(R.drawable.ic_round_modify_quote_favorite_off)
    }

    override fun getItemCount(): Int {
        return quoteSet.size
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

    override fun getFilter(): Filter {
        return QuoteListFilter(quoteSetAll, quoteSet, this)
    }
}