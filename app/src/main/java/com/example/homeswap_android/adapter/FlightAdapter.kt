package com.example.homeswap_android.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeswap_android.data.models.apiData.Dictionaries
import com.example.homeswap_android.data.models.apiData.FlightOffer
import com.example.homeswap_android.data.models.apiData.Itinerary
import com.example.homeswap_android.databinding.FlightListItemBinding


class FlightAdapter(
    private var flights: List<FlightOffer>,
    private var dictionaries: Dictionaries
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val flight = flights[position]
        val binding = holder.binding

        //outbound flight
        bindFlightDetails(binding, flight.itineraries[0], isOutbound = true)

        if (flight.itineraries.size > 1) {
            //return flight for round trip
            binding.returnFlightGroup.visibility = View.VISIBLE
            bindFlightDetails(binding, flight.itineraries[1], isOutbound = false)
        } else {
            //hide return flight views if it's a one way trip
            binding.returnFlightGroup.visibility = View.GONE
        }

        //total price
        val formattedPrice = String.format("%.2f", flight.price.total.toDouble())
        binding.totalPriceTV.text = "${flight.price.currency} $formattedPrice"
    }

    @SuppressLint("SetTextI18n")
    private fun bindFlightDetails(
        binding: FlightListItemBinding,
        itinerary: Itinerary,
        isOutbound: Boolean
    ) {
        val segments = itinerary.segments
        val firstSegment = segments.first()
        val lastSegment = segments.last()

        val airlineName = dictionaries.carriers[firstSegment.carrierCode] ?: "Unknown Airline"
        val departureIata = firstSegment.departure.iataCode
        val arrivalIata = lastSegment.arrival.iataCode
        val departureTime = firstSegment.departure.at.substring(11, 16)
        val arrivalTime = lastSegment.arrival.at.substring(11, 16)

        if (isOutbound) {
            binding.outboundAirlineTV.text = airlineName
            binding.outboundDepartureTV.text = "$departureIata $departureTime"
            binding.outboundArrivalTV.text = "$arrivalIata $arrivalTime"
            binding.outboundDurationTV.text = "Duration: ${itinerary.duration.substring(2)}"

            if (segments.size > 1) {
                val stops = segments.size - 1
                val stopsList = segments.dropLast(1).joinToString(", ") { it.arrival.iataCode }
                binding.outboundStopsTV.text = "$stops stop(s): $stopsList"
            } else {
                binding.outboundStopsTV.text = "Non-stop"
            }
        } else {
            binding.returnAirlineTV.text = airlineName
            binding.returnDepartureTV.text = "$departureIata $departureTime"
            binding.returnArrivalTV.text = "$arrivalIata $arrivalTime"
            binding.returnDurationTV.text = "Duration: ${itinerary.duration.substring(2)}"

            if (segments.size > 1) {
                val stops = segments.size - 1
                val stopsList = segments.dropLast(1).joinToString(", ") { it.arrival.iataCode }
                binding.returnStopsTV.text = "$stops stop(s): $stopsList"
            } else {
                binding.returnStopsTV.text = "Non-stop"
            }
        }


    }

    override fun getItemCount(): Int {
        return flights.size
    }

    fun updateFlights(newFlights: List<FlightOffer>, newDictionaries: Dictionaries) {
        flights = newFlights
        dictionaries = newDictionaries
        notifyDataSetChanged()
    }

}

