package com.orlove101.android.casersapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.domain.models.CarDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParsedCarsViewModel @Inject constructor(
    val repositoryImpl: CarsRepositoryImpl
): ViewModel() {
    private val carsEventsChannel = Channel<ParsedCarsEvents>()
    val carsEvent = carsEventsChannel.receiveAsFlow()

    val cars: StateFlow<PagingData<CarDomain>> = repositoryImpl.getParsedCars()
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    fun deleteCar(car: CarDomain) = viewModelScope.launch {
        repositoryImpl.deleteCar(car)
        repositoryImpl.refreshPageSource()
        carsEventsChannel.send(
            ParsedCarsEvents.ShowArticleDeletedSnackbar(
                R.string.dlete_car_snackbar_msg,
                R.string.delete_article_snackbar_action,
                car
            )
        )
    }

    fun saveCar(car: CarDomain) = viewModelScope.launch {
        repositoryImpl.upsert(car)
        repositoryImpl.refreshPageSource()
    }

    sealed class ParsedCarsEvents {
        data class ShowArticleDeletedSnackbar(
            val msgId: Int,
            val actonMsgId: Int,
            val article: CarDomain
        ): ParsedCarsEvents()
    }
}