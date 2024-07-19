package com.example.homeswap_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.PhotoItemBinding

class PhotoAdapter(
    private var photos: List<String>,
    private val itemClickedCallback: (Int) -> Unit,
) : RecyclerView.Adapter<PhotoAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: PhotoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            PhotoItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val photo = photos[position]
        holder.binding.photoIV.load(photo) {
            placeholder(R.drawable.ic_launcher_foreground)
            error(com.google.android.material.R.drawable.mtrl_ic_error)
        }

        holder.binding.photoIV.setOnClickListener {
            itemClickedCallback(position)
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    fun updatePhotos(newPhotos: List<String>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}
