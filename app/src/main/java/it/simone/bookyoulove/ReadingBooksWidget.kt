package it.simone.bookyoulove

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.opengl.GLException
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import it.simone.bookyoulove.Constants.QUOTE_FROM_WIDGET_INTENT
import it.simone.bookyoulove.Constants.READING_BOOK_STATE
import it.simone.bookyoulove.Constants.TAG
import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.DAO.ShowedBookInfo
import it.simone.bookyoulove.view.MainActivity

/**
 * Implementation of App Widget functionality.
 */
class ReadingBooksWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val serviceIntent = Intent(context, ReadingBooksWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val remoteViews = RemoteViews(context.packageName, R.layout.reading_books_widget).apply {
                setRemoteAdapter(R.id.readingBooksWidgetListView, serviceIntent)
            }

            val getQuotePendingIntent = Intent(context, ReadingBooksWidget::class.java).run {
                action = QUOTE_FROM_WIDGET_INTENT
                PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            remoteViews.setPendingIntentTemplate(R.id.readingBooksWidgetListView, getQuotePendingIntent)

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.readingBooksWidgetListView)
            //Permette di aggiornare la visualizzazione delle informazioni
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "Ricevuto intent")
        Log.d(TAG, "${intent?.extras?.getBoolean("fromReadingWidget")}")

        if (context != null && intent != null && intent.action == QUOTE_FROM_WIDGET_INTENT) {
            val startAppIntent = Intent(context, MainActivity::class.java)
            startAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //Log.d(TAG, "${startAppIntent.extras?.getBoolean("fromReadingWidget")}")
            startAppIntent.run {
                putExtra("fromReadingWidget", true)
                putExtra("bookId", intent.extras?.getLong("bookId"))
                putExtra("bookTitle", intent.extras?.getString("bookTitle"))
                putExtra("bookAuthor", intent.extras?.getString("bookAuthor"))
            }
            context.startActivity(startAppIntent)
        }
    }
}

class ReadingBooksWidgetService() : RemoteViewsService() {
    override fun onGetViewFactory(p0: Intent?): RemoteViewsFactory {
        return ReadingBooksWidgetRemoteViewsFactory(this.applicationContext)
    }
}

class ReadingBooksWidgetRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var readingBooksArray = arrayOf<ShowedBookInfo>()

    private val myAppDatabase = AppDatabase.getDatabaseInstance(context)

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        readingBooksArray = myAppDatabase.bookDao().loadShowedBookInfoByState(READING_BOOK_STATE)
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return readingBooksArray.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteView = RemoteViews(context.packageName, R.layout.reading_books_widget_item)

        remoteView.apply {
            setTextViewText(R.id.readingBooksWidgetBookTitle, readingBooksArray[position].title)
            setTextViewText(R.id.readingBooksWidgetBookAuthor, readingBooksArray[position].author)

            val getQuoteIntent = Intent()
            getQuoteIntent.apply {
                extras.let {
                    putExtra("fromReadingWidget", true)
                    putExtra("bookId", readingBooksArray[position].bookId)
                    putExtra("bookTitle", readingBooksArray[position].title)
                    putExtra("bookAuthor", readingBooksArray[position].author)
                }
            }
            setOnClickFillInIntent(R.id.readingBooksWidgetItemRoot, getQuoteIntent)
        }

        return remoteView
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}
