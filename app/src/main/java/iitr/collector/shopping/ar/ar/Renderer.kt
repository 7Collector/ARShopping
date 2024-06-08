package iitr.collector.shopping.ar.ar

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import iitr.collector.shopping.ar.helpers.DisplayRotationHelper
import iitr.collector.shopping.ar.helpers.TextureDisplay
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Renderer(private val context: Context, private val session: Session): GLSurfaceView.Renderer {
    private lateinit var displayRotationHelper: DisplayRotationHelper
    private lateinit var textureDisplay: TextureDisplay
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        displayRotationHelper = DisplayRotationHelper(context)
        textureDisplay = TextureDisplay()
        textureDisplay.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        displayRotationHelper.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        try {
            session.setCameraTextureName(textureDisplay.textureId)
            val frame: Frame = session.update()
            textureDisplay.onDrawFrame(frame)
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        }
    }
}