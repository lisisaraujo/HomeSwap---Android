package com.example.homeswap_android.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class BottomNavViewModel : ViewModel() {

    private val _bottomNavBarVisible = MutableLiveData<Boolean>(false)
    val bottomNavBarVisible: LiveData<Boolean?>
        get() = _bottomNavBarVisible

    fun showBottomNavBar(): Boolean {
        val isVisible = true
        _bottomNavBarVisible.value = isVisible
        return isVisible
    }

    fun hideBottomNavBar(): Boolean{
        val isVisible = false
        _bottomNavBarVisible.value = isVisible
        return isVisible
    }
}