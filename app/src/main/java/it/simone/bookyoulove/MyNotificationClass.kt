package it.simone.bookyoulove

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import it.simone.bookyoulove.database.entity.Quote
import java.util.*


class MyNotificationClass(private val context: Context) {

    init {
        createNotificationChannel()
    }

    companion object {
        const val QUOTE_OF_THE_DAY_CHANNEL_ID = "it.simone.bookyoulove.QUOTE_OF_THE_DAY_NOTIFICATION_CHANNEL_NAME"
    }

    fun quoteOfTheDayChangeQuoteStateNotification(toastString: String, clickedQuote: Quote) {

        val notification = NotificationCompat.Builder(context, QUOTE_OF_THE_DAY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_quotes_bottom_bar)
                .setContentTitle(toastString)
                .setContentText(clickedQuote.quoteText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(clickedQuote.quoteText))
                .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(Random().nextInt(101), notification)
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
}