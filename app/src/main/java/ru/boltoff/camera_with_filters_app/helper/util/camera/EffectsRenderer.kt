package ru.boltoff.camera_with_filters_app.helper.util.camera

import android.graphics.Bitmap
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import ru.boltoff.camera_with_filters_app.helper.extension.logAsDebug
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class EffectsRenderer : GLSurfaceView.Renderer {

    private var photo: Bitmap? = null
    private var photoWidth: Int = 0
    private var photoHeight: Int = 0

    private val textures = IntArray(2)
    private var square: Square? = null
    private var effectContext: EffectContext? = null
    private var effect: Effect? = null

    override fun onDrawFrame(gl: GL10?) {
        "on draw frame".logAsDebug()
        photo?.let {
            if (effectContext == null) {
                effectContext = EffectContext.createWithCurrentGlContext()
            }
            effect?.release()
            grayScaleEffect()
            square?.draw(textures[1])
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        "on surface changed".logAsDebug()
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        generateSquare()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    }

    fun setBitmap(photo: Bitmap, photoWidth: Int, photoHeight: Int) {
        this.photo = photo
        this.photoHeight = photoHeight
        this.photoWidth = photoWidth
    }

    private fun grayScaleEffect() {
        val factory = effectContext?.factory
        effect = factory?.createEffect(EffectFactory.EFFECT_GRAYSCALE)
        effect?.apply(textures[0], photoWidth, photoHeight, textures[1])
    }

    private fun generateSquare() {
        GLES20.glGenTextures(2, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        photo?.let { GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, photo, 0) }
        square = Square()
    }
}