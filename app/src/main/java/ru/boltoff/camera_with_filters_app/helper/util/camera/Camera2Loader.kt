package ru.boltoff.camera_with_filters_app.helper.util.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.util.Size
import ru.boltoff.camera_with_filters_app.helper.extension.generateNV21Data
import ru.boltoff.camera_with_filters_app.helper.extension.logAsError

class Camera2Loader(private val context: Context) {

    private var onPreviewFrame: ((data: ByteArray, width: Int, height: Int) -> Unit)? = null
    private var cameraInstance: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    fun onResume(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        setUpCamera()
    }

    fun onPause() {
        releaseCamera()
    }

    fun setOnPreviewFrameListener(onPreviewFrame: (data: ByteArray, width: Int, height: Int) -> Unit) {
        this.onPreviewFrame = onPreviewFrame
    }

    @SuppressLint("MissingPermission")
    private fun setUpCamera() {
        val cameraId = getCameraId() ?: return
        try {
            cameraManager.openCamera(cameraId, CameraDeviceCallback(), null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            "Opening camera (ID: $cameraId) failed.".logAsError()
        }
    }

    private fun releaseCamera() {
        imageReader?.close()
        cameraInstance?.close()
        captureSession?.close()
        imageReader = null
        cameraInstance = null
        captureSession = null
    }

    private fun getCameraId(): String? {
        return cameraManager.cameraIdList.find { id ->
            cameraManager
                .getCameraCharacteristics(id)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        }
    }

    private fun startCaptureSession() {
        val size = chooseOptimalSize()
        imageReader = ImageReader.newInstance(
            size.width,
            size.height,
            ImageFormat.YUV_420_888,
            2
        ).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader?.acquireNextImage() ?: return@setOnImageAvailableListener
                onPreviewFrame?.invoke(image.generateNV21Data(), image.width, image.height)
                image.close()
            }, null)
        }

        try {
            cameraInstance?.createCaptureSession(
                listOf(imageReader?.surface),
                CaptureStateCallback(),
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            "Failed to start camera session".logAsError()
        }
    }

    private fun chooseOptimalSize(): Size {
        if (viewWidth == 0 || viewHeight == 0) {
            return Size(0, 0)
        }
        val cameraId = getCameraId() ?: return Size(0, 0)
        val outputSizes = cameraManager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(ImageFormat.YUV_420_888)

        return outputSizes?.filter {
            it.width < viewHeight / 2 && it.height < viewWidth / 2
        }?.maxBy {
            it.width * it.height
        } ?: Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)
    }

    private inner class CameraDeviceCallback : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraInstance = camera
            startCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraInstance = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraInstance = null
        }
    }

    private inner class CaptureStateCallback : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            "Failed to configure capture session.".logAsError()
        }

        override fun onConfigured(session: CameraCaptureSession) {
            cameraInstance ?: return
            captureSession = session
            val builder = cameraInstance?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            imageReader?.surface?.let { builder?.addTarget(it) }
            try {
                builder?.let { session.setRepeatingRequest(it.build(), null, null) }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                "Failed to start camera preview because it couldn't access camera".logAsError()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                "Failed to start camera preview.".logAsError()
            }
        }
    }

    companion object {
        private const val PREVIEW_WIDTH = 480
        private const val PREVIEW_HEIGHT = 640
    }
}