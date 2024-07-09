package com.example.homeswap_android.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    var startDate: String? = null
    var endDate: String? = null
    var country: String? = null
    var destination: String? = null

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
            Log.d("SearchFragment", "$startDate, $endDate, $destination")
            if (apartmentsBySearchList.isNotEmpty()) {
                findNavController().navigate(
                    SearchFragmentDirections.actionSearchFragmentToSearchResultsFragment(
                        destination = destination,
                        departureDate = startDate,
                        returnDate = endDate
                    )
                )
            } else {
                Toast.makeText(context, "No apartments found", Toast.LENGTH_LONG).show()
            }
        }

        binding.searchButton.setOnClickListener {

            country = binding.countryInput.text.toString().trim()
            destination = binding.cityInput.text.toString().trim()

            Log.d("SearchFragmentStartDate", "$startDate, $endDate")
            Log.d("SearchFragmentDestinationCountry", "$destination, $country")
            apartmentViewModel.searchApartments(
                city = destination.takeIf { it!!.isNotBlank() },
                country = country.takeIf { it!!.isNotBlank() },
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
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            startDate = dateFormat.format(Date(selection.first))
            endDate = dateFormat.format(Date(selection.second))
            binding.selectedDateRange.text =
                "$startDate - $endDate"
        }


    }
}
