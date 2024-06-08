package iitr.collector.shopping.ar.ar

import android.content.Context
import android.opengl.GLSurfaceView
import com.google.ar.core.Session

class MyGLSurfaceView(context: Context, session: Session): GLSurfaceView(context) {
    private val renderer: iitr.collector.shopping.ar.ar.Renderer
    init {
        // Set the OpenGL ES version
        setEGLContextClientVersion(2)

        // Initialize the renderer
        renderer = Renderer(context, session)

        // Set the renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        // Set the render mode to only render when there is a change in the drawing data
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}