package iitr.collector.shopping.ar

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import iitr.collector.shopping.ar.adapters.BagAdapter
import iitr.collector.shopping.ar.data.Product
import iitr.collector.shopping.ar.helpers.AddressHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class BagActivity<Activity> : AppCompatActivity(), PaymentResultWithDataListener {
    private lateinit var root: View
    private lateinit var ivBackOrders: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var checkout: TextView;
    private lateinit var empty: TextView;
    private lateinit var checkoutrl: RelativeLayout;
    private lateinit var rlAddress: RelativeLayout;
    private lateinit var total: TextView;
    private lateinit var tvAddress: TextView;
    private lateinit var ivEditAddress: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var addressHelper: AddressHelper
    private var uid = "test"
    private val db = Firebase.firestore
    private var amount = 0.0;
    private val auth = FirebaseAuth.getInstance()
    private val productsInBag = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bag)

        root = findViewById(R.id.main)
        ivBackOrders = findViewById(R.id.iv_back_orders)
        recyclerView = findViewById(R.id.recyclerView)
        checkout = findViewById(R.id.tv_checkout_bag)
        empty = findViewById(R.id.empty_bag_tv)
        checkoutrl = findViewById(R.id.relativeLayoutBag)
        total = findViewById(R.id.tv_bag_price)
        progressBar = findViewById(R.id.progressBar)
        rlAddress = findViewById(R.id.rl_address)
        tvAddress = findViewById(R.id.tv_address)
        ivEditAddress = findViewById(R.id.ibutton_edit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        addressHelper = AddressHelper(this)
        empty.visibility = View.GONE
        checkoutrl.visibility = View.GONE
        rlAddress.visibility = View.GONE
        ivBackOrders.setOnClickListener { finish() }
        checkout.setOnClickListener {
            if (addressHelper.isAddressSaved()){
                checkoutOrders()
            }else{
                Snackbar.make(root,"Please Provide with an address.", Snackbar.LENGTH_SHORT).show()
            }
        }
        Checkout.preload(applicationContext)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            uid = user.uid
            db.collection("users").document(uid).collection("bag").get()
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
                        val price = document.getDouble("price") ?: 0.0
                        amount+=price;
                        val discount = document.getDouble("discount") ?: 0.0
                        val mrp = document.getDouble("mrp") ?: 0.0
                        val description = document.getString("description") ?: ""
                        val stock = document.getLong("stock")?.toInt() ?: 0
                        val popularity = document.getLong("popularity")?.toInt() ?: 0
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
                            model
                        )
                        productsInBag += np
                    }
                    total.text = "₹ " + amount.toString()
                    if (productsInBag.size > 0) {
                        setNavigationBarColor(R.color.black)
                        checkoutrl.visibility = View.VISIBLE
                        rlAddress.visibility = View.VISIBLE
                        if (addressHelper.isAddressSaved()){
                            val address = addressHelper.getAddress()
                            tvAddress.text = address
                        } else {
                            tvAddress.setOnClickListener{
                                addressHelper.showAddressDialog {
                                    val address = addressHelper.getAddress()
                                    tvAddress.text = address
                                }
                            }
                        }
                    }
                    else{
                        empty.visibility = View.VISIBLE
                    }
                    recyclerView.adapter = BagAdapter(productsInBag)
                }.addOnFailureListener {
                    finish()
                }
        }
        ivEditAddress.setOnClickListener{
            addressHelper.showAddressDialog {
                val address = addressHelper.getAddress()
                tvAddress.text = address
            }
        }
    }

    private fun checkoutOrders() {
        lifecycleScope.launch {
            try {
                val orderId = createOrderOnServer()
                startPayment(orderId)
            } catch (e: Exception) {
                Toast.makeText(this@BagActivity, "Error creating order: ${e.message}", Toast.LENGTH_LONG).show()
                Log.d("Razorpay", "Error creating order: ${e.message}")
            }
        }
    }

    private fun setNavigationBarColor(colorResId: Int) {
        val window: Window = window
        window.navigationBarColor = resources.getColor(colorResId)
    }

    private suspend fun createOrderOnServer(): String {
        val response = createOrder()
        val jsonObject = JSONObject(response)
        return jsonObject.getString("id")
    }


    private fun startPayment(orderId: String) {
        val activity: BagActivity<Activity> = this
        val co = Checkout()
        co.setKeyID("rzp_test_mXoGZ32a0P54G0")

        try {
            val options = JSONObject()
            options.put("name", "Your Business Name")
            options.put("description", "Order Payment")
            options.put("currency", "INR")
            options.put("amount", (amount*100).toInt().toString())
            options.put("order_id", orderId)

            val prefill = JSONObject()
            prefill.put("email", auth.currentUser?.email ?: "test@example.com")
            prefill.put("contact", "12345678")
            options.put("prefill", prefill)

            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?, paymentData: PaymentData) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        handlePaymentSuccess(paymentData)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_SHORT).show()
        Log.d("Payment ", "Payment Failed: $response")
    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentId = paymentData.paymentId ?: return
        val orderId = paymentData.orderId ?: return
        val signature = paymentData.signature ?: return

        verifyPaymentSignature(paymentId, orderId, signature) { isValid ->
            if (isValid) {
                addToOrders()
            } else {
                Toast.makeText(this, "Payment verification failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyPaymentSignature(
        paymentId: String,
        orderId: String,
        signature: String,
        callback: (Boolean) -> Unit
    ) {

        callback(true)
    }

    private fun addToOrders() {
        val ordersCollection = db.collection("users").document(uid).collection("orders")
        val batch = db.batch()

        productsInBag.forEach { product ->
            val orderDoc = ordersCollection.document(product.id)
            val productData = product.toMap(getFutureDateInDateMonthFormat())
            batch.set(orderDoc, productData)

            val bagDoc = db.collection("users").document(uid).collection("bag").document(product.id)
            bagDoc.delete()
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Order placed successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order placement failed", Toast.LENGTH_SHORT).show()
            }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun createOrder(): String {
        val apiKey = "rzp_test_mXoGZ32a0P54G0"
        val apiSecret = "qzHpGGkUWEPnJ5zUuegdTk5y"
        val authHeaderValue = Base64.encodeToByteArray("$apiKey:$apiSecret".toByteArray()).toString(Charsets.UTF_8)

        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("amount", (amount*100).toInt().toString())
                .add("currency", "INR")
                .add("receipt", "receipt#1")
                .build()
            val baseUrl = "https://api.razorpay.com/v1/orders"
            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .header("Authorization", "Basic $authHeaderValue")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                response.body.string() ?: ""
            }
        }
    }
    fun getFutureDateInDateMonthFormat(): String {
        val daysToAdd = Random.nextInt(7, 16)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        val futureDate = calendar.time
        val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
        return dateFormat.format(futureDate)
    }
}