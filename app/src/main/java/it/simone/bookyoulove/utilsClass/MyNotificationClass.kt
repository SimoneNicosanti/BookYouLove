package it.simone.bookyoulove.utilsClass

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import it.simone.bookyoulove.R
import it.simone.bookyoulove.database.entity.Quote


class MyNotificationClass(private val context: Context) {

    init {
        createNotificationChannel()
    }

    companion object {
        const val QUOTE_OF_THE_DAY_CHANNEL_ID = "it.simone.bookyoulove.QUOTE_OF_THE_DAY_NOTIFICATION_CHANNEL_NAME"

        const val QUOTE_OF_THE_DAY_CHANGE_STATE_ADD = 1452
    }

    fun quoteOfTheDayChangeQuoteStateNotification(toastString: String, clickedQuote: Quote, widgetId : Int) {

        val notification = NotificationCompat.Builder(context, QUOTE_OF_THE_DAY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_quotes_bottom_bar)
                .setContentTitle(toastString)
                .setContentText(clickedQuote.quoteText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(clickedQuote.quoteText))
                .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(widgetId + QUOTE_OF_THE_DAY_CHANGE_STATE_ADD, notification)
    }

    fun quoteOfTheDayChangeQuoteStateNotificationCancel(widgetId: Int) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(widgetId + QUOTE_OF_THE_DAY_CHANGE_STATE_ADD)
    }

    fun quoteOfTheDayNewQuoteNotification(quoteText: String, appWidgetId: Int) {

        val notification = NotificationCompat.Builder(context, QUOTE_OF_THE_DAY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_quotes_bottom_bar)
                .setContentTitle(context.getString(R.string.new_quote_for_you_string))
                .setContentText(quoteText)
                .build()

        /*
            Utilizzo numero fisso perché la getViewAt è chiamata più volte mi invia sempre la stessa
            notifica: utilizzando un valore fisso il sistema la vede come singola notifica e non
            la rimanda
         */
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(appWidgetId, notification)
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(QUOTE_OF_THE_DAY_CHANNEL_ID,
                                            context.getString(R.string.change_state_notification_string),
                                            NotificationManager.IMPORTANCE_DEFAULT)

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    fun cancelAllNotification() {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
    }

    fun cancelNewQuoteNotification(appWidgetId: Int) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(appWidgetId)
    }
}