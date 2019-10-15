package ru.boltoff.camera_with_filters_app.helper.util.camera

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.opengles.GL10

class CameraEffectsRenderer : GLSurfaceView.Renderer {
    private val TAG = this.javaClass.name
    private var cameraStream: CameraStream? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var egl10: EGL10? = null
    private var eglDisplay: EGLDisplay? = null
    private var eglContext: EGLContext? = null
    private var eglSurfacePreview: EGLSurface? = null
    private var eglSurfaceEncode: EGLSurface? = null

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        cameraStream = CameraStream()
        surfaceTexture = null
        egl10 = EGLContext.getEGL() as EGL10
        eglDisplay = egl10!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
    }

    fun getTextureHandle(): Int {
        if (cameraStream == null) {
            cameraStream = CameraStream()
        }
        return cameraStream?.getTextureHandle() ?: 0
    }

    override fun onDrawFrame(gl: GL10) {
        // Step 8: SurfaceTexture update texture image in context of GLSurfaceView.Renderer.onDrawFrame()
        if (surfaceTexture != null) {
            // Here is right context of calling SurfaceTexture.updateTexImage()
            surfaceTexture!!.updateTexImage()
            surfaceTexture = null
        }

        // Step 9: Fragment shader draw preview texture (samplerExternalOES)
        // Draw on MediaCodec InputSurface for encode
        egl10!!.eglMakeCurrent(eglDisplay, eglSurfaceEncode, eglSurfaceEncode, eglContext)
        cameraStream!!.draw()
        egl10!!.eglSwapBuffers(eglDisplay, eglSurfaceEncode)
        // Draw on native window (surface) for preview
        egl10!!.eglMakeCurrent(eglDisplay, eglSurfacePreview, eglSurfacePreview, eglContext)
        cameraStream!!.draw()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        cameraStream!!.setResolution(width, height)
    }

    fun updateTexture(st: SurfaceTexture) {
        // Update SurfaceText pointer to update texture in next call of onDrawFrame()
        surfaceTexture = st
    }

    fun screenshot(fileName: String) {
        cameraStream!!.screenshot(fileName)
    }

    fun setEglContext(ctx: EGLContext) {
        eglContext = ctx
    }

    fun setEglSurface(preview: EGLSurface, encode: EGLSurface) {
        eglSurfacePreview = preview
        eglSurfaceEncode = encode
    }
}