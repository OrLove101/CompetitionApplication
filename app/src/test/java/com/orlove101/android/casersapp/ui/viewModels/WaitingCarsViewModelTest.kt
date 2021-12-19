package com.orlove101.android.casersapp.ui.viewModels

import android.content.Context
import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.domain.usecases.*
import com.orlove101.android.casersapp.ui.viewmodels.WaitingCarsViewModel
import com.orlove101.android.casersapp.utils.Resource
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.isA

@ExperimentalCoroutinesApi
object WaitingCarsViewModelTest : Spek({
    val carsRepository by memoized {
        mockk<CarsRepositoryImpl>(relaxed = true)
    }
    val waitingCarsViewModel by memoized {
        WaitingCarsViewModel(
            waitingCarsUseCases = WaitingCarsUseCases(
                setUpWaitingCarsSyncUseCase = SetUpWaitingCarsSyncUseCase(),
                searchWaitingCarsUseCase = SearchWaitingCarsUseCase(carsRepository),
                saveWaitingCarUseCase = SaveWaitingCarUseCase(carsRepository),
                deleteCarFromApiUseCase = DeleteCarFromApiUseCase(carsRepository)
            )
        )
    }
    val contextMock = mockk<Context>(relaxed = true)
    val testDispatcher = StandardTestDispatcher()

    beforeEachTest {
        Dispatchers.setMain(testDispatcher)
    }

    afterEachTest {
        Dispatchers.resetMain()
    }

    describe("Waiting cars view model test") {
        describe("Process plomb number") {
            it("Should emit success resource state") {
                waitingCarsViewModel.processPlombNumber("A12345678", contextMock)
                    expectThat(waitingCarsViewModel.plomb.value)
                        .isA<Resource.Success<String>>()
            }
            it("Should emit error resource state") {
                waitingCarsViewModel.processPlombNumber("jkla325lnds3j", contextMock)
                expectThat(waitingCarsViewModel.plomb.value)
                    .isA<Resource.Error<String>>()
            }
        }
    }
})