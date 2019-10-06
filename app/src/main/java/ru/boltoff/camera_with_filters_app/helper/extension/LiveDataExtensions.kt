package lv.slyfox.carguru.camera_with_filters_app.helper.extension

import androidx.lifecycle.MutableLiveData
import lv.slyfox.carguru.camera_with_filters_app.helper.util.SingleLiveData


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