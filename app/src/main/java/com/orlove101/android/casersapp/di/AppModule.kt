package com.orlove101.android.casersapp.di

import android.app.Application
import androidx.room.Room
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.data.api.CarsApi
import com.orlove101.android.casersapp.data.db.CarsDao
import com.orlove101.android.casersapp.data.db.CarsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideDatabase(
        app: Application,
    ): CarsDatabase {
        return Room.databaseBuilder(
            app,
            CarsDatabase::class.java,
            app.getString(R.string.db_name)
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(db: CarsDatabase): CarsDao = db.getCarsDao()

//    @ApplicationScope
//    @Provides
//    @Singleton
//    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())
}
//
//@Retention(AnnotationRetention.RUNTIME)
//@Qualifier
//annotation class ApplicationScope