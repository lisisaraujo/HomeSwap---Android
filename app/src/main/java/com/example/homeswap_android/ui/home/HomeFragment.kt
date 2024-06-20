package com.example.homeswap_android.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.homeswap_android.adapter.ViewPagerAdapter
import com.example.homeswap_android.databinding.FragmentHomeBinding
import com.example.homeswap_android.ui.apartment.AddApartmentFragmentDirections
import com.example.homeswap_android.ui.apartment.ApartmentDetailsFragmentArgs
import com.google.android.material.tabs.TabLayout

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val args: HomeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragments = listOf(UsersListHomeFragment(), ApartmentsListHomeFragment())
        val adapter =
            ViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle, fragments)


        binding.viewPager.adapter = adapter
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Users"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Apartments"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        Log.d("TabChangeHome", args.isApartments.toString())

        if (args.isApartments) {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1))
            binding.viewPager.setCurrentItem(1, false)
        }
    }


}
