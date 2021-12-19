package com.orlove101.android.casersapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.domain.models.DeleteCarFromDbParam
import com.orlove101.android.casersapp.domain.usecases.ParsedCarsUseCases
import com.orlove101.android.casersapp.domain.models.SaveCarInDbParam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParsedCarsViewModel @Inject constructor(
    val parsedCarsUseCases: ParsedCarsUseCases
): ViewModel() {
    private val carsEventsChannel = Channel<ParsedCarsEvents>()
    val carsEvent = carsEventsChannel.receiveAsFlow()

    val cars: StateFlow<PagingData<CarDomain>> = parsedCarsUseCases.getParsedCarsUseCase()
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    fun deleteCar(car: CarDomain) = viewModelScope.launch {
        val param = DeleteCarFromDbParam(
            car = car,
            carsEventsChannel = carsEventsChannel
        )
        parsedCarsUseCases.deleteCarFromDbUseCase(param = param)
    }

    fun saveCar(car: CarDomain) = viewModelScope.launch {
        val param = SaveCarInDbParam(
            car = car
        )
        parsedCarsUseCases.saveCarInDbUseCase(param = param)
    }

    sealed class ParsedCarsEvents {
        data class ShowArticleDeletedSnackbar(
            val msgId: Int,
            val actonMsgId: Int,
            val article: CarDomain
        ): ParsedCarsEvents()
    }
}