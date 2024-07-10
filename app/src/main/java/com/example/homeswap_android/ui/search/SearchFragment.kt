package com.example.homeswap_android.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentSearchBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient

    private var startDate: String? = null
    private var endDate: String? = null
    private var destination: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesClient = Places.createClient(requireContext())

        Utils.setupAutoCompleteTextView(requireContext(), binding.destinationInput, placesClient) { selectedPlace ->
            destination = selectedPlace.split(",").firstOrNull()?.trim()
            binding.destinationInput.setText(selectedPlace)
        }

        binding.showDatePickerButton.setOnClickListener {
            Utils.showDateRangePicker(parentFragmentManager) { start, end ->
                startDate = start
                endDate = end
                binding.selectedDateRange.text = "$start - $end"
            }
        }

        apartmentViewModel.apartmentsBySearch.observe(viewLifecycleOwner) { apartmentsBySearchList ->
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
            apartmentViewModel.searchApartments(
                city = destination.takeIf { it!!.isNotBlank() },
                startDate = startDate,
                endDate = endDate
            )
        }

        binding.searchApartmentBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
