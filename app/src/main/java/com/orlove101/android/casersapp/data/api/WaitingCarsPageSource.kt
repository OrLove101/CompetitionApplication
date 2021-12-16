package com.orlove101.android.casersapp.data.api

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.database.ktx.getValue
import com.orlove101.android.casersapp.data.models.Car
import com.orlove101.android.casersapp.utils.CASH_ELEMENTS_QUANTITY
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import kotlinx.coroutines.sync.Semaphore
import javax.inject.Inject

class WaitingCarsPageSource @Inject constructor(
    private val api: CarsApi,
    private val query: String
): PagingSource<Int, Car>() {
    // change to db
    
    override fun getRefreshKey(state: PagingState<Int, Car>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Car> {
        val page: Int = params.key ?: 1
        val getFromDb: Int = page.minus(1) * QUERY_PAGE_SIZE
        var lastItemNodeIndex: String? = null
        var carsToSend = carsFromApi

        // TODO add if on search for waitingCars
        // TODO !!!!!!!!  start listener in repo and here only get values from db  !!!!!!!!
        api.searchForWaitingCars(
            fromNodeId = fromNodeId,
            carsPerPage = QUERY_PAGE_SIZE,
            queryByCarNumber = query
        ) { dataSnapshot ->
            lastItemNodeIndex = dataSnapshot.children.last().key
            dataSnapshot.children.forEach { snapshot ->
                val car = snapshot.getValue<Car>()

                car?.let { carsFromApi.add(it) }
            }
            if (carsFromDb.last() == null) {
                // TODO add her to db
                val parsedDbCars = mutableListOf<Car>()

                carsFromDb.forEachIndexed { index, car ->
                    var carToAdd = car

                    if ((index >= getFromDb) && (index < (getFromDb + QUERY_PAGE_SIZE))) {
                        val indexInCarsFromApi = index.minus(getFromDb)

                        // add to db if not

                        carToAdd = carsFromApi[indexInCarsFromApi]
                    }
                    if (carToAdd != null) {
                        parsedDbCars.add(carToAdd)
                    }
                }
                parsedDbCars.forEachIndexed { index, car ->
                    carsFromDb[index] = car
                }
                //invalidate()
            }
        }
        // todo make flag to check if is loading and if it is
        // check internet and if no get from db
        // close firebase listener with invalidate
        if (carsToSend.isNotEmpty()) {
            val nextKey = if (carsToSend.size < QUERY_PAGE_SIZE) null else page + 1
            val prevKey = if (page == 1) null else page - 1

            fromNodeId = if (carsToSend.size < QUERY_PAGE_SIZE) null else lastItemNodeIndex
            Log.d(TAG, "loaded: ${carsToSend.size} nextKey: $nextKey prevKey: $prevKey")
            return LoadResult.Page(carsToSend, prevKey, nextKey)
        }
        return LoadResult.Invalid()

        // EVERY QUERY REWRITE DBS WALUES for firs 60 pages
    }

    companion object {
        val carsFromApi = mutableListOf<Car>()
        private var fromNodeId: String? = null
        private var carsFromDb = arrayOfNulls<Car>(CASH_ELEMENTS_QUANTITY)
    }
}

private const val TAG = "WaitingCarsPageSource"