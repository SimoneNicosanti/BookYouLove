package it.simone.bookyoulove.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class GameEntity(
        @PrimaryKey var gameId : Long,
        var quotesIdStringList : String,        //Parse per blank
        var correctAnswersIdList: String,
        var date : Long,
) : Serializable