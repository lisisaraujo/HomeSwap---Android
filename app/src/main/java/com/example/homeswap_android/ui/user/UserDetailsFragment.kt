package com.example.homeswap_android.ui.user

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
import com.example.homeswap_android.databinding.FragmentUserDetailsBinding
import com.example.homeswap_android.ui.apartment.AddApartmentFragmentDirections
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class UserDetailsFragment : Fragment() {
    private lateinit var binding: FragmentUserDetailsBinding
    private val apartmentViewModel: FirebaseApartmentViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private val args: UserDetailsFragmentArgs by navArgs()

    private lateinit var apartmentAdapter: ApartmentAdapter

    val TAG = "UserDetailsFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userID = args.userID!!

        userViewModel.fetchUserData(userID)
        apartmentViewModel.fetchUserApartments(userID)

        userViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.userProfileNameTV.text = user.name
                binding.userProfileIV.load(user.profilePic)
                binding.userProfileEmailTV.text = user.email
            }
        }

        apartmentAdapter = ApartmentAdapter(emptyList()) { apartment ->

        }
        binding.apartmentsRecyclerView.adapter = apartmentAdapter

        apartmentViewModel.fetchUserApartments(userID)

        apartmentViewModel.userApartments.observe(viewLifecycleOwner) { apartments ->
            apartmentAdapter.updateApartments(apartments)
        }

        binding.backBTN.setOnClickListener {
            findNavController().navigate(UserDetailsFragmentDirections.actionUserDetailsFragmentToHomeFragment(isUsers = true))

        }

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) findNavController().navigate(R.id.loginFragment)
        }
    }
}