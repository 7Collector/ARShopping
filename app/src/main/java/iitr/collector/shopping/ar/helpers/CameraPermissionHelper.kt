package iitr.collector.shopping.ar.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraPermissionHelper {
    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001

        fun hasCameraPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }

        fun requestCameraPermission(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

        fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.CAMERA)
        }

        fun launchPermissionSettings(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
        }

        fun showPermissionRationaleDialog(activity: Activity) {
            AlertDialog.Builder(activity)
                .setTitle("Camera Permission Required")
                .setMessage("This app needs the camera to function properly. Please grant camera access.")
                .setPositiveButton("OK") { dialog, _ ->
                    requestCameraPermission(activity)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

    }
}