package com.orlove101.android.casersapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "cars"
)
data class CarDb(
    @PrimaryKey
    var uuid: String,
    val carNumber: String,
    val startWaitingAt: String,
    var stopWaitingAt: String,
    val cargoDescription: String,
    val plombQuantity: Int,
    var waiting: Boolean,
)