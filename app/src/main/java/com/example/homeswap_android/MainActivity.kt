package com.example.homeswap_android

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.homeswap_android.databinding.ActivityMainBinding
import com.example.homeswap_android.viewModels.BottomNavViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val viewmodel: BottomNavViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavView.setupWithNavController(navController)

        //Listener der bei jeder Navigation ausgeführt
        navController.addOnDestinationChangedListener{ navController: NavController, navDestination: NavDestination, bundle: Bundle? ->

            logBackStack(navController)
            Log.d("navDestination", navDestination.label.toString())

            when(navDestination.id){
                //Entfernt alle Destinations vom Stack bis zum ersten Destination mit der angegebenen id
                R.id.usersListHomeFragment -> navController.popBackStack(R.id.usersListHomeFragment, false)
                R.id.checkFlightsFragment -> navController.popBackStack(R.id.checkFlightsFragment, false)
            }
        }

        //Listener der bei BottomNavigation Klick ausgeführt
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->

            //Rufe die Funktion auf die standardmäßig für die bottom navigation zuständig ist.
            NavigationUI.onNavDestinationSelected(menuItem, navController)

            navController.popBackStack(menuItem.itemId, false)

            true
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {


                if (navController.currentDestination?.id == R.id.usersListHomeFragment) {


                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Bitte Bestätigen")

                    builder.setPositiveButton("Ja") { _, _ ->
                        finish()
                    }

                    builder.setNegativeButton("Nein") { _, _ ->

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

            //it.destination.label
            //it.destination.displayName.split('/').last()
            it.destination.displayName.substringAfterLast('/')

        }
        Log.d("Backstack", backStackString.toString())
    }
}