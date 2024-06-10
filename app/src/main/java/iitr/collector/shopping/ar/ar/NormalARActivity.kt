package iitr.collector.shopping.ar.ar

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

class NormalARActivity : AppCompatActivity(R.layout.activity_normal_aractivity) {
    lateinit var sceneView: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView
    private lateinit var tvName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvAddToBag: TextView
    private lateinit var ivIncreaseSize: ImageView
    private lateinit var ivDecreaseSize: ImageView
    private lateinit var ivRotateLeft: ImageView
    private lateinit var ivRotateRight: ImageView
    private lateinit var modelNode:ModelNode

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


    var trackingFailureReason: TrackingFailureReason? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    fun updateInstructions() {
        instructionText.text = trackingFailureReason?.getDescription(this) ?: if (anchorNode == null) {
            getString(R.string.point_your_phone_down)
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.apply {
            navigationBarColor = Color.parseColor("#F3F3F3")
            statusBarColor = Color.TRANSPARENT
            decorView.visibility = View.GONE
        }
        enableEdgeToEdge()
        tvName = findViewById(R.id.tv_product_name)
        tvPrice = findViewById(R.id.tv_product_price)
        tvAddToBag = findViewById(R.id.tv_add_bag)
        tvName.text = intent.getStringExtra("name")
        tvPrice.text = intent.getStringExtra("price")
        instructionText = findViewById(R.id.instructionText)
        loadingView = findViewById(R.id.loadingView)
        ivIncreaseSize = findViewById(R.id.iv_increase_size)
        ivDecreaseSize = findViewById(R.id.iv_decrease_size)
        ivRotateLeft = findViewById(R.id.iv_rotate_left)
        ivRotateRight = findViewById(R.id.iv_rotate_right)

        ivIncreaseSize.setOnClickListener { modifyModelSize(1.1f) }
        ivDecreaseSize.setOnClickListener { modifyModelSize(0.9f) }
        ivRotateLeft.setOnClickListener { rotateModel(-15.0f) }
        ivRotateRight.setOnClickListener { rotateModel(15.0f) }

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
        tvAddToBag.setOnClickListener {
            Toast.makeText(this,"Add to bag here", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun addAnchorNode(anchor: Anchor) {
        sceneView.addChildNode(
            AnchorNode(sceneView.engine, anchor)
                .apply {
                    isEditable = true
                    lifecycleScope.launch {
                        isLoading = true
                        buildModelNode()?.let {
                            addChildNode(it)
                            modelNode=it
                        }
                        isLoading = false
                    }
                    anchorNode = this
                }
        )
    }

    suspend fun buildModelNode(): ModelNode? {
        val a = intent.getStringExtra("model")
        if (a != null) {
            Log.d("Model",a)
        };
        if (a != null) {
            isLoading = true
            sceneView.modelLoader.loadModelInstance(
                a
            )?.let { modelInstance ->
                isLoading=false
                return ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 0.5f,
                    centerOrigin = Position(y = -0.5f)
                ).apply {
                    isEditable = true
                }

            }
        }
        return null
    }
    private fun modifyModelSize(factor: Float) {
        modelNode?.let { node ->
            node.scale *= factor
        }
    }

    private fun rotateModel(angle: Float) {
        modelNode?.let { node ->
            val currentRotation = node.rotation
            node.rotation = Rotation(
                x = currentRotation.x,
                y = currentRotation.y + angle,
                z = currentRotation.z
            )
        }
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