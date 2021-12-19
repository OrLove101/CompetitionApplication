package com.orlove101.android.casersapp.domain.usecases

import com.orlove101.android.casersapp.domain.models.SearchWaitingCarsParam
import com.orlove101.android.casersapp.domain.repository.CarsRepository
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import javax.inject.Inject

class SearchWaitingCarsUseCase @Inject constructor(
    private val carsRepository: CarsRepository
) {

    operator fun invoke(
        param: SearchWaitingCarsParam
    ) {
        carsRepository.searchForWaitingCars(
            carsPerPage = QUERY_PAGE_SIZE,
            fromNodeId = param.fromNodeId,
            countCarsInApi = param.countCarsInApi,
            onDataChanged = param.onDataChanged
        )
    }
}