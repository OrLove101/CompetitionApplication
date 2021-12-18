package com.orlove101.android.casersapp.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import com.orlove101.android.casersapp.utils.Resource
import com.orlove101.android.casersapp.utils.filterByCarNumber
import com.orlove101.android.casersapp.utils.works.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WaitingCarsViewModel @Inject constructor(
    // TODO refactor with usecases with repo inside them
    val repositoryImpl: CarsRepositoryImpl
    // TODO -----------------
): ViewModel() {



    private val carsEventsChannel = Channel<WaitingCarsEvents>()
    val carsEvent = carsEventsChannel.receiveAsFlow()

    private var carsResponse = ArrayList<CarDomain>()
    private val _cars: MutableStateFlow<Resource<List<CarDomain>>> = MutableStateFlow(Resource.Loading())
    val cars = _cars.asStateFlow()
    var carsInApiQuantity: Long? = null

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private var fromNodeId: String? = null

    init {
        searchWaitingCars(isNewQuery = true)
    }

    fun setUpWaitingCarsSync(context: Context) {
        val syncConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncWorkRequest = PeriodicWorkRequest.Builder(
            SyncWorker::class.java,
            16,
            TimeUnit.MINUTES
        )
            .setConstraints(syncConstraint)
            .build()

        WorkManager.getInstance(context).enqueue(syncWorkRequest)
    }

    fun searchWaitingCars(isNewQuery: Boolean = false) {
        _cars.value = Resource.Loading()
        if (isNewQuery) fromNodeId = null
        repositoryImpl.searchForWaitingCars(
            carsPerPage = QUERY_PAGE_SIZE,
            fromNodeId = fromNodeId,
            countCarsInApi = ::setCarsInApiQuantity,
            onDataChanged = ::handleCarsApiResponse
        )
    }

    private fun handleCarsApiResponse(snapshot: DataSnapshot) {
        var response: List<CarDomain>? = null

        if (fromNodeId.isNullOrBlank()) carsResponse = ArrayList()
        if (snapshot.children.count() > 0) fromNodeId = snapshot.children.last().key
        snapshot.children.forEach { carSnapshot ->
            carSnapshot.getValue<CarDomain>()?.let { car ->
                if (car.waiting) carsResponse.add(car)
            }
        }
        response = if (query.value.isBlank()) carsResponse else carsResponse.filterByCarNumber(query.value)
        _cars.value = Resource.Success(response)
    }

    private fun setCarsInApiQuantity(carsQuantity: Long) {
        carsInApiQuantity = carsQuantity
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