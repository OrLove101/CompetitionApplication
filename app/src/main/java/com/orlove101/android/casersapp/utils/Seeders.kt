package com.orlove101.android.casersapp.utils

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.domain.models.CarDomain
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

fun seedCarsApiData(quantity: Int, carsRef: DatabaseReference) {
    for (counter in 1..quantity) {
        val childWaitingCarsRef = carsRef.push()

        childWaitingCarsRef.setValue(
            CarDomain(
                carNumber = getRandomCarNumber(),
                cargoDescription = "Car $counter with fancy cargo",
                waiting = true,
                plombQuantity = Random.nextInt(1,12)
            )
        )
        .addOnSuccessListener {
            Log.d(TAG, "setWaitingCarsApiChangeListener: succeess")
        }
        .addOnFailureListener {
            Log.d(TAG, "setWaitingCarsApiChangeListener: ${it.stackTraceToString()}")
        }
    }
}

suspend fun seedCarsDbData(quantity: Int, repositoryImpl: CarsRepositoryImpl) {
    for (counter in 1..quantity) {
        repositoryImpl.upsert(
            CarDomain(
                carNumber = getRandomCarNumber(),
                stopWaitingAt = SimpleDateFormat("dd-M-yyyy hh:mm:ss", Locale.getDefault()).format(Date()),
                cargoDescription = "Car $counter with fancy cargo",
                plombQuantity = Random.nextInt(1,12),
                waiting = false
            )
        )
    }
}

private const val TAG = "Seeders"
