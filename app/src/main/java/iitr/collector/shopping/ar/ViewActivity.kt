package iitr.collector.shopping.ar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.ArCoreApk
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import iitr.collector.shopping.ar.adapters.ViewPagerAdapter
import iitr.collector.shopping.ar.ar.NormalARActivity
import iitr.collector.shopping.ar.ar.PoseLandmarkActivity
import org.json.JSONArray

class ViewActivity : AppCompatActivity() {
    private lateinit var detailsContainer: RelativeLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var tvName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvAddToBag: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var ivBack: ImageView
    private lateinit var mArButton: ImageView
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setNavigationBarColor(R.color.black)

        viewPager2 = findViewById(R.id.vp2)
        detailsContainer = findViewById(R.id.rl_all_details)
        tvDescription = findViewById(R.id.tv_product_description)
        tvName = findViewById(R.id.tv_product_name)
        tvPrice = findViewById(R.id.tv_product_price)
        tvAddToBag = findViewById(R.id.tv_add_bag)
        ratingBar = findViewById(R.id.ratingBar)
        ivBack = findViewById(R.id.ibutton_back)
        mArButton = findViewById(R.id.ibutton_AR)
        relativeLayout = findViewById(R.id.relativeLayout)
        loadData()
        maybeEnableArButton()

        val bottomSheetBehavior = BottomSheetBehavior.from(detailsContainer)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        relativeLayout.visibility = View.VISIBLE
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        relativeLayout.visibility = View.GONE
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        relativeLayout.visibility = View.GONE
                    }
                }
            }

            override fun onSlide(p0: View, p1: Float) {

            }


        })
        ivBack.setOnClickListener { finish() }
        tvAddToBag.setOnClickListener {
            addToBag()
        }
        mArButton.setOnClickListener { ar(intent.getStringExtra("name")?:"Product Name",intent.getStringExtra("model")?:"",intent.getStringExtra("type") ?: "normal") }
    }

    private fun setNavigationBarColor(colorResId: Int) {
        val window: Window = window
        window.navigationBarColor = resources.getColor(colorResId)
    }

    private fun loadData() {
        val images = JSONArray(intent.getStringExtra("images"))
        viewPager2.adapter = ViewPagerAdapter(images)
        Log.d("images", intent.getStringExtra("images").toString())
        tvDescription.text = intent.getStringExtra("description")
        tvName.text = intent.getStringExtra("name")
        tvPrice.text = "₹ " + intent.getDoubleExtra("price", 99.0).toString()
        ratingBar.rating = intent.getDoubleExtra("rating", 4.4).toFloat()
    }

    private fun addToBag() {
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val bagRef = db.collection("users").document(uid).collection("bag")

            val product = hashMapOf(
                "id" to intent.getStringExtra("id"),
                "name" to tvName.text.toString(),
                "category" to intent.getStringExtra("category"),
                "rating" to intent.getDoubleExtra("rating", 4.4),
                "image" to intent.getStringExtra("image"),
                "images" to intent.getStringExtra("images"),
                "price" to intent.getDoubleExtra("price", 99.0),
                "discount" to intent.getDoubleExtra("discount", 0.0),
                "mrp" to intent.getDoubleExtra("mrp", 0.0),
                "description" to tvDescription.text.toString(),
                "stock" to intent.getIntExtra("stock", 0),
                "popularity" to intent.getIntExtra("popularity", 0),
                "ordered" to false
            )

            bagRef.add(product)
                .addOnSuccessListener {
                    Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show()
                    Log.d("addToBag", "Product added to bag successfully")
                    val documentId = it.id
                    bagRef.document(documentId).update("id", documentId)
                }
                .addOnFailureListener { e ->
                    Log.w("addToBag", "Error adding product to bag", e)
                }
        } else {
            Log.w("addToBag", "User not logged in")
        }
    }

    private fun maybeEnableArButton() {
        ArCoreApk.getInstance().checkAvailabilityAsync(this) { availability ->
            if (availability.isSupported) {
                mArButton.visibility = View.VISIBLE
                mArButton.isEnabled = true
            } else {
                mArButton.visibility = View.INVISIBLE
                mArButton.isEnabled = false
            }
        }
    }

    private fun ar(title: String, file: String, type: String) {
        if (type == "normal" && file.isNotEmpty()) {
            val i = Intent(this,NormalARActivity::class.java)
            i.putExtra("model",file)
            startActivity(i)
            /*val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
            val intentUri: Uri = Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
                .appendQueryParameter("file", file)
                .appendQueryParameter("mode", "ar_only")
                .appendQueryParameter("title", title)
                .build()
            sceneViewerIntent.data = intentUri
            sceneViewerIntent.setPackage("com.google.ar.core")
            startActivity(sceneViewerIntent)*/
        } else {
            val i = Intent(this, PoseLandmarkActivity::class.java)
            startActivity(i)
            //Snackbar.make(relativeLayout, "AR is not available for this product.", Snackbar.LENGTH_SHORT).show()
        }
    }

}