package ru.boltoff.camera_with_filters_app.helper.extension

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.boltoff.camera_with_filters_app.di.DIManager

fun Application.initDIModules() {
    startKoin {
        androidContext(this@initDIModules)
        modules(DIManager.getModules())
    }
}