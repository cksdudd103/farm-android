package com.smartfarm.app

import android.app.Application
import com.smartfarm.app.data.AppContainer

class SmartFarmApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer.getInstance(this)
    }
}
