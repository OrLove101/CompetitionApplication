package com.orlove101.android.casersapp.di

import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.domain.repository.CarsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module()
abstract class AppDataModule {

    @Binds
    @Singleton
    abstract fun bindsRepository(repositoryImpl: CarsRepositoryImpl): CarsRepository
}