package iitr.collector.shopping.ar

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import iitr.collector.shopping.ar.adapters.OrdersAdapter
import iitr.collector.shopping.ar.data.Product

class ProfileActivity : AppCompatActivity() {
    private lateinit var ivBackOrders: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var empty:TextView
    private var uid = "test"
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ivBackOrders = findViewById(R.id.iv_back_orders)
        recyclerView = findViewById(R.id.recyclerViewOrders)
        empty = findViewById(R.id.empty_orders_tv)
        progressBar = findViewById(R.id.progressBarOrders)



        empty.visibility = View.GONE
        uid = auth.currentUser?.uid ?: "test"
        val orders = mutableListOf<Product>()
        val dates = mutableListOf<String>()
        db.collection("users").document(uid).collection("orders").get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE

                for (document in result) {

                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val category = document.getString("category") ?: ""
                    val rating = document.getDouble("rating") ?: 0.0
                    val image = document.getString("image") ?: ""
                    val images = document.getString("images") ?: ""
                    val cat = document.getString("cat") ?: ""
                    val model = document.getString("model") ?: ""
                    val type = document.getString("type") ?: "normal"
                    val price = document.getDouble("price") ?: 0.0
                    val discount = document.getDouble("discount") ?: 0.0
                    val mrp = document.getDouble("mrp") ?: 0.0
                    val description = document.getString("description") ?: ""
                    val stock = document.getLong("stock")?.toInt() ?: 0
                    val popularity = document.getLong("popularity")?.toInt() ?: 0
                    val arriving = document.getString("arriving") ?: "69 June"
                    val np = Product(
                        id,
                        name,
                        category,
                        rating,
                        image,
                        images,
                        price,
                        discount,
                        mrp,
                        description,
                        stock,
                        popularity,
                        cat,
                        model,
                        type
                    )
                    dates += arriving
                    orders += np
                }
                if (orders.size ==0)
                    empty.visibility = View.VISIBLE
                recyclerView.adapter = OrdersAdapter(orders,dates)
            }.addOnFailureListener {
                finish()
            }
        ivBackOrders.setOnClickListener { finish() }
    }
}