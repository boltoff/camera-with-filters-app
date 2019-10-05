package lv.slyfox.carguru.camera_with_filters_app.helper.extension

import android.app.Application
import lv.slyfox.carguru.camera_with_filters_app.di.DIManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun Application.initDIModules() {
    startKoin {
        androidContext(this@initDIModules)
        modules(DIManager.getModules())
    }
}