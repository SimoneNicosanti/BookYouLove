package it.simone.bookyoulove.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipDrawable
import com.squareup.picasso.Picasso
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Book
import it.simone.bookyoulove.view.SettingsFragmentDirections
import it.simone.bookyoulove.viewmodel.EndedViewModel
import androidx.fragment.app.activityViewModels

class EndedAdapter(private val bookSet: Array<Book>): RecyclerView.Adapter<EndedAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val coverImageView : ImageView
        val titleTextView : TextView

        init {
            coverImageView = view.findViewById(R.id.readCoverImageView)
            titleTextView = view.findViewById(R.id.readTitleTextView)
        }

        override fun onClick(v: View?) {
            TODO("Not yet implemented")
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.read_list_grid_item, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (bookSet[position].coverName != "") Picasso.get().load(bookSet[position].coverName).into(holder.coverImageView)
        else Picasso.get().load(R.mipmap.book_cover_placeholder).into(holder.coverImageView)

        holder.titleTextView.text = bookSet[position].title

        holder.itemView.setOnClickListener {
            /*
            val navController = findNavController(it)
            val action = SettingsFragmentDirections.actionGlobalSettingsFragment()
            navController.navigate(action)*/
            Log.i("Nicosanti", "Cliccato ${bookSet[position].title}")

        }
    }


    override fun getItemCount(): Int {
        return bookSet.size
    }

    interface OnRecyclerViewItemSelectedListener {
        fun onRecyclerViewItemSelected(selectedBook : Book)
    }
}