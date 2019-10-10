package ru.boltoff.camera_with_filters_app.helper.util.camera

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Size
import ru.boltoff.camera_with_filters_app.helper.extension.logAsDebug
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class EffectsRenderer(
    private val surfaceTexture: SurfaceTexture,
    private val size: Size
) : GLSurfaceView.Renderer {

    private var photo: Bitmap? = null

    private val textures = IntArray(2)
    private var square: Square? = null
    private var cameraStream: CameraStream? = null
    private var effectContext: EffectContext? = null
    private var effect: Effect? = null

    override fun onDrawFrame(gl: GL10?) {
        "on draw frame".logAsDebug()
//        photo?.let {
//            if (effectContext == null) {
//                effectContext = EffectContext.createWithCurrentGlContext()
//            }
//            effect?.release()
//            grayScaleEffect()
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        surfaceTexture.updateTexImage()
        cameraStream?.draw(textures[0])
//            square?.draw(textures[1])
//        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
//        GLES20.glClearColor(0f, 0f, 0f, 1f)
//        createTexture()
//        cameraStream = CameraStream(textures[0])
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        createTexture()
        cameraStream = CameraStream()
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
    }

    private fun createTexture() {
        GLES20.glGenTextures(2, textures, 0)
        GLES20.glBindTexture(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            textures[0]
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE
        )
    }

    private fun grayScaleEffect() {
        val factory = effectContext?.factory
        effect = factory?.createEffect(EffectFactory.EFFECT_GRAYSCALE)
        effect?.apply(textures[0], size.width, size.height, textures[1])
    }
}