package com.orlove101.android.casersapp.utils

import com.orlove101.android.casersapp.domain.models.CarDomain

fun List<CarDomain>.filterByCarNumber(query: String): List<CarDomain> {
    return this.filter { car ->
        car.carNumber.startsWith(query)
    }
}

fun getRandomCarNumber(): String {
    val allowedPrefixPostfixChars = ('A'..'Z')
    val allowedDigitsChars = ('0'..'9')
    val numberLength = 4
    val prefixPostfixLength = 2

    val prefix = (1..prefixPostfixLength)
        .map { allowedPrefixPostfixChars.random() }
        .joinToString("")
    val number = (1..numberLength)
        .map { allowedDigitsChars.random() }
        .joinToString("")
    val postfix = (1..prefixPostfixLength)
        .map { allowedPrefixPostfixChars.random() }
        .joinToString("")

    return prefix + number + postfix
}