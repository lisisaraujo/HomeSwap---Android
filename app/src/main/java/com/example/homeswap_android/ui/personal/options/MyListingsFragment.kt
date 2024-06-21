package com.example.homeswap_android.ui.personal.options

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.ApartmentAdapter
import com.example.homeswap_android.databinding.FragmentMyListingsBinding
import com.example.homeswap_android.databinding.FragmentUserDetailsBinding
import com.example.homeswap_android.ui.user.UserDetailsFragmentArgs
import com.example.homeswap_android.ui.user.UserDetailsFragmentDirections
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class MyListingsFragment : Fragment() {
    private lateinit var binding: FragmentMyListingsBinding
    private val apartmentViewModel: FirebaseApartmentViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private val args: UserDetailsFragmentArgs by navArgs()

    private lateinit var apartmentAdapter: ApartmentAdapter

    val TAG = "MyListingsFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyListingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        apartmentAdapter = ApartmentAdapter(emptyList()) { apartment ->
            findNavController().navigate(
                MyListingsFragmentDirections.actionMyListingsFragmentToEditApartmentFragment(apartment.apartmentID)
            )
        }
        binding.apartmentsRecyclerView.adapter = apartmentAdapter

        userViewModel.currentUser.observe(viewLifecycleOwner){user ->
            apartmentViewModel.fetchUserApartments(user!!.uid)
        }

        apartmentViewModel.userApartments.observe(viewLifecycleOwner) { apartments ->
            apartmentAdapter.updateApartments(apartments)
        }

        binding.myListingsBackBTN.setOnClickListener {
            findNavController().navigateUp()

        }

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) findNavController().navigate(R.id.loginFragment)
        }
    }
}