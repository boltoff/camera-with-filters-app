package lv.slyfox.carguru.camera_with_filters_app.presentation._base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import lv.slyfox.carguru.camera_with_filters_app.helper.util.SingleLiveData
import kotlin.coroutines.CoroutineContext


abstract class BaseViewModel : ViewModel(), CoroutineScope {

    init {
        viewModelScope.launch { onStart() }
    }

    protected val _showError = SingleLiveData<String>()
    val showError: LiveData<String> = _showError

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
        _showError.postValue(exception.message.toString())
    }

    protected abstract fun onStart()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job + handler

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancelChildren()
    }
}