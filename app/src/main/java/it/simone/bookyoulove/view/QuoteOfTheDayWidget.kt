package it.simone.bookyoulove.view

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote


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


class QuoteOfTheDayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return QuoteOfTheDayRemoteViewsFactory(this.applicationContext)
    }
}


class QuoteOfTheDayRemoteViewsFactory(
        private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var widgetQuote : Quote? = null

    private val myAppDatabase = AppDatabase.getDatabaseInstance(context)

    override fun onCreate() {
    }

    override fun onDataSetChanged() {

        widgetQuote = myAppDatabase.quoteDao().loadRandomQuote()
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

            if (widgetQuote != null) {
                setTextViewText(R.id.quoteOfTheDayQuoteText, widgetQuote!!.quoteText)
                setTextViewText(R.id.quoteOfTheDayWidgetTitle, widgetQuote!!.bookTitle)
                setTextViewText(R.id.quoteOfTheDayWidgetAuthor, widgetQuote!!.bookAuthor)
            }

            else {
                setTextViewText(R.id.quoteOfTheDayQuoteText, context.getString(R.string.placeholder_quote))
                setTextViewText(R.id.quoteOfTheDayWidgetAuthor, "")
                setTextViewText(R.id.quoteOfTheDayWidgetAuthor, "Umberto Eco")
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


