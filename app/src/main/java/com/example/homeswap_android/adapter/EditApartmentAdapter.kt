package com.example.homeswap_android.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.MyApartmentsListItemBinding
import com.example.homeswap_android.ui.personal.options.MyListingsFragmentDirections
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel

class EditApartmentAdapter(
    private var apartments: List<Apartment>,
    private val itemClickedCallback: (Apartment) -> Unit,
) :
    RecyclerView.Adapter<EditApartmentAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: MyApartmentsListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            MyApartmentsListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val apartment = apartments[position]
        Log.d("ApartmentID", apartment.apartmentID)


        holder.binding.apartmentTitleTV.text = apartment.title
        holder.binding.apartmentCityTV.text = apartment.city

        holder.binding.coverPictureIV.load(apartment.coverPicture)

        holder.binding.myApartmentsCV.setOnClickListener {
            Log.d("ClickedApartment", apartment.title)
            itemClickedCallback(apartment)
        }

        holder.binding.editButton.setOnClickListener {
            it.findNavController().navigate(
                MyListingsFragmentDirections.actionMyListingsFragmentToEditApartmentFragment(
                    apartment.apartmentID
                )
            )
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