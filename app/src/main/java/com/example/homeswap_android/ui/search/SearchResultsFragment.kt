package com.example.homeswap_android.ui.search

import ApartmentAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentSearchResultsBinding
import com.example.homeswap_android.viewModels.FiltersViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.material.chip.Chip

class SearchResultsFragment : Fragment() {

    val TAG = "SearchResultsFragment"
    private lateinit var binding: FragmentSearchResultsBinding
    private val apartmentsViewModel: FirebaseApartmentsViewModel by activityViewModels()

    private lateinit var apartmentAdapter: ApartmentAdapter
    private val args: SearchResultsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filters = args.filters

        if(!filters.isNullOrEmpty()){
            displaySelectedFilters(filters)
        }

        // Create a bundle
        val bundle = Bundle()
        bundle.putString("destination", args.destination)
        bundle.putString("departureDate", args.departureDate)
        bundle.putString("returnDate", args.returnDate)



        apartmentsViewModel.getApartments()

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(
                SearchResultsFragmentDirections.actionSearchResultsFragmentToApartmentDetailsFragment(
                    apartment.apartmentID
                )
            )
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            apartmentsViewModel.toggleLike(apartment)
        }

        apartmentAdapter = ApartmentAdapter(
            emptyList(),
            itemClickedCallback,
            onLikeClickListener
        )
        binding.searchResultRV.adapter = apartmentAdapter

        apartmentsViewModel.apartmentsBySearch.observe(viewLifecycleOwner) { apartmentsBySearch ->
            Log.d(TAG ,apartmentsBySearch.toString())
            if (!apartmentsBySearch.isNullOrEmpty()) {
                apartmentAdapter.updateApartments(apartmentsBySearch)
                updateLoadingState(false)
                binding.searchResultsInfoTV.text =
                    "Found ${apartmentsBySearch.size} results for your search"
            } else {
                Toast.makeText(context, "No apartments found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.flightSearchChip.setOnClickListener {
            findNavController().navigate(R.id.checkFlightsFragment, bundle)
        }

        apartmentsViewModel.loadingApartments.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
        }

    }

    private fun displaySelectedFilters(filtersString: String) {
        val filters = filtersString.removeSurrounding("{", "}")
            .split(", ")
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to parts[1]
                } else {
                    null
                }
            }.toMap()

        filters.forEach { (key, value) ->
            when (key) {
                "typeOfHome", "rooms", "maxGuests" -> addFilterChip("$key: $value")
                "amenities" -> {
                    val amenities = value.removeSurrounding("[", "]").split(", ")
                    amenities.forEach { amenity ->
                        if (amenity.isNotBlank()) {
                            addFilterChip(amenity.trim('"')) //remove quotes if present
                        }
                    }
                }
            }
        }
    }

    private fun addFilterChip(text: String) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                // Handle removing the filter
                binding.selectedFiltersChipGroup.removeView(this)
                // You might want to update the search results here
            }
        }
        binding.selectedFiltersChipGroup.addView(chip)
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
