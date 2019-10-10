package ru.boltoff.camera_with_filters_app.presentation.camera

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.Image
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.util.Rational
import android.util.Size
import android.view.Surface
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.ImageCaptureConfig
import androidx.camera.extensions.BokehImageCaptureExtender
import androidx.lifecycle.LifecycleOwner
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_camera.*
import lv.slyfox.carguru.camera_with_filters_app.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.boltoff.camera_with_filters_app.helper.util.camera.EffectsRenderer
import ru.boltoff.camera_with_filters_app.presentation._base.BaseActivity
import java.nio.ByteBuffer


class CameraActivity : BaseActivity<CameraViewModel>() {

    override val layoutId: Int = R.layout.activity_camera
    override val viewModel: CameraViewModel by viewModel()

    private val effectsRenderer: EffectsRenderer by lazy {
        EffectsRenderer()
    }

    override fun initViews(savedInstanceState: Bundle?) {
        onPermissionsAccepted()
    }

    override fun initViewModel(owner: LifecycleOwner) {
        super.initViewModel(owner)
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

//        val pConfig = PreviewConfig.Builder()
//            .setTargetAspectRatio(aspectRatio)
//            .setTargetResolution(screen)
//            .setLensFacing(CameraX.LensFacing.FRONT)
//            .build()
//        val preview = Preview(pConfig)
//        preview.setOnPreviewOutputUpdateListener {
        //to update the surface texture we  have to destroy it first then re-add it
//            val parent = texture.parent as ViewGroup
//            parent.removeView(texture)
//            parent.addView(texture, 0)
//            texture.surfaceTexture = it.surfaceTexture
//            updateTransform()
//        }

        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .setLensFacing(CameraX.LensFacing.FRONT)
            .setTargetAspectRatio(aspectRatio)
            .setTargetResolution(screen)
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)

        imageAnalysis.setAnalyzer { image, _ ->
            image.image?.let { convertToBitmap(it) }
        }

        //bind to lifecycle:
//        CameraX.bindToLifecycle(this, preview, imageAnalysis)
        CameraX.bindToLifecycle(this, imageAnalysis)
    }

    private fun convertToBitmap(image: Image) {
        val rs = RenderScript.create(applicationContext)
        val yuvBytes = imageToByteBuffer(image)

        val bitmap = Bitmap.createBitmap(
            image.width,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        val allocationRgb = Allocation.createFromBitmap(rs, bitmap)

        val allocationYuv = Allocation.createSized(rs, Element.U8(rs), yuvBytes.array().size)
        allocationYuv.copyFrom(yuvBytes.array())

        val scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
        scriptYuvToRgb.setInput(allocationYuv)
        scriptYuvToRgb.forEach(allocationRgb)

        allocationRgb.copyTo(bitmap)

        // Release
        effectsRenderer.setBitmap(bitmap, image.width, image.height)
        surfaceView.requestRender()

        allocationYuv.destroy()
        allocationRgb.destroy()
        rs.destroy()
    }

    private fun imageToByteBuffer(image: Image): ByteBuffer {
        val crop = image.cropRect
        val width = crop.width()
        val height = crop.height()

        val planes = image.planes
        val rowData = ByteArray(planes[0].rowStride)
        val bufferSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8
        val output = ByteBuffer.allocateDirect(bufferSize)

        var channelOffset = 0
        var outputStride = 0

        for (planeIndex in 0..2) {
            when (planeIndex) {
                0 -> {
                    channelOffset = 0
                    outputStride = 1
                }
                1 -> {
                    channelOffset = width * height + 1
                    outputStride = 2
                }
                2 -> {
                    channelOffset = width * height
                    outputStride = 2
                }
            }

            val buffer = planes[planeIndex].buffer
            val rowStride = planes[planeIndex].rowStride
            val pixelStride = planes[planeIndex].pixelStride

            val shift = if (planeIndex == 0) 0 else 1
            val widthShifted = width shr shift
            val heightShifted = height shr shift

            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))

            for (row in 0 until heightShifted) {
                val length: Int

                if (pixelStride == 1 && outputStride == 1) {
                    length = widthShifted
                    buffer.get(output.array(), channelOffset, length)
                    channelOffset += length
                } else {
                    length = (widthShifted - 1) * pixelStride + 1
                    buffer.get(rowData, 0, length)

                    for (col in 0 until widthShifted) {
                        output.array()[channelOffset] = rowData[col * pixelStride]
                        channelOffset += outputStride
                    }
                }

                if (row < heightShifted - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
        }

        return output
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
        surfaceView?.setEGLContextClientVersion(2)
        surfaceView?.setRenderer(effectsRenderer)
        surfaceView?.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}