package it.simone.bookyoulove.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LabelEntity (
        @PrimaryKey(autoGenerate = true) var labelId : Long,
        var name : String,
        var color : Int,
        var typeCode : Int
)