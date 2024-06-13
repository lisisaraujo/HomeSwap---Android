package com.example.homeswap_android.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeswap_android.data.Repository
import com.example.homeswap_android.data.models.apiData.FlightOffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val TAG = "FlightsViewModel"

class FlightsViewModel : ViewModel() {

    private val repository = Repository()

    private val _flights = MutableLiveData<List<FlightOffer>>()
    val flights: LiveData<List<FlightOffer>>
        get() = _flights

    private val _origin = MutableLiveData<String>("BER")
    val origin: LiveData<String>
        get() = _origin

    private val _destination = MutableLiveData<String>("LON")
    val destination: LiveData<String>
        get() = _destination

    private val _departureDate = MutableLiveData<String>("2024-11-01")
    val departureDate: LiveData<String>
        get() = _departureDate

    fun loadFlights() {
        val currentOrigin = origin.value!!
        val currentDestination = destination.value!!
        val currentDepartureDate = departureDate.value!!
        if (validateIATACode(currentOrigin) && validateIATACode(currentDestination)) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val flightsList = repository.loadData(currentOrigin, currentDestination, currentDepartureDate)
                    _flights.postValue(flightsList)
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading flights", e)
                }
            }
        } else {
            Log.e(TAG, "Invalid IATA code: $currentOrigin or $currentDestination")
        }
    }

    private fun validateIATACode(code: String): Boolean {
        return code.matches(Regex("^[A-Z]{3}$"))
    }
}
