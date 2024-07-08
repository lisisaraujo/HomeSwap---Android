package com.example.homeswap_android.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.apiData.FlightOffer
import com.example.homeswap_android.data.models.apiData.FlightResponse
import com.example.homeswap_android.data.remote.FlightsApi

class FlightsRepository {
    private val TAG = "FlightsRepository"

    private val _flights = MutableLiveData<List<FlightOffer>>()
    val flights: LiveData<List<FlightOffer>> = _flights

    private val apiService = FlightsApi.flightsApiService

    suspend fun loadFlights(
        origin: String,
        destination: String,
        departureDate: String,
        adults: Int = 1
    ): List<FlightOffer> {
        return try {
            val flightsResponse = apiService.getFlights(
                origin = origin,
                destination = destination,
                departureDate = departureDate,
                adults = adults
            )
            Log.d(TAG, "Flights loaded successfully: $flightsResponse")
            _flights.postValue(flightsResponse.data)
            flightsResponse.data
        } catch (e: Exception) {
            Log.e(TAG, "Error loading flights", e)
            throw e
        }
    }

    suspend fun searchFlightsByCity(originCity: String, destinationCity: String, departureDate: String, adults: Int = 1): FlightResponse {
        val originIata = getIataCode(originCity)
        val destinationIata = getIataCode(destinationCity)

        return apiService.getFlights(originIata, destinationIata, departureDate, adults)
    }

    private suspend fun getIataCode(cityName: String): String {
        val response = apiService.searchAirports(keyword = cityName)
        return response.data.firstOrNull()?.iataCode
            ?: throw IllegalArgumentException("No IATA code found for $cityName")
    }
}
