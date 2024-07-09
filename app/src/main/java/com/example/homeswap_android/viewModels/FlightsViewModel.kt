package com.example.homeswap_android.viewModels

import androidx.lifecycle.LiveData
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
    val errorMessage: LiveData<String?> = repository.errorMessage
    val flightResponse: LiveData<FlightResponse> = repository.flightResponse
    val clearSearch = repository.clearSearch


    fun searchRoundTripFlights(
        originCity: String,
        destinationCity: String,
        departureDate: Date,
        returnDate: Date,
        adults: Int = 1
    ) {
        viewModelScope.launch {
            repository.searchRoundTripFlights(
                originCity,
                destinationCity,
                departureDate,
                returnDate,
                adults
            )
        }
    }

    fun clearFlightsSearch() {
        repository.clearFlightsSearch()
    }

}
