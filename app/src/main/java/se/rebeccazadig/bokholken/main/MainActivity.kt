package se.rebeccazadig.bokholken.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import se.rebeccazadig.bokholken.R
import se.rebeccazadig.bokholken.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private val viewModel: MainActivityViewModel by viewModels()
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment).navController
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val currentDest = navController.currentDestination?.id
            if (item.itemId != currentDest) {
                val builder = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.advertsFragment, false)
                val options = builder.build()
                try {
                    // Try to make the navigation to the selected item's destination
                    navController.navigate(item.itemId, null, options)
                    true
                } catch (e: IllegalArgumentException) {
                    false
                }
            } else false
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginRegisterFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }

        if (savedInstanceState == null && !viewModel.isLoggedIn()) {
            navController.navigate((R.id.action_to_login_nav_graph))
        } else {
            navController.navigate(R.id.advertsFragment)
        }
    }
}

