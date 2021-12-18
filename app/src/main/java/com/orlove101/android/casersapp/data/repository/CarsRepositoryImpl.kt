package com.orlove101.android.casersapp.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.orlove101.android.casersapp.data.db.CarsDatabase
import com.orlove101.android.casersapp.data.page_sources.ParsedCarsPageSource
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.utils.PREFETCH_DISTANCE
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import com.orlove101.android.casersapp.utils.getRandomCarNumber
import com.orlove101.android.casersapp.utils.mapToDbCar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class CarsRepositoryImpl @Inject constructor(
    private val db: CarsDatabase,
) {
    private val database = Firebase.database("https://casersapp-default-rtdb.europe-west1.firebasedatabase.app/")
    private var currentParsedCarsDataSource: ParsedCarsPageSource? = null

    init {
        database.setPersistenceEnabled(true)
    }

    fun refreshPageSource() {
        currentParsedCarsDataSource?.invalidate()
    }

    suspend fun upsert(car: CarDomain): Long =
        db.getCarsDao().upsert(car.mapToDbCar())

    suspend fun deleteCar(car: CarDomain) =
        db.getCarsDao().deleteCar(car.mapToDbCar())

    fun getParsedCars() = Pager<Int, CarDomain>(
        PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE
        )
    ) {
        ParsedCarsPageSource(db = db).also {
            currentParsedCarsDataSource = it
        }
    }.flow

    fun searchForWaitingCars(
        fromNodeId: String?,
        carsPerPage: Int = QUERY_PAGE_SIZE,
        countCarsInApi: (Long) -> Unit,
        onDataChanged: (DataSnapshot) -> Unit
    ) {
        val carsRef = database.getReference("cars")
        val searchRef = fromNodeId?.let { carsRef.orderByKey().startAfter(it) } ?: carsRef
        val countWaitingRef = carsRef.orderByChild("waiting").equalTo(true)

        countWaitingRef
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    countCarsInApi(snapshot.childrenCount)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "waitingCars:onCancelled", error.toException())
                }
            })

        searchRef
            .limitToFirst(carsPerPage)
            .addListenerForSingleValueEvent(object : ValueEventListener {
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