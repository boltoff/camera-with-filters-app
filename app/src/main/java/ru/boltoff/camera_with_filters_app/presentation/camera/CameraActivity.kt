package ru.boltoff.camera_with_filters_app.presentation.camera

import android.Manifest
import android.graphics.Matrix
import android.os.Bundle
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.lifecycle.LifecycleOwner
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_camera.*
import lv.slyfox.carguru.camera_with_filters_app.R
import ru.boltoff.camera_with_filters_app.presentation._base.BaseActivity

class CameraActivity : BaseActivity<CameraViewModel>() {

    override val layoutId: Int = R.layout.activity_camera
    override val viewModel: CameraViewModel by lazy { CameraViewModel() }

    override fun initViews(savedInstanceState: Bundle?) {
        askPermission(Manifest.permission.CAMERA) {
            //all of your permissions have been accepted by the user
            startCameraForCapture()
        }.onDeclined { error ->
            //at least one permission have been declined by the user
        }
    }

    override fun initViewModel(owner: LifecycleOwner) {
        super.initViewModel(owner)
    }

    private fun startCameraForCapture() {
        CameraX.unbindAll()
        val aspectRatio = Rational(textureView.width, textureView.height)
        val screen = Size(textureView.width, textureView.height)

        val pConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetResolution(screen)
            .setLensFacing(CameraX.LensFacing.FRONT)
            .build()
        val preview = Preview(pConfig)
        preview.onPreviewOutputUpdateListener =
            Preview.OnPreviewOutputUpdateListener { output ->
                //to update the surface texture we  have to destroy it first then re-add it
                val parent = textureView.parent as ViewGroup
                parent.removeView(textureView)
                parent.addView(textureView, 0)

                textureView.surfaceTexture = output.surfaceTexture
                updateTransform()
            }

        //bind to lifecycle:
        CameraX.bindToLifecycle(this, preview)
    }

    private fun updateTransform() {
        val mx = Matrix()
        val w = textureView.measuredWidth
        val h = textureView.measuredHeight

        val cX = w / 2f
        val cY = h / 2f

        val rotationDgr: Int = when (textureView.rotation.toInt()) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        mx.postRotate(rotationDgr.toFloat(), cX, cY)
        textureView.setTransform(mx)
    }
}