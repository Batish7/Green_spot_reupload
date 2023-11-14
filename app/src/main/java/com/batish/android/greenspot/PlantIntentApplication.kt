package com.batish.android.greenspot

import android.app.Application

class PlantIntentApplication : Application () {
    override fun onCreate() {
        super.onCreate()
        PlantRepository.initialize(this)
    }
}