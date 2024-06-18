package com.example.homeswap_android.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.ApartmentAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentApartmentsListHomeBinding
import com.example.homeswap_android.viewModels.BottomNavViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel

class ApartmentsListHomeFragment : Fragment() {
    private lateinit var binding: FragmentApartmentsListHomeBinding
    private val viewModelApartmentFB: FirebaseApartmentViewModel by activityViewModels()
    val viewModelBottomNav: BottomNavViewModel by activityViewModels()
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

        viewModelBottomNav.showBottomNavBar()

        val itemClickedCallback: (Apartment) -> Unit = {
            findNavController().navigate(R.id.apartmentDetails)
        }

        apartmentAdapter = ApartmentAdapter(emptyList(), itemClickedCallback)
        binding.rvApartmentsList.adapter = apartmentAdapter

        viewModelApartmentFB.fetchApartments()

        viewModelApartmentFB.apartments.observe(viewLifecycleOwner) { apartments ->
            Log.d("Apartments", apartments.toString())
            apartmentAdapter.updateApartments(apartments)
        }
    }
}
