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
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000, pendingIntent)

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

    val myAppDatabase = AppDatabase.getDatabaseInstance(context)
    CoroutineScope(Dispatchers.IO).launch {
        val newQuote = myAppDatabase.quoteDao().loadRandomQuote()
        withContext(Dispatchers.Main) {
            if (newQuote != null) {
                views.setTextViewText(R.id.quoteOfTheDayWidgetQuoteText, newQuote.quoteText)
                views.setTextViewText(R.id.quoteOfTheDayWidgetTitle, newQuote.bookTitle)
                views.setTextViewText(R.id.quoteOfTheDayWidgetAuthor, newQuote.bookAuthor)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

class ScrollableTextView(context : Context) : androidx.appcompat.widget.AppCompatTextView(context) {

    init {
        this.movementMethod = ScrollingMovementMethod()
    }
}




