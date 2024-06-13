package com.example.homeswap_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeswap_android.data.models.apiData.FlightOffer
import com.example.homeswap_android.databinding.FlightListItemBinding


class FlightAdapter(
    private var flights: List<FlightOffer>,
) :
    RecyclerView.Adapter<FlightAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: FlightListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            FlightListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val flight = flights[position]

        holder.binding.flightOriginTV.text = flight.itineraries.first().segments.first().departure.iataCode

    }
    override fun getItemCount(): Int {
        return flights.size
    }

}
