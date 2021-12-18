package com.orlove101.android.casersapp

import android.app.Application
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.orlove101.android.casersapp.utils.works.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class CarsApplication @Inject constructor(): Application()