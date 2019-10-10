package ru.boltoff.camera_with_filters_app

import android.app.Application
import ru.boltoff.camera_with_filters_app.helper.extension.initDIModules


class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initDIModules()
    }
}