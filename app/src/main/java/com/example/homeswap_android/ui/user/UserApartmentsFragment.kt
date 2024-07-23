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
import com.example.homeswap_android.adapter.EditApartmentAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentMyListingsBinding
import com.example.homeswap_android.databinding.FragmentUserApartmentsBinding
import com.example.homeswap_android.ui.apartment.ApartmentDetailsFragmentArgs
import com.example.homeswap_android.ui.home.HomeFragmentDirections
import com.example.homeswap_android.ui.personal.options.MyListingsFragmentDirections
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class UserApartmentsFragment : Fragment() {

    private lateinit var binding: FragmentUserApartmentsBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private val args: UserApartmentsFragmentArgs by navArgs()


    private lateinit var apartmentAdapter: ApartmentAdapter

    val TAG = "UserApartmentsFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserApartmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val userID = args.userID

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            Log.d("ItemCallbackApartment", apartment.apartmentID)
            findNavController().navigate(
                UserApartmentsFragmentDirections.actionUserApartmentsFragmentToApartmentDetailsFragment(
                    apartment.apartmentID
                )
            )
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            Log.d(
                "LikeClicked",
                "onLikeClickListener called for apartment ${apartment.apartmentID}"
            )
            apartmentViewModel.toggleLike(apartment)
        }


        apartmentAdapter = ApartmentAdapter(
            emptyList(),
            itemClickedCallback,
            onLikeClickListener
        )
        binding.userApartmentsListRV.adapter = apartmentAdapter

        apartmentViewModel.getUserApartments(userID!!).addSnapshotListener { userApartments, _ ->
            Log.d(TAG, userApartments.toString())
            apartmentAdapter.updateApartments(userApartments!!.toObjects(Apartment::class.java))
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()

        }

    }

}