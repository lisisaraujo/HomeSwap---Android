package com.example.homeswap_android.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.adapter.UserAdapter
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentUsersListHomeBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.firebase.firestore.toObject

class UsersListHomeFragment : Fragment() {

    private val TAG = "UsersListHomeFragment"

    private lateinit var binding: FragmentUsersListHomeBinding
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersListHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemClickedCallback: (UserData) -> Unit = { userData ->
            Log.d(TAG, userData.userID!!)
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToUserDetailsFragment(userData.userID)
            )
        }

        userAdapter = UserAdapter(emptyList(), itemClickedCallback)
        binding.rvUsersList.adapter = userAdapter

        userViewModel.fetchUsers()

        userViewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.updateUsers(users)
        }

    }
}
