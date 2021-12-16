package com.orlove101.android.casersapp.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orlove101.android.casersapp.data.api.CarsApi
import com.orlove101.android.casersapp.data.db.CarsDatabase
import com.orlove101.android.casersapp.data.models.Car
import com.orlove101.android.casersapp.utils.PREFETCH_DISTANCE
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarsRepositoryImpl @Inject constructor(
    private val db: CarsDatabase,
    private val api: CarsApi
) {
    private val database = Firebase.database("https://casersapp-default-rtdb.europe-west1.firebasedatabase.app/")

    suspend fun upsert(car: Car): Long =
        db.getCarsDao().upsert(car)

    suspend fun deleteCar(car: Car) =
        db.getCarsDao().deleteCar(car)

    fun searchForWaitingCars(
        queryByCarNumber: String,
        fromNodeId: String?,
        carsPerPage: Int = QUERY_PAGE_SIZE,
        onDataChanged: (DataSnapshot) -> Unit,
    ) {
        var searchCarsRef = database
            .getReference("cars")
            .orderByKey()
            .limitToFirst(carsPerPage)


        fromNodeId?.let {
            Log.d(TAG, "searchForWaitingCars: start after $it")
            searchCarsRef = searchCarsRef.startAfter(it)
        }
        searchCarsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onDataChanged(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "waitingCars:onCancelled", error.toException())
            }
        })
    }
}

private const val TAG = "CarsRepositoryImpl"