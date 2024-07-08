package com.example.homeswap_android.ui.search

import android.annotation.SuppressLint
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
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val country = binding.countryInput.text.toString().trim()
        val city = binding.cityInput.text.toString().trim()
        val dateRange = binding.selectedDateRange.text.toString()

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

        binding.showDatePickerButton.setOnClickListener {
            showDateRangePicker()
        }

        apartmentViewModel.apartmentsBySearch.observe(viewLifecycleOwner) { apartmentsBySearchList ->
            Log.d("ApartmentsBySearch", apartmentsBySearchList.toString())
            if (apartmentsBySearchList.isNotEmpty()) {
                findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToSearchResultsFragment(city, startDate, endDate ))
            }
        }

        binding.searchButton.setOnClickListener {
            apartmentViewModel.searchApartments(
                city = city.takeIf { it.isNotBlank() },
                country = country.takeIf { it.isNotBlank() },
                startDate = startDate,
                endDate = endDate
            )
        }

        binding.searchApartmentBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setTitleText("Select Dates")
            .build()

        picker.show(parentFragmentManager, "dateRangePicker")
        picker.addOnPositiveButtonClickListener { selection ->
            val startDate = Date(selection.first)
            val endDate = Date(selection.second)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.selectedDateRange.text = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        }
    }

}