package com.orlove101.android.casersapp.ui.viewModels

import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.domain.usecases.DeleteCarFromDbUseCase
import com.orlove101.android.casersapp.domain.usecases.GetParsedCarsUseCase
import com.orlove101.android.casersapp.domain.usecases.ParsedCarsUseCases
import com.orlove101.android.casersapp.domain.usecases.SaveCarInDbUseCase
import com.orlove101.android.casersapp.ui.viewmodels.ParsedCarsViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.isA

@ExperimentalCoroutinesApi
object ParsedCarsViewModelTest : Spek({
    val carsRepository by memoized {
        mockk<CarsRepositoryImpl>(relaxed = true)
    }
    val parsedCarsViewModel by memoized {
        ParsedCarsViewModel(
            parsedCarsUseCases = ParsedCarsUseCases(
                deleteCarFromDbUseCase = DeleteCarFromDbUseCase(carsRepository),
                saveCarInDbUseCase = SaveCarInDbUseCase(carsRepository),
                getParsedCarsUseCase = GetParsedCarsUseCase(carsRepository)
            )
        )
    }
    val car by memoized {
        mockk<CarDomain>(relaxed = true)
    }
    val testDispatcher = StandardTestDispatcher()

    beforeEachTest {
        Dispatchers.setMain(testDispatcher)
    }

    afterEachTest {
        Dispatchers.resetMain()
    }

    describe("Parsed cars view model test") {
        describe("Delete car") {
            it("Should show snackbar with undo action") {
                parsedCarsViewModel.deleteCar(car).invokeOnCompletion {
                    runTest {
                        expectThat(parsedCarsViewModel.carsEvent.last())
                            .isA<ParsedCarsViewModel.ParsedCarsEvents.ShowArticleDeletedSnackbar>()
                    }
                }
            }
        }
    }
})