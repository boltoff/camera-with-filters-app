package ru.boltoff.camera_with_filters_app.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.boltoff.camera_with_filters_app.helper.provider.ResourceProvider
import ru.boltoff.camera_with_filters_app.helper.util.PermissionUtil
import ru.boltoff.camera_with_filters_app.helper.util.camera.CameraLoader
import ru.boltoff.camera_with_filters_app.presentation.camera.CameraViewModel

object DIManager {

    fun getModules() = listOf(
        appModule,
        viewModelModule
    )

    private val appModule = module {
        single {
            ResourceProvider(androidContext())
        }
        single {
            CameraLoader(androidContext())
        }
        single {
            PermissionUtil(androidContext())
        }
    }

    private val viewModelModule = module {
        viewModel { CameraViewModel(get(), get(), get()) }
    }
}