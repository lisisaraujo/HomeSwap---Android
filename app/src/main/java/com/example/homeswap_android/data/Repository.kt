package com.example.homeswap_android.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.apiData.FlightOffer
import com.example.homeswap_android.data.models.apiData.FlightResponse
import com.example.homeswap_android.data.remote.FlightsApi

const val TAG = "Repository"

class Repository {

    private val _flights = MutableLiveData<List<FlightOffer>>()
    val flights: LiveData<List<FlightOffer>>
        get() = _flights

    suspend fun loadData(
        origin: String,
        destination: String,
        departureDate: String
    ): List<FlightOffer> {
        try {
            val flightsListResponse = FlightsApi.flightsApiService.getFlights(
                origin = origin,
                destination = destination,
                departureDate = departureDate
            )
            Log.d(TAG, "Flights loaded successfully: $flightsListResponse")
            return flightsListResponse.data
        } catch (e: Exception) {
            Log.e(TAG, "Error loading flights", e)
            throw e
        }
    }
}