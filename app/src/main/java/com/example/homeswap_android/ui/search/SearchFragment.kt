package com.example.homeswap_android.ui.search

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentSearchBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val apartmentViewModel: FirebaseApartmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.showDatePickerButton.setOnClickListener {
            showDateRangePicker()
        }

        apartmentViewModel.apartmentsBySearch.observe(viewLifecycleOwner) { apartmentsBySearchList ->
            Log.d("ApartmentsBySearch", apartmentsBySearchList.toString())
            if (apartmentsBySearchList.isNotEmpty()) {
                findNavController().navigate(R.id.searchResultsFragment)
            }
        }

        binding.searchButton.setOnClickListener {
            performSearch()
        }

        binding.searchApartmentBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setTitleText("Select Dates")
            .build()

        picker.show(parentFragmentManager, "dateRangePicker")
        picker.addOnPositiveButtonClickListener { selection ->
            val startDate = convertTimeToDate(selection.first)
            val endDate = convertTimeToDate(selection.second)
            binding.selectedDateRange.text = "$startDate - $endDate"
        }
    }

    private fun convertTimeToDate(time: Long): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = time
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(utc.time)
    }

    private fun performSearch() {
        val country = binding.countryInput.text.toString().trim()
        val city = binding.cityInput.text.toString().trim()
        val dateRange = binding.selectedDateRange.text.toString()

        // process date range
        val startDate: String?
        val endDate: String?
        if (dateRange.contains(" - ")) {
            val dates = dateRange.split(" - ")
            startDate = dates[0]
            endDate = dates[1]
        } else {
            startDate = null
            endDate = null
        }

        apartmentViewModel.searchApartments(
            city = city.takeIf { it.isNotBlank() },
            country = country.takeIf { it.isNotBlank() },
            startDate = startDate,
            endDate = endDate
        )
    }

}
