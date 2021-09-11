package it.simone.bookyoulove.utilsClass

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class MyVolleyQueue {

    companion object {
        private var volleyQueue: RequestQueue? = null

        fun getVolleyQueue(context: Context): RequestQueue {
            if (volleyQueue == null) {
                volleyQueue = Volley.newRequestQueue(context)
            }
            return volleyQueue as RequestQueue
        }
    }
}