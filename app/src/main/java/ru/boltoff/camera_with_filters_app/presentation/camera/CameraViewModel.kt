package ru.boltoff.camera_with_filters_app.presentation.camera

import android.Manifest
import android.net.Uri
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.florent37.runtimepermission.PermissionResult
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import ru.boltoff.camera_with_filters_app.R
import ru.boltoff.camera_with_filters_app.helper.provider.ResourceProvider
import ru.boltoff.camera_with_filters_app.helper.util.PermissionUtil
import ru.boltoff.camera_with_filters_app.helper.util.SingleLiveData
import ru.boltoff.camera_with_filters_app.helper.util.camera.CameraLoader
import ru.boltoff.camera_with_filters_app.presentation._base.BaseViewModel

class CameraViewModel(
    private val cameraLoader: CameraLoader,
    private val permissionUtil: PermissionUtil,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    companion object {
        private const val FILTERS_COUNT = 3

        private const val FILTER_GRAY_SCALE = 1
        private const val FILTER_NEGATIVE = 2
        private const val FILTER_SEPIA = 3

        private const val PHOTO_FOLDER_NAME = "CameraWithFiltersApp"
        private const val JPG_EXTENSION = ".jpg"

        private const val BUTTON_SIZE_CHANGING_DURATION = 300L
        private const val BUTTON_SIZE_CHANGING_SCALE_NORMAL = 1f
        private const val BUTTON_SIZE_CHANGING_SCALE_INCREASED = 1.5f
    }

    private val _updatePreview = MutableLiveData<Triple<ByteArray, Int, Int>>()
    val updatePreview: LiveData<Triple<ByteArray, Int, Int>> = _updatePreview

    private val _setFilter = MutableLiveData<GPUImageFilter>()
    val setFilter: LiveData<GPUImageFilter> = _setFilter

    private val _savePhoto = SingleLiveData<Pair<String, String>>()
    val savePhoto: LiveData<Pair<String, String>> = _savePhoto

    private val _changeButtonSize = SingleLiveData<Pair<Float, Long>>()
    val changeButtonSize: LiveData<Pair<Float, Long>> = _changeButtonSize

    private var currentPosition = 0

    fun onResume(height: Int, width: Int) {
        if (permissionUtil.isPermissionGranted(Manifest.permission.CAMERA)) {
            cameraLoader.onResume(width, height)
        }
    }

    fun onPause() {
        cameraLoader.onPause()
    }

    fun onPermissionAccepted() {
        cameraLoader.setOnPreviewFrameListener { data, width, height ->
            _updatePreview.value = Triple(data, width, height)
        }
    }

    fun onPermissionDeclined(permissionResult: PermissionResult) {
        if (permissionResult.hasForeverDenied()) {
            permissionResult.goToSettings()
            return
        }
        permissionResult.askAgain()
    }

    fun onTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                changeButtonSize(BUTTON_SIZE_CHANGING_SCALE_INCREASED)
            }
            MotionEvent.ACTION_UP -> {
                changeButtonSize(BUTTON_SIZE_CHANGING_SCALE_NORMAL)
                takePhoto()
            }
        }
    }

    fun onPictureSaved(uri: Uri?, photoName: String) {
        if (uri == null) {
            _showError.value = resourceProvider.getString(R.string.message_can_not_save_photo)
        } else {
            _showMessage.value = resourceProvider.getString(
                R.string.message_photo_saved_at,
                "$PHOTO_FOLDER_NAME/$photoName"
            )
        }
    }

    fun onSwipeRight() {
        if (currentPosition > 0) {
            changeFilter(currentPosition - 1)
        }
    }

    fun onSwipeLeft() {
        if (currentPosition < FILTERS_COUNT) {
            changeFilter(currentPosition + 1)
        }
    }

    private fun takePhoto() {
        _savePhoto.value = Pair(PHOTO_FOLDER_NAME, "${System.currentTimeMillis()}$JPG_EXTENSION")
    }

    private fun changeButtonSize(scale: Float) {
        _changeButtonSize.value = Pair(
            scale,
            BUTTON_SIZE_CHANGING_DURATION
        )
    }

    private fun changeFilter(position: Int) {
        _setFilter.value = when (position) {
            FILTER_GRAY_SCALE -> GPUImageGrayscaleFilter()
            FILTER_NEGATIVE -> GPUImageColorInvertFilter()
            FILTER_SEPIA -> GPUImageSepiaToneFilter()
            else -> GPUImageFilter()
        }
        currentPosition = position
    }
}