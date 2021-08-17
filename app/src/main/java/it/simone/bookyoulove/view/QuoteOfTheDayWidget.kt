package it.simone.bookyoulove.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.database.DataSetObserver
import android.os.SystemClock
import android.text.method.MovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.databinding.QuoteOfTheDayWidgetBinding
import it.simone.bookyoulove.providers.QuotesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class QuoteOfTheDayWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        Log.i("Nicosanti", "Updating")
        val alarm = Intent(context, QuoteOfTheDayWidget::class.java)
        alarm.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        alarm.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 10000, pendingIntent)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
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

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.quote_of_the_day_widget)

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

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }


}

class ScrollableTextView(context : Context) : androidx.appcompat.widget.AppCompatTextView(context) {

    init {
        this.movementMethod = ScrollingMovementMethod()
    }
}




