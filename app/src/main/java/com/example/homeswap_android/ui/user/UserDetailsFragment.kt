package com.example.homeswap_android.ui.user

import ApartmentAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentUserDetailsBinding
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

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(
                UserDetailsFragmentDirections.actionUserDetailsFragmentToApartmentDetailsFragment(apartment.apartmentID)
            )
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            apartmentViewModel.toggleLike(apartment)
        }

        apartmentAdapter = ApartmentAdapter(emptyList(), itemClickedCallback, onLikeClickListener)
        binding.userApartmentsRV.adapter = apartmentAdapter

        apartmentViewModel.fetchUserApartments(userID).addSnapshotListener{ userApartments, _ ->
            Log.d(TAG, userApartments.toString())
            apartmentAdapter.updateApartments(userApartments!!.toObjects(Apartment::class.java))
        }

        binding.backBTN.setOnClickListener {
            findNavController().navigate(UserDetailsFragmentDirections.actionUserDetailsFragmentToHomeFragment(isUsers = true))
        }

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) findNavController().navigate(R.id.loginFragment)
        }
    }
}