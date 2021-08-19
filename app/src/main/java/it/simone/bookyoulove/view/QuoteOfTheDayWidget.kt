package it.simone.bookyoulove.view

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.widget.*
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.AppDatabase


class QuoteOfTheDayWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        Log.i("Nicosanti", "Updating")

        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, QuoteOfTheDayWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val remoteViews = RemoteViews(context.packageName, R.layout.quote_of_the_day_widget).apply {
                setRemoteAdapter(R.id.quoteOfTheDayWidgetListView, intent)
            }

            //Chiamo prima la notify che invoca la onDataSetChanged (Ricarico nuova quote) e la imposta come quella visualizzata
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.quoteOfTheDayWidgetListView)
            //Permette di aggiornare la visualizzazione delle informazioni
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }
}

/*
internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.quote_of_the_day_widget)

    /*
    CoroutineScope(Dispatchers.IO).launch {
        val quoteCursor = context.contentResolver.query(QuotesProvider().URI_RANDOM, null, null, null, null)

        withContext(Dispatchers.Main) {
            /*
                Il Cursor è come un array di oggetti Quotes. Poiché in questo caso ho un UNICA quote, mi muovo sulla prima con moveToFirst
                e poi prendoo i vari campi dell'oggetto Quote cui punta il cursore tramite le getString
             */
            if (quoteCursor?.moveToFirst() == true) {
                views.setTextViewText(R.id.quoteOfTheDayWidgetQuoteText, quoteCursor.getString(0))

                views.setTextViewText(R.id.quoteOfTheDayWidgetTitle, quoteCursor.getString(quoteCursor.getColumnIndex("bookTitle")))

                views.setTextViewText(R.id.quoteOfTheDayWidgetAuthor, quoteCursor.getString(quoteCursor.getColumnIndex("bookAuthor")))
            }
            quoteCursor?.close()

     */

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }


}*/

class QuoteOfTheDayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return QuoteOfTheDayRemoteViewsFactory(this.applicationContext, intent)
    }
}


class QuoteOfTheDayRemoteViewsFactory(
        private val context : Context,
        intent : Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var quoteCursor : Cursor? = null

    private val myAppDatabase = AppDatabase.getDatabaseInstance(context)

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        //quoteCursor = context.contentResolver.query(QuotesProvider().URI_RANDOM, null, null, null, null)
        quoteCursor = myAppDatabase.quoteDao().loadRandomQuote()
        /*
            Devo per forza utilizzare l'istanza del DB in maniera diretta senza passare per il Provider.
            Infatti mi serve accedere al DB in maniera Sincrona, per fare in modo che quando il quoteCursor
            è caricato poi venga visualizzata la quote caricata. Dovendo accedere al DB non posso farlo con
            il Main Thread e dovei quindi lanciare una Coroutine, che però non mi garantisce sincronia a meno di
            fare join e quindi rendere la funzione suspend.
            Invece la onDataSetChanged è fatta proprio per eseguire operazioni lunghe (come accesso a DB) in maniera
            sincrona
         */
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return 1
    }

    override fun getViewAt(position: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.quote_of_the_day_widget_quote_text).apply {

            if (quoteCursor != null) {
                quoteCursor!!.moveToFirst()
                setTextViewText(R.id.quoteOfTheDayQuoteText, quoteCursor!!.getString(0))
                setTextViewText(R.id.quoteOfTheDayWidgetTitle, quoteCursor!!.getString(quoteCursor!!.getColumnIndex("bookTitle")))
                setTextViewText(R.id.quoteOfTheDayWidgetAuthor, quoteCursor!!.getString(quoteCursor!!.getColumnIndex("bookAuthor")))
            }

        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

}


