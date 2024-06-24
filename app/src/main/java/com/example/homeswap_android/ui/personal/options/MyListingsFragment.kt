package com.example.homeswap_android.ui.personal.options

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
import com.example.homeswap_android.adapter.EditApartmentAdapter
import com.example.homeswap_android.data.models.Apartment
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

    private lateinit var editApartmentAdapter: EditApartmentAdapter

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

        val userID = userViewModel.currentUser.value?.uid

        editApartmentAdapter = EditApartmentAdapter(emptyList()) { apartment ->
            findNavController().navigate(
                MyListingsFragmentDirections.actionMyListingsFragmentToEditApartmentFragment(
                    apartment.apartmentID
                )
            )
        }
        binding.myListingsRV.adapter = editApartmentAdapter

        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            apartmentViewModel.fetchUserApartments(user!!.uid)
        }

        apartmentViewModel.userApartments.observe(viewLifecycleOwner) { apartments ->

        }

        apartmentViewModel.fetchUserApartments(userID!!).addSnapshotListener { userApartments, _ ->
            Log.d(TAG, userApartments.toString())
            editApartmentAdapter.updateApartments(userApartments!!.toObjects(Apartment::class.java))
        }

        binding.myListingsBackBTN.setOnClickListener {
            findNavController().navigateUp()

        }

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) findNavController().navigate(R.id.loginFragment)
        }
    }
}