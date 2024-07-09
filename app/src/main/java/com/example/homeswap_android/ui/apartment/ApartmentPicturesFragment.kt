package com.example.homeswap_android.ui.apartment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.adapter.PhotoAdapter
import com.example.homeswap_android.databinding.FragmentApartmentPicturesBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel


class ApartmentPicturesFragment : Fragment() {

    val TAG = "ApartmentDetailsFragment"

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


        photoAdapter = PhotoAdapter(emptyList())
        binding.recyclerView.adapter = photoAdapter

        val apartmentID = args.apartmentID
        val userID = userViewModel.loggedInUser.value?.uid!!

        apartmentViewModel.getApartmentPictures(apartmentID, userID).observe(viewLifecycleOwner) { pictureUrls ->
            if (pictureUrls.isNotEmpty()) {
                photoAdapter.updatePhotos(pictureUrls)
            } else {
                Log.d("ApartmentPictures", "No pictures found for this apartment")
            }
        }
    }
}