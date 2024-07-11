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
import com.example.homeswap_android.viewModels.FiltersViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.math.max

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val filtersViewModel: FiltersViewModel by activityViewModels()

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

        setupTypeOfHomeDropdown()
        setupSliders()
        setupAdditionalFiltersToggle()

        placesClient = Places.createClient(requireContext())

        Utils.setupAutoCompleteTextView(
            requireContext(),
            binding.destinationInput,
            placesClient
        ) { selectedPlace ->
            destination = selectedPlace.split(",").firstOrNull()?.trim()
            binding.destinationInput.setText(selectedPlace)
        }

        binding.etDateRange.setOnClickListener {
            Utils.showDateRangePicker(parentFragmentManager) { start, end ->
                startDate = start
                endDate = end
                binding.selectedDateRange.hint = "$start - $end"
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
            val typeOfHome = binding.typeOfHomeAutoComplete.text.toString()
            val petsAllowed = binding.petsAllowedSwitch.isChecked
            val homeOffice = binding.homeOfficeSwitch.isChecked
            val hasWifi = binding.hasWifiSwitch.isChecked
            val rooms = binding.roomsSlider.value.toInt()
            val maxGuests = binding.maxGuestsSlider.value.toInt()

            apartmentViewModel.searchApartments(
                city = destination.takeIf { !it.isNullOrBlank() },
                startDate = startDate.takeIf { it.isNullOrEmpty() },
                endDate = endDate.takeIf { it.isNullOrEmpty() },
                typeOfHome = typeOfHome.takeIf { !it.isBlank() },
                petsAllowed = petsAllowed.takeIf { it != null },
                homeOffice = homeOffice.takeIf { it != null },
                hasWifi = hasWifi.takeIf { it != null },
                rooms = rooms.takeIf { it != 0 },
                maxGuests = maxGuests.takeIf { it != 0 }

                )
        }

        binding.searchApartmentBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTypeOfHomeDropdown() {
        val items = listOf("Apartment", "House", "Studio", "Villa", "Cottage")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        (binding.typeOfHomeAutoComplete as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupSliders() {
        binding.roomsSlider.addOnChangeListener { _, value, _ ->
            binding.roomsLabel.text = "Number of Rooms: ${value.toInt()}"
        }

        binding.maxGuestsSlider.addOnChangeListener { _, value, _ ->
            binding.maxGuestsLabel.text = "Max Guests: ${value.toInt()}"
        }
    }

    private fun setupAdditionalFiltersToggle() {
        binding.additionalFiltersHeader.setOnClickListener {
            val isVisible = binding.additionalFiltersContent.visibility == View.VISIBLE
            binding.additionalFiltersContent.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.additionalFiltersArrow.setImageResource(
                if (isVisible) R.drawable.baseline_arrow_drop_down_24 else R.drawable.baseline_keyboard_arrow_up_24
            )
        }
    }
}

