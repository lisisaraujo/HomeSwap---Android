package com.example.homeswap_android.ui.apartment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentApartmentDetailsBinding
import com.example.homeswap_android.databinding.FragmentUserProfileBinding
import com.example.homeswap_android.viewModels.BottomNavViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.firebase.firestore.toObject

class ApartmentDetails : Fragment() {
    private lateinit var binding: FragmentApartmentDetailsBinding
    private val viewModel: FirebaseApartmentViewModel by activityViewModels ()
    val viewmodelBottomNav: BottomNavViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewmodelBottomNav.showBottomNavBar()

        viewModel.apartmentDataDocumentReference?.addSnapshotListener { value, error ->
            val apartment = value?.toObject<Apartment>()
            binding.apartmentTitleTV.text = apartment?.title
            binding.apartmentCityTV.text = apartment?.city
            binding.apartmentDetailsReviewsTV.text = apartment?.reviews?.size.toString()
            binding.apartmentDetailPicIV.load(apartment?.pictures?.first())
        }

        viewModel.currentApartment.observe(viewLifecycleOwner){
            if(it == null) findNavController().navigate(R.id.loginFragment)
        }


    }
}