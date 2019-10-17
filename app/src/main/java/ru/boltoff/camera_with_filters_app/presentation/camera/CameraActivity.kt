package ru.boltoff.camera_with_filters_app.presentation.camera

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import ru.boltoff.camera_with_filters_app.helper.extension.showPermissionAlertDialog
import ru.boltoff.camera_with_filters_app.helper.util.OnSwipeListener
import ru.boltoff.camera_with_filters_app.presentation._base.BaseActivity


class CameraActivity : BaseActivity<CameraViewModel>() {

    companion object {
        private const val PROPERTY_SCALE_X = "scaleX"
        private const val PROPERTY_SCALE_Y = "scaleY"
    }

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
            observe(savePhoto) {
                gpuImageView.saveToPictures(it.first, it.second) { uri ->
                    viewModel.onPictureSaved(uri, it.second)
                }
            }
            observe(changeButtonSize) {
                animateButtonResizing(it.first, it.second)
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
        askPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            viewModel.onPermissionAccepted()
        }.onDeclined { error ->
            showPermissionAlertDialog {
                viewModel.onPermissionDeclined(error)
            }
        }
    }

    private fun animateButtonResizing(scale: Float, durationTime: Long) {
        AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    takePhotoButton,
                    PROPERTY_SCALE_X,
                    scale
                ).apply { duration = durationTime }
            ).with(
                ObjectAnimator.ofFloat(
                    takePhotoButton,
                    PROPERTY_SCALE_Y,
                    scale
                ).apply { duration = durationTime }
            )
        }.start()
    }

    private fun initListeners() {
        takePhotoButton.setOnTouchListener { _, event ->
            viewModel.onTouch(event)
            return@setOnTouchListener true
        }
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