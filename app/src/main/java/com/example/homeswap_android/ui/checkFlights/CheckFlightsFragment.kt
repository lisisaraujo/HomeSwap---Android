package com.example.homeswap_android.ui.checkFlights

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.FlightAdapter
import com.example.homeswap_android.databinding.FragmentCheckFlightsBinding
import com.example.homeswap_android.viewModels.FlightsViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckFlightsFragment : Fragment() {
    private val viewModel: FlightsViewModel by activityViewModels()
    private lateinit var binding: FragmentCheckFlightsBinding
    private val args: CheckFlightsFragmentArgs by navArgs()

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFlightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDateRangePicker()
        prefillFromApartmentSearch()


        val recyclerView = binding.rvFlightsList
        viewModel.flightResponse.observe(viewLifecycleOwner) { response ->
            recyclerView.adapter = FlightAdapter(response.data, response.dictionaries)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            //show or hide loading indicator
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }


        //perform immediate search if coming from SearchResults fragment
        if (args.origin != null && args.destination != null && args.departureDate != null && args.returnDate != null) {
            performSearchWithArgs()
        }

        binding.btnSearchFlights.setOnClickListener {
            performManualSearch()
        }

    }


    private fun performSearchWithArgs() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val departureDate = args.departureDate?.let { dateFormat.parse(it) }
        val returnDate = args.returnDate?.let { dateFormat.parse(it) }

        if (args.origin != null && args.destination != null && departureDate != null && returnDate != null) {
            viewModel.searchRoundTripFlights(
                args.origin!!,
                args.destination!!,
                departureDate,
                returnDate
            )
        } else {
            Toast.makeText(context, "Please enter your flight search manually", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun performManualSearch() {
        val origin = binding.etOrigin.text.toString()
        val destination = binding.etDestination.text.toString()
        val departureDate = selectedStartDate
        val returnDate = selectedEndDate

        if (origin.isNotBlank() && destination.isNotBlank() && departureDate != null && returnDate != null) {
            viewModel.searchRoundTripFlights(origin, destination, departureDate, returnDate)
        } else {
            Toast.makeText(context, "Fill out all data to search for flights", Toast.LENGTH_LONG)
                .show()
        }
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

    @SuppressLint("SetTextI18n")
    private fun prefillFromApartmentSearch() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        args.origin?.let { binding.etOrigin.setText(it) }
        args.destination?.let { binding.etDestination.setText(it) }

        if (args.departureDate != null && args.returnDate != null) {
            binding.etDateRange.setText("${args.departureDate} - ${args.returnDate}")
            selectedStartDate = args.departureDate?.let { dateFormat.parse(it) }
            selectedEndDate = args.returnDate?.let { dateFormat.parse(it) }
        }
    }

}