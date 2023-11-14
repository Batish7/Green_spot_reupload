package com.batish.android.greenspot

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Plant(

    @PrimaryKey val id : UUID,
    val title: String,
    val date: Date,
    val place: String,
    val isFound: Boolean,
    val reporter: String = "",
    val photoFileName: String? = null
)


