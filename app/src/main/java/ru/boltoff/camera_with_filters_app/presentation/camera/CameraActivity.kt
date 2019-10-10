package ru.boltoff.camera_with_filters_app.presentation.camera

import android.Manifest
import android.graphics.Matrix
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.DEBUG_CHECK_GL_ERROR
import android.opengl.GLSurfaceView.DEBUG_LOG_GL_CALLS
import android.os.Bundle
import android.util.Rational
import android.util.Size
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.extensions.BokehImageCaptureExtender
import androidx.lifecycle.LifecycleOwner
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_camera.*
import lv.slyfox.carguru.camera_with_filters_app.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.boltoff.camera_with_filters_app.helper.util.camera.EffectsRenderer
import ru.boltoff.camera_with_filters_app.presentation._base.BaseActivity


class CameraActivity : BaseActivity<CameraViewModel>() {

    override val layoutId: Int = R.layout.activity_camera
    override val viewModel: CameraViewModel by viewModel()

//    private val effectsRenderer: EffectsRenderer by lazy {
//    }

    override fun initViews(savedInstanceState: Bundle?) {
        onPermissionsAccepted()
    }

    override fun initViewModel(owner: LifecycleOwner) {
        super.initViewModel(owner)
    }

    override fun onPause() {
        super.onPause()
        surfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        surfaceView.onResume()
    }

    private fun onPermissionsAccepted() {
        askPermission(Manifest.permission.CAMERA) {
            startCameraForCapture()
        }.onDeclined { error -> }
    }

    private fun startCameraForCapture() {
        initGLSurfaceView()

        CameraX.unbindAll()
        val aspectRatio = Rational(texture.width, texture.height)
        val screen = Size(texture.width, texture.height)

        val pConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetResolution(screen)
            .setLensFacing(CameraX.LensFacing.FRONT)
            .build()
        val preview = Preview(pConfig)
        preview.setOnPreviewOutputUpdateListener { output ->
            //to update the surface texture we  have to destroy it first then re-add it
//            output.surfaceTexture.attachToGLContext(textures[0])
//            val parent = texture.parent as ViewGroup
//            parent.removeView(texture)
//            parent.addView(texture, 0)
//            texture.surfaceTexture = output.surfaceTexture
            surfaceView.setEGLContextClientVersion(2)
            surfaceView.setRenderer(EffectsRenderer(output.surfaceTexture, output.textureSize))
            surfaceView.debugFlags = DEBUG_CHECK_GL_ERROR and DEBUG_LOG_GL_CALLS
            surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
//            updateTransform()
        }

        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .setLensFacing(CameraX.LensFacing.FRONT)
            .setTargetAspectRatio(aspectRatio)
            .setTargetResolution(screen)
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)

        imageAnalysis.setAnalyzer { image, _ ->
            surfaceView.requestRender()
        }

        //bind to lifecycle:
        CameraX.bindToLifecycle(this, preview, imageAnalysis)
    }

    private fun updateTransform() {
        val mx = Matrix()
        val w = texture.measuredWidth
        val h = texture.measuredHeight

        val cX = w / 2f
        val cY = h / 2f

        val rotationDgr: Int = when (texture.rotation.toInt()) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        mx.postRotate(rotationDgr.toFloat(), cX, cY)
        texture.setTransform(mx)

        BokehImageCaptureExtender.create(ImageCaptureConfig.Builder())
    }

    private fun initGLSurfaceView() {
//        surfaceView?.setEGLContextClientVersion(2)
//        surfaceView?.setRenderer(effectsRenderer)
//        surfaceView?.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}