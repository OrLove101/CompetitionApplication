package com.orlove101.android.casersapp.domain.usecases

import com.orlove101.android.casersapp.domain.models.DeleteCarFromApiParam
import com.orlove101.android.casersapp.domain.repository.CarsRepository
import javax.inject.Inject

class DeleteCarFromApiUseCase @Inject constructor(
    private val carsRepository: CarsRepository
) {

    operator fun invoke(
        param: DeleteCarFromApiParam
    ) {
        carsRepository.deleteCarFromApi(param.car)
    }
}
