package com.orlove101.android.casersapp.domain.usecases

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.orlove101.android.casersapp.utils.works.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SetUpWaitingCarsSyncUseCase @Inject constructor() {

    operator fun invoke(
        setUpWaitingCarsParam: SetUpWaitingCarsParam
    ) {
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

        WorkManager.getInstance(setUpWaitingCarsParam.context).enqueue(syncWorkRequest)
    }
}