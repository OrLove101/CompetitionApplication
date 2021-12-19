package com.orlove101.android.casersapp.domain.usecases

import com.orlove101.android.casersapp.domain.models.SaveCarInDbParam
import com.orlove101.android.casersapp.domain.repository.CarsRepository
import javax.inject.Inject

class SaveCarInDbUseCase @Inject constructor(
    private val carsRepository: CarsRepository
) {

    suspend operator fun invoke(
        param: SaveCarInDbParam
    ) {
        carsRepository.upsert(param.car)
        carsRepository.refreshPageSource()
    }
}
