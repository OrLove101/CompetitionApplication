package com.orlove101.android.casersapp.domain.usecases

import javax.inject.Inject

data class WaitingCarsUseCases @Inject constructor(
    val setUpWaitingCarsSyncUseCase: SetUpWaitingCarsSyncUseCase
)