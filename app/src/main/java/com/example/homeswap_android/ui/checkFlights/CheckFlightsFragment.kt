package com.example.homeswap_android.ui.checkFlights

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.FlightAdapter
import com.example.homeswap_android.data.models.apiData.FlightResponse
import com.example.homeswap_android.databinding.FragmentCheckFlightsBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.FlightsViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import java.util.Date

class CheckFlightsFragment : Fragment() {
    private val TAG = "CheckFlightsFragment"
    private lateinit var binding: FragmentCheckFlightsBinding
    private val flightViewModel: FlightsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null
    private var userOrigin: String? = null
    private var origin: String? = null
    private var destination: String? = null
    private var isOneWayTrip = false
    private var hasBundleData = false

    private data class BundleArgs(
        var destination: String? = null,
        var departureDate: String? = null,
        var returnDate: String? = null
    )
    private val bundleArgs = BundleArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCheckFlightsBinding.inflate(inflater, container, false)
        setupTripTypeToggle()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBundleArgs()
        observeViewModels()
        setupPlacesAutocomplete()
        setupDatePicker()
        setupSearchButton()
    }

    private fun setupTripTypeToggle() {
        binding.toggleTripType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isOneWayTrip = checkedId == R.id.btnOneWay
                updateUIForTripType()
            }
        }
    }

    private fun setupPlacesAutocomplete() {
        placesClient = Places.createClient(requireContext())
        setupAutoCompleteTextView(binding.etOrigin) { selectedPlace ->
            origin = selectedPlace.split(",").firstOrNull()?.trim()
            binding.etOrigin.setText(selectedPlace)
        }
        setupAutoCompleteTextView(binding.etDestination) { selectedPlace ->
            destination = selectedPlace.split(",").firstOrNull()?.trim()
            binding.etDestination.setText(selectedPlace)
        }
    }

    private fun setupAutoCompleteTextView(editText: AutoCompleteTextView, onPlaceSelected: (String) -> Unit) {
        Utils.setupAutoCompleteTextView(requireContext(), editText, placesClient, onPlaceSelected)
    }

    private fun setupDatePicker() {
        binding.etDateRange.setOnClickListener {
            if (isOneWayTrip) {
                showSingleDatePicker()
            } else {
                showDateRangePicker()
            }
        }
    }

    private fun showSingleDatePicker() {
        Utils.showDatePicker(parentFragmentManager) { date ->
            selectedStartDate = Utils.dateFormat.parse(date)
            selectedEndDate = null
            updateDateDisplay()
        }
    }

    private fun showDateRangePicker() {
        Utils.showDateRangePicker(parentFragmentManager) { start, end ->
            selectedStartDate = Utils.dateFormat.parse(start)
            selectedEndDate = Utils.dateFormat.parse(end)
            updateDateDisplay()
        }
    }

    private fun setupSearchButton() {
        binding.btnSearchFlights.setOnClickListener { performSearch() }
    }

    private fun observeViewModels() {
        observeUserData()
        observeFlightResponse()
        observeLoadingState()
        observeErrorMessages()
    }

    private fun observeUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.loggedInUserData.collect { user ->
                userOrigin = user?.location?.split(",")?.firstOrNull()?.trim()
                if (hasBundleData) {
                    origin = userOrigin
                    prefillFromApartmentSearch()
                    performSearchWithBundle()
                }
            }
        }
    }

    private fun observeFlightResponse() {
        flightViewModel.flightResponse.observe(viewLifecycleOwner) { response ->
            if (response != null && response.data.isNotEmpty()) {
                updateFlightList(response)
            } else {
                showNoResults()
            }
        }
    }

    private fun updateFlightList(response: FlightResponse) {
        binding.rvFlightsList.visibility = View.VISIBLE
        updateLoadingState(false)
        val adapter = binding.rvFlightsList.adapter as? FlightAdapter
        if (adapter == null) {
            binding.rvFlightsList.adapter = FlightAdapter(response.data, response.dictionaries)
        } else {
            adapter.updateFlights(response.data, response.dictionaries)
        }
    }

    private fun showNoResults() {
        binding.rvFlightsList.visibility = View.GONE
        binding.noResultsTV.visibility = View.VISIBLE
    }

    private fun observeLoadingState() {
        flightViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
        }
    }

    private fun observeErrorMessages() {
        flightViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
        }
    }

    private fun handleBundleArgs() {
        arguments?.let { bundle ->
            bundleArgs.destination = bundle.getString("destination")
            bundleArgs.departureDate = bundle.getString("departureDate")
            bundleArgs.returnDate = bundle.getString("returnDate")

            if (!bundleArgs.destination.isNullOrEmpty() &&
                !bundleArgs.departureDate.isNullOrEmpty() &&
                !bundleArgs.returnDate.isNullOrEmpty()) {
                hasBundleData = true
                performSearchWithBundle()
            }
        }
    }

    private fun performSearchWithBundle() {
        destination = bundleArgs.destination
        selectedStartDate = bundleArgs.departureDate?.let { Utils.dateFormat.parse(it) }
        selectedEndDate = bundleArgs.returnDate?.let { Utils.dateFormat.parse(it) }
        origin = userOrigin

        binding.etDestination.setText(destination)
        binding.etDateRange.setText(getString(R.string.date_range, bundleArgs.departureDate, bundleArgs.returnDate))

        isOneWayTrip = false
        binding.toggleTripType.check(R.id.btnRoundTrip)
        updateUIForTripType()
        flightViewModel.clearSearch()
        performSearch()
    }

    private fun updateUIForTripType() {
        if (isOneWayTrip) {
            binding.toggleTripType.check(R.id.btnOneWay)
            binding.etDateRange.hint = getString(R.string.departure_date)
            selectedEndDate = null
        } else {
            binding.toggleTripType.check(R.id.btnRoundTrip)
            binding.etDateRange.hint = getString(R.string.select_dates)
        }
        updateDateDisplay()
    }

    private fun updateDateDisplay() {
        val formattedStartDate = selectedStartDate?.let { Utils.dateFormat.format(it) }
        val formattedEndDate = selectedEndDate?.let { Utils.dateFormat.format(it) }

        if (isOneWayTrip) {
            binding.etDateRange.setText(formattedStartDate)
        } else {
            binding.etDateRange.setText(getString(R.string.date_range, formattedStartDate, formattedEndDate))
        }
    }

    private fun performSearch() {
        if (origin.isNullOrEmpty() || destination.isNullOrEmpty() || selectedStartDate == null) {
            showError("Please fill in all the fields.")
            return
        }

        flightViewModel.clearSearch()

        if (isOneWayTrip) {
            flightViewModel.searchOneWayFlights(origin!!, destination!!, selectedStartDate!!)
        } else {
            if (selectedEndDate == null) {
                showError("Please select a return date for round-trip.")
                return
            }
            flightViewModel.searchRoundTripFlights(origin!!, destination!!, selectedStartDate!!, selectedEndDate!!)
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        val loadingOverlay = view?.findViewById<ConstraintLayout>(R.id.loading_overlay)
        if (isLoading) Utils.showLoadingOverlay(loadingOverlay!!) else Utils.hideLoadingOverlay(loadingOverlay!!)
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun prefillFromApartmentSearch() {
        binding.etOrigin.setText(userOrigin)
    }
}
