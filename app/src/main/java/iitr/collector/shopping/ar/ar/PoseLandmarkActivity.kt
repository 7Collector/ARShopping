package iitr.collector.shopping.ar.ar

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableException
import iitr.collector.shopping.ar.helpers.CameraPermissionHelper


class PoseLandmarkActivity : AppCompatActivity() {
    private lateinit var session: Session
    private lateinit var myGLSurfaceView:MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (isARCoreSupportedAndUpToDate()){
            if (CameraPermissionHelper.hasCameraPermission(this)) {
                createSession()
                myGLSurfaceView=MyGLSurfaceView(this, session)
                setContentView(myGLSurfaceView)
            } else {
                CameraPermissionHelper.requestCameraPermission(this)
            }
        } else {
            Toast.makeText(this, "ARCore is not supported or needs to be updated.", Toast.LENGTH_LONG).show()
            finish()
        }


    }

    override fun onPause() {
        super.onPause()
        session?.pause()
        myGLSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        } else {
            session?.resume()
            myGLSurfaceView.onResume()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    private fun isARCoreSupportedAndUpToDate(): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        when (availability) {
            Availability.SUPPORTED_INSTALLED -> return true
            Availability.SUPPORTED_APK_TOO_OLD, Availability.SUPPORTED_NOT_INSTALLED -> {
                try {
                    // Request ARCore installation or update if needed.
                    val installStatus = ArCoreApk.getInstance().requestInstall(this, true)
                    return when (installStatus) {
                        ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                            Log.i(TAG, "ARCore installation requested.")
                            false
                        }

                        ArCoreApk.InstallStatus.INSTALLED -> true
                    }
                } catch (e: UnavailableException) {
                    Log.e(TAG, "ARCore not installed", e)
                }
                return false
            }

            Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE ->       // This device is not supported for AR.
                return false

            Availability.UNKNOWN_CHECKING, Availability.UNKNOWN_ERROR, Availability.UNKNOWN_TIMED_OUT -> {
                return false;
            }
        }
    }

    private fun createSession() {
        session = Session(this)
        val config = Config(session)

        session.configure(config)
    }
}