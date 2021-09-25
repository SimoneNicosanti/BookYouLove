package it.simone.bookyoulove.utilsClass

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import it.simone.bookyoulove.R
import java.lang.Exception

class MyPicasso {

    companion object {
        const val COVER_PLACEHOLDER = R.drawable.book_cover_place_holder
        const val COVER_ERROR = R.drawable.cover_not_found
    }

    fun putImageIntoView(imageLink : String, imageView: ImageView) {
        if (imageLink != "") Picasso.get().load(imageLink)
                .placeholder(COVER_PLACEHOLDER).error(COVER_ERROR)
                .into(imageView)
        else Picasso.get().load(COVER_PLACEHOLDER).into(imageView)
    }
}