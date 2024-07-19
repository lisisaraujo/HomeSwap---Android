package com.example.homeswap_android.ui.apartment

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
import com.example.homeswap_android.adapter.PhotoAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentApartmentPicturesBinding
import com.example.homeswap_android.ui.home.HomeFragmentDirections
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel


class ApartmentPicturesFragment : Fragment() {

    val TAG = "ApartmentPicturesFragment"

    private lateinit var binding: FragmentApartmentPicturesBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private val args: ApartmentDetailsFragmentArgs by navArgs()
    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentPicturesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apartmentID = args.apartmentID
        val userID = apartmentViewModel.currentApartment.value?.userID!!

        val itemClickedCallback: (Int) -> Unit = { position ->
            Log.d(TAG, "Navigating to apartment single pictures: $position")
            findNavController().navigate(
                ApartmentPicturesFragmentDirections.actionApartmentPicturesFragmentToApartmentSinglePictureFragment(
                    position,
                    apartmentID,
                    userID
                )
            )
        }

        photoAdapter = PhotoAdapter(emptyList(), itemClickedCallback)
        binding.recyclerView.adapter = photoAdapter

        apartmentViewModel.getApartmentPictures(apartmentID, userID)
            .observe(viewLifecycleOwner) { pictureUrls ->
                if (pictureUrls.isNotEmpty()) {
                    photoAdapter.updatePhotos(pictureUrls)
                } else {
                    Log.d("ApartmentPictures", "No pictures found for this apartment")
                }
            }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
}