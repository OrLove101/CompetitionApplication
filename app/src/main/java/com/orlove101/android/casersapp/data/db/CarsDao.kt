package com.orlove101.android.casersapp.data.db

import androidx.room.*
import com.orlove101.android.casersapp.data.models.Car
import kotlinx.coroutines.flow.Flow

@Dao
interface CarsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(car: Car): Long

    @Query("SELECT * FROM cars WHERE cars.waiting = 1  LIMIT :pageSize OFFSET :getFrom")
    fun getWaitingCars(getFrom: Int, pageSize: Int): Flow<List<Car>>

    @Query("SELECT * FROM cars WHERE cars.waiting = 0 LIMIT :pageSize OFFSET :getFrom")
    fun getParsedCars(getFrom: Int, pageSize: Int): Flow<List<Car>>

    @Delete
    suspend fun deleteCar(car: Car)
}