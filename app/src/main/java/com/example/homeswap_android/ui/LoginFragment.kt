package com.example.homeswap_android.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentLoginBinding
import com.example.homeswap_android.viewModels.FirebaseViewModel

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    private val viewModel: FirebaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        viewModel.signOut()

        binding.loginBTN.setOnClickListener {
            val email = binding.emailET.text.toString()
            val password = binding.passwordET.text.toString()

            viewModel.login(email, password)
        }

        binding.joinTextView.setOnClickListener {
        findNavController().navigate(R.id.registerFragment)
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                //User ist nicht eingeloggt
                Log.d("CurrentUser", "Kein User eingeloggt")
            } else {
                //User ist eingeloggt
                Log.d("CurrentUser", user.uid)
                findNavController().navigate(R.id.usersListHomeFragment)
            }

        }

        binding.checkFlightsBTN.setOnClickListener {
         findNavController().navigate(R.id.checkFlightsFragment)
        }

//        binding.openCalendarBTN.setOnClickListener {
//            findNavController().navigate(R.id.calendarFragment)
//        }
    }
}