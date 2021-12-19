package com.orlove101.android.casersapp.utils.works

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orlove101.android.casersapp.utils.FIREBASE_DB_REF
import com.orlove101.android.casersapp.utils.SYNC_DATA_SIZE
import java.util.concurrent.CountDownLatch

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
): Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val database = Firebase.database(FIREBASE_DB_REF)
        val latch = CountDownLatch(2)

        try {
            // if app process not exist is needed
            database.setPersistenceEnabled(true)
        } catch(e: Exception) {
            // if app process exists
            Log.w(TAG, "doWork: exception ${e.stackTrace}")
        }
        searchForWaitingCars(
            SYNC_DATA_SIZE,
            latch,
            database
        )
        latch.await()
        return Result.success()
    }

    private fun searchForWaitingCars(
        carsPerPage: Int,
        latch: CountDownLatch,
        database: FirebaseDatabase
    ) {
        val carsRef = database.getReference("cars")
        val countWaitingRef = carsRef.orderByChild("waiting").equalTo(true)

        countWaitingRef
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "setCarsInApiQuantity: ${snapshot.childrenCount}")
                    latch.countDown()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "onCancelled: ${error.toException()}")
                    latch.countDown()
                }
            })

        carsRef
            .limitToFirst(carsPerPage)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "handleCarsApiResponse: ${snapshot.children.count()}")
                    latch.countDown()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "onCancelled: ${error.toException()}")
                    latch.countDown()
                }
            })
    }
}

private const val TAG = "SyncWorker"