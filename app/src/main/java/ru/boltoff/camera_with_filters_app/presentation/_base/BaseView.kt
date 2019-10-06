package ru.boltoff.camera_with_filters_app.presentation._base

interface BaseView<VM> {

    val layoutId: Int

    val viewModel: VM

}