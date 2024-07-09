package com.example.homeswap_android.ui.checkFlights

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.FlightAdapter
import com.example.homeswap_android.databinding.FragmentCheckFlightsBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.FlightsViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckFlightsFragment : Fragment() {
    private lateinit var binding: FragmentCheckFlightsBinding

    private val flightViewModel: FlightsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    private var userOrigin: String? = null

    private var destination: String? = null
    private var departureDateString: String? = null
    private var returnDateString: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFlightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDateRangePicker()
        observeUserData()
        prefillFromApartmentSearch()

        val recyclerView = binding.rvFlightsList
        flightViewModel.flightResponse.observe(viewLifecycleOwner) { response ->
            recyclerView.adapter = FlightAdapter(response.data, response.dictionaries)
        }

        flightViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show or hide loading indicator
        }

        flightViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                if (it.isNotEmpty()) {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnSearchFlights.setOnClickListener {
            performManualSearch()
        }
    }

    private fun observeUserData() {
        userViewModel.loggedInUserData.observe(viewLifecycleOwner) { user ->
            Log.d("UserDataLoggedIn", user.toString())
            userOrigin = user?.city
            performSearchWithBundle()
        }
    }

    private fun performSearchWithBundle() {
        // Read the data from the bundle
        destination = arguments?.getString("destination")
        departureDateString = arguments?.getString("departureDate")
        returnDateString = arguments?.getString("returnDate")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        var departureDate: Date? = null
        var returnDate: Date? = null

        Log.d(
            "CheckFlightsBundleData",
            "$destination, $departureDateString, $returnDateString, $userOrigin"
        )

        try {
            departureDate = departureDateString?.let { dateFormat.parse(it) }
        } catch (e: ParseException) {
            Log.e("CheckFlightsFragment", "Error parsing departure date: ${e.message}")
        }

        try {
            returnDate = returnDateString?.let { dateFormat.parse(it) }
        } catch (e: ParseException) {
            Log.e("CheckFlightsFragment", "Error parsing return date: ${e.message}")
        }

        if (userOrigin != null && destination != null && departureDate != null && returnDate != null) {
            flightViewModel.searchRoundTripFlights(
                userOrigin!!,
                destination!!,
                departureDate,
                returnDate
            )
        } else {
            Toast.makeText(context, "Please enter your flight search manually", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun performManualSearch() {
        flightViewModel.clearFlightsSearch()

        val origin = binding.etOrigin.text.toString()
        val destination = binding.etDestination.text.toString()
        val departureDate = selectedStartDate
        val returnDate = selectedEndDate

        if (origin.isNotBlank() && destination.isNotBlank() && departureDate != null && returnDate != null) {
            flightViewModel.searchRoundTripFlights(origin, destination, departureDate, returnDate)
        } else {
            Toast.makeText(context, "Fill out all data to search for flights", Toast.LENGTH_LONG)
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prefillFromApartmentSearch() {
        binding.etOrigin.setText(userOrigin)
        binding.etDestination.setText(destination)
        binding.etDateRange.setText("$departureDateString - $returnDateString")
    }

    private fun setupDateRangePicker() {
        binding.etDateRange.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setTitleText("Select Dates")
            .build()

        picker.show(parentFragmentManager, "dateRangePicker")
        picker.addOnPositiveButtonClickListener { selection ->
            selection?.let {
                selectedStartDate = Date(it.first)
                selectedEndDate = Date(it.second)
                updateDateRangeDisplay()
            }
        }
    }

    private fun updateDateRangeDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startDateString = selectedStartDate?.let { dateFormat.format(it) } ?: "Not selected"
        val endDateString = selectedEndDate?.let { dateFormat.format(it) } ?: "Not selected"
        binding.etDateRange.setText("$startDateString - $endDateString")
        binding.etDateRange.contentDescription =
            "Selected date range: from $startDateString to $endDateString"
    }
}
