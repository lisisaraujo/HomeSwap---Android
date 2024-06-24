package com.example.homeswap_android.ui.home

import ApartmentAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentApartmentsListHomeBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel

class ApartmentsListHomeFragment : Fragment() {
    private lateinit var binding: FragmentApartmentsListHomeBinding
    private val apartmentsViewModel: FirebaseApartmentViewModel by activityViewModels()
    private lateinit var apartmentAdapter: ApartmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentsListHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apartmentsViewModel.fetchApartments()

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            Log.d("ItemCallbackApartment", apartment.apartmentID)
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToApartmentDetailsFragment(apartment.apartmentID))
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            Log.d("LikeClicked", "onLikeClickListener called for apartment ${apartment.apartmentID}")
            apartmentsViewModel.toggleLike(apartment)
        }

        apartmentAdapter = ApartmentAdapter(
            emptyList(),
            itemClickedCallback,
            onLikeClickListener
        )
        binding.apartmentListRV.adapter = apartmentAdapter

        apartmentsViewModel.apartments.observe(viewLifecycleOwner) { apartments ->
            Log.d("Apartments", apartments.toString())
            apartmentAdapter.updateApartments(apartments)
        }

    }
}