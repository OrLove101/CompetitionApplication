package com.orlove101.android.casersapp.domain.models

import com.orlove101.android.casersapp.ui.viewmodels.ParsedCarsViewModel
import kotlinx.coroutines.channels.Channel

class DeleteCarFromDbParam(
    val car: CarDomain,
    val carsEventsChannel: Channel<ParsedCarsViewModel.ParsedCarsEvents>,
)
