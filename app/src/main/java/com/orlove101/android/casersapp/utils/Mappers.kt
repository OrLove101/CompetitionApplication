package com.orlove101.android.casersapp.utils

import com.orlove101.android.casersapp.data.models.CarDb
import com.orlove101.android.casersapp.domain.models.CarDomain

fun List<CarDb>.mapCarsDbListToCarsDomainList(): List<CarDomain> {
    val parsedList = mutableListOf<CarDomain>()

    for (car in this) {
        parsedList.add(car.mapToDomainCar())
    }
    return parsedList
}

fun CarDb.mapToDomainCar(): CarDomain {
    return CarDomain(
        uuid = this.uuid,
        carNumber = this.carNumber,
        startWaitingAt = this.startWaitingAt,
        stopWaitingAt = this.stopWaitingAt,
        cargoDescription = this.cargoDescription,
        plombQuantity = this.plombQuantity,
        waiting = this.waiting
    )
}


fun CarDomain.mapToDbCar(): CarDb {
    return CarDb(
        uuid = this.uuid,
        carNumber = this.carNumber,
        startWaitingAt = this.startWaitingAt,
        stopWaitingAt = this.stopWaitingAt,
        cargoDescription = this.cargoDescription,
        plombQuantity = this.plombQuantity,
        waiting = this.waiting
    )
}