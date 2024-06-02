package iitr.collector.shopping.ar.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import iitr.collector.shopping.ar.data.Category
import iitr.collector.shopping.ar.R
import iitr.collector.shopping.ar.SearchActivity

class CategoriesAdapter(private val data: List<Category>) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = data[position]
        val imageUrl = category.image
        val categoryName = category.name
        holder.textView.text = categoryName
        Glide.with(holder.itemView).load(imageUrl).circleCrop().into(holder.imageView)
        holder.itemView.setOnClickListener {
            val i = Intent(holder.itemView.context, SearchActivity::class.java)
            i.putExtra("category", categoryName)
            holder.itemView.context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView : TextView = itemView.findViewById(R.id.tv_category_name)
        val imageView : ImageView = itemView.findViewById(R.id.iv_category)
    }
}
