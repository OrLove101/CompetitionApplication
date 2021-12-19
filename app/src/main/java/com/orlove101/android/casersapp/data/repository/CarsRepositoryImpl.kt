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
import com.orlove101.android.casersapp.domain.repository.CarsRepository
import com.orlove101.android.casersapp.utils.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarsRepositoryImpl @Inject constructor(
    private val roomDatabase: CarsDatabase,
): CarsRepository {
    private val firebaseDatabase = Firebase.database(FIREBASE_DB_REF)
    private var currentParsedCarsDataSource: ParsedCarsPageSource? = null

    init {
        firebaseDatabase.setPersistenceEnabled(true)
    }

    override fun refreshPageSource() {
        currentParsedCarsDataSource?.invalidate()
    }

    override suspend fun upsert(car: CarDomain): Long =
        roomDatabase.getCarsDao().upsert(car.mapToDbCar())

    override suspend fun deleteCar(car: CarDomain) =
        roomDatabase.getCarsDao().deleteCar(car.mapToDbCar())

    override fun getParsedCars() = Pager<Int, CarDomain>(
        PagingConfig(
            pageSize = QUERY_PAGE_SIZE,
            initialLoadSize = QUERY_PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE
        )
    ) {
        ParsedCarsPageSource(db = roomDatabase).also {
            currentParsedCarsDataSource = it
        }
    }.flow

    override fun deleteCarFromApi(car: CarDomain) {
        firebaseDatabase
            .getReference("cars")
            .orderByChild("uuid")
            .equalTo(car.uuid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.key?.let { valueKey ->
                                firebaseDatabase
                                    .getReference("cars")
                                    .child(valueKey)
                                    .removeValue()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "waitingCars:onCancelled", error.toException())
                }
            })
    }

    override fun searchForWaitingCars(
        fromNodeId: String?,
        carsPerPage: Int,
        countCarsInApi: (Long) -> Unit,
        onDataChanged: (DataSnapshot) -> Unit
    ) {
        val carsRef = firebaseDatabase.getReference("cars")
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