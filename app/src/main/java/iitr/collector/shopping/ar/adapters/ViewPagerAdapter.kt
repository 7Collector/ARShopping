package iitr.collector.shopping.ar.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import iitr.collector.shopping.ar.R
import org.json.JSONArray

class ViewPagerAdapter(private val list: JSONArray): RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.ViewHolder, position: Int) {
        Glide.with(holder.itemView).load(list.getJSONObject(position).getString("image")).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return list.length()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.vp_image_view)
    }
}