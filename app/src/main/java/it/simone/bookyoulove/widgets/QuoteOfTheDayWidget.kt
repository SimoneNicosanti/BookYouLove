package it.simone.bookyoulove.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import it.simone.bookyoulove.Constants.QUOTE_OF_THE_DAY_FAVORITE_SWITCH_INTENT
import it.simone.bookyoulove.Constants.TAG
import it.simone.bookyoulove.utilsClass.MyNotificationClass
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Quote
import kotlinx.coroutines.*
import java.util.*


class QuoteOfTheDayWidget : AppWidgetProvider() {

    companion object {
        const val HEART = "\uD83D\uDC96"
        const val BROKEN_HEART = "\uD83D\uDC94"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        Log.i(TAG, "Updating")

        for (appWidgetId in appWidgetIds) {

            //Creazione intent per riempire l'adapter
            val intent = Intent(context, QuoteOfTheDayWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            //Impostazione dell'adapter della remoteView Principale
            val remoteViews = RemoteViews(context.packageName, R.layout.quote_of_the_day_widget).apply {
                setRemoteAdapter(R.id.quoteOfTheDayWidgetListView, intent)
            }

            //Pending intent per tutti gli items della list view
            val switchFavoritePendingIntent = Intent(context, QuoteOfTheDayWidget::class.java).run {
                action = QUOTE_OF_THE_DAY_FAVORITE_SWITCH_INTENT
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            remoteViews.setPendingIntentTemplate(R.id.quoteOfTheDayWidgetListView, switchFavoritePendingIntent)

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
        Log.d(TAG, "Receive")

        if (context != null && intent != null && intent.action == QUOTE_OF_THE_DAY_FAVORITE_SWITCH_INTENT) {

            //Modifico il valore di preferenza nel DB
            CoroutineScope(Dispatchers.Default).launch {
                val appDatabase = AppDatabase.getDatabaseInstance(context)
                val quoteId = intent.extras?.getLong("quoteId", 0)!!
                val clickedQuote = appDatabase.quoteDao().loadSingleQuote(quoteId)
                clickedQuote.favourite = !clickedQuote.favourite
                appDatabase.quoteDao().updateQuote(clickedQuote)

                withContext(Dispatchers.Main) {
                    val toastString = if (clickedQuote.favourite) context.resources.getString(R.string.favorite_string) + HEART
                                        else context.resources.getString(R.string.not_favorite_string) + BROKEN_HEART
                    Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show()

                    MyNotificationClass(context).run{
                        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                        quoteOfTheDayChangeQuoteStateNotificationCancel(widgetId)       //Cancello Precedente
                        quoteOfTheDayChangeQuoteStateNotification(toastString, clickedQuote, widgetId)
                    }
                }
            }
        }
        super.onReceive(context, intent)
    }

}


class QuoteOfTheDayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return QuoteOfTheDayRemoteViewsFactory(this.applicationContext, intent)
    }
}


class QuoteOfTheDayRemoteViewsFactory(
        private val context: Context,
        intent : Intent
) : RemoteViewsService.RemoteViewsFactory {


    private var widgetQuote : Quote? = null
    private val myAppDatabase = AppDatabase.getDatabaseInstance(context)
    private val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        //widgetQuote = myAppDatabase.quoteDao().loadRandomQuote()

        val idsList = myAppDatabase.quoteDao().loadQuoteKeys()

        widgetQuote = if (idsList.isNotEmpty()) {
            val time = Calendar.getInstance(Locale.getDefault()).timeInMillis
            val index = time % idsList.size
            val quoteId = idsList[index.toInt()]

            myAppDatabase.quoteDao().loadSingleQuote(quoteId)
        }
        else null
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return 1
    }

    override fun getViewAt(position: Int): RemoteViews {
        Log.d(TAG, "Get View At")
        val quoteOfTheDayItemRemoteView =  RemoteViews(context.packageName, R.layout.quote_of_the_day_widget_quote_text).apply {

            if (widgetQuote != null) {
                setTextViewText(R.id.quoteOfTheDayQuoteText, widgetQuote!!.quoteText)
                setTextViewText(R.id.quoteOfTheDayWidgetTitle, widgetQuote!!.bookTitle)
                setTextViewText(R.id.quoteOfTheDayWidgetAuthor, widgetQuote!!.bookAuthor)
                setImageViewResource(R.id.quoteOfheDayWidgetFavoriteIndicator,
                if (widgetQuote!!.favourite) R.drawable.ic_round_modify_quote_favorite_on
                else R.drawable.ic_round_modify_quote_favorite_off)
            }

            else {
                setTextViewText(R.id.quoteOfTheDayQuoteText, context.getString(R.string.placeholder_quote))
                setTextViewText(R.id.quoteOfTheDayWidgetTitle, "")
                setTextViewText(R.id.quoteOfTheDayWidgetAuthor, "Umberto Eco")
            }
        }

        widgetQuote?.let {
            //Imposto l'intent per il click dell'item: Questo intent va a riempire il pending intent impostato per tutti gli items nella onUpdate
            val switchFavoriteIntent = Intent()
            switchFavoriteIntent.putExtra("quoteId", widgetQuote!!.quoteId)
            switchFavoriteIntent.putExtra("bookId", widgetQuote!!.bookId)
            switchFavoriteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            //quoteOfTheDayItemRemoteView.setOnClickFillInIntent(R.id.quoteOfTheDayItemRoot, switchFavoriteIntent)

            MyNotificationClass(context).run {
                cancelNewQuoteNotification(appWidgetId)
                quoteOfTheDayNewQuoteNotification(widgetQuote!!.quoteText, appWidgetId)
            }


        }

        return quoteOfTheDayItemRemoteView
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


