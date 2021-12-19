package com.orlove101.android.casersapp.domain.usecases

import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.domain.models.DeleteCarFromDbParam
import com.orlove101.android.casersapp.domain.repository.CarsRepository
import com.orlove101.android.casersapp.ui.viewmodels.ParsedCarsViewModel
import javax.inject.Inject

class DeleteCarFromDbUseCase @Inject constructor(
    private val carsRepository: CarsRepository
) {

    suspend operator fun invoke(
        param: DeleteCarFromDbParam
    ) {
        carsRepository.deleteCar(param.car)
        carsRepository.refreshPageSource()
        param.carsEventsChannel.send(
            ParsedCarsViewModel.ParsedCarsEvents.ShowArticleDeletedSnackbar(
                R.string.dlete_car_snackbar_msg,
                R.string.delete_article_snackbar_action,
                param.car
            )
        )
    }
}
