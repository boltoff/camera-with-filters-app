package ru.boltoff.camera_with_filters_app.presentation.camera

import android.Manifest
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import com.github.florent37.runtimepermission.kotlin.askPermission
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.util.Rotation
import kotlinx.android.synthetic.main.activity_camera.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.boltoff.camera_with_filters_app.R
import ru.boltoff.camera_with_filters_app.helper.extension.observe
import ru.boltoff.camera_with_filters_app.helper.util.OnSwipeListener
import ru.boltoff.camera_with_filters_app.presentation._base.BaseActivity

class CameraActivity : BaseActivity<CameraViewModel>() {

    override val layoutId: Int = R.layout.activity_camera
    override val viewModel: CameraViewModel by viewModel()

    override fun initViews(savedInstanceState: Bundle?) {
        initGpuImage()
        checkCameraPermission()
        initListeners()
    }

    override fun initViewModel(owner: LifecycleOwner) {
        super.initViewModel(owner)

        with(viewModel) {
            observe(updatePreview) {
                gpuImageView.updatePreviewFrame(it.first, it.second, it.third)
            }
            observe(setFilter) {
                gpuImageView.filter = it
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gpuImageView.doOnLayout { viewModel.onResume(it.height, it.width) }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun initGpuImage() {
        gpuImageView.gpuImage.setRotation(Rotation.ROTATION_270, false, true)
        gpuImageView.setRenderMode(GPUImageView.RENDERMODE_CONTINUOUSLY)
    }

    private fun checkCameraPermission() {
        askPermission(Manifest.permission.CAMERA) {
            viewModel.onPermissionAccepted()
        }.onDeclined { error -> viewModel.onPermissionDeclined(error) }
    }

    private fun initListeners() {
        takePhotoButton.setOnClickListener { viewModel.onTakePhotoButtonClick() }
        gpuImageView.setOnTouchListener(
            OnSwipeListener(
                context = this,
                onSwipeRight = {
                    viewModel.onSwipeRight()
                },
                onSwipeLeft = {
                    viewModel.onSwipeLeft()
                }
            )
        )
    }
}