package com.orlove101.android.casersapp.data.api

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.orlove101.android.casersapp.data.models.Car
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import kotlinx.coroutines.sync.Semaphore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarsApi @Inject constructor() {
    private val database = Firebase.database("https://casersapp-default-rtdb.europe-west1.firebasedatabase.app/")

//    fun setWaitingCarsApiChangeListener() {
//        val carsRef = database.getReference("cars")
//
//        // TODO seed value in firebase with valid data
////        for (i in 0..24) {
////            val childWaitingCarsRef = carsRef.push()
////
////            childWaitingCarsRef.setValue(
////                Car(
////                    carNumber = java.util.UUID.randomUUID().toString(),
////                    cargoDescription = "Car with fancy cargo",
////                    waiting = true,
////                    plombQuantity = 3
////                )
////            )
////            .addOnSuccessListener {
////                Log.d(TAG, "setWaitingCarsApiChangeListener: succeess")
////            }
////            .addOnFailureListener {
////                Log.d(TAG, "setWaitingCarsApiChangeListener: ${it.stackTraceToString()}")
////            }
////        }
//
//        carsRef.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "onChildAdded: " + snapshot.getValue<Car>())
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "onChildChanged:" + snapshot.key)
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                Log.d(TAG, "onChildRemoved:" + snapshot.key)
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "onChildMoved:" + snapshot.key)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.w(TAG, "waitingCars:onCancelled", error.toException())
//            }
//        })
//    }


    suspend fun searchForWaitingCars(
        fromNodeId: String?,
        carsPerPage: Int = QUERY_PAGE_SIZE,
        queryByCarNumber: String,
        onGetData: (DataSnapshot) -> Unit
    ) {
        // THREAD BLOCK
        //val semaphore = Semaphore(0)
        // ------------

        var searchCarsRef = database
            .getReference("cars")
            .orderByChild("waiting")
            .equalTo(true)
            .limitToFirst(carsPerPage)

        if (queryByCarNumber.isNotEmpty()) {
            searchCarsRef = searchCarsRef
                .orderByChild("carNumber")
                .equalTo(queryByCarNumber)
        }

        fromNodeId?.let {
            searchCarsRef = searchCarsRef.startAfter(it)
        }
        searchCarsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onGetData(snapshot)
                // THREAD
                // ----------
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "waitingCars:onCancelled", error.toException())
            }
        })
    }

    suspend fun searchForParsedCars(
        fromNodeId: String? = null,
        carsPerPage: Int = QUERY_PAGE_SIZE,
        queryByCarNumber: String = ""
    ) {
        val searchCarsRef = database
            .getReference("cars")
            .orderByChild("waiting")
            .equalTo(false)
            .limitToFirst(carsPerPage)

        fromNodeId?.let {
            searchCarsRef.startAfter(it)
        }

        if (queryByCarNumber.isNotEmpty()) {
            searchCarsRef
                .orderByChild("carNumber")
                .equalTo(queryByCarNumber)
        }

        searchCarsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

private const val TAG = "CarsApi"