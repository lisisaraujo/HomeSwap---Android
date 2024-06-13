package com.example.homeswap_android.ui.home

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_explore -> {
                changeMenuItemColor(item)
                true
            }
            R.id.navigation_liked -> {
                // Handle click for menu item 2
                changeMenuItemColor(item)
                true
            }
            R.id.navigation_flights -> {
                // Handle click for menu item 2
                changeMenuItemColor(item)
                true
            }
            R.id.navigation_inbox -> {
                // Handle click for menu item 2
                changeMenuItemColor(item)
                true
            }
            R.id.navigation_personal -> {
                // Handle click for menu item 2
                changeMenuItemColor(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun changeMenuItemColor(item: MenuItem) {
        val color = ContextCompat.getColor(requireContext(), R.color.primary) // Replace with your desired color
        item.title = SpannableString(item.title).apply {
            setSpan(ForegroundColorSpan(color), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

