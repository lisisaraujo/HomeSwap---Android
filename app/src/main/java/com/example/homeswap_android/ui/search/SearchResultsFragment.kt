package com.example.homeswap_android.ui.search

import ApartmentAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentSearchResultsBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.FlightsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class SearchResultsFragment : Fragment() {
    private lateinit var binding: FragmentSearchResultsBinding
    private val apartmentsViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val flightsViewModel: FlightsViewModel by activityViewModels()
    private val usersViewModel: FirebaseUsersViewModel by activityViewModels()


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

        val user = usersViewModel.currentUserData
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val destination = args.location
        val departureDate = args.startDate?.let { dateString -> dateFormat.parse(dateString) }
        val returnDate = args.endDate?.let { dateString -> dateFormat.parse(dateString) }
        val origin = user.value!!.city


        apartmentsViewModel.getApartments()

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            Log.d("ItemCallbackApartment", apartment.apartmentID)
            findNavController().navigate(
                SearchResultsFragmentDirections.actionSearchResultsFragmentToApartmentDetailsFragment(
                    apartment.apartmentID
                )
            )
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            Log.d(
                "LikeClicked",
                "onLikeClickListener called for apartment ${apartment.apartmentID}"
            )
            apartmentsViewModel.toggleLike(apartment)
        }

        apartmentAdapter = ApartmentAdapter(
            emptyList(),
            itemClickedCallback,
            onLikeClickListener
        )
        binding.searchResultRV.adapter = apartmentAdapter

        apartmentsViewModel.apartmentsBySearch.observe(viewLifecycleOwner) { apartmentsBySearch ->
            Log.d("apartmentsBySearch", apartmentsBySearch.toString())
            if (!apartmentsBySearch.isNullOrEmpty()) {
                apartmentAdapter.updateApartments(apartmentsBySearch)
                binding.searchResultsInfoTV.text =
                    "Found ${apartmentsBySearch.size} results for your search"
                apartmentsViewModel.clearSearch()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.flightSearchChip.setOnClickListener {
            Log.d("SearchResults", "Destination: $destination, Origin: $origin, DepartureDate: $departureDate, ReturnDate: $returnDate")

            findNavController().navigate(
                SearchResultsFragmentDirections.actionSearchResultsFragmentToCheckFlightsFragment(
                    destination,
                    origin,
                    departureDate.toString(),
                    returnDate.toString()
                )
            )
        }
    }
}