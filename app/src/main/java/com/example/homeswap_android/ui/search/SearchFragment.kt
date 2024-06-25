package com.example.homeswap_android.ui.search

import ApartmentAdapter
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R

import com.example.homeswap_android.databinding.FragmentSearchBinding
import com.example.homeswap_android.databinding.FragmentSearchResultsBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val apartmentViewModel: FirebaseApartmentViewModel by activityViewModels()
    private lateinit var apartmentAdapter: ApartmentAdapter

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

        binding.searchButton.setOnClickListener {
            performSearch()
            findNavController().navigate(R.id.searchResultsFragment)
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
        val location = binding.locationInput.text.toString()
        val dateRange = binding.selectedDateRange.text.toString()

        apartmentViewModel.searchByCity(location)
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding = null
//    }
}
