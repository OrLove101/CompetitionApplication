package com.orlove101.android.casersapp.data.page_sources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.orlove101.android.casersapp.data.db.CarsDatabase
import com.orlove101.android.casersapp.domain.models.CarDomain
import com.orlove101.android.casersapp.utils.QUERY_PAGE_SIZE
import com.orlove101.android.casersapp.utils.mapCarsDbListToCarsDomainList
import kotlinx.coroutines.flow.first

class ParsedCarsPageSource(
    private val db: CarsDatabase
): PagingSource<Int, CarDomain>() {

    override fun getRefreshKey(state: PagingState<Int, CarDomain>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CarDomain> {
        val page: Int = params.key ?: 1
        val getFrom: Int = page.minus(1) * QUERY_PAGE_SIZE
        val cars = db
            .getCarsDao()
            .getParsedCars(
                getFrom = getFrom,
                pageSize = QUERY_PAGE_SIZE
            )
            .first()
        val nextKey = if (cars.size < QUERY_PAGE_SIZE) null else page + 1
        val prevKey = if (page == 1) null else page - 1

        return LoadResult.Page(cars.mapCarsDbListToCarsDomainList(), prevKey, nextKey)
    }
}