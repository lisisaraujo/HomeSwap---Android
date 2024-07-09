package com.example.homeswap_android.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeswap_android.data.models.apiData.Dictionaries
import com.example.homeswap_android.data.models.apiData.FlightOffer
import com.example.homeswap_android.data.models.apiData.FlightResponse
import com.example.homeswap_android.data.models.apiData.Price
import com.example.homeswap_android.data.repositories.FlightsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlightsViewModel : ViewModel() {
    private val repository = FlightsRepository()

    val flights: LiveData<List<FlightOffer>> = repository.flights
    val isLoading: LiveData<Boolean> = repository.isLoading
    val errorMessage: LiveData<String?> = repository.errorMessage
    val flightResponse: LiveData<FlightResponse> = repository.flightResponse
    val clearSearch = repository.clearSearch



    fun searchRoundTripFlights(originCity: String, destinationCity: String, departureDate: Date, returnDate: Date, adults: Int = 1) {
        viewModelScope.launch {
            repository.searchRoundTripFlights(originCity, destinationCity, departureDate, returnDate, adults)
        }
    }

    fun clearFlightsSearch() {
        repository.clearFlightsSearch()
    }
}
