package com.orlove101.android.casersapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.orlove101.android.casersapp.data.models.CarDb

@Database(
    entities = [CarDb::class],
    version = 1
)
abstract class CarsDatabase: RoomDatabase() {
    abstract fun getCarsDao(): CarsDao
}