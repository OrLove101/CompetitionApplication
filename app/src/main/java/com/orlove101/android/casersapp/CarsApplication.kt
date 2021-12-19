package com.orlove101.android.casersapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CarsApplication @Inject constructor(): Application()