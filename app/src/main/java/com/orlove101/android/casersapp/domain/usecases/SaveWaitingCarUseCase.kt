package com.orlove101.android.casersapp.domain.usecases

import com.orlove101.android.casersapp.domain.models.SaveWaitingCarParam
import com.orlove101.android.casersapp.domain.repository.CarsRepository
import javax.inject.Inject

class SaveWaitingCarUseCase @Inject constructor(
    private val carsRepository: CarsRepository
) {

    suspend operator fun invoke(
        param: SaveWaitingCarParam
    ) {
        carsRepository.upsert(param.car)
        carsRepository.refreshPageSource()
    }
}
