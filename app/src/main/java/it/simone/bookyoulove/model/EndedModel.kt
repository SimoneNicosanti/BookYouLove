package it.simone.bookyoulove.model

import it.simone.bookyoulove.database.AppDatabase
import it.simone.bookyoulove.database.entity.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EndedModel(private val myAppDatabase: AppDatabase) {

    suspend fun loadReadList(requestedState : Int) : Array<Book> {
        val loadedList : Array<Book>
        withContext(Dispatchers.IO) {
            loadedList = myAppDatabase.bookDao().loadBookArrayByState(requestedState)
        }
        return loadedList
    }

    suspend fun sortByDate() {
        TODO("Ordina per anno ; per ogni anno diverso, ordina la singola lista per mese ; crea liste per ogni mese ; ordina la lista del singolo mese in base al giorno ; unisci le liste di mese e poi di anno")
    }
}