package com.example.actiontest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.actiontest.DATE_FORMAT
import com.example.actiontest.R
import com.example.actiontest.models.MediaStoreImage
import com.example.actiontest.utils.stringDateFormat

class GalleryAdapter() : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    private var mList: List<MediaStoreImage> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate single image view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_image_view, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Getting the specific item
        val item = mList[position]

        //Binding data to the views
        holder.dateTV.text = item.dateAdded.stringDateFormat(DATE_FORMAT)

        //Set the imageView with Glide
        Glide.with(holder.imageView)
            .load(item.contentUri)
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateItems(items: List<MediaStoreImage>?) {
        mList = items ?: emptyList()
        notifyDataSetChanged()
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.singleIV)
        val dateTV: TextView = itemView.findViewById(R.id.dateTV)
    }
}