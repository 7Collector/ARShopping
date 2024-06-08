package iitr.collector.shopping.ar.helpers

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.WindowManager

class DisplayRotationHelper(private val context: Context) : DisplayManager.DisplayListener {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val display: Display = windowManager.defaultDisplay

    fun onResume() {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(this, null)
    }

    fun onPause() {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.unregisterDisplayListener(this)
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        // Handle surface changes if necessary
    }

    fun getRotation(): Int {
        return display.rotation
    }

    override fun onDisplayAdded(displayId: Int) {}

    override fun onDisplayRemoved(displayId: Int) {}

    override fun onDisplayChanged(displayId: Int) {}
}
