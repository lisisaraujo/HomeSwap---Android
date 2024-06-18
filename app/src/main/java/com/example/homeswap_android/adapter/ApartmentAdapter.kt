package com.example.homeswap_android.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.ApartmentsListItemBinding

class ApartmentAdapter(private var apartments: List<Apartment>,
private val itemClickedCallback: (Apartment) -> Unit
) :
RecyclerView.Adapter<ApartmentAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: ApartmentsListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ApartmentsListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val apartment = apartments[position]


        holder.binding.apartmentTitleTV.text = apartment.title
        holder.binding.apartmentcityTV.text = apartment.city
        holder.binding.apartmentReviewsTV.text = apartment.reviews.size.toString()
//        holder.binding.apartmentPicIV.load(apartment.pictures.first())

        holder.binding.apartmentListCV.setOnClickListener {
            Log.d("ClickedApartment", apartment.title)
            itemClickedCallback(apartment)
        }
    }

    override fun getItemCount(): Int {
        return apartments.size
    }

    fun updateApartments(newApartments: List<Apartment>) {
        apartments = newApartments
        notifyDataSetChanged()
    }
}