package com.orlove101.android.casersapp.data.db

import androidx.room.*
import com.orlove101.android.casersapp.data.models.CarDb
import kotlinx.coroutines.flow.Flow

@Dao
interface CarsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(carApi: CarDb): Long

    @Query("SELECT * FROM cars LIMIT :pageSize OFFSET :getFrom")
    fun getParsedCars(getFrom: Int, pageSize: Int): Flow<List<CarDb>>

    @Delete
    suspend fun deleteCar(carApi: CarDb)
}