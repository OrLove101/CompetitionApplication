package com.orlove101.android.casersapp.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.getValue
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.data.api.CarsApi
import com.orlove101.android.casersapp.data.models.Car
import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WaitingCarsViewModel @Inject constructor(
    // TODO refactor with usecases with repo inside them
    val api: CarsApi,
    val repositoryImpl: CarsRepositoryImpl
    // TODO -----------------
): ViewModel() {
    private val carsEventsChannel = Channel<WaitingCarsEvents>()
    val carsEvent = carsEventsChannel.receiveAsFlow()

    private val _cars = MutableStateFlow(mutableListOf<Car>())
    val cars = _cars.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private var fromNodeId: String? = null


    fun searchWaitingCars(query: String = _query.value, isNewQuery: Boolean = false) {
        if (isNewQuery) fromNodeId = null

        repositoryImpl.searchForWaitingCars(
            queryByCarNumber = query,
            carsPerPage = QUERY_PAGE_SIZE,
            fromNodeId = fromNodeId
        ) { snapshot ->
            val cars = mutableListOf<Car>()
            var carsToEmit = cars

            snapshot.children.forEach { carSnapshot ->
                val car = carSnapshot.getValue<Car>()

                car?.let { cars.add(car) }
            }
            if (!fromNodeId.isNullOrBlank()) {
                val allCars = mutableListOf<Car>()

                _cars.value.addAll(cars)
                allCars.addAll(_cars.value)
                carsToEmit = allCars
            }
            Log.d(TAG, "listenQueryForSearchWaitingCars: ${carsToEmit.count()}")
            if(snapshot.children.count() > 0) fromNodeId = snapshot.children.last().key
            // TODO find out why dont drigger collectLatest block in fragment on second emit
            viewModelScope.launch {
                val result = _cars.emit(carsToEmit)
                Log.d(TAG, "listenQueryForSearchWaitingCars: $fromNodeId  $result")
            }
            Log.d(TAG, "listenQueryForSearchWaitingCars: $fromNodeId")
        }
    }


    fun handleImageResult(uri: Uri?, context: Context) {
        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val imageBitmap: Bitmap = Glide
                    .with(context)
                    .asBitmap()
                    .load(it)
                    .submit()
                    .get()

                getTextFromImage(imageBitmap)
            }
        }
    }

    fun setQuery(query: String) {
        _query.tryEmit(query)
    }

    private suspend fun getTextFromImage(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                viewModelScope.launch {
                    processTextRecognitionResult(text)
                }
            }
            .addOnFailureListener { error ->
                error.printStackTrace();
            }
    }

    private suspend fun processTextRecognitionResult(texts: Text) {
        val blocks = texts.textBlocks

        if (blocks.size == 0) {
            carsEventsChannel.send(
                WaitingCarsEvents.ShowToast(
                    textId = R.string.no_text_found
                )
            )
            return
        }
        if (blocks.size > 1) {
            carsEventsChannel.send(
                WaitingCarsEvents.ShowToast(
                    textId = R.string.some_text_fields_detected
                )
            )
            return
        }
        carsEventsChannel.send(
            WaitingCarsEvents.ShowToast(
                text = texts.text
            )
        )
    }

    sealed class WaitingCarsEvents {
        data class ShowToast(val textId: Int? = null, val text: String? = null): WaitingCarsEvents()
    }
}

private const val TAG = "WaitingCarsViewModel"