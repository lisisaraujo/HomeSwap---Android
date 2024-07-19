package com.example.homeswap_android

import InboxDialogFragment
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.example.homeswap_android.databinding.ActivityMainBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.android.libraries.places.api.Places
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val userViewModel: FirebaseUsersViewModel by viewModels()
    private val googlePlacesApiKey = BuildConfig.googlePlacesApiKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )



        // google places API initialization
        Places.initialize(applicationContext, googlePlacesApiKey)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController


// fix elements position when keyboard appears:
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)


        //Listener der bei jeder Navigation ausgeführt
        navController.addOnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->

            logBackStack(navController)
            Log.d("navDestination", navDestination.label.toString())

            when (navDestination.id) {
                R.id.homeFragment, R.id.checkFlightsFragment, R.id.userProfileFragment, R.id.favoritesFragment -> {
                    binding.bottomNavView.visibility = View.VISIBLE
                    binding.bottomNavView.menu.findItem(navDestination.id)?.isChecked = true
                }
                else -> binding.bottomNavView.visibility = View.GONE
            }

        }

        if (!userViewModel.isUserLoggedIn()) {
            navController.navigate(R.id.loginFragment)
        }

        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.inbox -> {
                    showInboxDialog()
                    false
                }
                else -> {
                    if (navController.currentDestination?.id != menuItem.itemId) {
                        navController.navigate(menuItem.itemId)
                    }
                    navController.popBackStack(menuItem.itemId, false)
                    true
                }
            }
        }



        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (navController.currentDestination?.id == R.id.usersListHomeFragment) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Please confirm")

                    builder.setPositiveButton("Yes") { _, _ ->
                        finish()
                    }
                    builder.setNegativeButton("No") { _, _ ->
                    }
                    builder.show()
                } else {
                    navController.navigateUp()
                }

            }

        })

    }

    //Test Code, Warnungen können ignoriert werden da der Code vor Release entfernt wird
    @SuppressLint("RestrictedApi")
    fun logBackStack(navController: NavController) {
        //Print navController Backstack
        val backStackList = navController.currentBackStack.value
        val backStackString = backStackList.map {

            it.destination.label
            it.destination.displayName.split('/').last()
            it.destination.displayName.substringAfterLast('/')

        }
        Log.d("Backstack", backStackString.toString())
    }

    private fun showInboxDialog() {
        val dialog = InboxDialogFragment()
        dialog.show(supportFragmentManager, "InboxDialogFragment")
    }

}
