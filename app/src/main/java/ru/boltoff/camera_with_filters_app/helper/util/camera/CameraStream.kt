package ru.boltoff.camera_with_filters_app.helper.util.camera

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CameraStream {
    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0
    private var screenshotName: String? = null

    private val textureHandle = IntArray(1)
    private val vertexBuffer: FloatBuffer
    // GL_TRIANGLE_STRIP rule
    // http://www.matrix44.net/cms/notes/opengl-3d-graphics/understanding-gl_triangle_strip
    private val vertexCoords = floatArrayOf(
        -1.0f, -1.0f, 0.0f, // Bottom-Left
        1.0f, -1.0f, 0.0f, // Bottom-Right
        -1.0f, 1.0f, 0.0f, // Top-Left
        1.0f, 1.0f, 0.0f  // Top-Right
    )
    private val textureBuffer: FloatBuffer
    // Texturing UV coordinates
    // http://ogldev.atspace.co.uk/www/tutorial16/tutorial16.html
    private val textureCoords = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)

    private val mProgram: Int
    private val vertexShaderCode = "attribute vec4 vPosition;" +
        "attribute vec2 vTexCoord;" +
        "varying vec2 texCoordVar;" +
        "void main() {" +
        "    gl_Position = vPosition;" +
        "    texCoordVar = vTexCoord;" +
        "};"

    // For video streaming from SurfaceTexture, GL_OES_EGL_image_external extension must be declared
    // and use samplerExternalOES instead of Sample2D
    // http://developer.android.com/reference/android/graphics/SurfaceTexture.html
    private val fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;" +
        "uniform samplerExternalOES texture;" +
        "varying vec2 texCoordVar;" +
        "void main() {" +
        "    gl_FragColor = texture2D(texture, texCoordVar);" +
        "}"

    // Create bitmap of video returned by glReadPixels()
    // Must be called in context of onFrameDraw()
    private fun createBitmap(fileName: String) {
        val videoFrame = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.ARGB_8888)
        val size = surfaceWidth * surfaceHeight
        val buf = ByteBuffer.allocateDirect(size * 4)
        buf.order(ByteOrder.nativeOrder())
        GLES20.glReadPixels(
            0,
            0,
            surfaceWidth,
            surfaceHeight,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            buf
        )
        val data = IntArray(size)
        buf.asIntBuffer().get(data)
        videoFrame.setPixels(
            data,
            size - surfaceWidth,
            -surfaceWidth,
            0,
            0,
            surfaceWidth,
            surfaceHeight
        )
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(fileName)
            videoFrame.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        videoFrame.recycle()
    }

    fun draw() {
        val mPositionHandle: Int
        val mTexCoordHandle: Int

        // Start using shader
        GLES20.glUseProgram(mProgram)

        // Get vertex position "vPosition" and texture coordinate "vTextCoord" handles
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "vTexCoord")

        // Enable vertex handle
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Set vertices data of vertex handle
        GLES20.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
            (COORDS_PER_VERTEX * BYTES_PER_FLOAT), vertexBuffer
        )

        // Enable texture handle
        GLES20.glEnableVertexAttribArray(mTexCoordHandle)

        // Set texture coordinates of texture coordinate handle
        GLES20.glVertexAttribPointer(
            mTexCoordHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false,
            (COORDS_PER_TEXTURE * BYTES_PER_FLOAT), textureBuffer
        )

        // Draw square by GL_TRIANGLE_STRIP
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCoords.size / COORDS_PER_VERTEX)

        // Disable vertex handle
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTexCoordHandle)

        screenshotName?.let {
            createBitmap(it)
            screenshotName = null
        }
    }

    init {
        // Prepare vertices buffer for square and texture buffer
        // We need square to put texture on it
        var bb: ByteBuffer = ByteBuffer.allocateDirect(vertexCoords.size * BYTES_PER_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertexCoords)
        vertexBuffer.position(0)
        bb = ByteBuffer.allocateDirect(textureCoords.size * BYTES_PER_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        textureBuffer = bb.asFloatBuffer()
        textureBuffer.put(textureCoords)
        textureBuffer.position(0)

        // All about Texture of OpenGL and GLSL Shader language
        // https://www.opengl.org/wiki/Texture#Texture_image_units

        // Create Vertex and Fragment Shaders
        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode))
        GLES20.glAttachShader(mProgram, loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode))
        GLES20.glLinkProgram(mProgram)

        // Assign GL_TEXTURE0 to fragment shader Sampler2D object "texture"
        GLES20.glUseProgram(mProgram)
        val texture: Int
        texture = GLES20.glGetUniformLocation(mProgram, "texture")
        GLES20.glUniform1i(texture, 0 /* texture unit 0 */)

        // Create "One" "texture object"
        GLES20.glGenTextures(1, textureHandle, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // Use GL_TEXTURE_EXTERNAL_OES instead of GL_TEXTURE0 for video stream comes from SurfaceTexture
        // http://developer.android.com/reference/android/graphics/SurfaceTexture.html
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureHandle[0])

        // Set up filter - GL_LINEAR for better image quality
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
    }

    fun getTextureHandle(): Int {
        return textureHandle[0]
    }

    fun setResolution(w: Int, h: Int) {
        surfaceWidth = w
        surfaceHeight = h
    }

    fun screenshot(fileName: String) {
        screenshotName = fileName
    }

    companion object {

        private val COORDS_PER_VERTEX = 3
        private val COORDS_PER_TEXTURE = 2
        private val BYTES_PER_FLOAT = 4

        private fun loadShader(type: Int, shaderCode: String): Int {
            val shader: Int = GLES20.glCreateShader(type)

            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            // Check shader compile status
            val compileStatus = intArrayOf(GLES20.GL_FALSE)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == GLES20.GL_FALSE) {
                val logSize = intArrayOf(0)
                GLES20.glGetShaderiv(shader, GLES20.GL_INFO_LOG_LENGTH, logSize, 0)
                if (logSize[0] > 0) {
                    val errorLog = GLES20.glGetShaderInfoLog(shader)
                    Log.d(CameraStream::class.java.name, errorLog)
                }
            }
            return shader
        }
    }
}