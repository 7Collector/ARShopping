package iitr.collector.shopping.ar

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.ar.core.ArCoreApk
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import iitr.collector.shopping.ar.data.Category
import iitr.collector.shopping.ar.data.Product
import iitr.collector.shopping.ar.adapters.CategoriesAdapter
import iitr.collector.shopping.ar.adapters.ProductAdapter
import org.json.JSONArray

class HomeActivity : AppCompatActivity() {

    private val dataCategories = mutableListOf<Category>()
    private val dataTop = mutableListOf<Product>()
    private val dataNew = mutableListOf<Product>()
    private var category = "Home"

    private lateinit var rvNew : RecyclerView
    private lateinit var rvCategories : RecyclerView
    private lateinit var rvTop : RecyclerView
    private lateinit var spinner : Spinner
    private lateinit var searchBar : View
    private lateinit var bagImage: ImageView
    private lateinit var profileImage:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        rvNew = findViewById(R.id.rv_new)
        rvCategories = findViewById(R.id.rv_categories)
        rvTop = findViewById(R.id.rv_top)
        spinner = findViewById(R.id.cat_spinner)
        searchBar = findViewById(R.id.search_bar)
        bagImage = findViewById(R.id.ibutton_bag)
        profileImage = findViewById(R.id.ibutton_profile)

        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rvTop.layoutManager = staggeredGridLayoutManager
        rvNew.adapter = ProductAdapter(emptyList())
        rvTop.adapter = ProductAdapter(emptyList())
        rvCategories.adapter = CategoriesAdapter(emptyList())
        val items = arrayOf("Men", "Women", "Home")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(2)
        loadData()
        ArCoreApk.getInstance().checkAvailability(this)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                category = parent?.getItemAtPosition(position).toString()
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        searchBar.setOnClickListener {
            val ii = Intent(this, SearchActivity::class.java)
            startActivity(ii)
        }
        bagImage.setOnClickListener {
            val i = Intent(this, BagActivity::class.java)
            startActivity(i)
        }
        profileImage.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            startActivity(i)
        }
    }

    private fun loadData() {
        val db = Firebase.firestore
        db.collection(category).get()
            .addOnSuccessListener { result ->
            dataCategories.clear()
            for (document in result) {
                val image = document.getString("image")?:""
                val name = document.getString("name")?:""
                val cat = Category(image,name)
                dataCategories += cat
            }
            rvCategories.adapter = CategoriesAdapter(dataCategories)
        }
        db.collection("products").whereEqualTo("category", category).orderBy("timestamp", Query.Direction.DESCENDING).limit(20).get()
            .addOnSuccessListener { result ->
            dataNew.clear()
                Log.d("Data New", result.toString())
            for (document in result) {
                val id = document.getString("id") ?: ""
                val name = document.getString("name") ?: ""
                val category = document.getString("category") ?: ""
                val rating = document.getDouble("rating") ?: 0.0
                val image = document.getString("image") ?: ""
                val images = document.getString("images") ?: ""
                val cat = document.getString("cat") ?: ""
                val model = document.getString("model") ?: ""
                val price = document.getDouble("price") ?: 0.0
                val discount = document.getDouble("discount") ?: 0.0
                val mrp = document.getDouble("mrp") ?: 0.0
                val description = document.getString("description") ?: ""
                val stock = document.getLong("stock")?.toInt() ?: 0
                val popularity = document.getLong("popularity")?.toInt() ?: 0

                val np = Product(id, name, category, rating, image, images, price, discount, mrp, description, stock, popularity, cat, model)

                dataNew += np
            }
            rvNew.adapter = ProductAdapter(dataNew)
        }
        db.collection("products").whereEqualTo("category", category).orderBy("popularity", Query.Direction.DESCENDING).limit(10).get()
            .addOnSuccessListener { result ->
            dataTop.clear()
                Log.d("Data Top", result.toString())
            for (document in result) {
                val id = document.getString("id") ?: ""
                val name = document.getString("name") ?: ""
                val category = document.getString("category") ?: ""
                val rating = document.getDouble("rating") ?: 0.0
                val image = document.getString("image") ?: ""
                val cat = document.getString("cat") ?: ""
                val model = document.getString("model") ?: ""
                val images = document.getString("images") ?: ""
                val price = document.getDouble("price") ?: 0.0
                val discount = document.getDouble("discount") ?: 0.0
                val mrp = document.getDouble("mrp") ?: 0.0
                val description = document.getString("description") ?: ""
                val stock = document.getLong("stock")?.toInt() ?: 0
                val popularity = document.getLong("popularity")?.toInt() ?: 0

                val tp = Product(id, name, category, rating, image, images, price, discount, mrp, description, stock, popularity, cat, model)

                dataTop += tp
            }
            rvTop.adapter = ProductAdapter(dataTop)
        }
    }
}