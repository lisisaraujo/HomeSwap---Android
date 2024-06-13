package com.example.homeswap_android.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.UserAdapter
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentUsersListHomeBinding
import com.example.homeswap_android.viewModels.FirebaseViewModel
import com.google.firebase.firestore.toObject

class UsersListHomeFragment : Fragment() {
    private lateinit var binding: FragmentUsersListHomeBinding
    private val viewModel: FirebaseViewModel by activityViewModels()
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

        val itemClickedCallback: (UserData) -> Unit = {
            findNavController().navigate(R.id.userProfileFragment)
        }

        userAdapter = UserAdapter(emptyList(), itemClickedCallback)
        binding.rvUsersList.adapter = userAdapter

        viewModel.fetchUsers()
        val users = viewModel.fetchUsers()
        Log.d("Users", users.toString())

        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.updateUsers(users)
        }

        viewModel.userDataDocumentReference?.addSnapshotListener { value, error ->
            val profile = value?.toObject<UserData>()
            if (profile != null) {
                viewModel.setProfile(profile)
            }
        }
    }
}
