package com.example.homeswap_android.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeswap_android.data.models.apiData.FlightOffer
import com.example.homeswap_android.data.models.apiData.FlightResponse
import com.example.homeswap_android.data.repositories.FlightsRepository
import kotlinx.coroutines.launch
import java.util.Date

class FlightsViewModel : ViewModel() {

    val TAG = "FlightsViewModel"
    private val repository = FlightsRepository()

    val flights: LiveData<List<FlightOffer>> = repository.flights
    val isLoading: LiveData<Boolean> = repository.isLoading
    val flightResponse: LiveData<FlightResponse> = repository.flightResponse
    val clearSearch = repository.clearSearch


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun clearSearch() {
        repository.clearFlightsSearch()
    }


    fun searchOneWayFlights(originCity: String, destinationCity: String, departureDate: Date, adults: Int = 1) {
        viewModelScope.launch {
            try {
                repository.searchOneWayFlights(originCity, destinationCity, departureDate, adults)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search one-way flights: ${e.localizedMessage}"
            }
        }
    }

    fun searchRoundTripFlights(originCity: String, destinationCity: String, departureDate: Date, returnDate: Date, adults: Int = 1) {
        viewModelScope.launch {
            try {
                repository.searchRoundTripFlights(originCity, destinationCity, departureDate, returnDate, adults)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search round-trip flights: ${e.localizedMessage}"
            }
        }
    }

}
