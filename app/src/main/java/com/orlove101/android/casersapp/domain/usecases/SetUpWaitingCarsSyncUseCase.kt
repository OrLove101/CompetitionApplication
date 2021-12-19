package com.orlove101.android.casersapp.domain.usecases

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.orlove101.android.casersapp.domain.models.SetUpWaitingCarsParam
import com.orlove101.android.casersapp.utils.SYNC_INTERVAL_UNIT
import com.orlove101.android.casersapp.utils.SYNC_REPEAT_INTERVAL
import com.orlove101.android.casersapp.utils.works.SyncWorker
import javax.inject.Inject

class SetUpWaitingCarsSyncUseCase @Inject constructor() {

    operator fun invoke(
        param: SetUpWaitingCarsParam
    ) {
        val syncConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncWorkRequest = PeriodicWorkRequest.Builder(
            SyncWorker::class.java,
            SYNC_REPEAT_INTERVAL.toLong(),
            SYNC_INTERVAL_UNIT
        )
            .setConstraints(syncConstraint)
            .build()

        WorkManager.getInstance(param.context).enqueue(syncWorkRequest)
    }
}