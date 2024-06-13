package com.example.homeswap_android.ui.registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentRegisterBinding
import com.example.homeswap_android.viewModels.FirebaseViewModel

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: FirebaseViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.continueBTN.setOnClickListener {
            val name = binding.nameET.text.toString().trim()
            val email = binding.emailET.text.toString().trim()
            val password = binding.passwordET.text.toString().trim()

            val newUserData = UserData(
                name = name,
                email = email,
                profilePic = "",
                reviews = emptyList(),
                apartment = Apartment("", "", "", "", "", listOf(), listOf())
            )

            viewModel.register(email, password, newUserData)

        }
        viewModel.currentUser.observe(viewLifecycleOwner){
            if(it != null) findNavController().navigate(R.id.registerProfileDetailsFragment)
        }

    }
}
