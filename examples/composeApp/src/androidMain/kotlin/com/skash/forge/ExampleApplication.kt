package com.skash.forge

import android.app.Application

class ExampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        PlatformContext.init(this)
    }
}