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
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.EditApartmentAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentMyListingsBinding
import com.example.homeswap_android.ui.user.UserDetailsFragmentArgs
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class MyListingsFragment : Fragment() {
    private lateinit var binding: FragmentMyListingsBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
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

        val userID = userViewModel.loggedInUser.value?.uid

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(
                MyListingsFragmentDirections.actionMyListingsFragmentToEditApartmentFragment(apartment.apartmentID)
            )
        }


        editApartmentAdapter = EditApartmentAdapter(emptyList(), itemClickedCallback)

        binding.myListingsRV.adapter = editApartmentAdapter

        apartmentViewModel.getUserApartments(userID!!).addSnapshotListener { userApartments, _ ->
            Log.d(TAG, userApartments.toString())
            editApartmentAdapter.updateApartments(userApartments!!.toObjects(Apartment::class.java))
        }

        binding.myListingsBackBTN.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)

        }

//        userViewModel.loggedInUser.observe(viewLifecycleOwner) {
//            if (it == null) findNavController().navigate(R.id.loginFragment)
//        }
    }
}