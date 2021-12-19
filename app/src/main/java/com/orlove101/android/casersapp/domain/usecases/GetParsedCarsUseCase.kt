package com.orlove101.android.casersapp.domain.usecases

import androidx.paging.PagingData
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.domain.repository.CarsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetParsedCarsUseCase @Inject constructor(
    private val carsRepository: CarsRepository
) {

    operator fun invoke(): Flow<PagingData<CarDomain>> {
        return carsRepository.getParsedCars()
    }
}
