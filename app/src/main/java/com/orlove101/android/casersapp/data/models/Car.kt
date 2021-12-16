package com.orlove101.android.casersapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "cars"
)
data class Car(
    @PrimaryKey
    var uuid: String = UUID.randomUUID().toString(),
    val carNumber: String? = "",
    val startWaitingAt: String? = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    var stopWaitingAt: String? = "",
    val cargoDescription: String? = "",
    val plombQuantity: Int? = 4,
    var waiting: Boolean? = true,
)