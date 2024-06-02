package iitr.collector.shopping.ar

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import iitr.collector.shopping.ar.adapters.ProductAdapter
import iitr.collector.shopping.ar.data.Product

class SearchActivity : AppCompatActivity() {
    private lateinit var searchBar: RelativeLayout
    private lateinit var searchResultBar: RelativeLayout
    private lateinit var searchEditText: EditText
    private lateinit var searchTextView: TextView
    private lateinit var ivBack1: ImageView
    private lateinit var ivBack2: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        searchBar = findViewById(R.id.search_bar)
        searchResultBar = findViewById(R.id.search_results_bar)
        searchEditText = findViewById(R.id.search_edit_text)
        searchTextView = findViewById(R.id.search_result_textview)
        ivBack1 = findViewById(R.id.iv_back_search_bar)
        ivBack2 = findViewById(R.id.iv_back_search_results_bar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progress_bar_search)
        recyclerView.adapter = ProductAdapter(emptyList())
        if (intent.hasExtra("category")) {
            performCategorySearch(intent.getStringExtra("category") ?: "")
        } else {
            searchEditText.performClick()
            searchResultBar.visibility = View.GONE
            progressBar.visibility = View.GONE
            searchEditText.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {
                    performSearch()
                    true
                } else {
                    false
                }
            }
        }
        ivBack1.setOnClickListener { finish() }
        ivBack2.setOnClickListener { finish() }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim().lowercase()
        searchBar.visibility = View.GONE
        searchResultBar.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        searchTextView.text = query

        val db = Firebase.firestore
        val productsRef = db.collection("products")

        Log.d("SearchActivity", "Performing search with query: $query")

        productsRef.whereGreaterThanOrEqualTo("nameL", query)
            .whereLessThanOrEqualTo("nameL", query + '\uf8ff')
            .get()
            .addOnSuccessListener { results ->
                val data = mutableListOf<Product>()
                for (document in results) {
                    val name = document.getString("name") ?: ""
                    val id = document.getString("id") ?: ""
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
                    Log.d("SearchActivity", "Found document: $name")
                    val tp = Product(
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
                        model
                    )
                    data += tp

                }
                Log.d("SearchActivity", "Total documents found: ${data.size}")
                recyclerView.adapter = ProductAdapter(data)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("SearchActivity", "Error getting documents: ", e)
                progressBar.visibility = View.GONE
            }
    }

    private fun performCategorySearch(category: String) {

        searchBar.visibility = View.GONE
        searchResultBar.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        searchTextView.text = category

        val db = Firebase.firestore
        val productsRef = db.collection("products")
        productsRef.whereEqualTo("cat", category)
            .get()
            .addOnSuccessListener { results ->
                val data = mutableListOf<Product>()
                for (document in results) {
                    val name = document.getString("name") ?: ""
                    val id = document.getString("id") ?: ""
                    val ccategory = document.getString("category") ?: ""
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
                    Log.d("SearchActivity", "Found document: $name")
                    val tp = Product(
                        id,
                        name,
                        ccategory,
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
                        model
                    )
                    data += tp
                }
                recyclerView.adapter = ProductAdapter(data)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
            }
    }
}