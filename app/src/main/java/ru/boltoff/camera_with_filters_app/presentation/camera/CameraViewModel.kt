package ru.boltoff.camera_with_filters_app.presentation.camera

import android.Manifest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.florent37.runtimepermission.PermissionResult
import ru.boltoff.camera_with_filters_app.helper.util.PermissionUtil
import ru.boltoff.camera_with_filters_app.helper.util.camera.Camera2Loader
import ru.boltoff.camera_with_filters_app.presentation._base.BaseViewModel

class CameraViewModel(
    private val cameraLoader: Camera2Loader,
    private val permissionUtil: PermissionUtil
) : BaseViewModel() {

    private val _updatePreview = MutableLiveData<Triple<ByteArray, Int, Int>>()
    val updatePreview: LiveData<Triple<ByteArray, Int, Int>> = _updatePreview

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

    }
}