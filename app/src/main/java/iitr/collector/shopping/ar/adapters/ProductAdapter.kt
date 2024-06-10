package iitr.collector.shopping.ar.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import iitr.collector.shopping.ar.R
import iitr.collector.shopping.ar.ViewActivity
import iitr.collector.shopping.ar.data.Product

class ProductAdapter(private val data: List<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = data[position]
        val imageUrl = product.image
        holder.name.text = product.name
        holder.price.text = "₹"+product.price.toString()
        Glide.with(holder.itemView).load(imageUrl).into(holder.mainImage)
        holder.itemView.setOnClickListener {
            val intents = Intent(holder.itemView.context, ViewActivity::class.java)
            intents.putExtra("id", product.id)
            intents.putExtra("name", product.name)
            intents.putExtra("category", product.category)
            intents.putExtra("rating", product.rating)
            intents.putExtra("image", product.image)
            intents.putExtra("images", product.images)
            intents.putExtra("price", product.price)
            intents.putExtra("model", product.model)
            intents.putExtra("type", product.type)
            intents.putExtra("discount", product.discount)
            intents.putExtra("mrp", product.mrp)
            intents.putExtra("description", product.description)
            intents.putExtra("stock", product.stock)
            intents.putExtra("popularity", product.popularity)
            holder.itemView.context.startActivity(intents)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainImage : ImageView = itemView.findViewById(R.id.product_image)
        val name : TextView = itemView.findViewById(R.id.product_name)
        val price : TextView = itemView.findViewById(R.id.product_price)
        val wishlistIV : ImageView = itemView.findViewById(R.id.ibutton_wishlist)
    }
}
