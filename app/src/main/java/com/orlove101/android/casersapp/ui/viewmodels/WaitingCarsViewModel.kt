package com.orlove101.android.casersapp.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.orlove101.android.casersapp.R
import com.orlove101.android.casersapp.data.repository.CarsRepositoryImpl
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.domain.usecases.SetUpWaitingCarsParam
import com.orlove101.android.casersapp.domain.usecases.WaitingCarsUseCases
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import com.orlove101.android.casersapp.utils.Resource
import com.orlove101.android.casersapp.utils.filterByCarNumber
import com.orlove101.android.casersapp.utils.works.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WaitingCarsViewModel @Inject constructor(
    // TODO refactor with usecases with repo inside them
    val repositoryImpl: CarsRepositoryImpl,
    // TODO -----------------
    private val waitingCarsUseCases: WaitingCarsUseCases
): ViewModel() {

    private var carsResponse = ArrayList<CarDomain>()
    private val _cars: MutableStateFlow<Resource<List<CarDomain>>> = MutableStateFlow(Resource.Loading())
    val cars = _cars.asStateFlow()
    var carsInApiQuantity: Long? = null

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private var fromNodeId: String? = null

    private var _plombNumber: MutableStateFlow<Resource<String>> = MutableStateFlow(Resource.Loading())
    val plombNumber = _plombNumber.asStateFlow()

    var currentCar: CarDomain? = null
    var currentCarPlombsParsed = 0

    init {
        searchWaitingCars(isNewQuery = true)
    }

    fun setUpWaitingCarsSync(context: Context) {
        val param = SetUpWaitingCarsParam(
            context = context
        )
        waitingCarsUseCases.setUpWaitingCarsSyncUseCase(param)
        //
//        val syncConstraint = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//        val syncWorkRequest = PeriodicWorkRequest.Builder(
//            SyncWorker::class.java,
//            16,
//            TimeUnit.MINUTES
//        )
//            .setConstraints(syncConstraint)
//            .build()
//
//        WorkManager.getInstance(context).enqueue(syncWorkRequest)
        //
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

                getTextFromImage(imageBitmap, context)
            }
        }
    }

    fun setQuery(query: String) {
        _query.tryEmit(query)
    }

    private fun getTextFromImage(imageBitmap: Bitmap, context: Context) {
        val image = InputImage.fromBitmap(imageBitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                viewModelScope.launch {
                    processTextRecognitionResult(text, context)
                }
            }
            .addOnFailureListener { error ->
                error.printStackTrace();
            }
    }

    private fun processTextRecognitionResult(texts: Text, context: Context) {
        val blocks = texts.textBlocks

        if (blocks.size == 0 || blocks.size > 1) {
            _plombNumber.value = Resource.Error(context.getString(R.string.validation_error))
            return
        }
        processPlombNumber(texts.text, context)
    }

    private fun saveCar(car: CarDomain) = viewModelScope.launch {
        repositoryImpl.upsert(car)
        repositoryImpl.refreshPageSource()
    }

    private fun deleteCarFromApi(car: CarDomain) {
        repositoryImpl.deleteCarFromApi(car)
    }

    fun processPlombNumber(plombNumber: String, context: Context) {
        val regexPlombCondition = Regex("^[A-Za-z][0-9]{8}\$")
        val isValidPlombNumber = regexPlombCondition.matches(plombNumber)

        if (isValidPlombNumber) {
            var plombInfo = ""

            plombSuccessfullyParsed()
            if (allPlombParsed()) {
                currentCar?.let { car ->
                    saveCar(car)
                    deleteCarFromApi(car)
                }
                plombInfo = context.getString(R.string.all_plomb_parsed_toast)
            } else {
                plombInfo = context.getString(
                    R.string.some_plombs_parsed,
                    currentCarPlombsParsed,
                    currentCar?.plombQuantity
                )
            }
            _plombNumber.value = Resource.Success(plombInfo)
        } else {
            _plombNumber.value = Resource.Error(context.getString(R.string.validation_error))
        }
    }

    fun startParseNewCar(car: CarDomain) {
        currentCar = car
        currentCarPlombsParsed = 0
    }

    fun allPlombParsed(): Boolean {
        return currentCarPlombsParsed == currentCar?.plombQuantity
    }

    private fun plombSuccessfullyParsed() {
        currentCarPlombsParsed++
    }
}