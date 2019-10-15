package ru.boltoff.camera_with_filters_app.helper.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ru.boltoff.camera_with_filters_app.helper.util.SingleLiveData

fun MutableLiveData<Unit>.call() {
    value = Unit
}

fun MutableLiveData<Unit>.postCall() {
    postValue(Unit)
}

fun SingleLiveData<Unit>.call() {
    value = Unit
}

fun SingleLiveData<Unit>.postCall() {
    postValue(Unit)
}

fun <T> AppCompatActivity.observe(liveData: LiveData<T>, observer: ((T) -> Unit)) {
    liveData.observe(this, Observer {
        observer.invoke(it)
    })
}