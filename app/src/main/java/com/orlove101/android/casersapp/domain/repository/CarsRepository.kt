package com.orlove101.android.casersapp.domain.repository

import androidx.paging.PagingData
import com.google.firebase.database.DataSnapshot
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import kotlinx.coroutines.flow.Flow

interface CarsRepository {
    fun refreshPageSource()

    suspend fun upsert(car: CarDomain): Long

    suspend fun deleteCar(car: CarDomain)

    fun getParsedCars(): Flow<PagingData<CarDomain>>

    fun deleteCarFromApi(car: CarDomain)

    fun searchForWaitingCars(
        fromNodeId: String?,
        carsPerPage: Int = QUERY_PAGE_SIZE,
        countCarsInApi: (Long) -> Unit,
        onDataChanged: (DataSnapshot) -> Unit
    )
}