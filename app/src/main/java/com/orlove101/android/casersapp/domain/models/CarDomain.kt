package com.orlove101.android.casersapp.domain.models

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class CarDomain(
    var uuid: String = UUID.randomUUID().toString(),
    val carNumber: String = "",
    val startWaitingAt: String = SimpleDateFormat("dd-M-yyyy hh:mm:ss", Locale.getDefault()).format(Date()),
    var stopWaitingAt: String = "",
    val cargoDescription: String = "",
    val plombQuantity: Int = 4,
    var waiting: Boolean = true,
): Serializable