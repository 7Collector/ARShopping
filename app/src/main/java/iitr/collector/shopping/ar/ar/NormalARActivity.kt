package iitr.collector.shopping.ar.ar

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import iitr.collector.shopping.ar.R
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

class NormalARActivity : AppCompatActivity(R.layout.activity_normal_aractivity) {
    lateinit var sceneView: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    var anchorNode: AnchorNode? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    var anchorNodeView: View? = null

    var trackingFailureReason: TrackingFailureReason? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    fun updateInstructions() {
        instructionText.text = trackingFailureReason?.let {
            it.getDescription(this)
        } ?: if (anchorNode == null) {
            getString(R.string.point_your_phone_down)
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.apply {
            decorView.visibility = View.GONE
            statusBarColor = Color.TRANSPARENT
        }
        enableEdgeToEdge()
        instructionText = findViewById(R.id.instructionText)
        loadingView = findViewById(R.id.loadingView)
        sceneView = findViewById<ARSceneView?>(R.id.sceneView).apply {
            lifecycle = this@NormalARActivity.lifecycle
            planeRenderer.isEnabled = true
            configureSession { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
            onSessionUpdated = { _, frame ->
                if (anchorNode == null) {
                    frame.getUpdatedPlanes()
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        ?.let { plane ->
                            addAnchorNode(plane.createAnchor(plane.centerPose))
                        }
                }
            }
            onTrackingFailureChanged = { reason ->
                this@NormalARActivity.trackingFailureReason = reason
            }
        }
//        sceneView.viewNodeWindowManager = ViewAttachmentManager(context, this).apply { onResume() }
    }

    fun addAnchorNode(anchor: Anchor) {
        sceneView.addChildNode(
            AnchorNode(sceneView.engine, anchor)
                .apply {
                    isEditable = true
                    lifecycleScope.launch {
                        isLoading = true
                        buildModelNode()?.let { addChildNode(it) }
//                        buildViewNode()?.let { addChildNode(it) }
                        isLoading = false
                    }
                    anchorNode = this
                }
        )
    }

    suspend fun buildModelNode(): ModelNode? {
        val a = intent.getStringExtra("model")
        if (a != null) {
            sceneView.modelLoader.loadModelInstance(
                a
            )?.let { modelInstance ->
                return ModelNode(
                    modelInstance = modelInstance,
                    // Scale to fit in a 0.5 meters cube
                    scaleToUnits = 0.5f,
                    // Bottom origin instead of center so the model base is on floor
                    centerOrigin = Position(y = -0.5f)
                ).apply {
                    isEditable = true
                }
            }
        }
        return null
    }

//    suspend fun buildViewNode(): ViewNode? {
//        return withContext(Dispatchers.Main) {
//            val engine = sceneView.engine
//            val materialLoader = sceneView.materialLoader
//            val windowManager = sceneView.viewNodeWindowManager ?: return@withContext null
//            val view = LayoutInflater.from(materialLoader.context).inflate(R.layout.view_node_label, null, false)
//            val ViewAttachmentManager(context, this).apply { onResume() }
//            val viewNode = ViewNode(engine, windowManager, materialLoader, view, true, true)
//            viewNode.position = Position(0f, -0.2f, 0f)
//            anchorNodeView = view
//            viewNode
//        }
//    }

}