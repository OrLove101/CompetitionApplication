package com.orlove101.android.casersapp.domain.usecases

import javax.inject.Inject

data class ParsedCarsUseCases @Inject constructor(
    val deleteCarFromDbUseCase: DeleteCarFromDbUseCase,
    val saveCarInDbUseCase: SaveCarInDbUseCase,
    val getParsedCarsUseCase: GetParsedCarsUseCase
)