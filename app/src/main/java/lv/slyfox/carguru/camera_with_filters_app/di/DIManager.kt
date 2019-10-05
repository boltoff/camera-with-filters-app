package lv.slyfox.carguru.camera_with_filters_app.di

import lv.slyfox.carguru.camera_with_filters_app.helper.provider.ResourceProvider
import lv.slyfox.carguru.camera_with_filters_app.presentation.camera.CameraViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object DIManager {

    fun getModules() = listOf(
        appModule,
        viewModelModule
    )

    private val appModule = module {
        single {
            ResourceProvider(androidContext())
        }
    }

    private val viewModelModule = module {
        viewModel { CameraViewModel() }
    }
}